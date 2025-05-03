package modelo;
import util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MovimientoRegistro extends Movimiento {

    public MovimientoRegistro() {
        super("Registro");
    }

    @Override
    public void guardarMovimientoEnBD(int idActivo) {
        String sql = "INSERT INTO movimientos (id_activo, tipo_movimiento, fecha_hora) VALUES (?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idActivo);
            pstmt.setString(2, "Registro");
            pstmt.setTimestamp(3, Timestamp.valueOf(fechaHora));
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}