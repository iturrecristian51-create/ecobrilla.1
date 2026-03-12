package inventario;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistorialInsumo implements Serializable {
    private String fecha;
    private String lote;
    private String nombreInsumo;
    private String accion;
    private double cantidad;
    private String observacion;

    public HistorialInsumo(String lote, String nombreInsumo, String accion, double cantidad, String observacion) {
        this.fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        this.lote = lote;
        this.nombreInsumo = nombreInsumo;
        this.accion = accion;
        this.cantidad = cantidad;
        this.observacion = observacion;
    }
    public HistorialInsumo(String fecha, String lote, String nombreInsumo, String accion, double cantidad, String observacion) {
        this.fecha = fecha != null ? fecha : "";
        this.lote = lote;
        this.nombreInsumo = nombreInsumo;
        this.accion = accion;
        this.cantidad = cantidad;
        this.observacion = observacion;
    }
    public String getFecha() { return fecha; }
    public String getLote() { return lote; }
    public String getNombreInsumo() { return nombreInsumo; }
    public String getAccion() { return accion; }
    public double getCantidad() { return cantidad; }
    public String getObservacion() { return observacion; }

    @Override
    public String toString() {
        return String.format("%s | %s | %s | %.2f | %s | %s", fecha, lote, nombreInsumo, cantidad, accion, observacion);
    }
    
}
