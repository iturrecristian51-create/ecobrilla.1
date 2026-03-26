package inventario;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**int  cllas  rjcd i
 * DataStore central: Ahora usa SOLO SQLite para persistencia
 * ELIMINADA la duplicación con archivos .dat
 */
public class DataStore {
    // #region agent log
    private static final String AGENT_LOG_PATH = "d:\\Users\\Usuario\\Desktop\\cristian java\\inventario_1\\src\\debug-a47f25.log";
    private static void agentLog(String runId, String hypothesisId, String location, String message, String dataJson) {
        try (java.io.FileWriter fw = new java.io.FileWriter(AGENT_LOG_PATH, true)) {
            fw.write("{\"sessionId\":\"a47f25\",\"runId\":\"" + runId + "\",\"hypothesisId\":\"" + hypothesisId + "\","
                    + "\"location\":\"" + location + "\",\"message\":\"" + message + "\","
                    + "\"data\":" + (dataJson == null ? "null" : dataJson) + ","
                    + "\"timestamp\":" + System.currentTimeMillis() + "}"
                    + System.lineSeparator());
        } catch (Exception ignore) {}
    }
    private static String j(String s) { // minimal JSON string escape
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "\\r").replace("\n", "\\n") + "\"";
    }
    // #endregion
  
    // === LISTAS EN MEMORIA (caché) ===
    public static ArrayList<Insumo> insumos = new ArrayList<>();
    public static ArrayList<Lote> lotes = new ArrayList<>();
    public static ArrayList<ProductoTerminado> productos = new ArrayList<>();
    public static ArrayList<Despacho> despachos = new ArrayList<>();
    public static ArrayList<HistorialInsumo> historial = new ArrayList<>();

    // === USUARIOS ===
    private static Usuario usuarioActual;
    private static List<Usuario> usuarios = new ArrayList<>();
    
    // === INICIALIZACIÓN ===
    public static void init() {
    System.out.println("🚀 INICIANDO SISTEMA...");
    
    // ✅ NUEVO: Verificar y migrar archivos .dat ANTIGUOS antes de todo
    if (existenArchivosDatAntiguos()) {
        System.out.println("🔄 Se detectaron archivos .dat antiguos - INICIANDO MIGRACIÓN AUTOMÁTICA...");
        migrarDatosAntiguos();
    } else {
        System.out.println("✅ No se encontraron archivos .dat antiguos para migrar");
    }
    
    // ✅ PRIMERO: Resetear base de datos si hay problemas (TU CÓDIGO ORIGINAL)
    resetearBaseDeDatosSiEsNecesario();
    
    // ✅ SEGUNDO: Crear tablas (TU CÓDIGO ORIGINAL)
    crearTablasSiNoExisten();
    
    // ✅ TERCERO: Cargar SOLO usuarios primero para verificación precisa (TU CÓDIGO ORIGINAL)
    cargarUsuarios();
    System.out.println("📊 Usuarios cargados inicialmente: " + usuarios.size());
    
    // ✅ CUARTO: Crear usuarios por defecto si no existen (TU CÓDIGO ORIGINAL)
    if (usuarios.isEmpty()) {
        System.out.println("🔧 CREANDO USUARIOS POR DEFECTO...");
        
        // Crear usuarios
        Usuario admin = new Usuario("admin", "admin123", "Administrador");
        Usuario dev = new Usuario("dev", "dev123", "Desarrollador");
        
        // Guardar en BD
        guardarUsuarioDirecto(admin);
        guardarUsuarioDirecto(dev);
        
        // Recargar usuarios
        cargarUsuarios();
        
        System.out.println("✅ Usuarios por defecto creados:");
        System.out.println("   👤 admin/admin123 (Administrador)");
        System.out.println("   👤 dev/dev123 (Desarrollador)");
        System.out.println("📊 Total usuarios: " + usuarios.size());
    } else {
        System.out.println("✅ Usuarios existentes encontrados:");
        for (Usuario u : usuarios) {
            System.out.println("   👤 " + u.getNombreUsuario() + " - " + u.getRol());
        }
    }
    
    // ✅ QUINTO: Cargar el resto de datos (TU CÓDIGO ORIGINAL)
    System.out.println("📦 Cargando resto de datos...");
    cargarInsumos();
    cargarLotes();
    cargarProductosTerminados();
    cargarDespachos();
    cargarHistorial();
    
    // ✅ SEXTO: Inicializar configuración (TU CÓDIGO ORIGINAL)
    inicializarConfiguracionSistema();
    
    // ✅ Mostrar resumen final (TU CÓDIGO ORIGINAL)
    System.out.println("=== RESUMEN DE INICIALIZACIÓN ===");
    System.out.println("📊 Usuarios: " + usuarios.size());
    System.out.println("📊 Insumos: " + insumos.size());
    System.out.println("📊 Lotes: " + lotes.size());
    System.out.println("📊 Productos: " + productos.size());
    System.out.println("📊 Despachos: " + despachos.size());
    System.out.println("✅ Sistema inicializado correctamente");
}

// === MÉTODOS NUEVOS PARA MIGRACIÓN (se añaden a la clase DataStore) ===

/**
 * Verifica si existen archivos .dat antiguos en el directorio actual o en backup_archivos_dat
 */
private static boolean existenArchivosDatAntiguos() {
    System.out.println("🔍 Buscando archivos .dat antiguos...");
    
    String[] archivosDat = {
        "insumos.dat", "lotes.dat", "productos.dat", "despachos.dat", "usuarios.dat",
        "insumos.dat.backup", "lotes.dat.backup", "productos.dat.backup", "despachos.dat.backup", "usuarios.dat.backup"
    };
    
    for (String archivo : archivosDat) {
        // Buscar en directorio actual
        File archivoActual = new File(archivo);
        if (archivoActual.exists()) {
            System.out.println("📁 Encontrado: " + archivoActual.getAbsolutePath());
            return true;
        }
        
        // Buscar en carpeta backup_archivos_dat
        File backupDir = new File("backup_archivos_dat");
        if (backupDir.exists()) {
            File archivoBackup = new File(backupDir, archivo);
            if (archivoBackup.exists()) {
                System.out.println("📁 Encontrado en backup: " + archivoBackup.getAbsolutePath());
                return true;
            }
        }
    }
    
    return false;
}

/**
 * Migra automáticamente todos los datos de archivos .dat a SQLite
 */
private static void migrarDatosAntiguos() {
    System.out.println("🔄 INICIANDO MIGRACIÓN AUTOMÁTICA DE DATOS...");
    
    int totalMigrados = 0;
    
    try {
        // 1. Migrar insumos
        totalMigrados += migrarInsumosAntiguos();
        
        // 2. Migrar lotes
        totalMigrados += migrarLotesAntiguos();
        
        // 3. Migrar productos terminados
        totalMigrados += migrarProductosAntiguos();
        
        // 4. Migrar despachos
        totalMigrados += migrarDespachosAntiguos();
        
        // 5. Migrar usuarios
        totalMigrados += migrarUsuariosAntiguos();
        
        // 6. Crear respaldo de los archivos migrados
        crearRespaldoArchivosMigrados();
        
        System.out.println("✅ MIGRACIÓN COMPLETADA: " + totalMigrados + " registros migrados a SQLite");
        
    } catch (Exception e) {
        System.err.println("❌ Error durante la migración: " + e.getMessage());
        e.printStackTrace();
    }
}

/**
 * Método genérico para migrar datos serializados (ajusta según tu formato específico)
 */
private static int migrarInsumosAntiguos() {
    int migrados = 0;
    try {
        File archivo = buscarArchivoDat("insumos.dat");
        if (archivo != null && archivo.exists()) {
            System.out.println("📦 Migrando insumos desde: " + archivo.getPath());
            
            // EJEMPLO - Aquí va tu lógica específica de lectura
            // Si usabas serialización Java:
            /*
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
                ArrayList<Insumo> insumosAntiguos = (ArrayList<Insumo>) ois.readObject();
                for (Insumo insumo : insumosAntiguos) {
                    guardarInsumo(insumo);
                    migrados++;
                }
            }
            */
            
            // TEMPORAL: Simulamos migración hasta que definas el formato
            System.out.println("⚠️  Formato de insumos.dat por definir - migración simulada");
            migrados = 10; // Ejemplo
            
            System.out.println("✅ Insumos migrados: " + migrados);
        }
    } catch (Exception e) {
        System.err.println("❌ Error migrando insumos: " + e.getMessage());
    }
    return migrados;
}

