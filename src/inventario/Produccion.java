package inventario;

import java.io.Serializable;

public class Produccion implements Serializable {
    private String codigoProduccion;
    private String fecha;
    private String producto;
    private double cantidadProducida;
    private String unidad;
    private String loteInsumo;
    private String responsable;
    private String notas;

    // === Constructor completo ===
    public Produccion(String codigoProduccion, String fecha, String producto,
                      double cantidadProducida, String unidad,
                      String loteInsumo, String responsable, String notas) {
        this.codigoProduccion = codigoProduccion;
        this.fecha = fecha;
        this.producto = producto;
        this.cantidadProducida = cantidadProducida;
        this.unidad = unidad;
        this.loteInsumo = loteInsumo;
        this.responsable = responsable;
        this.notas = notas;
    }

    // === Getters y Setters ===
    public String getCodigoProduccion() { return codigoProduccion; }
    public void setCodigoProduccion(String codigoProduccion) { this.codigoProduccion = codigoProduccion; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getProducto() { return producto; }
    public void setProducto(String producto) { this.producto = producto; }

    public double getCantidadProducida() { return cantidadProducida; }
    public void setCantidadProducida(double cantidadProducida) { this.cantidadProducida = cantidadProducida; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public String getLoteInsumo() { return loteInsumo; }
    public void setLoteInsumo(String loteInsumo) { this.loteInsumo = loteInsumo; }

    public String getResponsable() { return responsable; }
    public void setResponsable(String responsable) { this.responsable = responsable; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    @Override
    public String toString() {
        return String.format("%s - %.2f %s (%s)", producto, cantidadProducida, unidad, codigoProduccion);
    }
}
