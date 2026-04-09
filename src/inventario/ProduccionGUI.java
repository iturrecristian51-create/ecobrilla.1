package inventario;

import java.awt.*;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ProduccionGUI extends JDialog {
    private Lote loteActual;
    private DefaultTableModel modelInsumos;
    private DefaultTableModel modelLotes;
    private JComboBox<String> comboInsumos;
    private JComboBox<Producto> comboProductos;  // ✅ Cambio a JComboBox<Producto>
    private JTextField txtUnidades;
    private JTextField txtIdLote;
    private JTextField txtFecha;
    private JTextField txtCantInsumo;
    private JTable tablaLotes;
    private JTable tInsumos;  // ✅ Variable de clase para poder accederla en eliminarInsumoSeleccionado()

    private DespachoGUI despachoGUI;

    public ProduccionGUI(JFrame parent, DespachoGUI despachoGUI) {
        super(parent, "Gestión de Producción", true);
        this.despachoGUI = despachoGUI;
        setSize(1050, 600);
        setLocationRelativeTo(parent);
        initUI();
    }

    private void initUI() {
        JTabbedPane tabsProduccion = new JTabbedPane();

        // === PANEL PRODUCCIÓN ===
        JPanel panelProduccion = new JPanel(new BorderLayout());

        // Panel izquierdo (formulario)
        JPanel left = new JPanel(new GridLayout(0, 2, 8, 8));
        left.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        txtIdLote = new JTextField();
        comboProductos = new JComboBox<>();
        refreshComboProductos();

        txtFecha = new JTextField(java.time.LocalDate.now().toString());
        txtUnidades = new JTextField();
        comboInsumos = new JComboBox<>();
        refreshComboInsumos();
        txtCantInsumo = new JTextField();

        JButton btnCrear = new JButton("Crear lote temporal");
        JButton btnContinuar = new JButton("Continuar lote seleccionado");
        JButton btnAgregarInsumo = new JButton("Agregar insumo");
        JButton btnEliminarInsumo = new JButton("Eliminar insumo seleccionado");
        JButton btnFinalizar = new JButton("Finalizar y Guardar Lote");

        left.add(new JLabel("ID del Lote:")); left.add(txtIdLote);
        left.add(new JLabel("Producto:")); left.add(comboProductos);
        left.add(new JLabel("Fecha:")); left.add(txtFecha);
        left.add(new JLabel("Unidades a producir:")); left.add(txtUnidades);
        left.add(new JLabel("Insumo:")); left.add(comboInsumos);
        left.add(new JLabel("Cantidad insumo:")); left.add(txtCantInsumo);
        left.add(btnCrear); left.add(btnContinuar);
        left.add(btnAgregarInsumo); left.add(btnEliminarInsumo);
        left.add(new JLabel()); left.add(btnFinalizar);

        // Panel central (insumos del lote actual)
        // ✅ CAMBIO: Permitir editar solo la columna 'Cantidad' (índice 1)
        modelInsumos = new DefaultTableModel(new String[]{"Insumo", "Cantidad"}, 0) {
            @Override public boolean isCellEditable(int r, int c){
                return c == 1;  // Solo la columna 'Cantidad' es editable
            }
        };
        tInsumos = new JTable(modelInsumos);  // ✅ Usar variable de clase
        JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createTitledBorder("Insumos del Lote Actual"));
        center.add(new JScrollPane(tInsumos), BorderLayout.CENTER);

        // Panel derecho (lista de lotes existentes)
        modelLotes = new DefaultTableModel(new String[]{"ID Lote", "Producto", "Fecha", "Estado", "Unidades"}, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        tablaLotes = new JTable(modelLotes);
        
        // Agregar menú contextual para eliminar
        JPopupMenu menuContextual = new JPopupMenu();
        JMenuItem menuEliminar = new JMenuItem("Eliminar Lote");
        menuEliminar.addActionListener(e -> eliminarLoteSeleccionado());
        menuContextual.add(menuEliminar);
        
        tablaLotes.setComponentPopupMenu(menuContextual);
        
        refreshTablaLotes();

        JPanel right = new JPanel(new BorderLayout());
        right.setBorder(BorderFactory.createTitledBorder("Lotes Existentes - Click derecho para eliminar"));
        right.add(new JScrollPane(tablaLotes), BorderLayout.CENTER);

        JPanel top = new JPanel(new GridLayout(1, 3, 8, 8));
        top.add(left);
        top.add(center);
        top.add(right);

        // Botones inferiores
        JPanel bottom = new JPanel();
        JButton btnCerrar = new JButton("Cerrar");
        bottom.add(btnCerrar);

        panelProduccion.add(top, BorderLayout.CENTER);
        panelProduccion.add(bottom, BorderLayout.SOUTH);

        tabsProduccion.addTab("Gestión de Producción", panelProduccion);

        // Pestaña de rótulos
        RotuloPanel rotuloPanel = new RotuloPanel();
        tabsProduccion.addTab("Rótulos", rotuloPanel);

        add(tabsProduccion, BorderLayout.CENTER);

        // === ACCIONES ===
        // ✅ ActionListener en comboProductos para actualizar tabla de insumos automáticamente
        comboProductos.addActionListener(e -> actualizarTablaInsumos());
        
        btnCrear.addActionListener(e -> crearLote());
        btnContinuar.addActionListener(e -> continuarLoteSeleccionado());
        btnAgregarInsumo.addActionListener(e -> agregarInsumo());
        btnEliminarInsumo.addActionListener(e -> eliminarInsumoSeleccionado());
        btnFinalizar.addActionListener(e -> finalizarLote());
        btnCerrar.addActionListener(e -> dispose());
    }

    private void crearLote() {
        String idLote = txtIdLote.getText().trim();
        String producto = (String) comboProductos.getSelectedItem();
        String fecha = txtFecha.getText().trim();

        if (idLote.isEmpty() || producto == null) {
            JOptionPane.showMessageDialog(this, "Completa todos los campos antes de crear el lote.");
            return;
        }

        // Verificar que el lote no exista ya
        for (Lote l : DataStore.lotes) {
            if (l.getIdLote().equals(idLote)) {
                JOptionPane.showMessageDialog(this, "El lote ID '" + idLote + "' ya existe. Usa otro ID.");
                return;
            }
        }

        loteActual = new Lote(idLote, producto, fecha);
        modelInsumos.setRowCount(0);
        JOptionPane.showMessageDialog(this, "Lote creado temporalmente. Estado: EN PROCESO\n\nAhora agrega insumos.");
    }

    // ✅ NUEVO MÉTODO: Continuar editando un lote existente en "En proceso"
    private void continuarLoteSeleccionado() {
        int fila = tablaLotes.getSelectedRow();
        if (fila == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un lote de la tabla para continuar editándolo.");
            return;
        }
        
        String idLote = (String) modelLotes.getValueAt(fila, 0);
        String estado = (String) modelLotes.getValueAt(fila, 3);
        
        // Solo se pueden continuar lotes en estado "En proceso"
        if (!estado.equals("En proceso")) {
            JOptionPane.showMessageDialog(this, 
                "Solo puedes continuar lotes en estado 'En proceso'.\n" +
                "Este lote está en estado: " + estado);
            return;
        }
        
        // Buscar el lote en DataStore
        Lote loteEncontrado = null;
        for (Lote l : DataStore.lotes) {
            if (l.getIdLote().equals(idLote)) {
                loteEncontrado = l;
                break;
            }
        }
        
        if (loteEncontrado == null) {
            JOptionPane.showMessageDialog(this, "Error: No se encontró el lote en el sistema.");
            return;
        }
        
        // Cargar el lote en loteActual
        loteActual = loteEncontrado;
        
        // Cargar los datos en el formulario
        txtIdLote.setText(loteActual.getIdLote());
        txtFecha.setText(loteActual.getFechaProduccion());
        comboProductos.setSelectedItem(loteActual.getNombreProducto());
        
        // Cargar los insumos en la tabla
        modelInsumos.setRowCount(0);
        for (java.util.Map.Entry<String, Double> entry : loteActual.getInsumosUsados().entrySet()) {
            modelInsumos.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
        
        JOptionPane.showMessageDialog(this, 
            "Lote cargado: " + idLote + "\n" +
            "Puedes seguir agregando insumos o cambiar la cantidad de unidades.");
    }

    private void agregarInsumo() {
        if (loteActual == null) {
            JOptionPane.showMessageDialog(this, "Primero crea un lote.");
            return;
        }

        String nombreInsumo = (String) comboInsumos.getSelectedItem();
        String cantidadTxt = txtCantInsumo.getText().trim();

        if (nombreInsumo == null || cantidadTxt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Selecciona un insumo y una cantidad válida.");
            return;
        }

        try {
            double cantidad = Double.parseDouble(cantidadTxt);
            if (cantidad <= 0) {
                JOptionPane.showMessageDialog(this, "La cantidad debe ser mayor a 0.");
                return;
            }

            loteActual.agregarInsumoUsado(nombreInsumo, cantidad);
            modelInsumos.addRow(new Object[]{nombreInsumo, cantidad});
            txtCantInsumo.setText("");

            // ✅ CORRECCIÓN 1: Guardar lote temporal en DataStore después de agregar el primer insumo
            // Esto asegura que aunque se cierre la ventana sin hacer clic en "Finalizar",
            // los insumos agregados no desaparezcan
            if (loteActual.getInsumosUsados().size() == 1) {  // Primera vez que se agrega un insumo
                DataStore.agregarLote(loteActual);
                JOptionPane.showMessageDialog(this, 
                    "Lote temporal guardado en el sistema con el primer insumo.\n" +
                    "Puedes seguir agregando insumos o finalizar después.");
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad inválida.");
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage());
        }
    }

    private void finalizarLote() {
        if (loteActual == null) {
            JOptionPane.showMessageDialog(this, "No hay un lote creado.");
            return;
        }

        int unidades;
        try {
            unidades = Integer.parseInt(txtUnidades.getText().trim());
            if (unidades <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Cantidad de unidades inválida.");
            return;
        }

        // Cambiar estado a "Terminado" y establecer unidades
        loteActual.setEstado("Terminado");
        loteActual.setUnidadesProducidas(unidades);

        // ✅ GUARDAR EN SQLITE (actualizar si ya existe como "En proceso", o agregar si es nuevo)
        // DataStore.agregarLote() internamente verifica si ya existe y lo actualiza
        DataStore.agregarLote(loteActual);
        DataStore.addOrUpdateProductoTerminado(loteActual, unidades);

        if (despachoGUI != null) {
            despachoGUI.refreshProductos();
        }

        JOptionPane.showMessageDialog(this, "Lote guardado como TERMINADO. Productos listos para despacho.");
        refreshTablaLotes();
        limpiarGUI();
    }

    private void eliminarLoteSeleccionado() {
    int fila = tablaLotes.getSelectedRow();
    if (fila == -1) {
        ConfirmDialogUtil.mostrarAdvertencia(this, "Selección Requerida", 
            "Seleccione un lote para eliminar.");
        return;
    }
    
    String idLote = (String) modelLotes.getValueAt(fila, 0);
    String producto = (String) modelLotes.getValueAt(fila, 1);
    
    boolean confirmado = ConfirmDialogUtil.confirmarEliminacion(this,
        "el lote: " + idLote,
        "Producto: " + producto + "<br>" +
        "Los insumos utilizados serán devueltos al stock automáticamente.");
        
    if (confirmado) {
        // ✅ USAR EL NUEVO MÉTODO CON RESTAURACIÓN DE STOCK
        boolean eliminado = DataStore.eliminarLoteConRestauracion(idLote);
        if (eliminado) {
            ConfirmDialogUtil.mostrarExito(this, "Lote Eliminado", 
                "Lote eliminado correctamente.<br>" +
                "Los insumos utilizados han sido devueltos al stock.");
            refreshTablaLotes();
            
            // Actualizar productos en despacho
            if (despachoGUI != null) {
                despachoGUI.refreshProductos();
            }
            
        } else {
            ConfirmDialogUtil.mostrarError(this, "Error", 
                "No se pudo eliminar el lote.");
        }
    }
}

    // ✅ CORRECCIÓN 2: Nuevo método para eliminar insumo seleccionado de la tabla
    private void eliminarInsumoSeleccionado() {
        if (loteActual == null) {
            JOptionPane.showMessageDialog(this, "No hay un lote creado.");
            return;
        }
        
        int filaSeleccionada = tInsumos.getSelectedRow();
        if (filaSeleccionada == -1) {
            JOptionPane.showMessageDialog(this, "Selecciona un insumo de la tabla para eliminar.");
            return;
        }
        
        String nombreInsumo = (String) modelInsumos.getValueAt(filaSeleccionada, 0);
        double cantidad = (Double) modelInsumos.getValueAt(filaSeleccionada, 1);
        
        // Eliminar del lote actual y devolver al stock
        boolean eliminado = loteActual.eliminarInsumoUsado(nombreInsumo);
        
        if (eliminado) {
            // Eliminar de la tabla
            modelInsumos.removeRow(filaSeleccionada);
            JOptionPane.showMessageDialog(this, 
                "Insumo '" + nombreInsumo + "' (x" + cantidad + ") eliminado.\n" +
                "El stock ha sido devuelto automáticamente.");
        } else {
            JOptionPane.showMessageDialog(this, "Error al eliminar el insumo.");
        }
    }

    private void refreshTablaLotes() {
        modelLotes.setRowCount(0);
        for (Lote lote : DataStore.lotes) {
            modelLotes.addRow(new Object[]{
                lote.getIdLote(),
                lote.getNombreProducto(),
                lote.getFechaProduccion(),
                lote.getEstado(),
                lote.getUnidadesProducidas()
            });
        }
    }

    private void limpiarGUI() {
        loteActual = null;
        modelInsumos.setRowCount(0);
        txtIdLote.setText("");
        txtUnidades.setText("");
        txtCantInsumo.setText("");
        refreshComboInsumos();
    }

    private void refreshComboInsumos() {
        comboInsumos.removeAllItems();
        List<Insumo> insumos = DataStore.getInsumosDisponibles();
        for (Insumo ins : insumos) {
            comboInsumos.addItem(ins.getNombre());
        }
    }

    private void refreshComboProductos() {
        comboProductos.removeAllItems();
        for (Producto p : ListaProductos.obtenerProductos()) {
            comboProductos.addItem(p);  // ✅ Agregar el objeto Producto, no solo el nombre
        }
    }

    // ✅ NUEVO MÉTODO: Actualizar tabla de insumos cuando se selecciona un producto
    private void actualizarTablaInsumos() {
        modelInsumos.setRowCount(0);  // ✅ Limpiar la tabla
        
        Producto productoSeleccionado = (Producto) comboProductos.getSelectedItem();
        if (productoSeleccionado == null) {
            return;
        }
        
        // ✅ Obtener insumos del producto seleccionado
        java.util.Map<String, Double> insumos = productoSeleccionado.getInsumosRequeridos();
        if (insumos == null || insumos.isEmpty()) {
            return;
        }
        
        // ✅ Cargar insumos en la tabla (editable la cantidad)
        for (java.util.Map.Entry<String, Double> entry : insumos.entrySet()) {
            modelInsumos.addRow(new Object[]{entry.getKey(), entry.getValue()});
        }
    }
}