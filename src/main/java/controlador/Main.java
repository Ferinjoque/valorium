package controlador;

import modelo.Usuario;
import servicio.ActivoServicio;
import vista.LoginFrame;
import vista.MainFrame;

import javax.swing.*;

public class Main {

    public static Usuario usuarioAutenticado;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ActivoServicio activoServicio = new ActivoServicio();
            ActivoControlador activoControlador = new ActivoControlador(activoServicio);
            mostrarLogin();
        });
    }

    private static void mostrarLogin() {
        LoginFrame loginFrame = new LoginFrame();
        loginFrame.setVisible(true);
    }

    public static void iniciarMainFrame() {
        MainFrame mainFrame = new MainFrame();
        mainFrame.setVisible(true);
    }
}