package inventario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductoTerminadoDAO {

    // === INSERTAR o ACTUALIZAR un producto terminado ===
    public static void insertar(ProductoTerminado producto) {
        String sql = "INSERT OR REPLACE INTO productos_terminados (nombre, cantidad, unidad, fecha_produccion, notas) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, producto.getNombre());
            ps.setInt(2, producto.getCantidad());
            ps.setString(3, "unidades"); // si tuvieras unidad fija
            ps.setString(4, producto.getFechaProduccion());
            ps.setString(5, ""); // notas vacías por defecto
            ps.executeUpdate();

            System.out.println("💾 Producto terminado guardado: " + producto.getNombre());
        } catch (SQLException e) {
            System.err.println("❌ Error al insertar producto: " + e.getMessage());
        }
    }

    // === OBTENER TODOS LOS PRODUCTOS ===
    public static List<ProductoTerminado> obtenerTodos() {
        List<ProductoTerminado> lista = new ArrayList<>();
        String sql = "SELECT nombre, cantidad, fecha_produccion FROM productos_terminados";

        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                ProductoTerminado p = new ProductoTerminado(
                        rs.getString("nombre"),
                        rs.getInt("cantidad"),
                        rs.getString("fecha_produccion")
                );
                lista.add(p);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al obtener productos terminados: " + e.getMessage());
        }

        return lista;
    }

    // === ELIMINAR PRODUCTO POR NOMBRE ===
    public static void eliminar(String nombre) {
        String sql = "DELETE FROM productos_terminados WHERE nombre = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, nombre);
            ps.executeUpdate();
            System.out.println("🗑️ Producto eliminado: " + nombre);
        } catch (SQLException e) {
            System.err.println("❌ Error al eliminar producto: " + e.getMessage());
        }
    }
}
