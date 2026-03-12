package inventario;

import java.io.Serializable;

public class Insumo implements Serializable {
    private String lote;
    private String nombre;
    private String fechaIngreso;
    private double cantidad;
    private String unidad;
    private String proveedor;
    private String notas;

    // === Constructor completo ===
    public Insumo(String lote, String nombre, String fechaIngreso,
                  double cantidad, String unidad, String proveedor, String notas) {
        this.lote = lote;
        this.nombre = nombre;
        this.fechaIngreso = fechaIngreso;
        this.cantidad = cantidad;
        this.unidad = unidad;
        this.proveedor = proveedor;
        this.notas = notas;
    }

    // === Getters y Setters ===
    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getFechaIngreso() { return fechaIngreso; }
    public void setFechaIngreso(String fechaIngreso) { this.fechaIngreso = fechaIngreso; }

    public double getCantidad() { return cantidad; }
    public void setCantidad(double cantidad) { this.cantidad = cantidad; }

    public String getUnidad() { return unidad; }
    public void setUnidad(String unidad) { this.unidad = unidad; }

    public String getProveedor() { return proveedor; }
    public void setProveedor(String proveedor) { this.proveedor = proveedor; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    @Override
    public String toString() {
        return String.format("%s (%s) - %.2f %s", nombre, lote, cantidad, unidad);
    }
}
