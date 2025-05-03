package controlador;

import modelo.Usuario;
import modelo.UsuarioSesion;
import util.ConexionBD;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class Autenticacion {

    public Usuario autenticarUsuario(String nombreUsuario, String contrasena) {
        String query = "SELECT * FROM usuarios WHERE nombre_usuario = ? AND contrasena = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, nombreUsuario);
            stmt.setString(2, contrasena);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                Usuario usuario = new Usuario(rs.getString("nombre_usuario"), rs.getString("contrasena"), rs.getString("rol"));
                UsuarioSesion.getInstancia().setUsuarioAutenticado(usuario);
                return usuario;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}