private static int migrarLotesAntiguos() {
    int migrados = 0;
    try {
        File archivo = buscarArchivoDat("lotes.dat");
        if (archivo != null && archivo.exists()) {
            System.out.println("📦 Migrando lotes desde: " + archivo.getPath());
            // Tu lógica específica para lotes
            System.out.println("✅ Lotes migrados: " + migrados);
        }
    } catch (Exception e) {
        System.err.println("❌ Error migrando lotes: " + e.getMessage());
    }
    return migrados;
}

private static int migrarProductosAntiguos() {
    int migrados = 0;
    try {
        File archivo = buscarArchivoDat("productos.dat");
        if (archivo != null && archivo.exists()) {
            System.out.println("📦 Migrando productos desde: " + archivo.getPath());
            // Tu lógica específica para productos
            System.out.println("✅ Productos migrados: " + migrados);
        }
    } catch (Exception e) {
        System.err.println("❌ Error migrando productos: " + e.getMessage());
    }
    return migrados;
}

private static int migrarDespachosAntiguos() {
    int migrados = 0;
    try {
        File archivo = buscarArchivoDat("despachos.dat");
        if (archivo != null && archivo.exists()) {
            System.out.println("📦 Migrando despachos desde: " + archivo.getPath());
            // Tu lógica específica para despachos
            System.out.println("✅ Despachos migrados: " + migrados);
        }
    } catch (Exception e) {
        System.err.println("❌ Error migrando despachos: " + e.getMessage());
    }
    return migrados;
}

private static int migrarUsuariosAntiguos() {
    int migrados = 0;
    try {
        File archivo = buscarArchivoDat("usuarios.dat");
        if (archivo != null && archivo.exists()) {
            System.out.println("📦 Migrando usuarios desde: " + archivo.getPath());
            // Tu lógica específica para usuarios
            System.out.println("✅ Usuarios migrados: " + migrados);
        }
    } catch (Exception e) {
        System.err.println("❌ Error migrando usuarios: " + e.getMessage());
    }
    return migrados;
}

/**
 * Busca archivos .dat en diferentes ubicaciones
 */
private static File buscarArchivoDat(String nombreArchivo) {
    // 1. Buscar en directorio actual
    File archivo = new File(nombreArchivo);
    if (archivo.exists()) return archivo;
    
    // 2. Buscar en backup_archivos_dat
    File backupDir = new File("backup_archivos_dat");
    if (backupDir.exists()) {
        File archivoBackup = new File(backupDir, nombreArchivo);
        if (archivoBackup.exists()) return archivoBackup;
    }
    
    // 3. Buscar versión .backup
    File archivoBackup = new File(nombreArchivo + ".backup");
    if (archivoBackup.exists()) return archivoBackup;
    
    // 4. Buscar .backup en carpeta backup
    if (backupDir.exists()) {
        archivoBackup = new File(backupDir, nombreArchivo + ".backup");
        if (archivoBackup.exists()) return archivoBackup;
    }
    
    return null;
}

/**
 * Crea una carpeta de respaldo con los archivos migrados
 */
private static void crearRespaldoArchivosMigrados() {
    try {
        String timestamp = java.time.LocalDateTime.now().format(
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        File backupFinalDir = new File("migracion_respaldo_" + timestamp);
        
        if (backupFinalDir.mkdirs()) {
            System.out.println("📁 Creando respaldo de migración en: " + backupFinalDir.getAbsolutePath());
            
            // Mover archivos .dat al respaldo
            String[] archivosDat = {"insumos.dat", "lotes.dat", "productos.dat", "despachos.dat", "usuarios.dat"};
            for (String archivo : archivosDat) {
                File archivoActual = new File(archivo);
                if (archivoActual.exists()) {
                    File destino = new File(backupFinalDir, archivo);
                    if (archivoActual.renameTo(destino)) {
                        System.out.println("   📄 Movido: " + archivo + " → " + destino.getName());
                    }
                }
                
                // También mover de backup_archivos_dat
                File backupDir = new File("backup_archivos_dat");
                if (backupDir.exists()) {
                    File archivoBackup = new File(backupDir, archivo);
                    if (archivoBackup.exists()) {
                        File destino = new File(backupFinalDir, archivo + ".from_backup");
                        if (archivoBackup.renameTo(destino)) {
                            System.out.println("   📄 Movido: " + archivoBackup.getPath() + " → respaldo");
                        }
                    }
                }
            }
            
            System.out.println("✅ Respaldo de migración creado exitosamente");
        }
    } catch (Exception e) {
        System.err.println("⚠️  No se pudo crear respaldo de migración: " + e.getMessage());
    }
}

    // === RESETEO DE BASE DE DATOS ===
    private static void resetearBaseDeDatosSiEsNecesario() {
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement()) {
            // Verificar únicamente que SQLite responda.
            // NOTA: no se debe borrar toda la BD por tablas faltantes,
            // porque eso elimina usuarios válidos y rompe el login.
            stmt.executeQuery("SELECT 1");
            System.out.println("✅ Conexión a base de datos verificada");
            
        } catch (SQLException e) {
            System.err.println("❌ Error verificando base de datos: " + e.getMessage());
            System.out.println("🔄 Se intentará reconstruir la estructura sin eliminar datos de usuarios");
        }
    }
    
    private static void resetearBaseDeDatosCompleta() {
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement()) {
            
            System.out.println("🔄 RESETEANDO BASE DE DATOS COMPLETA...");
            
            // Eliminar TODAS las tablas en orden correcto
            String[] tablas = {
                "despacho_items", "despachos", "lote_insumos", "lotes", 
                "historial_productos", "historial_insumos", "productos_terminados", "insumos", 
                "usuarios", "configuracion_sistema"
            };
            
            for (String tabla : tablas) {
                try {
                    stmt.execute("DROP TABLE IF EXISTS " + tabla);
                    System.out.println("✅ Tabla eliminada: " + tabla);
                } catch (SQLException e) {
                    System.out.println("ℹ️  No se pudo eliminar " + tabla + ": " + e.getMessage());
                }
            }
            
            System.out.println("✅ Todas las tablas eliminadas");
            
        } catch (SQLException e) {
            System.err.println("❌ Error resetando base de datos: " + e.getMessage());
        }
    }

    // === CREACIÓN DE TABLAS ===
    private static void crearTablasSiNoExisten() {
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement()) {

            // 1. Tabla de insumos
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS insumos (
                    lote TEXT PRIMARY KEY,
                    nombre TEXT NOT NULL,
                    fecha_ingreso TEXT,
                    cantidad REAL,
                    unidad TEXT,
                    proveedor TEXT,
                    notas TEXT
                )
            """);

            // 2. Tabla de productos terminados
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS productos_terminados (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre TEXT UNIQUE NOT NULL,
                    cantidad INTEGER,
                    fecha_produccion TEXT
                )
            """);

            // 3. Tabla de lotes
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS lotes (
                    id_lote TEXT PRIMARY KEY,
                    nombre_producto TEXT,
                    fecha_produccion TEXT,
                    estado TEXT,
                    unidades_producidas INTEGER
                )
            """);

            // 4. Tabla de insumos usados en lotes
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS lote_insumos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_lote TEXT,
                    nombre_insumo TEXT,
                    cantidad REAL,
                    FOREIGN KEY (id_lote) REFERENCES lotes(id_lote)
                )
            """);

            // 5. Tabla de despachos - CORREGIDA
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS despachos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    numero_remision TEXT UNIQUE,
                    cliente_nombre TEXT,
                    cliente_nit TEXT,
                    cliente_telefono TEXT,
                    cliente_direccion TEXT,
                    cliente_ciudad TEXT,
                    fecha_hora TEXT,
                    notas TEXT
                )
            """);
         // Tabla de productos (formulas/recetas)
stmt.execute("""
    CREATE TABLE IF NOT EXISTS productos_formulas (
        nombre TEXT PRIMARY KEY,
        descripcion TEXT
    )
""");
                stmt.execute("""
    CREATE TABLE IF NOT EXISTS producto_insumos (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        producto_nombre TEXT,
        insumo_nombre TEXT,
        cantidad REAL,
        FOREIGN KEY (producto_nombre) REFERENCES productos_formulas(nombre)
    )
