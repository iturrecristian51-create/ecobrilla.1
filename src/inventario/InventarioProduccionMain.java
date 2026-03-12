package inventario;

import java.util.ArrayList;
import javax.swing.*;

public class InventarioProduccionMain {

    public static void main(String[] args) {
        // Configurar Look and Feel - VERSIÓN CORREGIDA
        try {
            // Usar el Look and Feel del sistema (más compatible)
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            ThemeUtil.configurarEstiloGlobal();
        } catch (Exception e) {
            System.err.println("⚠️  No se pudo cargar el Look and Feel del sistema. Usando default.");
            e.printStackTrace();
        }
// Cargar productos existentes
ArrayList<Producto> productosExistentes = DataStore.getProductosFormulas();
for (Producto p : productosExistentes) {
    ListaProductos.agregarProducto(p);
}
        // ✅ INICIALIZAR SQLITE - Esto ahora incluye migración automática
        DataStore.init();

        // ✅ ELIMINAR LOGIN - Configurar usuario automáticamente
        configurarUsuarioAutomatico();

        // === Abrir la ventana principal DIRECTAMENTE ===
        javax.swing.SwingUtilities.invokeLater(() -> {
            new MenuPrincipalGUI().setVisible(true);
            System.out.println("✅ Sistema iniciado sin login - Usuario: " + 
                DataStore.getUsuarioActual().getNombreUsuario());
        });
    }

    // ✅ MÉTODO PARA CONFIGURAR USUARIO AUTOMÁTICAMENTE
    private static void configurarUsuarioAutomatico() {
        System.out.println("🔧 Configurando auto-login...");
        
        // Verificar si ya hay un usuario actual
        if (DataStore.getUsuarioActual() != null) {
            System.out.println("✅ Usuario actual ya configurado: " + 
                DataStore.getUsuarioActual().getNombreUsuario());
            return;
        }
        
        // Intentar usar el primer usuario disponible
        if (!DataStore.getListaUsuarios().isEmpty()) {
            Usuario primerUsuario = DataStore.getListaUsuarios().get(0);
            DataStore.setUsuarioActual(primerUsuario);
            System.out.println("✅ Usando usuario existente: " + primerUsuario.getNombreUsuario());
        } else {
            // Crear usuario temporal si no hay usuarios
            Usuario tempUser = new Usuario("admin", "admin123", "Administrador");
            DataStore.setUsuarioActual(tempUser);
            System.out.println("✅ Usuario temporal creado: admin");
        }
        
        System.out.println("🎯 Rol del usuario: " + DataStore.getUsuarioActual().getRol());
    }
}