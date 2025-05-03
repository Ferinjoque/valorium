package modelo;
import util.ConexionBD;

import java.sql.*;

public class MovimientoVenta extends Movimiento {
    private double precioVenta;
    private String detalleVenta;
    private String comprador;
    private String rucComprador;
    private String formaPago;

    public MovimientoVenta(int idActivo, double precioVenta, String detalleVenta, String comprador, String rucComprador, String formaPago) {
        super("Venta");
        this.precioVenta = precioVenta;
        this.detalleVenta = detalleVenta;
        this.comprador = comprador;
        this.rucComprador = rucComprador;
        this.formaPago = formaPago;
    }

    @Override
    public void guardarMovimientoEnBD(int idActivo) {
        Connection conn = null;
        PreparedStatement pstmtVentas = null;
        PreparedStatement pstmtMovimientos = null;

        try {
            conn = ConexionBD.getConnection();
            conn.setAutoCommit(false);

            String sqlVentas = "INSERT INTO ventas (id_activo, comprador, ruc_comprador, fecha_venta, precio_venta, detalle_venta, forma_pago) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?)";
            pstmtVentas = conn.prepareStatement(sqlVentas);
            pstmtVentas.setInt(1, idActivo);
            pstmtVentas.setString(2, comprador);
            pstmtVentas.setString(3, rucComprador);
            pstmtVentas.setTimestamp(4, Timestamp.valueOf(fechaHora));
            pstmtVentas.setDouble(5, precioVenta);
            pstmtVentas.setString(6, detalleVenta);
            pstmtVentas.setString(7, formaPago);
            pstmtVentas.executeUpdate();

            String detalleGeneral = String.format(
                    "\nComprador: %s\nRUC: %s\nPrecio Venta: %.2f\nForma de Pago: %s\nDescripción: %s\n",
                    comprador, rucComprador, precioVenta, formaPago, detalleVenta);

            String sqlMovimientos = "INSERT INTO movimientos (id_activo, tipo_movimiento, fecha_hora, detalle_general) "
                    + "VALUES (?, ?, ?, ?)";
            pstmtMovimientos = conn.prepareStatement(sqlMovimientos);
            pstmtMovimientos.setInt(1, idActivo);
            pstmtMovimientos.setString(2, "Venta");
            pstmtMovimientos.setTimestamp(3, Timestamp.valueOf(fechaHora));
            pstmtMovimientos.setString(4, detalleGeneral);
            pstmtMovimientos.executeUpdate();

            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            e.printStackTrace();
        } finally {
            try {
                if (pstmtVentas != null) pstmtVentas.close();
                if (pstmtMovimientos != null) pstmtMovimientos.close();
                if (conn != null) conn.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        }
    }
}