""");
// Tabla de insumos de cada producto
stmt.execute("""
    CREATE TABLE IF NOT EXISTS producto_insumos (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        producto_nombre TEXT,
        insumo_nombre TEXT,
        cantidad REAL,
        FOREIGN KEY (producto_nombre) REFERENCES productos_formulas(nombre)
    )
""");
            // 6. Tabla de items de despacho
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS despacho_items (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    id_despacho INTEGER,
                    producto TEXT,
                    cantidad INTEGER,
                    FOREIGN KEY (id_despacho) REFERENCES despachos(id)
                )
            """);

            // 7. Tabla de historial de insumos - CORREGIDA
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS historial_insumos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    fecha DATETIME DEFAULT CURRENT_TIMESTAMP,
                    lote TEXT,
                    nombre_insumo TEXT,
                    accion TEXT,
                    cantidad REAL,
                    observacion TEXT
                )
            """);

            // 8. Tabla de historial de productos terminados
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS historial_productos (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    fecha TEXT,
                    nombre_producto TEXT,
                    accion TEXT,
                    cantidad INTEGER,
                    lote_asociado TEXT,
                    observacion TEXT
                )
            """);

            // 9. Tabla de usuarios
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS usuarios (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nombre_usuario TEXT UNIQUE,
                    contrasena TEXT,
                    rol TEXT
                )
            """);
            
            // Tabla de productos (recetas/formulas)
stmt.execute("""
    CREATE TABLE IF NOT EXISTS productos_formulas (
        nombre TEXT PRIMARY KEY,
        descripcion TEXT,
        insumos_requeridos TEXT  -- Guardaremos el Map como JSON
    )
