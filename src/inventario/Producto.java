/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventario;

import java.util.HashMap;
import java.util.Map;

public class Producto {
    private String nombre;
    private String descripcion;
    private Map<String, Double> insumosRequeridos; // Nombre del insumo -> cantidad
    private String idLote; // Se asocia luego con Producción

    private Map<String, Double> insumos = new HashMap<>();

public Map<String, Double> getInsumos() {
    return insumos;
}

public void setInsumos(Map<String, Double> insumos) {
    this.insumos = insumos;
}


    public Producto(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.insumosRequeridos = new HashMap<>();
    }

    public void agregarInsumo(String nombreInsumo, double cantidad) {
        insumosRequeridos.put(nombreInsumo, cantidad);
    }

    public Map<String, Double> getInsumosRequeridos() {
        return insumosRequeridos;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
    
}

