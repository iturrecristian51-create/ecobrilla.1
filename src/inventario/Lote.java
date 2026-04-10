package inventario;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Lote implements Serializable {
    private static final long serialVersionUID = 1L;

    private String idLote;
    private String nombreProducto;
    private String fechaProduccion;
    private String estado; // En proceso / Finalizado
    private int unidadesProducidas;
    
 private Map<String, Double> insumosUsados = new HashMap<>();
    public Lote(String idLote, String nombreProducto, String fechaProduccion) {
        if (idLote == null || idLote.trim().isEmpty()) {
            throw new IllegalArgumentException("El ID del lote no puede estar vacío.");
        }

        this.idLote = idLote.trim();
        this.nombreProducto = nombreProducto != null ? nombreProducto.trim() : "";
        this.fechaProduccion = fechaProduccion != null ? fechaProduccion.trim() : "";
        this.estado = "En proceso";
        this.unidadesProducidas = 0;
        this.insumosUsados = new HashMap<>();
    }

    // Getters
     public Map<String, Double> getInsumosUsados() {
        return insumosUsados;
    }
    public String getIdLote() { return idLote; }
    public String getNombreProducto() { return nombreProducto; }
    public String getFechaProduccion() { return fechaProduccion; }
    public String getEstado() { return estado; }
    public int getUnidadesProducidas() { return unidadesProducidas; }
    
    // Setters
     public void setInsumosUsados(Map<String, Double> insumosUsados) {
        this.insumosUsados = insumosUsados;
    }
    public void setEstado(String estado) { if (estado != null) this.estado = estado; }
    public void setUnidadesProducidas(int unidades) { this.unidadesProducidas = Math.max(0, unidades); }

    /**
     * Agrega insumo usado: valida stock, reduce inventario y guarda.
     */
    public void agregarInsumoUsado(String nombreInsumo, double cantidad) {
        if (nombreInsumo == null || nombreInsumo.trim().isEmpty()) 
            throw new IllegalArgumentException("Nombre insumo inválido");
        if (cantidad <= 0) 
            throw new IllegalArgumentException("Cantidad debe ser > 0");

        Insumo ins = DataStore.buscarInsumoPorNombre(nombreInsumo);
        if (ins == null) 
            throw new IllegalArgumentException("Insumo no encontrado: " + nombreInsumo);
        if (cantidad > ins.getCantidad()) 
            throw new IllegalArgumentException("Stock insuficiente: " + ins.getCantidad());

        // Reduce stock
        DataStore.reducirStockInsumo(ins.getNombre(), cantidad);

        // Registrar en lote
        insumosUsados.merge(ins.getNombre(), cantidad, Double::sum);

        // ✅ CORREGIDO: Ya no existe guardarDatos(), se guarda automáticamente en SQLite
        // El stock se guarda automáticamente en DataStore.reducirStockInsumo()
    }

    /**
     * Eliminar insumo usado y devolver al stock
     */
    public boolean eliminarInsumoUsado(String nombreInsumo) {
        if (insumosUsados.containsKey(nombreInsumo)) {
            double cant = insumosUsados.remove(nombreInsumo);
            // Devolver al inventario
            DataStore.aumentarStockInsumo(nombreInsumo, cant);
            // ✅ CORREGIDO: Ya no existe guardarDatos(), se guarda automáticamente en SQLite
            return true;
        }
        return false;
    }

    /**
     * ✅ NUEVO: Actualizar cantidad de insumo existente (edición de tabla)
     * Ajusta automáticamente el stock en DataStore
     */
    public boolean actualizarInsumoUsado(String nombreInsumo, double nuevaCantidad) {
        if (!insumosUsados.containsKey(nombreInsumo)) {
            return false;
        }
        
        if (nuevaCantidad <= 0) {
            return eliminarInsumoUsado(nombreInsumo);
        }
        
        double cantidadAnterior = insumosUsados.get(nombreInsumo);
        double diferencia = nuevaCantidad - cantidadAnterior;
        
        Insumo ins = DataStore.buscarInsumoPorNombre(nombreInsumo);
        if (ins == null) {
            return false;
        }
        
        if (diferencia > 0) {
            // Aumentó la cantidad: validar que haya stock
            if (diferencia > ins.getCantidad()) {
                throw new IllegalArgumentException("Stock insuficiente. Disponible: " + ins.getCantidad());
            }
            // Reducir stock adicional
            DataStore.reducirStockInsumo(nombreInsumo, diferencia);
        } else if (diferencia < 0) {
            // Disminuyó la cantidad: devolver al stock
            DataStore.aumentarStockInsumo(nombreInsumo, Math.abs(diferencia));
        }
        
        // Actualizar en el lote
        insumosUsados.put(nombreInsumo, nuevaCantidad);
        return true;
    }

    /**
     * Obtener lista de insumos como texto para mostrar
     */
    public String getInsumosUsadosTexto() {
        if (insumosUsados.isEmpty()) return "Sin insumos";
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> entry : insumosUsados.entrySet()) {
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return "[" + idLote + "] " + nombreProducto + " | " + fechaProduccion + " | " + estado + " | Unidades: " + unidadesProducidas;
    }
}