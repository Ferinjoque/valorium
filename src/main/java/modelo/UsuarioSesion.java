package modelo;

public class UsuarioSesion {

    private static UsuarioSesion instancia;
    private Usuario usuarioAutenticado;

    private UsuarioSesion() {}

    public static UsuarioSesion getInstancia() {
        if (instancia == null) {
            instancia = new UsuarioSesion();
        }
        return instancia;
    }

    public void setUsuarioAutenticado(Usuario usuario) {
        this.usuarioAutenticado = usuario;
    }
}