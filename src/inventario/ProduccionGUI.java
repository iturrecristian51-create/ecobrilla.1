package inventario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ProduccionGUI extends JDialog {
    private Lote loteActual;
    private DefaultTableModel modelInsumos;
    private DefaultTableModel modelLotes;
    private JComboBox<String> comboInsumos;
    private JComboBox<String> comboProductos;
    private JTextField txtUnidades;
    private JTextField txtIdLote;
    private JTextField txtFecha;
    private JTextField txtCantInsumo;
    private JTable tablaLotes;

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
        JButton btnAgregarInsumo = new JButton("Agregar insumo");
        JButton btnFinalizar = new JButton("Finalizar y Guardar Lote");

        left.add(new JLabel("ID del Lote:")); left.add(txtIdLote);
        left.add(new JLabel("Producto:")); left.add(comboProductos);
        left.add(new JLabel("Fecha:")); left.add(txtFecha);
        left.add(new JLabel("Unidades a producir:")); left.add(txtUnidades);
        left.add(new JLabel("Insumo:")); left.add(comboInsumos);
        left.add(new JLabel("Cantidad insumo:")); left.add(txtCantInsumo);
        left.add(btnCrear); left.add(btnAgregarInsumo);
        left.add(new JLabel()); left.add(btnFinalizar);

        // Panel central (insumos del lote actual)
        modelInsumos = new DefaultTableModel(new String[]{"Insumo", "Cantidad"}, 0) {
            @Override public boolean isCellEditable(int r, int c){ return false; }
        };
        JTable tInsumos = new JTable(modelInsumos);
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
        btnCrear.addActionListener(e -> crearLote());
        btnAgregarInsumo.addActionListener(e -> agregarInsumo());
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

        loteActual = new Lote(idLote, producto, fecha);
        modelInsumos.setRowCount(0);
        JOptionPane.showMessageDialog(this, "Lote creado temporalmente. Estado: EN PROCESO");
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

        // ✅ GUARDAR EN SQLITE
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
            comboProductos.addItem(p.getNombre());
        }
    }
}