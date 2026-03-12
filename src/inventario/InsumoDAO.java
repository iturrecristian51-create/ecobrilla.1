package inventario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InsumoDAO {

    // === INSERTAR o REEMPLAZAR un insumo ===
    public static void insertar(Insumo insumo) {
        String sql = """
            INSERT OR REPLACE INTO insumos 
            (lote, nombre, fechaIngreso, cantidad, unidad, proveedor, notas)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, insumo.getLote());
            ps.setString(2, insumo.getNombre());
            ps.setString(3, insumo.getFechaIngreso());
            ps.setDouble(4, insumo.getCantidad());
            ps.setString(5, insumo.getUnidad());
            ps.setString(6, insumo.getProveedor());
            ps.setString(7, insumo.getNotas());
            ps.executeUpdate();

            System.out.println("💾 Insumo guardado en SQLite: " + insumo.getNombre());

        } catch (SQLException e) {
            System.err.println("❌ Error al insertar insumo: " + e.getMessage());
        }
    }

    // === ACTUALIZAR un insumo existente ===
    public static void actualizar(Insumo insumo) {
        String sql = """
            UPDATE insumos SET 
                nombre = ?, 
                fechaIngreso = ?, 
                cantidad = ?, 
                unidad = ?, 
                proveedor = ?, 
                notas = ?
            WHERE lote = ?
        """;

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, insumo.getNombre());
            ps.setString(2, insumo.getFechaIngreso());
            ps.setDouble(3, insumo.getCantidad());
            ps.setString(4, insumo.getUnidad());
            ps.setString(5, insumo.getProveedor());
            ps.setString(6, insumo.getNotas());
            ps.setString(7, insumo.getLote());
            ps.executeUpdate();

            System.out.println("✏️ Insumo actualizado: " + insumo.getNombre());

        } catch (SQLException e) {
            System.err.println("❌ Error al actualizar insumo: " + e.getMessage());
        }
    }

    // === ELIMINAR un insumo ===
    public static void eliminar(String lote) {
        String sql = "DELETE FROM insumos WHERE lote = ?";

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lote);
            ps.executeUpdate();
            System.out.println("🗑️ Insumo eliminado: " + lote);

        } catch (SQLException e) {
            System.err.println("❌ Error al eliminar insumo: " + e.getMessage());
        }
    }

    // === BUSCAR un insumo por lote ===
    public static Insumo buscarPorLote(String lote) {
        String sql = "SELECT * FROM insumos WHERE lote = ?";
        Insumo insumo = null;

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lote);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                insumo = new Insumo(
                        rs.getString("lote"),
                        rs.getString("nombre"),
                        rs.getString("fechaIngreso"),
                        rs.getDouble("cantidad"),
                        rs.getString("unidad"),
                        rs.getString("proveedor"),
                        rs.getString("notas")
                );
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al buscar insumo: " + e.getMessage());
        }
        return insumo;
    }

    public static void registrarHistorialInsumo(HistorialInsumo h) {
        String sql = "INSERT INTO historial_insumos(lote, nombre_insumo, accion, cantidad, observacion) VALUES(?,?,?,?,?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, h.getLote());
            ps.setString(2, h.getNombreInsumo());
            ps.setString(3, h.getAccion());
            ps.setDouble(4, h.getCantidad());
            ps.setString(5, h.getObservacion());
            ps.executeUpdate();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    // === CORREGIDO: OBTENER TODOS los insumos ===
    public static List<Insumo> obtenerTodos() {
        List<Insumo> lista = new ArrayList<>();
        String sql = "SELECT * FROM insumos ORDER BY nombre ASC";

        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Insumo insumo = new Insumo(
                        rs.getString("lote"),
                        rs.getString("nombre"),
                        rs.getString("fechaIngreso"),
                        rs.getDouble("cantidad"),
                        rs.getString("unidad"),
                        rs.getString("proveedor"),
                        rs.getString("notas")
                );
                lista.add(insumo);
            }

        } catch (SQLException e) {
            System.err.println("❌ Error al obtener insumos: " + e.getMessage());
        }
        return lista; // ✅ Ahora retorna la lista, no null
    }

    private static Connection getConnection() throws SQLException {
        String url = "jdbc:sqlite:data/inventario.db";
        return DriverManager.getConnection(url);
    }
}