package inventario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;

public class ProductoTerminadoGUI extends JDialog {
    private DefaultTableModel tableModel;
    private JTable table;
    private JComboBox<String> comboModo;
    private FiltroPanel filtroProductos;

    public ProductoTerminadoGUI(JFrame parent) {
        super(parent, "Productos Terminados", true);
        setSize(800, 500);
        setLocationRelativeTo(parent);
        initUI();
        cargarDatos("Actuales");
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(ThemeUtil.COLOR_FONDO);

        // Panel superior con controles
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(ThemeUtil.COLOR_FONDO);
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Título
        JPanel panelTitulo = ThemeUtil.crearPanelTitulo("Productos Terminados");
        topPanel.add(panelTitulo, BorderLayout.NORTH);

        // Controles de filtro y modo
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        controles.setBackground(ThemeUtil.COLOR_FONDO);
        
        JLabel lblVer = new JLabel("Ver:");
        lblVer.setFont(ThemeUtil.FUENTE_NORMAL);
        comboModo = new JComboBox<>(new String[]{"Actuales", "Historial"});
        comboModo.setFont(ThemeUtil.FUENTE_NORMAL);
        comboModo.addActionListener(e -> cargarDatos(comboModo.getSelectedItem().toString()));
        
        controles.add(lblVer);
        controles.add(comboModo);
        
        topPanel.add(controles, BorderLayout.CENTER);

        // Filtro de productos
        filtroProductos = new FiltroPanel("Buscar productos por nombre...", this::aplicarFiltro);
        topPanel.add(filtroProductos, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.NORTH);

        // Tabla de productos
        tableModel = new DefaultTableModel(new String[]{"Nombre", "Cantidad", "Fecha Producción"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel);
        ThemeUtil.aplicarEstiloTabla(table);
        
        // Configurar filtrado
        FiltradorUtil.configurarFiltroColumnas(table, new String[]{"Nombre", "Cantidad", "Fecha Producción"});

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(ThemeUtil.COLOR_FONDO);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        JButton btnExport = new JButton("📤 Exportar CSV");
        ThemeUtil.aplicarEstiloSecundario(btnExport);
        
        JButton btnCerrar = new JButton("❌ Cerrar");
        ThemeUtil.aplicarEstiloSecundario(btnCerrar);
        
        bottom.add(btnExport);
        bottom.add(btnCerrar);
        
        add(bottom, BorderLayout.SOUTH);

        btnExport.addActionListener(e -> exportarCSV());
        btnCerrar.addActionListener(e -> dispose());
    }

    /** Carga los productos desde DataStore */
    private void cargarDatos(String modo) {
        tableModel.setRowCount(0);
        
        if (modo.equals("Actuales")) {
            // Cargar productos actuales con stock > 0
            for (ProductoTerminado p : DataStore.productos) {
                if (p.getCantidad() > 0) {
                    Vector<Object> fila = new Vector<>();
                    fila.add(p.getNombre());
                    fila.add(p.getCantidad());
                    fila.add(p.getFechaProduccion());
                    tableModel.addRow(fila);
                }
            }
        } else {
            // Cargar historial (todos los productos)
            for (ProductoTerminado p : DataStore.productos) {
                Vector<Object> fila = new Vector<>();
                fila.add(p.getNombre());
                fila.add(p.getCantidad());
                fila.add(p.getFechaProduccion());
                tableModel.addRow(fila);
            }
        }
        
        aplicarFiltro();
        actualizarContador();
    }

    /** Aplica filtro a la tabla */
    private void aplicarFiltro() {
        String filtro = filtroProductos.getTextoFiltro();
        FiltradorUtil.aplicarFiltroTabla(table, filtro);
        actualizarContador();
    }

    /** Actualiza contador de resultados */
    private void actualizarContador() {
        int total = DataStore.productos.size();
        int filtrados = filtroProductos.getTextoFiltro().isEmpty() ? 
            total : table.getRowCount();
        filtroProductos.actualizarContador(total, filtrados);
    }

    /** Exporta los datos actuales de la tabla a un archivo CSV */
    private void exportarCSV() {
        try (FileWriter fw = new FileWriter("data/productos_terminados_export.csv");
             PrintWriter pw = new PrintWriter(fw)) {

            pw.println("Nombre,Cantidad,FechaProduccion");
            for (int i = 0; i < tableModel.getRowCount(); i++) {
                pw.printf("\"%s\",%s,\"%s\"\n",
                        tableModel.getValueAt(i, 0),
                        tableModel.getValueAt(i, 1),
                        tableModel.getValueAt(i, 2));
            }

            ConfirmDialogUtil.mostrarExito(this, "Exportación Exitosa", 
                "Los datos se han exportado correctamente a: data/productos_terminados_export.csv");

        } catch (Exception ex) {
            ConfirmDialogUtil.mostrarError(this, "Error en Exportación", 
                "Error al exportar datos: " + ex.getMessage());
        }
    }
}