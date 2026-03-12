/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventario;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class VentanaProductoGUI extends JFrame {
    private JTextField txtNombre;
    private JTextField txtDescripcion;
    private JTextField txtInsumo;
    private JTextField txtCantidad;
    private DefaultListModel<String> modeloLista;
    private Map<String, Double> insumos;

    public VentanaProductoGUI() {
        setTitle("Nuevo Producto");
        setSize(450, 400);
        setLayout(null);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(240, 248, 255));

        insumos = new HashMap<>();

        JLabel lblTitulo = new JLabel("Registrar Nuevo Producto");
        lblTitulo.setFont(new Font("Arial", Font.BOLD, 18));
        lblTitulo.setBounds(90, 10, 300, 30);
        add(lblTitulo);

        JLabel lblNombre = new JLabel("Nombre:");
        lblNombre.setBounds(30, 60, 100, 25);
        add(lblNombre);

        txtNombre = new JTextField();
        txtNombre.setBounds(130, 60, 250, 25);
        add(txtNombre);

        JLabel lblDesc = new JLabel("Descripción:");
        lblDesc.setBounds(30, 100, 100, 25);
        add(lblDesc);

        txtDescripcion = new JTextField();
        txtDescripcion.setBounds(130, 100, 250, 25);
        add(txtDescripcion);

        JLabel lblInsumo = new JLabel("Insumo:");
        lblInsumo.setBounds(30, 140, 100, 25);
        add(lblInsumo);

        txtInsumo = new JTextField();
        txtInsumo.setBounds(130, 140, 150, 25);
        add(txtInsumo);

        JLabel lblCantidad = new JLabel("Cant.:");
        lblCantidad.setBounds(290, 140, 50, 25);
        add(lblCantidad);

        txtCantidad = new JTextField();
        txtCantidad.setBounds(340, 140, 40, 25);
        add(txtCantidad);

        JButton btnAgregarInsumo = new JButton("Agregar");
        btnAgregarInsumo.setBounds(150, 180, 120, 30);
        add(btnAgregarInsumo);

        modeloLista = new DefaultListModel<>();
        JList<String> listaInsumos = new JList<>(modeloLista);
        JScrollPane scroll = new JScrollPane(listaInsumos);
        scroll.setBounds(30, 220, 350, 80);
        add(scroll);

        btnAgregarInsumo.addActionListener(e -> {
            String nombreInsumo = txtInsumo.getText();
            String cantidadStr = txtCantidad.getText();

            if (!nombreInsumo.isEmpty() && !cantidadStr.isEmpty()) {
                try {
                    double cantidad = Double.parseDouble(cantidadStr);
                    insumos.put(nombreInsumo, cantidad);
                    modeloLista.addElement(nombreInsumo + " → " + cantidad);
                    txtInsumo.setText("");
                    txtCantidad.setText("");
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Cantidad inválida.");
                }
            }
        });

        JButton btnGuardar = new JButton("Guardar Producto");
        btnGuardar.setBounds(120, 320, 180, 35);
        add(btnGuardar);

        btnGuardar.addActionListener(e -> {
            String nombre = txtNombre.getText();
            String desc = txtDescripcion.getText();

            if (nombre.isEmpty() || insumos.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Debe ingresar nombre e insumos.");
                return;
            }

          Producto nuevo = new Producto(nombre, desc);
for (String insumo : insumos.keySet()) {
    nuevo.agregarInsumo(insumo, insumos.get(insumo));
}

// ✅ GUARDAR EN BASE DE DATOS (esto sí persiste)
DataStore.guardarProductoFormula(nuevo);

// Si necesitas mantener lo antiguo:
ListaProductos.agregarProducto(nuevo);
// Si necesitas mantener compatibilidad, deja también la línea original:
ListaProductos.agregarProducto(nuevo);

JOptionPane.showMessageDialog(this,
    "Producto \"" + nombre + "\" guardado correctamente.",
    "Confirmación", JOptionPane.INFORMATION_MESSAGE);

dispose();

        });
    }
}
