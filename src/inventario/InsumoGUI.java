package inventario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.FileWriter;
import java.io.PrintWriter;

public class InsumoGUI extends JDialog {
    private DefaultTableModel tableModel;
    private JTable tablaInsumos;
    private JTable tablaHistorial;
    private DefaultTableModel modeloHistorial;
    private FiltroPanel filtroInsumos;
    private FiltroPanel filtroHistorial;

    public InsumoGUI(JFrame parent) {
        super(parent, "Gestión de Insumos", true);
        setSize(1100, 750);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        getContentPane().setBackground(ThemeUtil.COLOR_FONDO);
        
        JTabbedPane pestañas = new JTabbedPane();
        pestañas.setFont(ThemeUtil.FUENTE_NORMAL);
        getContentPane().add(pestañas);

        // ================== PANEL DE INSUMOS ==================
        JPanel panelInsumos = new JPanel(new BorderLayout());
        panelInsumos.setBackground(ThemeUtil.COLOR_FONDO);

        // Panel superior con título y filtro
        JPanel panelSuperiorInsumos = new JPanel(new BorderLayout());
        panelSuperiorInsumos.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelSuperiorInsumos.setBackground(ThemeUtil.COLOR_FONDO);
        
        JPanel panelTitulo = ThemeUtil.crearPanelTitulo("Gestión de Insumos");
        panelSuperiorInsumos.add(panelTitulo, BorderLayout.NORTH);
        
        filtroInsumos = new FiltroPanel("Buscar insumos por nombre, lote, proveedor...", this::aplicarFiltroInsumos);
        panelSuperiorInsumos.add(filtroInsumos, BorderLayout.CENTER);
        
        panelInsumos.add(panelSuperiorInsumos, BorderLayout.NORTH);

        // Panel central con formulario y tabla
        JPanel panelCentral = new JPanel(new BorderLayout(10, 10));
        panelCentral.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        panelCentral.setBackground(ThemeUtil.COLOR_FONDO);

        // Formulario en card
        JPanel cardFormulario = ThemeUtil.crearCardPanel();
        cardFormulario.setLayout(new BorderLayout());
        cardFormulario.add(crearFormularioInsumos(), BorderLayout.CENTER);
        panelCentral.add(cardFormulario, BorderLayout.NORTH);

        // Tabla en card
        JPanel cardTabla = ThemeUtil.crearCardPanel();
        cardTabla.setLayout(new BorderLayout());
        
        String[] cols = {"Lote", "Nombre", "Fecha", "Cantidad", "Unidad", "Proveedor", "Notas"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaInsumos = new JTable(tableModel);
        ThemeUtil.aplicarEstiloTabla(tablaInsumos);
        FiltradorUtil.configurarFiltroColumnas(tablaInsumos, cols);
        
        JScrollPane scrollTabla = new JScrollPane(tablaInsumos);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());
        cardTabla.add(scrollTabla, BorderLayout.CENTER);
        
        panelCentral.add(cardTabla, BorderLayout.CENTER);
        panelInsumos.add(panelCentral, BorderLayout.CENTER);

        // Panel inferior con botones
        JPanel panelInferior = crearPanelBotones();
        panelInsumos.add(panelInferior, BorderLayout.SOUTH);

        pestañas.addTab("📦 Gestión de Insumos", panelInsumos);

        // ================== PANEL DE HISTORIAL ==================
        JPanel panelHistorial = new JPanel(new BorderLayout());
        panelHistorial.setBackground(ThemeUtil.COLOR_FONDO);
        
        // Header del historial
        JPanel panelHeaderHistorial = new JPanel(new BorderLayout());
        panelHeaderHistorial.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelHeaderHistorial.setBackground(ThemeUtil.COLOR_FONDO);
        
        JPanel panelTituloHistorial = ThemeUtil.crearPanelTitulo("Historial de Movimientos");
        panelHeaderHistorial.add(panelTituloHistorial, BorderLayout.NORTH);
        
        filtroHistorial = new FiltroPanel("Buscar en historial por lote, nombre, acción...", this::aplicarFiltroHistorial);
        panelHeaderHistorial.add(filtroHistorial, BorderLayout.CENTER);
        
        panelHistorial.add(panelHeaderHistorial, BorderLayout.NORTH);

        // Tabla de historial en card
        JPanel cardHistorial = ThemeUtil.crearCardPanel();
        cardHistorial.setLayout(new BorderLayout());
        
        modeloHistorial = new DefaultTableModel(
                new Object[]{"Fecha", "Lote", "Nombre", "Acción", "Cantidad", "Observación"}, 0
        ) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tablaHistorial = new JTable(modeloHistorial);
        ThemeUtil.aplicarEstiloTabla(tablaHistorial);
        FiltradorUtil.configurarFiltroColumnas(tablaHistorial, 
            new String[]{"Fecha", "Lote", "Nombre", "Acción", "Cantidad", "Observación"});
        
        JScrollPane scrollHistorial = new JScrollPane(tablaHistorial);
        scrollHistorial.setBorder(BorderFactory.createEmptyBorder());
        cardHistorial.add(scrollHistorial, BorderLayout.CENTER);
        
        panelHistorial.add(cardHistorial, BorderLayout.CENTER);

        pestañas.addTab("📊 Historial de Insumos", panelHistorial);

        // Cargar datos iniciales
        refreshTable();
        cargarHistorialInsumos();
        actualizarContadores();
    }
private void eliminarInsumoSeleccionado() {
    int filaSeleccionada = tablaInsumos.getSelectedRow();
    if (filaSeleccionada == -1) {
        ConfirmDialogUtil.mostrarAdvertencia(this, "Selección Requerida", 
            "Por favor, seleccione un insumo para eliminar.");
        return;
    }
    
    int modeloIndex = tablaInsumos.convertRowIndexToModel(filaSeleccionada);
    Insumo insumo = DataStore.insumos.get(modeloIndex);
    
    // ✅ VERIFICAR SI EL INSUMO ESTÁ SIENDO USADO EN LOTES
    double cantidadUtilizada = DataStore.obtenerCantidadUtilizadaEnLotes(insumo.getNombre());
    
    if (cantidadUtilizada > 0) {
        ConfirmDialogUtil.mostrarError(this, "Insumo en Uso", 
            "No se puede eliminar este insumo porque está siendo utilizado en lotes de producción.<br>" +
            "Cantidad utilizada: " + cantidadUtilizada + " " + insumo.getUnidad() + "<br><br>" +
            "Primero debe eliminar los lotes que utilizan este insumo.");
        return;
    }
    
    boolean confirmado = ConfirmDialogUtil.confirmarEliminacion(this,
        "el insumo: " + insumo.getNombre(),
        "Lote: " + insumo.getLote() + "<br>" +
        "Cantidad actual: " + insumo.getCantidad() + " " + insumo.getUnidad());
        
    if (confirmado) {
        boolean eliminado = DataStore.eliminarInsumo(insumo.getLote());
        
        if (eliminado) {
            refreshTable();
            cargarHistorialInsumos(); // ✅ ACTUALIZAR HISTORIAL
            actualizarContadores();
            
            ConfirmDialogUtil.mostrarExito(this, "Insumo Eliminado", 
                "El insumo se ha eliminado correctamente del sistema.");
        } else {
            ConfirmDialogUtil.mostrarError(this, "Error", 
                "No se pudo eliminar el insumo.");
        }
    }
}
    private void aplicarFiltroInsumos() {
        String filtro = filtroInsumos.getTextoFiltro();
        FiltradorUtil.aplicarFiltroTabla(tablaInsumos, filtro);
        actualizarContadores();
    }

