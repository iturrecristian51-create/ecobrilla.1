package inventario;

import javax.swing.*;
import java.awt.*;

/**
 * Utilidades para diálogos de confirmación consistentes
 * MEJORAS FASE 3: Diálogos unificados y profesionales
 */
public class ConfirmDialogUtil {
    
    /**
     * Muestra diálogo de confirmación para eliminación
     */
    public static boolean confirmarEliminacion(Component parent, String elemento, String detalles) {
        String mensaje = "<html><body style='width: 300px;'>" +
                "<b>¿Está seguro de eliminar " + elemento + "?</b>" +
                (detalles != null ? "<br><br>" + detalles : "") +
                "</body></html>";
        
        int result = JOptionPane.showConfirmDialog(
            parent,
            mensaje,
            "Confirmar Eliminación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE
        );
        
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Confirmación para operaciones críticas
     */
    public static boolean confirmarOperacionCritica(Component parent, String titulo, String mensaje) {
        String mensajeHtml = "<html><body style='width: 300px;'>" +
                "<b>" + titulo + "</b>" +
                "<br><br>" + mensaje +
                "</body></html>";
        
        int result = JOptionPane.showConfirmDialog(
            parent,
            mensajeHtml,
            "Confirmar Operación",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        return result == JOptionPane.YES_OPTION;
    }
    
    /**
     * Diálogo de información mejorado
     */
    public static void mostrarInformacion(Component parent, String titulo, String mensaje) {
        String mensajeHtml = "<html><body style='width: 300px;'>" +
                "<b>" + titulo + "</b>" +
                "<br><br>" + mensaje +
                "</body></html>";
        
        JOptionPane.showMessageDialog(
            parent,
            mensajeHtml,
            "Información",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    /**
     * Diálogo de advertencia mejorado
     */
    public static void mostrarAdvertencia(Component parent, String titulo, String mensaje) {
        String mensajeHtml = "<html><body style='width: 300px;'>" +
                "<b>" + titulo + "</b>" +
                "<br><br>" + mensaje +
                "</body></html>";
        
        JOptionPane.showMessageDialog(
            parent,
            mensajeHtml,
            "Advertencia",
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    /**
     * Diálogo de error mejorado
     */
    public static void mostrarError(Component parent, String titulo, String mensaje) {
        String mensajeHtml = "<html><body style='width: 300px;'>" +
                "<b>" + titulo + "</b>" +
                "<br><br>" + mensaje +
                "</body></html>";
        
        JOptionPane.showMessageDialog(
            parent,
            mensajeHtml,
            "Error",
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    /**
     * Diálogo de éxito
     */
    public static void mostrarExito(Component parent, String titulo, String mensaje) {
        String mensajeHtml = "<html><body style='width: 300px;'>" +
                "<b>" + titulo + "</b>" +
                "<br><br>" + mensaje +
                "</body></html>";
        
        JOptionPane.showMessageDialog(
            parent,
            mensajeHtml,
            "Éxito",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}