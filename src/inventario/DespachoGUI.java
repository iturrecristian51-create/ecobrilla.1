package inventario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DespachoGUI extends JDialog {
    private JFrame parentFrame;

    private JTable tablaProductos;
    private DefaultTableModel modelProductos;

    private JTable tablaItems;
    private DefaultTableModel modelItems;

    private JTable tablaDespachos;
    private DefaultTableModel modelDespachos;

    private JTextField txtCantidad;

    // Campos de cliente
    private JTextField txtCliente;
    private JTextField txtNIT;
    private JTextField txtTelefono;
    private JTextField txtDireccion;
    private JTextField txtCiudad;

    private JTextArea areaNotas;

    // Filtros
    private FiltroPanel filtroProductos;
    private FiltroPanel filtroDespachos;

    // Variables para manejar el despacho actual
    private Despacho despachoActual;
    private List<Despacho.Item> itemsDespacho;

    public DespachoGUI(JFrame parent) {
        super(parent, "Gestión de Despachos", true);
        this.parentFrame = parent;
        setSize(1200, 750);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout(8, 8));

        // ===== PANEL IZQUIERDO (PRODUCTOS) =====
        JPanel left = new JPanel(new BorderLayout(6, 6));
        left.setBorder(BorderFactory.createTitledBorder("Productos disponibles"));
        left.setPreferredSize(new Dimension(350, 400));

        // Filtro para productos
        filtroProductos = new FiltroPanel("Buscar productos...", this::aplicarFiltroProductos);
        left.add(filtroProductos, BorderLayout.NORTH);

        modelProductos = new DefaultTableModel(new String[]{"Nombre", "Cantidad"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaProductos = new JTable(modelProductos);
        
        // Configurar filtrado
        FiltradorUtil.configurarFiltroColumnas(tablaProductos, new String[]{"Nombre", "Cantidad"});
        
        // Scroll con altura mejorada
        JScrollPane scrollProductos = new JScrollPane(tablaProductos);
        scrollProductos.setPreferredSize(new Dimension(320, 280));
        scrollProductos.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        left.add(scrollProductos, BorderLayout.CENTER);

        JPanel addPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        txtCantidad = new JTextField(6);
        JButton btnAgregar = new JButton("Agregar al despacho");
        addPanel.add(new JLabel("Cantidad:"));
        addPanel.add(txtCantidad);
        addPanel.add(btnAgregar);
        left.add(addPanel, BorderLayout.SOUTH);

        // ===== PANEL CENTRAL (ITEMS DEL DESPACHO) =====
        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.setBorder(BorderFactory.createTitledBorder("Items del despacho"));

        modelItems = new DefaultTableModel(new String[]{"Producto", "Cantidad"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaItems = new JTable(modelItems);
        center.add(new JScrollPane(tablaItems), BorderLayout.CENTER);

        JPanel centerBtns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnQuitar = new JButton("Quitar seleccionado");
        centerBtns.add(btnQuitar);
        center.add(centerBtns, BorderLayout.SOUTH);

        // ===== PANEL DERECHO (DATOS CLIENTE) =====
        JPanel right = new JPanel(new BorderLayout(6, 6));
        right.setBorder(BorderFactory.createTitledBorder("Datos del cliente"));

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        txtCliente = new JTextField();
        txtNIT = new JTextField();
        txtTelefono = new JTextField();
        txtDireccion = new JTextField();
        txtCiudad = new JTextField();
        areaNotas = new JTextArea(4, 20);

        Font fuenteCampos = new Font("SansSerif", Font.PLAIN, 14);
        txtCliente.setFont(fuenteCampos);
        txtNIT.setFont(fuenteCampos);
        txtTelefono.setFont(fuenteCampos);
        txtDireccion.setFont(fuenteCampos);
        txtCiudad.setFont(fuenteCampos);
        areaNotas.setFont(fuenteCampos);

        Dimension tamanoCampo = new Dimension(260, 34);
        txtCliente.setPreferredSize(tamanoCampo);
        txtNIT.setPreferredSize(tamanoCampo);
        txtTelefono.setPreferredSize(tamanoCampo);
        txtDireccion.setPreferredSize(tamanoCampo);
        txtCiudad.setPreferredSize(tamanoCampo);
        areaNotas.setPreferredSize(new Dimension(260, 100));

        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Cliente:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; form.add(txtCliente, gbc);
        gbc.gridx = 0; gbc.gridy = 1; form.add(new JLabel("NIT:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; form.add(txtNIT, gbc);
        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Teléfono:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; form.add(txtTelefono, gbc);
        gbc.gridx = 0; gbc.gridy = 3; form.add(new JLabel("Dirección:"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; form.add(txtDireccion, gbc);
        gbc.gridx = 0; gbc.gridy = 4; form.add(new JLabel("Ciudad:"), gbc);
        gbc.gridx = 1; gbc.gridy = 4; form.add(txtCiudad, gbc);
        gbc.gridx = 0; gbc.gridy = 5; form.add(new JLabel("Notas:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; form.add(new JScrollPane(areaNotas), gbc);

        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollForm.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        right.add(scrollForm, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnConfirmar = new JButton("Confirmar despacho");
        JButton btnCerrar = new JButton("Cerrar");
        actions.add(btnConfirmar);
        actions.add(btnCerrar);
        right.add(actions, BorderLayout.SOUTH);

        // ===== PANEL DESPACHOS REGISTRADOS =====
        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        bottom.setBorder(BorderFactory.createTitledBorder("Despachos registrados"));

        // Filtro para despachos
        filtroDespachos = new FiltroPanel("Buscar despachos por cliente, NIT, ciudad...", this::aplicarFiltroDespachos);
        bottom.add(filtroDespachos, BorderLayout.NORTH);

        modelDespachos = new DefaultTableModel(new String[]{"Remisión", "Fecha", "Cliente", "Items", "Ciudad"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaDespachos = new JTable(modelDespachos);
        
        // Configurar filtrado
        FiltradorUtil.configurarFiltroColumnas(tablaDespachos, new String[]{"Remisión", "Fecha", "Cliente", "Items", "Ciudad"});
        
        bottom.add(new JScrollPane(tablaDespachos), BorderLayout.CENTER);

        // ===== JTabbedPane PRINCIPAL =====
        JTabbedPane tabsDespachos = new JTabbedPane();

        // Pestaña principal: Gestión de despachos
        JPanel panelDespachos = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new GridLayout(1, 3, 8, 8));
        top.add(left);
        top.add(center);
        top.add(right);
        panelDespachos.add(top, BorderLayout.CENTER);
        panelDespachos.add(bottom, BorderLayout.SOUTH);
        tabsDespachos.addTab("Gestión de Despachos", panelDespachos);

        // Pestaña historial productos terminados
        HistorialProductosPanel historialPanel = new HistorialProductosPanel();
        tabsDespachos.addTab("Historial Productos", historialPanel);

        add(tabsDespachos, BorderLayout.CENTER);

        // ===== LISTENERS =====
        btnAgregar.addActionListener(e -> onAgregarItem());
        btnQuitar.addActionListener(e -> onQuitarItem());
        btnConfirmar.addActionListener(e -> onConfirmarDespacho());
        btnCerrar.addActionListener(e -> dispose());

        tablaDespachos.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = tablaDespachos.getSelectedRow();
                    if (row >= 0) {
                        String remision = (String) modelDespachos.getValueAt(row, 0);
                        abrirFacturaPorId(remision);
                    }
                }
            }
        });

        // Inicializar despacho actual
        crearNuevoDespacho();
        
        // Cargar datos iniciales
        refreshProductos();
        refreshDespachos();
        actualizarContadores();
    }

    // ===== MÉTODOS DE FILTRADO =====
    private void aplicarFiltroProductos() {
        String filtro = filtroProductos.getTextoFiltro();
        FiltradorUtil.aplicarFiltroTabla(tablaProductos, filtro);
        actualizarContadores();
    }

    private void aplicarFiltroDespachos() {
        String filtro = filtroDespachos.getTextoFiltro();
        FiltradorUtil.aplicarFiltroTabla(tablaDespachos, filtro);
        actualizarContadores();
    }

    private void actualizarContadores() {
        // Contador para productos
        int totalProductos = DataStore.productos.size();
        int productosFiltrados = filtroProductos.getTextoFiltro().isEmpty() ? 
            totalProductos : tablaProductos.getRowCount();
        filtroProductos.actualizarContador(totalProductos, productosFiltrados);
        
        // Contador para despachos
        int totalDespachos = DataStore.despachos.size();
        int despachosFiltrados = filtroDespachos.getTextoFiltro().isEmpty() ? 
            totalDespachos : tablaDespachos.getRowCount();
        filtroDespachos.actualizarContador(totalDespachos, despachosFiltrados);
    }

    // ===== MÉTODOS DE REFRESCO =====
    public void refreshProductos() {
        modelProductos.setRowCount(0);
        for (ProductoTerminado p : DataStore.productos) {
            if (p.getCantidad() > 0) {
                modelProductos.addRow(new Object[]{p.getNombre(), p.getCantidad()});
            }
        }
        filtroProductos.limpiarFiltro();
        actualizarContadores();
    }

    public void refreshDespachos() {
        modelDespachos.setRowCount(0);
        for (Despacho d : DataStore.despachos) {
            String itemsCount = Integer.toString(d.getItems().size());
            // CORRECCIÓN: Usar los métodos correctos según tu clase Despacho
            modelDespachos.addRow(new Object[]{
                d.getNumeroRemision(),
                d.getFechaHora() != null ? d.getFechaHora().toLocalDate().toString() : "Sin fecha",
                d.getClienteNombre() != null ? d.getClienteNombre() : d.getCliente(),
                itemsCount,
                d.getClienteCiudad() != null ? d.getClienteCiudad() : d.getCiudad()
            });
        }
        filtroDespachos.limpiarFiltro();
        actualizarContadores();
    }

    // ===== MÉTODOS PARA MANEJAR EL DESPACHO ACTUAL =====
    private void crearNuevoDespacho() {
        despachoActual = new Despacho();
        itemsDespacho = new ArrayList<>();
        limpiarFormulario();
        actualizarTablaItems();
    }

    private void limpiarFormulario() {
        txtCliente.setText("");
        txtNIT.setText("");
        txtTelefono.setText("");
        txtDireccion.setText("");
        txtCiudad.setText("");
        areaNotas.setText("");
        txtCantidad.setText("");
        modelItems.setRowCount(0);
    }

    private void actualizarTablaItems() {
        modelItems.setRowCount(0);
        for (Despacho.Item item : itemsDespacho) {
            modelItems.addRow(new Object[]{item.getNombreProducto(), item.getCantidad()});
        }
    }

    // ===== MÉTODOS DE ACCIÓN =====
    private void onAgregarItem() {
        int sel = tablaProductos.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto.");
            return;
        }
        
        // Convertir índice de fila filtrada
        int modeloIndex = tablaProductos.convertRowIndexToModel(sel);
        String nombre = (String) modelProductos.getValueAt(modeloIndex, 0);
        String sCantidad = txtCantidad.getText().trim();
        int cantidad;
        try {
            cantidad = Integer.parseInt(sCantidad);
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida.");
            return;
        }

        // Verificar stock disponible
        ProductoTerminado producto = buscarProductoTerminadoPorNombre(nombre);
        if (producto == null || cantidad > producto.getCantidad()) {
            JOptionPane.showMessageDialog(this, 
                "Stock insuficiente. Disponible: " + 
                (producto != null ? producto.getCantidad() : 0));
            return;
        }

        // Agregar item al despacho actual
        Despacho.Item nuevoItem = new Despacho.Item(nombre, cantidad);
        itemsDespacho.add(nuevoItem);
        actualizarTablaItems();
        txtCantidad.setText("");
    }

    private void onQuitarItem() {
        int sel = tablaItems.getSelectedRow();
        if (sel >= 0) {
            itemsDespacho.remove(sel);
            actualizarTablaItems();
        } else {
            JOptionPane.showMessageDialog(this, "Seleccione un item para quitar.");
        }
    }

    private ProductoTerminado buscarProductoTerminadoPorNombre(String nombre) {
        for (ProductoTerminado p : DataStore.productos) {
            if (p.getNombre().equalsIgnoreCase(nombre)) return p;
        }
        return null;
    }

    private void onConfirmarDespacho() {
        if (itemsDespacho.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Agregue items al despacho");
            return;
        }

        String cliente = txtCliente.getText().trim();
        String nit = txtNIT.getText().trim();
        
        if (cliente.isEmpty() || nit.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar al menos Cliente y NIT.");
            return;
        }

        try {
            // Completar datos del despacho actual
            // CORRECCIÓN: Usar los métodos correctos según tu clase Despacho
            if (despachoActual != null) {
                // Intentar usar ambos sets de métodos para compatibilidad
                try {
                    despachoActual.setClienteNombre(cliente);
                    despachoActual.setClienteNIT(nit);
                    despachoActual.setClienteTelefono(txtTelefono.getText().trim());
                    despachoActual.setClienteDireccion(txtDireccion.getText().trim());
                    despachoActual.setClienteCiudad(txtCiudad.getText().trim());
                } catch (Exception e) {
                    // Si falla, usar los métodos antiguos
                    despachoActual.setCliente(cliente);
                    despachoActual.setNit(nit);
                    despachoActual.setTelefono(txtTelefono.getText().trim());
                    despachoActual.setDireccion(txtDireccion.getText().trim());
                    despachoActual.setCiudad(txtCiudad.getText().trim());
                }
                
                despachoActual.setNotas(areaNotas.getText().trim());
                despachoActual.setFechaHora(LocalDateTime.now());
                
                // Asignar los items al despacho
                despachoActual.setItems(new ArrayList<>(itemsDespacho));
            } else {
                // Crear nuevo despacho si no existe
                despachoActual = new Despacho();
                // Usar métodos según disponibilidad
                try {
                    despachoActual.setClienteNombre(cliente);
                    despachoActual.setClienteNIT(nit);
                    despachoActual.setClienteTelefono(txtTelefono.getText().trim());
                    despachoActual.setClienteDireccion(txtDireccion.getText().trim());
                    despachoActual.setClienteCiudad(txtCiudad.getText().trim());
                } catch (Exception e) {
                    despachoActual.setCliente(cliente);
                    despachoActual.setNit(nit);
                    despachoActual.setTelefono(txtTelefono.getText().trim());
                    despachoActual.setDireccion(txtDireccion.getText().trim());
                    despachoActual.setCiudad(txtCiudad.getText().trim());
                }
                despachoActual.setNotas(areaNotas.getText().trim());
                despachoActual.setFechaHora(LocalDateTime.now());
                despachoActual.setItems(new ArrayList<>(itemsDespacho));
            }

            // Registrar el despacho usando DataStore
            DataStore.registrarDespacho(despachoActual);

            JOptionPane.showMessageDialog(this, 
                "Despacho registrado exitosamente\nRemisión: " + despachoActual.getNumeroRemision());

            // Crear nuevo despacho para el próximo
            crearNuevoDespacho();
            refreshProductos();
            refreshDespachos();

            // Abrir factura
            new VentanaFacturaGUI(this, despachoActual).setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void abrirFacturaPorId(String numeroRemision) {
        for (Despacho d : DataStore.despachos) {
            if (d.getNumeroRemision().equals(numeroRemision)) {
                new VentanaFacturaGUI(this, d).setVisible(true);
                return;
            }
        }
        JOptionPane.showMessageDialog(this, "Despacho no encontrado.");
    }
}