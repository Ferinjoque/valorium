package vista;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class NotificacionFrame {
    private final JFrame ventanaPrincipal;
    private final List<JDialog> notificacionesActivas = new ArrayList<>();
    private final int anchoNotificacion = 350;
    private final int altoNotificacion = 100;

    public NotificacionFrame(JFrame ventanaPrincipal) {
        this.ventanaPrincipal = ventanaPrincipal;
    }

    public void mostrarNotificacion(String titulo, String mensaje, int idNotificacion, Runnable accionListo, Runnable accionRecordar) {
        JDialog notificacion = new JDialog(ventanaPrincipal, false);
        notificacion.setUndecorated(true);
        notificacion.setSize(anchoNotificacion, altoNotificacion + 20);

        JPanel panelContenido = new JPanel();
        panelContenido.setLayout(new BorderLayout());
        panelContenido.setBackground(new Color(245, 245, 245));

        panelContenido.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createMatteBorder(5, 5, 5, 5, new Color(245, 245, 245))
        ));

        JLabel labelTitulo = new JLabel(titulo);
        labelTitulo.setFont(new Font("Arial", Font.BOLD, 14));
        labelTitulo.setForeground(new Color(50, 50, 50));
        labelTitulo.setBorder(BorderFactory.createEmptyBorder(5, 10, 0, 10));

        JTextArea textAreaMensaje = new JTextArea(mensaje);
        textAreaMensaje.setFont(new Font("Arial", Font.PLAIN, 12));
        textAreaMensaje.setForeground(new Color(50, 50, 50));
        textAreaMensaje.setWrapStyleWord(true);
        textAreaMensaje.setLineWrap(true);
        textAreaMensaje.setOpaque(false);
        textAreaMensaje.setEditable(false);
        textAreaMensaje.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        panelBotones.setBackground(new Color(245, 245, 245));

        JButton btnRecordar = new JButton("Recordar");
        JButton btnListo = new JButton("Listo");

        estilizarBoton(btnRecordar, new Color(240, 240, 240), new Color(100, 100, 100));
        estilizarBoton(btnListo, new Color(33, 150, 243), Color.WHITE);

        btnRecordar.addActionListener(e -> {
            accionRecordar.run();
            desvanecerNotificacion(notificacion);
        });

        btnListo.addActionListener(e -> {
            accionListo.run();
            desvanecerNotificacion(notificacion);
        });

        panelBotones.add(btnRecordar);
        panelBotones.add(btnListo);

        panelContenido.add(labelTitulo, BorderLayout.NORTH);
        panelContenido.add(textAreaMensaje, BorderLayout.CENTER);
        panelContenido.add(panelBotones, BorderLayout.SOUTH);

        notificacion.add(panelContenido);

        int x = ventanaPrincipal.getWidth() - anchoNotificacion - 30;
        int separacionVertical = 30;
        int margenSuperior = 175;
        int y = ventanaPrincipal.getHeight() - margenSuperior - (notificacionesActivas.size() * (altoNotificacion + separacionVertical));
        notificacion.setLocation(x, y);

        notificacionesActivas.add(notificacion);

        notificacion.setVisible(true);

        Timer temporizador = new Timer(8000, e -> desvanecerNotificacion(notificacion));
        temporizador.setRepeats(false);
        temporizador.start();
    }

    private void estilizarBoton(JButton boton, Color fondo, Color texto) {
        boton.setBackground(fondo);
        boton.setForeground(texto);
        boton.setFocusPainted(false);
        boton.setFont(new Font("Arial", Font.PLAIN, 12));
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(5, 15, 5, 15)
        ));
    }

    private void desvanecerNotificacion(JDialog notificacion) {
        new Thread(() -> {
            try {
                for (float i = 1.0f; i > 0; i -= 0.1f) {
                    Thread.sleep(50);
                    notificacion.setOpacity(i);
                }
            } catch (InterruptedException ignored) {}
            SwingUtilities.invokeLater(() -> {
                notificacion.dispose();
                notificacionesActivas.remove(notificacion);
                actualizarPosicionesNotificaciones();
            });
        }).start();
    }

    private void actualizarPosicionesNotificaciones() {
        int y = ventanaPrincipal.getHeight() - 175;
        for (JDialog notificacion : notificacionesActivas) {
            notificacion.setLocation(notificacion.getX(), y);
            y -= (altoNotificacion + 10);
        }
    }
}