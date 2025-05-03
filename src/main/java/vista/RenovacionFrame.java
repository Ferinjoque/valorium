package vista;

import modelo.ActivoFijo;
import servicio.ActivoServicio;
import util.VentanaUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RenovacionFrame extends JFrame {
    private final MainFrame mainFrame;
    private final ActivoServicio activoServicio;
    private DefaultTableModel tableModel;
    private JTable activosTable;
    private JComboBox<String> cmbTipoActivo;
    private JTextField txtBuscarActivo;
    private JLabel lblTotalRenovados;
    private List<Object[]> estadoInicial = new ArrayList<>();

    public RenovacionFrame(ActivoServicio servicio, MainFrame mainFrame) {
        this.activoServicio = servicio;
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setTitle("Renovación de Activos");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        VentanaUtils.deshabilitarMinimizar(this);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // Panel superior
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTipoActivo = new JLabel("Tipo de Activo:");
        cmbTipoActivo = new JComboBox<>(new String[]{"Todos", "Equipo de Computo", "Mobiliario",
                "Equipo de Laboratorio", "Vehiculo", "Terreno"});

        txtBuscarActivo = new JTextField(20);
        txtBuscarActivo.setPreferredSize(new Dimension(200, 25));

        JButton btnReload = new JButton("✕");
        btnReload.setPreferredSize(new Dimension(30, 25));
        btnReload.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        btnReload.setFocusPainted(false);
        btnReload.setContentAreaFilled(false);
        btnReload.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JButton btnBuscar = new JButton("Buscar");

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        searchPanel.add(txtBuscarActivo);
        searchPanel.add(btnReload);

        gbc.gridx = 0;
        gbc.gridy = 0;
        filterPanel.add(lblTipoActivo, gbc);

        gbc.gridx = 1;
        gbc.gridy = 0;
        filterPanel.add(cmbTipoActivo, gbc);

        gbc.gridx = 2;
        gbc.gridy = 0;
        filterPanel.add(searchPanel, gbc);

        gbc.gridx = 3;
        gbc.gridy = 0;
        filterPanel.add(btnBuscar, gbc);

        // Tabla de activos
        String[] columnNames = {
                "ID", "Nombre", "Categoría", "Estado", "Fecha Adquisición", "Costo Inicial"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Ninguna celda editable
            }
        };
        activosTable = new JTable(tableModel);
        activosTable.getTableHeader().setReorderingAllowed(false);

        JScrollPane tableScrollPane = new JScrollPane(activosTable);

        // Panel inferior
        JPanel buttonPanel = new JPanel(new BorderLayout());

        // Mensaje
        lblTotalRenovados = new JLabel("Total de activos renovados: 0");
        lblTotalRenovados.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));

        // Botones
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Renovar");
        JButton btnCancelar = new JButton("Cancelar");

        botonesPanel.add(btnGuardar);
        botonesPanel.add(btnCancelar);

        buttonPanel.add(lblTotalRenovados, BorderLayout.WEST);
        buttonPanel.add(botonesPanel, BorderLayout.EAST);

        cmbTipoActivo.addActionListener(e -> filtrarPorTipoActivo(cmbTipoActivo.getSelectedItem().toString()));
        btnBuscar.addActionListener(e -> buscarActivo(txtBuscarActivo.getText()));
        btnReload.addActionListener(e -> {
            txtBuscarActivo.setText("");
            filtrarPorTipoActivo(cmbTipoActivo.getSelectedItem().toString());
        });

        btnGuardar.addActionListener(e -> renovarActivoSeleccionado());
        btnCancelar.addActionListener(e -> dispose());

        add(filterPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cargarDatosDesdeBD();
        actualizarTotalRenovados();
    }

    private void actualizarTotalRenovados() {
        int totalRenovados = activoServicio.contarActivosPorEstado("Renovado");
        lblTotalRenovados.setText("Total de activos renovados: " + totalRenovados);
    }

    private void cargarDatosDesdeBD() {
        tableModel.setRowCount(0);
        estadoInicial.clear();

        List<ActivoFijo> activos = activoServicio.cargarActivosDesdeBD();
        for (ActivoFijo activo : activos) {
            if (activo.getEstado().equalsIgnoreCase("Depreciado") ||
                    activo.getEstado().equalsIgnoreCase("Baja") ||
                    activo.getEstado().equalsIgnoreCase("Baja por venta")) {

                Object[] row = {
                        activo.getNumero(),
                        activo.getNombre(),
                        activo.getCategoria(),
                        activo.getEstado(),
                        activo.getFechaAdquisicion(),
                        activo.getCostoInicial()
                };
                tableModel.addRow(row);
                estadoInicial.add(row);
            }
        }

        SwingUtilities.invokeLater(() -> {
            activosTable.revalidate();
            activosTable.repaint();
        });
    }

    private void filtrarPorTipoActivo(String tipoActivo) {
        tableModel.setRowCount(0);

        for (Object[] row : estadoInicial) {
            boolean tipoCoincide = tipoActivo.equals("Todos") || row[2].equals(tipoActivo);
            if (tipoCoincide) {
                tableModel.addRow(row);
            }
        }

        SwingUtilities.invokeLater(() -> {
            activosTable.revalidate();
            activosTable.repaint();
        });
    }

    private void buscarActivo(String query) {
        tableModel.setRowCount(0);

        for (Object[] row : estadoInicial) {
            boolean coincideBusqueda = row[1].toString().toLowerCase().contains(query.toLowerCase()) ||
                    row[0].toString().equals(query);

            if (coincideBusqueda) {
                tableModel.addRow(row);
            }
        }

        SwingUtilities.invokeLater(() -> {
            activosTable.revalidate();
            activosTable.repaint();
        });
    }

    private void renovarActivoSeleccionado() {
        int selectedRow = activosTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Seleccione un activo para renovar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int idActivo = Integer.parseInt(tableModel.getValueAt(selectedRow, 0).toString());
        String nombreActivo = tableModel.getValueAt(selectedRow, 1).toString();
        String motivo = null;
        boolean motivoValido = false;

        while (!motivoValido) {
            motivo = JOptionPane.showInputDialog(this, "Ingrese el motivo de la renovación:", "Motivo de Renovación", JOptionPane.PLAIN_MESSAGE);

            if (motivo == null) {
                return;
            }

            if (!motivo.trim().isEmpty()) {
                motivoValido = true;
            } else {
                JOptionPane.showMessageDialog(this, "Debe ingresar un motivo para renovar el activo.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        }

        try {
            activoServicio.renovarActivo(idActivo, motivo);
            String mensajeRenovacion = String.format("Activo '%s' (ID: %d) ha sido renovado con éxito.",
                    nombreActivo, idActivo);
            JOptionPane.showMessageDialog(this, "Activo renovado con éxito.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            mainFrame.mostrarMensaje(mensajeRenovacion);
            mainFrame.recargarDatos();
            cargarDatosDesdeBD();
            actualizarTotalRenovados();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error al renovar el activo: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}