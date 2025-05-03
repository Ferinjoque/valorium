package vista;

import servicio.ActivoServicio;
import util.ValidadorVentas;
import util.ConexionBD;
import util.VentanaUtils;

import java.sql.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class VentaFrame extends JFrame {
    private JTextField txtPrecioVenta;
    private JTextField txtComprador;
    private JTextField txtRUCComprador;
    private JTextField txtNumeroFactura;
    private JComboBox<String> cmbFormaPago;
    private JTextArea txtDetalleVenta;
    private JTextArea txtFacturaPreview;
    private JButton btnVender, btnSalir;

    private String nombre;
    private String categoria;
    private String fechaAdquisicion;
    private double costoInicial;
    private double valorResidual;
    private static int contadorFacturas = 1;

    private MainFrame mainFrame;
    private int idActivo;
    private ActivoServicio activoServicio;

    public VentaFrame(MainFrame mainFrame, int idActivo, String nombre, String categoria, String fechaAdquisicion, double costoInicial, double valorResidual) {
        this.mainFrame = mainFrame;
        this.idActivo = idActivo;
        this.nombre = nombre;
        this.categoria = categoria;
        this.fechaAdquisicion = fechaAdquisicion;
        this.costoInicial = costoInicial;
        this.valorResidual = valorResidual;
        this.activoServicio = new ActivoServicio();

        initComponents();

        int numeroFactura = obtenerNumeroFactura();
        txtNumeroFactura.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("ddMMyyyy")) + "-" + String.format("%04d", numeroFactura));

        agregarListenersParaPrevisualizacion();
        actualizarFacturaPreview();
        setVisible(true);
    }

    private void initComponents() {
        setTitle("Formulario de Venta");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        setResizable(false);
        VentanaUtils.deshabilitarMinimizar(this);

        txtNumeroFactura = new JTextField();
        txtNumeroFactura.setEditable(false);

        // Panel de entrada de datos
        JPanel panelDatos = new JPanel(new GridLayout(0, 2, 5, 5));
        panelDatos.setBorder(BorderFactory.createTitledBorder("Detalles de Venta"));

        panelDatos.add(new JLabel("Número de Factura:"));
        panelDatos.add(txtNumeroFactura);

        panelDatos.add(new JLabel("Activo:"));
        panelDatos.add(new JLabel(nombre));

        panelDatos.add(new JLabel("Categoría:"));
        panelDatos.add(new JLabel(categoria));

        panelDatos.add(new JLabel("Fecha de Adquisición:"));
        panelDatos.add(new JLabel(fechaAdquisicion));

        panelDatos.add(new JLabel("Valor Residual:"));
        panelDatos.add(new JLabel("S/ " + String.format("%.2f", valorResidual)));

        panelDatos.add(new JLabel("Precio de Venta:"));
        txtPrecioVenta = new JTextField(String.format("%.2f", valorResidual));
        txtPrecioVenta.addActionListener(e -> btnVender.doClick());
        panelDatos.add(txtPrecioVenta);

        panelDatos.add(new JLabel("Comprador:"));
        txtComprador = new JTextField();
        txtComprador.addActionListener(e -> btnVender.doClick());
        panelDatos.add(txtComprador);

        panelDatos.add(new JLabel("RUC Comprador:"));
        txtRUCComprador = new JTextField();
        txtRUCComprador.addActionListener(e -> btnVender.doClick());
        panelDatos.add(txtRUCComprador);

        panelDatos.add(new JLabel("Forma de Pago:"));
        cmbFormaPago = new JComboBox<>(new String[]{"Efectivo", "Tarjeta de Crédito", "Transferencia Bancaria", "Cheque"});
        panelDatos.add(cmbFormaPago);

        panelDatos.add(new JLabel("Detalles de la Venta:"));
        txtDetalleVenta = new JTextArea(5, 20);
        txtDetalleVenta.setLineWrap(true);
        txtDetalleVenta.setWrapStyleWord(true);
        txtDetalleVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    e.consume();
                    btnVender.doClick();
                }
            }
        });
        panelDatos.add(new JScrollPane(txtDetalleVenta));

        add(panelDatos, BorderLayout.WEST);

        // Panel de vista previa de factura
        txtFacturaPreview = new JTextArea();
        txtFacturaPreview.setEditable(false);
        txtFacturaPreview.setLineWrap(true);
        txtFacturaPreview.setWrapStyleWord(true);
        txtFacturaPreview.setBorder(BorderFactory.createTitledBorder("Vista Previa de Factura"));
        txtFacturaPreview.setFont(new Font("Monospaced", Font.PLAIN, 12));
        txtFacturaPreview.setPreferredSize(new Dimension(400, 500));

        JScrollPane scrollFacturaPreview = new JScrollPane(txtFacturaPreview);
        scrollFacturaPreview.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollFacturaPreview.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollFacturaPreview, BorderLayout.CENTER);

        // Panel de botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        btnVender = new JButton("Vender");
        btnSalir = new JButton("Salir");

        panelBotones.add(btnVender);
        panelBotones.add(btnSalir);

        add(panelBotones, BorderLayout.SOUTH);

        btnVender.addActionListener(this::venderActivo);
        btnSalir.addActionListener(e -> dispose());

        setSize(850, 550);
        setLocationRelativeTo(null);
    }

    private void venderActivo(ActionEvent e) {
        if (!ValidadorVentas.validarFormularioVenta(txtComprador, txtRUCComprador, txtDetalleVenta, txtPrecioVenta, valorResidual)) {
            return;
        }

        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Está seguro de que desea vender este activo?", "Confirmar Venta", JOptionPane.YES_NO_OPTION);
        if (confirmacion == JOptionPane.NO_OPTION) {
            return;
        }

        try {
            activoServicio.actualizarEstadoActivo(idActivo, "Baja por venta");

            int indiceActivo = mainFrame.obtenerIndicePorNumero(idActivo);
            if (indiceActivo != -1) {
                mainFrame.getListaBaseDatos().get(indiceActivo).setEstado("Baja por venta");
            }

            double precioVenta = Double.parseDouble(txtPrecioVenta.getText());
            String detalleVenta = txtDetalleVenta.getText();
            String comprador = txtComprador.getText();
            String rucComprador = txtRUCComprador.getText();
            String formaPago = cmbFormaPago.getSelectedItem().toString();
            activoServicio.registrarVenta(idActivo, comprador, rucComprador, precioVenta, detalleVenta, formaPago);
            contadorFacturas++;

            mainFrame.actualizarEstadoActivoEnTabla(idActivo, "Baja por venta");
            mainFrame.mostrarMensaje("Activo vendido exitosamente por S/ " + String.format("%.2f", precioVenta));
            mainFrame.getFiltroFrame().resetearFiltros(mainFrame);
            mainFrame.txtBuscar.setText("");
            mainFrame.actualizarVistaDeTotales();
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Error en el precio de venta. Por favor, ingrese un valor numérico.", "Error", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al procesar la venta: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void actualizarFacturaPreview() {
        String nombreComercial = "Universidad Tecnológica del Perú";
        String rucEmisor = "R.U.C. 20462509236";
        String direccionSucursal = "Av. Arequipa, 265, Lima, Peru";
        String fechaEmision = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

        String factura = String.format(
                "------------------- FACTURA -------------------\n" +
                        "%s\n" +
                        "%s\n" +
                        "-----------------------------------------------\n" +
                        "%s\n" +
                        "FACTURA ELECTRÓNICA\n" +
                        "Número de Factura: %s\n" +
                        "-----------------------------------------------\n" +
                        "Fecha de Emisión: %s\n" +
                        "Comprador: %s\n" +
                        "RUC Comprador: %s\n" +
                        "-----------------------------------------------\n" +
                        "Activo: %s\n" +
                        "Categoría: %s\n" +
                        "Fecha de Adquisición: %s\n" +
                        "Precio de Venta: S/ %s\n" +
                        "-----------------------------------------------\n" +
                        "Forma de Pago: %s\n" +
                        "Detalles:\n%s\n" +
                        "-----------------------------------------------\n" +
                        "Gracias por su compra.\n",
                nombreComercial,
                direccionSucursal,
                rucEmisor,
                txtNumeroFactura.getText(),
                fechaEmision,
                txtComprador.getText(),
                txtRUCComprador.getText(),
                nombre,
                categoria,
                fechaAdquisicion,
                txtPrecioVenta.getText(),
                cmbFormaPago.getSelectedItem(),
                txtDetalleVenta.getText()
        );

        txtFacturaPreview.setText(factura);
    }

    private void agregarListenersParaPrevisualizacion() {
        DocumentListener listener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizarFacturaPreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizarFacturaPreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                actualizarFacturaPreview();
            }
        };

        txtPrecioVenta.getDocument().addDocumentListener(listener);
        txtComprador.getDocument().addDocumentListener(listener);
        txtRUCComprador.getDocument().addDocumentListener(listener);
        txtDetalleVenta.getDocument().addDocumentListener(listener);
        cmbFormaPago.addActionListener(e -> actualizarFacturaPreview());
    }

    private int obtenerNumeroFactura() {
        int numeroFactura = 1;
        String sql = "SELECT COUNT(*) AS total_ventas FROM ventas WHERE DATE(fecha_venta) = CURDATE()";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            if (rs.next()) {
                numeroFactura += rs.getInt("total_ventas");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return numeroFactura;
    }
}