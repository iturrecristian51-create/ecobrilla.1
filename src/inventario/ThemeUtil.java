package inventario;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import javax.swing.table.JTableHeader;

/**
 * Utilidades para estilos consistentes en la aplicación
 * MEJORAS FASE 3: Estilos unificados y profesionales
 */
public class ThemeUtil {
    
    // Colores de la aplicación
    public static final Color COLOR_PRIMARIO = new Color(0, 123, 255);
    public static final Color COLOR_SECUNDARIO = new Color(108, 117, 125);
    public static final Color COLOR_EXITO = new Color(40, 167, 69);
    public static final Color COLOR_PELIGRO = new Color(220, 53, 69);
    public static final Color COLOR_ADVERTENCIA = new Color(255, 193, 7);
    public static final Color COLOR_INFO = new Color(23, 162, 184);
    
    public static final Color COLOR_FONDO = new Color(248, 249, 250);
    public static final Color COLOR_BORDE = new Color(222, 226, 230);
    public static final Color COLOR_TEXTO = new Color(33, 37, 41);
    public static final Color COLOR_TEXTO_SECUNDARIO = new Color(108, 117, 125);
    
    // Fuentes
    public static final Font FUENTE_TITULO = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FUENTE_SUBTITULO = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FUENTE_NORMAL = new Font("Segoe UI", Font.PLAIN, 12);
    public static final Font FUENTE_PEQUEÑA = new Font("Segoe UI", Font.PLAIN, 11);
    
    /**
     * Aplica estilo primario a un botón
     */
    public static void aplicarEstiloPrimario(JButton boton) {
        boton.setFont(FUENTE_NORMAL);
        boton.setBackground(COLOR_PRIMARIO);
        boton.setForeground(Color.BLACK);
        boton.setFocusPainted(false);
        boton.setBorder(crearBordeRedondeado(COLOR_PRIMARIO));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        // Efecto hover
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(COLOR_PRIMARIO.darker());
                boton.setForeground(Color.BLACK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(COLOR_PRIMARIO);
                boton.setForeground(Color.BLACK);
            }
        });
    }
    
    /**
     * Aplica estilo secundario a un botón
     */
    public static void aplicarEstiloSecundario(JButton boton) {
        boton.setFont(FUENTE_NORMAL);
        boton.setBackground(Color.WHITE);
        boton.setForeground(Color.BLACK);
        boton.setFocusPainted(false);
        boton.setBorder(crearBordeRedondeado(COLOR_BORDE));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(COLOR_FONDO);
                boton.setForeground(Color.BLACK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(Color.WHITE);
                boton.setForeground(Color.BLACK);
            }
        });
    }
    
    /**
     * Aplica estilo de peligro a un botón
     */
    public static void aplicarEstiloPeligro(JButton boton) {
        boton.setFont(FUENTE_NORMAL);
        boton.setBackground(COLOR_PELIGRO);
        boton.setForeground(Color.BLACK);
        boton.setFocusPainted(false);
        boton.setBorder(crearBordeRedondeado(COLOR_PELIGRO));
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(COLOR_PELIGRO.darker());
                boton.setForeground(Color.BLACK);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(COLOR_PELIGRO);
                boton.setForeground(Color.BLACK);
            }
        });
    }
    
    /**
     * Crea un borde redondeado
     */
    public static Border crearBordeRedondeado(Color color) {
        return new CompoundBorder(
            new LineBorder(color, 1),
            new EmptyBorder(8, 16, 8, 16)
        );
    }
    
    /**
     * Crea un borde con esquinas redondeadas
     */
    public static Border crearBordeRedondeadoCompleto(Color color, int radio) {
        return new javax.swing.border.AbstractBorder() {
            @Override
            public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(color);
                g2.drawRoundRect(x, y, width - 1, height - 1, radio, radio);
                g2.dispose();
            }
            
            @Override
            public Insets getBorderInsets(Component c) {
                return new Insets(4, 8, 4, 8);
            }
        };
    }
    
    /**
     * Aplica estilo a un campo de texto
     */
    public static void aplicarEstiloCampo(JTextField campo) {
        campo.setFont(FUENTE_NORMAL);
        campo.setBorder(crearBordeCampo());
        campo.setBackground(Color.WHITE);
        campo.setForeground(COLOR_TEXTO);
    }
    
    /**
     * Aplica estilo a un área de texto
     */
    public static void aplicarEstiloAreaTexto(JTextArea area) {
        area.setFont(FUENTE_NORMAL);
        area.setBorder(crearBordeCampo());
        area.setBackground(Color.WHITE);
        area.setForeground(COLOR_TEXTO);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    }
    
    /**
     * Crea borde para campos de texto
     */
    private static Border crearBordeCampo() {
        return new CompoundBorder(
            new LineBorder(COLOR_BORDE, 1),
            new EmptyBorder(6, 8, 6, 8)
        );
    }
    
    /**
     * Aplica estilo a una tabla
     */
    public static void aplicarEstiloTabla(JTable tabla) {
        tabla.setFont(FUENTE_NORMAL);
        tabla.setRowHeight(25);
        tabla.setShowGrid(true);
        tabla.setGridColor(COLOR_BORDE);
        tabla.setSelectionBackground(COLOR_PRIMARIO);
        tabla.setSelectionForeground(Color.WHITE);
        
        // Header de la tabla
        JTableHeader header = tabla.getTableHeader();
        header.setFont(FUENTE_SUBTITULO);
        header.setBackground(COLOR_SECUNDARIO);
        header.setForeground(Color.WHITE);
        header.setReorderingAllowed(false);
    }
    
    /**
     * Aplica estilo a un panel de título
     */
    public static JPanel crearPanelTitulo(String titulo) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_FONDO);
        panel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDE),
            new EmptyBorder(10, 15, 10, 15)
        ));
        
        JLabel label = new JLabel(titulo);
        label.setFont(FUENTE_TITULO);
        label.setForeground(COLOR_TEXTO);
        panel.add(label, BorderLayout.WEST);
        
        return panel;
    }
    
    /**
     * Crea un panel de card con sombra
     */
    public static JPanel crearCardPanel() {
        JPanel card = new JPanel();
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(COLOR_BORDE),
            new EmptyBorder(15, 15, 15, 15)
        ));
        return card;
    }
    
    /**
     * Configura el estilo global de la aplicación
     */
   public static void configurarEstiloGlobal() {
    try {
        // Método corregido - usa la clase del sistema
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        
        // Colores globales
        UIManager.put("Panel.background", COLOR_FONDO);
        UIManager.put("Table.background", Color.WHITE);
        UIManager.put("Table.gridColor", COLOR_BORDE);
        UIManager.put("Table.selectionBackground", COLOR_PRIMARIO);
        UIManager.put("Table.selectionForeground", Color.WHITE);
        
    } catch (Exception e) {
            e.printStackTrace();
        }
    }
}