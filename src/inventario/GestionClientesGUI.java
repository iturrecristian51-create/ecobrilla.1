package inventario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Ventana para crear, editar y eliminar clientes.
 * Se abre desde el menú principal o desde DespachoGUI.
 */
public class GestionClientesGUI extends JDialog {

    private DefaultTableModel model;
    private JTable tabla;

    public GestionClientesGUI(JFrame parent) {
        super(parent, "Gestión de Clientes", true);
        setSize(700, 450);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // ── Tabla ──────────────────────────────────────────────────────────
        model = new DefaultTableModel(
                new String[]{"ID", "Nombre", "NIT", "Teléfono", "Dirección", "Ciudad"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        tabla = new JTable(model);
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.setRowHeight(24);
        tabla.getColumnModel().getColumn(0).setMaxWidth(50); // ID pequeño
        cargarTabla();
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        // ── Botones ────────────────────────────────────────────────────────
        JPanel panelBtns = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 8));
        JButton btnNuevo   = new JButton("➕ Nuevo");
        JButton btnEditar  = new JButton("✏️ Editar");
        JButton btnEliminar= new JButton("🗑️ Eliminar");
        JButton btnCerrar  = new JButton("Cerrar");

        btnEliminar.setForeground(Color.RED);
        panelBtns.add(btnNuevo);
        panelBtns.add(btnEditar);
        panelBtns.add(btnEliminar);
        panelBtns.add(btnCerrar);
        add(panelBtns, BorderLayout.SOUTH);

        // ── Listeners ──────────────────────────────────────────────────────
        btnNuevo.addActionListener(e    -> abrirFormulario(null));
        btnEditar.addActionListener(e   -> {
            Cliente c = getClienteSeleccionado();
            if (c != null) abrirFormulario(c);
        });
        btnEliminar.addActionListener(e -> eliminarCliente());
        btnCerrar.addActionListener(e   -> dispose());

        // Doble clic para editar
        tabla.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    Cliente c = getClienteSeleccionado();
                    if (c != null) abrirFormulario(c);
                }
            }
        });
    }

    // ── Cargar tabla ───────────────────────────────────────────────────────
    public void cargarTabla() {
        model.setRowCount(0);
        for (Cliente c : ClienteDAO.listarTodos()) {
            model.addRow(new Object[]{
                c.getId(), c.getNombre(), c.getNit(),
                c.getTelefono(), c.getDireccion(), c.getCiudad()
            });
        }
    }

    // ── Obtener cliente seleccionado ───────────────────────────────────────
    private Cliente getClienteSeleccionado() {
        int fila = tabla.getSelectedRow();
        if (fila < 0) {
            JOptionPane.showMessageDialog(this, "Seleccione un cliente.");
            return null;
        }
        int id = (int) model.getValueAt(fila, 0);
        for (Cliente c : ClienteDAO.listarTodos()) {
            if (c.getId() == id) return c;
        }
        return null;
    }

    // ── Formulario nuevo/editar ────────────────────────────────────────────
    private void abrirFormulario(Cliente clienteExistente) {
        boolean esNuevo = (clienteExistente == null);
        String titulo   = esNuevo ? "Nuevo Cliente" : "Editar Cliente";

        JTextField txtNombre    = new JTextField(esNuevo ? "" : clienteExistente.getNombre());
        JTextField txtNit       = new JTextField(esNuevo ? "" : clienteExistente.getNit());
        JTextField txtTelefono  = new JTextField(esNuevo ? "" : clienteExistente.getTelefono());
        JTextField txtDireccion = new JTextField(esNuevo ? "" : clienteExistente.getDireccion());
        JTextField txtCiudad    = new JTextField(esNuevo ? "" : clienteExistente.getCiudad());

        JPanel form = new JPanel(new GridLayout(5, 2, 6, 8));
        form.add(new JLabel("Nombre:"));    form.add(txtNombre);
        form.add(new JLabel("NIT:"));       form.add(txtNit);
        form.add(new JLabel("Teléfono:"));  form.add(txtTelefono);
        form.add(new JLabel("Dirección:")); form.add(txtDireccion);
        form.add(new JLabel("Ciudad:"));    form.add(txtCiudad);

        int res = JOptionPane.showConfirmDialog(
                this, form, titulo, JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        String nombre    = txtNombre.getText().trim();
        String nit       = txtNit.getText().trim();
        String telefono  = txtTelefono.getText().trim();
        String direccion = txtDireccion.getText().trim();
        String ciudad    = txtCiudad.getText().trim();

        if (nombre.isEmpty() || nit.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nombre y NIT son obligatorios.");
            return;
        }

        Cliente c = esNuevo ? new Cliente() : clienteExistente;
        c.setNombre(nombre);
        c.setNit(nit);
        c.setTelefono(telefono);
        c.setDireccion(direccion);
        c.setCiudad(ciudad);

        boolean ok = ClienteDAO.guardar(c);
        if (ok) {
            JOptionPane.showMessageDialog(this,
                esNuevo ? "Cliente creado correctamente." : "Cliente actualizado.");
            cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this,
                "Error al guardar. Verifique que el NIT no esté duplicado.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // ── Eliminar ───────────────────────────────────────────────────────────
    private void eliminarCliente() {
        Cliente c = getClienteSeleccionado();
        if (c == null) return;

        int confirm = JOptionPane.showConfirmDialog(this,
            "¿Eliminar al cliente '" + c.getNombre() + "'?",
            "Confirmar", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        if (ClienteDAO.eliminar(c.getId())) {
            JOptionPane.showMessageDialog(this, "Cliente eliminado.");
            cargarTabla();
        } else {
            JOptionPane.showMessageDialog(this, "No se pudo eliminar.",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