    private void aplicarFiltroHistorial() {
        String filtro = filtroHistorial.getTextoFiltro();
        FiltradorUtil.aplicarFiltroTabla(tablaHistorial, filtro);
        actualizarContadores();
    }

    private void actualizarContadores() {
        int totalInsumos = DataStore.insumos.size();
        int insumosFiltrados = filtroInsumos.getTextoFiltro().isEmpty() ? 
            totalInsumos : tablaInsumos.getRowCount();
        filtroInsumos.actualizarContador(totalInsumos, insumosFiltrados);
        
        int totalHistorial = DataStore.historial.size();
        int historialFiltrado = filtroHistorial.getTextoFiltro().isEmpty() ? 
            totalHistorial : tablaHistorial.getRowCount();
        filtroHistorial.actualizarContador(totalHistorial, historialFiltrado);
    }

     private JPanel crearFormularioInsumos() {
    JPanel form = new JPanel(new GridBagLayout());
    form.setBackground(Color.WHITE);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(8, 8, 8, 8);
    gbc.fill = GridBagConstraints.HORIZONTAL;

    JTextField txtLote = new JTextField();
    JTextField txtNombre = new JTextField();
    JTextField txtFecha = new JTextField(java.time.LocalDate.now().toString());
    JTextField txtCantidad = new JTextField();
    JTextField txtUnidad = new JTextField();
    JTextField txtProveedor = new JTextField();
    JTextArea txtNotas = new JTextArea(3, 20);
    JScrollPane notasScroll = new JScrollPane(txtNotas);
    
    // Aplicar estilos a los campos
    ThemeUtil.aplicarEstiloCampo(txtLote);
    ThemeUtil.aplicarEstiloCampo(txtNombre);
    ThemeUtil.aplicarEstiloCampo(txtFecha);
    ThemeUtil.aplicarEstiloCampo(txtCantidad);
    ThemeUtil.aplicarEstiloCampo(txtUnidad);
    ThemeUtil.aplicarEstiloCampo(txtProveedor);
    ThemeUtil.aplicarEstiloAreaTexto(txtNotas);

    JButton btnAdd = new JButton("➕ Agregar Insumo");
    ThemeUtil.aplicarEstiloPrimario(btnAdd);

    // Primera fila
    gbc.gridx = 0; gbc.gridy = 0; 
    form.add(crearLabel("Lote:"), gbc);
    gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.5; 
    form.add(txtLote, gbc);
    gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0; 
    form.add(crearLabel("Nombre:"), gbc);
    gbc.gridx = 3; gbc.gridy = 0; gbc.weightx = 1; 
    form.add(txtNombre, gbc);

    // Segunda fila
    gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0; 
    form.add(crearLabel("Fecha:"), gbc);
    gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1; 
    form.add(txtFecha, gbc);
    gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0; 
    form.add(crearLabel("Cantidad:"), gbc);
    gbc.gridx = 3; gbc.gridy = 1; gbc.weightx = 1; 
    form.add(txtCantidad, gbc);
    gbc.gridx = 4; gbc.gridy = 1; gbc.weightx = 0; 
    form.add(crearLabel("Unidad:"), gbc);
    gbc.gridx = 5; gbc.gridy = 1; gbc.weightx = 1; 
    form.add(txtUnidad, gbc);

    // Tercera fila
    gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0; 
    form.add(crearLabel("Proveedor:"), gbc);
    gbc.gridx = 1; gbc.gridy = 2; gbc.gridwidth = 5; gbc.weightx = 1; 
    form.add(txtProveedor, gbc);
    gbc.gridwidth = 1;

    // Cuarta fila
    gbc.gridx = 0; gbc.gridy = 3; 
    form.add(crearLabel("Notas:"), gbc);
    gbc.gridx = 1; gbc.gridy = 3; gbc.gridwidth = 4; gbc.fill = GridBagConstraints.BOTH;
    form.add(notasScroll, gbc);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.gridx = 5; gbc.gridy = 3; gbc.gridwidth = 1; 
    form.add(btnAdd, gbc);

    // ✅ ACTION LISTENER FALTANTE - AGREGAR ESTO:
   btnAdd.addActionListener(e -> {
    try {
        String lote = txtLote.getText().trim();
        String nombre = txtNombre.getText().trim();
        String fecha = txtFecha.getText().trim();
        double cantidad = Double.parseDouble(txtCantidad.getText().trim());
        String unidad = txtUnidad.getText().trim();
        String proveedor = txtProveedor.getText().trim();
        String notas = txtNotas.getText().trim();

        if (lote.isEmpty() || nombre.isEmpty()) {
            ConfirmDialogUtil.mostrarAdvertencia(this, "Campos Obligatorios", 
                "Lote y Nombre son campos obligatorios.");
            return;
        }

        Insumo ins = new Insumo(lote, nombre, fecha, cantidad, unidad, proveedor, notas);
        
        // ✅ GUARDAR EN SQLITE
        boolean guardado = DataStore.guardarInsumo(ins);
        
        if (guardado) {
            // ✅ REGISTRAR MOVIMIENTO EN HISTORIAL
            DataStore.registrarMovimiento(ins.getLote(), ins.getNombre(), "Entrada", cantidad, 
                notas.isEmpty() ? "Ingreso manual" : notas);

            refreshTable();
            
            // ✅ FORZAR ACTUALIZACIÓN DEL HISTORIAL EN LA INTERFAZ
            cargarHistorialInsumos();
            actualizarContadores();

            // Limpiar campos
            txtLote.setText("");
            txtNombre.setText("");
            txtCantidad.setText("");
            txtUnidad.setText("");
            txtProveedor.setText("");
            txtNotas.setText("");
            
            ConfirmDialogUtil.mostrarExito(this, "Insumo Agregado", 
                "El insumo se ha agregado correctamente al inventario.");
        } else {
            ConfirmDialogUtil.mostrarError(this, "Error", 
                "No se pudo guardar el insumo.");
        }

    } catch (NumberFormatException ex) {
        ConfirmDialogUtil.mostrarError(this, "Error de Datos", 
            "La cantidad debe ser un número válido.");
    }
});

    return form;
}
    private JLabel crearLabel(String texto) {
        JLabel label = new JLabel(texto);
        label.setFont(ThemeUtil.FUENTE_NORMAL);
        label.setForeground(ThemeUtil.COLOR_TEXTO_SECUNDARIO);
        return label;
    }

