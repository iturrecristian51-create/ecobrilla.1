package inventario;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.io.File;

public class ConexionSQLite {

    private static final String APP_DATA_DIR = "inventario_data";
    private static final String DB_NAME = "inventario.db";
    private static String DB_URL;

    static {
        inicializarDirectorioYBaseDeDatos();
    }

    // === Inicializar directorio y ruta de la base de datos ===
    private static void inicializarDirectorioYBaseDeDatos() {
        try {
            // Crear directorio de la aplicación si no existe
            File appDir = new File(APP_DATA_DIR);
            if (!appDir.exists()) {
                boolean creado = appDir.mkdirs();
                if (creado) {
                    System.out.println("✅ Directorio de aplicación creado: " + appDir.getAbsolutePath());
                } else {
                    System.err.println("❌ No se pudo crear el directorio: " + APP_DATA_DIR);
                    // Fallback: usar directorio actual
                    DB_URL = "jdbc:sqlite:" + DB_NAME;
                    return;
                }
            }

            // Establecer la ruta completa de la base de datos
            DB_URL = "jdbc:sqlite:" + APP_DATA_DIR + File.separator + DB_NAME;
            System.out.println("📁 Base de datos ubicada en: " + new File(APP_DATA_DIR, DB_NAME).getAbsolutePath());

        } catch (Exception e) {
            System.err.println("❌ Error inicializando directorio: " + e.getMessage());
            // Fallback extremo
            DB_URL = "jdbc:sqlite:" + DB_NAME;
        }
    }

    // === Conectar a la base de datos ===
    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // === Cerrar conexión ===
    public static void cerrar(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error al cerrar la conexión: " + e.getMessage());
            }
        }
    }
}