package modelo;

import util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class MovimientoRenovacion extends Movimiento {
    private String motivo;
    private String nuevoCodigoPatrimonial;

    public MovimientoRenovacion(String motivo, String nuevoCodigoPatrimonial) {
        super("Renovación");
        this.motivo = motivo;
        this.nuevoCodigoPatrimonial = nuevoCodigoPatrimonial;
    }

    @Override
    public void guardarMovimientoEnBD(int idActivo) {
        String sql = "INSERT INTO movimientos (id_activo, tipo_movimiento, fecha_hora, detalle_general) VALUES (?, ?, CURRENT_TIMESTAMP, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            String detalle = String.format("\nMotivo: %s \nNuevo Código Patrimonial: %s\n", motivo, nuevoCodigoPatrimonial);
            pstmt.setInt(1, idActivo);
            pstmt.setString(2, tipoMovimiento); // "Renovación"
            pstmt.setString(3, detalle);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
