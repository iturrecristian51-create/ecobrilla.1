package inventario;

import javax.swing.*;
import java.awt.*;

public class LoginDialog extends JDialog {
    private JTextField tfUsuario;
    private JPasswordField pfContrasena;
    private JButton btnLogin;
    private boolean autenticado = false;

    public LoginDialog(JFrame parent) {
        super(parent, "Inicio de Sesión", true);
        setSize(300, 200);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(3, 2, 10, 10));

        JLabel lblUsuario = new JLabel("Usuario:");
        tfUsuario = new JTextField();
        JLabel lblContrasena = new JLabel("Contraseña:");
        pfContrasena = new JPasswordField();
        btnLogin = new JButton("Ingresar");

        add(lblUsuario); add(tfUsuario);
        add(lblContrasena); add(pfContrasena);
        add(new JLabel()); add(btnLogin);

        // ✅ AUTO-LOGIN: Ejecutar inmediatamente al crear el diálogo
        ejecutarAutoLogin();

        btnLogin.addActionListener(e -> {
            String usuario = tfUsuario.getText().trim();
            String contrasena = new String(pfContrasena.getPassword());

            // ✅ AUTENTICACIÓN CON SQLITE
            if (DataStore.autenticar(usuario, contrasena)) {
                autenticado = true;
                JOptionPane.showMessageDialog(this, "Bienvenido " + DataStore.getUsuarioActual().getRol());
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos");
            }
        });
    }

    // ✅ NUEVO MÉTODO: Auto-login automático
    private void ejecutarAutoLogin() {
        // Auto-login con usuario admin
        System.out.println("🔐 Ejecutando auto-login...");
        
        // Intentar autenticar automáticamente
        if (DataStore.autenticar("admin", "admin123")) {
            autenticado = true;
            System.out.println("✅ Auto-login exitoso con admin");
            dispose(); // Cerrar el diálogo automáticamente
        } else if (DataStore.autenticar("dev", "dev123")) {
            autenticado = true;
            System.out.println("✅ Auto-login exitoso con dev");
            dispose(); // Cerrar el diálogo automáticamente
        } else {
            System.out.println("❌ Auto-login falló, mostrando diálogo normal");
            // Si falla el auto-login, mostrar el diálogo normal
        }
    }

    public boolean fueAutenticado() { return autenticado; }
}