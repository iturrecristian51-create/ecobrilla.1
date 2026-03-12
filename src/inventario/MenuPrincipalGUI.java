package inventario;

import javax.swing.*;
import java.awt.*;

public class MenuPrincipalGUI extends JFrame {

    public MenuPrincipalGUI() {
        setTitle("🏭 Sistema de Inventario y Producción - ECOBRILLA");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 450); // Aumentado para mejor distribución
        setLocationRelativeTo(null);
        getContentPane().setBackground(ThemeUtil.COLOR_FONDO);
        
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());
        
        // Header con logo y título
        JPanel header = crearHeader();
        add(header, BorderLayout.NORTH);
        
        // Panel central con botones
        JPanel panelCentral = crearPanelCentral();
        add(panelCentral, BorderLayout.CENTER);
        
        // Footer con información
        JPanel footer = crearFooter();
        add(footer, BorderLayout.SOUTH);

        // Menú para desarrollador
        if (DataStore.getUsuarioActual() != null && 
            DataStore.getUsuarioActual().esDesarrollador()) {
            crearMenuDesarrollador();
        }
    }
    
    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(ThemeUtil.COLOR_PRIMARIO);
        header.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titulo = new JLabel("Sistema de Inventario y Producción", JLabel.CENTER);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titulo.setForeground(Color.WHITE);
        
        JLabel subtitulo = new JLabel("ECOBRILLA SOLUCIONES S.A.S", JLabel.CENTER);
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitulo.setForeground(new Color(240, 240, 240));
        
        JPanel panelTitulos = new JPanel(new GridLayout(2, 1));
        panelTitulos.setBackground(ThemeUtil.COLOR_PRIMARIO);
        panelTitulos.add(titulo);
        panelTitulos.add(subtitulo);
        
        header.add(panelTitulos, BorderLayout.CENTER);
        
        // Panel derecho con info de usuario Y ícono de desarrollador
        JPanel panelDerecho = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panelDerecho.setBackground(ThemeUtil.COLOR_PRIMARIO);
        
        // Info de usuario
        if (DataStore.getUsuarioActual() != null) {
            JLabel lblUsuario = new JLabel("👤 " + DataStore.getUsuarioActual().getNombreUsuario() + 
                                         " (" + DataStore.getUsuarioActual().getRol() + ")");
            lblUsuario.setFont(ThemeUtil.FUENTE_PEQUEÑA);
            lblUsuario.setForeground(Color.WHITE);
            panelDerecho.add(lblUsuario);
            
            // === ÍCONO DE DESARROLLADOR (solo para desarrolladores) ===
            if (DataStore.getUsuarioActual().esDesarrollador()) {
                JButton btnDev = crearBotonDesarrollador();
                panelDerecho.add(btnDev);
            }
        }
        
        header.add(panelDerecho, BorderLayout.EAST);
        
        return header;
    }

    // Método para crear el botón pequeño de desarrollador
    private JButton crearBotonDesarrollador() {
        JButton btnDev = new JButton("🛠️");
        btnDev.setToolTipText("Panel de Desarrollador - Herramientas avanzadas");
        
        // Estilo minimalista y pequeño
        btnDev.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnDev.setBackground(new Color(255, 215, 0)); // Dorado para destacar
        btnDev.setForeground(Color.BLACK);
        btnDev.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 170, 0), 1),
            BorderFactory.createEmptyBorder(3, 6, 3, 6)
        ));
        btnDev.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnDev.setFocusPainted(false);
        
        // Hover effect
        btnDev.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btnDev.setBackground(new Color(255, 230, 100));
                btnDev.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(255, 200, 0), 2),
                    BorderFactory.createEmptyBorder(3, 6, 3, 6)
                ));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btnDev.setBackground(new Color(255, 215, 0));
                btnDev.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(200, 170, 0), 1),
                    BorderFactory.createEmptyBorder(3, 6, 3, 6)
                ));
            }
        });
        
        // Acción al hacer clic - abre el menú de desarrollador existente
        btnDev.addActionListener(e -> {
            // Simplemente abre la gestión de usuarios como ejemplo
            // Puedes modificar esto para abrir un panel específico
            new GestionUsuariosGUI(this).setVisible(true);
        });
        
        return btnDev;
    }
    
    private JPanel crearPanelCentral() {
        JPanel panel = new JPanel(new GridLayout(0, 2, 15, 15));
        panel.setBackground(ThemeUtil.COLOR_FONDO);
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Botones principales
        JButton btnInsumos = crearBotonMenu("📦 Insumos", "Gestión de materias primas e insumos", 
            e -> new InsumoGUI(this).setVisible(true));
        
        JButton btnProduccion = crearBotonMenu("🏭 Producción", "Creación y gestión de lotes de producción", 
            e -> new ProduccionGUI(this, new DespachoGUI(this)).setVisible(true));
        
        JButton btnNProductos = crearBotonMenu("🆕 Nuevo Producto", "Definir nuevos productos terminados", 
            e -> new VentanaProductoGUI().setVisible(true));
        
        JButton btnProductos = crearBotonMenu("📊 Productos Terminados", "Consulta de stock de productos", 
            e -> new ProductoTerminadoGUI(this).setVisible(true));
        
        JButton btnDespachos = crearBotonMenu("🚚 Despachos", "Gestión de ventas y despachos", 
            e -> new DespachoGUI(this).setVisible(true));
        
        JButton btnSalir = crearBotonMenu("🚪 Salir", "Cerrar la aplicación", 
            e -> System.exit(0));
        
        panel.add(btnInsumos);
        panel.add(btnProduccion);
        panel.add(btnNProductos);
        panel.add(btnProductos);
        panel.add(btnDespachos);
        panel.add(btnSalir);
        
        return panel;
    }
    
    private JButton crearBotonMenu(String texto, String tooltip, java.awt.event.ActionListener action) {
        JButton boton = new JButton("<html><center>" + texto.replace(" ", "<br>") + "</center></html>");
        boton.setToolTipText(tooltip);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        boton.setBackground(Color.WHITE);
        boton.setForeground(ThemeUtil.COLOR_TEXTO);
        boton.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ThemeUtil.COLOR_BORDE, 1),
            BorderFactory.createEmptyBorder(20, 10, 20, 10)
        ));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setFocusPainted(false);
        
        // Efecto hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(ThemeUtil.COLOR_PRIMARIO);
                boton.setForeground(Color.WHITE);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(Color.WHITE);
                boton.setForeground(ThemeUtil.COLOR_TEXTO);
            }
        });
        
        boton.addActionListener(action);
        return boton;
    }
    
    private JPanel crearFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(ThemeUtil.COLOR_FONDO);
        footer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel lblFooter = new JLabel("© 2024 ECOBRILLA SOLUCIONES S.A.S - Sistema de Inventario v2.0");
        lblFooter.setFont(ThemeUtil.FUENTE_PEQUEÑA);
        lblFooter.setForeground(ThemeUtil.COLOR_TEXTO_SECUNDARIO);
        
        footer.add(lblFooter);
        return footer;
    }
    
    private void crearMenuDesarrollador() {
        JMenuBar barra = new JMenuBar();
        
        JMenu menuSistema = new JMenu("⚙️ Sistema");
        menuSistema.setFont(ThemeUtil.FUENTE_NORMAL);
        
        JMenuItem itemUsuarios = new JMenuItem("👥 Gestión de Usuarios");
        itemUsuarios.setFont(ThemeUtil.FUENTE_NORMAL);
        itemUsuarios.addActionListener(e -> new GestionUsuariosGUI(this).setVisible(true));
        
        JMenuItem itemBaseDatos = new JMenuItem("🗃️ Herramientas BD");
        itemBaseDatos.setFont(ThemeUtil.FUENTE_NORMAL);
        itemBaseDatos.addActionListener(e -> {
            ConfirmDialogUtil.mostrarInformacion(this, "Herramientas de BD", 
                "Funcionalidad en desarrollo...");
        });
        
        JMenuItem itemSalir = new JMenuItem("🚪 Salir");
        itemSalir.setFont(ThemeUtil.FUENTE_NORMAL);
        itemSalir.addActionListener(e -> System.exit(0));
        
        menuSistema.add(itemUsuarios);
        menuSistema.add(itemBaseDatos);
        menuSistema.add(new JSeparator());
        menuSistema.add(itemSalir);
        
        barra.add(menuSistema);
        setJMenuBar(barra);
    }
}