/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package inventario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProduccionDAO {

    // === INSERTAR o actualizar una producción ===
    public static void insertar(Produccion produccion) {
        String sql = "INSERT OR REPLACE INTO produccion (codigoProduccion, fecha, producto, cantidadProducida, unidad, loteInsumo, responsable, notas) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, produccion.getCodigoProduccion());
            ps.setString(2, produccion.getFecha());
            ps.setString(3, produccion.getProducto());
            ps.setDouble(4, produccion.getCantidadProducida());
            ps.setString(5, produccion.getUnidad());
            ps.setString(6, produccion.getLoteInsumo());
            ps.setString(7, produccion.getResponsable());
            ps.setString(8, produccion.getNotas());

            ps.executeUpdate();
            System.out.println("💾 Producción guardada en SQLite: " + produccion.getProducto());

        } catch (SQLException e) {
            System.err.println("❌ Error al insertar producción: " + e.getMessage());
        }
    }

    // === OBTENER TODAS las producciones ===
    public static List<Produccion> obtenerTodas() {
        List<Produccion> lista = new ArrayList<>();
        String sql = "SELECT * FROM produccion";

        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Produccion p = new Produccion(
                        rs.getString("codigoProduccion"),
                        rs.getString("fecha"),
                        rs.getString("producto"),
                        rs.getDouble("cantidadProducida"),
                        rs.getString("unidad"),
                        rs.getString("loteInsumo"),
                        rs.getString("responsable"),
                        rs.getString("notas")
                );
                lista.add(p);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al obtener producciones: " + e.getMessage());
        }

        return lista;
    }

    // === ELIMINAR una producción ===
    public static void eliminar(String codigoProduccion) {
        String sql = "DELETE FROM produccion WHERE codigoProduccion = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, codigoProduccion);
            ps.executeUpdate();
            System.out.println("🗑️ Producción eliminada: " + codigoProduccion);

        } catch (SQLException e) {
            System.err.println("❌ Error al eliminar producción: " + e.getMessage());
        }
    }
}
