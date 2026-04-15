package inventario;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;
import java.util.logging.Logger;

public class ConexionSQLite {

    private static final String APP_DATA_DIR = "inventario_data";
    private static final String DB_NAME = "inventario.db";
    private static String DB_URL;

    static {
        inicializarDirectorioYBaseDeDatos();
        inicializarDriverSQLite();
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


    private static void inicializarDriverSQLite() {
        try {
            Class.forName("org.sqlite.JDBC");
            return;
        } catch (ClassNotFoundException ignored) {
            // Intentar cargar desde un JAR local del proyecto
        }

        try {
            File directorioActual = new File(".");
            File[] jarsSQLite = directorioActual.listFiles((dir, name) ->
                    name.toLowerCase().startsWith("sqlite-jdbc") && name.toLowerCase().endsWith(".jar"));

            if (jarsSQLite == null || jarsSQLite.length == 0) {
                System.err.println("⚠️ No se encontró sqlite-jdbc en el classpath ni en el directorio actual.");
                return;
            }

            URL jarURL = jarsSQLite[0].toURI().toURL();
            URLClassLoader loader = new URLClassLoader(new URL[]{jarURL}, ConexionSQLite.class.getClassLoader());
            Class<?> driverClass = Class.forName("org.sqlite.JDBC", true, loader);
            Driver driver = (Driver) driverClass.getDeclaredConstructor().newInstance();
            DriverManager.registerDriver(new DriverShim(driver));
            System.out.println("✅ Driver SQLite cargado desde: " + jarsSQLite[0].getName());

        } catch (Exception e) {
            System.err.println("⚠️ No se pudo cargar automáticamente el driver SQLite: " + e.getMessage());
        }
    }

    private static class DriverShim implements Driver {
        private final Driver driver;

        DriverShim(Driver driver) {
            this.driver = driver;
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            return driver.connect(url, info);
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return driver.acceptsURL(url);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return driver.getPropertyInfo(url, info);
        }

        @Override
        public int getMajorVersion() {
            return driver.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return driver.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return driver.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() {
            try {
                return driver.getParentLogger();
            } catch (Exception e) {
                return Logger.getGlobal();
            }
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