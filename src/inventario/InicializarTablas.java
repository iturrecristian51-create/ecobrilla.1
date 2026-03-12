package inventario;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class InicializarTablas {

    public static void crearTablas() {
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement()) {

            // === Tabla de Insumos ===
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS insumos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    lote TEXT,
                    nombre TEXT,
                    fecha_ingreso TEXT,
                    cantidad REAL,
                    unidad TEXT,
                    proveedor TEXT,
                    notas TEXT
                )
            """);

            // === Tabla de Productos Terminados ===
            // Borrar tabla vieja solo una vez
            stmt.execute("DROP TABLE IF EXISTS productos_terminados");

            // Crear tabla nueva con fecha_produccion
            stmt.execute("""
                CREATE TABLE productos_terminados (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT,
                    cantidad REAL,
                    unidad TEXT,
                    fecha_produccion TEXT,
                    notas TEXT
                )
            """);

            // === Tabla de Despachos ===
            stmt.execute("""
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
            """);

            // === Tabla de Ítems de Despacho ===
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS despacho_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_despacho INTEGER,
                    producto TEXT,
                    cantidad REAL,
                    FOREIGN KEY (id_despacho) REFERENCES despachos(id)
                )
            """);

            // === Tabla de Historial de Insumos ===
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS historial_insumos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    fecha TEXT,
                    lote TEXT,
                    nombre TEXT,
                    accion TEXT,
                    cantidad REAL,
                    observacion TEXT
                )
            """);

            // === Tabla de Movimientos Generales ===
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS movimientos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    tipo TEXT,
                    descripcion TEXT,
                    cantidad REAL,
                    fecha TEXT
                )
            """);

            System.out.println("✅ Todas las tablas fueron creadas correctamente.");

        } catch (SQLException e) {
            System.err.println("❌ Error al crear tablas: " + e.getMessage());
        }
    }

    // === MAIN para inicializar tablas desde consola ===
    public static void main(String[] args) {
        crearTablas();
        System.out.println("✅ Inicialización de tablas finalizada.");
    }
}