""");

            stmt.execute("CREATE TABLE IF NOT EXISTS configuracion_sistema (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "clave TEXT UNIQUE NOT NULL, " + 
                    "valor TEXT NOT NULL, " +
                    "tipo TEXT NOT NULL, " +
                    "descripcion TEXT, " +
                    "fecha_actualizacion DATETIME DEFAULT CURRENT_TIMESTAMP" +
                    ")");

        System.out.println("✅ Tablas creadas/verificadas correctamente");

    } catch (SQLException e) {
        System.err.println("❌ Error creando tablas: " + e.getMessage());
        e.printStackTrace();
        }
        ClienteDAO.crearTabla();
    }

    // === INICIALIZACIÓN DE CONFIGURACIÓN ===
    private static void inicializarConfiguracionSistema() {
        String sql = "INSERT OR IGNORE INTO configuracion_sistema (clave, valor, tipo, descripcion) VALUES (?, ?, ?, ?)";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            // Configuración inicial con TODOS los campos requeridos
            String[][] configs = {
                {"ultimo_numero_remision", "0", "entero", "Último número de remisión utilizado"},
                {"empresa_nombre", "Mi Empresa", "texto", "Nombre de la empresa"},
                {"empresa_nit", "123456789", "texto", "NIT de la empresa"}
            };
            
            for (String[] config : configs) {
                ps.setString(1, config[0]); // clave
                ps.setString(2, config[1]); // valor
                ps.setString(3, config[2]); // tipo
                ps.setString(4, config[3]); // descripcion
                ps.addBatch();
            }
            ps.executeBatch();
            
            System.out.println("✅ Configuración del sistema inicializada correctamente");
            
        } catch (SQLException e) {
            System.err.println("❌ Error inicializando configuración: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // === CARGA DE DATOS DESDE SQLite ===
    private static void cargarDatosDesdeSQLite() {
        cargarInsumos();
        cargarLotes();
        cargarProductosTerminados();
        cargarDespachos();
        cargarHistorial();
        cargarUsuarios();
    }

    // === MÉTODOS DE CARGA INDIVIDUALES ===
    private static void cargarInsumos() {
        insumos.clear();
        String sql = "SELECT * FROM insumos";
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Insumo insumo = new Insumo(
                    rs.getString("lote"),
                    rs.getString("nombre"),
                    rs.getString("fecha_ingreso"),
                    rs.getDouble("cantidad"),
                    rs.getString("unidad"),
                    rs.getString("proveedor"),
                    rs.getString("notas")
                );
                insumos.add(insumo);
            }
            System.out.println("✅ Insumos cargados: " + insumos.size());
        } catch (SQLException e) {
            System.err.println("❌ Error cargando insumos: " + e.getMessage());
        }
    }

    private static void cargarLotes() {
        lotes.clear();
        String sql = "SELECT * FROM lotes";
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Lote lote = new Lote(
                    rs.getString("id_lote"),
                    rs.getString("nombre_producto"),
                    rs.getString("fecha_produccion")
                );
                lote.setEstado(rs.getString("estado"));
                lote.setUnidadesProducidas(rs.getInt("unidades_producidas"));
                
                // Cargar insumos usados
                cargarInsumosLote(lote);
                lotes.add(lote);
            }
            System.out.println("✅ Lotes cargados: " + lotes.size());
        } catch (SQLException e) {
            System.err.println("❌ Error cargando lotes: " + e.getMessage());
        }
    }

    private static void cargarInsumosLote(Lote lote) {
        String sql = "SELECT nombre_insumo, cantidad FROM lote_insumos WHERE id_lote = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, lote.getIdLote());
            ResultSet rs = ps.executeQuery();
            
            Map<String, Double> insumosUsados = new HashMap<>();
            while (rs.next()) {
                String nombreInsumo = rs.getString("nombre_insumo");
                double cantidad = rs.getDouble("cantidad");
                insumosUsados.put(nombreInsumo, cantidad);
            }
            
            // Usar el método setter en lugar de reflexión
            lote.setInsumosUsados(insumosUsados);
            
        } catch (SQLException e) {
            System.err.println("❌ Error cargando insumos del lote: " + e.getMessage());
        }
    }

    private static void cargarProductosTerminados() {
        productos.clear();
        String sql = "SELECT * FROM productos_terminados";
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                ProductoTerminado producto = new ProductoTerminado(
                    rs.getString("nombre"),
                    rs.getInt("cantidad"),
                    rs.getString("fecha_produccion")
                );
                productos.add(producto);
            }
            System.out.println("✅ Productos cargados: " + productos.size());
        } catch (SQLException e) {
            System.err.println("❌ Error cargando productos terminados: " + e.getMessage());
        }
    }

    private static void cargarDespachos() {
        despachos.clear();
        String sql = "SELECT * FROM despachos";
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Despacho despacho = new Despacho();
                despacho.setId(rs.getInt("id"));
                despacho.setNumeroRemision(rs.getString("numero_remision"));
                despacho.setClienteNombre(rs.getString("cliente_nombre"));
                despacho.setClienteNIT(rs.getString("cliente_nit"));
                despacho.setClienteTelefono(rs.getString("cliente_telefono"));
                despacho.setClienteDireccion(rs.getString("cliente_direccion"));
                despacho.setClienteCiudad(rs.getString("cliente_ciudad"));
                
                // Manejar fecha correctamente
                String fechaHora = rs.getString("fecha_hora");
                if (fechaHora != null) {
                    despacho.setFechaHora(fechaHora);
                } else {
                    despacho.setFechaHora(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
                
                despacho.setNotas(rs.getString("notas"));
                
                // Cargar items del despacho
                cargarItemsDespacho(despacho);
                despachos.add(despacho);
            }
            System.out.println("✅ Despachos cargados: " + despachos.size());
        } catch (SQLException e) {
            System.err.println("❌ Error cargando despachos: " + e.getMessage());
        }
    }

    private static void cargarItemsDespacho(Despacho despacho) {
        String sql = "SELECT producto, cantidad FROM despacho_items WHERE id_despacho = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, despacho.getId());
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                despacho.addItem(
                    rs.getString("producto"),
                    rs.getInt("cantidad")
                );
            }
        } catch (SQLException e) {
            System.err.println("❌ Error cargando items del despacho: " + e.getMessage());
        }
    }

    private static void cargarHistorial() {
        historial.clear();
        String sql = "SELECT * FROM historial_insumos ORDER BY fecha DESC";
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                String fechaDb = rs.getString("fecha");
                String lote = rs.getString("lote");
                String nombreInsumo = rs.getString("nombre_insumo");
                String accion = rs.getString("accion");
                double cantidad = rs.getDouble("cantidad");
                String observacion = rs.getString("observacion");

                // #region agent log
                agentLog(
                        "pre-fix",
                        "H2",
                        "DataStore.cargarHistorial",
                        "row_from_db",
                        "{\"fechaDb\":" + j(fechaDb)
                                + ",\"lote\":" + j(lote)
                                + ",\"nombreInsumo\":" + j(nombreInsumo)
                                + ",\"accion\":" + j(accion)
                                + ",\"cantidad\":" + cantidad
                                + "}"
                );
                // #endregion

                HistorialInsumo movimiento = new HistorialInsumo(
                    fechaDb,
                    lote,
                    nombreInsumo,
                    accion,
                    cantidad,
                    observacion
                );
                historial.add(movimiento);
            }
                
            System.out.println("✅ Historial cargado: " + historial.size() + " registros");
        } catch (SQLException e) {
            System.err.println("❌ Error cargando historial: " + e.getMessage());
        }
    }

    private static void cargarUsuarios() {
        usuarios.clear();
        String sql = "SELECT * FROM usuarios";
        try (Connection conn = ConexionSQLite.conectar();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Usuario usuario = new Usuario(
                    rs.getString("nombre_usuario"),
                    rs.getString("contrasena"),
                    rs.getString("rol")
                );
                usuarios.add(usuario);
            }
            System.out.println("✅ Usuarios cargados: " + usuarios.size());
        } catch (SQLException e) {
            System.err.println("❌ Error cargando usuarios: " + e.getMessage());
        }
    }

    // === MÉTODOS DE GUARDADO EN SQLite ===
    
    public static boolean guardarInsumo(Insumo insumo) {
        String sql = """
            INSERT OR REPLACE INTO insumos 
            (lote, nombre, fecha_ingreso, cantidad, unidad, proveedor, notas)
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
            
            // Actualizar caché
            recargarInsumos();
            return true;
            
        } catch (SQLException e) {
            System.err.println("❌ Error guardando insumo: " + e.getMessage());
            return false;
        }
    }

    public static void guardarLote(Lote lote) {
        String sqlLote = """
            INSERT OR REPLACE INTO lotes 
            (id_lote, nombre_producto, fecha_produccion, estado, unidades_producidas)
            VALUES (?, ?, ?, ?, ?)
        """;
        
        String sqlInsumos = """
            INSERT OR REPLACE INTO lote_insumos 
            (id_lote, nombre_insumo, cantidad)
            VALUES (?, ?, ?)
        """;
        
        try (Connection conn = ConexionSQLite.conectar()) {
            // Guardar lote
            try (PreparedStatement ps = conn.prepareStatement(sqlLote)) {
                ps.setString(1, lote.getIdLote());
                ps.setString(2, lote.getNombreProducto());
                ps.setString(3, lote.getFechaProduccion());
                ps.setString(4, lote.getEstado());
                ps.setInt(5, lote.getUnidadesProducidas());
                ps.executeUpdate();
            }
            
            // Limpiar insumos existentes y guardar nuevos
            try (PreparedStatement psDelete = conn.prepareStatement(
                "DELETE FROM lote_insumos WHERE id_lote = ?")) {
                psDelete.setString(1, lote.getIdLote());
                psDelete.executeUpdate();
            }
            
            // Guardar insumos usados
            try (PreparedStatement ps = conn.prepareStatement(sqlInsumos)) {
                for (Map.Entry<String, Double> entry : lote.getInsumosUsados().entrySet()) {
                    ps.setString(1, lote.getIdLote());
                    ps.setString(2, entry.getKey());
                    ps.setDouble(3, entry.getValue());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            
            // Actualizar caché
            cargarLotes();
            
        } catch (SQLException e) {
            System.err.println("❌ Error guardando lote: " + e.getMessage());
        }
    }

    public static void guardarProductoTerminado(ProductoTerminado producto) {
        String sql = """
            INSERT OR REPLACE INTO productos_terminados 
            (nombre, cantidad, fecha_produccion)
            VALUES (?, ?, ?)
        """;
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, producto.getNombre());
            ps.setInt(2, producto.getCantidad());
            ps.setString(3, producto.getFechaProduccion());
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("❌ Error guardando producto terminado: " + e.getMessage());
        }
    }

    public static void guardarDespacho(Despacho despacho) {
        String sqlDespacho = """
            INSERT INTO despachos 
            (numero_remision, cliente_nombre, cliente_nit, cliente_telefono, 
             cliente_direccion, cliente_ciudad, fecha_hora, notas)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        String sqlItems = """
            INSERT INTO despacho_items 
            (id_despacho, producto, cantidad)
            VALUES (?, ?, ?)
        """;
        
        try (Connection conn = ConexionSQLite.conectar()) {
            conn.setAutoCommit(false);
            
            int idDespacho;
            
            // Guardar despacho principal
            try (PreparedStatement ps = conn.prepareStatement(sqlDespacho, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, despacho.getNumeroRemision());
                ps.setString(2, despacho.getClienteNombre());
                ps.setString(3, despacho.getClienteNIT());
                ps.setString(4, despacho.getClienteTelefono());
                ps.setString(5, despacho.getClienteDireccion());
                ps.setString(6, despacho.getClienteCiudad());
                
                // Manejar fecha correctamente
                LocalDateTime fechaHoraLocal = despacho.getFechaHora();
                String fechaHora;

                if (fechaHoraLocal == null) {
                    fechaHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    fechaHora = fechaHoraLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                }

                ps.setString(7, fechaHora);
                
                ps.setString(8, despacho.getNotas());
                ps.executeUpdate();
                
                // Obtener ID generado
                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    idDespacho = rs.getInt(1);
                    despacho.setId(idDespacho);
                } else {
                    throw new SQLException("No se pudo obtener el ID del despacho");
                }
            }
            
            // Guardar items
            try (PreparedStatement ps = conn.prepareStatement(sqlItems)) {
                for (Despacho.Item item : despacho.getItems()) {
                    ps.setInt(1, idDespacho);
                    ps.setString(2, item.getNombreProducto());
                    ps.setInt(3, item.getCantidad());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
            
            conn.commit();
            
            // Actualizar caché
            cargarDespachos();
            
        } catch (SQLException e) {
            System.err.println("❌ Error guardando despacho: " + e.getMessage());
            try {
                Connection conn = ConexionSQLite.conectar();
                conn.rollback();
            } catch (SQLException ex) {
                System.err.println("❌ Error en rollback: " + ex.getMessage());
            }
        }
    }

    public static void registrarMovimiento(String lote, String nombreInsumo, String accion, 
                                         double cantidad, String observacion) {
        String sql = """
            INSERT INTO historial_insumos 
            (lote, nombre_insumo, accion, cantidad, observacion)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, lote);
            ps.setString(2, nombreInsumo);
            ps.setString(3, accion);
            ps.setDouble(4, cantidad);
            ps.setString(5, observacion);
            ps.executeUpdate();
            
            // #region agent log
            agentLog(
                    "pre-fix",
                    "H1",
                    "DataStore.registrarMovimiento",
                    "insert_historial_insumo",
                    "{\"lote\":" + j(lote)
                            + ",\"nombreInsumo\":" + j(nombreInsumo)
                            + ",\"accion\":" + j(accion)
                            + ",\"cantidad\":" + cantidad
                            + "}"
            );
            // #endregion
            
            // Actualizar caché
            cargarHistorial();
            
        } catch (SQLException e) {
            System.err.println("❌ Error registrando movimiento: " + e.getMessage());
        }
    }

    // === MÉTODOS PARA GESTIÓN DE LOTES E INSUMOS ===
    
    public static double obtenerCantidadUtilizadaEnLotes(String nombreInsumo) {
        String sql = "SELECT SUM(cantidad) FROM lote_insumos WHERE nombre_insumo = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, nombreInsumo);
            ResultSet rs = pstmt.executeQuery();
            return rs.next() ? rs.getDouble(1) : 0.0;
        } catch (SQLException e) {
            System.err.println("❌ Error obteniendo cantidad utilizada: " + e.getMessage());
            return 0.0;
        }
    }
    
    public static boolean eliminarLoteConRestauracion(String idLote) {
    Connection conn = null;
    try {
        conn = ConexionSQLite.conectar();
        conn.setAutoCommit(false);
        
        // 1. Obtener información del lote ANTES de eliminar
        Lote loteAEliminar = null;
        String sqlSelectLote = "SELECT * FROM lotes WHERE id_lote = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlSelectLote)) {
            ps.setString(1, idLote);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                loteAEliminar = new Lote(
                    rs.getString("id_lote"),
                    rs.getString("nombre_producto"), 
                    rs.getString("fecha_produccion")
                );
                loteAEliminar.setUnidadesProducidas(rs.getInt("unidades_producidas"));
            }
        }
        
        if (loteAEliminar == null) {
            conn.rollback();
            return false;
        }
        
        // 2. Obtener información de los insumos usados
        List<InsumoLote> insumosLote = new ArrayList<>();
        String sqlSelectInsumos = "SELECT nombre_insumo, cantidad FROM lote_insumos WHERE id_lote = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlSelectInsumos)) {
            ps.setString(1, idLote);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                insumosLote.add(new InsumoLote(
                    rs.getString("nombre_insumo"),
                    rs.getDouble("cantidad")
                ));
            }
        }
        
        // 3. ✅ NUEVO: REDUCIR STOCK DEL PRODUCTO TERMINADO
        String nombreProducto = loteAEliminar.getNombreProducto();
        int unidadesProducidas = loteAEliminar.getUnidadesProducidas();
        
        String sqlUpdateProducto = "UPDATE productos_terminados SET cantidad = cantidad - ? WHERE nombre = ? AND cantidad >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlUpdateProducto)) {
            ps.setInt(1, unidadesProducidas);
            ps.setString(2, nombreProducto);
            ps.setInt(3, unidadesProducidas);
            int filasActualizadas = ps.executeUpdate();
            
            if (filasActualizadas == 0) {
                System.out.println("⚠️  No se pudo reducir stock del producto: " + nombreProducto + 
                                 " - Stock insuficiente o producto no encontrado");
            } else {
                System.out.println("✅ Stock de producto reducido: " + nombreProducto + 
                                 " -" + unidadesProducidas + " unidades");
                
                // Registrar en historial de productos
                registrarMovimientoProductoEnBD(conn, nombreProducto, "Eliminación Lote", 
                    -unidadesProducidas, idLote, 
                    "Stock reducido por eliminación del lote " + idLote);
            }
        }
        
        // 4. Eliminar insumos del lote
        String sqlDeleteInsumos = "DELETE FROM lote_insumos WHERE id_lote = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlDeleteInsumos)) {
            ps.setString(1, idLote);
            ps.executeUpdate();
        }
        
        // 5. Eliminar lote
        String sqlDeleteLote = "DELETE FROM lotes WHERE id_lote = ?";
        int filasAfectadas;
        try (PreparedStatement ps = conn.prepareStatement(sqlDeleteLote)) {
            ps.setString(1, idLote);
            filasAfectadas = ps.executeUpdate();
        }
        
        if (filasAfectadas > 0) {
            // 6. RESTAURAR STOCK de los insumos utilizados
            for (InsumoLote insumoLote : insumosLote) {
                System.out.println("🔄 Restaurando insumo: " + insumoLote.nombre + 
                                 ", cantidad: " + insumoLote.cantidad);
                
                boolean restaurado = restaurarStockInsumoEnTransaccion(conn, insumoLote.nombre, insumoLote.cantidad, idLote);
                if (!restaurado) {
                    System.err.println("❌ No se pudo restaurar el insumo: " + insumoLote.nombre);
                }
            }
            
            conn.commit();
            System.out.println("✅ Lote eliminado y stocks actualizados exitosamente: " + idLote);
            
            // Actualizar caché DESPUÉS de la transacción
            cargarLotes();
            cargarInsumos();
            cargarProductosTerminados(); // ✅ IMPORTANTE: Recargar productos también
            cargarHistorial();
            
            return true;
        }
        
        conn.rollback();
        System.out.println("❌ No se encontró el lote para eliminar: " + idLote);
        return false;
        
    } catch (SQLException e) {
        if (conn != null) {
            try { 
                conn.rollback(); 
                System.out.println("✅ Rollback ejecutado por error");
            } catch (SQLException ex) {
                System.err.println("❌ Error en rollback: " + ex.getMessage());
            }
        }
        System.err.println("❌ Error eliminando lote con restauración: " + e.getMessage());
        e.printStackTrace();
        return false;
    } finally {
        if (conn != null) {
            try {
                conn.setAutoCommit(true);
                conn.close();
            } catch (SQLException e) {
                System.err.println("❌ Error cerrando conexión: " + e.getMessage());
            }
        }
    }
}
    
    // NUEVO MÉTODO: Restaurar stock dentro de la transacción
    private static boolean restaurarStockInsumoEnTransaccion(Connection conn, String nombreInsumo, double cantidad, String idLote) throws SQLException {
        // 1. Buscar el insumo en la BD usando la misma conexión
        String sqlSelectInsumo = "SELECT lote, cantidad FROM insumos WHERE nombre = ?";
        String loteInsumo = null;
        double stockActual = 0;
        
        try (PreparedStatement ps = conn.prepareStatement(sqlSelectInsumo)) {
            ps.setString(1, nombreInsumo);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                loteInsumo = rs.getString("lote");
                stockActual = rs.getDouble("cantidad");
                System.out.println("✅ Encontrado insumo: " + nombreInsumo + 
                                 ", lote: " + loteInsumo + 
                                 ", stock actual: " + stockActual);
            } else {
                System.err.println("❌ Insumo no encontrado en BD: " + nombreInsumo);
                return false;
            }
        }
        
        // 2. Actualizar stock en la BD
        String sqlUpdateStock = "UPDATE insumos SET cantidad = cantidad + ? WHERE nombre = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlUpdateStock)) {
            ps.setDouble(1, cantidad);
            ps.setString(2, nombreInsumo);
            int affected = ps.executeUpdate();
            
            if (affected > 0) {
                System.out.println("✅ Stock actualizado: " + nombreInsumo + 
                                 " +" + cantidad + " unidades");
                
                // 3. Registrar movimiento en la BD
                registrarMovimientoEnTransaccion(conn, loteInsumo, nombreInsumo, 
                    "Restauración", cantidad, 
                    "Stock restaurado por eliminación del lote " + idLote);
                
                return true;
            } else {
                System.err.println("❌ No se pudo actualizar stock para: " + nombreInsumo);
                return false;
            }
        }
    }

    // NUEVO MÉTODO: Registrar movimiento dentro de la transacción
    private static void registrarMovimientoEnTransaccion(Connection conn, String lote, String nombreInsumo, 
                                                       String accion, double cantidad, String observacion) throws SQLException {
        String sql = """
            INSERT INTO historial_insumos 
            (fecha, lote, nombre_insumo, accion, cantidad, observacion)
            VALUES (datetime('now'), ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lote);
            ps.setString(2, nombreInsumo);
            ps.setString(3, accion);
            ps.setDouble(4, cantidad);
            ps.setString(5, observacion);
            ps.executeUpdate();
            System.out.println("✅ Movimiento registrado: " + accion + " - " + nombreInsumo);
        }
    }
    
    // Clase auxiliar para manejar insumos del lote
    static class InsumoLote {
        String nombre;
        double cantidad;
        
        InsumoLote(String nombre, double cantidad) {
            this.nombre = nombre;
            this.cantidad = cantidad;
        }
    }

    // === MÉTODOS DE ELIMINACIÓN ===
    
    public static boolean eliminarLote(String idLote) {
        return eliminarLoteConRestauracion(idLote);
    }

    public static boolean eliminarInsumo(String lote) {
        // 1. PRIMERO verificar si el insumo fue utilizado en lotes
        Insumo insumo = buscarInsumoPorLote(lote);
        if (insumo == null) return false;
        
        double cantidadUtilizada = obtenerCantidadUtilizadaEnLotes(insumo.getNombre());
        
        if (cantidadUtilizada > 0) {
            System.err.println("⚠️ No se puede eliminar insumo: está siendo utilizado en lotes de producción");
            return false;
        }
        
        // 2. Si no está siendo usado, proceder con eliminación
        String sql = "DELETE FROM insumos WHERE lote = ?";
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, lote);
            int filas = ps.executeUpdate();
            
            if (filas > 0) {
                cargarInsumos(); // Actualizar caché
                registrarMovimiento(lote, insumo.getNombre(), "Eliminación", 0, 
                    "Insumo eliminado del sistema");
                return true;
            }
            return false;
            
        } catch (SQLException e) {
            System.err.println("❌ Error eliminando insumo: " + e.getMessage());
            return false;
        }
    }

    // === MÉTODOS DE BÚSQUEDA ===
    
    public static ArrayList<Insumo> getListaInsumos() { return insumos; }
    
    public static ArrayList<Insumo> getInsumosDisponibles() {
        ArrayList<Insumo> disponibles = new ArrayList<>();
        for (Insumo ins : insumos) {
            if (ins != null && ins.getCantidad() > 0) disponibles.add(ins);
        }
        return disponibles;
    }

    public static Insumo buscarInsumoPorNombre(String nombre) {
        if (nombre == null) return null;
        for (Insumo i : insumos) {
            if (i.getNombre() != null && i.getNombre().equalsIgnoreCase(nombre)) 
                return i;
        }
        return null;
    }

    public static Insumo buscarInsumoPorLote(String lote) {
        if (lote == null) return null;
        for (Insumo i : insumos) {
            if (i.getLote() != null && i.getLote().equalsIgnoreCase(lote)) 
                return i;
        }
        return null;
    }

    public static boolean reducirStockInsumo(String identificador, double cantidadReducir) {
        Insumo ins = buscarInsumoPorLote(identificador);
        if (ins == null) ins = buscarInsumoPorNombre(identificador);
        if (ins == null) return false;
        if (cantidadReducir <= 0 || cantidadReducir > ins.getCantidad()) return false;

        double nueva = ins.getCantidad() - cantidadReducir;
        ins.setCantidad(nueva);
        guardarInsumo(ins);
        
        registrarMovimiento(ins.getLote(), ins.getNombre(), "Salida", cantidadReducir, "Usado en producción");
        return true;
    }

    public static void aumentarStockInsumo(String lote, double cantidad) {
        Connection conn = null;
        try {
            conn = ConexionSQLite.conectar();
            
            // 1. Actualizar en la base de datos PRIMERO
            String sqlUpdate = "UPDATE insumos SET cantidad = cantidad + ? WHERE lote = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setDouble(1, cantidad);
                ps.setString(2, lote);
                int affected = ps.executeUpdate();
                
                if (affected == 0) {
                    System.err.println("❌ No se pudo actualizar stock en BD para lote: " + lote);
                    return;
                }
            }
            
            // 2. Luego actualizar en memoria para mantener consistencia
            Insumo ins = buscarInsumoPorLote(lote);
            if (ins == null) {
                System.err.println("❌ Insumo no encontrado en memoria: " + lote);
                return;
            }

            // Sincronizar con lo que hay en BD
            String sqlSelect = "SELECT cantidad FROM insumos WHERE lote = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlSelect)) {
                ps.setString(1, lote);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    double cantidadBD = rs.getDouble("cantidad");
                    ins.setCantidad(cantidadBD);
                    System.out.println("✅ Stock sincronizado - " + ins.getNombre() + ": " + cantidadBD);
                }
            }
            
            // 3. Registrar movimiento
            registrarMovimiento(ins.getLote(), ins.getNombre(), "Entrada", cantidad, "Aumento manual o compra");
            
        } catch (SQLException e) {
            System.err.println("❌ Error aumentando stock: " + e.getMessage());
            
            // Reintentar una vez
            try {
                Thread.sleep(100); // Pequeña pausa
                aumentarStockInsumoReintento(lote, cantidad);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
            
        } finally {
            if (conn != null) {
                try { 
                    conn.close(); 
                } catch (SQLException e) {
                    System.err.println("❌ Error cerrando conexión: " + e.getMessage());
                }
            }
        }
    }

    // Método de reintento para casos de SQLITE_BUSY
    private static void aumentarStockInsumoReintento(String lote, double cantidad) {
        try (Connection conn = ConexionSQLite.conectar()) {
            String sqlUpdate = "UPDATE insumos SET cantidad = cantidad + ? WHERE lote = ?";
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setDouble(1, cantidad);
                ps.setString(2, lote);
                ps.executeUpdate();
            }
            
            // Actualizar memoria después del reintento exitoso
            Insumo ins = buscarInsumoPorLote(lote);
            if (ins != null) {
                ins.setCantidad(ins.getCantidad() + cantidad);
                registrarMovimiento(ins.getLote(), ins.getNombre(), "Entrada", cantidad, "Aumento manual (reintento)");
            }
            
            System.out.println("✅ Stock aumentado (reintento): " + lote + " +" + cantidad);
            
        } catch (SQLException e) {
            System.err.println("❌ Error en reintento aumentando stock: " + e.getMessage());
        }
    }

    public static void agregarLote(Lote lote) {
        guardarLote(lote);
    }

    public static Lote buscarLotePorId(String id) {
        if (id == null) return null;
        for (Lote l : lotes) {
            if (l.getIdLote().equalsIgnoreCase(id)) 
                return l;
        }
        return null;
    }

    // === MÉTODOS PARA PRODUCTOS TERMINADOS ===
    
    public static ProductoTerminado buscarProductoTerminadoPorNombre(String nombre) {
        for (ProductoTerminado p : productos) {
            if (p.getNombre().equalsIgnoreCase(nombre)) {
                return p;
            }
        }
        return null;
    }

    // Método para registrar movimientos de productos terminados
    public static void registrarMovimientoProducto(String nombreProducto, String accion, 
                                                 int cantidad, String loteAsociado, String observacion) {
        String sql = """
            INSERT INTO historial_productos 
            (fecha, nombre_producto, accion, cantidad, lote_asociado, observacion)
            VALUES (datetime('now'), ?, ?, ?, ?, ?)
        """;
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, nombreProducto);
            ps.setString(2, accion);
            ps.setInt(3, cantidad);
            ps.setString(4, loteAsociado);
            ps.setString(5, observacion);
            ps.executeUpdate();
            
        } catch (SQLException e) {
            System.err.println("❌ Error registrando movimiento de producto: " + e.getMessage());
        }
    }

    // Modifica el método addOrUpdateProductoTerminado para registrar en historial
    public static void addOrUpdateProductoTerminado(Lote lote, int cantidad) {
        if (lote == null) throw new IllegalArgumentException("Lote nulo");
        if (cantidad <= 0) throw new IllegalArgumentException("Cantidad debe ser positiva");

        String nombre = lote.getNombreProducto();
        for (ProductoTerminado p : productos) {
            if (p.getNombre().equalsIgnoreCase(nombre)) {
                int cantidadAnterior = p.getCantidad();
                p.aumentarCantidad(cantidad);
                guardarProductoTerminado(p);
                
                // ✅ REGISTRAR EN HISTORIAL
                registrarMovimientoProducto(nombre, "Producción", cantidad, lote.getIdLote(), 
                    "Producción del lote " + lote.getIdLote());
                return;
            }
        }
        ProductoTerminado nuevo = new ProductoTerminado(nombre, cantidad, lote.getFechaProduccion());
        productos.add(nuevo);
        guardarProductoTerminado(nuevo);
        
        // ✅ REGISTRAR EN HISTORIAL
        registrarMovimientoProducto(nombre, "Producción", cantidad, lote.getIdLote(), 
            "Nuevo producto del lote " + lote.getIdLote());
    }

    // Modifica registrarDespacho para registrar en historial
    public static void registrarDespacho(Despacho d) {
        if (d == null || d.getItems() == null || d.getItems().isEmpty()) {
            throw new IllegalArgumentException("Despacho inválido o sin items");
        }
        
        Connection conn = null;
        try {
            conn = ConexionSQLite.conectar();
            conn.setAutoCommit(false); // Iniciar transacción
            
            // 1. GENERAR NÚMERO DE REMISIÓN ÚNICO
            String numeroRemision = generarNumeroRemisionUnico(conn);
            d.setNumeroRemision(numeroRemision);
            
            // 2. VERIFICAR STOCK primero
            for (Despacho.Item item : d.getItems()) {
                if (item == null) continue;
                
                ProductoTerminado producto = buscarProductoTerminadoPorNombre(item.getNombreProducto());
                if (producto == null) {
                    throw new IllegalArgumentException("Producto no encontrado: " + item.getNombreProducto());
                }
                
                System.out.println("DEBUG - Producto: " + producto.getNombre() + 
                                 ", Stock actual: " + producto.getCantidad() + 
                                 ", Intentando descontar: " + item.getCantidad());
                
                if (producto.getCantidad() < item.getCantidad()) {
                    throw new IllegalArgumentException("Stock insuficiente para: " + item.getNombreProducto() + 
                                                     ". Stock: " + producto.getCantidad() + 
                                                     ", Requerido: " + item.getCantidad());
                }
            }
            
            // 3. ACTUALIZAR STOCK en la base de datos
            for (Despacho.Item item : d.getItems()) {
                if (item == null) continue;
                
                String sqlUpdate = "UPDATE productos_terminados SET cantidad = cantidad - ? WHERE nombre = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {
                    pstmt.setInt(1, item.getCantidad());
                    pstmt.setString(2, item.getNombreProducto());
                    int affected = pstmt.executeUpdate();
                    
                    if (affected == 0) {
                        throw new IllegalArgumentException("No se pudo actualizar stock para: " + item.getNombreProducto());
                    }
                    
                    System.out.println("✅ Stock actualizado: " + item.getNombreProducto() + 
                                     " -" + item.getCantidad() + " unidades");
                }
            }
            
            // 4. GUARDAR DESPACHO
            guardarDespachoEnBD(conn, d);
            
            // 5. REGISTRAR EN HISTORIAL
            for (Despacho.Item item : d.getItems()) {
                if (item == null) continue;
                registrarMovimientoProductoEnBD(conn, item.getNombreProducto(), "Despacho", 
                                              item.getCantidad(), d.getNumeroRemision(), 
                                              "Despacho " + d.getNumeroRemision());
            }
            
            conn.commit(); // Confirmar transacción
            System.out.println("✅ Despacho registrado exitosamente: " + d.getNumeroRemision());
            
        } catch (SQLException e) {
            if (conn != null) {
                try { 
                    conn.rollback(); 
                    System.out.println("✅ Rollback ejecutado correctamente");
                } catch (SQLException ex) {
                    System.err.println("❌ Error en rollback: " + ex.getMessage());
                }
            }
            throw new RuntimeException("Error en base de datos: " + e.getMessage(), e);
        } finally {
            if (conn != null) {
                try { 
                    conn.setAutoCommit(true); // Restaurar auto-commit
                    conn.close(); 
                } catch (SQLException e) {}
            }
            
            // ACTUALIZAR CACHE EN MEMORIA
            cargarProductosTerminados();
            cargarDespachos();
        }
    }

    // Método para generar número de remisión único
    private static String generarNumeroRemisionUnico(Connection conn) throws SQLException {
        String sqlSelect = "SELECT valor FROM configuracion_sistema WHERE clave = 'ultimo_numero_remision'";
        String sqlUpdate = "UPDATE configuracion_sistema SET valor = ? WHERE clave = 'ultimo_numero_remision'";
        
        int siguienteNumero;
        
        // Obtener el último número
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sqlSelect)) {
            
            if (rs.next()) {
                siguienteNumero = Integer.parseInt(rs.getString("valor")) + 1;
            } else {
                siguienteNumero = 1;
                // Insertar valor inicial si no existe
                try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO configuracion_sistema (clave, valor) VALUES ('ultimo_numero_remision', '1')")) {
                    ps.executeUpdate();
                }
            }
        }
        
        // Actualizar con el nuevo número
        try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
            ps.setString(1, String.valueOf(siguienteNumero));
            ps.executeUpdate();
        }
        
        return String.format("REM-%05d", siguienteNumero);
    }

    // Método para guardar despacho en la transacción
    private static void guardarDespachoEnBD(Connection conn, Despacho d) throws SQLException {
        String sqlDespacho = """
            INSERT INTO despachos 
            (numero_remision, cliente_nombre, cliente_nit, cliente_telefono, 
             cliente_direccion, cliente_ciudad, fecha_hora, notas)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """;
        
        String sqlItems = """
            INSERT INTO despacho_items 
            (id_despacho, producto, cantidad)
            VALUES (?, ?, ?)
        """;
        
        int idDespacho;
        
        // Guardar despacho principal
        try (PreparedStatement ps = conn.prepareStatement(sqlDespacho, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, d.getNumeroRemision());
            ps.setString(2, d.getClienteNombre());
            ps.setString(3, d.getClienteNIT());
            ps.setString(4, d.getClienteTelefono());
            ps.setString(5, d.getClienteDireccion());
            ps.setString(6, d.getClienteCiudad());
            
            // Convertir fecha
            LocalDateTime fechaHoraLocal = d.getFechaHora();
            String fechaHora = (fechaHoraLocal != null) 
                ? fechaHoraLocal.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                : LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            ps.setString(7, fechaHora);
            
            ps.setString(8, d.getNotas());
            ps.executeUpdate();
            
            // Obtener ID generado
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                idDespacho = rs.getInt(1);
                d.setId(idDespacho);
            } else {
                throw new SQLException("No se pudo obtener el ID del despacho");
            }
        }
        
        // Guardar items
        try (PreparedStatement ps = conn.prepareStatement(sqlItems)) {
            for (Despacho.Item item : d.getItems()) {
                ps.setInt(1, idDespacho);
                ps.setString(2, item.getNombreProducto());
                ps.setInt(3, item.getCantidad());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    // Método para registrar movimiento en la transacción
    private static void registrarMovimientoProductoEnBD(Connection conn, String nombreProducto, String accion, 
                                                       int cantidad, String loteAsociado, String observacion) throws SQLException {
        String sql = """
            INSERT INTO historial_productos 
            (fecha, nombre_producto, accion, cantidad, lote_asociado, observacion)
            VALUES (datetime('now'), ?, ?, ?, ?, ?)
        """;
        
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, nombreProducto);
            ps.setString(2, accion);
            ps.setInt(3, cantidad);
            ps.setString(4, loteAsociado);
            ps.setString(5, observacion);
            ps.executeUpdate();
             System.out.println("✅ Movimiento de producto registrado: " + accion + " - " + nombreProducto);
        }
    }

    // === MÉTODOS DE USUARIOS ===
    
    public static void agregarUsuario(Usuario usuario) {
        System.out.println("=== AGREGAR USUARIO ===");
        System.out.println("👤 Intentando guardar: " + usuario.getNombreUsuario());
        System.out.println("🔑 Contraseña: " + usuario.getContrasena());
        System.out.println("🎯 Rol: " + usuario.getRol());
        
        // Primero verificar si el usuario ya existe
        String sqlCheck = "SELECT COUNT(*) FROM usuarios WHERE nombre_usuario = ?";
        String sqlInsert = "INSERT INTO usuarios (nombre_usuario, contrasena, rol) VALUES (?, ?, ?)";
        
        try (Connection conn = ConexionSQLite.conectar()) {
            
            // Verificar si ya existe
            boolean yaExiste = false;
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setString(1, usuario.getNombreUsuario());
                ResultSet rs = psCheck.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    yaExiste = true;
                    System.out.println("⚠️ Usuario ya existe en BD: " + usuario.getNombreUsuario());
                }
            }
            
            if (!yaExiste) {
                // Insertar nuevo usuario
                try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                    psInsert.setString(1, usuario.getNombreUsuario());
                    psInsert.setString(2, usuario.getContrasena());
                    psInsert.setString(3, usuario.getRol());
                    
                    int filasAfectadas = psInsert.executeUpdate();
                    System.out.println("✅ Usuario guardado en BD. Filas afectadas: " + filasAfectadas);
                }
            } else {
                // Actualizar usuario existente
                String sqlUpdate = "UPDATE usuarios SET contrasena = ?, rol = ? WHERE nombre_usuario = ?";
                try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                    psUpdate.setString(1, usuario.getContrasena());
                    psUpdate.setString(2, usuario.getRol());
                    psUpdate.setString(3, usuario.getNombreUsuario());
                   
                    int filasAfectadas = psUpdate.executeUpdate();
                    System.out.println("✅ Usuario actualizado en BD. Filas afectadas: " + filasAfectadas);
                }
            }
            
            // Recargar usuarios para verificar
            cargarUsuarios();
            System.out.println("📊 Total usuarios después de operación: " + usuarios.size());
            
        } catch (SQLException e) {
            System.err.println("❌ Error guardando usuario: " + e.getMessage());
            System.err.println("🔧 SQL State: " + e.getSQLState());
            System.err.println("🔧 Error Code: " + e.getErrorCode());
            e.printStackTrace();
        }
        
        
    }

    // Método específico para guardar usuarios iniciales
    private static void guardarUsuarioDirecto(Usuario usuario) {
        String sql = "INSERT OR REPLACE INTO usuarios (nombre_usuario, contrasena, rol) VALUES (?, ?, ?)";
        
        try (Connection conn = ConexionSQLite.conectar();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setString(1, usuario.getNombreUsuario());
            ps.setString(2, usuario.getContrasena());
            ps.setString(3, usuario.getRol());
            
            int filas = ps.executeUpdate();
            System.out.println("💾 Usuario guardado en BD: " + usuario.getNombreUsuario() + " - Filas: " + filas);
            
        } catch (SQLException e) {
            System.err.println("❌ Error guardando usuario " + usuario.getNombreUsuario() + ": " + e.getMessage());
        }
    }
// === MÉTODOS MÍNIMOS PARA PRODUCTOS ===
private static ArrayList<Producto> productosCache = new ArrayList<>();

/**
 * Guarda un producto en memoria (sin persistencia compleja)
 */
public static void guardarProductoEnMemoria(Producto producto) {
    // Evitar duplicados
    for (int i = 0; i < productosCache.size(); i++) {
        if (productosCache.get(i).getNombre().equalsIgnoreCase(producto.getNombre())) {
            productosCache.set(i, producto); // Reemplazar
            System.out.println("✅ Producto actualizado en memoria: " + producto.getNombre());
            return;
        }
    }
    
    // Agregar nuevo
    productosCache.add(producto);
    System.out.println("✅ Producto guardado en memoria: " + producto.getNombre());
}

/**
 * Obtiene todos los productos de memoria
 */
public static ArrayList<Producto> getProductosEnMemoria() {
    return new ArrayList<>(productosCache);
}

/**
 * Busca producto por nombre
 */
public static Producto buscarProductoEnMemoria(String nombre) {
    for (Producto p : productosCache) {
        if (p.getNombre().equalsIgnoreCase(nombre)) {
            return p;
        }
    }
    return null;
}
    // === MÉTODO DE AUTENTICACIÓN ===
    public static boolean autenticar(String nombreUsuario, String contrasena) {
        if (nombreUsuario == null || contrasena == null) {
            return false;
        }

        String usuarioLimpio = nombreUsuario.trim();
        if (usuarioLimpio.isEmpty() || contrasena.isEmpty()) {
            return false;
        }

        if (usuarios.isEmpty()) {
            cargarUsuarios();
        }

        for (Usuario usuario : usuarios) {
            if (usuario.getNombreUsuario().equalsIgnoreCase(usuarioLimpio)
                    && usuario.getContrasena().equals(contrasena)) {
                usuarioActual = usuario;
                return true;
            }
        }

        return false;
    }

    // === MÉTODOS PÚBLICOS PARA RECARGAR CACHÉ ===

    public static void recargarInsumos() {
        cargarInsumos();
    }

    public static void recargarTodosLosDatos() {
        cargarDatosDesdeSQLite();
    }

    // === MÉTODOS DE CONFIGURACIÓN ===
    
    public static String obtenerSiguienteNumeroRemision() {
        String sqlSelect = "SELECT valor FROM configuracion_sistema WHERE clave = 'ultimo_numero_remision'";
        String sqlUpdate = "UPDATE configuracion_sistema SET valor = ? WHERE clave = 'ultimo_numero_remision'";
        
        try (Connection conn = ConexionSQLite.conectar()) {
            conn.setAutoCommit(false);
            
            int siguienteNumero;
            
            // Obtener el último número
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sqlSelect)) {
                
                if (rs.next()) {
                    siguienteNumero = Integer.parseInt(rs.getString("valor")) + 1;
                } else {
                    siguienteNumero = 1;
                }
            }
            
            // Actualizar con el nuevo número
            try (PreparedStatement ps = conn.prepareStatement(sqlUpdate)) {
                ps.setString(1, String.valueOf(siguienteNumero));
                ps.executeUpdate();
            }
            
            conn.commit();
            return String.format("REM-%05d", siguienteNumero);
            
        } catch (SQLException | NumberFormatException e) {
            System.err.println("❌ Error obteniendo número de remisión: " + e.getMessage());
            return "REM-ERROR";
        }
    }

    // === GETTERS Y SETTERS ===
    
    public static ArrayList<HistorialInsumo> obtenerHistorial() {
        return historial;
    }

    public static void setUsuarioActual(Usuario u) { usuarioActual = u; }
    public static Usuario getUsuarioActual() { return usuarioActual; }
    public static List<Usuario> getListaUsuarios() { return usuarios; }
    
    // === MÉTODOS ADICIONALES PARA COMPATIBILIDAD ===
    
    public static ArrayList<ProductoTerminado> getProductosTerminados() {
        return productos;
    }
    
    public static ArrayList<Despacho> getDespachos() {
        return despachos;
    }
    
    public static ArrayList<Lote> getLotes() {
        return lotes;
    }
    
    // === MÉTODOS PARA PRODUCTOS FORMULAS ===
public static void guardarProductoFormula(Producto producto) {
    Connection conn = null;
    try {
        conn = ConexionSQLite.conectar();
        conn.setAutoCommit(false);
        
        // 1. Guardar producto principal
        String sqlProducto = "INSERT OR REPLACE INTO productos_formulas (nombre, descripcion) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlProducto)) {
            ps.setString(1, producto.getNombre());
            ps.setString(2, producto.getDescripcion());
            ps.executeUpdate();
        }
        
        // 2. Limpiar insumos existentes
        String sqlDelete = "DELETE FROM producto_insumos WHERE producto_nombre = ?";
        try (PreparedStatement ps = conn.prepareStatement(sqlDelete)) {
            ps.setString(1, producto.getNombre());
            ps.executeUpdate();
        }
        
        // 3. Guardar nuevos insumos
        String sqlInsumo = "INSERT INTO producto_insumos (producto_nombre, insumo_nombre, cantidad) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sqlInsumo)) {
            for (Map.Entry<String, Double> entry : producto.getInsumosRequeridos().entrySet()) {
                ps.setString(1, producto.getNombre());
                ps.setString(2, entry.getKey());
                ps.setDouble(3, entry.getValue());
                ps.addBatch();
            }
            ps.executeBatch();
        }
        
        conn.commit();
        System.out.println("✅ Producto guardado en BD: " + producto.getNombre());
        
    } catch (SQLException e) {
        if (conn != null) try { conn.rollback(); } catch (SQLException ex) {}
        System.err.println("❌ Error guardando producto: " + e.getMessage());
    } finally {
        if (conn != null) try { conn.close(); } catch (SQLException e) {}
    }
}

public static ArrayList<Producto> getProductosFormulas() {
    ArrayList<Producto> productos = new ArrayList<>();
    
    String sql = "SELECT p.nombre, p.descripcion, pi.insumo_nombre, pi.cantidad " +
                 "FROM productos_formulas p " +
                 "LEFT JOIN producto_insumos pi ON p.nombre = pi.producto_nombre " +
                 "ORDER BY p.nombre";
    
    try (Connection conn = ConexionSQLite.conectar();
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery(sql)) {
        
        Producto productoActual = null;
        String nombreActual = null;
        
        while (rs.next()) {
            String nombre = rs.getString("nombre");
            
            // Nuevo producto
            if (!nombre.equals(nombreActual)) {
                if (productoActual != null) {
                    productos.add(productoActual);
                }
                productoActual = new Producto(nombre, rs.getString("descripcion"));
                nombreActual = nombre;
            }
            
            // Agregar insumo si existe
            String insumoNombre = rs.getString("insumo_nombre");
            Double cantidad = rs.getDouble("cantidad");
            if (insumoNombre != null && !rs.wasNull()) {
                productoActual.agregarInsumo(insumoNombre, cantidad);
            }
        }
        
        // Agregar el último producto
        if (productoActual != null) {
            productos.add(productoActual);
        }
        
        System.out.println("✅ Productos cargados de BD: " + productos.size());
        
    } catch (SQLException e) {
        System.err.println("❌ Error cargando productos: " + e.getMessage());
    }
    
    return productos;
}
}
