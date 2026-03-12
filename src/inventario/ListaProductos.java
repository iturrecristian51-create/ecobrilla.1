/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventario;

import java.util.ArrayList;
import java.util.List;

public class ListaProductos {
    private static List<Producto> productos = new ArrayList<>();

    public static void agregarProducto(Producto p) {
        productos.add(p);
    }

    public static List<Producto> obtenerProductos() {
        return productos;
    }

    public static Producto buscarPorNombre(String nombre) {
        for (Producto p : productos) {
            if (p.getNombre().equalsIgnoreCase(nombre)) {
                return p;
            }
        }
        return null;
    }

    public static boolean existe(String nombre) {
        return buscarPorNombre(nombre) != null;
    }

    public static void limpiar() {
        productos.clear();
    }
}
