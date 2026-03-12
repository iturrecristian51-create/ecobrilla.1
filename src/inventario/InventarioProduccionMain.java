package inventario;

import java.util.ArrayList;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class InventarioProduccionMain {

    public static void main(String[] args) {
        configurarLookAndFeel();

        DataStore.init();
        cargarProductosExistentes();

        SwingUtilities.invokeLater(() -> {
            LoginDialog login = new LoginDialog(null);
            login.setVisible(true);

            if (!login.fueAutenticado()) {
                System.exit(0);
                return;
            }

            new MenuPrincipalGUI().setVisible(true);
        });
    }

    private static void configurarLookAndFeel() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            ThemeUtil.configurarEstiloGlobal();
        } catch (Exception e) {
            System.err.println("⚠️ No se pudo cargar el Look and Feel del sistema. Se usará el predeterminado.");
        }
    }

    private static void cargarProductosExistentes() {
        ArrayList<Producto> productosExistentes = DataStore.getProductosFormulas();
        for (Producto producto : productosExistentes) {
            ListaProductos.agregarProducto(producto);
        }
    }
}
