package modelo;

import util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MovimientoRevaluacion extends Movimiento {
    private double valorAnterior;
    private double valorRevaluado;
    private String justificacion;

    public MovimientoRevaluacion(int idActivo, double valorAnterior, double valorRevaluado, String justificacion) {
        super("Revaluación");
        this.valorAnterior = valorAnterior;
        this.valorRevaluado = valorRevaluado;
        this.justificacion = justificacion;
    }

    @Override
    public void guardarMovimientoEnBD(int idActivo) {
        String sql = "INSERT INTO movimientos (id_activo, tipo_movimiento, fecha_hora, valor_anterior, valor_revaluado, justificacion) " +
                "VALUES (?, ?, CURRENT_TIMESTAMP, ?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idActivo);
            pstmt.setString(2, tipoMovimiento); // "Revaluación"
            pstmt.setDouble(3, valorAnterior);
            pstmt.setDouble(4, valorRevaluado);
            pstmt.setString(5, justificacion);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}