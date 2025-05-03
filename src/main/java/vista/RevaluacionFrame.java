package vista;

import servicio.ActivoServicio;
import servicio.RevaluacionServicio;
import util.ImpactoTotalCellRenderer;
import util.VentanaUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RevaluacionFrame extends JFrame {
    private final MainFrame mainFrame;
    private final ActivoServicio activoServicio;
    private DefaultTableModel tableModel;
    private final Set<Integer> filasBloqueadas = new HashSet<>();
    private boolean cambiosRealizados = false;
    private JTable activosTable;
    private JComboBox<String> cmbTipoActivo;
    private JTextField txtBuscarActivo;
    private List<Object[]> estadoInicial = new ArrayList<>();

    public RevaluacionFrame(ActivoServicio servicio, MainFrame mainFrame) {
        this.activoServicio = servicio;
        this.mainFrame = mainFrame;
        initUI();
    }

    private void initUI() {
        setTitle("Revaluación de Activos");
        setSize(1000, 600);
        setLocationRelativeTo(null);
        VentanaUtils.deshabilitarMinimizar(this);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        // Panel superior
        JPanel filterPanel = new JPanel(new GridBagLayout());
        filterPanel.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10)); // Márgenes adicionales
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JLabel lblTipoActivo = new JLabel("Tipo de Activo:");
        cmbTipoActivo = new JComboBox<>(new String[]{"Todos", "Equipo de Computo", "Mobiliario",
                "Equipo de Laboratorio", "Vehiculo", "Terreno"});

        // Campo de búsqueda
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
                "ID", "Nombre", "Categoría", "Valor Anterior", "Valor Revaluado",
                "Depreciación Anterior", "Nueva Depreciación", "Cambio Absoluto", "Impacto Total"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 && !filasBloqueadas.contains(row);
            }
        };
        activosTable = new JTable(tableModel);
        activosTable.getTableHeader().setReorderingAllowed(false);
        activosTable.getColumnModel().getColumn(8).setCellRenderer(new ImpactoTotalCellRenderer());

        JScrollPane tableScrollPane = new JScrollPane(activosTable);

        // Panel inferior
        JPanel buttonPanel = new JPanel(new BorderLayout());

        // Mensaje guía
        JLabel lblGuia = new JLabel("Seleccione (con doble click) un campo en la columna 'Valor Revaluado' para revaluar.");
        lblGuia.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        lblGuia.setFont(new Font("SansSerif", Font.PLAIN, 12));

        // Panel de botones
        JPanel botonesPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnGuardar = new JButton("Guardar");
        JButton btnCancelar = new JButton("Cancelar");

        botonesPanel.add(btnGuardar);
        botonesPanel.add(btnCancelar);

        buttonPanel.add(lblGuia, BorderLayout.WEST);
        buttonPanel.add(botonesPanel, BorderLayout.EAST);

        cmbTipoActivo.addActionListener(e -> filtrarPorTipoActivo(cmbTipoActivo.getSelectedItem().toString()));
        btnBuscar.addActionListener(e -> buscarActivo(txtBuscarActivo.getText()));
        btnReload.addActionListener(e -> {
            txtBuscarActivo.setText("");
            String categoriaSeleccionada = cmbTipoActivo.getSelectedItem().toString();
            filtrarPorTipoActivo(categoriaSeleccionada);
        });

        btnGuardar.addActionListener(e -> guardarRevaluaciones(activosTable));
        btnCancelar.addActionListener(e -> {
            tableModel.setRowCount(0);
            filasBloqueadas.clear();
            dispose();
        });

        add(filterPanel, BorderLayout.NORTH);
        add(tableScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        cargarDatosDesdeBD();
        cambiosRealizados = false;
    }

    private void filtrarPorTipoActivo(String tipoActivo) {
        tableModel.setRowCount(0);
        filasBloqueadas.clear();
        RevaluacionServicio revaluacionServicio = new RevaluacionServicio();
        List<Object[]> datos = revaluacionServicio.cargarDatosDesdeBD();

        for (int i = 0; i < datos.size(); i++) {
            Object[] row = datos.get(i);

            if (tipoActivo.equals("Todos") || row[2].equals(tipoActivo)) {
                if (row[4] != null && !row[4].toString().trim().isEmpty()) {
                    try {
                        double valorAnterior = (double) row[3];
                        double valorRevaluado = Double.parseDouble(row[4].toString());

                        row[7] = valorRevaluado - valorAnterior;
                        row[8] = String.format("%.2f%%", new RevaluacionServicio().calcularImpactoPorcentual(valorAnterior, valorRevaluado));

                        filasBloqueadas.add(tableModel.getRowCount());
                    } catch (NumberFormatException | ClassCastException e) {
                        row[7] = "";
                        row[8] = "";
                    }
                } else {
                    row[7] = "";
                    row[8] = "";
                }

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
        filasBloqueadas.clear();

        RevaluacionServicio revaluacionServicio = new RevaluacionServicio();
        List<Object[]> datos = revaluacionServicio.cargarDatosDesdeBD();

        String categoriaSeleccionada = ((JComboBox<String>) ((JPanel) getContentPane().getComponent(0))
                .getComponent(1)).getSelectedItem().toString();

        for (int i = 0; i < datos.size(); i++) {
            Object[] row = datos.get(i);

            boolean coincideCategoria = categoriaSeleccionada.equals("Todos") || row[2].equals(categoriaSeleccionada);

            boolean coincideBusqueda = row[1].toString().toLowerCase().contains(query.toLowerCase()) || row[0].toString().equals(query);

            if (coincideCategoria && coincideBusqueda) {
                if (row[4] != null && !row[4].toString().trim().isEmpty()) {
                    try {
                        double valorAnterior = (double) row[3];
                        double valorRevaluado = Double.parseDouble(row[4].toString());

                        row[7] = valorRevaluado - valorAnterior;
                        row[8] = String.format("%.2f%%", new RevaluacionServicio().calcularImpactoPorcentual(valorAnterior, valorRevaluado));

                        filasBloqueadas.add(tableModel.getRowCount());
                    } catch (NumberFormatException | ClassCastException e) {
                        row[7] = "";
                        row[8] = "";
                    }
                } else {
                    row[7] = "";
                    row[8] = "";
                }

                tableModel.addRow(row);
            }
        }

        SwingUtilities.invokeLater(() -> {
            activosTable.revalidate();
            activosTable.repaint();
        });
    }

    private void guardarRevaluaciones(JTable activosTable) {
        try {
            boolean cambiosGuardados = false;
            List<String> mensajesRevaluacion = new ArrayList<>();

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (filasBloqueadas.contains(i)) {
                    continue;
                }

                String valorRevaluadoStr = tableModel.getValueAt(i, 4) != null
                        ? tableModel.getValueAt(i, 4).toString().trim()
                        : "";

                if (valorRevaluadoStr.isEmpty()) {
                    continue;
                }

                double valorRevaluado;
                try {
                    valorRevaluado = Double.parseDouble(valorRevaluadoStr);

                    if (valorRevaluado <= 0) {
                        throw new IllegalArgumentException("El valor revaluado en la fila " + (i + 1) + " debe ser mayor a 0.");
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "El valor revaluado en la fila " + (i + 1) + " debe ser un número válido.", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                double valorAnterior = (double) tableModel.getValueAt(i, 3);

                if (valorRevaluado <= valorAnterior) {
                    JOptionPane.showMessageDialog(this, "El valor revaluado en la fila " + (i + 1) + " debe ser mayor al valor anterior.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                int idActivo = Integer.parseInt(tableModel.getValueAt(i, 0).toString());
                String nombreActivo = tableModel.getValueAt(i, 1).toString();

                mensajesRevaluacion.add("Activo '" + nombreActivo + "' (ID: " + idActivo + ") revaluado de " +
                        String.format("%.2f", valorAnterior) + " a " +
                        String.format("%.2f", valorRevaluado) + ".");

                cambiosGuardados = true;
            }

            if (!cambiosGuardados) {
                JOptionPane.showMessageDialog(this, "No se ha revaluado ningún activo. Por favor, realice cambios antes de guardar.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                return;
            }

            String motivoGeneral = null;
            boolean motivoValido = false;
            while (!motivoValido) {
                motivoGeneral = JOptionPane.showInputDialog(this, "Ingrese el motivo para esta revaluación:", "Motivo de Revaluación", JOptionPane.PLAIN_MESSAGE);

                if (motivoGeneral == null) {
                    return;
                }

                if (!motivoGeneral.trim().isEmpty()) {
                    motivoValido = true;
                } else {
                    JOptionPane.showMessageDialog(this, "Debe ingresar un motivo para guardar los cambios.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                }
            }

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                if (filasBloqueadas.contains(i)) {
                    continue;
                }

                String valorRevaluadoStr = tableModel.getValueAt(i, 4) != null
                        ? tableModel.getValueAt(i, 4).toString().trim()
                        : "";

                if (valorRevaluadoStr.isEmpty()) {
                    continue;
                }

                double valorRevaluado = Double.parseDouble(valorRevaluadoStr);
                double valorAnterior = (double) tableModel.getValueAt(i, 3);

                int idActivo = Integer.parseInt(tableModel.getValueAt(i, 0).toString());
                new RevaluacionServicio().guardarRevaluacionEnBD(idActivo, valorRevaluado, motivoGeneral);

                filasBloqueadas.add(i);
                tableModel.setValueAt(valorRevaluado, i, 3);
                tableModel.setValueAt("", i, 4);
            }

            SwingUtilities.invokeLater(() -> {
                activosTable.revalidate();
                activosTable.repaint();
            });
            cmbTipoActivo.setSelectedIndex(0);
            txtBuscarActivo.setText("");
            filtrarPorTipoActivo("Todos");

            if (mainFrame != null) {
                mainFrame.recargarDatos();
            }

            JOptionPane.showMessageDialog(this, "Revaluaciones guardadas exitosamente.");
            for (String mensaje : mensajesRevaluacion) {
                mainFrame.mostrarMensaje(mensaje);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error al guardar las revaluaciones: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void cargarDatosDesdeBD() {
        RevaluacionServicio revaluacionServicio = new RevaluacionServicio();

        tableModel.setRowCount(0);
        filasBloqueadas.clear();
        estadoInicial.clear();
        cambiosRealizados = false;

        List<Object[]> datos = revaluacionServicio.cargarDatosDesdeBD();

        for (int i = 0; i < datos.size(); i++) {
            Object[] row = datos.get(i);

            if (row[4] != null && !row[4].toString().trim().isEmpty()) {
                try {
                    double valorAnterior = (double) row[3];
                    double valorRevaluado = Double.parseDouble(row[4].toString());

                    row[7] = String.format("%.2f", valorRevaluado - valorAnterior);
                    row[8] = String.format("%.2f%%", new RevaluacionServicio().calcularImpactoPorcentual(valorAnterior, valorRevaluado));

                    filasBloqueadas.add(i);
                } catch (NumberFormatException | ClassCastException e) {
                    row[7] = "";
                    row[8] = "";
                }
            } else {
                row[7] = "";
                row[8] = "";
            }

            tableModel.addRow(row);
            estadoInicial.add(new Object[]{row[3], row[4]});
        }

        SwingUtilities.invokeLater(() -> {
            activosTable.revalidate();
            activosTable.repaint();
        });
    }
}