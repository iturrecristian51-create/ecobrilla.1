 package inventario;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DespachoDAO {

    // === Crear tabla si no existe ===
    public static void crearTabla() {
        String sql = """
            CREATE TABLE IF NOT EXISTS despachos (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente_nombre TEXT,
                cliente_nit TEXT,
                cliente_telefono TEXT,
                cliente_direccion TEXT,
                cliente_ciudad TEXT,
                fecha TEXT,
                notas TEXT
            )
            """;
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error al crear tabla despachos: " + e.getMessage());
        }
    }

    // === Insertar un despacho ===
    public static boolean insertar(Despacho despacho) {
        String sql = """
            INSERT INTO despachos (
                cliente_nombre, cliente_nit, cliente_telefono,
                cliente_direccion, cliente_ciudad, fecha, notas
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, despacho.getClienteNombre());
            ps.setString(2, despacho.getClienteNIT());
            ps.setString(3, despacho.getClienteTelefono());
            ps.setString(4, despacho.getClienteDireccion());
            ps.setString(5, despacho.getClienteCiudad());
            ps.setString(6, despacho.getFechaHora().toString());
            ps.setString(7, despacho.getNotas());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    despacho.setId(rs.getInt(1));
                }
            }

            // Guardar los ítems del despacho
            insertarItems(despacho);

            return true;
        } catch (SQLException e) {
            System.err.println("Error al insertar despacho: " + e.getMessage());
            return false;
        }
    }

    // === Insertar ítems de un despacho ===
    private static void insertarItems(Despacho despacho) {
        String sql = """
            CREATE TABLE IF NOT EXISTS despacho_items (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                id_despacho INTEGER,
                producto TEXT,
                cantidad REAL,
                FOREIGN KEY (id_despacho) REFERENCES despachos(id)
            )
            """;
        try (Connection conn = ConexionSQLite.conectar();
             Statement st = conn.createStatement()) {
            st.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error al crear tabla despacho_items: " + e.getMessage());
        }

        String insertItemSQL = "INSERT INTO despacho_items (id_despacho, producto, cantidad) VALUES (?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(insertItemSQL)) {

            for (Despacho.Item item : despacho.getItems()) {
                ps.setInt(1, despacho.getId());
                ps.setString(2, item.getNombreProducto());
                ps.setDouble(3, item.getCantidad());
                ps.addBatch();
            }
            ps.executeBatch();

        } catch (SQLException e) {
            System.err.println("Error al insertar ítems del despacho: " + e.getMessage());
        }
    }

    // === Listar todos los despachos ===
    public static List<Despacho> listar() {
        List<Despacho> lista = new ArrayList<>();
        String sql = "SELECT * FROM despachos ORDER BY fecha DESC";

        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Despacho d = new Despacho();
                d.setId(rs.getInt("id"));
                d.setClienteNombre(rs.getString("cliente_nombre"));
                d.setClienteNIT(rs.getString("cliente_nit"));
                d.setClienteTelefono(rs.getString("cliente_telefono"));
                d.setClienteDireccion(rs.getString("cliente_direccion"));
                d.setClienteCiudad(rs.getString("cliente_ciudad"));
                d.setNotas(rs.getString("notas"));
                d.setFechaHora(rs.getString("fecha")); // conversión automática en clase Despacho
                d.setItems(obtenerItemsPorDespacho(d.getId()));
                lista.add(d);
            }

        } catch (SQLException e) {
            System.err.println("Error al listar despachos: " + e.getMessage());
        }
        return lista;
    }

    // === Obtener ítems por ID de despacho ===
    public static List<Despacho.Item> obtenerItemsPorDespacho(int idDespacho) {
        List<Despacho.Item> items = new ArrayList<>();
        String sql = "SELECT producto, cantidad FROM despacho_items WHERE id_despacho = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, idDespacho);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                items.add(new Despacho.Item(
                        rs.getString("producto"),
                        rs.getInt("cantidad")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error al obtener ítems de despacho: " + e.getMessage());
        }
        return items;
    }

    // === Buscar despacho por ID ===
    public static Despacho buscarPorId(int id) {
        String sql = "SELECT * FROM despachos WHERE id = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                Despacho d = new Despacho();
                d.setId(rs.getInt("id"));
                d.setClienteNombre(rs.getString("cliente_nombre"));
                d.setClienteNIT(rs.getString("cliente_nit"));
                d.setClienteTelefono(rs.getString("cliente_telefono"));
                d.setClienteDireccion(rs.getString("cliente_direccion"));
                d.setClienteCiudad(rs.getString("cliente_ciudad"));
                d.setNotas(rs.getString("notas"));
                d.setFechaHora(rs.getString("fecha"));
                d.setItems(obtenerItemsPorDespacho(d.getId()));
                return d;
            }

        } catch (SQLException e) {
            System.err.println("Error al buscar despacho: " + e.getMessage());
        }
        return null;
    }

    // === Eliminar un despacho ===
    public static boolean eliminar(int id) {
        String sqlItems = "DELETE FROM despacho_items WHERE id_despacho = ?";
        String sqlDespacho = "DELETE FROM despachos WHERE id = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps1 = conn.prepareStatement(sqlItems);
             PreparedStatement ps2 = conn.prepareStatement(sqlDespacho)) {

            ps1.setInt(1, id);
            ps1.executeUpdate();

            ps2.setInt(1, id);
            ps2.executeUpdate();

            return true;
        } catch (SQLException e) {
            System.err.println("Error al eliminar despacho: " + e.getMessage());
            return false;
        }
    }
    // === Insertar despacho si no existe ===
public static boolean insertarSiNoExiste(Despacho despacho) {
    String verificarSQL = "SELECT COUNT(*) FROM despachos WHERE cliente_nombre = ? AND fecha_hora = ?";
    String insertarSQL = """
        INSERT INTO despachos (cliente_nombre, cliente_nit, cliente_telefono, cliente_direccion,
                               cliente_ciudad, fecha_hora, notas)
        VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
    String insertarItemSQL = """
        INSERT INTO items_despacho (id_despacho, producto, cantidad, precio_unitario)
        VALUES (?, ?, ?, ?)
        """;

    try (Connection conn = ConexionSQLite.conectar()) {
        // Verificar si ya existe
        try (PreparedStatement psVerificar = conn.prepareStatement(verificarSQL)) {
            psVerificar.setString(1, despacho.getClienteNombre());
            psVerificar.setString(2, despacho.getFechaHora().toString());
            ResultSet rs = psVerificar.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                System.out.println("⚠️ Despacho ya existente, no se insertó duplicado.");
                return false;
            }
        }

        // Insertar despacho
        try (PreparedStatement psInsertar = conn.prepareStatement(insertarSQL, Statement.RETURN_GENERATED_KEYS)) {
            psInsertar.setString(1, despacho.getClienteNombre());
            psInsertar.setString(2, despacho.getClienteNIT());
            psInsertar.setString(3, despacho.getClienteTelefono());
            psInsertar.setString(4, despacho.getClienteDireccion());
            psInsertar.setString(5, despacho.getClienteCiudad());
            psInsertar.setString(6, despacho.getFechaHora().toString());
            psInsertar.setString(7, despacho.getNotas());
            psInsertar.executeUpdate();

            // Obtener ID generado
            ResultSet generatedKeys = psInsertar.getGeneratedKeys();
            int idDespacho = 0;
            if (generatedKeys.next()) {
                idDespacho = generatedKeys.getInt(1);
            }

            // Insertar ítems asociados
            try (PreparedStatement psItem = conn.prepareStatement(insertarItemSQL)) {
                for (Despacho.Item item : despacho.getItems()) {
                    psItem.setInt(1, idDespacho);
                    psItem.setString(2, item.getNombreProducto());
                    psItem.setDouble(3, item.getCantidad());
                    psItem.setDouble(4, item.getPrecioUnitario());
                    psItem.addBatch();
                }
                psItem.executeBatch();
            }
        }

        System.out.println("✅ Despacho guardado correctamente en SQLite.");
        return true;

    } catch (SQLException e) {
        System.err.println("Error insertando despacho: " + e.getMessage());
        return false;
    }
}

    // === Crear tabla de despachos e ítems si no existen ===
public static void crearTablas() {
    String sqlDespachos = """
        CREATE TABLE IF NOT EXISTS despachos (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            cliente_nombre TEXT,
            cliente_nit TEXT,
            cliente_telefono TEXT,
            cliente_direccion TEXT,
            cliente_ciudad TEXT,
            fecha_hora TEXT,
            notas TEXT
        );
        """;

    String sqlItems = """
        CREATE TABLE IF NOT EXISTS items_despacho (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            id_despacho INTEGER,
            producto TEXT,
            cantidad REAL,
            precio_unitario REAL,
            FOREIGN KEY(id_despacho) REFERENCES despachos(id)
        );
        """;

    try (Connection conn = ConexionSQLite.conectar();
         Statement stmt = conn.createStatement()) {
        stmt.execute(sqlDespachos);
        stmt.execute(sqlItems);
        System.out.println("✅ Tablas de despachos creadas correctamente.");
    } catch (SQLException e) {
        System.err.println("Error creando tablas de despachos: " + e.getMessage());
    }
}

}
