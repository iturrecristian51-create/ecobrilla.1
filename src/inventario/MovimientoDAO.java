package inventario;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MovimientoDAO {

    // === Crear tabla movimientos si no existe ===
    public static void crearTabla() {
        String sql = "CREATE TABLE IF NOT EXISTS movimientos (" +
                     "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                     "fecha TEXT NOT NULL," +
                     "tipo TEXT NOT NULL," +
                     "referencia TEXT," +
                     "descripcion TEXT," +
                     "cantidad REAL" +
                     ");";
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            System.out.println("✅ Tabla 'movimientos' creada o ya existente.");
        } catch (SQLException e) {
            System.err.println("Error creando tabla movimientos: " + e.getMessage());
        }
    }

    // === Insertar un movimiento ===
    public static boolean insertarMovimiento(Movimiento movimiento) {
        String sql = "INSERT INTO movimientos (fecha, tipo, referencia, descripcion, cantidad) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, movimiento.getFecha());
            ps.setString(2, movimiento.getTipo());
            ps.setString(3, movimiento.getReferencia());
            ps.setString(4, movimiento.getDescripcion());
            ps.setDouble(5, movimiento.getCantidad());

            int filas = ps.executeUpdate();
            return filas > 0;

        } catch (SQLException e) {
            System.err.println("Error insertando movimiento: " + e.getMessage());
            return false;
        }
    }

    // === Obtener todos los movimientos ===
    public static List<Movimiento> obtenerTodos() {
        List<Movimiento> lista = new ArrayList<>();
        String sql = "SELECT * FROM movimientos ORDER BY fecha DESC";

        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Movimiento m = new Movimiento(
                        rs.getString("fecha"),
                        rs.getString("tipo"),
                        rs.getString("referencia"),
                        rs.getString("descripcion"),
                        rs.getDouble("cantidad")
                );
                lista.add(m);
            }

        } catch (SQLException e) {
            System.err.println("Error obteniendo movimientos: " + e.getMessage());
        }

        return lista;
    }

    // === Verificar si un movimiento ya existe ===
    public static boolean existeMovimiento(String tipo, String referencia) {
        String sql = "SELECT COUNT(*) FROM movimientos WHERE tipo = ? AND referencia = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, tipo);
            ps.setString(2, referencia);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            System.err.println("Error verificando movimiento: " + e.getMessage());
        }

        return false;
    }

    // === Registrar movimiento automáticamente desde un despacho ===
    public static void registrarMovimientoDespacho(Despacho despacho) {
        String tipo = "Despacho";
        String referencia = "ID-" + despacho.getId();
        String descripcion = "Cliente: " + despacho.getClienteNombre() + ", Items: " + despacho.getItems().size();
        double cantidadTotal = despacho.getTotal();

        if (!existeMovimiento(tipo, referencia)) {
            Movimiento m = new Movimiento(
                    despacho.getFechaHora().toString(),
                    tipo,
                    referencia,
                    descripcion,
                    cantidadTotal
            );
            insertarMovimiento(m);
        }
    }

   
}
