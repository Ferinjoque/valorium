package modelo;

import util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MovimientoModificacion extends Movimiento {
    private String detalleModificacion;

    public MovimientoModificacion(String detalleModificacion) {
        super("Modificación");
        this.detalleModificacion = detalleModificacion;
    }

    @Override
    public void guardarMovimientoEnBD(int idActivo) {
        String sql = "INSERT INTO movimientos (id_activo, tipo_movimiento, fecha_hora, detalle_general) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idActivo);
            pstmt.setString(2, "Modificación");
            pstmt.setTimestamp(3, Timestamp.valueOf(fechaHora));
            pstmt.setString(4, detalleModificacion);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}