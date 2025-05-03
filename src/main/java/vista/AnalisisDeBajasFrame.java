package vista;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import modelo.ActivoFijo;
import util.ConexionBD;
import util.VentanaUtils;

public class AnalisisDeBajasFrame extends JFrame {

    public AnalisisDeBajasFrame(List<ActivoFijo> activos) {
        setTitle("Reporte de Activos en Baja");
        setSize(1000, 700);
        setLocationRelativeTo(null);
        VentanaUtils.deshabilitarMinimizar(this);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        List<ActivoFijo> activosFiltrados = activos.stream()
                .filter(a -> !a.getEstado().equalsIgnoreCase("Renovado"))
                .toList();

        // Encabezado
        JPanel encabezado = crearPanelEncabezado(activosFiltrados);
        add(encabezado, BorderLayout.NORTH);

        // Tabla de activos
        JTable tablaActivos = crearTablaActivos(activosFiltrados);
        JScrollPane scrollPane = new JScrollPane(tablaActivos);
        scrollPane.setBorder(new EmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);

        // Panel de gráficos
        JTabbedPane panelGraficos = crearPanelGraficos(activosFiltrados);
        add(panelGraficos, BorderLayout.SOUTH);
    }

    private JPanel crearPanelEncabezado(List<ActivoFijo> activos) {
        JPanel panel = new JPanel(new GridLayout(1, 3, 10, 10));
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        double perdidaTotal = activos.stream()
                .filter(a -> a.getEstado().equalsIgnoreCase("Baja") || a.getEstado().equalsIgnoreCase("Baja por venta"))
                .mapToDouble(a -> a.getCostoInicial() - a.calcularValorResidual())
                .sum();

        double montoRecuperado = activos.stream()
                .filter(a -> a.getEstado().equalsIgnoreCase("Baja por venta"))
                .mapToDouble(a -> obtenerPrecioVentaDesdeBD(a.getNumero()))
                .sum();

        double perdidaNeta = perdidaTotal - montoRecuperado;

        // Etiquetas
        JLabel labelPerdidaTotal = new JLabel("Pérdida Bruta: S/ " + String.format("%.2f", perdidaTotal), JLabel.CENTER);
        JLabel labelMontoRecuperado = new JLabel("Monto Recuperado: S/ " + String.format("%.2f", montoRecuperado), JLabel.CENTER);
        JLabel labelPerdidaNeta = new JLabel("Pérdida Neta: S/ " + String.format("%.2f", Math.max(perdidaNeta, 0)), JLabel.CENTER);

        labelPerdidaTotal.setFont(new Font("Arial", Font.BOLD, 16));
        labelMontoRecuperado.setFont(new Font("Arial", Font.BOLD, 16));
        labelPerdidaNeta.setFont(new Font("Arial", Font.BOLD, 16));

        panel.add(labelPerdidaTotal);
        panel.add(labelMontoRecuperado);
        panel.add(labelPerdidaNeta);

        return panel;
    }

    private JTable crearTablaActivos(List<ActivoFijo> activos) {
        String[] columnas = {"ID", "Nombre", "Categoría", "Estado", "Costo Inicial", "Depreciación Acumulada", "Valor Residual", "Pérdida Neta"};
        DefaultTableModel modelo = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        for (ActivoFijo activo : activos) {
            if (activo.getEstado().equalsIgnoreCase("Baja") || activo.getEstado().equalsIgnoreCase("Baja por venta")) {
                double depreciacionAcumulada = activo.calcularDepreciacionAcumulada();
                double valorResidual = activo.calcularValorResidual();
                double perdidaNeta = activo.getCostoInicial() - valorResidual;

                modelo.addRow(new Object[]{
                        activo.getNumero(),
                        activo.getNombre(),
                        activo.getCategoria(),
                        activo.getEstado(),
                        String.format("%.2f", activo.getCostoInicial()),
                        String.format("%.2f", depreciacionAcumulada),
                        String.format("%.2f", valorResidual),
                        String.format("%.2f", Math.max(0, perdidaNeta))
                });
            }
        }
        JTable tabla = new JTable(modelo);
        tabla.getTableHeader().setReorderingAllowed(false);
        return tabla;
    }

    private JTabbedPane crearPanelGraficos(List<ActivoFijo> activos) {
        JTabbedPane panel = new JTabbedPane();
        panel.addTab("Pérdidas por Categoría", crearGraficoBarras(activos));

        boolean hayBajasPorVenta = activos.stream()
                .anyMatch(a -> a.getEstado().equalsIgnoreCase("Baja por venta"));

        if (hayBajasPorVenta) {
            panel.addTab("Análisis de Ventas", crearGraficoVentas(activos));
        }

        return panel;
    }

    private JPanel crearGraficoBarras(List<ActivoFijo> activos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        for (ActivoFijo activo : activos) {
            if (activo.getEstado().equalsIgnoreCase("Baja") || activo.getEstado().equalsIgnoreCase("Baja por venta")) {
                double perdidaNeta = activo.getCostoInicial() - activo.calcularValorResidual();
                if (perdidaNeta > 0) {
                    dataset.addValue(perdidaNeta, activo.getCategoria(), activo.getNombre());
                }
            }
        }
        JFreeChart chart = ChartFactory.createBarChart(
                "Pérdidas por Categoría",
                "Categoría",
                "Monto (S/)",
                dataset,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chart.getTitle().setMargin(20, 0, 20, 0);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        Color[] colores = {
                new Color(112, 173, 71),
                new Color(255, 192, 0),
                new Color(237, 125, 49),
                new Color(91, 155, 213),
                new Color(193, 193, 193)
        };

        for (int i = 0; i < dataset.getRowCount(); i++) {
            renderer.setSeriesPaint(i, colores[i % colores.length]);
        }

        renderer.setBarPainter(new BarRenderer().getBarPainter());
        renderer.setMaximumBarWidth(0.1);

        return new ChartPanel(chart);
    }

    private JPanel crearGraficoVentas(List<ActivoFijo> activos) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (ActivoFijo activo : activos) {
            if (activo.getEstado().equalsIgnoreCase("Baja por venta")) {
                double valorResidual = activo.calcularValorResidual();
                double precioVenta = obtenerPrecioVentaDesdeBD(activo.getNumero());

                dataset.addValue(valorResidual, "Valor Residual", activo.getNombre());
                dataset.addValue(precioVenta, "Precio de Venta", activo.getNombre());
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                "Análisis de Ventas",
                "Activo",
                "Monto (S/)",
                dataset,
                org.jfree.chart.plot.PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        chart.getTitle().setMargin(20, 0, 20, 0);

        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        Color[] colores = {
                new Color(91, 155, 213),
                new Color(112, 173, 71),
                new Color(255, 192, 0),
                new Color(237, 125, 49)
        };

        for (int i = 0; i < dataset.getRowCount(); i++) {
            renderer.setSeriesPaint(i, colores[i % colores.length]);
        }

        renderer.setMaximumBarWidth(0.1);

        return new ChartPanel(chart);
    }

    private double obtenerPrecioVentaDesdeBD(int idActivo) {
        double precioVenta = 0.0;
        String sql = "SELECT precio_venta FROM ventas WHERE id_activo = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, idActivo);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    precioVenta = rs.getDouble("precio_venta");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return precioVenta;
    }
}