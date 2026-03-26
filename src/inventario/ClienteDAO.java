package inventario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * ClienteDAO — toda la lógica SQLite para clientes.
 * Sigue el mismo patrón que el resto de tu DataStore.
 */
public class ClienteDAO {

    // ── Crear tabla (llámalo desde DataStore.crearTablasSiNoExisten) ───────
    public static void crearTabla() {
        String sql = """
            CREATE TABLE IF NOT EXISTS clientes (
                id        INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre    TEXT NOT NULL,
                nit       TEXT UNIQUE NOT NULL,
                telefono  TEXT,
                direccion TEXT,
                ciudad    TEXT
            )
        """;
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt  = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Tabla clientes creada/verificada");
        } catch (SQLException e) {
            System.err.println("❌ Error creando tabla clientes: " + e.getMessage());
        }
    }

    // ── Insertar o actualizar ──────────────────────────────────────────────
    public static boolean guardar(Cliente c) {
        // Si tiene id > 0 es actualización, si no es inserción
        if (c.getId() > 0) {
            return actualizar(c);
        }
        String sql = """
            INSERT INTO clientes (nombre, nit, telefono, direccion, ciudad)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(
                     sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getNit());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getDireccion());
            ps.setString(5, c.getCiudad());
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) c.setId(rs.getInt(1));
            System.out.println("✅ Cliente guardado: " + c.getNombre());
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error guardando cliente: " + e.getMessage());
            return false;
        }
    }

    private static boolean actualizar(Cliente c) {
        String sql = """
            UPDATE clientes
            SET nombre=?, nit=?, telefono=?, direccion=?, ciudad=?
            WHERE id=?
        """;
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNombre());
            ps.setString(2, c.getNit());
            ps.setString(3, c.getTelefono());
            ps.setString(4, c.getDireccion());
            ps.setString(5, c.getCiudad());
            ps.setInt(6, c.getId());
            ps.executeUpdate();
            System.out.println("✅ Cliente actualizado: " + c.getNombre());
            return true;
        } catch (SQLException e) {
            System.err.println("❌ Error actualizando cliente: " + e.getMessage());
            return false;
        }
    }

    // ── Eliminar ───────────────────────────────────────────────────────────
    public static boolean eliminar(int id) {
        String sql = "DELETE FROM clientes WHERE id = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            int filas = ps.executeUpdate();
            System.out.println("✅ Cliente eliminado id=" + id);
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("❌ Error eliminando cliente: " + e.getMessage());
            return false;
        }
    }

    // ── Listar todos ───────────────────────────────────────────────────────
    public static List<Cliente> listarTodos() {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes ORDER BY nombre";
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt  = conn.createStatement();
             ResultSet rs    = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Cliente c = new Cliente(
                    rs.getString("nombre"),
                    rs.getString("nit"),
                    rs.getString("telefono"),
                    rs.getString("direccion"),
                    rs.getString("ciudad")
                );
                c.setId(rs.getInt("id"));
                lista.add(c);
            }
        } catch (SQLException e) {
            System.err.println("❌ Error listando clientes: " + e.getMessage());
        }
        return lista;
    }

    // ── Buscar por NIT ─────────────────────────────────────────────────────
    public static Cliente buscarPorNit(String nit) {
        String sql = "SELECT * FROM clientes WHERE nit = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nit);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Cliente c = new Cliente(
                    rs.getString("nombre"),
                    rs.getString("nit"),
                    rs.getString("telefono"),
                    rs.getString("direccion"),
                    rs.getString("ciudad")
                );
                c.setId(rs.getInt("id"));
                return c;
            }
        } catch (SQLException e) {
            System.err.println("❌ Error buscando cliente: " + e.getMessage());
        }
        return null;
    }
}
