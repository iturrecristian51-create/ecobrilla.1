package inventario;

import java.io.*;
import java.util.ArrayList;

/**
 * Migrador de datos: Convierte archivos .dat a SQLite
 * Se ejecuta automáticamente al iniciar la aplicación
 */
public class MigradorDeDatos {
    
    private static final File DIR_DAT = new File("data");
    
    public static void migrarDatosSiEsNecesario() {
        System.out.println("🔍 Verificando migración de datos...");
        
        // Verificar si existen archivos .dat
        boolean existenArchivosDat = existenArchivosDat();
        boolean baseDatosVacia = estaBaseDatosVacia();
        
        if (existenArchivosDat && baseDatosVacia) {
            System.out.println("🚀 Migrando datos de archivos .dat a SQLite...");
            migrarTodosLosDatos();
            crearBackupArchivosDat();
            System.out.println("✅ Migración completada exitosamente");
        } else if (existenArchivosDat && !baseDatosVacia) {
            System.out.println("⚠️  Tienes datos en ambos sistemas. Se usarán los de SQLite.");
        } else {
            System.out.println("✅ No se requiere migración");
        }
    }
    
    private static boolean existenArchivosDat() {
        File[] archivos = DIR_DAT.listFiles((dir, name) -> name.endsWith(".dat"));
        return archivos != null && archivos.length > 0;
    }
    
    private static boolean estaBaseDatosVacia() {
        try {
            // Verificar si hay insumos en SQLite
            String sql = "SELECT COUNT(*) FROM insumos";
            try (var conn = ConexionSQLite.conectar();
                 var stmt = conn.createStatement();
                 var rs = stmt.executeQuery(sql)) {
                return rs.getInt(1) == 0;
            }
        } catch (Exception e) {
            return true; // Si hay error, asumir que está vacía
        }
    }
    
    @SuppressWarnings("unchecked")
    private static void migrarTodosLosDatos() {
        try {
            // === MIGRAR INSUMOS ===
            File archivoInsumos = new File(DIR_DAT, "insumos.dat");
            if (archivoInsumos.exists()) {
                System.out.println("📦 Migrando insumos...");
                ArrayList<Insumo> insumos = cargarArchivo(archivoInsumos);
                for (Insumo insumo : insumos) {
                    DataStore.guardarInsumo(insumo);
                }
                System.out.println("✅ " + insumos.size() + " insumos migrados");
            }
            
            // === MIGRAR LOTES ===
            File archivoLotes = new File(DIR_DAT, "lotes.dat");
            if (archivoLotes.exists()) {
                System.out.println("🏭 Migrando lotes...");
                ArrayList<Lote> lotes = cargarArchivo(archivoLotes);
                for (Lote lote : lotes) {
                    DataStore.guardarLote(lote);
                }
                System.out.println("✅ " + lotes.size() + " lotes migrados");
            }
            
            // === MIGRAR PRODUCTOS TERMINADOS ===
            File archivoProductos = new File(DIR_DAT, "productos.dat");
            if (archivoProductos.exists()) {
                System.out.println("📊 Migrando productos terminados...");
                ArrayList<ProductoTerminado> productos = cargarArchivo(archivoProductos);
                for (ProductoTerminado producto : productos) {
                    DataStore.guardarProductoTerminado(producto);
                }
                System.out.println("✅ " + productos.size() + " productos migrados");
            }
            
            // === MIGRAR DESPACHOS ===
            File archivoDespachos = new File(DIR_DAT, "despachos.dat");
            if (archivoDespachos.exists()) {
                System.out.println("🚚 Migrando despachos...");
                ArrayList<Despacho> despachos = cargarArchivo(archivoDespachos);
                for (Despacho despacho : despachos) {
                    DataStore.guardarDespacho(despacho);
                }
                System.out.println("✅ " + despachos.size() + " despachos migrados");
            }
            
            // === MIGRAR HISTORIAL ===
            File archivoHistorial = new File(DIR_DAT, "historial.dat");
            if (archivoHistorial.exists()) {
                System.out.println("📋 Migrando historial...");
                ArrayList<HistorialInsumo> historial = cargarArchivo(archivoHistorial);
                for (HistorialInsumo movimiento : historial) {
                    // Re-registrar el movimiento en SQLite
                    DataStore.registrarMovimiento(
                        movimiento.getLote(),
                        movimiento.getNombreInsumo(),
                        movimiento.getAccion(),
                        movimiento.getCantidad(),
                        movimiento.getObservacion()
                    );
                }
                System.out.println("✅ " + historial.size() + " registros de historial migrados");
            }
            
            // === MIGRAR USUARIOS ===
            File archivoUsuarios = new File("usuarios.dat");
            if (archivoUsuarios.exists()) {
                System.out.println("👥 Migrando usuarios...");
                ArrayList<Usuario> usuarios = cargarArchivo(archivoUsuarios);
                for (Usuario usuario : usuarios) {
                    DataStore.agregarUsuario(usuario);
                }
                System.out.println("✅ " + usuarios.size() + " usuarios migrados");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Error durante la migración: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @SuppressWarnings("unchecked")
    private static <T> ArrayList<T> cargarArchivo(File archivo) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(archivo))) {
            return (ArrayList<T>) ois.readObject();
        } catch (Exception e) {
            System.err.println("❌ Error cargando archivo " + archivo.getName() + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private static void crearBackupArchivosDat() {
        File backupDir = new File("backup_archivos_dat");
        if (!backupDir.exists()) {
            backupDir.mkdir();
        }
        
        File[] archivosDat = DIR_DAT.listFiles((dir, name) -> name.endsWith(".dat"));
        if (archivosDat != null) {
            for (File archivo : archivosDat) {
                File backup = new File(backupDir, archivo.getName() + ".backup");
                try {
                    copiarArchivo(archivo, backup);
                    System.out.println("💾 Backup creado: " + backup.getPath());
                } catch (IOException e) {
                    System.err.println("❌ Error creando backup de " + archivo.getName());
                }
            }
        }
        
        // También backup de usuarios.dat si existe
        File usuariosDat = new File("usuarios.dat");
        if (usuariosDat.exists()) {
            File backup = new File(backupDir, "usuarios.dat.backup");
            try {
                copiarArchivo(usuariosDat, backup);
                System.out.println("💾 Backup creado: " + backup.getPath());
            } catch (IOException e) {
                System.err.println("❌ Error creando backup de usuarios.dat");
            }
        }
    }
    
    private static void copiarArchivo(File origen, File destino) throws IOException {
        try (FileInputStream fis = new FileInputStream(origen);
             FileOutputStream fos = new FileOutputStream(destino)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = fis.read(buffer)) > 0) {
                fos.write(buffer, 0, length);
            }
        }
    }
}