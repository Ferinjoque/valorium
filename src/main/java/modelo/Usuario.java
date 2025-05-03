package modelo;

public class Usuario {

    private String nombreUsuario;
    private String password;
    private String rol;

    public Usuario(String nombreUsuario, String password, String rol) {
        this.nombreUsuario = nombreUsuario;
        this.password = password;
        this.rol = rol;
    }

    public String getNombreUsuario() {
        return nombreUsuario;
    }
}