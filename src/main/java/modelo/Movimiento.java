package modelo;

import util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public abstract class Movimiento implements Registrable {
    protected String tipoMovimiento;
    protected LocalDateTime fechaHora;

    public Movimiento(String tipoMovimiento) {
        this.tipoMovimiento = tipoMovimiento;
        this.fechaHora = LocalDateTime.now();
    }

    public String getFechaHora() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        return fechaHora.format(formatter);
    }

    public void guardarMovimientoEnBD(int idActivo) {
        String sql = "INSERT INTO movimientos (id_activo, tipo_movimiento, fecha_hora, motivo_baja, tipo_baja, precio_venta, detalle_general) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, idActivo);
            pstmt.setString(2, tipoMovimiento);
            pstmt.setTimestamp(3, Timestamp.valueOf(fechaHora));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}