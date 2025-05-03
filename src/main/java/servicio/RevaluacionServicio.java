package servicio;

import util.ConexionBD;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RevaluacionServicio {

    public RevaluacionServicio() {
    }

    public List<Object[]> cargarDatosDesdeBD() {
        List<Object[]> datos = new ArrayList<>();
        String query = """
        SELECT 
            a.id AS id_activo,
            a.codigo_patrimonial,
            a.nombre,
            a.categoria,
            a.estado,
            COALESCE(r.valor_anterior, a.costo_inicial) AS valor_anterior,
            COALESCE(r.valor_revaluado, '') AS valor_revaluado,
            COALESCE(r.depreciacion_anterior, a.depreciacion_mensual) AS depreciacion_anterior, -- Aquí se modifica para incluir la depreciación anterior
            COALESCE(r.nueva_depreciacion, '') AS nueva_depreciacion,
            COALESCE(r.impacto_total, 0) AS impacto_total
        FROM activos a
        LEFT JOIN movimientos m ON a.id = m.id_activo AND m.tipo_movimiento = 'Revaluación'
        LEFT JOIN revaluaciones r ON r.id_movimiento = m.id
        WHERE a.estado NOT IN ('Depreciado', 'Baja', 'Baja por venta', 'Renovado') -- Excluir activos con estados no deseados
    """;

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                double valorAnterior = rs.getDouble("valor_anterior");
                double valorRevaluado = rs.getDouble("valor_revaluado");
                double cambioAbsoluto = valorRevaluado - valorAnterior;

                double impactoTotal = calcularImpactoPorcentual(valorAnterior, valorRevaluado);

                Object[] row = {
                        rs.getInt("id_activo"),
                        rs.getString("nombre"),
                        rs.getString("categoria"),
                        valorAnterior,
                        valorRevaluado == 0 ? "" : valorRevaluado,
                        rs.getDouble("depreciacion_anterior"),
                        rs.getString("nueva_depreciacion"),
                        cambioAbsoluto,
                        String.format("%.2f%%", impactoTotal)
                };
                datos.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return datos;
    }

    public void guardarRevaluacionEnBD(int idActivo, double valorRevaluado, String motivo) {
        try (Connection conn = ConexionBD.getConnection()) {
            PreparedStatement pstmtCheck = conn.prepareStatement(
                    "SELECT r.id FROM revaluaciones r " +
                            "INNER JOIN movimientos m ON r.id_movimiento = m.id " +
                            "WHERE m.id_activo = ? AND m.tipo_movimiento = 'Revaluación'"
            );
            pstmtCheck.setInt(1, idActivo);
            ResultSet rsCheck = pstmtCheck.executeQuery();

            double valorAnterior = obtenerValorAnterior(conn, idActivo);
            double depreciacionAnterior = obtenerDepreciacionAnterior(conn, idActivo);

            double depreciacionMensual = calcularDepreciacionMensual(valorRevaluado, idActivo, conn);
            double depreciacionAcumulada = calcularDepreciacionAcumulada(valorRevaluado, idActivo, conn);
            double valorResidual = calcularValorResidual(valorRevaluado, depreciacionAcumulada);

            double impactoTotal = calcularImpactoPorcentual(valorAnterior, valorRevaluado);

            if (rsCheck.next()) {
                int idRevaluacion = rsCheck.getInt("id");
                PreparedStatement pstmtUpdate = conn.prepareStatement(
                        "UPDATE revaluaciones SET valor_revaluado = ?, depreciacion_anterior = ?, nueva_depreciacion = ?, impacto_total = ? WHERE id = ?"
                );

                pstmtUpdate.setDouble(1, valorRevaluado);
                pstmtUpdate.setDouble(2, depreciacionAnterior);
                pstmtUpdate.setDouble(3, depreciacionMensual);
                pstmtUpdate.setDouble(4, impactoTotal);
                pstmtUpdate.setInt(5, idRevaluacion);
                pstmtUpdate.executeUpdate();

                PreparedStatement pstmtMovimientoUpdate = conn.prepareStatement(
                        "UPDATE movimientos SET detalle_general = ? WHERE id = (SELECT id_movimiento FROM revaluaciones WHERE id = ?)"
                );
                String detalleGeneral = String.format(
                        "\nMotivo: %s\nCosto Anterior: %.2f\nCosto Revaluado: %.2f\nDepreciación Anterior: %.2f" +
                                "\nDepreciación Revaluada: %.2f\nImpacto Total: %.2f%%\n",
                        motivo, valorAnterior, valorRevaluado, depreciacionAnterior, depreciacionMensual, impactoTotal
                );
                pstmtMovimientoUpdate.setString(1, detalleGeneral);
                pstmtMovimientoUpdate.setInt(2, idRevaluacion);
                pstmtMovimientoUpdate.executeUpdate();

            } else {
                PreparedStatement pstmtMovimiento = conn.prepareStatement(
                        "INSERT INTO movimientos (id_activo, tipo_movimiento, fecha_hora, detalle_general) " +
                                "VALUES (?, 'Revaluación', NOW(), ?)",
                        Statement.RETURN_GENERATED_KEYS
                );

                String detalleGeneral = String.format(
                        "\nMotivo: %s\nCosto Anterior: %.2f\nCosto Revaluado: %.2f\nDepreciación Anterior: %.2f" +
                                "\nDepreciación Revaluada: %.2f\nImpacto Total: %.2f%%\n",
                        motivo, valorAnterior, valorRevaluado, depreciacionAnterior, depreciacionMensual, impactoTotal
                );

                pstmtMovimiento.setInt(1, idActivo);
                pstmtMovimiento.setString(2, detalleGeneral);
                pstmtMovimiento.executeUpdate();

                ResultSet rsMovimiento = pstmtMovimiento.getGeneratedKeys();
                if (rsMovimiento.next()) {
                    int idMovimiento = rsMovimiento.getInt(1);

                    PreparedStatement pstmtRevaluacion = conn.prepareStatement(
                            "INSERT INTO revaluaciones (id_movimiento, valor_anterior, valor_revaluado, depreciacion_anterior, nueva_depreciacion, impacto_total) " +
                                    "VALUES (?, ?, ?, ?, ?, ?)"
                    );
                    pstmtRevaluacion.setInt(1, idMovimiento);
                    pstmtRevaluacion.setDouble(2, valorAnterior);
                    pstmtRevaluacion.setDouble(3, valorRevaluado);
                    pstmtRevaluacion.setDouble(4, depreciacionAnterior);
                    pstmtRevaluacion.setDouble(5, depreciacionMensual);
                    pstmtRevaluacion.setDouble(6, impactoTotal);
                    pstmtRevaluacion.executeUpdate();
                }
            }

            PreparedStatement pstmtUpdateActivo = conn.prepareStatement(
                    "UPDATE activos SET costo_inicial = ?, depreciacion_mensual = ?, depreciacion_acumulada = ?, valor_residual = ? WHERE id = ?"
            );
            pstmtUpdateActivo.setDouble(1, valorRevaluado);
            pstmtUpdateActivo.setDouble(2, depreciacionMensual);
            pstmtUpdateActivo.setDouble(3, depreciacionAcumulada);
            pstmtUpdateActivo.setDouble(4, valorResidual);
            pstmtUpdateActivo.setInt(5, idActivo);
            pstmtUpdateActivo.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private double obtenerDepreciacionAnterior(Connection conn, int idActivo) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT depreciacion_mensual FROM activos WHERE id = ?");
        pstmt.setInt(1, idActivo);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getDouble("depreciacion_mensual") : 0.0;
    }

    public double calcularImpactoPorcentual(double valorAnterior, double valorRevaluado) {
        if (valorAnterior > 0) {
            return ((valorRevaluado - valorAnterior) / Math.max(valorAnterior, valorRevaluado) * 100);
        }
        return 0.0;
    }

    private double obtenerValorAnterior(Connection conn, int idActivo) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement("SELECT costo_inicial FROM activos WHERE id = ?");
        pstmt.setInt(1, idActivo);
        ResultSet rs = pstmt.executeQuery();
        return rs.next() ? rs.getDouble("costo_inicial") : 0.0;
    }

    private double calcularDepreciacionMensual(double costoInicial, int idActivo, Connection conn) throws SQLException {
        String query = "SELECT vida_util FROM activos WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, idActivo);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            int vidaUtil = rs.getInt("vida_util");
            if (vidaUtil == 0) return 0.0;
            return costoInicial / (vidaUtil * 12);
        }
        return 0.0;
    }

    private double calcularDepreciacionAcumulada(double costoInicial, int idActivo, Connection conn) throws SQLException {
        String query = """
        SELECT 
            TIMESTAMPDIFF(MONTH, DATE_ADD(fecha_adquisicion, INTERVAL 1 MONTH), CURDATE()) AS meses_transcurridos,
            vida_util 
        FROM activos WHERE id = ?
    """;
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setInt(1, idActivo);
        ResultSet rs = pstmt.executeQuery();
        if (rs.next()) {
            int mesesTranscurridos = rs.getInt("meses_transcurridos");
            int vidaUtilMeses = rs.getInt("vida_util") * 12;
            return Math.min(costoInicial / vidaUtilMeses * mesesTranscurridos, costoInicial);
        }
        return 0.0;
    }

    private double calcularValorResidual(double costoInicial, double depreciacionAcumulada) {
        return Math.max(0, costoInicial - depreciacionAcumulada);
    }
}