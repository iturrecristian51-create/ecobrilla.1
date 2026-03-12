package inventario;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseSetup {
    public static void crearTablas() {
        String sqlInsumos = """
            CREATE TABLE IF NOT EXISTS insumos (
                lote TEXT PRIMARY KEY,
                nombre TEXT NOT NULL,
                fechaIngreso TEXT,
                cantidad REAL,
                unidad TEXT,
                proveedor TEXT,
                notas TEXT
            );
        """;

        String sqlProductosTerminados = """
            CREATE TABLE IF NOT EXISTS productos_terminados (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nombre TEXT,
                cantidad INTEGER,
                fechaProduccion TEXT
            );
        """;

        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlInsumos);
            stmt.execute(sqlProductosTerminados);
            System.out.println("📦 Tablas creadas correctamente en inventario.db");
        } catch (Exception e) {
            System.err.println("❌ Error creando tablas: " + e.getMessage());
        } finally {
          try (Connection conn = ConexionSQLite.conectar();
     Statement stmt = conn.createStatement()) {
    // código
} catch (SQLException e) {
    e.printStackTrace();
}
// conn y stmt se cierran automáticamente

        }
    }
  

}
