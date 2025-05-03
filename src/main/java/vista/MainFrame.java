package vista;

import controlador.ActivoControlador;
import controlador.Main;
import modelo.*;
import org.apache.poi.ss.usermodel.Font;
import servicio.ActivoServicio;
import util.ConexionBD;
import util.ValidadorActivo;
import util.NonEditableTableModel;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileOutputStream;
import javax.swing.JFileChooser;
import com.toedter.calendar.JDateChooser;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.format.TextStyle;
import java.util.*;
import java.text.SimpleDateFormat;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.table.*;

import static modelo.ActivoFijo.determinarVidaUtil;

public class MainFrame extends JFrame {
    private NonEditableTableModel model;
    private JTable table;
    public JTextField txtBuscar;
    private JTextField txtNombre;
    private JComboBox<String> comboCategoria;
    private JDateChooser dateChooserFechaAdquisicion;
    private JTextField txtVidaUtil;
    private JTextField txtCostoInicial;
    private JTextField txtDepreciacionMensual;
    private JTextField txtDepreciacionAcumulada;
    private JTextField txtValorNeto;
    private JTextArea txtAreaMensajes;
    private JPanel panelTotales;
    private JButton btnModificar;
    private JButton btnRegistrar;
    private JButton btnDarBaja;
    private JButton btnVender;
    private JComboBox<String> comboTipoBaja;

    private JButton btnCambios;
    private JButton btnProyectarDepreciacion;
    private JButton btnRevaluar;
    private JButton btnBuscar;
    private JButton btnFiltrar;
    private JButton btnExportar;
    private JButton btnCerrarSesion;
    private JButton btnInventario;
    boolean enModoEdicion = false;
    private List<ActivoFijo> listaBaseDatos;
    private ActivoControlador activoControlador;
    private int indiceActivoEnEdicion = -1;
    private double totalCostoInicial = 0.0;
    private double totalDepreciacionMensual = 0.0;
    private double totalDepreciacionAcumulada = 0.0;
    private double totalValorResidual = 0.0;
    private int numeroDeActivos = 0;
    private ActivoServicio activoServicio;
    private String nombreOriginal;
    private String categoriaOriginal;
    private NotificacionFrame notificacionFrame;
    private int columnaActualOrdenada = -1;
    private SortOrder ordenActual = SortOrder.UNSORTED;
    private Map<String, Object> filtrosGuardados = new HashMap<>();
    private FiltroFrame filtroFrame;
    private JButton btnRenovar;
    private JButton btnUbicacionActivos;
    private JButton btnRecargar;
    private JButton btnResetearBusqueda;
    private JButton btnAnalisisDeBajas;
    private List<ActivoFijo> listaFiltrada;
    private JComboBox<Sede> comboSede;
    public List<ActivoFijo> getListaBaseDatos() {
        return listaBaseDatos;
    }

