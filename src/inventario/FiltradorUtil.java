package inventario;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Utilidades para filtrado y búsqueda en listas y tablas
 * MEJORAS FASE 2: Sistema de filtrado universal
 */
public class FiltradorUtil {
    
    // === FILTRADO DE LISTAS ===
    
    /**
     * Filtra una lista basado en un criterio de texto
     */
    public static <T> List<T> filtrar(List<T> lista, String criterio, Function<T, String> extractor) {
        if (criterio == null || criterio.trim().isEmpty()) {
            return lista;
        }
        
        String textoBusqueda = criterio.toLowerCase().trim();
        return lista.stream()
            .filter(item -> {
                String texto = extractor.apply(item);
                return texto != null && texto.toLowerCase().contains(textoBusqueda);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Filtra múltiples campos de una lista
     */
    public static <T> List<T> filtrarMultiplesCampos(List<T> lista, String criterio, 
                                                   Function<T, String>... extractores) {
        if (criterio == null || criterio.trim().isEmpty()) {
            return lista;
        }
        
        String textoBusqueda = criterio.toLowerCase().trim();
        return lista.stream()
            .filter(item -> {
                for (Function<T, String> extractor : extractores) {
                    String texto = extractor.apply(item);
                    if (texto != null && texto.toLowerCase().contains(textoBusqueda)) {
                        return true;
                    }
                }
                return false;
            })
            .collect(Collectors.toList());
    }
    
    // === FILTRADO DE TABLAS SWING ===
    
    /**
     * Aplica filtro de texto a una JTable
     */
    public static void aplicarFiltroTabla(JTable tabla, String texto) {
        TableRowSorter<?> sorter = (TableRowSorter<?>) tabla.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>(tabla.getModel());
            tabla.setRowSorter(sorter);
        }
        
        if (texto == null || texto.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto));
        }
    }
    
    /**
     * Aplica filtro a columna específica
     */
    public static void aplicarFiltroColumna(JTable tabla, int columna, String texto) {
        TableRowSorter<?> sorter = (TableRowSorter<?>) tabla.getRowSorter();
        if (sorter == null) {
            sorter = new TableRowSorter<>(tabla.getModel());
            tabla.setRowSorter(sorter);
        }
        
        if (texto == null || texto.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, columna));
        }
    }
    
    /**
     * Configura filtrado por columnas con placeholders
     */
    public static void configurarFiltroColumnas(JTable tabla, String[] placeholders) {
        TableRowSorter<?> sorter = new TableRowSorter<>(tabla.getModel());
        tabla.setRowSorter(sorter);
        
        // Hacer todas las columnas buscables
        for (int i = 0; i < tabla.getColumnCount(); i++) {
            sorter.setSortable(i, true);
        }
    }
    
    // === UTILIDADES DE BÚSQUEDA ESPECÍFICAS ===
    
    /**
     * Busca insumos por múltiples criterios
     */
    public static List<Insumo> filtrarInsumos(List<Insumo> insumos, String criterio) {
        return filtrarMultiplesCampos(insumos, criterio,
            Insumo::getNombre,
            Insumo::getLote,
            Insumo::getProveedor,
            Insumo::getNotas
        );
    }
    
    /**
     * Busca lotes por múltiples criterios
     */
    public static List<Lote> filtrarLotes(List<Lote> lotes, String criterio) {
        return filtrarMultiplesCampos(lotes, criterio,
            Lote::getIdLote,
            Lote::getNombreProducto,
            Lote::getEstado
        );
    }
    
    /**
     * Busca productos terminados por múltiples criterios
     */
    public static List<ProductoTerminado> filtrarProductosTerminados(List<ProductoTerminado> productos, String criterio) {
        return filtrarMultiplesCampos(productos, criterio,
            ProductoTerminado::getNombre
        );
    }
    
    /**
     * Busca despachos por múltiples criterios
     */
    public static List<Despacho> filtrarDespachos(List<Despacho> despachos, String criterio) {
        return filtrarMultiplesCampos(despachos, criterio,
            Despacho::getClienteNombre,
            Despacho::getClienteNIT,
            Despacho::getClienteCiudad
        );
    }
    
    /**
     * Busca en historial por múltiples criterios
     */
    public static List<HistorialInsumo> filtrarHistorial(List<HistorialInsumo> historial, String criterio) {
        return filtrarMultiplesCampos(historial, criterio,
            HistorialInsumo::getLote,
            HistorialInsumo::getNombreInsumo,
            HistorialInsumo::getAccion,
            HistorialInsumo::getObservacion
        );
    }
}