package vista;

import com.toedter.calendar.JDateChooser;
import util.ValidadorActivo;
import util.ValidadorFechas;
import util.VentanaUtils;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FiltroFrame extends JFrame {
    private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private JTextField txtCostoMin, txtCostoMax;
    private JDateChooser fechaInicio, fechaFin;
    private JComboBox<String> comboCategoria, comboEstado;
    private JButton btnAplicar, btnReset;

    private Map<String, Object> filtrosActuales;

    public FiltroFrame(MainFrame mainFrame, Map<String, Object> filtrosGuardados) {
        setTitle("Filtrar Activos Fijos");
        setSize(800, 300);
        setLocationRelativeTo(null);
        VentanaUtils.deshabilitarMinimizar(this);
        setResizable(false);
        setLayout(new BorderLayout(10, 10));

        filtrosActuales = filtrosGuardados != null ? new HashMap<>(filtrosGuardados) : new HashMap<>();

        // Panel principal
        JPanel panelFiltros = new JPanel();
        panelFiltros.setLayout(new GridBagLayout());
        panelFiltros.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.weightx = 1;

        // Filtro: Costo Inicial
        gbc.gridx = 0; gbc.gridy = 0;
        panelFiltros.add(new JLabel("Costo Inicial (Mín):"), gbc);
        gbc.gridx = 1;
        txtCostoMin = new JTextField();
        panelFiltros.add(txtCostoMin, gbc);

        gbc.gridx = 2;
        panelFiltros.add(new JLabel("Costo Inicial (Máx):"), gbc);
        gbc.gridx = 3;
        txtCostoMax = new JTextField();
        panelFiltros.add(txtCostoMax, gbc);

        // Filtro: Fechas
        gbc.gridx = 0; gbc.gridy = 1;
        panelFiltros.add(new JLabel("Fecha Adquisición (Inicio):"), gbc);
        gbc.gridx = 1;
        fechaInicio = new JDateChooser();
        fechaInicio.setDateFormatString("dd-MM-yyyy");
        panelFiltros.add(fechaInicio, gbc);

        gbc.gridx = 2;
        panelFiltros.add(new JLabel("Fecha Adquisición (Fin):"), gbc);
        gbc.gridx = 3;
        fechaFin = new JDateChooser();
        fechaFin.setDateFormatString("dd-MM-yyyy");
        panelFiltros.add(fechaFin, gbc);

        // Filtro: Categoría
        gbc.gridx = 0; gbc.gridy = 2;
        panelFiltros.add(new JLabel("Categoría:"), gbc);
        gbc.gridx = 1;
        comboCategoria = new JComboBox<>(new String[]{"Todos", "Equipo de Computo", "Mobiliario", "Equipo de Laboratorio", "Vehiculo", "Terreno"});
        panelFiltros.add(comboCategoria, gbc);

        // Filtro: Estado
        gbc.gridx = 2;
        panelFiltros.add(new JLabel("Estado:"), gbc);
        gbc.gridx = 3;
        comboEstado = new JComboBox<>(new String[]{"Todos", "Alta", "Depreciado", "Baja", "Baja por venta"});
        panelFiltros.add(comboEstado, gbc);

        // Botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnAplicar = new JButton("Aplicar");
        btnAplicar.addActionListener(e -> aplicarFiltros(mainFrame));
        btnReset = new JButton("Resetear");
        btnReset.addActionListener(e -> resetearFiltros(mainFrame));

        panelBotones.add(btnAplicar);
        panelBotones.add(btnReset);

        add(panelFiltros, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        cargarFiltros();
    }

    private void aplicarFiltros(MainFrame mainFrame) {
        Map<String, Object> criterios = new HashMap<>();

        try {
            RowSorter<? extends TableModel> sorter = mainFrame.getTable().getRowSorter();
            int sortedColumnIndex = sorter.getSortKeys().isEmpty() ? -1 : sorter.getSortKeys().get(0).getColumn();
            SortOrder sortOrder = sorter.getSortKeys().isEmpty() ? SortOrder.UNSORTED : sorter.getSortKeys().get(0).getSortOrder();

            if (!txtCostoMin.getText().isEmpty()) {
                String errorCostoMin = ValidadorActivo.validarCostoInicial(txtCostoMin.getText());
                if (errorCostoMin != null) {
                    throw new IllegalArgumentException(errorCostoMin);
                }
                criterios.put("costoMin", Double.parseDouble(txtCostoMin.getText()));
            }

            if (!txtCostoMax.getText().isEmpty()) {
                String errorCostoMax = ValidadorActivo.validarCostoInicial(txtCostoMax.getText());
                if (errorCostoMax != null) {
                    throw new IllegalArgumentException(errorCostoMax);
                }
                criterios.put("costoMax", Double.parseDouble(txtCostoMax.getText()));
            }

            if (criterios.containsKey("costoMin") && criterios.containsKey("costoMax")) {
                double costoMin = (double) criterios.get("costoMin");
                double costoMax = (double) criterios.get("costoMax");
                if (costoMin > costoMax) {
                    throw new IllegalArgumentException("El costo mínimo no puede ser mayor que el costo máximo.");
                }
            }

            if (fechaInicio.getDate() != null) {
                LocalDate inicio = convertirDateALocalDate(fechaInicio.getDate());
                String fechaInicioStr = convertirLocalDateAString(inicio);
                String errorFechaInicio = ValidadorFechas.validarFecha(fechaInicioStr);
                if (errorFechaInicio != null) {
                    throw new IllegalArgumentException(errorFechaInicio);
                }
                criterios.put("fechaInicio", inicio);
            }
            if (fechaFin.getDate() != null) {
                LocalDate fin = convertirDateALocalDate(fechaFin.getDate());
                String fechaFinStr = convertirLocalDateAString(fin);
                String errorFechaFin = ValidadorFechas.validarFecha(fechaFinStr);
                if (errorFechaFin != null) {
                    throw new IllegalArgumentException(errorFechaFin);
                }
                criterios.put("fechaFin", fin);
            }

            if (criterios.containsKey("fechaInicio") && criterios.containsKey("fechaFin")) {
                LocalDate inicio = (LocalDate) criterios.get("fechaInicio");
                LocalDate fin = (LocalDate) criterios.get("fechaFin");
                if (inicio.isAfter(fin)) {
                    throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin.");
                }
            }

            if (!comboCategoria.getSelectedItem().toString().equals("Todos")) {
                criterios.put("categoria", comboCategoria.getSelectedItem().toString());
            }
            if (!comboEstado.getSelectedItem().toString().equals("Todos")) {
                criterios.put("estado", comboEstado.getSelectedItem().toString());
            }

            filtrosActuales.clear();
            filtrosActuales.putAll(criterios);

            mainFrame.actualizarFiltrosGuardados(filtrosActuales);

            mainFrame.filtrarActivosAvanzado(criterios);

            if (mainFrame.tablaEstaVacia()) {
                JOptionPane.showMessageDialog(this,
                        "No se encontraron activos que coincidan con los filtros seleccionados.",
                        "Sin Resultados",
                        JOptionPane.INFORMATION_MESSAGE);
            }

            mainFrame.configurarOrdenamiento();

            if (sortedColumnIndex != -1) {
                sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(sortedColumnIndex, sortOrder)));
            }

        } catch (IllegalArgumentException ex) {
            mostrarMensajeError(ex.getMessage());
        }
    }

    private static LocalDate convertirDateALocalDate(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static String convertirLocalDateAString(LocalDate date) {
        return date.format(FORMATO_FECHA);
    }

    private static Date convertirLocalDateADate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public void resetearFiltros(MainFrame mainFrame) {
        txtCostoMin.setText("");
        txtCostoMax.setText("");
        fechaInicio.setDate(null);
        fechaFin.setDate(null);
        comboCategoria.setSelectedIndex(0);
        comboEstado.setSelectedIndex(0);

        filtrosActuales.clear();
        mainFrame.actualizarFiltrosGuardados(new HashMap<>());

        mainFrame.filtrarActivosAvanzado(new HashMap<>());

        mainFrame.configurarOrdenamiento();
    }

    private void cargarFiltros() {
        if (filtrosActuales.containsKey("costoMin")) {
            txtCostoMin.setText(String.valueOf(filtrosActuales.get("costoMin")));
        }
        if (filtrosActuales.containsKey("costoMax")) {
            txtCostoMax.setText(String.valueOf(filtrosActuales.get("costoMax")));
        }
        if (filtrosActuales.containsKey("fechaInicio")) {
            Object fechaInicioObj = filtrosActuales.get("fechaInicio");
            if (fechaInicioObj instanceof LocalDate) {
                fechaInicio.setDate(convertirLocalDateADate((LocalDate) fechaInicioObj));
            } else if (fechaInicioObj instanceof Date) {
                fechaInicio.setDate((Date) fechaInicioObj);
            }
        }
        if (filtrosActuales.containsKey("fechaFin")) {
            Object fechaFinObj = filtrosActuales.get("fechaFin");
            if (fechaFinObj instanceof LocalDate) {
                fechaFin.setDate(convertirLocalDateADate((LocalDate) fechaFinObj));
            } else if (fechaFinObj instanceof Date) {
                fechaFin.setDate((Date) fechaFinObj);
            }
        }
        if (filtrosActuales.containsKey("categoria")) {
            comboCategoria.setSelectedItem(filtrosActuales.get("categoria"));
        }
        if (filtrosActuales.containsKey("estado")) {
            comboEstado.setSelectedItem(filtrosActuales.get("estado"));
        }
    }

    private void mostrarMensajeError(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Error", JOptionPane.ERROR_MESSAGE);
    }
}