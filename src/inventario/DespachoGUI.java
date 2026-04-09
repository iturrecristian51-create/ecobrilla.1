package inventario;

import java.awt.*;
import java.awt.event.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class DespachoGUI extends JDialog {
    private JFrame parentFrame;

    private JTable tablaProductos;
    private DefaultTableModel modelProductos;

    private JTable tablaItems;
    private DefaultTableModel modelItems;

    private JTable tablaDespachos;
    private DefaultTableModel modelDespachos;

    private JTextField txtCantidad;

    // Selector de cliente (JComboBox)
    private JComboBox<Cliente> cmbCliente;

    // Campos de solo lectura — se llenan automáticamente al elegir cliente
    private JTextField txtCliente;
    private JTextField txtNIT;
    private JTextField txtTelefono;
    private JTextField txtDireccion;
    private JTextField txtCiudad;

    private JTextArea areaNotas;
    
    private JSpinner spinnerFechaEntrega; // Campo para fecha de entrega

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
        left.setPreferredSize(new Dimension(380, 500));

        filtroProductos = new FiltroPanel("Buscar productos...", this::aplicarFiltroProductos);
        left.add(filtroProductos, BorderLayout.NORTH);

        modelProductos = new DefaultTableModel(new String[]{"Nombre", "Cantidad"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaProductos = new JTable(modelProductos);
        FiltradorUtil.configurarFiltroColumnas(tablaProductos, new String[]{"Nombre", "Cantidad"});

        JScrollPane scrollProductos = new JScrollPane(tablaProductos);
        scrollProductos.setPreferredSize(new Dimension(360, 380));
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

        // ── JComboBox para seleccionar cliente ──────────────────────────────
        cmbCliente = new JComboBox<>();
        cmbCliente.setFont(new Font("SansSerif", Font.PLAIN, 13));
        cmbCliente.setPreferredSize(new Dimension(260, 30));

        // Botón para gestionar clientes sin cerrar DespachoGUI
        JButton btnGestionarClientes = new JButton("Gestionar Clientes");
        btnGestionarClientes.setFont(new Font("SansSerif", Font.PLAIN, 11));

        // ── Campos de solo lectura ──────────────────────────────────────────
        txtCliente   = new JTextField(); txtCliente.setEditable(false);
        txtNIT       = new JTextField(); txtNIT.setEditable(false);
        txtTelefono  = new JTextField(); txtTelefono.setEditable(false);
        txtDireccion = new JTextField(); txtDireccion.setEditable(false);
        txtCiudad    = new JTextField(); txtCiudad.setEditable(false);
        areaNotas    = new JTextArea(4, 20);
        
        // Selector de fecha de entrega
        Date fechaActual = new Date();
        SpinnerDateModel modeloFechaEntrega = new SpinnerDateModel(fechaActual, null, null, Calendar.DAY_OF_MONTH);
        spinnerFechaEntrega = new JSpinner(modeloFechaEntrega);
        spinnerFechaEntrega.setEditor(new JSpinner.DateEditor(spinnerFechaEntrega, "yyyy-MM-dd"));
        spinnerFechaEntrega.setPreferredSize(new Dimension(260, 30));

        // Fondo gris para indicar que son solo lectura
        Color soloLectura = new Color(240, 240, 240);
        txtCliente.setBackground(soloLectura);
        txtNIT.setBackground(soloLectura);
        txtTelefono.setBackground(soloLectura);
        txtDireccion.setBackground(soloLectura);
        txtCiudad.setBackground(soloLectura);

        Font fuenteCampos = new Font("SansSerif", Font.PLAIN, 13);
        txtCliente.setFont(fuenteCampos);
        txtNIT.setFont(fuenteCampos);
        txtTelefono.setFont(fuenteCampos);
        txtDireccion.setFont(fuenteCampos);
        txtCiudad.setFont(fuenteCampos);
        areaNotas.setFont(fuenteCampos);

        Dimension tamanoCampo = new Dimension(260, 30);
        txtCliente.setPreferredSize(tamanoCampo);
        txtNIT.setPreferredSize(tamanoCampo);
        txtTelefono.setPreferredSize(tamanoCampo);
        txtDireccion.setPreferredSize(tamanoCampo);
        txtCiudad.setPreferredSize(tamanoCampo);

        // ── Layout del formulario ───────────────────────────────────────────
        // Fila 0: selector de cliente
        gbc.gridx = 0; gbc.gridy = 0; form.add(new JLabel("Cliente:"),      gbc);
        gbc.gridx = 1; gbc.gridy = 0; form.add(cmbCliente,                  gbc);
        // Fila 1: botón gestionar
        gbc.gridx = 1; gbc.gridy = 1; form.add(btnGestionarClientes,        gbc);
        // Fila 2-6: datos de solo lectura
        gbc.gridx = 0; gbc.gridy = 2; form.add(new JLabel("Nombre:"),       gbc);
        gbc.gridx = 1; gbc.gridy = 2; form.add(txtCliente,                  gbc);
        gbc.gridx = 0; gbc.gridy = 3; form.add(new JLabel("NIT:"),          gbc);
        gbc.gridx = 1; gbc.gridy = 3; form.add(txtNIT,                      gbc);
        gbc.gridx = 0; gbc.gridy = 4; form.add(new JLabel("Teléfono:"),     gbc);
        gbc.gridx = 1; gbc.gridy = 4; form.add(txtTelefono,                 gbc);
        gbc.gridx = 0; gbc.gridy = 5; form.add(new JLabel("Dirección:"),    gbc);
        gbc.gridx = 1; gbc.gridy = 5; form.add(txtDireccion,                gbc);
        gbc.gridx = 0; gbc.gridy = 6; form.add(new JLabel("Ciudad:"),       gbc);
        gbc.gridx = 1; gbc.gridy = 6; form.add(txtCiudad,                   gbc);
        gbc.gridx = 0; gbc.gridy = 7; form.add(new JLabel("Notas:"),        gbc);
        gbc.gridx = 1; gbc.gridy = 7; form.add(new JScrollPane(areaNotas),  gbc);
        gbc.gridx = 0; gbc.gridy = 8; form.add(new JLabel("Fecha de Entrega:"), gbc);
        gbc.gridx = 1; gbc.gridy = 8; form.add(spinnerFechaEntrega,         gbc);

        JScrollPane scrollForm = new JScrollPane(form);
        scrollForm.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollForm.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        right.add(scrollForm, BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnConfirmar = new JButton("Confirmar despacho");
        JButton btnCerrar    = new JButton("Cerrar");
        actions.add(btnConfirmar);
        actions.add(btnCerrar);
        right.add(actions, BorderLayout.SOUTH);

        // ===== PANEL DESPACHOS REGISTRADOS =====
        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        bottom.setBorder(BorderFactory.createTitledBorder("Despachos registrados"));

        filtroDespachos = new FiltroPanel(
                "Buscar despachos por cliente, NIT, ciudad...",
                this::aplicarFiltroDespachos);

        JPanel panelFiltroProducto = new JPanel(new BorderLayout(6, 6));
        panelFiltroProducto.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        JLabel     lblFiltroProducto  = new JLabel("Filtrar por Producto:");
        JTextField txtFiltroProducto  = new JTextField(20);
        JButton    btnMostrarTotales  = new JButton("Mostrar Total Entregado");
        JLabel     lblTotalEntregado  = new JLabel("Total: 0");
        panelFiltroProducto.add(lblFiltroProducto, BorderLayout.WEST);
        panelFiltroProducto.add(txtFiltroProducto, BorderLayout.CENTER);
        panelFiltroProducto.add(btnMostrarTotales, BorderLayout.EAST);
        panelFiltroProducto.add(lblTotalEntregado, BorderLayout.SOUTH);

        JPanel panelFiltrosCombinados = new JPanel(new BorderLayout(4, 4));
        panelFiltrosCombinados.add(filtroDespachos,     BorderLayout.NORTH);
        panelFiltrosCombinados.add(panelFiltroProducto, BorderLayout.SOUTH);
        bottom.add(panelFiltrosCombinados, BorderLayout.NORTH);

        modelDespachos = new DefaultTableModel(
                new String[]{"Remisión", "Fecha", "Fecha Entrega", "Cliente", "Items", "Ciudad"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaDespachos = new JTable(modelDespachos);
        FiltradorUtil.configurarFiltroColumnas(tablaDespachos,
                new String[]{"Remisión", "fecha", "Fecha Entrega", "Cliente", "Items", "Ciudad"});
        bottom.add(new JScrollPane(tablaDespachos), BorderLayout.CENTER);

        // ===== JTabbedPane PRINCIPAL =====
        JTabbedPane tabsDespachos = new JTabbedPane();
        JPanel panelDespachos = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new GridLayout(1, 3, 8, 8));
        top.add(left);
        top.add(center);
        top.add(right);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, top, bottom);
        splitPane.setResizeWeight(0.65);
        splitPane.setDividerSize(6);
        splitPane.setContinuousLayout(true);
        panelDespachos.add(splitPane, BorderLayout.CENTER);

        tabsDespachos.addTab("Gestión de Despachos", panelDespachos);
        HistorialProductosPanel historialPanel = new HistorialProductosPanel();
        tabsDespachos.addTab("Historial Productos", historialPanel);
        add(tabsDespachos, BorderLayout.CENTER);

        // ===== LISTENERS =====
        btnAgregar.addActionListener(e   -> onAgregarItem());
        btnQuitar.addActionListener(e    -> onQuitarItem());
        btnConfirmar.addActionListener(e -> onConfirmarDespacho());
        btnCerrar.addActionListener(e    -> dispose());

        // Cuando se selecciona un cliente en el combo → llenar campos
        cmbCliente.addActionListener(e -> {
            Cliente seleccionado = (Cliente) cmbCliente.getSelectedItem();
            if (seleccionado != null) {
                txtCliente.setText(seleccionado.getNombre());
                txtNIT.setText(seleccionado.getNit());
                txtTelefono.setText(seleccionado.getTelefono());
                txtDireccion.setText(seleccionado.getDireccion());
                txtCiudad.setText(seleccionado.getCiudad());
            } else {
                limpiarCamposCliente();
            }
        });

        // Botón gestionar clientes → abre GestionClientesGUI y recarga el combo al cerrar
        btnGestionarClientes.addActionListener(e -> {
            new GestionClientesGUI(parentFrame).setVisible(true);
            recargarComboClientes(); // Actualiza la lista después de gestionar
        });

        // Listener total entregado
        btnMostrarTotales.addActionListener(e -> {
            String nombreProducto = txtFiltroProducto.getText().trim();
            if (nombreProducto.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Ingrese el nombre del producto");
                return;
            }
            aplicarFiltroDespachosPorProducto(nombreProducto, lblTotalEntregado);
        });

        // Doble clic en tabla despachos → abrir factura
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

        recargarComboClientes();  // ← primero llena el combo
crearNuevoDespacho();     // ← ahora sí puede hacer setSelectedIndex(0)
refreshProductos();
refreshDespachos();
actualizarContadores();
    }

    // ===== COMBO DE CLIENTES =====

    /**
     * Recarga el JComboBox con la lista actual de clientes desde SQLite.
     * Llama a esto después de crear/editar/eliminar clientes.
     */
    public void recargarComboClientes() {
        Cliente seleccionadoAntes = (Cliente) cmbCliente.getSelectedItem();
        cmbCliente.removeAllItems();
        cmbCliente.addItem(null); // opción vacía al inicio
        for (Cliente c : ClienteDAO.listarTodos()) {
            cmbCliente.addItem(c);
        }
        // Restaurar selección si sigue existiendo
        if (seleccionadoAntes != null) {
            for (int i = 0; i < cmbCliente.getItemCount(); i++) {
                Cliente c = cmbCliente.getItemAt(i);
                if (c != null && c.getId() == seleccionadoAntes.getId()) {
                    cmbCliente.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    private void limpiarCamposCliente() {
        txtCliente.setText("");
        txtNIT.setText("");
        txtTelefono.setText("");
        txtDireccion.setText("");
        txtCiudad.setText("");
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

    private void aplicarFiltroDespachosPorProducto(String nombreProducto, JLabel lblTotal) {
        modelDespachos.setRowCount(0);
        int totalEntregado = 0;
        for (Despacho d : DataStore.despachos) {
            boolean contieneProducto = false;
            for (Despacho.Item item : d.getItems()) {
                if (item.getNombreProducto().toLowerCase()
                        .contains(nombreProducto.toLowerCase())) {
                    contieneProducto = true;
                    totalEntregado  += item.getCantidad();
                    break;
                }
            }
            if (contieneProducto) {
                modelDespachos.addRow(new Object[]{
                    d.getNumeroRemision(),
                    d.getFechaHora() != null
                        ? d.getFechaHora().toLocalDate().toString() : "Sin fecha",
                    d.getFechaEntrega() != null
                        ? d.getFechaEntrega().toLocalDate().toString() : "Sin fecha",
                    d.getClienteNombre() != null ? d.getClienteNombre() : d.getCliente(),
                    d.getItems().size(),
                    d.getClienteCiudad() != null ? d.getClienteCiudad() : d.getCiudad()
                });
            }
        }
        lblTotal.setText("Total entregado de '" + nombreProducto + "': " + totalEntregado);
    }

    public Map<String, Integer> obtenerProductosEntregados() {
        Map<String, Integer> totalPorProducto = new HashMap<>();
        for (Despacho d : DataStore.despachos) {
            for (Despacho.Item item : d.getItems()) {
                totalPorProducto.merge(
                    item.getNombreProducto(), item.getCantidad(), Integer::sum);
            }
        }
        return totalPorProducto;
    }

    public void generarReporteProductosEntregados() {
        Map<String, Integer> totales = obtenerProductosEntregados();
        StringBuilder reporte = new StringBuilder("REPORTE DE PRODUCTOS ENTREGADOS\n");
        reporte.append("================================\n");
        int totalGeneral = 0;
        for (Map.Entry<String, Integer> entry : totales.entrySet()) {
            reporte.append(String.format("%-30s %d unidades\n",
                entry.getKey(), entry.getValue()));
            totalGeneral += entry.getValue();
        }
        reporte.append("================================\n");
        reporte.append("TOTAL GENERAL: ").append(totalGeneral).append(" unidades\n");
        JOptionPane.showMessageDialog(this, reporte.toString(),
            "Reporte de Entregas", JOptionPane.INFORMATION_MESSAGE);
    }

    private void actualizarContadores() {
        int totalProductos     = DataStore.productos.size();
        int productosFiltrados = filtroProductos.getTextoFiltro().isEmpty()
            ? totalProductos : tablaProductos.getRowCount();
        filtroProductos.actualizarContador(totalProductos, productosFiltrados);

        int totalDespachos     = DataStore.despachos.size();
        int despachosFiltrados = filtroDespachos.getTextoFiltro().isEmpty()
            ? totalDespachos : tablaDespachos.getRowCount();
        filtroDespachos.actualizarContador(totalDespachos, despachosFiltrados);
    }

    // ===== REFRESCO =====

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
            modelDespachos.addRow(new Object[]{
                d.getNumeroRemision(),
                d.getFechaHora() != null
                    ? d.getFechaHora().toLocalDate().toString() : "Sin fecha",
                d.getFechaEntrega() != null
                    ? d.getFechaEntrega().toLocalDate().toString() : "Sin fecha",
                d.getClienteNombre() != null ? d.getClienteNombre() : d.getCliente(),
                Integer.toString(d.getItems().size()),
                d.getClienteCiudad() != null ? d.getClienteCiudad() : d.getCiudad()
            });
        }
        filtroDespachos.limpiarFiltro();
        actualizarContadores();
    }

    // ===== MANEJO DEL DESPACHO ACTUAL =====

    private void crearNuevoDespacho() {
        despachoActual = new Despacho();
        itemsDespacho  = new ArrayList<>();
        limpiarFormulario();
        actualizarTablaItems();
    }

    private void limpiarFormulario() {
    if (cmbCliente.getItemCount() > 0) {
        cmbCliente.setSelectedIndex(0);
    }
    limpiarCamposCliente();
    areaNotas.setText("");
    txtCantidad.setText("");
    spinnerFechaEntrega.setValue(new Date()); // Reinicializar a fecha actual
    modelItems.setRowCount(0);
}

    private void actualizarTablaItems() {
        modelItems.setRowCount(0);
        for (Despacho.Item item : itemsDespacho) {
            modelItems.addRow(new Object[]{item.getNombreProducto(), item.getCantidad()});
        }
    }

    // ===== ACCIONES =====

    private void onAgregarItem() {
        int sel = tablaProductos.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un producto.");
            return;
        }
        int modeloIndex = tablaProductos.convertRowIndexToModel(sel);
        String nombre   = (String) modelProductos.getValueAt(modeloIndex, 0);
        String sCantidad = txtCantidad.getText().trim();
        int cantidad;
        try {
            cantidad = Integer.parseInt(sCantidad);
            if (cantidad <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida.");
            return;
        }
        ProductoTerminado producto = buscarProductoTerminadoPorNombre(nombre);
        if (producto == null || cantidad > producto.getCantidad()) {
            JOptionPane.showMessageDialog(this,
                "Stock insuficiente. Disponible: " +
                (producto != null ? producto.getCantidad() : 0));
            return;
        }
        itemsDespacho.add(new Despacho.Item(nombre, cantidad));
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

        // Verificar que se haya seleccionado un cliente del combo
        Cliente clienteSeleccionado = (Cliente) cmbCliente.getSelectedItem();
        if (clienteSeleccionado == null) {
            JOptionPane.showMessageDialog(this,
                "Debe seleccionar un cliente del listado.\n" +
                "Si el cliente no existe, use 'Gestionar Clientes' para crearlo.");
            return;
        }

        try {
            if (despachoActual == null) despachoActual = new Despacho();

            despachoActual.setClienteNombre(clienteSeleccionado.getNombre());
            despachoActual.setClienteNIT(clienteSeleccionado.getNit());
            despachoActual.setClienteTelefono(clienteSeleccionado.getTelefono());
            despachoActual.setClienteDireccion(clienteSeleccionado.getDireccion());
            despachoActual.setClienteCiudad(clienteSeleccionado.getCiudad());
            despachoActual.setNotas(areaNotas.getText().trim());
            despachoActual.setFechaHora(LocalDateTime.now());
            
            // Capturar fecha de entrega del spinner
            Date fechaEntregaDate = (Date) spinnerFechaEntrega.getValue();
            if (fechaEntregaDate != null) {
                LocalDateTime fechaEntregaLocal = Instant.ofEpochMilli(fechaEntregaDate.getTime())
                    .atZone(ZoneId.systemDefault()).toLocalDateTime();
                despachoActual.setFechaEntrega(fechaEntregaLocal);
            }
            
            despachoActual.setItems(new ArrayList<>(itemsDespacho));

            DataStore.registrarDespacho(despachoActual);

            JOptionPane.showMessageDialog(this,
                "Despacho registrado exitosamente\nRemisión: " +
                despachoActual.getNumeroRemision());

            crearNuevoDespacho();
            refreshProductos();
            refreshDespachos();

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