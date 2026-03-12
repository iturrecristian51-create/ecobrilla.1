/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventario;

public class Movimiento {
    private String fecha;
    private String tipo;
    private String referencia;
    private String descripcion;
    private double cantidad;

    public Movimiento(String fecha, String tipo, String referencia, String descripcion, double cantidad) {
        this.fecha = fecha;
        this.tipo = tipo;
        this.referencia = referencia;
        this.descripcion = descripcion;
        this.cantidad = cantidad;
    }

    public String getFecha() { return fecha; }
    public String getTipo() { return tipo; }
    public String getReferencia() { return referencia; }
    public String getDescripcion() { return descripcion; }
    public double getCantidad() { return cantidad; }
}
