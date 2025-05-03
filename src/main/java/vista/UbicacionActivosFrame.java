package vista;

import modelo.ActivoFijo;
import modelo.Sede;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import servicio.ActivoServicio;
import util.VentanaUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class UbicacionActivosFrame extends JFrame {
    private ActivoServicio activoServicio;

    public UbicacionActivosFrame(ActivoServicio activoServicio) {
        this.activoServicio = activoServicio;
        setTitle("Distribución de Activos por Ubicación");
        setSize(1200, 700);
        setResizable(false);
        setLocationRelativeTo(null);
        VentanaUtils.deshabilitarMinimizar(this);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Panel principal
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // Panel superior
        JPanel headerPanel = crearPanelContadores();
        mainPanel.add(headerPanel, BorderLayout.NORTH);

        // Panel central
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // Panel de tarjetas
        JScrollPane cardsScrollPane = new JScrollPane(crearPanelTarjetas());
        cardsScrollPane.setPreferredSize(new Dimension(700, 0));
        centerPanel.add(cardsScrollPane, BorderLayout.CENTER);

        // Panel del gráfico
        JPanel chartPanel = crearPanelGrafico();
        chartPanel.setPreferredSize(new Dimension(450, 0));
        centerPanel.add(chartPanel, BorderLayout.EAST);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);
        setVisible(true);
    }

    private JPanel crearPanelContadores() {
        JPanel headerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        headerPanel.setBackground(new Color(240, 240, 240));

        Map<Sede, Long> activosPorSede = activoServicio.obtenerConteoPorSede();

        if (activosPorSede.isEmpty()) {
            JLabel lblNoData = new JLabel("No hay datos de sedes disponibles");
            lblNoData.setFont(new Font("Arial", Font.BOLD, 16));
            headerPanel.add(lblNoData);
            return headerPanel;
        }

        for (Map.Entry<Sede, Long> entry : activosPorSede.entrySet()) {
            JPanel counterPanel = new JPanel(new BorderLayout());
            counterPanel.setBackground(new Color(245, 245, 245));
            counterPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            counterPanel.setPreferredSize(new Dimension(180, 80));

            JLabel lblSede = new JLabel(entry.getKey().getNombre(), SwingConstants.CENTER);
            lblSede.setForeground(new Color(54, 54, 54));
            lblSede.setFont(new Font("Arial", Font.BOLD, 14));
            lblSede.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));

            JLabel lblConteo = new JLabel(String.valueOf(entry.getValue()), SwingConstants.CENTER);
            lblConteo.setForeground(new Color(54, 162, 235));
            lblConteo.setFont(new Font("Arial", Font.BOLD, 36));

            counterPanel.add(lblSede, BorderLayout.NORTH);
            counterPanel.add(lblConteo, BorderLayout.CENTER);

            headerPanel.add(counterPanel);
        }

        return headerPanel;
    }

    private JPanel crearPanelTarjetas() {
        JPanel cardsPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        cardsPanel.setBackground(Color.WHITE);

        List<ActivoFijo> activos = activoServicio.cargarActivosDesdeBD();

        Map<Sede, List<ActivoFijo>> activosPorSede = activos.stream()
                .filter(activo -> activo.getSede() != null)
                .collect(Collectors.groupingBy(ActivoFijo::getSede));

        for (Map.Entry<Sede, List<ActivoFijo>> entry : activosPorSede.entrySet()) {
            List<ActivoFijo> activosEnSede = entry.getValue();

            for (ActivoFijo activo : activosEnSede) {
                JPanel card = crearTarjetaActivo(activo);
                cardsPanel.add(card);
            }
        }

        JPanel containerPanel = new JPanel(new BorderLayout());
        containerPanel.setBackground(Color.WHITE);

        JScrollPane scrollPane = new JScrollPane(cardsPanel);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        containerPanel.add(scrollPane, BorderLayout.CENTER);

        return containerPanel;
    }

    private JPanel crearTarjetaActivo(ActivoFijo activo) {
        JPanel cardPanel = new JPanel(new BorderLayout(10, 10));
        cardPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setPreferredSize(new Dimension(250, 150));

        // Panel izquierdo
        JPanel iconPanel = new JPanel();
        iconPanel.setPreferredSize(new Dimension(80, 80));
        iconPanel.setBackground(new Color(255, 255, 255, 0));
        JLabel iconLabel = new JLabel();
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        iconLabel.setVerticalAlignment(SwingConstants.CENTER);

        try {
            String iconPath = switch (activo.getCategoria().toLowerCase()) {
                case "equipo de computo" -> "/computer_icon.png";
                case "mobiliario" -> "/sofa_icon.png";
                case "equipo de laboratorio" -> "/lab_icon.png";
                case "vehiculo" -> "/car_icon.png";
                case "terreno" -> "/building_icon.png";
                default -> "/question_icon.png";
            };

            ImageIcon originalIcon = new ImageIcon(getClass().getResource(iconPath));
            Image scaledImage = originalIcon.getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH);
            iconLabel.setIcon(new ImageIcon(scaledImage));
        } catch (NullPointerException e) {
            System.err.println("Icono no encontrado para categoría: " + activo.getCategoria());
            iconLabel.setIcon(new ImageIcon(getClass().getResource("/question_icon.png")));
        }
        iconPanel.add(iconLabel);

        // Panel central
        JPanel infoPanel = new JPanel(new GridLayout(3, 1));
        infoPanel.setBackground(Color.WHITE);

        JLabel lblNombre = new JLabel(activo.getNombre(), SwingConstants.LEFT);
        lblNombre.setFont(new Font("Arial", Font.BOLD, 14));
        lblNombre.setForeground(new Color(54, 54, 54));

        JLabel lblCodigo = new JLabel(activo.getCodigoPatrimonial(), SwingConstants.LEFT);
        lblCodigo.setFont(new Font("Arial", Font.PLAIN, 12));
        lblCodigo.setForeground(new Color(120, 120, 120));

        JLabel lblCategoria = new JLabel("Estado: " + activo.getEstado(), SwingConstants.LEFT);
        lblCategoria.setFont(new Font("Arial", Font.PLAIN, 12));
        lblCategoria.setForeground(new Color(90, 90, 90));

        infoPanel.add(lblNombre);
        infoPanel.add(lblCodigo);
        infoPanel.add(lblCategoria);

        // Panel inferior
        JPanel footerPanel = new JPanel(new BorderLayout());
        footerPanel.setBackground(Color.WHITE);

        JLabel lblUbicacion = new JLabel("Ubicación: " + activo.getSede().getNombre(), SwingConstants.LEFT);
        lblUbicacion.setFont(new Font("Arial", Font.PLAIN, 12));
        lblUbicacion.setForeground(new Color(150, 150, 150));

        footerPanel.add(lblUbicacion, BorderLayout.WEST);

        cardPanel.add(iconPanel, BorderLayout.WEST);
        cardPanel.add(infoPanel, BorderLayout.CENTER);
        cardPanel.add(footerPanel, BorderLayout.SOUTH);

        return cardPanel;
    }

    private JPanel crearPanelGrafico() {
        JPanel chartContainer = new JPanel(new BorderLayout());
        chartContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        Map<Sede, Long> activosPorSede = activoServicio.obtenerConteoPorSede();

        DefaultPieDataset dataset = new DefaultPieDataset();
        for (Map.Entry<Sede, Long> entry : activosPorSede.entrySet()) {
            dataset.setValue(entry.getKey().getNombre(), entry.getValue());
        }

        JFreeChart chart = ChartFactory.createPieChart(
                "Distribución de Activos por Ubicación",
                dataset,
                false,
                true,
                false
        );

        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({1})"));
        plot.setBackgroundPaint(new Color(0, 0, 0, 0));

        Color[] colores = {
                new Color(169, 209, 142),
                new Color(255, 192, 0),
                new Color(112, 173, 71),
                new Color(237, 125, 49),
                new Color(91, 155, 213),
                new Color(193, 193, 193),
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

        ChartPanel chartPanel = new ChartPanel(chart);
        chartContainer.add(chartPanel, BorderLayout.CENTER);

        return chartContainer;
    }
}