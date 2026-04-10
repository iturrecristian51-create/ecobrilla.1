package inventario;

import java.awt.*;
import java.sql.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ProductosTerminadosPanel extends JPanel {

    private JTabbedPane tabs;
    private DefaultTableModel modelActuales;
    private DefaultTableModel modelHistorial;
    private JTable tableActuales;
    private JTable tableHistorial;
    private JComboBox<String> comboProductosFiltro;
    private JButton btnRefrescarHistorial;

    public ProductosTerminadosPanel() {
        setLayout(new BorderLayout());

        tabs = new JTabbedPane();

        // ===== PESTAÑA 1: ACTUALES =====
        JPanel panelActuales = crearPanelActuales();
        tabs.addTab("Actuales", panelActuales);

        // ===== PESTAÑA 2: HISTORIAL =====
        JPanel panelHistorial = crearPanelHistorial();
        tabs.addTab("Historial", panelHistorial);

        add(tabs, BorderLayout.CENTER);

        // Cargar datos iniciales
        cargarProductosActuales();
        cargarHistorialProduccion();
    }

    // ===== PESTAÑA 1: ACTUALES =====
    private JPanel crearPanelActuales() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Botón refrescar
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefrescar = new JButton("Refrescar");
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
        JScrollPane scrollActuales = new JScrollPane(tableActuales);
        panel.add(scrollActuales, BorderLayout.CENTER);

        return panel;
    }

    // ===== PESTAÑA 2: HISTORIAL =====
    private JPanel crearPanelHistorial() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        // Panel de filtros
        JPanel panelFiltros = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panelFiltros.add(new JLabel("Filtrar por Producto:"));
        comboProductosFiltro = new JComboBox<>();
        comboProductosFiltro.addItem("--- Todos ---");
        comboProductosFiltro.addActionListener(e -> cargarHistorialProduccion());
        panelFiltros.add(comboProductosFiltro);

        btnRefrescarHistorial = new JButton("Refrescar");
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
