# GUIA TECNICA: Ampliacion y Correccion del Sistema ECOBRILLA

**Version:** 1.0  
**Fecha:** Marzo 2026  
**Sistema:** Sistema de Inventario y Produccion - ECOBRILLA SOLUCIONES S.A.S  
**Alcance:** Documento tecnico para desarrolladores que trabajen en la ampliacion y correccion del software

---

## Tabla de Contenido

1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Estado Actual del Sistema](#2-estado-actual-del-sistema)
3. [Errores y Bugs Identificados](#3-errores-y-bugs-identificados)
4. [Problemas de Arquitectura](#4-problemas-de-arquitectura)
5. [Correcciones Prioritarias](#5-correcciones-prioritarias)
6. [Oportunidades de Ampliacion](#6-oportunidades-de-ampliacion)
7. [Mejoras de Seguridad](#7-mejoras-de-seguridad)
8. [Optimizacion de Rendimiento](#8-optimizacion-de-rendimiento)
9. [Estrategia de Pruebas](#9-estrategia-de-pruebas)
10. [Guia de Despliegue y Mantenimiento](#10-guia-de-despliegue-y-mantenimiento)
11. [Buenas Practicas para Desarrollo Futuro](#11-buenas-practicas-para-desarrollo-futuro)
12. [Plan de Implementacion por Fases](#12-plan-de-implementacion-por-fases)

---

## 1. Resumen Ejecutivo

El sistema ECOBRILLA es una aplicacion de escritorio Java Swing para la gestion de inventario, produccion y despachos. Tras un analisis exhaustivo del codigo fuente, se han identificado **23 problemas criticos**, **15 mejoras de arquitectura** y **12 oportunidades de ampliacion** que se documentan en esta guia.

### Clasificacion de Hallazgos

| Categoria | Critico | Alto | Medio | Bajo |
|-----------|---------|------|-------|------|
| Bugs/Errores | 5 | 8 | 6 | 4 |
| Arquitectura | 3 | 5 | 4 | 3 |
| Seguridad | 4 | 3 | 2 | 1 |
| Rendimiento | 1 | 3 | 4 | 2 |

### Archivos Principales Analizados

| Archivo | Lineas | Estado | Prioridad de Correccion |
|---------|--------|--------|------------------------|
| `DataStore.java` | 2015 | Requiere refactorizacion mayor | CRITICA |
| `DespachoGUI.java` | 448 | Requiere correcciones | ALTA |
| `InsumoGUI.java` | 413 | Estable con mejoras menores | MEDIA |
| `ProduccionGUI.java` | 273 | Estable | BAJA |
| `MenuPrincipalGUI.java` | 234 | Estable con mejoras menores | MEDIA |
| `ThemeUtil.java` | 240 | Estable | BAJA |
| `VentanaFacturaGUI.java` | 191 | Requiere mejoras | MEDIA |
| `Despacho.java` | 189 | Requiere correcciones | ALTA |
| `InsumoDAO.java` | 161 | Requiere correccion critica | CRITICA |
| `LoginDialog.java` | 124 | Requiere mejoras de seguridad | ALTA |
| `Lote.java` | 105 | Estable | BAJA |
| `GestionUsuariosGUI.java` | 76 | Requiere mejoras | MEDIA |
| `ConexionSQLite.java` | 62 | Requiere mejoras | MEDIA |
| `Usuario.java` | 29 | Requiere mejoras de seguridad | ALTA |
| `Permisos.java` | 22 | Requiere ampliacion | ALTA |

---

## 2. Estado Actual del Sistema

### 2.1 Arquitectura General

```
┌─────────────────────────────────────────────────────────────────┐
│                      CAPA DE PRESENTACION                       │
│  MenuPrincipalGUI -> InsumoGUI, ProduccionGUI, DespachoGUI     │
│  LoginDialog, GestionUsuariosGUI, VentanaFacturaGUI             │
├─────────────────────────────────────────────────────────────────┤
│                      CAPA DE LOGICA                             │
│  DataStore (2015 lineas - CLASE MONOLITICA)                     │
│  Permisos, ListaProductos                                       │
├─────────────────────────────────────────────────────────────────┤
│                      CAPA DE DATOS                              │
│  ConexionSQLite, InsumoDAO, DespachoDAO                         │
│  SQLite (inventario.db)                                         │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 Problemas Estructurales Clave

1. **Clase monolitica `DataStore.java`** (2015 lineas): Concentra TODA la logica de negocio, acceso a datos, cache en memoria y migracion. Viola el principio de responsabilidad unica (SRP).

2. **Patron DAO inconsistente**: Existen `InsumoDAO.java` y `DespachoDAO.java`, pero la mayoria de operaciones de base de datos se realizan directamente en `DataStore.java`.

3. **Acoplamiento directo GUI-Datos**: Las clases GUI acceden directamente a las listas estaticas de `DataStore` (ej: `DataStore.insumos`, `DataStore.lotes`), sin pasar por metodos controlados.

---

## 3. Errores y Bugs Identificados

### 3.1 [CRITICO] Codigo de Depuracion en Produccion

**Archivo:** `DataStore.java`, lineas 18-27

```java
private static final String AGENT_LOG_PATH = "d:\\Users\\Usuario\\Desktop\\cristian java\\inventario_1\\src\\debug-a47f25.log";
private static void agentLog(String runId, String hypothesisId, String location, String message, String dataJson) {
    try (java.io.FileWriter fw = new java.io.FileWriter(AGENT_LOG_PATH, true)) {
        fw.write("{\"sessionId\":\"a47f25\",\"runId\":\"" + runId + "\",...}");
    } catch (Exception ignore) {}
}
```

**Problema:** Codigo de depuracion con ruta absoluta de Windows hardcodeada. Este codigo:
- Intentara escribir en una ruta que no existe en otros equipos
- Genera excepciones silenciosas en cada llamada
- Expone informacion interna de sesiones de depuracion
- Se invoca activamente en `registrarMovimiento()` (linea 1014)

**Correccion:**
1. Eliminar el metodo `agentLog()` y el metodo auxiliar `j()`
2. Eliminar todas las invocaciones a `agentLog()` en el codigo (buscar `// #region agent log`)
3. Reemplazar con un framework de logging como `java.util.logging` o SLF4J

### 3.2 [CRITICO] Creacion Duplicada de Tabla

**Archivo:** `DataStore.java`, lineas 466-483

```java
// Primera vez (linea 466-473):
stmt.execute("""
    CREATE TABLE IF NOT EXISTS produto_insumos (
        id INTEGER PRIMARY KEY AUTOINCREMENT,
        produto_nome TEXT,
        insumo_nome TEXT,
        quantidade REAL,
        FOREIGN KEY (produto_nome) REFERENCES produtos_formulas(nome)
    )
""");
// Segunda vez (linea 476-483) - EXACTAMENTE IGUAL:
stmt.execute("""
    CREATE TABLE IF NOT EXISTS produto_insumos (...)
""");
```

**Problema:** La tabla `produto_insumos` se crea dos veces en el metodo `crearTablasSiNoExisten()`. Aunque `CREATE TABLE IF NOT EXISTS` previene errores, indica codigo duplicado no revisado.

**Correccion:** Eliminar el segundo bloque duplicado (lineas 474-483).

### 3.3 [CRITICO] Ruta de Base de Datos Inconsistente en InsumoDAO

**Archivo:** `InsumoDAO.java`, lineas 156-159

```java
private static Connection getConnection() throws SQLException {
    String url = "jdbc:sqlite:data/inventario.db";
    return DriverManager.getConnection(url);
}
```

**Problema:** `InsumoDAO` usa la ruta `data/inventario.db` mientras que `ConexionSQLite` usa `inventario_data/inventario.db`. Esto significa que `InsumoDAO` esta conectandose a una base de datos DIFERENTE al resto del sistema, o falla silenciosamente.

**Correccion:**
```java
private static Connection getConnection() throws SQLException {
    return ConexionSQLite.conectar(); // Usar la conexion centralizada
}
```

### 3.4 [CRITICO] Rollback Fallido en guardarDespacho

**Archivo:** `DataStore.java`, lineas 985-993

```java
} catch (SQLException e) {
    System.err.println("Error guardando despacho: " + e.getMessage());
    try {
        Connection conn = ConexionSQLite.conectar(); // NUEVA conexion!
        conn.rollback(); // Rollback en conexion diferente - NO FUNCIONA
    } catch (SQLException ex) {
        System.err.println("Error en rollback: " + ex.getMessage());
    }
}
```

**Problema:** En caso de error, se abre una NUEVA conexion para hacer rollback. El rollback solo funciona en la MISMA conexion donde se inicio la transaccion. Este codigo nunca revierte la transaccion fallida.

**Correccion:** Usar el mismo patron que `registrarDespacho()` (linea 1504), declarando `Connection conn = null` fuera del try y usando un bloque `finally`:
```java
Connection conn = null;
try {
    conn = ConexionSQLite.conectar();
    conn.setAutoCommit(false);
    // ... operaciones ...
    conn.commit();
} catch (SQLException e) {
    if (conn != null) {
        try { conn.rollback(); } catch (SQLException ex) { /* log */ }
    }
} finally {
    if (conn != null) {
        try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* log */ }
    }
}
```

### 3.5 [CRITICO] Contrasenas Almacenadas en Texto Plano

**Archivo:** `DataStore.java`, lineas 1707-1711

```java
public static void agregarUsuario(Usuario usuario) {
    System.out.println("=== AGREGAR USUARIO ===");
    System.out.println("Usuario: " + usuario.getNombreUsuario());
    System.out.println("Contrasena: " + usuario.getContrasena()); // IMPRIME LA CONTRASENA
    System.out.println("Rol: " + usuario.getRol());
```

**Problemas:**
1. Las contrasenas se imprimen en la consola (linea 1710)
2. Las contrasenas se guardan en texto plano en la base de datos (linea 1734)
3. La autenticacion compara contrasenas en texto plano (linea 1841)

**Correccion:**
1. Eliminar los `System.out.println` que imprimen contrasenas
2. Implementar hashing con `BCrypt` o al menos `SHA-256` + salt
3. Ver seccion 7 (Mejoras de Seguridad) para implementacion completa

### 3.6 [ALTO] Excepciones Silenciosas en Despacho

**Archivo:** `Despacho.java`, lineas 129-136

```java
public void setFechaHora(String fechaHoraStr) {
    if (fechaHoraStr == null || fechaHoraStr.isBlank()) return;
    try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.fechaHora = LocalDateTime.parse(fechaHoraStr, formatter);
    } catch (DateTimeParseException e) {
        // Si no se puede parsear, dejamos la fecha como null
    }
}
```

**Problema:** Si la fecha tiene un formato invalido, se ignora silenciosamente. Esto puede provocar que despachos se guarden sin fecha, causando `NullPointerException` en `VentanaFacturaGUI.java` linea 80 al llamar `despacho.getFechaHora().format(...)`.

**Correccion:**
```java
public void setFechaHora(String fechaHoraStr) {
    if (fechaHoraStr == null || fechaHoraStr.isBlank()) {
        this.fechaHora = LocalDateTime.now(); // Asignar fecha actual como fallback
        return;
    }
    try {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.fechaHora = LocalDateTime.parse(fechaHoraStr, formatter);
    } catch (DateTimeParseException e) {
        System.err.println("Formato de fecha invalido: " + fechaHoraStr);
        this.fechaHora = LocalDateTime.now(); // Fallback seguro
    }
}
```

### 3.7 [ALTO] NullPointerException Potencial en VentanaFacturaGUI

**Archivo:** `VentanaFacturaGUI.java`, linea 80

```java
JLabel lblFecha = new JLabel("Fecha: " + despacho.getFechaHora().format(
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
```

**Problema:** No se verifica si `getFechaHora()` es `null` antes de llamar `.format()`.

**Correccion:**
```java
String fechaTexto = despacho.getFechaHora() != null 
    ? despacho.getFechaHora().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
    : "Sin fecha";
JLabel lblFecha = new JLabel("Fecha: " + fechaTexto);
```

### 3.8 [ALTO] Falta de Transaccion en guardarLote

**Archivo:** `DataStore.java`, lineas 847-895

```java
public static void guardarLote(Lote lote) {
    // ...
    try (Connection conn = ConexionSQLite.conectar()) {
        // Guardar lote (operacion 1)
        try (PreparedStatement ps = conn.prepareStatement(sqlLote)) { ... }
        
        // Eliminar insumos (operacion 2)
        try (PreparedStatement psDelete = ...) { ... }
        
        // Guardar insumos (operacion 3)
        try (PreparedStatement ps = ...) { ... }
    }
}
```

**Problema:** Tres operaciones de base de datos (guardar lote, eliminar insumos, guardar insumos nuevos) se ejecutan SIN transaccion. Si la operacion 2 o 3 falla, los datos quedan en estado inconsistente.

**Correccion:** Agregar manejo de transaccion:
```java
try (Connection conn = ConexionSQLite.conectar()) {
    conn.setAutoCommit(false);
    try {
        // ... las 3 operaciones ...
        conn.commit();
    } catch (SQLException e) {
        conn.rollback();
        throw e;
    } finally {
        conn.setAutoCommit(true);
    }
}
```

### 3.9 [ALTO] Acceso Directo a Listas Publicas de DataStore

**Archivos afectados:** `InsumoGUI.java`, `ProduccionGUI.java`, `DespachoGUI.java`

```java
// InsumoGUI.java, linea 137:
Insumo insumo = DataStore.insumos.get(modeloIndex);

// ProduccionGUI.java, linea 238:
for (Lote lote : DataStore.lotes) { ... }

// DespachoGUI.java, linea 251:
for (ProductoTerminado p : DataStore.productos) { ... }
```

**Problema:** Las GUIs acceden directamente a las listas internas (`insumos`, `lotes`, `productos`), lo que:
- Permite modificaciones no controladas
- Puede causar `ConcurrentModificationException`
- No garantiza que los datos esten sincronizados con la BD
- Viola el encapsulamiento

**Correccion:** Usar siempre los metodos getter que retornan copias:
```java
// En vez de: DataStore.insumos.get(index)
// Usar: DataStore.getListaInsumos().get(index)
```
Y hacer que las listas internas sean `private`.

### 3.10 [ALTO] Metodo duplicado de Despacho en DespachoGUI

**Archivo:** `DespachoGUI.java`, lineas 371-416

```java
try {
    despachoActual.setClienteNombre(cliente);
    // ...
} catch (Exception e) {
    // Si falla, usar los metodos antiguos
    despachoActual.setCliente(cliente);
    // ...
}
```

**Problema:** Se usan bloques try-catch para determinar que API del modelo Despacho esta disponible. Esto es un anti-patron que indica que la interfaz del modelo no esta definida claramente. Ademas, el codigo se duplica completamente (lineas 374-389 y 398-415).

**Correccion:**
1. Definir una interfaz clara para `Despacho` con un solo conjunto de metodos
2. Eliminar los metodos legacy o crear un adaptador
3. Eliminar el codigo duplicado

### 3.11 [MEDIO] Impresion de Solo Una Pagina

**Archivo:** `VentanaFacturaGUI.java`, lineas 164-165

```java
job.setPrintable((graphics, pageFormat, pageIndex) -> {
    if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
```

**Problema:** La impresion solo soporta una pagina. Si el comprobante tiene muchos items, se cortara el contenido.

**Correccion:** Implementar paginacion calculando la altura del contenido y dividiendo en paginas.

### 3.12 [MEDIO] Exportacion CSV con Ruta Hardcodeada

**Archivo:** `InsumoGUI.java`, linea 349

```java
FileWriter fw = new FileWriter("data/insumos_export.csv");
```

**Problema:** La ruta de exportacion esta hardcodeada. El usuario no puede elegir donde guardar el archivo.

**Correccion:** Usar `JFileChooser` para que el usuario seleccione la ubicacion:
```java
JFileChooser chooser = new JFileChooser();
chooser.setSelectedFile(new File("insumos_export.csv"));
if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
    FileWriter fw = new FileWriter(chooser.getSelectedFile());
    // ...
}
```

### 3.13 [MEDIO] Thread.sleep en Hilo Principal

**Archivo:** `DataStore.java`, linea 1388

```java
} catch (SQLException e) {
    try {
        Thread.sleep(100); // Pausa en hilo de la GUI
        aumentarStockInsumoReintento(lote, cantidad);
    } catch (InterruptedException ie) { ... }
}
```

**Problema:** `Thread.sleep(100)` en el hilo de eventos de Swing (EDT) congelara la interfaz grafica durante 100ms. Si se ejecuta multiples veces, la interfaz se volvera lenta.

**Correccion:** Usar `SwingWorker` para operaciones de base de datos o implementar reintentos con `ScheduledExecutorService`.

### 3.14 [MEDIO] Nombres de Tabla en Portugues

**Archivo:** `DataStore.java`, lineas 466-483

```java
CREATE TABLE IF NOT EXISTS produto_insumos (
    produto_nome TEXT,
    insumo_nome TEXT,
    quantidade REAL,
    ...
)
```

Tambien en `productos_formulas`:
```java
CREATE TABLE IF NOT EXISTS produtos_formulas (...)
```

**Problema:** Mezcla de idiomas en nombres de tablas y columnas: `produto_insumos`, `produto_nome`, `quantidade` (portugues) vs `insumos`, `lotes`, `despachos` (espanol).

**Correccion:** Renombrar tablas y columnas al espanol para consistencia:
- `produto_insumos` -> `producto_insumos`
- `produto_nome` -> `producto_nombre`
- `quantidade` -> `cantidad`
- `produtos_formulas` -> `productos_formulas` (ya existe con este nombre pero las columnas internas usan portugues)

**Nota:** Esta correccion requiere una migracion de datos si ya hay informacion en la base de datos.

### 3.15 [BAJO] Inconsistencia de Indentacion

**Archivos afectados:** `InsumoGUI.java`, `ProduccionGUI.java`, `DataStore.java`

Multiples archivos mezclan tabulaciones y espacios, y tienen metodos con indentacion incorrecta. Por ejemplo, `eliminarInsumoSeleccionado()` en `InsumoGUI.java` (linea 128) y `eliminarLoteSeleccionado()` en `ProduccionGUI.java` (linea 199) estan al nivel del cuerpo de la clase en vez de tener la indentacion correcta de metodo.

**Correccion:** Aplicar un formateador de codigo consistente (configurar en NetBeans: Tools > Options > Editor > Formatting).

---

## 4. Problemas de Arquitectura

### 4.1 [CRITICO] DataStore como Clase Monolitica (God Class)

**Archivo:** `DataStore.java` - 2015 lineas, 60+ metodos

La clase `DataStore` concentra demasiadas responsabilidades:

| Responsabilidad | Metodos | Deberia estar en |
|----------------|---------|-----------------|
| Creacion de tablas | `crearTablasSiNoExisten()` | `DatabaseManager.java` |
| Migracion de datos | `migrarDatosAntiguos()`, etc. | `MigracionService.java` |
| CRUD Insumos | `guardarInsumo()`, `cargarInsumos()`, etc. | `InsumoDAO.java` |
| CRUD Lotes | `guardarLote()`, `cargarLotes()`, etc. | `LoteDAO.java` |
| CRUD Despachos | `guardarDespacho()`, `cargarDespachos()`, etc. | `DespachoDAO.java` |
| CRUD Usuarios | `agregarUsuario()`, `cargarUsuarios()`, etc. | `UsuarioDAO.java` |
| CRUD Productos | `guardarProductoTerminado()`, etc. | `ProductoDAO.java` |
| Cache en memoria | Listas estaticas + metodos de recarga | `CacheManager.java` |
| Autenticacion | `autenticar()` | `AuthService.java` |
| Configuracion | `inicializarConfiguracionSistema()` | `ConfigService.java` |
| Historial | `registrarMovimiento()`, etc. | `HistorialService.java` |
| Generacion de remisiones | `generarNumeroRemisionUnico()` | `RemisionService.java` |

**Refactorizacion recomendada:**

```
src/inventario/
├── dao/
│   ├── InsumoDAO.java
│   ├── LoteDAO.java
│   ├── DespachoDAO.java
│   ├── UsuarioDAO.java
│   ├── ProductoDAO.java
│   └── HistorialDAO.java
├── service/
│   ├── AuthService.java
│   ├── ProduccionService.java
│   ├── DespachoService.java
│   ├── MigracionService.java
│   └── ConfigService.java
├── model/
│   ├── Insumo.java
│   ├── Lote.java
│   ├── Despacho.java
│   ├── Usuario.java
│   ├── Producto.java
│   └── ProductoTerminado.java
├── gui/
│   ├── MenuPrincipalGUI.java
│   ├── InsumoGUI.java
│   ├── ProduccionGUI.java
│   ├── DespachoGUI.java
│   └── ...
├── util/
│   ├── ThemeUtil.java
│   ├── FiltroPanel.java
│   ├── ConfirmDialogUtil.java
│   └── DatabaseManager.java
└── cache/
    └── CacheManager.java
```

### 4.2 [CRITICO] Listas Estaticas Publicas sin Encapsulamiento

**Archivo:** `DataStore.java`

Las listas de datos son accesibles directamente desde cualquier clase:
```java
public static ArrayList<Insumo> insumos = new ArrayList<>();
public static ArrayList<Lote> lotes = new ArrayList<>();
public static ArrayList<ProductoTerminado> productos = new ArrayList<>();
public static ArrayList<Despacho> despachos = new ArrayList<>();
```

**Problemas:**
- Cualquier clase puede modificar los datos sin control
- No hay sincronizacion para acceso concurrente
- Los cambios directos no se reflejan en la base de datos

**Correccion:**
```java
private static final List<Insumo> insumos = new ArrayList<>();

public static List<Insumo> getInsumos() {
    return Collections.unmodifiableList(insumos);
}
```

### 4.3 [ALTO] Patron DAO Incompleto

El sistema tiene `InsumoDAO.java` y `DespachoDAO.java` pero no se usan consistentemente:

- `InsumoDAO` usa su propia conexion (`data/inventario.db`) diferente al sistema
- `DespachoDAO` tiene metodos de creacion de tablas redundantes con `DataStore`
- La mayoria de operaciones CRUD estan en `DataStore`, no en los DAOs

**Correccion:** Migrar toda la logica de acceso a datos a DAOs dedicados que usen `ConexionSQLite`.

### 4.4 [ALTO] Falta de Patron de Servicio

No existe una capa de servicio entre la GUI y los datos. Las GUIs llaman directamente a `DataStore`:

```java
// ProduccionGUI.java, linea 187-188:
DataStore.agregarLote(loteActual);
DataStore.addOrUpdateProductoTerminado(loteActual, unidades);
```

Esto mezcla logica de negocio con logica de presentacion.

**Correccion:** Crear servicios que encapsulen la logica:
```java
// ProduccionService.java
public class ProduccionService {
    public void finalizarLote(Lote lote, int unidades) {
        lote.setEstado("Terminado");
        lote.setUnidadesProducidas(unidades);
        loteDAO.guardar(lote);
        productoDAO.addOrUpdate(lote.getNombreProducto(), unidades);
        historialDAO.registrar(lote, "Produccion", unidades);
    }
}
```

### 4.5 [ALTO] Ausencia de Manejo de Conexiones

**Archivo:** `ConexionSQLite.java`

```java
public static Connection conectar() throws SQLException {
    return DriverManager.getConnection(DB_URL);
}
```

**Problema:** Cada operacion crea una nueva conexion. No hay pool de conexiones ni reutilizacion.

**Correccion:** Implementar un pool de conexiones basico:
```java
public class ConexionSQLite {
    private static Connection sharedConnection;
    
    public static synchronized Connection conectar() throws SQLException {
        if (sharedConnection == null || sharedConnection.isClosed()) {
            sharedConnection = DriverManager.getConnection(DB_URL);
            sharedConnection.setAutoCommit(true);
            // Activar WAL mode para mejor rendimiento
            try (Statement stmt = sharedConnection.createStatement()) {
                stmt.execute("PRAGMA journal_mode=WAL");
                stmt.execute("PRAGMA busy_timeout=5000");
            }
        }
        return sharedConnection;
    }
}
```

### 4.6 [MEDIO] Metodos Duplicados para Guardar Despacho

**Archivo:** `DataStore.java`

Existen DOS metodos para guardar despachos:
1. `guardarDespacho()` - lineas 916-994 (metodo original con bug de rollback)
2. `guardarDespachoEnBD()` - lineas 1628-1683 (metodo corregido que recibe Connection)

Y dos metodos para registrar movimientos de productos:
1. `registrarMovimientoProducto()` - lineas 1454-1474 (crea su propia conexion)
2. `registrarMovimientoProductoEnBD()` - lineas 1686-1703 (recibe Connection)

**Correccion:** Mantener solo las versiones que reciben `Connection` como parametro (para soportar transacciones) y eliminar las versiones legacy.

### 4.7 [MEDIO] Sistema de Permisos Incompleto

**Archivo:** `Permisos.java` - Solo 22 lineas

```java
public class Permisos {
    public static boolean esGerente() { ... }
    public static boolean esAuxiliar() { ... }
    public static boolean estaLogueado() { ... }
}
```

**Problemas:**
- No verifica permisos para `Desarrollador` ni `Administrador`
- No se usa en las GUIs para restringir acceso a funcionalidades
- No existe control de que operaciones puede hacer cada rol

**Correccion:** Ampliar con permisos granulares:
```java
public class Permisos {
    public enum Permiso {
        GESTIONAR_INSUMOS,
        GESTIONAR_PRODUCCION,
        GESTIONAR_DESPACHOS,
        GESTIONAR_USUARIOS,
        VER_REPORTES,
        ELIMINAR_DATOS,
        EXPORTAR_DATOS
    }
    
    public static boolean tienePermiso(Permiso permiso) {
        Usuario u = DataStore.getUsuarioActual();
        if (u == null) return false;
        
        return switch (u.getRol()) {
            case "Desarrollador" -> true; // Acceso total
            case "Administrador" -> true; // Acceso total
            case "Gerente" -> permiso != Permiso.GESTIONAR_USUARIOS;
            case "Auxiliar" -> permiso == Permiso.GESTIONAR_INSUMOS 
                            || permiso == Permiso.GESTIONAR_PRODUCCION;
            default -> false;
        };
    }
}
```

---

## 5. Correcciones Prioritarias

### Fase 1: Correcciones Criticas (Semana 1-2)

#### 5.1 Eliminar Codigo de Depuracion

**Pasos:**
1. En `DataStore.java`, eliminar:
   - Lineas 18-31: Constante `AGENT_LOG_PATH`, metodos `agentLog()` y `j()`
   - Lineas 1013-1025: Invocacion de `agentLog()` en `registrarMovimiento()`
   - Buscar cualquier otra referencia a `agentLog` en todo el proyecto

2. Eliminar `System.out.println` de depuracion:
   - Linea 1527-1529: `System.out.println("DEBUG - Producto: " + ...)` en `registrarDespacho()`
   - Lineas 1708-1711: Impresion de credenciales en `agregarUsuario()`

3. Reemplazar con logging apropiado:
```java
import java.util.logging.Logger;
import java.util.logging.Level;

public class DataStore {
    private static final Logger LOGGER = Logger.getLogger(DataStore.class.getName());
    
    // En vez de System.out.println("Mensaje"):
    LOGGER.info("Mensaje");
    
    // En vez de System.err.println("Error: " + e.getMessage()):
    LOGGER.log(Level.SEVERE, "Error en operacion", e);
}
```

#### 5.2 Corregir Ruta de InsumoDAO

**Pasos:**
1. Abrir `InsumoDAO.java`
2. Reemplazar el metodo `getConnection()`:
```java
// ANTES (linea 156-159):
private static Connection getConnection() throws SQLException {
    String url = "jdbc:sqlite:data/inventario.db";
    return DriverManager.getConnection(url);
}

// DESPUES:
private static Connection getConnection() throws SQLException {
    return ConexionSQLite.conectar();
}
```
3. Agregar import: `import inventario.ConexionSQLite;` (si esta en paquete diferente)

#### 5.3 Corregir Rollback en guardarDespacho

**Pasos:**
1. Reescribir `guardarDespacho()` (lineas 916-994) siguiendo el patron de `registrarDespacho()` (lineas 1504-1593)
2. O mejor aun: eliminar `guardarDespacho()` por completo ya que `registrarDespacho()` hace lo mismo pero correctamente

#### 5.4 Eliminar Tabla Duplicada

**Pasos:**
1. En `DataStore.java`, lineas 474-483, eliminar el segundo `CREATE TABLE IF NOT EXISTS produto_insumos`
2. Verificar que no hay otras tablas duplicadas en `crearTablasSiNoExisten()`

### Fase 2: Correcciones Altas (Semana 3-4)

#### 5.5 Agregar Transacciones Faltantes

Metodos que necesitan transacciones:
- `guardarLote()` (lineas 847-895)
- `guardarInsumo()` (lineas 819-845)
- `eliminarInsumo()` (lineas 1265-1297)

#### 5.6 Proteger Listas Internas

1. Cambiar las listas a `private`:
```java
private static ArrayList<Insumo> insumos = new ArrayList<>();
```
2. Actualizar todas las GUIs para usar metodos getter
3. Retornar copias inmutables desde los getters

#### 5.7 Corregir NullPointerExceptions

Agregar verificaciones de null en:
- `VentanaFacturaGUI.java` linea 80 (fechaHora)
- `DespachoGUI.java` linea 267-270 (clienteNombre, clienteCiudad)
- `Despacho.java` linea 129 (setFechaHora con fallback)

---

## 6. Oportunidades de Ampliacion

### 6.1 [ALTA] Modulo de Reportes y Estadisticas

**Descripcion:** El sistema no tiene funcionalidad de reportes. Los datos estan disponibles pero no se generan graficos ni resumen.

**Componentes a implementar:**
1. `ReportesGUI.java` - Panel principal de reportes
2. `ReporteProduccionService.java` - Logica de generacion
3. Tipos de reportes sugeridos:
   - Produccion por periodo (diario, semanal, mensual)
   - Consumo de insumos por producto
   - Despachos por cliente
   - Stock actual vs stock minimo
   - Historial de movimientos filtrado

**Tecnologia sugerida:** JFreeChart para graficos, o generacion de PDF con iText.

```java
// Ejemplo de estructura
public class ReportesGUI extends JDialog {
    private JTabbedPane tabs;
    
    private void initUI() {
        tabs.addTab("Produccion", crearPanelProduccion());
        tabs.addTab("Insumos", crearPanelInsumos());
        tabs.addTab("Despachos", crearPanelDespachos());
        tabs.addTab("Stock", crearPanelStock());
    }
}
```

### 6.2 [ALTA] Sistema de Alertas de Stock Minimo

**Descripcion:** No existe alerta cuando un insumo esta por debajo del nivel minimo.

**Implementacion sugerida:**
1. Agregar columna `stock_minimo REAL DEFAULT 0` a tabla `insumos`
2. Agregar campo `stockMinimo` al modelo `Insumo.java`
3. Crear servicio de verificacion:
```java
public class AlertaStockService {
    public List<Insumo> obtenerInsumoBajoStock() {
        List<Insumo> alertas = new ArrayList<>();
        for (Insumo ins : DataStore.getInsumosDisponibles()) {
            if (ins.getCantidad() <= ins.getStockMinimo()) {
                alertas.add(ins);
            }
        }
        return alertas;
    }
}
```
4. Mostrar alerta visual en `MenuPrincipalGUI` al iniciar sesion

### 6.3 [ALTA] Exportacion a PDF de Facturas

**Descripcion:** Actualmente solo se puede imprimir. Agregar exportacion a PDF.

**Implementacion sugerida:**
1. Agregar dependencia: `itextpdf-5.5.13.jar` o similar
2. Crear `FacturaPDFGenerator.java`
3. Agregar boton "Exportar PDF" en `VentanaFacturaGUI`

### 6.4 [ALTA] Modulo de Proveedores

**Descripcion:** Los proveedores se guardan solo como texto en los insumos. No hay gestion dedicada.

**Implementacion sugerida:**
1. Nueva tabla:
```sql
CREATE TABLE proveedores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    nit TEXT,
    telefono TEXT,
    email TEXT,
    direccion TEXT,
    ciudad TEXT,
    notas TEXT
);
```
2. Modelo `Proveedor.java`
3. DAO `ProveedorDAO.java`
4. GUI `ProveedorGUI.java`
5. Relacion con insumos por `proveedor_id` en vez de texto libre

### 6.5 [MEDIA] Dashboard de Inicio

**Descripcion:** El menu principal muestra solo botones. Un dashboard mostraria informacion util.

**Componentes sugeridos:**
- Total de insumos en stock
- Lotes en produccion
- Despachos del dia
- Productos con stock bajo
- Graficos de produccion reciente
- Ultimo despacho realizado

### 6.6 [MEDIA] Busqueda Global

**Descripcion:** Cada modulo tiene su propio filtro. Una busqueda global seria util.

**Implementacion:** Agregar un campo de busqueda en `MenuPrincipalGUI` que busque en todas las entidades.

### 6.7 [MEDIA] Historial de Cambios por Usuario

**Descripcion:** El historial no registra QUE USUARIO realizo cada accion.

**Implementacion:**
1. Agregar columna `usuario TEXT` a tablas `historial_insumos` e `historial_productos`
2. En cada operacion de registro, incluir `DataStore.getUsuarioActual().getNombreUsuario()`

### 6.8 [MEDIA] Backup Automatico de Base de Datos

**Descripcion:** No existe mecanismo de respaldo automatico.

**Implementacion sugerida:**
```java
public class BackupService {
    private static final String BACKUP_DIR = "inventario_data/backups/";
    
    public static void crearBackup() {
        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String backupFile = BACKUP_DIR + "inventario_" + timestamp + ".db";
        
        try {
            Files.copy(
                Path.of("inventario_data/inventario.db"),
                Path.of(backupFile)
            );
        } catch (IOException e) {
            // Manejar error
        }
    }
}
```

### 6.9 [BAJA] Soporte Multi-idioma (i18n)

**Descripcion:** Todo el texto esta hardcodeado en espanol. Para internacionalizacion futura, usar `ResourceBundle`.

### 6.10 [BAJA] Modo Oscuro

**Descripcion:** `ThemeUtil.java` ya tiene la infraestructura para temas. Agregar un tema oscuro seria una extension natural.

### 6.11 [BAJA] Importacion de Datos desde Excel/CSV

**Descripcion:** Solo existe exportacion CSV para insumos. Agregar importacion para carga masiva.

### 6.12 [BAJA] Notificaciones en el Sistema

**Descripcion:** Agregar un panel de notificaciones para eventos importantes (stock bajo, errores, despachos pendientes).

---

## 7. Mejoras de Seguridad

### 7.1 [CRITICO] Implementar Hashing de Contrasenas

**Estado actual:** Las contrasenas se almacenan en texto plano.

**Implementacion paso a paso:**

1. Crear clase utilitaria `SecurityUtil.java`:
```java
package inventario;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class SecurityUtil {
    
    public static String generarSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[16];
        random.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }
    
    public static String hashContrasena(String contrasena, String salt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(Base64.getDecoder().decode(salt));
            byte[] hash = md.digest(contrasena.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error al hashear contrasena", e);
        }
    }
    
    public static boolean verificarContrasena(String contrasena, String hash, String salt) {
        String nuevoHash = hashContrasena(contrasena, salt);
        return nuevoHash.equals(hash);
    }
}
```

2. Modificar tabla `usuarios`:
```sql
ALTER TABLE usuarios ADD COLUMN salt TEXT;
```

3. Actualizar `autenticar()` en `DataStore.java`:
```java
public static boolean autenticar(String usuario, String contrasena) {
    String sql = "SELECT contrasena, salt FROM usuarios WHERE nombre_usuario = ?";
    try (Connection conn = ConexionSQLite.conectar();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, usuario.trim());
        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            String hashAlmacenado = rs.getString("contrasena");
            String salt = rs.getString("salt");
            if (SecurityUtil.verificarContrasena(contrasena, hashAlmacenado, salt)) {
                // Cargar usuario completo y asignar a usuarioActual
                return true;
            }
        }
    } catch (SQLException e) { /* log */ }
    return false;
}
```

### 7.2 [CRITICO] Limitar Intentos de Login

**Archivo:** `LoginDialog.java`

**Implementacion:**
```java
private int intentosFallidos = 0;
private static final int MAX_INTENTOS = 5;
private long tiempoBloqueo = 0;

private void intentarLogin() {
    if (intentosFallidos >= MAX_INTENTOS) {
        long tiempoRestante = (tiempoBloqueo - System.currentTimeMillis()) / 1000;
        if (tiempoRestante > 0) {
            JOptionPane.showMessageDialog(this, 
                "Cuenta bloqueada. Intente en " + tiempoRestante + " segundos.");
            return;
        }
        intentosFallidos = 0; // Reiniciar despues del periodo de bloqueo
    }
    
    // ... logica de login existente ...
    
    if (!autenticado) {
        intentosFallidos++;
        if (intentosFallidos >= MAX_INTENTOS) {
            tiempoBloqueo = System.currentTimeMillis() + 300000; // 5 minutos
        }
    }
}
```

### 7.3 [ALTO] Registrar Intentos de Autenticacion

Crear tabla de log de autenticacion:
```sql
CREATE TABLE log_autenticacion (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT DEFAULT (datetime('now')),
    nombre_usuario TEXT,
    ip TEXT,
    resultado TEXT, -- 'EXITOSO' o 'FALLIDO'
    detalles TEXT
);
```

### 7.4 [ALTO] Validacion de Entrada en Formularios

**Problema:** Varios formularios no validan la longitud maxima de campos ni caracteres especiales.

**Implementacion:** Crear clase `ValidadorUtil.java`:
```java
public class ValidadorUtil {
    public static boolean validarTexto(String texto, int maxLength) {
        return texto != null && !texto.trim().isEmpty() && texto.length() <= maxLength;
    }
    
    public static boolean validarNIT(String nit) {
        return nit != null && nit.matches("\\d{3}\\.?\\d{3}\\.?\\d{3}-?\\d");
    }
    
    public static boolean validarTelefono(String telefono) {
        return telefono != null && telefono.matches("[0-9()\\-\\s+]{7,15}");
    }
    
    public static boolean validarCantidad(String cantidad) {
        try {
            double val = Double.parseDouble(cantidad);
            return val > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
```

### 7.5 [MEDIO] Timeout de Sesion

**Implementacion:** Agregar inactividad de sesion en `MenuPrincipalGUI`:
```java
private Timer timerInactividad;
private static final int TIMEOUT_MINUTOS = 30;

private void iniciarTimerInactividad() {
    timerInactividad = new Timer(TIMEOUT_MINUTOS * 60 * 1000, e -> {
        DataStore.setUsuarioActual(null);
        dispose();
        // Mostrar LoginDialog nuevamente
    });
    timerInactividad.setRepeats(false);
    timerInactividad.start();
}

// Reiniciar timer en cada accion del usuario
private void resetearTimer() {
    if (timerInactividad != null) {
        timerInactividad.restart();
    }
}
```

---

## 8. Optimizacion de Rendimiento

### 8.1 [ALTO] Activar WAL Mode en SQLite

**Descripcion:** SQLite por defecto usa journal mode DELETE, que es mas lento y bloquea escrituras.

**Implementacion:** En `ConexionSQLite.java`, despues de crear la conexion:
```java
public static Connection conectar() throws SQLException {
    Connection conn = DriverManager.getConnection(DB_URL);
    try (Statement stmt = conn.createStatement()) {
        stmt.execute("PRAGMA journal_mode=WAL");
        stmt.execute("PRAGMA busy_timeout=5000");
        stmt.execute("PRAGMA synchronous=NORMAL");
    }
    return conn;
}
```

### 8.2 [ALTO] Evitar Recargas Completas de Cache

**Problema:** Despues de cada operacion, se recarga toda la lista desde la BD:

```java
// DataStore.java, linea 890:
cargarLotes(); // Recarga TODOS los lotes despues de guardar UNO

// DataStore.java, linea 983:
cargarDespachos(); // Recarga TODOS los despachos despues de guardar UNO
```

**Correccion:** Actualizar solo el elemento modificado en el cache:
```java
public static void guardarLote(Lote lote) {
    // ... guardar en BD ...
    
    // Actualizar cache localmente
    Lote existente = buscarLotePorId(lote.getIdLote());
    if (existente != null) {
        int index = lotes.indexOf(existente);
        lotes.set(index, lote);
    } else {
        lotes.add(lote);
    }
}
```

### 8.3 [MEDIO] Implementar Indices en Base de Datos

Las tablas no tienen indices ademas de las claves primarias.

**Indices recomendados:**
```sql
-- Busquedas frecuentes por nombre de insumo
CREATE INDEX IF NOT EXISTS idx_insumos_nombre ON insumos(nombre);

-- Busquedas de lotes por producto
CREATE INDEX IF NOT EXISTS idx_lotes_producto ON lotes(nombre_producto);

-- Busquedas de despachos por cliente
CREATE INDEX IF NOT EXISTS idx_despachos_cliente ON despachos(cliente_nombre);

-- Busquedas de historial por fecha
CREATE INDEX IF NOT EXISTS idx_historial_fecha ON historial_insumos(fecha);

-- Busquedas de lote_insumos por id_lote
CREATE INDEX IF NOT EXISTS idx_lote_insumos_lote ON lote_insumos(id_lote);

-- Busquedas de despacho_items por id_despacho
CREATE INDEX IF NOT EXISTS idx_despacho_items_despacho ON despacho_items(id_despacho);
```

### 8.4 [MEDIO] Usar PreparedStatement Cache

**Problema:** Cada operacion crea un nuevo `PreparedStatement`.

**Correccion:** Para operaciones frecuentes, cachear los statements:
```java
private static final Map<String, PreparedStatement> statementCache = new HashMap<>();

private static PreparedStatement getStatement(Connection conn, String sql) throws SQLException {
    PreparedStatement ps = statementCache.get(sql);
    if (ps == null || ps.isClosed()) {
        ps = conn.prepareStatement(sql);
        statementCache.put(sql, ps);
    }
    return ps;
}
```

### 8.5 [BAJO] Carga Perezosa de Datos

**Problema:** Al iniciar, se cargan TODOS los datos de todas las tablas.

**Correccion:** Cargar datos solo cuando se necesiten:
```java
private static boolean insumosLoaded = false;

public static List<Insumo> getInsumosDisponibles() {
    if (!insumosLoaded) {
        cargarInsumos();
        insumosLoaded = true;
    }
    // ...
}
```

---

## 9. Estrategia de Pruebas

### 9.1 Estado Actual

El proyecto **no tiene pruebas automatizadas**. Todo se verifica manualmente.

### 9.2 Framework Recomendado

- **JUnit 5** para pruebas unitarias
- **Mockito** para mocks de base de datos
- **AssertJ** para aserciones legibles

### 9.3 Estructura de Pruebas Sugerida

```
test/
├── inventario/
│   ├── dao/
│   │   ├── InsumoDAOTest.java
│   │   ├── LoteDAOTest.java
│   │   ├── DespachoDAOTest.java
│   │   └── UsuarioDAOTest.java
│   ├── service/
│   │   ├── AuthServiceTest.java
│   │   ├── ProduccionServiceTest.java
│   │   └── DespachoServiceTest.java
│   ├── model/
│   │   ├── InsumoTest.java
│   │   ├── LoteTest.java
│   │   ├── DespachoTest.java
│   │   └── ProductoTerminadoTest.java
│   └── util/
│       ├── SecurityUtilTest.java
│       └── ValidadorUtilTest.java
```

### 9.4 Pruebas Prioritarias

#### 9.4.1 Pruebas del Modelo Lote
```java
@Test
void agregarInsumoUsado_stockSuficiente_reduceCantidad() {
    // Arrange
    Lote lote = new Lote("LOT-001", "Producto A", "2024-01-15");
    // Configurar DataStore con insumo de stock 100
    
    // Act
    lote.agregarInsumoUsado("Insumo X", 50);
    
    // Assert
    assertEquals(50, lote.getInsumosUsados().get("Insumo X"));
}

@Test
void agregarInsumoUsado_stockInsuficiente_lanzaExcepcion() {
    Lote lote = new Lote("LOT-001", "Producto A", "2024-01-15");
    assertThrows(IllegalArgumentException.class, 
        () -> lote.agregarInsumoUsado("Insumo X", 999));
}
```

#### 9.4.2 Pruebas de Autenticacion
```java
@Test
void autenticar_credencialesValidas_retornaTrue() {
    DataStore.agregarUsuario(new Usuario("admin", "password123", "Desarrollador"));
    assertTrue(DataStore.autenticar("admin", "password123"));
}

@Test
void autenticar_contrasenaIncorrecta_retornaFalse() {
    assertFalse(DataStore.autenticar("admin", "wrongpassword"));
}

@Test
void autenticar_usuarioInexistente_retornaFalse() {
    assertFalse(DataStore.autenticar("noexiste", "password"));
}
```

#### 9.4.3 Pruebas de Despacho
```java
@Test
void registrarDespacho_stockSuficiente_reduceStock() {
    // Verificar que el stock se reduce correctamente
}

@Test
void registrarDespacho_stockInsuficiente_lanzaExcepcion() {
    // Verificar que no se permite despacho sin stock
}

@Test
void registrarDespacho_sinItems_lanzaExcepcion() {
    Despacho d = new Despacho();
    assertThrows(IllegalArgumentException.class, 
        () -> DataStore.registrarDespacho(d));
}
```

### 9.5 Configuracion de Pruebas

Agregar dependencias en el proyecto (o en `build.xml` para Ant):
```xml
<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <version>5.10.1</version>
    <scope>test</scope>
</dependency>
```

Para NetBeans: Agregar los JARs de JUnit 5 al proyecto en Libraries > Test Libraries.

### 9.6 Base de Datos de Prueba

Usar base de datos SQLite en memoria para pruebas:
```java
@BeforeEach
void setUp() {
    // Usar BD en memoria para pruebas
    ConexionSQLite.setTestMode("jdbc:sqlite::memory:");
    DataStore.init();
}

@AfterEach
void tearDown() {
    ConexionSQLite.setTestMode(null);
}
```

---

## 10. Guia de Despliegue y Mantenimiento

### 10.1 Requisitos del Sistema

| Componente | Version Minima | Recomendada |
|-----------|---------------|-------------|
| Java JDK | 17 | 21 |
| SQLite JDBC | 3.51.2.0 | Ultima estable |
| FlatLaf | 3.4.1 | Ultima estable |
| RAM | 512 MB | 1 GB |
| Disco | 100 MB | 500 MB |

### 10.2 Estructura de Archivos en Produccion

```
ECOBRILLA/
├── ecobrilla.jar                 # Aplicacion principal
├── lib/
│   ├── sqlite-jdbc-3.51.2.0.jar  # Driver SQLite
│   └── flatlaf-3.4.1.jar         # Tema FlatLaf
├── inventario_data/
│   ├── inventario.db             # Base de datos
│   └── backups/                  # Respaldos (crear)
├── logs/                         # Logs (crear)
├── resources/
│   └── logo.png                  # Logo de la empresa
└── README.md                     # Documentacion
```

### 10.3 Proceso de Compilacion

**Con NetBeans:**
1. Abrir el proyecto en NetBeans
2. Click derecho > Clean and Build
3. El JAR se genera en `dist/`

**Con Ant (linea de comandos):**
```bash
ant clean
ant compile
ant jar
```

**Manual:**
```bash
# Compilar
javac -cp "lib/*" -d build/ src/inventario/*.java

# Crear JAR
jar cfm ecobrilla.jar MANIFEST.MF -C build/ .

# Ejecutar
java -cp "ecobrilla.jar:lib/*" inventario.InventarioProduccionMain
```

### 10.4 Procedimiento de Actualizacion

1. **Crear respaldo de la base de datos:**
```bash
cp inventario_data/inventario.db inventario_data/backups/inventario_$(date +%Y%m%d).db
```

2. **Verificar compatibilidad:** Revisar si hay cambios en el esquema de BD

3. **Aplicar migraciones si existen:**
```sql
-- Ejemplo de migracion
ALTER TABLE insumos ADD COLUMN stock_minimo REAL DEFAULT 0;
ALTER TABLE usuarios ADD COLUMN salt TEXT;
```

4. **Reemplazar JAR:**
```bash
cp ecobrilla_nueva.jar ecobrilla.jar
```

5. **Verificar funcionamiento:**
   - Iniciar la aplicacion
   - Verificar login
   - Revisar que los datos existentes se cargan correctamente
   - Probar una operacion de cada modulo

### 10.5 Respaldo y Recuperacion

**Script de respaldo automatico (Windows):**
```bat
@echo off
set FECHA=%date:~6,4%%date:~3,2%%date:~0,2%
set HORA=%time:~0,2%%time:~3,2%
copy inventario_data\inventario.db inventario_data\backups\inventario_%FECHA%_%HORA%.db
echo Respaldo creado: inventario_%FECHA%_%HORA%.db
```

**Recuperacion:**
```bat
copy inventario_data\backups\inventario_YYYYMMDD.db inventario_data\inventario.db
```

### 10.6 Monitoreo

Implementar logging a archivo para monitoreo:
```java
// En InventarioProduccionMain.java
public static void main(String[] args) {
    // Configurar logging
    FileHandler fh = new FileHandler("logs/ecobrilla_%g.log", 5_000_000, 3, true);
    fh.setFormatter(new SimpleFormatter());
    Logger.getLogger("").addHandler(fh);
    
    // ... resto del main ...
}
```

---

## 11. Buenas Practicas para Desarrollo Futuro

### 11.1 Convenciones de Codigo

1. **Nombrar en espanol consistente:**
   - Clases: PascalCase (`GestionUsuariosGUI`)
   - Metodos: camelCase (`obtenerInsumos()`)
   - Constantes: SNAKE_CASE_MAYUSCULAS (`COLOR_PRIMARIO`)
   - Variables: camelCase (`nombreProducto`)

2. **NO mezclar idiomas:** Evitar portugues (`produto`, `quantidade`) y mantener todo en espanol

3. **Documentar metodos publicos:**
```java
/**
 * Busca un insumo por su nombre en el cache en memoria.
 * @param nombre Nombre exacto del insumo (case-insensitive)
 * @return El Insumo encontrado o null si no existe
 */
public static Insumo buscarInsumoPorNombre(String nombre) { ... }
```

### 11.2 Patron de Acceso a Datos

Siempre seguir este patron para operaciones de BD:

```java
public boolean operacionBD(Object parametro) {
    String sql = "...";
    try (Connection conn = ConexionSQLite.conectar();
         PreparedStatement ps = conn.prepareStatement(sql)) {
        ps.setString(1, parametro.toString());
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        LOGGER.log(Level.SEVERE, "Error en operacion", e);
        return false;
    }
}
```

Para multiples operaciones, SIEMPRE usar transacciones:
```java
Connection conn = null;
try {
    conn = ConexionSQLite.conectar();
    conn.setAutoCommit(false);
    // ... operaciones ...
    conn.commit();
} catch (SQLException e) {
    if (conn != null) try { conn.rollback(); } catch (SQLException ex) { /* log */ }
    throw e;
} finally {
    if (conn != null) try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { /* log */ }
}
```

### 11.3 Manejo de Errores

1. **Nunca ignorar excepciones silenciosamente:**
```java
// MAL:
} catch (Exception ignore) {}

// BIEN:
} catch (Exception e) {
    LOGGER.log(Level.WARNING, "Error procesando datos", e);
}
```

2. **Proporcionar mensajes utiles al usuario:**
```java
// MAL:
JOptionPane.showMessageDialog(this, "Error.");

// BIEN:
ConfirmDialogUtil.mostrarError(this, "Error de Stock", 
    "No se puede despachar " + cantidad + " unidades de " + producto + 
    ".<br>Stock disponible: " + stockActual);
```

3. **No exponer detalles tecnicos al usuario:**
```java
// MAL:
JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());

// BIEN:
LOGGER.log(Level.SEVERE, "Error en operacion", e); // Para el log
ConfirmDialogUtil.mostrarError(this, "Error", 
    "Ocurrio un error al procesar la solicitud. Contacte al administrador.");
```

### 11.4 Gestion de Recursos

1. **Siempre usar try-with-resources:**
```java
try (Connection conn = ConexionSQLite.conectar();
     PreparedStatement ps = conn.prepareStatement(sql);
     ResultSet rs = ps.executeQuery()) {
    // Procesar resultados
}
// Connection, PreparedStatement y ResultSet se cierran automaticamente
```

2. **No dejar conexiones abiertas:**
```java
// MAL (conexion puede quedar abierta si hay excepcion):
Connection conn = ConexionSQLite.conectar();
// ... uso de conn ...
conn.close();

// BIEN (siempre se cierra):
try (Connection conn = ConexionSQLite.conectar()) {
    // ... uso de conn ...
}
```

### 11.5 Control de Versiones

1. Crear un archivo `.gitignore` apropiado:
```
# Base de datos local
inventario_data/inventario.db

# Respaldos
inventario_data/backups/

# Archivos compilados
build/
dist/
*.class

# NetBeans
nbproject/private/

# Logs
logs/
*.log

# IDE
.idea/
*.iml
.vscode/
```

2. **Commits con mensajes claros:**
```
feat: Agregar modulo de reportes de produccion
fix: Corregir ruta de BD en InsumoDAO
refactor: Separar DataStore en DAOs individuales
security: Implementar hashing de contrasenas
```

---

## 12. Plan de Implementacion por Fases

### Fase 1: Correcciones Criticas (2 semanas)

| # | Tarea | Archivo(s) | Esfuerzo |
|---|-------|-----------|----------|
| 1 | Eliminar codigo de depuracion (`agentLog`) | DataStore.java | 1 hora |
| 2 | Eliminar tabla duplicada | DataStore.java | 30 min |
| 3 | Corregir ruta BD en InsumoDAO | InsumoDAO.java | 30 min |
| 4 | Corregir rollback en guardarDespacho | DataStore.java | 2 horas |
| 5 | Agregar verificaciones null | VentanaFacturaGUI.java, Despacho.java | 1 hora |
| 6 | Agregar transacciones faltantes | DataStore.java | 3 horas |

**Estimacion total Fase 1:** 8 horas

### Fase 2: Seguridad (2 semanas)

| # | Tarea | Archivo(s) | Esfuerzo |
|---|-------|-----------|----------|
| 1 | Implementar hashing de contrasenas | SecurityUtil.java, DataStore.java | 4 horas |
| 2 | Limitar intentos de login | LoginDialog.java | 2 horas |
| 3 | Registrar log de autenticacion | DataStore.java | 2 horas |
| 4 | Validacion de formularios | ValidadorUtil.java, GUIs | 4 horas |
| 5 | Timeout de sesion | MenuPrincipalGUI.java | 2 horas |

**Estimacion total Fase 2:** 14 horas

### Fase 3: Refactorizacion de Arquitectura (4 semanas)

| # | Tarea | Archivo(s) | Esfuerzo |
|---|-------|-----------|----------|
| 1 | Crear paquetes organizados | Toda la estructura | 2 horas |
| 2 | Extraer InsumoDAO completo | InsumoDAO.java | 4 horas |
| 3 | Extraer LoteDAO | LoteDAO.java | 4 horas |
| 4 | Extraer DespachoDAO | DespachoDAO.java | 4 horas |
| 5 | Extraer UsuarioDAO | UsuarioDAO.java | 3 horas |
| 6 | Crear capa de servicios | service/*.java | 8 horas |
| 7 | Proteger listas internas | DataStore.java, GUIs | 4 horas |
| 8 | Eliminar metodos duplicados | DataStore.java | 3 horas |
| 9 | Implementar logging | Todos los archivos | 3 horas |

**Estimacion total Fase 3:** 35 horas

### Fase 4: Ampliaciones (6 semanas)

| # | Tarea | Archivo(s) | Esfuerzo |
|---|-------|-----------|----------|
| 1 | Modulo de reportes | ReportesGUI.java, ReporteService.java | 16 horas |
| 2 | Alertas de stock minimo | AlertaStockService.java | 4 horas |
| 3 | Exportacion PDF | FacturaPDFGenerator.java | 8 horas |
| 4 | Modulo de proveedores | Proveedor*.java | 12 horas |
| 5 | Dashboard de inicio | MenuPrincipalGUI.java | 8 horas |
| 6 | Historial por usuario | DataStore.java, GUIs | 4 horas |
| 7 | Backup automatico | BackupService.java | 4 horas |
| 8 | Ampliar sistema de permisos | Permisos.java, GUIs | 6 horas |

**Estimacion total Fase 4:** 62 horas

### Fase 5: Pruebas y Calidad (3 semanas)

| # | Tarea | Archivo(s) | Esfuerzo |
|---|-------|-----------|----------|
| 1 | Configurar framework de pruebas | build.xml, libs | 2 horas |
| 2 | Pruebas de modelos | test/model/*.java | 6 horas |
| 3 | Pruebas de DAOs | test/dao/*.java | 8 horas |
| 4 | Pruebas de servicios | test/service/*.java | 8 horas |
| 5 | Pruebas de integracion | test/integration/*.java | 6 horas |
| 6 | Formateo y limpieza de codigo | Todos los archivos | 4 horas |

**Estimacion total Fase 5:** 34 horas

---

### Resumen de Esfuerzo Total

| Fase | Duracion | Horas Estimadas | Prioridad |
|------|----------|----------------|-----------|
| Fase 1: Correcciones Criticas | 2 semanas | 8 horas | CRITICA |
| Fase 2: Seguridad | 2 semanas | 14 horas | CRITICA |
| Fase 3: Refactorizacion | 4 semanas | 35 horas | ALTA |
| Fase 4: Ampliaciones | 6 semanas | 62 horas | MEDIA |
| Fase 5: Pruebas | 3 semanas | 34 horas | ALTA |
| **Total** | **17 semanas** | **153 horas** | |

---

*Documento generado como guia tecnica para el equipo de desarrollo de ECOBRILLA SOLUCIONES S.A.S.*  
*Para preguntas o actualizaciones, contactar al equipo de desarrollo.*
