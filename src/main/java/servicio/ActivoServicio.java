package servicio;

import modelo.*;
import util.ConexionBD;
import util.ValidadorFechas;
import vista.MainFrame;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Date;
import java.util.stream.Collectors;
import java.sql.*;
import java.util.List;

public class ActivoServicio {
    private MainFrame mainFrame;

    public ActivoServicio(MainFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    public ActivoServicio() {
    }

    public List<ActivoFijo> cargarActivosDesdeBD() {
        List<ActivoFijo> activos = new ArrayList<>();
        String sql = """
        SELECT a.id, a.codigo_patrimonial, a.nombre, a.categoria, a.estado, 
               a.fecha_adquisicion, a.vida_util, a.costo_inicial, 
               s.id_sede, s.nombre AS nombre_sede, s.direccion
        FROM activos a
        LEFT JOIN sedes s ON a.id_sede = s.id_sede
        ORDER BY a.id
    """;

        try (Connection conn = ConexionBD.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String codigoPatrimonial = rs.getString("codigo_patrimonial");
                String nombre = rs.getString("nombre");
                String categoria = rs.getString("categoria");
                String estado = rs.getString("estado");
                String fechaAdquisicion = rs.getString("fecha_adquisicion");

                String fechaFormateada;
                try {
                    Date fecha = new SimpleDateFormat("yyyy-MM-dd").parse(fechaAdquisicion);
                    fechaFormateada = new SimpleDateFormat("dd-MM-yyyy").format(fecha);
                } catch (Exception e) {
                    fechaFormateada = fechaAdquisicion;
                }

                int vidaUtil = rs.getInt("vida_util");
                double costoInicial = rs.getDouble("costo_inicial");

                Sede sede = null;
                if (rs.getInt("id_sede") != 0) {
                    sede = new Sede(rs.getInt("id_sede"), rs.getString("nombre_sede"), rs.getString("direccion"));
                }

                ActivoFijo activo = new ActivoFijo(nombre, categoria, costoInicial, fechaFormateada, "", sede);
                activo.setCodigoPatrimonial(codigoPatrimonial);
                activo.setEstado(estado);
                activo.setVidaUtil(categoria);
                activo.setNumero(id);
                activos.add(activo);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return activos;
    }

    public ActivoFijo cargarActivoPorId(int id) {
        String sql = """
        SELECT a.id, a.codigo_patrimonial, a.nombre, a.categoria, a.estado, 
               a.fecha_adquisicion, a.vida_util, a.costo_inicial, 
               s.id_sede, s.nombre AS nombre_sede, s.direccion
        FROM activos a
        LEFT JOIN sedes s ON a.id_sede = s.id_sede
        WHERE a.id = ?
    """;

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String codigoPatrimonial = rs.getString("codigo_patrimonial");
                String nombre = rs.getString("nombre");
                String categoria = rs.getString("categoria");
                String estado = rs.getString("estado");
                String fechaAdquisicion = rs.getString("fecha_adquisicion");
                String fechaFormateada = new SimpleDateFormat("dd-MM-yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(fechaAdquisicion));
                int vidaUtil = rs.getInt("vida_util");
                double costoInicial = rs.getDouble("costo_inicial");

                // Crear instancia de Sede si existe
                Sede sede = null;
                if (rs.getInt("id_sede") != 0) {
                    sede = new Sede(rs.getInt("id_sede"), rs.getString("nombre_sede"), rs.getString("direccion"));
                }

                // Crear ActivoFijo con sede
                ActivoFijo activo = new ActivoFijo(nombre, categoria, costoInicial, fechaFormateada, "", sede);
                activo.setCodigoPatrimonial(codigoPatrimonial);
                activo.setEstado(estado);
                activo.setVidaUtil(String.valueOf(vidaUtil));
                return activo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<String> cargarHistorialMovimientos() {
        List<String> historial = new ArrayList<>();
        String sql = "SELECT m.tipo_movimiento, m.fecha_hora, m.motivo_baja, m.tipo_baja, m.precio_venta, m.detalle_general, "
                + "a.nombre, a.categoria, a.fecha_adquisicion, a.costo_inicial "
                + "FROM movimientos m "
                + "JOIN activos a ON m.id_activo = a.id "
                + "ORDER BY m.fecha_hora DESC";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                StringBuilder movimiento = new StringBuilder();
                movimiento.append("Activo: ").append(rs.getString("nombre")).append(", Categoría: ").append(rs.getString("categoria")).append("\n");
                movimiento.append("Movimiento: ").append(rs.getString("tipo_movimiento")).append("\n");
                movimiento.append("Fecha: ").append(rs.getTimestamp("fecha_hora")).append("\n");

                String motivoBaja = rs.getString("motivo_baja");
                if (motivoBaja != null) movimiento.append("\nMotivo Baja: ").append(motivoBaja).append("\n");

                String tipoBaja = rs.getString("tipo_baja");
                if (tipoBaja != null) movimiento.append("Tipo Baja: ").append(tipoBaja).append("\n\n");

                double precioVenta = rs.getDouble("precio_venta");
                if (precioVenta != 0.0) movimiento.append("Precio Venta: ").append(String.format("%.2f", precioVenta)).append("\n");

                String detalleVenta = rs.getString("detalle_general");
                if (detalleVenta != null) movimiento.append("").append(detalleVenta).append("\n");

                movimiento.append("Fecha Adquisición: ").append(rs.getDate("fecha_adquisicion")).append("\n");
                movimiento.append("Costo Inicial: ").append(String.format("%.2f", rs.getDouble("costo_inicial"))).append("\n");

                movimiento.append("----------------------------------------------------------------------");
                historial.add(movimiento.toString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return historial;
    }

    public List<Sede> cargarSedesDesdeBD() {
        List<Sede> sedes = new ArrayList<>();
        String sql = "SELECT id_sede, nombre, direccion FROM sedes";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                Sede sede = new Sede(rs.getInt("id_sede"), rs.getString("nombre"), rs.getString("direccion"));
                sedes.add(sede);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return sedes;
    }

    public Map<Sede, Long> obtenerConteoPorSede() {
        List<ActivoFijo> activos = cargarActivosDesdeBD();
        return activos.stream()
                .filter(activo -> activo.getSede() != null)
                .collect(Collectors.groupingBy(ActivoFijo::getSede, Collectors.counting()));
    }

    public GuardarActivoResultado guardarActivoEnBD(String nombre, String categoria, String estado,
                                                    String fechaAdquisicionStr, int vidaUtil,
                                                    double costoInicial, double depreciacionMensual,
                                                    double depreciacionAcumulada, double valorResidual,
                                                    int idSede) throws ParseException {
        String sql = """
        INSERT INTO activos (nombre, categoria, estado, fecha_adquisicion, vida_util, costo_inicial,
                             depreciacion_mensual, depreciacion_acumulada, valor_residual, 
                             codigo_patrimonial, id_sede)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

        String fechaFormateada = new SimpleDateFormat("yyyy-MM-dd")
                .format(new SimpleDateFormat("dd-MM-yyyy").parse(fechaAdquisicionStr));

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            String codigoPatrimonial = generarCodigoPatrimonial(fechaFormateada, conn);

            pstmt.setString(1, nombre);
            pstmt.setString(2, categoria);
            pstmt.setString(3, estado);
            pstmt.setString(4, fechaFormateada);
            pstmt.setInt(5, vidaUtil);
            pstmt.setDouble(6, costoInicial);
            pstmt.setDouble(7, depreciacionMensual);
            pstmt.setDouble(8, depreciacionAcumulada);
            pstmt.setDouble(9, valorResidual);
            pstmt.setString(10, codigoPatrimonial);
            pstmt.setInt(11, idSede);

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                registrarNotificacion(id, nombre, fechaAdquisicionStr, vidaUtil, "Pendiente");
                return new GuardarActivoResultado(id, codigoPatrimonial);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private String generarCodigoPatrimonial(String fechaAdquisicion, Connection conn) {
        String anio = fechaAdquisicion.split("-")[0];
        String sql = "SELECT MAX(CAST(SUBSTRING_INDEX(codigo_patrimonial, '-', -1) AS UNSIGNED)) AS max_codigo " +
                "FROM activos WHERE codigo_patrimonial LIKE ?";
        int numeroConsecutivo = 1;

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, anio + "-%");
            ResultSet rs = pstmt.executeQuery();
            if (rs.next() && rs.getString("max_codigo") != null) {
                numeroConsecutivo = rs.getInt("max_codigo") + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        String numeroFormateado = String.format("%04d", numeroConsecutivo);

        return anio + "-" + numeroFormateado;
    }

    public void registrarActivo(int idActivo) {
        MovimientoRegistro movimientoRegistro = new MovimientoRegistro();
        movimientoRegistro.guardarMovimientoEnBD(idActivo);
    }

    public void modificarActivoEnBD(int id, String nombre, String categoria, String estado, String fechaAdquisicionStr, int vidaUtil, double costoInicial, double depreciacionMensual, double depreciacionAcumulada, double valorResidual) {
        String sql = "UPDATE activos SET nombre = ?, categoria = ?, estado = ?, fecha_adquisicion = ?, vida_util = ?, costo_inicial = ?, depreciacion_mensual = ?, depreciacion_acumulada = ?, valor_residual = ? WHERE id = ?";

        String fechaFormateada;
        try {
            Date fechaAdquisicion = new SimpleDateFormat("dd-MM-yyyy").parse(fechaAdquisicionStr);
            fechaFormateada = new SimpleDateFormat("yyyy-MM-dd").format(fechaAdquisicion);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nombre);
            pstmt.setString(2, categoria);
            pstmt.setString(3, estado);
            pstmt.setString(4, fechaFormateada);
            pstmt.setInt(5, vidaUtil);
            pstmt.setDouble(6, costoInicial);
            pstmt.setDouble(7, depreciacionMensual);
            pstmt.setDouble(8, depreciacionAcumulada);
            pstmt.setDouble(9, valorResidual);
            pstmt.setInt(10, id);
            pstmt.executeUpdate();

            registrarNotificacion(id, nombre, fechaAdquisicionStr, vidaUtil, "Pendiente");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<ActivoFijo> filtrarActivos(Map<String, Object> criterios) {
        List<ActivoFijo> activosFiltrados = cargarActivosDesdeBD();

        for (Map.Entry<String, Object> criterio : criterios.entrySet()) {
            String clave = criterio.getKey();
            Object valor = criterio.getValue();

            switch (clave) {
                case "costoMin":
                    activosFiltrados = activosFiltrados.stream()
                            .filter(activo -> activo.getCostoInicial() >= (double) valor)
                            .collect(Collectors.toList());
                    break;
                case "costoMax":
                    activosFiltrados = activosFiltrados.stream()
                            .filter(activo -> activo.getCostoInicial() <= (double) valor)
                            .collect(Collectors.toList());
                    break;
                case "fechaInicio":
                    activosFiltrados = activosFiltrados.stream()
                            .filter(activo -> {
                                LocalDate fechaInicio = (LocalDate) valor;
                                LocalDate fechaAdquisicion = LocalDate.parse(activo.getFechaAdquisicion(),
                                        DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                                return !fechaAdquisicion.isBefore(fechaInicio);
                            })
                            .collect(Collectors.toList());
                    break;
                case "fechaFin":
                    activosFiltrados = activosFiltrados.stream()
                            .filter(activo -> {
                                LocalDate fechaFin = (LocalDate) valor;
                                LocalDate fechaAdquisicion = LocalDate.parse(activo.getFechaAdquisicion(),
                                        DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                                return !fechaAdquisicion.isAfter(fechaFin);
                            })
                            .collect(Collectors.toList());
                    break;
                case "categoria":
                    activosFiltrados = activosFiltrados.stream()
                            .filter(activo -> activo.getCategoria().equalsIgnoreCase((String) valor))
                            .collect(Collectors.toList());
                    break;
                case "estado":
                    activosFiltrados = activosFiltrados.stream()
                            .filter(activo -> activo.getEstado().equalsIgnoreCase((String) valor))
                            .collect(Collectors.toList());
                    break;
            }
        }
        return activosFiltrados;
    }

    public void modificarActivo(int idActivo, String detalleModificacion) {
        MovimientoModificacion movimientoModificacion = new MovimientoModificacion(detalleModificacion);
        movimientoModificacion.guardarMovimientoEnBD(idActivo);
    }

    public void darDeBaja(int idActivo, String motivo, String tipoBaja) {
        MovimientoBaja movimientoBaja = new MovimientoBaja(motivo, tipoBaja);
        movimientoBaja.guardarMovimientoEnBD(idActivo);
    }

    public void registrarVenta(int idActivo, String comprador, String rucComprador, double precioVenta, String detalleVenta, String formaPago) {
        MovimientoVenta movimientoVenta = new MovimientoVenta(idActivo, precioVenta, detalleVenta, comprador, rucComprador, formaPago);
        movimientoVenta.guardarMovimientoEnBD(idActivo);
        actualizarEstadoActivo(idActivo, "Baja por venta");
    }

    public void actualizarEstadoActivo(int id, String nuevoEstado) {
        String sql = "UPDATE activos SET estado = ? WHERE id = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, id);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void registrarNotificacion(int idActivo, String nombre, String fechaAdquisicion, int vidaUtil, String estado) {
        String mensaje = "El activo '" + nombre + "' se depreciará en menos de 6 meses.";

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        LocalDate fechaInicioDepreciacion = LocalDate.parse(fechaAdquisicion, formatter).plusMonths(1);
        LocalDate fechaFinVidaUtil = fechaInicioDepreciacion.plusYears(vidaUtil);
        LocalDate fechaActual = LocalDate.now();
        long mesesRestantes = ChronoUnit.MONTHS.between(fechaActual, fechaFinVidaUtil);

        if (mesesRestantes <= 6 && !fechaActual.isAfter(fechaFinVidaUtil)) {
            String sql = "INSERT INTO notificaciones (id_activo, mensaje, estado, fecha_creacion) VALUES (?, ?, ?, CURRENT_TIMESTAMP) " +
                    "ON DUPLICATE KEY UPDATE mensaje = VALUES(mensaje), estado = VALUES(estado), fecha_creacion = CURRENT_TIMESTAMP";

            try (Connection conn = ConexionBD.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, idActivo);
                pstmt.setString(2, mensaje);
                pstmt.setString(3, estado);
                pstmt.executeUpdate();

                if (mainFrame != null) {
                    mainFrame.mostrarNotificacionEmergente(mensaje, idActivo);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void renovarActivo(int idActivo, String motivo) {
        ActivoFijo activo = cargarActivoPorId(idActivo);
        if (activo == null) {
            throw new IllegalArgumentException("El activo no existe.");
        }

        if (!activo.getEstado().equalsIgnoreCase("Depreciado") &&
                !activo.getEstado().equalsIgnoreCase("Baja") &&
                !activo.getEstado().equalsIgnoreCase("Baja por venta")) {
            throw new IllegalArgumentException("Solo se pueden renovar activos en estado 'Depreciado', 'Baja' o 'Baja por venta'.");
        }

        actualizarEstadoActivo(idActivo, "Renovado");

        String nuevoCodigo;
        try (Connection conn = ConexionBD.getConnection()) {
            ActivoFijo nuevoActivo = new ActivoFijo(
                    activo.getNombre(),
                    activo.getCategoria(),
                    activo.getCostoInicial(),
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")),
                    "Administrador",
                    activo.getSede()
            );

            nuevoCodigo = generarCodigoPatrimonial(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), conn);
            nuevoActivo.setCodigoPatrimonial(nuevoCodigo);

            guardarActivoEnBD(
                    nuevoActivo.getNombre(),
                    nuevoActivo.getCategoria(),
                    "Alta",
                    nuevoActivo.getFechaAdquisicion(),
                    Integer.parseInt(String.valueOf(activo.getVidaUtil())),
                    nuevoActivo.getCostoInicial(),
                    nuevoActivo.calcularDepreciacionMensual(),
                    0.0,
                    nuevoActivo.calcularValorResidual(),
                    nuevoActivo.getSede().getId()
            );

            MovimientoRenovacion movimiento = new MovimientoRenovacion(motivo, nuevoCodigo);
            movimiento.guardarMovimientoEnBD(idActivo);
        } catch (SQLException | ParseException e) {
            throw new RuntimeException("Error al generar el código patrimonial o guardar el activo renovado.", e);
        }
    }

    public int contarActivosPorEstado(String estado) {
        String sql = "SELECT COUNT(*) AS total FROM activos WHERE estado = ?";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, estado);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}