    private JPanel crearPanelBotones() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottom.setBackground(ThemeUtil.COLOR_FONDO);
        
        JButton btnEliminar = new JButton("🗑️ Eliminar Seleccionado");
        ThemeUtil.aplicarEstiloPeligro(btnEliminar);
        btnEliminar.addActionListener(e -> eliminarInsumoSeleccionado());
        
        JButton btnExport = new JButton("📤 Exportar CSV");
        ThemeUtil.aplicarEstiloSecundario(btnExport);
        
        JButton btnClose = new JButton("❌ Cerrar");
        ThemeUtil.aplicarEstiloSecundario(btnClose);
        
        bottom.add(btnEliminar);
        bottom.add(btnExport);
        bottom.add(btnClose);

        
        
        
        btnExport.addActionListener(e -> {
            try {
                FileWriter fw = new FileWriter("data/insumos_export.csv");
                PrintWriter pw = new PrintWriter(fw);
                pw.println("Lote,Nombre,Fecha,Cantidad,Unidad,Proveedor,Notas");
                for (Insumo ins : DataStore.getListaInsumos()) {
                    pw.printf("\"%s\",\"%s\",\"%s\",%s,\"%s\",\"%s\",\"%s\"\n",
                            ins.getLote(), ins.getNombre(), ins.getFechaIngreso(),
                            ins.getCantidad(), ins.getUnidad(),
                            ins.getProveedor(), ins.getNotas());
                }
                pw.close();
                ConfirmDialogUtil.mostrarExito(this, "Exportación Exitosa", 
                    "Los datos se han exportado correctamente a: data/insumos_export.csv");
            } catch (Exception ex) {
                ConfirmDialogUtil.mostrarError(this, "Error en Exportación", 
                    "Error al exportar datos: " + ex.getMessage());
            }
        });

        btnClose.addActionListener(e -> dispose());
        
        return bottom;
    }

    

   private void refreshTable() {
    tableModel.setRowCount(0);
    
    // ✅ Asegurarnos de que los datos están actualizados
    DataStore.recargarInsumos();
    
    for (Insumo ins : DataStore.getListaInsumos()) {
        tableModel.addRow(new Object[]{
                ins.getLote(), 
                ins.getNombre(), 
                ins.getFechaIngreso(),
                ins.getCantidad(), 
                ins.getUnidad(), 
                ins.getProveedor(), 
                ins.getNotas()
        });
    }
    
    // Solo limpiar filtro si no hay texto de filtro activo
    if (filtroInsumos.getTextoFiltro().isEmpty()) {
        aplicarFiltroInsumos();
    }
}

    private void cargarHistorialInsumos() {
        modeloHistorial.setRowCount(0);
        for (HistorialInsumo h : DataStore.historial) {
            modeloHistorial.addRow(new Object[]{
                    h.getFecha(),
                    h.getLote(),
                    h.getNombreInsumo(),
                    h.getAccion(),
                    h.getCantidad(),
                    h.getObservacion()
            });
        }
        filtroHistorial.limpiarFiltro();
    }
}