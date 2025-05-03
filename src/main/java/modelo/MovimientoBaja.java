package modelo;
import util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class MovimientoBaja extends Movimiento {
    private String motivoBaja;
    private String tipoBaja;

    public MovimientoBaja(String motivoBaja, String tipoBaja) {
        super("Baja");
        this.motivoBaja = motivoBaja;
        this.tipoBaja = tipoBaja;
    }

    @Override
    public void guardarMovimientoEnBD(int idActivo) {
        String sql = "INSERT INTO movimientos (id_activo, tipo_movimiento, fecha_hora, motivo_baja, tipo_baja) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idActivo);
            pstmt.setString(2, "Baja");
            pstmt.setTimestamp(3, Timestamp.valueOf(fechaHora));
            pstmt.setString(4, motivoBaja);
            pstmt.setString(5, tipoBaja);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}