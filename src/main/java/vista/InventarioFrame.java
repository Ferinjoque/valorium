package vista;

import modelo.ActivoFijo;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import util.NonEditableTableModel;
import util.VentanaUtils;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.text.NumberFormat;
import java.util.Locale;

public class InventarioFrame extends JFrame {
    private List<ActivoFijo> inventario;
    private JTable tablaInventario;
    private JComboBox<String> filtroCombo;
    private JPanel contenedorGrafico;
    private JPanel panelResumen;

    public InventarioFrame(List<ActivoFijo> inventarioOriginal) {
        this.inventario = inventarioOriginal.stream()
                .filter(activo -> !activo.getEstado().equalsIgnoreCase("Renovado"))
                .collect(Collectors.toList());

        setTitle("Inventario de Activos");
        setSize(1400, 800);
        setResizable(false);
        setLocationRelativeTo(null);
        VentanaUtils.deshabilitarMinimizar(this);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        initComponents();
        setVisible(true);
    }

    private void initComponents() {
        JPanel panelPrincipal = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // Tabla de inventario
        NonEditableTableModel modeloTabla = crearModeloTabla();
        llenarModeloTabla(modeloTabla);

        tablaInventario = new JTable(modeloTabla);
        tablaInventario.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollTabla = new JScrollPane(tablaInventario);

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0.7;
        gbc.weighty = 1.0;
        gbc.fill = GridBagConstraints.BOTH;
        panelPrincipal.add(scrollTabla, gbc);

        // Panel de resumen
        panelResumen = new JPanel(new GridBagLayout());
        panelResumen.setBackground(Color.WHITE);
        GridBagConstraints gbcResumen = new GridBagConstraints();
        gbcResumen.insets = new Insets(10, 10, 10, 10);
        gbcResumen.fill = GridBagConstraints.HORIZONTAL;
        gbcResumen.gridx = 0;
        gbcResumen.weightx = 1;

        agregarResumenPanel(gbcResumen);

        // Filtro
        filtroCombo = new JComboBox<>(new String[]{"Categoría", "Estado", "Años"});
        filtroCombo.setPreferredSize(new Dimension(150, 30));
        filtroCombo.setAlignmentX(Component.CENTER_ALIGNMENT);
        gbcResumen.gridy = 3;
        gbcResumen.fill = GridBagConstraints.NONE;
        panelResumen.add(filtroCombo, gbcResumen);

        // Gráfico
        contenedorGrafico = new JPanel(new BorderLayout());
        contenedorGrafico.setBackground(Color.WHITE);
        actualizarGrafico("Categoría");

        gbcResumen.gridy = 4;
        gbcResumen.fill = GridBagConstraints.BOTH;
        gbcResumen.weighty = 1;
        panelResumen.add(contenedorGrafico, gbcResumen);

        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridheight = 2;
        gbc.weightx = 0.3;
        gbc.fill = GridBagConstraints.BOTH;
        panelPrincipal.add(panelResumen, gbc);

        filtroCombo.addActionListener(e -> actualizarGrafico((String) filtroCombo.getSelectedItem()));

        add(panelPrincipal);
    }

    private NonEditableTableModel crearModeloTabla() {
        NonEditableTableModel modelo = new NonEditableTableModel();
        modelo.addColumn("Código Patrimonial");
        modelo.addColumn("Nombre");
        modelo.addColumn("Categoría");
        modelo.addColumn("Estado");
        modelo.addColumn("Fecha de Adquisición");
        modelo.addColumn("Costo Inicial");
        modelo.addColumn("Depreciación Mensual");
        modelo.addColumn("Depreciación Acumulada");
        modelo.addColumn("Valor Residual");
        return modelo;
    }

    private void llenarModeloTabla(NonEditableTableModel modeloTabla) {
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

        for (ActivoFijo activo : inventario) {
            modeloTabla.addRow(new Object[]{
                    activo.getCodigoPatrimonial(),
                    activo.getNombre(),
                    activo.getCategoria(),
                    activo.getEstado(),
                    activo.getFechaAdquisicion(),
                    numberFormat.format(activo.getCostoInicial()),
                    numberFormat.format(activo.calcularDepreciacionMensual()),
                    numberFormat.format(activo.calcularDepreciacionAcumulada()),
                    numberFormat.format(activo.calcularValorResidual())
            });
        }
    }

    private void agregarResumenPanel(GridBagConstraints gbcResumen) {
        int totalActivos = inventario.size();

        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
        String costoTotalFormatted = numberFormat.format(inventario.stream().mapToDouble(ActivoFijo::getCostoInicial).sum());
        String valorLibrosTotalFormatted = numberFormat.format(inventario.stream().mapToDouble(ActivoFijo::calcularValorResidual).sum());

        JLabel lblTotalActivos = new JLabel(
                String.format("<html><div style='text-align: center; font-size: 16px;'>Total de Activos</div><div style='text-align: center; font-size: 24px; font-weight: bold;'>%d</div></html>", totalActivos),
                SwingConstants.CENTER
        );

        JLabel lblCostoTotal = new JLabel(
                String.format("<html><div style='text-align: center; font-size: 16px;'>Costo Inicial Total</div><div style='text-align: center; font-size: 24px; font-weight: bold;'>S/%s</div></html>", costoTotalFormatted),
                SwingConstants.CENTER
        );

        JLabel lblValorLibrosTotal = new JLabel(
                String.format("<html><div style='text-align: center; font-size: 16px;'>Valor en Libros Total</div><div style='text-align: center; font-size: 24px; font-weight: bold;'>S/%s</div></html>", valorLibrosTotalFormatted),
                SwingConstants.CENTER
        );

        gbcResumen.gridy = 0;
        panelResumen.add(lblTotalActivos, gbcResumen);
        gbcResumen.gridy = 1;
        panelResumen.add(lblCostoTotal, gbcResumen);
        gbcResumen.gridy = 2;
        panelResumen.add(lblValorLibrosTotal, gbcResumen);
    }

    private void actualizarGrafico(String criterio) {
        contenedorGrafico.removeAll();
        ChartPanel nuevoGrafico = crearGraficoTorta(criterio);
        contenedorGrafico.add(nuevoGrafico, BorderLayout.CENTER);
        contenedorGrafico.revalidate();
        contenedorGrafico.repaint();
    }

    private ChartPanel crearGraficoTorta(String criterio) {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<String, Long> agrupamiento = inventario.stream()
                .collect(Collectors.groupingBy(
                        criterio.equals("Categoría") ? ActivoFijo::getCategoria :
                                criterio.equals("Estado") ? ActivoFijo::getEstado :
                                        activo -> activo.getFechaAdquisicion().split("-")[2],
                        Collectors.counting()
                ));

        for (Map.Entry<String, Long> entry : agrupamiento.entrySet()) {
            dataset.setValue(entry.getKey(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Distribución de Activos por " + criterio,
                dataset,
                false,
                true,
                false
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({1})"));
        plot.setBackgroundPaint(Color.WHITE);
        plot.setOutlineVisible(false);

        Color[] colores = {
                new Color(169, 209, 142),
                new Color(255, 192, 0),
                new Color(112, 173, 71),
                new Color(237, 125, 49),
                new Color(91, 155, 213),
                new Color(193, 193, 193)
        };

        int index = 0;
        for (Object key : dataset.getKeys()) {
            if (index < colores.length) {
                plot.setSectionPaint(key.toString(), colores[index]);
            } else {
                plot.setSectionPaint(key.toString(), Color.LIGHT_GRAY);
            }
            index++;
        }

        return new ChartPanel(chart);
    }
}