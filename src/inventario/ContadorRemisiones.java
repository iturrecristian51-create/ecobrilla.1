package inventario;

import java.sql.*;

public class ContadorRemisiones {
    private static final String TABLA_CONFIG = "configuracion_sistema";
    
    static {
        crearTablaConfiguracion();
    }

    private static void crearTablaConfiguracion() {
        String sql = """
            CREATE TABLE IF NOT EXISTS """ + TABLA_CONFIG + """ 
            (
                clave TEXT PRIMARY KEY,
                valor TEXT
            )
        """;
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            
            // Insertar valor inicial si no existe
            String insertSQL = "INSERT OR IGNORE INTO " + TABLA_CONFIG + " (clave, valor) VALUES ('contador_remisiones', '1')";
            stmt.execute(insertSQL);
            
        } catch (SQLException e) {
            System.err.println("Error creando tabla de configuración: " + e.getMessage());
        }
    }

    public static synchronized String obtenerNumeroRemision() {
        String sqlSelect = "SELECT valor FROM " + TABLA_CONFIG + " WHERE clave = 'contador_remisiones'";
        String sqlUpdate = "UPDATE " + TABLA_CONFIG + " SET valor = ? WHERE clave = 'contador_remisiones'";
        
        try (Connection conn = ConexionSQLite.conectar()) {
            conn.setAutoCommit(false);
            
            // Leer valor actual
            int contadorActual;
            try (PreparedStatement ps = conn.prepareStatement(sqlSelect)) {
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    contadorActual = Integer.parseInt(rs.getString("valor"));
                } else {
                    contadorActual = 1;
                }
            }
            
            // Incrementar y guardar
            int nuevoContador = contadorActual + 1;
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, String.valueOf(nuevoContador));
                ps.executeUpdate();
            }
            
            conn.commit();
            
            // Formatear número
            return String.format("%04d", contadorActual);
            
        } catch (SQLException e) {
            System.err.println("Error obteniendo número de remisión: " + e.getMessage());
            return "0001";
        }
    }
}