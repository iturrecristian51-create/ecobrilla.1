package inventario;

import java.io.Serializable;

public class ProductoTerminado implements Serializable {
    private static final long serialVersionUID = 1L;

    private String nombre;
    private int cantidad;
    private String fechaProduccion;

    public ProductoTerminado(String nombre, int cantidad, String fechaProduccion) {
        this.nombre = nombre != null ? nombre.trim() : "";
        this.cantidad = Math.max(0, cantidad);
        this.fechaProduccion = fechaProduccion != null ? fechaProduccion.trim() : "";
    }

    public String getNombre() { return nombre; }
    public int getCantidad() { return cantidad; }
    public String getFechaProduccion() { return fechaProduccion; }

    public void setCantidad(int cantidad) { this.cantidad = Math.max(0, cantidad); }
    public void aumentarCantidad(int delta) { if (delta > 0) this.cantidad += delta; }

    @Override
    public String toString() {
        return nombre + " | " + cantidad + " unidades | " + fechaProduccion;
    }
}