    public MainFrame() {
        activoServicio = new ActivoServicio(this);
        activoControlador = new ActivoControlador(activoServicio);
        listaBaseDatos = activoServicio.cargarActivosDesdeBD();
        notificacionFrame = new NotificacionFrame(this);

        setTitle("Valorium");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setResizable(false);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));

        configurarUI();
        cargarSedes();
        cargarActivosEnTabla();
        configurarOrdenamiento();
        setVisible(true);
        mostrarNotificacionesPendientes();
    }

    private void configurarUI() {
        // Configuración de la tabla
        model = new NonEditableTableModel();

        model.addColumn("N°");
        model.addColumn("Código Patrimonial");
        model.addColumn("Nombre");
        model.addColumn("Categoría");
        model.addColumn("Estado");
        model.addColumn("Fecha de Adquisición");
        model.addColumn("Vida Útil");
        model.addColumn("Costo Inicial");
        model.addColumn("Depreciación Mensual");
        model.addColumn("Depreciación Acumulada");
        model.addColumn("Valor Residual");

        table = new JTable(model);
        table.getTableHeader().setReorderingAllowed(false);
        JScrollPane scrollPane = new JScrollPane(table);

        // Panel para el lateral derecho (lista de activos)
        JPanel panelDerecho = new JPanel();
        panelDerecho.setLayout(new BorderLayout());

        // Panel para el título
        JPanel panelTitulo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel lblTitulo = new JLabel("Lista de Activos Registrados");
        lblTitulo.setFont(new java.awt.Font("Arial", java.awt.Font.BOLD, 20));
        panelTitulo.add(Box.createVerticalStrut(50));
        panelTitulo.add(lblTitulo);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnModificar = new JButton("Modificar");
        btnModificar.addActionListener(e -> modificarActivo());
        panelBotones.add(btnModificar);
        panelBotones.add(Box.createHorizontalStrut(10));
        btnDarBaja = new JButton("Dar de Baja");
        btnDarBaja.addActionListener(e -> darDeBajaActivo());
        panelBotones.add(btnDarBaja);
        panelBotones.add(Box.createHorizontalStrut(0));
        comboTipoBaja = new JComboBox<>(new String[]{
                "Obsolescencia", "Siniestro", "Fin de vida útil"
        });
        panelBotones.add(comboTipoBaja);
        panelBotones.add(Box.createHorizontalStrut(10));
        btnVender = new JButton("Vender");
        btnVender.addActionListener(e -> venderActivo());
        panelBotones.add(btnVender);

        // Panel para los botones de búsqueda
        JPanel panelBuscar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtBuscar = new JTextField(15);
        panelBuscar.add(txtBuscar);
        txtBuscar.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filtrarActivos(txtBuscar.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filtrarActivos(txtBuscar.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filtrarActivos(txtBuscar.getText());
            }
        });
        panelBuscar.add(Box.createHorizontalStrut(0));
        btnResetearBusqueda = new JButton("✕");
        btnResetearBusqueda.addActionListener(e -> {
            txtBuscar.setText("");
        });
        panelBuscar.add(btnResetearBusqueda);
        panelBuscar.add(Box.createHorizontalStrut(10));
        btnBuscar = new JButton("Buscar");
        btnBuscar.addActionListener(e -> {
            String textoBusqueda = txtBuscar.getText();
            if (!textoBusqueda.isEmpty()) {
                seleccionarPrimeraCoincidencia(textoBusqueda);
            } else {
                mostrarMensaje("Por favor, ingrese un texto para buscar.");
            }
        });
        panelBuscar.add(btnBuscar);
        panelBuscar.add(Box.createHorizontalStrut(10));
        btnFiltrar = new JButton("Filtrar");
        panelBuscar.add(btnFiltrar);
        btnFiltrar.addActionListener(e -> abrirFiltroFrame());
        panelBuscar.add(Box.createHorizontalStrut(10));
        btnExportar = new JButton("Exportar reporte");
        btnExportar.addActionListener(e -> exportarReporte());
        panelBuscar.add(btnExportar);
        panelBuscar.add(Box.createHorizontalStrut(10));
        btnRecargar = new JButton("⟳");
        btnRecargar.addActionListener(e -> recargarDatos());
        panelBuscar.add(btnRecargar);
        panelBuscar.add(Box.createHorizontalStrut(10));

        // Panel que contiene los botones de búsqueda como los de acción
        JPanel panelAcciones = new JPanel(new BorderLayout());
        panelAcciones.add(panelBuscar, BorderLayout.WEST);
        panelAcciones.add(panelBotones, BorderLayout.EAST);

        // Panel que contiene tanto el título como las acciones
        JPanel panelTituloBotones = new JPanel();
        panelTituloBotones.setLayout(new BoxLayout(panelTituloBotones, BoxLayout.Y_AXIS));

        panelTituloBotones.add(panelTitulo);
        panelTituloBotones.add(panelAcciones);
        panelTituloBotones.add(Box.createVerticalStrut(10));
        panelDerecho.add(panelTituloBotones, BorderLayout.NORTH);
        panelDerecho.add(scrollPane, BorderLayout.CENTER);

        // Panel para los totales y el botón
        JPanel panelTotalesConBoton = new JPanel();
        panelTotalesConBoton.setLayout(new BorderLayout());
        panelTotales = new JPanel();
        panelTotales.add(new JLabel("Número de Activos: " + numeroDeActivos + " | "));
        panelTotales.add(new JLabel("Total de Costo Inicial: S/" + formatearNumero(totalCostoInicial) + " | "));
        panelTotales.add(new JLabel("Total de Depreciación Mensual: S/" + formatearNumero(totalDepreciacionMensual) + " | "));
        panelTotales.add(new JLabel("Total de Depreciación Acumulada: S/" + formatearNumero(totalDepreciacionAcumulada) + " | "));
        panelTotales.add(new JLabel("Total de Valor Residual: S/" + formatearNumero(totalValorResidual)));

        panelTotalesConBoton.add(panelTotales, BorderLayout.WEST);
        panelDerecho.add(panelTotalesConBoton, BorderLayout.SOUTH);
        actualizarVistaDeTotales();

        // Panel de logo y registro
        JPanel panelRegistro = new JPanel();
        panelRegistro.setLayout(new BoxLayout(panelRegistro, BoxLayout.Y_AXIS));
        JLabel lblLogo = new JLabel();
        ImageIcon logoIcon = new ImageIcon(getClass().getClassLoader().getResource("Valorium.png"));
        Image scaledImage = logoIcon.getImage().getScaledInstance(171, 145, Image.SCALE_SMOOTH);
        lblLogo.setIcon(new ImageIcon(scaledImage));
        lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblLogo.setBorder(BorderFactory.createEmptyBorder(50, 0, 0, 0));
        panelRegistro.add(lblLogo);

        // Panel de datos
        JPanel panelDatos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        Dimension textFieldSize = new Dimension(200, 25);

        txtNombre = new JTextField();
        txtNombre.setPreferredSize(textFieldSize);
        txtNombre.addActionListener(e -> activarBoton());
        comboCategoria = new JComboBox<>(new String[]{"Equipo de Computo", "Mobiliario", "Equipo de Laboratorio", "Vehiculo", "Terreno"});
        comboCategoria.setPreferredSize(textFieldSize);
        dateChooserFechaAdquisicion = new JDateChooser();
        dateChooserFechaAdquisicion.setDateFormatString("dd-MM-yyyy");
        dateChooserFechaAdquisicion.setPreferredSize(textFieldSize);
        dateChooserFechaAdquisicion.getDateEditor().getUiComponent().addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    activarBoton();
                }
            }
        });
        txtVidaUtil = new JTextField();
        txtVidaUtil.setPreferredSize(textFieldSize);
        txtCostoInicial = new JTextField();
        txtCostoInicial.setPreferredSize(textFieldSize);
        txtCostoInicial.addActionListener(e -> activarBoton());
        comboSede = new JComboBox<>();
        comboSede.setPreferredSize(textFieldSize);
        txtDepreciacionMensual = new JTextField();
        txtDepreciacionMensual.setPreferredSize(textFieldSize);
        txtDepreciacionAcumulada = new JTextField();
        txtDepreciacionAcumulada.setPreferredSize(textFieldSize);
        txtValorNeto = new JTextField();
        txtValorNeto.setPreferredSize(textFieldSize);

        agregarCampo(panelDatos, gbc, 0, "Nombre:", txtNombre);
        agregarCampo(panelDatos, gbc, 1, "Categoría:", comboCategoria);
        agregarCampo(panelDatos, gbc, 2, "Fecha de Adquisición:", dateChooserFechaAdquisicion);
        agregarCampo(panelDatos, gbc, 3, "Costo Inicial:", txtCostoInicial);
        agregarCampo(panelDatos, gbc, 4, "Sede:", comboSede);

        btnRegistrar = new JButton("Registrar");
        btnRegistrar.addActionListener(e -> agregarActivo());
        btnRegistrar.setPreferredSize(new Dimension(100, 30));
        gbc.gridy = 5;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(10, 0, 0, 0);
        panelDatos.add(btnRegistrar, gbc);

        panelRegistro.add(panelDatos);

        // Panel de botones de acción
        JPanel panelBotonesAccion = new JPanel(new GridLayout(4, 2, 10, 10));
        btnCambios = new JButton("Historial de cambios");
        btnCambios.addActionListener(e -> verHistorial());
        panelBotonesAccion.add(btnCambios);
        btnProyectarDepreciacion = new JButton("Proyectar depreciación");
        btnProyectarDepreciacion.addActionListener(e -> proyectarDepreciacion());
        panelBotonesAccion.add(btnProyectarDepreciacion);
        btnInventario = new JButton("Ver inventario");
        btnInventario.addActionListener(e -> {
            List<ActivoFijo> activos = activoServicio.cargarActivosDesdeBD();
            if (!activos.isEmpty()) {
                new InventarioFrame(activoControlador.obtenerInventario());
            } else {
                mostrarMensaje("No hay activos registrados en el inventario.");
            }
        });
        panelBotonesAccion.add(btnInventario);
        btnRevaluar = new JButton("Revaluar");
        btnRevaluar.addActionListener(e -> {
            List<ActivoFijo> activosElegibles = activoServicio.filtrarActivos(Map.of("estado", "Alta"));
            if (!activosElegibles.isEmpty()) {
                RevaluacionFrame revaluacionFrame = new RevaluacionFrame(activoServicio, this);
                revaluacionFrame.setVisible(true);
            } else {
                mostrarMensaje("No hay activos elegibles para revaluar.");
            }
        });
        panelBotonesAccion.add(btnRevaluar);
        btnRenovar = new JButton("Renovar");
        btnRenovar.addActionListener(e -> {
            int activosRenovables = activoServicio.contarActivosPorEstado("Depreciado") +
                    activoServicio.contarActivosPorEstado("Baja") +
                    activoServicio.contarActivosPorEstado("Baja por venta");
            if (activosRenovables > 0) {
                RenovacionFrame renovacionFrame = new RenovacionFrame(new ActivoServicio(), this);
                renovacionFrame.setVisible(true);
            } else {
                mostrarMensaje("No hay activos disponibles para renovar.");
            }
        });
        panelBotonesAccion.add(btnRenovar);
        btnAnalisisDeBajas = new JButton("Análisis de Bajas");
        btnAnalisisDeBajas.addActionListener(e -> {
            int activosBaja = activoServicio.contarActivosPorEstado("Baja") +
                    activoServicio.contarActivosPorEstado("Baja por venta");

            if (activosBaja > 0) {
                List<ActivoFijo> activos = activoControlador.obtenerInventario();
                new AnalisisDeBajasFrame(activos).setVisible(true);
            } else {
                mostrarMensaje("No hay activos en baja para analizar.");
            }
        });
        panelBotonesAccion.add(btnAnalisisDeBajas);
        btnUbicacionActivos = new JButton("Ubicación de Activos");
        btnUbicacionActivos.addActionListener(e -> {
            List<Sede> sedes = activoServicio.cargarSedesDesdeBD();
            List<ActivoFijo> activos = activoServicio.cargarActivosDesdeBD();
            if (!sedes.isEmpty() && !activos.isEmpty()) {
                new UbicacionActivosFrame(activoServicio);
            } else if (sedes.isEmpty()) {
                mostrarMensaje("No hay sedes registradas en el sistema.");
            } else if (activos.isEmpty()) {
                mostrarMensaje("No hay activos registrados en el sistema.");
            }
        });
        Color rojoPastel = new Color(255, 153, 153);
        panelBotonesAccion.add(btnUbicacionActivos);
        btnCerrarSesion = new JButton("Cerrar sesión");
        btnCerrarSesion.setBackground(rojoPastel);
        btnCerrarSesion.addActionListener(e -> cerrarSesion());
        panelBotonesAccion.add(btnCerrarSesion);

        panelRegistro.add(panelBotonesAccion);
        panelRegistro.add(Box.createVerticalStrut(20));

        // Panel para mostrar mensajes
        JPanel panelMensajes = new JPanel();
        panelMensajes.setLayout(new BorderLayout());

        // JTextArea para los mensajes
        txtAreaMensajes = new JTextArea(1, 30);
        txtAreaMensajes.setEditable(false);
        txtAreaMensajes.setLineWrap(true);
        txtAreaMensajes.setWrapStyleWord(true);
        txtAreaMensajes.setBackground(new java.awt.Color(245, 245, 245));

        // JScrollPane para permitir el desplazamiento
        JScrollPane scrollPaneMensajes = new JScrollPane(txtAreaMensajes);
        scrollPaneMensajes.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);

        // Título de mensajes
        JPanel panelTituloMensajes = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel lblTituloMensajes = new JLabel("Mensajes");
        panelTituloMensajes.add(lblTituloMensajes);

        panelMensajes.add(panelTituloMensajes, BorderLayout.NORTH);
        panelMensajes.add(scrollPaneMensajes, BorderLayout.CENTER);
        panelMensajes.setAlignmentX(Component.CENTER_ALIGNMENT);

        panelRegistro.add(panelMensajes);

        // Usuario, fecha y versión
        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.CENTER));
        Usuario usuario = Main.usuarioAutenticado;
        JLabel lblUsuario = new JLabel("Usuario: " + usuario.getNombreUsuario());
        panelInfo.add(lblUsuario);
        panelInfo.add(Box.createHorizontalGlue());

        LocalDate fechaActual = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String fechaFormateada = fechaActual.format(formatter);
        JLabel lblFecha = new JLabel("Fecha: " + fechaFormateada);
        panelInfo.add(lblFecha);
        panelInfo.add(Box.createHorizontalGlue());

        JLabel lblVersion = new JLabel("Versión: 4.0.0");
        panelInfo.add(lblVersion);

        panelRegistro.add(panelInfo);
        add(panelRegistro, BorderLayout.WEST);
        add(panelDerecho, BorderLayout.CENTER);
    }

    private void cargarSedes() {
        comboSede.removeAllItems();
        List<Sede> sedes = activoServicio.cargarSedesDesdeBD();
        for (Sede sede : sedes) {
            comboSede.addItem(sede);
        }
    }

    public void recargarDatos() {
        txtBuscar.setText("");
        getFiltroFrame().resetearFiltros(this);

        listaBaseDatos = activoServicio.cargarActivosDesdeBD()
                .stream()
                .filter(activo -> !activo.getEstado().equalsIgnoreCase("Renovado"))
                .collect(Collectors.toList());

        NonEditableTableModel nuevoModel = new NonEditableTableModel();
        nuevoModel.setColumnIdentifiers(new String[]{
                "N°", "Código Patrimonial", "Nombre", "Categoría", "Estado",
                "Fecha de Adquisición", "Vida Útil", "Costo Inicial",
                "Depreciación Mensual", "Depreciación Acumulada", "Valor Residual"
        });

        for (ActivoFijo activo : listaBaseDatos) {
            nuevoModel.addRow(new Object[]{
                    activo.getNumero(),
                    activo.getCodigoPatrimonial(),
                    activo.getNombre(),
                    activo.getCategoria(),
                    activo.getEstado(),
                    activo.getFechaAdquisicion(),
                    activo.getVidaUtil(),
                    String.format("%.2f", activo.getCostoInicial()),
                    String.format("%.2f", activo.calcularDepreciacionMensual()),
                    String.format("%.2f", activo.calcularDepreciacionAcumulada()),
                    String.format("%.2f", activo.calcularValorResidual())
            });
        }

        table.setModel(nuevoModel);
        model = nuevoModel;

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        configurarComparadores(sorter);
        table.setRowSorter(sorter);

        table.revalidate();
        table.repaint();

        actualizarVistaDeTotales();
    }

    private void configurarComparadores(TableRowSorter<TableModel> sorter) {
        sorter.setComparator(0, Comparator.comparingInt(o -> {
            try {
                return Integer.parseInt(o.toString());
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        })); // N°

        sorter.setComparator(1, Comparator.comparing(o -> {
            String[] partes = o.toString().split("-");
            try {
                int anio = Integer.parseInt(partes[0]);
                int consecutivo = partes.length > 1 ? Integer.parseInt(partes[1]) : 0;
                return anio * 10000 + consecutivo;
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        })); // Código Patrimonial

        sorter.setComparator(2, Comparator.comparing(o -> o.toString().toLowerCase())); // Nombre

        sorter.setComparator(3, Comparator.comparing(o -> o.toString().toLowerCase())); // Categoría

        sorter.setComparator(4, Comparator.comparing(o -> o.toString().toLowerCase())); // Estado

        sorter.setComparator(5, Comparator.comparing(o -> {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                return LocalDate.parse(o.toString(), formatter);
            } catch (Exception e) {
                return LocalDate.MIN;
            }
        })); // Fecha de Adquisición

        sorter.setComparator(6, Comparator.comparingInt(o -> {
            try {
                return Integer.parseInt(o.toString());
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        })); // Vida Útil

        sorter.setComparator(7, Comparator.comparingDouble(o -> {
            try {
                return Double.parseDouble(o.toString().replace(",", "").replace("S/", ""));
            } catch (NumberFormatException e) {
                return Double.MAX_VALUE;
            }
        })); // Costo Inicial

        sorter.setComparator(8, Comparator.comparingDouble(o -> {
            try {
                return Double.parseDouble(o.toString().replace(",", "").replace("S/", ""));
            } catch (NumberFormatException e) {
                return Double.MAX_VALUE;
            }
        })); // Depreciación Mensual

        sorter.setComparator(9, Comparator.comparingDouble(o -> {
            try {
                return Double.parseDouble(o.toString().replace(",", "").replace("S/", ""));
            } catch (NumberFormatException e) {
                return Double.MAX_VALUE;
            }
        })); // Depreciación Acumulada

        sorter.setComparator(10, Comparator.comparingDouble(o -> {
            try {
                return Double.parseDouble(o.toString().replace(",", "").replace("S/", ""));
            } catch (NumberFormatException e) {
                return Double.MAX_VALUE;
            }
        })); // Valor Residual
    }

    public void configurarOrdenamiento() {
        table.setAutoCreateRowSorter(true);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());

        sorter.setComparator(0, Comparator.comparingInt(o -> {
            try {
                return Integer.parseInt(o.toString());
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        })); // N°

        sorter.setComparator(1, Comparator.comparing(o -> {
            String[] partes = o.toString().split("-");
            try {
                int anio = Integer.parseInt(partes[0]);
                int consecutivo = partes.length > 1 ? Integer.parseInt(partes[1]) : 0;
                return anio * 10000 + consecutivo;
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        })); // Código Patrimonial

        sorter.setComparator(2, Comparator.comparing(o -> o.toString().toLowerCase())); // Nombre

        sorter.setComparator(3, Comparator.comparing(o -> o.toString().toLowerCase())); // Categoría

        sorter.setComparator(4, Comparator.comparing(o -> o.toString().toLowerCase())); // Estado

        sorter.setComparator(5, Comparator.comparing(o -> {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                return LocalDate.parse(o.toString(), formatter);
            } catch (Exception e) {
                return LocalDate.MIN;
            }
        })); // Fecha de Adquisición

        sorter.setComparator(6, Comparator.comparingInt(o -> {
            try {
                return Integer.parseInt(o.toString());
            } catch (NumberFormatException e) {
                return Integer.MAX_VALUE;
            }
        })); // Vida Útil

        sorter.setComparator(7, Comparator.comparingDouble(o -> {
            try {
                return Double.parseDouble(o.toString().replace(",", "").replace("S/", ""));
            } catch (NumberFormatException e) {
                return Double.MAX_VALUE;
            }
        })); // Costo Inicial

        sorter.setComparator(8, Comparator.comparingDouble(o -> {
            try {
                return Double.parseDouble(o.toString().replace(",", "").replace("S/", ""));
            } catch (NumberFormatException e) {
                return Double.MAX_VALUE;
            }
        })); // Depreciación Mensual

        sorter.setComparator(9, Comparator.comparingDouble(o -> {
            try {
                return Double.parseDouble(o.toString().replace(",", "").replace("S/", ""));
            } catch (NumberFormatException e) {
                return Double.MAX_VALUE;
            }
        })); // Depreciación Acumulada

        sorter.setComparator(10, Comparator.comparingDouble(o -> {
            try {
                return Double.parseDouble(o.toString().replace(",", "").replace("S/", ""));
            } catch (NumberFormatException e) {
                return Double.MAX_VALUE;
            }
        })); // Valor Residual

        table.setRowSorter(sorter);

        table.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int columnIndex = table.columnAtPoint(e.getPoint());
                manejarOrdenamiento(columnIndex);
            }
        });
    }

    private void manejarOrdenamiento(int columnIndex) {
        DefaultRowSorter<?, ?> sorter = (DefaultRowSorter<?, ?>) table.getRowSorter();
        List<? extends RowSorter.SortKey> sortKeys = sorter.getSortKeys();

        // Ciclo: UNSORTED → ASCENDING → DESCENDING → UNSORTED
        if (columnaActualOrdenada != columnIndex) {
            ordenActual = SortOrder.ASCENDING;
        } else {
            switch (ordenActual) {
                case UNSORTED:
                    ordenActual = SortOrder.ASCENDING;
                    break;
                case ASCENDING:
                    ordenActual = SortOrder.DESCENDING;
                    break;
                case DESCENDING:
                    ordenActual = SortOrder.UNSORTED;
                    break;
            }
        }

        columnaActualOrdenada = columnIndex;

        if (ordenActual == SortOrder.UNSORTED) {
            sorter.setSortKeys(null);
        } else {
            sorter.setSortKeys(Collections.singletonList(new RowSorter.SortKey(columnIndex, ordenActual)));
        }
    }

    public FiltroFrame getFiltroFrame() {
        if (filtroFrame == null) {
            filtroFrame = new FiltroFrame(this, filtrosGuardados);
        }
        return filtroFrame;
    }

    private void abrirFiltroFrame() {
        FiltroFrame filtroFrame = new FiltroFrame(this, filtrosGuardados);
        filtroFrame.setVisible(true);
    }

    public void actualizarFiltrosGuardados(Map<String, Object> filtros) {
        this.filtrosGuardados = new HashMap<>(filtros);
    }

    public void filtrarActivosAvanzado(Map<String, Object> criterios) {
        if (criterios.isEmpty()) {
            listaFiltrada = listaBaseDatos.stream()
                    .filter(activo -> !activo.getEstado().equalsIgnoreCase("Renovado"))
                    .collect(Collectors.toList());
        } else {
            listaFiltrada = activoServicio.filtrarActivos(criterios).stream()
                    .filter(activo -> !activo.getEstado().equalsIgnoreCase("Renovado"))
                    .collect(Collectors.toList());
        }
        cargarActivosEnTabla(listaFiltrada);
    }

    public JTable getTable() {
        return table;
    }

    private void filtrarActivos(String textoBusqueda) {
        textoBusqueda = textoBusqueda.toLowerCase();
        NonEditableTableModel modeloFiltrado = new NonEditableTableModel();
        modeloFiltrado.setColumnIdentifiers(new String[]{
                "N°", "Código Patrimonial", "Nombre", "Categoría", "Estado",
                "Fecha de Adquisición", "Vida Útil", "Costo Inicial",
                "Depreciación Mensual", "Depreciación Acumulada", "Valor Residual"
        });

        List<ActivoFijo> baseBusqueda = listaFiltrada != null ? listaFiltrada : listaBaseDatos;

        for (ActivoFijo activo : baseBusqueda.stream()
                .filter(a -> !a.getEstado().equalsIgnoreCase("Renovado"))
                .collect(Collectors.toList())) {

            if (activo.getNombre().toLowerCase().contains(textoBusqueda) ||
                    activo.getCodigoPatrimonial().toLowerCase().contains(textoBusqueda)) {

                modeloFiltrado.addRow(new Object[]{
                        activo.getNumero(),
                        activo.getCodigoPatrimonial(),
                        activo.getNombre(),
                        activo.getCategoria(),
                        activo.getEstado(),
                        activo.getFechaAdquisicion(),
                        activo.getVidaUtil(),
                        String.format("%.2f", activo.getCostoInicial()),
                        String.format("%.2f", activo.calcularDepreciacionMensual()),
                        String.format("%.2f", activo.calcularDepreciacionAcumulada()),
                        String.format("%.2f", activo.calcularValorResidual())
                });
            }
        }

        table.setModel(modeloFiltrado);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        configurarComparadores(sorter);
        table.setRowSorter(sorter);
    }

    private void cargarActivosEnTabla(List<ActivoFijo> activos) {
        NonEditableTableModel modelo = new NonEditableTableModel();
        modelo.setColumnIdentifiers(new String[]{
                "ID", "Código Patrimonial", "Nombre", "Categoría", "Estado",
                "Fecha de Adquisición", "Vida Útil", "Costo Inicial",
                "Depreciación Mensual", "Depreciación Acumulada", "Valor Residual"
        });

        totalCostoInicial = 0.0;
        totalDepreciacionMensual = 0.0;
        totalDepreciacionAcumulada = 0.0;
        totalValorResidual = 0.0;
        numeroDeActivos = 0;

        List<ActivoFijo> activosFiltrados = activos.stream()
                .filter(activo -> !activo.getEstado().equalsIgnoreCase("Renovado"))
                .collect(Collectors.toList());

        for (ActivoFijo activo : activosFiltrados) {
            double depreciacionMensual = activo.calcularDepreciacionMensual();
            double depreciacionAcumulada = activo.calcularDepreciacionAcumulada();
            double valorResidual = activo.calcularValorResidual();

            modelo.addRow(new Object[]{
                    activo.getNumero(),
                    activo.getCodigoPatrimonial(),
                    activo.getNombre(),
                    activo.getCategoria(),
                    activo.getEstado(),
                    activo.getFechaAdquisicion(),
                    activo.getVidaUtil(),
                    String.format("%.2f", activo.getCostoInicial()),
                    String.format("%.2f", depreciacionMensual),
                    String.format("%.2f", depreciacionAcumulada),
                    String.format("%.2f", valorResidual)
            });

            totalCostoInicial += activo.getCostoInicial();
            totalDepreciacionMensual += depreciacionMensual;
            totalDepreciacionAcumulada += depreciacionAcumulada;
            totalValorResidual += valorResidual;
            numeroDeActivos++;
        }

        table.setModel(modelo);
        actualizarVistaDeTotales();
    }

    private void seleccionarPrimeraCoincidencia(String textoBusqueda) {
        textoBusqueda = textoBusqueda.toLowerCase();
        for (int i = 0; i < table.getRowCount(); i++) {
            int idActivo = (int) table.getValueAt(i, 0);
            String nombre = table.getValueAt(i, 2).toString().toLowerCase();

            if (String.valueOf(idActivo).contains(textoBusqueda) || nombre.contains(textoBusqueda)) {
                table.setRowSelectionInterval(i, i);
                table.scrollRectToVisible(table.getCellRect(i, 0, true));
                return;
            }
        }

        mostrarMensaje("No se encontraron coincidencias para: " + textoBusqueda);
    }

    public int obtenerIndicePorNumero(int numeroActivo) {
        for (int i = 0; i < listaBaseDatos.size(); i++) {
            if (listaBaseDatos.get(i).getNumero() == numeroActivo) {
                return i;
            }
        }
        return -1;
    }

    public void cargarActivosEnTabla() {
        model.setRowCount(0);

        totalCostoInicial = 0.0;
        totalDepreciacionMensual = 0.0;
        totalDepreciacionAcumulada = 0.0;
        totalValorResidual = 0.0;

        List<ActivoFijo> activosFiltrados = listaBaseDatos.stream()
                .filter(a -> !a.getEstado().equalsIgnoreCase("Renovado"))
                .collect(Collectors.toList());

        for (ActivoFijo activo : activosFiltrados) {
            double depreciacionMensual = activo.calcularDepreciacionMensual();
            double depreciacionAcumulada = activo.calcularDepreciacionAcumulada();
            double valorResidual = activo.calcularValorResidual();

            model.addRow(new Object[]{
                    activo.getNumero(),
                    activo.getCodigoPatrimonial(),
                    activo.getNombre(),
                    activo.getCategoria(),
                    activo.getEstado(),
                    activo.getFechaAdquisicion(),
                    activo.getVidaUtil(),
                    String.format("%.2f", activo.getCostoInicial()),
                    String.format("%.2f", depreciacionMensual),
                    String.format("%.2f", depreciacionAcumulada),
                    String.format("%.2f", valorResidual)
            });

            totalCostoInicial += activo.getCostoInicial();
            totalDepreciacionMensual += depreciacionMensual;
            totalDepreciacionAcumulada += depreciacionAcumulada;
            totalValorResidual += valorResidual;
        }

        numeroDeActivos = activosFiltrados.size();
        actualizarVistaDeTotales();
    }

    private void agregarActivo() {
        String nombre = txtNombre.getText();

        Date fechaAdquisicion = dateChooserFechaAdquisicion.getDate();
        if (fechaAdquisicion == null) {
            mostrarMensaje("Por favor seleccione una fecha de adquisición.");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        String fechaAdquisicionStr;
        try {
            fechaAdquisicionStr = dateFormat.format(fechaAdquisicion);
        } catch (Exception e) {
            mostrarMensaje("Error al formatear la fecha: " + e.getMessage());
            return;
        }

        String errorNombre = ValidadorActivo.validarNombre(nombre);
        if (errorNombre != null) {
            mostrarMensaje(errorNombre);
            return;
        }

        String errorCategoria = ValidadorActivo.validarCategoria(comboCategoria);
        if (errorCategoria != null) {
            mostrarMensaje(errorCategoria);
            return;
        }

        String errorFecha = ValidadorActivo.validarFechaAdquisicion(dateChooserFechaAdquisicion);
        if (errorFecha != null) {
            mostrarMensaje(errorFecha);
            return;
        }

        Sede sedeSeleccionada = (Sede) comboSede.getSelectedItem();
        if (sedeSeleccionada == null) {
            mostrarMensaje("Por favor seleccione una sede.");
            return;
        }

        try {
            double costoInicial = limpiarFormatoCostoInicial(txtCostoInicial.getText());
            if (costoInicial == -1) return;

            String categoria = (String) comboCategoria.getSelectedItem();

            ActivoFijo nuevoActivo = new ActivoFijo(
                    nombre, categoria, costoInicial, fechaAdquisicionStr, "Alta",
                    sedeSeleccionada
            );

            double depreciacionMensual = nuevoActivo.calcularDepreciacionMensual();
            double depreciacionAcumulada = nuevoActivo.calcularDepreciacionAcumulada();
            double valorResidual = nuevoActivo.calcularValorResidual();

            GuardarActivoResultado resultado = activoServicio.guardarActivoEnBD(
                    nombre, categoria, nuevoActivo.getEstado(), fechaAdquisicionStr,
                    determinarVidaUtil(categoria), costoInicial, depreciacionMensual,
                    depreciacionAcumulada, valorResidual, sedeSeleccionada.getId()
            );

            if (resultado == null) {
                mostrarMensaje("Error al registrar el activo en la base de datos.");
                return;
            }

            nuevoActivo.setCodigoPatrimonial(resultado.getCodigoPatrimonial());
            nuevoActivo.setId(resultado.getId());
            nuevoActivo.setNumero(listaBaseDatos.size() + 1);

            listaBaseDatos.add(nuevoActivo);
            model.addRow(new Object[]{
                    resultado.getId(),
                    resultado.getCodigoPatrimonial(),
                    nombre,
                    categoria,
                    nuevoActivo.getEstado(),
                    fechaAdquisicionStr,
                    nuevoActivo.getVidaUtil(),
                    String.format("%.2f", costoInicial),
                    String.format("%.2f", depreciacionMensual),
                    String.format("%.2f", depreciacionAcumulada),
                    String.format("%.2f", valorResidual)
            });

            totalCostoInicial += costoInicial;
            totalDepreciacionMensual += depreciacionMensual;
            totalDepreciacionAcumulada += depreciacionAcumulada;
            totalValorResidual += valorResidual;
            numeroDeActivos++;

            activoServicio.registrarActivo(resultado.getId());
            mostrarMensaje("Activo registrado correctamente.");
            getFiltroFrame().resetearFiltros(this);
            txtBuscar.setText("");
            actualizarVistaDeTotales();
            limpiarCampos();
        } catch (Exception e) {
            mostrarMensaje("Error al registrar el activo: " + e.getMessage());
        }
    }

    private void modificarActivo() {
        if (!enModoEdicion) {
            int filaVista = table.getSelectedRow();
            if (filaVista == -1) {
                mostrarMensaje("Por favor seleccione un activo para modificar.");
                return;
            }

            int numeroActivo = (int) table.getValueAt(filaVista, 0);
            int indiceModelo = listaBaseDatos.stream()
                    .filter(a -> a.getNumero() == numeroActivo)
                    .findFirst()
                    .map(listaBaseDatos::indexOf)
                    .orElse(-1);

            if (indiceModelo == -1) {
                mostrarMensaje("No se encontró el activo en el modelo de datos.");
                return;
            }

            ActivoFijo activoSeleccionado = listaBaseDatos.get(indiceModelo);
            if (activoSeleccionado.getEstado().equals("Depreciado") || activoSeleccionado.getEstado().equals("Baja") || activoSeleccionado.getEstado().equals("Baja por venta")) {
                mostrarMensaje("No se puede modificar un activo que está en estado 'Depreciado' o 'Baja'.");
                return;
            }

            nombreOriginal = activoSeleccionado.getNombre();
            categoriaOriginal = activoSeleccionado.getCategoria();

            txtNombre.setText(activoSeleccionado.getNombre());
            comboCategoria.setSelectedItem(activoSeleccionado.getCategoria());

            dateChooserFechaAdquisicion.setEnabled(false);
            txtCostoInicial.setEnabled(false);
            comboSede.setEnabled(false);

            enModoEdicion = true;
            indiceActivoEnEdicion = indiceModelo;
            btnModificar.setText("Guardar Cambios");

            btnRegistrar.setEnabled(false);
            btnDarBaja.setEnabled(false);
            btnVender.setEnabled(false);
            comboTipoBaja.setEnabled(false);
            btnCambios.setEnabled(false);
            btnProyectarDepreciacion.setEnabled(false);
            btnInventario.setEnabled(false);
            txtBuscar.setEnabled(false);
            btnBuscar.setEnabled(false);
            btnExportar.setEnabled(false);
            btnCerrarSesion.setEnabled(false);
            btnResetearBusqueda.setEnabled(false);
            btnRecargar.setEnabled(false);
            btnRevaluar.setEnabled(false);
            btnRenovar.setEnabled(false);
            btnFiltrar.setEnabled(false);
            btnAnalisisDeBajas.setEnabled(false);
            btnUbicacionActivos.setEnabled(false);

        } else {
            String nuevoNombre = txtNombre.getText();
            String nuevaCategoria = (String) comboCategoria.getSelectedItem();

            String errorNombre = ValidadorActivo.validarNombre(nuevoNombre);
            if (errorNombre != null) {
                mostrarMensaje(errorNombre);
                return;
            }

            String errorCategoria = ValidadorActivo.validarCategoria(comboCategoria);
            if (errorCategoria != null) {
                mostrarMensaje(errorCategoria);
                return;
            }

            boolean categoriaCambiada = !categoriaOriginal.equals(nuevaCategoria);
            if (!categoriaCambiada && nombreOriginal.equals(nuevoNombre)) {
                salirModoEdicion();
                mostrarMensaje("No se realizaron cambios. El activo se mantiene igual.");
            } else {
                int numeroActivo = (int) table.getValueAt(table.getSelectedRow(), 0);
                ActivoFijo activoSeleccionado = listaBaseDatos.stream()
                        .filter(a -> a.getNumero() == numeroActivo)
                        .findFirst()
                        .orElse(null);

                if (activoSeleccionado == null) {
                    mostrarMensaje("Error: No se encontró el activo en la base de datos.");
                    return;
                }

                activoSeleccionado.setNombre(nuevoNombre);
                activoSeleccionado.setCategoria(nuevaCategoria);

                if (categoriaCambiada) {
                    activoSeleccionado.setVidaUtil(nuevaCategoria);
                    double nuevaDepreciacionMensual = activoSeleccionado.calcularDepreciacionMensual();
                    double nuevaDepreciacionAcumulada = activoSeleccionado.calcularDepreciacionAcumulada();
                    double nuevoValorResidual = activoSeleccionado.calcularValorResidual();

                    // Actualizar totales
                    totalCostoInicial -= activoSeleccionado.getCostoInicial();
                    totalDepreciacionMensual -= activoSeleccionado.calcularDepreciacionMensual();
                    totalDepreciacionAcumulada -= activoSeleccionado.calcularDepreciacionAcumulada();
                    totalValorResidual -= activoSeleccionado.calcularValorResidual();

                    totalDepreciacionMensual += nuevaDepreciacionMensual;
                    totalDepreciacionAcumulada += nuevaDepreciacionAcumulada;
                    totalValorResidual += nuevoValorResidual;
                }

                activoServicio.modificarActivoEnBD(
                        activoSeleccionado.getNumero(),
                        nuevoNombre,
                        nuevaCategoria,
                        activoSeleccionado.getEstado(),
                        activoSeleccionado.getFechaAdquisicion(),
                        activoSeleccionado.getVidaUtil(),
                        activoSeleccionado.getCostoInicial(),
                        activoSeleccionado.calcularDepreciacionMensual(),
                        activoSeleccionado.calcularDepreciacionAcumulada(),
                        activoSeleccionado.calcularValorResidual()
                );

                int filaVista = table.getSelectedRow();
                model.setValueAt(activoSeleccionado.getCodigoPatrimonial(), filaVista, 1);
                model.setValueAt(nuevoNombre, filaVista, 2);
                model.setValueAt(nuevaCategoria, filaVista, 3);
                model.setValueAt(activoSeleccionado.getEstado(), filaVista, 4);
                model.setValueAt(activoSeleccionado.getVidaUtil(), filaVista, 6);
                model.setValueAt(String.format("%.2f", activoSeleccionado.getCostoInicial()), filaVista, 7);
                model.setValueAt(String.format("%.2f", activoSeleccionado.calcularDepreciacionMensual()), filaVista, 8);
                model.setValueAt(String.format("%.2f", activoSeleccionado.calcularDepreciacionAcumulada()), filaVista, 9);
                model.setValueAt(String.format("%.2f", activoSeleccionado.calcularValorResidual()), filaVista, 10);

                salirModoEdicion();
                String detallesModificacion = "\n"
                        + (!nombreOriginal.equals(nuevoNombre) ? "Nombre: '" + nombreOriginal + "' -> '" + nuevoNombre + "'\n" : "")
                        + (!categoriaOriginal.equals(nuevaCategoria) ? "Categoría: '" + categoriaOriginal + "' -> '" + nuevaCategoria + "'\n" : "");
                activoServicio.modificarActivo(activoSeleccionado.getNumero(), detallesModificacion);
                mostrarMensaje("Activo modificado correctamente.");
                getFiltroFrame().resetearFiltros(this);
                txtBuscar.setText("");
                recargarDatos();
            }
        }
    }

    private void salirModoEdicion() {
        enModoEdicion = false;
        indiceActivoEnEdicion = -1;
        btnModificar.setText("Modificar");

        btnRegistrar.setEnabled(true);
        btnDarBaja.setEnabled(true);
        btnVender.setEnabled(true);
        comboTipoBaja.setEnabled(true);
        btnCambios.setEnabled(true);
        btnProyectarDepreciacion.setEnabled(true);
        btnInventario.setEnabled(true);
        txtBuscar.setEnabled(true);
        btnBuscar.setEnabled(true);
        btnExportar.setEnabled(true);
        btnCerrarSesion.setEnabled(true);
        btnResetearBusqueda.setEnabled(true);
        btnRecargar.setEnabled(true);
        btnRevaluar.setEnabled(true);
        btnRenovar.setEnabled(true);
        btnFiltrar.setEnabled(true);
        btnAnalisisDeBajas.setEnabled(true);
        btnUbicacionActivos.setEnabled(true);

        dateChooserFechaAdquisicion.setEnabled(true);
        txtCostoInicial.setEnabled(true);
        comboSede.setEnabled(true);

        limpiarCampos();
    }

    private void darDeBajaActivo() {
        int filaVista = table.getSelectedRow();
        if (filaVista == -1) {
            mostrarMensaje("Seleccione un activo para dar de baja.");
            return;
        }

        int numeroActivo = (int) table.getValueAt(filaVista, 0);

        ActivoFijo activoSeleccionado = listaBaseDatos.stream()
                .filter(a -> a.getNumero() == numeroActivo)
                .findFirst()
                .orElse(null);

        if (activoSeleccionado == null) {
            mostrarMensaje("Error: No se encontró el activo en la base de datos.");
            return;
        }

        String tipoBaja = (String) comboTipoBaja.getSelectedItem();

        if (tipoBaja == null || tipoBaja.isEmpty()) {
            mostrarMensaje("Seleccione un tipo de baja.");
            return;
        }

        if (tipoBaja.equals("Fin de vida útil") && !activoSeleccionado.getEstado().equals("Depreciado")) {
            mostrarMensaje("El activo no está depreciado y no puede darse de baja por 'Fin de vida útil'.");
            return;
        }

        if (activoSeleccionado.getEstado().equals("Depreciado") ||
                activoSeleccionado.getEstado().equals("Baja") ||
                activoSeleccionado.getEstado().equals("Baja por venta")) {
            mostrarMensaje("No se puede dar de baja un activo que está en estado 'Depreciado' o 'Baja'.");
            return;
        }

        String motivoBaja;
        do {
            motivoBaja = JOptionPane.showInputDialog(this, "Ingrese el motivo de la baja:");
            if (motivoBaja == null) {
                return;
            }
            motivoBaja = motivoBaja.trim();
            if (motivoBaja.isEmpty()) {
                JOptionPane.showMessageDialog(this, "El motivo de baja no puede estar vacío.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            }
        } while (motivoBaja.isEmpty());

        activoSeleccionado.darDeBaja(motivoBaja, tipoBaja);
        activoServicio.actualizarEstadoActivo(numeroActivo, "Baja");
        activoServicio.darDeBaja(numeroActivo, motivoBaja, tipoBaja);

        model.setValueAt("Baja", filaVista, 4); // Columna 4: Estado
        mostrarMensaje("Activo dado de baja como: " + tipoBaja);

        getFiltroFrame().resetearFiltros(this);
        txtBuscar.setText("");
        actualizarVistaDeTotales();
        limpiarCampos();
    }

    private void venderActivo() {
        int filaVista = table.getSelectedRow();
        if (filaVista == -1) {
            mostrarMensaje("Debe seleccionar un activo para vender.");
            return;
        }

        int numeroActivo = (int) table.getValueAt(filaVista, 0);

        ActivoFijo activoSeleccionado = listaBaseDatos.stream()
                .filter(a -> a.getNumero() == numeroActivo)
                .findFirst()
                .orElse(null);

        if (activoSeleccionado == null) {
            mostrarMensaje("Error: No se encontró el activo en la base de datos.");
            return;
        }

        if (activoSeleccionado.getEstado().equalsIgnoreCase("Baja") ||
                activoSeleccionado.getEstado().equalsIgnoreCase("Baja por venta")) {
            mostrarMensaje("No se puede vender un activo que ya está en estado de baja.");
            return;
        }

        double valorResidual = activoSeleccionado.calcularValorResidual();
        if (valorResidual <= 0) {
            mostrarMensaje("No se puede vender un activo con valor residual 0.");
            return;
        }

        String nombre = activoSeleccionado.getNombre();
        String categoria = activoSeleccionado.getCategoria();
        String fechaAdquisicion = activoSeleccionado.getFechaAdquisicion();
        double costoInicial = activoSeleccionado.getCostoInicial();

        VentaFrame ventaFrame = new VentaFrame(this, numeroActivo, nombre, categoria, fechaAdquisicion, costoInicial, valorResidual);
        ventaFrame.setVisible(true);
    }

    private void activarBoton() {
        if (enModoEdicion) {
            btnModificar.doClick();
        } else {
            btnRegistrar.doClick();
        }
    }

    private double limpiarFormatoCostoInicial(String costoInicialStr) {
        String valorLimpio = costoInicialStr.replaceAll("[^\\d.]", "");

        int ultimoPunto = valorLimpio.lastIndexOf('.');
        if (ultimoPunto != -1) {
            valorLimpio = valorLimpio.substring(0, ultimoPunto).replace(".", "") + valorLimpio.substring(ultimoPunto);
        }

        try {
            double costoInicial = Double.parseDouble(valorLimpio);

            if (costoInicial <= 0) {
                mostrarMensaje("El costo inicial debe ser un número mayor a 0.");
                return -1;
            }

            return costoInicial;
        } catch (NumberFormatException e) {
            mostrarMensaje("Formato incorrecto para el costo inicial.");
            return -1;
        }
    }

    private void agregarCampo(JPanel panel, GridBagConstraints gbc, int fila, String label, Component componente) {
        gbc.gridx = 0;
        gbc.gridy = fila;
        panel.add(new JLabel(label), gbc);

        gbc.gridx = 1;
        panel.add(componente, gbc);
    }

    private void limpiarCampos() {
        txtNombre.setText("");
        comboCategoria.setSelectedIndex(0);
        comboSede.setSelectedIndex(0);
        dateChooserFechaAdquisicion.setDate(null);
        txtVidaUtil.setText("");
        txtCostoInicial.setText("");
    }

    public void actualizarEstadoActivoEnTabla(int idActivo, String nuevoEstado) {
        for (int i = 0; i < model.getRowCount(); i++) {
            if ((int) model.getValueAt(i, 0) == idActivo) {
                model.setValueAt(nuevoEstado, i, 4);
                break;
            }
        }
    }

    public boolean tablaEstaVacia() {
        return table.getRowCount() == 0;
    }

    public static String formatearNumero(double numero) {
        if (numero >= 1_000_000_000_000.0) { // Billones
            return String.format("%.2f T", numero / 1_000_000_000_000.0);
        } else if (numero >= 1_000_000_000) { // Miles de millones
            return String.format("%.2f B", numero / 1_000_000_000);
        } else if (numero >= 1_000_000) { // Millones
            return String.format("%.2f M", numero / 1_000_000);
        } else if (numero >= 1_000) { // Miles
            return String.format("%.2f K", numero / 1_000);
        } else {
            return String.format("%.2f", numero);
        }
    }

    public void actualizarVistaDeTotales() {
        panelTotales.removeAll();
        numeroDeActivos = model.getRowCount();
        panelTotales.add(new JLabel("Número de Activos: " + numeroDeActivos + " | "));
        panelTotales.add(new JLabel("Total de Costo Inicial: S/" + formatearNumero(totalCostoInicial) + " | "));
        panelTotales.add(new JLabel("Total de Depreciación Mensual: S/" + formatearNumero(totalDepreciacionMensual) + " | "));
        panelTotales.add(new JLabel("Total de Depreciación Acumulada: S/" + formatearNumero(totalDepreciacionAcumulada) + " | "));
        panelTotales.add(new JLabel("Total de Valor Residual: S/" + formatearNumero(totalValorResidual)));
        panelTotales.revalidate();
        panelTotales.repaint();
    }

    public void mostrarMensaje(String mensaje) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        String horaActual = sdf.format(new Date());

        txtAreaMensajes.append("[" + horaActual + "] " + mensaje + "\n");

        txtAreaMensajes.setCaretPosition(txtAreaMensajes.getDocument().getLength());
    }

    private void verHistorial() {
        List<String> detallesMovimientos = activoServicio.cargarHistorialMovimientos();

        if (detallesMovimientos.isEmpty()) {
            mostrarMensaje("No hay movimientos registrados.");
            return;
        }

        StringBuilder historialTexto = new StringBuilder();
        for (String detalle : detallesMovimientos) {
            historialTexto.insert(0, detalle + "\n");
        }

        String historialFinal = historialTexto.toString().replaceAll("\\n+$", "");

        JTextArea areaTexto = new JTextArea(historialFinal, 20, 50);
        areaTexto.setLineWrap(true);
        areaTexto.setWrapStyleWord(true);
        areaTexto.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(areaTexto);

        areaTexto.setCaretPosition(areaTexto.getDocument().getLength());

        JOptionPane.showMessageDialog(null, scrollPane, "Historial de cambios", JOptionPane.INFORMATION_MESSAGE);
    }

    private void exportarReporte() {
        String nombreArchivo = "reporte_valorium.xlsx";

        if (table.getRowCount() == 0) {
            mostrarMensaje("No hay activos disponibles en la tabla para exportar.");
            return;
        }

        try {
            Workbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Activos");

            // Estilo para el encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Estilo para los datos
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.RIGHT);

            // Fila de encabezado
            Row headerRow = sheet.createRow(0);
            String[] columnas = {"N°", "Código Patrimonial", "Nombre", "Categoría", "Estado", "Fecha de Adquisición",
                    "Vida Útil (años)", "Costo Inicial", "Depreciación Mensual", "Depreciación Acumulada",
                    "Valor Residual"};
            for (int i = 0; i < columnas.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnas[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;

            for (int i = 0; i < table.getRowCount(); i++) {
                Row row = sheet.createRow(rowNum++);

                for (int j = 0; j < table.getColumnCount(); j++) {
                    Object valorCelda = table.getValueAt(i, j);
                    Cell cell = row.createCell(j);

                    if (valorCelda instanceof Number) {
                        cell.setCellValue(((Number) valorCelda).doubleValue());
                    } else if (valorCelda != null) {
                        cell.setCellValue(valorCelda.toString());
                    }
                    cell.setCellStyle(dataStyle);
                }
            }

            for (int i = 0; i < columnas.length; i++) {
                sheet.autoSizeColumn(i);
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar reporte");
            fileChooser.setSelectedFile(new File(nombreArchivo));
            int userSelection = fileChooser.showSaveDialog(this);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File archivoSeleccionado = fileChooser.getSelectedFile();

                try (FileOutputStream fileOut = new FileOutputStream(archivoSeleccionado)) {
                    workbook.write(fileOut);
                    JOptionPane.showMessageDialog(this, "Reporte exportado correctamente a: " + archivoSeleccionado.getAbsolutePath());
                    mostrarMensaje("Reporte exportado correctamente.");
                }
            }
            workbook.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error al exportar el reporte: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            mostrarMensaje("Error al exportar el reporte.");
        }
    }

    private void proyectarDepreciacion() {
        int filaVista = table.getSelectedRow();
        if (filaVista == -1) {
            mostrarMensaje("Por favor seleccione un activo para proyectar la depreciación.");
            return;
        }

        int numeroActivo = (int) table.getValueAt(filaVista, 0);
        int indiceModelo = obtenerIndicePorNumero(numeroActivo);
        if (indiceModelo == -1) {
            mostrarMensaje("No se encontró el activo en el modelo de datos.");
            return;
        }

        ActivoFijo activoSeleccionado = listaBaseDatos.get(indiceModelo);

        if ("Terreno".equalsIgnoreCase(activoSeleccionado.getCategoria()) || activoSeleccionado.getVidaUtil() == 0) {
            mostrarMensaje("No se puede proyectar la depreciación de un terreno.");
            return;
        }

        int vidaUtil = activoSeleccionado.getVidaUtil();
        double depreciacionMensual = activoSeleccionado.calcularDepreciacionMensual();

        LocalDate fechaInicio = LocalDate.parse(activoSeleccionado.getFechaAdquisicion(), DateTimeFormatter.ofPattern("dd-MM-yyyy")).plusMonths(1);
        LocalDate fechaFin = fechaInicio.plusYears(vidaUtil);

        String[] columnNames = {"Mes", "Año", "Depreciación Mensual", "Depreciación Acumulada", "Valor en Libros"};
        Object[][] data = new Object[vidaUtil * 12][5];

        double depreciacionAcumulada = 0;
        double valorEnLibros = activoSeleccionado.getCostoInicial();

        for (int año = 0; año < vidaUtil; año++) {
            for (int mes = 0; mes < 12; mes++) {
                LocalDate fechaActual = fechaInicio.plusMonths(año * 12 + mes);

                if (fechaActual.isBefore(fechaFin) || fechaActual.isEqual(fechaFin)) {
                    String mesNombre = fechaActual.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
                    data[año * 12 + mes][0] = mesNombre;
                    data[año * 12 + mes][1] = fechaActual.getYear();
                    data[año * 12 + mes][2] = String.format("S/ %.2f", depreciacionMensual);

                    depreciacionAcumulada += depreciacionMensual;
                    data[año * 12 + mes][3] = String.format("S/ %.2f", depreciacionAcumulada);

                    valorEnLibros -= depreciacionMensual;
                    data[año * 12 + mes][4] = String.format("S/ %.2f", Math.max(0, valorEnLibros));
                }
            }
        }

        JTable tableProyeccion = new JTable(data, columnNames) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tableProyeccion.getTableHeader().setReorderingAllowed(false);
        tableProyeccion.setFillsViewportHeight(true);
        tableProyeccion.setPreferredScrollableViewportSize(new Dimension(800, 400));

        JScrollPane scrollPane = new JScrollPane(tableProyeccion);

        JButton btnExportar = new JButton("Exportar a Excel");
        btnExportar.addActionListener(e -> exportarProyeccionAExcel(activoSeleccionado, data, columnNames));

        JDialog dialog = new JDialog((Frame) null, "Proyección de Depreciación para '" + activoSeleccionado.getNombre() + "'", true);
        dialog.setLayout(new BorderLayout());
        dialog.add(scrollPane, BorderLayout.CENTER);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelBotones.add(btnExportar);
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dialog.dispose());
        panelBotones.add(btnCerrar);

        dialog.add(panelBotones, BorderLayout.SOUTH);

        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
    }

    private void exportarProyeccionAExcel(ActivoFijo activo, Object[][] data, String[] columnNames) {
        String nombreArchivo = activo.getNombre() + "_Proyeccion_Depreciacion.xlsx";

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Proyección");

            // Estilos de encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < columnNames.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columnNames[i]);
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < data.length; i++) {
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < data[i].length; j++) {
                    Cell cell = row.createCell(j);
                    if (data[i][j] instanceof String) {
                        cell.setCellValue((String) data[i][j]);
                    } else if (data[i][j] instanceof Double) {
                        cell.setCellValue((Double) data[i][j]);
                    } else if (data[i][j] instanceof Integer) {
                        cell.setCellValue((Integer) data[i][j]);
                    }
                }
            }

            for (int i = 0; i < columnNames.length; i++) {
                sheet.autoSizeColumn(i);
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Guardar reporte");
            fileChooser.setSelectedFile(new File(nombreArchivo));
            int userSelection = fileChooser.showSaveDialog(null);

            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File archivoSeleccionado = fileChooser.getSelectedFile();
                try (FileOutputStream fileOut = new FileOutputStream(archivoSeleccionado)) {
                    workbook.write(fileOut);
                    JOptionPane.showMessageDialog(null, "Proyección exportada correctamente a: " + archivoSeleccionado.getAbsolutePath());
                }
            }
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, "Error al exportar la proyección: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void mostrarNotificacionEmergente(String mensaje, int idNotificacion) {
        String titulo = "Notificación de Activo";

        notificacionFrame.mostrarNotificacion(
                titulo,
                mensaje,
                idNotificacion,
                () -> {
                    actualizarEstadoNotificacion(idNotificacion, "Listo");
                },
                () -> {
                    actualizarEstadoNotificacion(idNotificacion, "Recordar");
                }
        );
    }

    private void actualizarEstadoNotificacion(int idNotificacion, String nuevoEstado) {
        String sql = "UPDATE notificaciones SET estado = ?, fecha_creacion = CURRENT_TIMESTAMP WHERE id = ?";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, nuevoEstado);
            pstmt.setInt(2, idNotificacion);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void mostrarNotificacionesPendientes() {
        String sql = "SELECT id, mensaje FROM notificaciones WHERE estado IN ('Pendiente', 'Recordar')";

        try (Connection conn = ConexionBD.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                int idNotificacion = rs.getInt("id");
                String mensaje = rs.getString("mensaje");
                mostrarNotificacionEmergente(mensaje, idNotificacion);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    private void cerrarSesion() {
        this.dispose();

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }
}