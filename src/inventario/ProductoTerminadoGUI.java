package inventario;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ProductoTerminadoGUI extends JDialog {
    private JTabbedPane tabsProductos;
    
    // Pestaña Actuales
    private DefaultTableModel modelActuales;
    private JTable tableActuales;
    
    // Pestaña Historial
    private DefaultTableModel modelHistorial;
    private JTable tableHistorial;
    private JComboBox<String> comboProductosFiltro;
    private JButton btnRefrescarHistorial;

    public ProductoTerminadoGUI(JFrame parent) {
        super(parent, "Productos Terminados", true);
        setSize(1100, 650);
        setLocationRelativeTo(parent);
        initUI();
        cargarProductosActuales();
        cargarHistorialProduccion();
    }

    private void initUI() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(ThemeUtil.COLOR_FONDO);

        // Panel superior con título
        JPanel panelTitulo = ThemeUtil.crearPanelTitulo("Productos Terminados");
        panelTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(panelTitulo, BorderLayout.NORTH);

        // ===== PANEL CON PESTAÑAS =====
        tabsProductos = new JTabbedPane();

        // Pestaña 1: Actuales
        JPanel panelActuales = crearPanelActuales();
        tabsProductos.addTab("📦 Actuales", panelActuales);

        // Pestaña 2: Historial
        JPanel panelHistorial = crearPanelHistorial();
        tabsProductos.addTab("📋 Historial", panelHistorial);

        add(tabsProductos, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottom.setBackground(ThemeUtil.COLOR_FONDO);
        bottom.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        JButton btnCerrar = new JButton("❌ Cerrar");
        ThemeUtil.aplicarEstiloSecundario(btnCerrar);
        btnCerrar.addActionListener(e -> dispose());
        
        bottom.add(btnCerrar);
        add(bottom, BorderLayout.SOUTH);
    }

    // ===== PESTAÑA 1: ACTUALES =====
    private JPanel crearPanelActuales() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setBackground(ThemeUtil.COLOR_FONDO);

        // Botón refrescar
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelBotones.setBackground(ThemeUtil.COLOR_FONDO);
        JButton btnRefrescar = new JButton("🔄 Refrescar");
        ThemeUtil.aplicarEstiloSecundario(btnRefrescar);
        btnRefrescar.addActionListener(e -> cargarProductosActuales());
        panelBotones.add(btnRefrescar);
        panel.add(panelBotones, BorderLayout.NORTH);

        // Tabla de productos actuales
        modelActuales = new DefaultTableModel(
            new String[]{"Producto", "Cantidad", "Fecha Última Producción"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;  // Solo lectura
            }
        };
        tableActuales = new JTable(modelActuales);
        tableActuales.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        ThemeUtil.aplicarEstiloTabla(tableActuales);
        
        JScrollPane scrollActuales = new JScrollPane(tableActuales);
        panel.add(scrollActuales, BorderLayout.CENTER);

        return panel;
    }

    // ===== PESTAÑA 2: HISTORIAL =====
    private JPanel crearPanelHistorial() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        panel.setBackground(ThemeUtil.COLOR_FONDO);

        // Panel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        panelFiltros.setBackground(ThemeUtil.COLOR_FONDO);
        
        JLabel lblFiltro = new JLabel("Filtrar por Producto:");
        lblFiltro.setFont(ThemeUtil.FUENTE_NORMAL);
        panelFiltros.add(lblFiltro);
        
        comboProductosFiltro = new JComboBox<>();
        comboProductosFiltro.setFont(ThemeUtil.FUENTE_NORMAL);
        comboProductosFiltro.addItem("--- Todos ---");
        comboProductosFiltro.addActionListener(e -> cargarHistorialProduccion());
        panelFiltros.add(comboProductosFiltro);

        btnRefrescarHistorial = new JButton("🔄 Refrescar");
        ThemeUtil.aplicarEstiloSecundario(btnRefrescarHistorial);
        btnRefrescarHistorial.addActionListener(e -> cargarHistorialProduccion());
        panelFiltros.add(btnRefrescarHistorial);

        panel.add(panelFiltros, BorderLayout.NORTH);

        // Tabla de historial
        modelHistorial = new DefaultTableModel(
            new String[]{"Producto", "Cantidad Producida", "Fecha", "Lote"}, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;  // Solo lectura
            }
        };
        tableHistorial = new JTable(modelHistorial);
        tableHistorial.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        ThemeUtil.aplicarEstiloTabla(tableHistorial);
        
        JScrollPane scrollHistorial = new JScrollPane(tableHistorial);
        panel.add(scrollHistorial, BorderLayout.CENTER);

        return panel;
    }

    // ===== CARGAR PRODUCTOS ACTUALES =====
    private void cargarProductosActuales() {
        modelActuales.setRowCount(0);  // Limpiar

        // Llenar comboProductosFiltro también
        comboProductosFiltro.removeAllItems();
        comboProductosFiltro.addItem("--- Todos ---");

        for (ProductoTerminado p : DataStore.getProductosTerminados()) {
            modelActuales.addRow(new Object[]{
                p.getNombre(),
                p.getCantidad(),
                p.getFechaProduccion() != null ? p.getFechaProduccion() : "N/A"
            });
            comboProductosFiltro.addItem(p.getNombre());
        }

        System.out.println("✅ Productos actuales cargados: " + modelActuales.getRowCount());
    }

    // ===== CARGAR HISTORIAL DE PRODUCCIÓN =====
    private void cargarHistorialProduccion() {
        modelHistorial.setRowCount(0);  // Limpiar

        String filtroProducto = (String) comboProductosFiltro.getSelectedItem();
        boolean filtrarPorProducto = filtroProducto != null && 
                                     !filtroProducto.equals("--- Todos ---");

        String sql = """
            SELECT nombre_producto, cantidad, fecha, lote_asociado 
            FROM historial_productos 
            WHERE accion = 'Producción'
        """;

        if (filtrarPorProducto) {
            sql += " AND nombre_producto = ? ";
        }

        sql += " ORDER BY fecha DESC LIMIT 500";

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (filtrarPorProducto) {
                ps.setString(1, filtroProducto);
            }

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String nombreProducto = rs.getString("nombre_producto");
                int cantidad = rs.getInt("cantidad");
                String fecha = rs.getString("fecha");
                String lote = rs.getString("lote_asociado");

                modelHistorial.addRow(new Object[]{
                    nombreProducto,
                    cantidad,
                    fecha != null ? fecha : "N/A",
                    lote != null ? lote : "N/A"
                });
            }

            if (modelHistorial.getRowCount() == 0) {
                System.out.println("ℹ️  No hay registros de producción para mostrar");
            } else {
                System.out.println("✅ Historial de producción cargado: " + 
                    modelHistorial.getRowCount() + " registros");
            }

        } catch (SQLException e) {
            System.err.println("❌ Error cargando historial de producción: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error cargando historial: " + e.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ===== REFRESCAR TODAS LAS VISTAS =====
    public void refrescarTodas() {
        cargarProductosActuales();
        cargarHistorialProduccion();
    }
}