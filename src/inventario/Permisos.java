/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventario;

public class Permisos {
    public static boolean esGerente() {
        Usuario u = DataStore.getUsuarioActual();
        return u != null && "Gerente".equalsIgnoreCase(u.getRol());
    }

    public static boolean esAuxiliar() {
        Usuario u = DataStore.getUsuarioActual();
        return u != null && "Auxiliar".equalsIgnoreCase(u.getRol());
    }

    public static boolean estaLogueado() {
        return DataStore.getUsuarioActual() != null;
    }
}
