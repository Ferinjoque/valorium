package vista;

import controlador.Main;
import controlador.Autenticacion;
import modelo.Usuario;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginFrame extends JFrame {

    private Autenticacion autenticacion = new Autenticacion();
    private Usuario usuarioAutenticado;
    private int intentosRestantes = 3;

    private JTextField TextUsuario;
    private JPasswordField TxtPass;
    private JButton jButtonIngresar;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel logo;

    public LoginFrame() {
        initComponents();
    }

    private void initComponents() {
        // Panel principal
        JPanel jPanel1 = new JPanel();
        jPanel1.setBackground(new Color(37, 51, 60));
        jPanel1.setLayout(null);

        // Logo
        logo = new JLabel();
        logo.setIcon(redimensionarLogo("Valorium.png", 85, 73));
        logo.setBounds(120, 20, 120, 120);
        jPanel1.add(logo);

        // Etiqueta de "Usuario"
        jLabel2 = new JLabel("Usuario");
        jLabel2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        jLabel2.setForeground(new Color(255, 255, 255));
        jLabel2.setBounds(40, 150, 140, 30);
        jPanel1.add(jLabel2);

        // Campo de texto para ingresar el usuario
        TextUsuario = new JTextField();
        TextUsuario.setBounds(40, 190, 250, 40);
        TextUsuario.setBackground(new Color(43, 47, 51));
        TextUsuario.setForeground(Color.WHITE);
        TextUsuario.setBorder(BorderFactory.createLineBorder(new Color(103, 113, 121), 2));
        TextUsuario.setCaretColor(Color.WHITE);
        TextUsuario.setOpaque(true);
        agregarEfectoFoco(TextUsuario);
        jPanel1.add(TextUsuario);

        // Etiqueta de "Contraseña"
        jLabel1 = new JLabel("Contraseña");
        jLabel1.setFont(new Font("Segoe UI", Font.BOLD, 18));
        jLabel1.setForeground(new Color(255, 255, 255));
        jLabel1.setBounds(40, 250, 140, 30);
        jPanel1.add(jLabel1);

        // Campo de texto para la contraseña
        TxtPass = new JPasswordField();
        TxtPass.setBounds(40, 290, 250, 40);
        TxtPass.setBackground(new Color(43, 47, 51));
        TxtPass.setForeground(Color.WHITE);
        TxtPass.setBorder(BorderFactory.createLineBorder(new Color(103, 113, 121), 2));
        TxtPass.setCaretColor(Color.WHITE);
        TxtPass.setOpaque(true);
        agregarEfectoFoco(TxtPass);
        jPanel1.add(TxtPass);

        // Botón de "Iniciar sesión"
        jButtonIngresar = new JButton("Iniciar sesión");
        jButtonIngresar.setFont(new Font("Segoe UI", Font.BOLD, 18));
        jButtonIngresar.setBackground(new Color(108, 215, 159));
        jButtonIngresar.setForeground(Color.WHITE); // Texto blanco
        jButtonIngresar.setBounds(85, 360, 160, 40);
        jButtonIngresar.setFocusPainted(false);
        jButtonIngresar.setBorder(BorderFactory.createLineBorder(new Color(108, 215, 159), 2));
        jButtonIngresar.setBorder(BorderFactory.createEmptyBorder());
        agregarEfectoHover(jButtonIngresar);
        jPanel1.add(jButtonIngresar);

        jButtonIngresar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButtonIngresarActionPerformed(evt);
            }
        });

        TextUsuario.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButtonIngresar.doClick();
            }
        });

        TxtPass.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                jButtonIngresar.doClick();
            }
        });

        this.add(jPanel1);
        this.setTitle("Valorium - Iniciar sesión");
        this.setSize(340, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }

    private void jButtonIngresarActionPerformed(ActionEvent evt) {
        String nombreUsuario = TextUsuario.getText();
        String contrasena = new String(TxtPass.getPassword());

        if (nombreUsuario.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe completar todos los campos.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        usuarioAutenticado = autenticacion.autenticarUsuario(nombreUsuario, contrasena);

        if (usuarioAutenticado != null) {
            Main.usuarioAutenticado = usuarioAutenticado;
            this.dispose();
            Main.iniciarMainFrame();
        } else {
            TxtPass.setText("");
            intentosRestantes--;
            if (intentosRestantes > 0) {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos. Intentos restantes: " + intentosRestantes, "Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Se ha excedido el número de intentos. El programa se cerrará.", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }
    }

    private ImageIcon redimensionarLogo(String ruta, int width, int height) {
        ImageIcon logoIcon = new ImageIcon(getClass().getClassLoader().getResource(ruta));
        Image logoImage = logoIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(logoImage);
    }

    private void agregarEfectoFoco(JTextField campo) {
        campo.setBorder(BorderFactory.createLineBorder(new Color(103, 113, 121), 2));

        campo.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                campo.setBorder(BorderFactory.createLineBorder(new Color(108, 215, 159), 2));
            }

            @Override
            public void focusLost(FocusEvent e) {
                campo.setBorder(BorderFactory.createLineBorder(new Color(103, 113, 121), 2));
            }
        });
    }

    private void agregarEfectoHover(JButton boton) {
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(87, 171, 127));
            }

            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(108, 215, 159));
            }
        });
    }
}