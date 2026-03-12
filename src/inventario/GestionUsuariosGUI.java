package inventario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class GestionUsuariosGUI extends JDialog {

    private DefaultTableModel model;

    public GestionUsuariosGUI(JFrame parent) {
        super(parent, "Gestión de Usuarios", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        model = new DefaultTableModel(new String[]{"Usuario", "Rol"}, 0);
        JTable table = new JTable(model);
        cargarTabla();

        JButton btnAgregar = new JButton("Agregar Usuario");
        JButton btnCerrar = new JButton("Cerrar");

        JPanel bottom = new JPanel();
        bottom.add(btnAgregar);
        bottom.add(btnCerrar);

        add(new JScrollPane(table), BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        // === Acciones ===
        btnAgregar.addActionListener(e -> agregarUsuario());
        btnCerrar.addActionListener(e -> dispose());
    }

    private void cargarTabla() {
        model.setRowCount(0);
        List<Usuario> lista = DataStore.getListaUsuarios();
        for (Usuario u : lista) {
            model.addRow(new Object[]{u.getNombreUsuario(), u.getRol()});
        }
    }

    private void agregarUsuario() {
        JTextField tfNombre = new JTextField();
        JPasswordField pfContrasena = new JPasswordField();
        JComboBox<String> cbRol = new JComboBox<>(new String[]{"Gerente", "Auxiliar"});

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Usuario:"));
        panel.add(tfNombre);
        panel.add(new JLabel("Contraseña:"));
        panel.add(pfContrasena);
        panel.add(new JLabel("Rol:"));
        panel.add(cbRol);

        int r = JOptionPane.showConfirmDialog(this, panel, "Nuevo Usuario", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            String nombre = tfNombre.getText().trim();
            String contrasena = new String(pfContrasena.getPassword());
            String rol = (String) cbRol.getSelectedItem();

            if (nombre.isEmpty() || contrasena.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Complete todos los campos.");
                return;
            }

            // ✅ GUARDAR EN SQLITE
            DataStore.agregarUsuario(new Usuario(nombre, contrasena, rol));
            JOptionPane.showMessageDialog(this, "Usuario agregado correctamente.");
            cargarTabla();
        }
    }
}