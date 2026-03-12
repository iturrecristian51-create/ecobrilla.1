package inventario;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

public class LoginDialog extends JDialog {
    private final JTextField tfUsuario;
    private final JPasswordField pfContrasena;
    private final JButton btnIngresar;
    private final JButton btnCancelar;
    private boolean autenticado;

    public LoginDialog(Frame parent) {
        super(parent, "Inicio de sesión", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        tfUsuario = new JTextField(18);
        pfContrasena = new JPasswordField(18);
        btnIngresar = new JButton("Ingresar");
        btnCancelar = new JButton("Cancelar");

        construirLayout();
        registrarEventos();

        pack();
        setResizable(false);
        setLocationRelativeTo(parent);
    }

    private void construirLayout() {
        JPanel panelCampos = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0;
        gbc.gridy = 0;
        panelCampos.add(new JLabel("Usuario:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCampos.add(tfUsuario, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panelCampos.add(new JLabel("Contraseña:"), gbc);

        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panelCampos.add(pfContrasena, gbc);

        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelBotones.add(btnCancelar);
        panelBotones.add(btnIngresar);

        setLayout(new BorderLayout(10, 10));
        add(panelCampos, BorderLayout.CENTER);
        add(panelBotones, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(btnIngresar);
    }

    private void registrarEventos() {
        btnIngresar.addActionListener(e -> intentarLogin());
        btnCancelar.addActionListener(e -> cancelar());

        getRootPane().registerKeyboardAction(
            new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    cancelar();
                }
            },
            KeyStroke.getKeyStroke("ESCAPE"),
            JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private void intentarLogin() {
        String usuario = tfUsuario.getText().trim();
        String contrasena = new String(pfContrasena.getPassword());

        if (usuario.isEmpty() || contrasena.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debes completar usuario y contraseña.");
            return;
        }

        if (DataStore.autenticar(usuario, contrasena)) {
            autenticado = true;
            dispose();
            return;
        }

        JOptionPane.showMessageDialog(this, "Usuario o contraseña inválidos.");
        pfContrasena.setText("");
        pfContrasena.requestFocusInWindow();
    }

    private void cancelar() {
        autenticado = false;
        dispose();
    }

    public boolean fueAutenticado() {
        return autenticado;
    }
}
