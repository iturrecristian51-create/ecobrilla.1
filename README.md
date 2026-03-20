# ECOBRILLA SOLUCIONES S.A.S - Sistema de Inventario y Produccion

Sistema de escritorio para la gestion integral de inventario, produccion y despachos de **ECOBRILLA SOLUCIONES S.A.S**, desarrollado en Java con interfaz grafica Swing y base de datos SQLite.

---

## Tabla de Contenidos

1. [Descripcion General](#descripcion-general)
2. [Caracteristicas Principales](#caracteristicas-principales)
3. [Requisitos del Sistema](#requisitos-del-sistema)
4. [Instalacion y Configuracion](#instalacion-y-configuracion)
5. [Ejecucion del Sistema](#ejecucion-del-sistema)
6. [Guia de Usuario](#guia-de-usuario)
   - [Inicio de Sesion](#inicio-de-sesion)
   - [Menu Principal](#menu-principal)
   - [Modulo de Insumos](#modulo-de-insumos)
   - [Modulo de Produccion](#modulo-de-produccion)
   - [Modulo de Nuevo Producto](#modulo-de-nuevo-producto)
   - [Modulo de Productos Terminados](#modulo-de-productos-terminados)
   - [Modulo de Despachos](#modulo-de-despachos)
   - [Gestion de Usuarios](#gestion-de-usuarios)
7. [Arquitectura del Sistema](#arquitectura-del-sistema)
   - [Estructura de Archivos](#estructura-de-archivos)
   - [Capa de Modelos](#capa-de-modelos)
   - [Capa de Acceso a Datos (DAO)](#capa-de-acceso-a-datos-dao)
   - [Capa de Interfaz Grafica (GUI)](#capa-de-interfaz-grafica-gui)
   - [Utilidades](#utilidades)
8. [Base de Datos](#base-de-datos)
   - [Esquema de Tablas](#esquema-de-tablas)
   - [Conexion SQLite](#conexion-sqlite)
9. [Sistema de Roles y Permisos](#sistema-de-roles-y-permisos)
10. [Migracion de Datos](#migracion-de-datos)
11. [Exportacion de Datos](#exportacion-de-datos)
12. [Impresion de Documentos](#impresion-de-documentos)
13. [Referencia Tecnica de Clases](#referencia-tecnica-de-clases)
14. [Flujos de Trabajo](#flujos-de-trabajo)
15. [Solucion de Problemas](#solucion-de-problemas)
16. [Informacion de la Empresa](#informacion-de-la-empresa)

---

## Descripcion General

El **Sistema de Inventario y Produccion de ECOBRILLA** es una aplicacion de escritorio que permite gestionar el ciclo completo de operaciones de la empresa:

- **Entrada de materias primas** (insumos) al inventario
- **Creacion de lotes de produccion** que consumen insumos
- **Registro de productos terminados** resultantes de la produccion
- **Despacho de productos** a clientes con generacion de comprobantes
- **Trazabilidad completa** mediante historial de movimientos

El sistema utiliza **SQLite** como base de datos local, lo que permite operar sin necesidad de un servidor de base de datos externo. Toda la informacion se almacena en el archivo `inventario_data/inventario.db`.

---

## Caracteristicas Principales

| Caracteristica | Descripcion |
|---|---|
| **Gestion de Insumos** | Registro, consulta, filtrado y eliminacion de materias primas con trazabilidad por lote |
| **Produccion por Lotes** | Creacion de lotes que consumen insumos del inventario y generan productos terminados |
| **Productos Terminados** | Consulta de stock actual e historial de todos los productos producidos |
| **Despachos** | Registro de ventas/envios con datos completos del cliente y numero de remision unico |
| **Comprobantes** | Generacion e impresion de comprobantes de despacho con datos de la empresa |
| **Fichas Tecnicas** | Impresion de rotulos/fichas tecnicas de produccion |
| **Historial** | Registro automatico de todos los movimientos de inventario (entradas, salidas, uso en produccion) |
| **Filtrado en Tiempo Real** | Busqueda instantanea en todas las tablas del sistema |
| **Exportacion CSV** | Exportacion de datos de insumos y productos terminados a formato CSV |
| **Roles de Usuario** | Sistema de autenticacion con roles: Desarrollador, Administrador, Gerente, Auxiliar |
| **Migracion Automatica** | Migracion automatica de datos desde archivos `.dat` antiguos a SQLite |
| **Tema Visual** | Interfaz profesional con tema consistente y efectos hover |

---

## Requisitos del Sistema

| Requisito | Especificacion |
|---|---|
| **Java** | JDK 17 o superior (requiere text blocks) |
| **Sistema Operativo** | Windows, macOS o Linux |
| **Memoria RAM** | 512 MB minimo recomendado |
| **Espacio en Disco** | 50 MB para la aplicacion + espacio para datos |
| **Dependencias** | `sqlite-jdbc-3.51.2.0.jar` (incluido en el repositorio) |
| **IDE Recomendado** | Apache NetBeans (proyecto preconfigurado) |

---

## Instalacion y Configuracion

### Opcion 1: Usando Apache NetBeans (Recomendado)

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/iturrecristian51-create/ecobrilla.1.git
   cd ecobrilla.1
   ```

2. **Abrir en NetBeans:**
   - Ir a `File > Open Project`
   - Seleccionar la carpeta `ecobrilla.1`
   - NetBeans detectara automaticamente el proyecto gracias a los archivos `nbproject/`

3. **Verificar dependencias:**
   - El archivo `sqlite-jdbc-3.51.2.0.jar` ya esta incluido en la raiz del proyecto
   - La libreria `flatlaf-3.4.1.jar` esta en la carpeta `lib/`
   - Verificar que estan agregadas al classpath del proyecto

4. **Ejecutar:**
   - Click derecho en el proyecto > `Run`
   - O presionar `F6`

### Opcion 2: Compilacion Manual por Linea de Comandos

1. **Clonar el repositorio:**
   ```bash
   git clone https://github.com/iturrecristian51-create/ecobrilla.1.git
   cd ecobrilla.1
   ```

2. **Compilar:**
   ```bash
   mkdir -p build/classes
   javac -cp "sqlite-jdbc-3.51.2.0.jar:lib/flatlaf-3.4.1.jar" \
         -d build/classes \
         src/inventario/*.java
   ```
   > En Windows, usar `;` en lugar de `:` como separador de classpath.

3. **Ejecutar:**
   ```bash
   java -cp "build/classes:sqlite-jdbc-3.51.2.0.jar:lib/flatlaf-3.4.1.jar" \
        inventario.InventarioProduccionMain
   ```

### Opcion 3: Usando Apache Ant

El proyecto incluye un archivo `build.xml` para compilacion con Ant:

```bash
ant clean
ant jar
```

El JAR generado se encontrara en la carpeta `dist/`.

---

## Ejecucion del Sistema

Al ejecutar la aplicacion, el sistema realiza los siguientes pasos automaticamente:

1. **Configura el Look and Feel** del sistema operativo y el tema visual personalizado
2. **Inicializa DataStore** (almacen de datos central):
   - Verifica si existen archivos `.dat` antiguos y los migra a SQLite
   - Verifica la conexion a la base de datos
   - Crea las tablas si no existen
   - Carga usuarios y crea usuarios por defecto si la base esta vacia
   - Carga todos los datos en memoria (insumos, lotes, productos, despachos, historial)
3. **Carga las formulas de productos** existentes
4. **Muestra la ventana de Login**
5. Tras autenticacion exitosa, abre el **Menu Principal**

### Usuarios por Defecto

Si la base de datos esta vacia (primer inicio), el sistema crea automaticamente:

| Usuario | Contrasena | Rol |
|---|---|---|
| `admin` | `admin123` | Administrador |
| `dev` | `dev123` | Desarrollador |

> **Importante:** Se recomienda cambiar las credenciales por defecto despues del primer inicio.

---

## Guia de Usuario

### Inicio de Sesion

Al abrir la aplicacion se presenta un dialogo de inicio de sesion:

1. Ingrese su **nombre de usuario**
2. Ingrese su **contrasena**
3. Presione **Ingresar** o pulse `Enter`
4. Para cancelar, presione **Cancelar** o la tecla `Escape`

El sistema valida las credenciales contra la base de datos SQLite. Si son correctas, se abre el menu principal.

### Menu Principal

El menu principal muestra seis opciones organizadas en una cuadricula:

| Boton | Funcion |
|---|---|
| **Insumos** | Abre el modulo de gestion de materias primas |
| **Produccion** | Abre el modulo de creacion de lotes de produccion |
| **Nuevo Producto** | Abre el formulario para definir nuevos productos con sus formulas |
| **Productos Terminados** | Consulta el stock actual de productos terminados |
| **Despachos** | Abre el modulo de gestion de ventas y despachos |
| **Salir** | Cierra la aplicacion |

En la esquina superior derecha se muestra el nombre del usuario autenticado y su rol. Los usuarios con rol **Desarrollador** ven un boton adicional (icono de herramienta) que da acceso al panel de gestion de usuarios, y tambien un menu **Sistema** en la barra de menus.

---

### Modulo de Insumos

**Ventana:** `InsumoGUI` | **Acceso:** Menu Principal > Insumos

Este modulo tiene dos pestanas:

#### Pestana "Gestion de Insumos"

**Formulario de registro** con los siguientes campos:

| Campo | Descripcion | Obligatorio |
|---|---|---|
| Lote | Identificador unico del lote de insumo | Si |
| Nombre | Nombre del insumo/materia prima | Si |
| Fecha | Fecha de ingreso (auto-completada con fecha actual) | No |
| Cantidad | Cantidad numerica del insumo | Si |
| Unidad | Unidad de medida (kg, litros, unidades, etc.) | No |
| Proveedor | Nombre del proveedor | No |
| Notas | Observaciones adicionales | No |

**Acciones disponibles:**
- **Agregar Insumo:** Guarda el insumo en la base de datos y registra el movimiento en el historial
- **Eliminar Seleccionado:** Elimina un insumo. Verifica primero que no este siendo utilizado en lotes de produccion
- **Exportar CSV:** Exporta todos los insumos a `data/insumos_export.csv`
- **Cerrar:** Cierra la ventana

**Filtro de busqueda:** Barra de busqueda en tiempo real que filtra por nombre, lote o proveedor.

#### Pestana "Historial de Insumos"

Muestra una tabla con todos los movimientos registrados:
- Fecha, Lote, Nombre del insumo, Accion (Entrada/Salida/Uso en produccion), Cantidad y Observacion
- Incluye filtro de busqueda en tiempo real

---

### Modulo de Produccion

**Ventana:** `ProduccionGUI` | **Acceso:** Menu Principal > Produccion

Este modulo tiene dos pestanas:

#### Pestana "Gestion de Produccion"

Dividida en tres paneles:

**Panel Izquierdo - Formulario:**

| Campo | Descripcion |
|---|---|
| ID del Lote | Identificador unico para el nuevo lote |
| Producto | Selector de producto (de los productos registrados) |
| Fecha | Fecha de produccion (auto-completada) |
| Unidades a producir | Cantidad de unidades que se van a producir |
| Insumo | Selector de insumos disponibles en inventario |
| Cantidad insumo | Cantidad del insumo a utilizar |

**Flujo de trabajo:**

1. Completar ID del lote, seleccionar producto y fecha
2. Presionar **"Crear lote temporal"** para iniciar el lote en estado "En proceso"
3. Seleccionar un insumo y cantidad, presionar **"Agregar insumo"** (repite para cada insumo necesario)
   - El sistema valida que haya stock suficiente
   - Reduce automaticamente el stock del insumo
4. Ingresar las unidades producidas
5. Presionar **"Finalizar y Guardar Lote"** para cambiar el estado a "Terminado" y registrar los productos terminados

**Panel Central:** Muestra los insumos agregados al lote actual.

**Panel Derecho:** Muestra todos los lotes existentes. Se puede hacer clic derecho sobre un lote para eliminarlo (los insumos utilizados se devuelven al stock automaticamente).

#### Pestana "Rotulos"

Permite crear e imprimir fichas tecnicas de produccion con los campos:
- Producto, Lote, Fecha, Realizado por, Estado
- Incluye el logo y nombre de la empresa

---

### Modulo de Nuevo Producto

**Ventana:** `VentanaProductoGUI` | **Acceso:** Menu Principal > Nuevo Producto

Permite definir nuevos productos con su formula (lista de insumos necesarios):

1. Ingresar el **nombre** del producto
2. Ingresar una **descripcion**
3. Para cada insumo de la formula:
   - Escribir el nombre del insumo
   - Escribir la cantidad requerida
   - Presionar **"Agregar"**
4. Presionar **"Guardar Producto"** para almacenar la formula en la base de datos

Los productos registrados aqui apareceran en el selector de productos del modulo de Produccion.

---

### Modulo de Productos Terminados

**Ventana:** `ProductoTerminadoGUI` | **Acceso:** Menu Principal > Productos Terminados

Muestra el inventario de productos terminados con dos modos de visualizacion:

| Modo | Descripcion |
|---|---|
| **Actuales** | Solo muestra productos con stock mayor a 0 |
| **Historial** | Muestra todos los productos, incluyendo los que tienen stock 0 |

**Acciones disponibles:**
- **Exportar CSV:** Exporta los datos a `data/productos_terminados_export.csv`
- **Filtro de busqueda:** Busqueda en tiempo real por nombre del producto
- **Cerrar:** Cierra la ventana

---

### Modulo de Despachos

**Ventana:** `DespachoGUI` | **Acceso:** Menu Principal > Despachos

Este modulo tiene dos pestanas:

#### Pestana "Gestion de Despachos"

Dividida en cuatro secciones:

**1. Productos Disponibles (izquierda):**
- Tabla con nombre y cantidad de productos en stock
- Filtro de busqueda
- Campo para ingresar la cantidad a despachar
- Boton **"Agregar al despacho"**

**2. Items del Despacho (centro):**
- Lista de productos agregados al despacho actual
- Boton **"Quitar seleccionado"** para remover items

**3. Datos del Cliente (derecha):**

| Campo | Descripcion | Obligatorio |
|---|---|---|
| Cliente | Nombre del cliente | Si |
| NIT | Numero de identificacion tributaria | Si |
| Telefono | Telefono de contacto | No |
| Direccion | Direccion de entrega | No |
| Ciudad | Ciudad | No |
| Notas | Observaciones | No |

**4. Despachos Registrados (inferior):**
- Tabla con todos los despachos realizados
- Doble clic sobre un despacho abre el comprobante
- Filtro de busqueda por cliente, NIT o ciudad

**Flujo de despacho:**
1. Seleccionar un producto de la tabla y agregar la cantidad deseada
2. Repetir para cada producto a despachar
3. Completar los datos del cliente (minimo Cliente y NIT)
4. Presionar **"Confirmar despacho"**
5. El sistema:
   - Genera un numero de remision unico
   - Reduce el stock de productos terminados
   - Registra el despacho en la base de datos
   - Abre automaticamente el comprobante de despacho

#### Pestana "Historial Productos"

Muestra el historial completo de productos terminados cargado directamente desde la base de datos, con opcion de impresion.

---

### Gestion de Usuarios

**Ventana:** `GestionUsuariosGUI` | **Acceso:** Solo usuarios con rol Desarrollador (boton de herramienta o Menu Sistema)

Permite:
- Ver la lista de usuarios registrados (nombre y rol)
- Agregar nuevos usuarios con los roles disponibles: **Gerente** y **Auxiliar**

---

## Arquitectura del Sistema

### Estructura de Archivos

```
ecobrilla.1/
|-- src/
|   |-- inventario/
|       |-- InventarioProduccionMain.java   # Punto de entrada principal
|       |
|       |-- # === MODELOS (Entidades de datos) ===
|       |-- Insumo.java                     # Materia prima
|       |-- Producto.java                   # Formula de producto con insumos requeridos
|       |-- ProductoTerminado.java          # Producto producido con stock
|       |-- Lote.java                       # Lote de produccion
|       |-- Produccion.java                 # Registro de produccion
|       |-- Despacho.java                   # Despacho/venta con items y datos de cliente
|       |-- Movimiento.java                 # Movimiento de inventario
|       |-- HistorialInsumo.java            # Entrada de historial de insumos
|       |-- Usuario.java                    # Usuario del sistema
|       |
|       |-- # === ACCESO A DATOS ===
|       |-- ConexionSQLite.java             # Conexion a base de datos SQLite
|       |-- DatabaseSetup.java              # Creacion inicial de tablas
|       |-- DataStore.java                  # Almacen central de datos (cache + SQLite)
|       |-- InsumoDAO.java                  # Operaciones CRUD para insumos
|       |-- DespachoDAO.java                # Operaciones CRUD para despachos
|       |-- ProduccionDAO.java              # Operaciones CRUD para produccion
|       |-- MovimientoDAO.java              # Operaciones CRUD para movimientos
|       |-- ContadorRemisiones.java         # Generacion de numeros de remision unicos
|       |-- InicializarTablas.java          # Inicializacion de tablas
|       |-- ListaProductos.java             # Lista en memoria de formulas de productos
|       |-- MigradorDeDatos.java            # Migracion de archivos .dat a SQLite
|       |
|       |-- # === INTERFAZ GRAFICA (GUI) ===
|       |-- LoginDialog.java                # Dialogo de inicio de sesion
|       |-- MenuPrincipalGUI.java           # Menu principal de la aplicacion
|       |-- InsumoGUI.java                  # Ventana de gestion de insumos
|       |-- ProduccionGUI.java              # Ventana de gestion de produccion
|       |-- VentanaProductoGUI.java          # Ventana para registrar nuevos productos
|       |-- ProductoTerminadoGUI.java       # Ventana de consulta de productos terminados
|       |-- DespachoGUI.java                # Ventana de gestion de despachos
|       |-- VentanaFacturaGUI.java          # Comprobante de despacho (factura)
|       |-- GestionUsuariosGUI.java         # Ventana de administracion de usuarios
|       |-- RotuloPanel.java                # Panel para fichas tecnicas de produccion
|       |-- HistorialProductosPanel.java    # Panel de historial de productos
|       |
|       |-- # === UTILIDADES ===
|       |-- ThemeUtil.java                  # Estilos, colores y fuentes del tema visual
|       |-- FiltroPanel.java                # Componente reutilizable de filtro/busqueda
|       |-- FiltradorUtil.java              # Utilidades de filtrado para listas y tablas
|       |-- ConfirmDialogUtil.java          # Dialogos de confirmacion estandarizados
|       |-- Permisos.java                   # Utilidades de verificacion de permisos
|
|-- inventario_data/
|   |-- inventario.db                       # Base de datos SQLite principal
|
|-- data/
|   |-- inventario.db                       # Base de datos alternativa
|
|-- lib/
|   |-- flatlaf-3.4.1.jar                  # Libreria FlatLaf para temas modernos
|
|-- sqlite-jdbc-3.51.2.0.jar               # Driver JDBC para SQLite
|-- build.xml                               # Configuracion de compilacion Ant
|-- nbproject/                              # Configuracion de proyecto NetBeans
|-- backup_archivos_dat/                    # Respaldo de archivos .dat migrados
|-- manifest.mf                             # Manifiesto para JAR
```

### Capa de Modelos

Las clases de modelo representan las entidades del negocio. La mayoria implementa `Serializable` para compatibilidad con el antiguo sistema de persistencia por archivos `.dat`.

| Clase | Descripcion | Campos Principales |
|---|---|---|
| `Insumo` | Materia prima del inventario | lote (PK), nombre, fechaIngreso, cantidad, unidad, proveedor, notas |
| `Producto` | Formula/receta de un producto | nombre, descripcion, insumosRequeridos (Map nombre->cantidad) |
| `ProductoTerminado` | Producto producido en stock | nombre, cantidad, fechaProduccion |
| `Lote` | Lote de produccion | idLote (PK), nombreProducto, fechaProduccion, estado, unidadesProducidas, insumosUsados (Map) |
| `Produccion` | Registro de produccion | codigoProduccion, fecha, producto, cantidadProducida, unidad, loteInsumo, responsable, notas |
| `Despacho` | Despacho/venta | id, clienteNombre, clienteNIT, clienteTelefono, clienteDireccion, clienteCiudad, notas, fechaHora, items (List), numeroRemision |
| `Despacho.Item` | Item dentro de un despacho | nombreProducto, cantidad, precioUnitario |
| `Movimiento` | Movimiento de inventario | fecha, tipo, referencia, descripcion, cantidad |
| `HistorialInsumo` | Historial de movimiento de insumo | fecha, lote, nombreInsumo, accion, cantidad, observacion |
| `Usuario` | Usuario del sistema | nombreUsuario, contrasena, rol |

### Capa de Acceso a Datos (DAO)

| Clase | Responsabilidad |
|---|---|
| `ConexionSQLite` | Administra la conexion a la base de datos SQLite. Crea el directorio `inventario_data/` si no existe |
| `DataStore` | **Clase central** del sistema. Maneja la cache en memoria, todas las operaciones CRUD, autenticacion, migracion y configuracion del sistema. Contiene ~2000 lineas |
| `InsumoDAO` | Operaciones CRUD especificas para insumos (insertar, actualizar, eliminar, buscar, listar) |
| `DespachoDAO` | Operaciones CRUD para despachos e items (insertar, listar, buscar, eliminar) |
| `ProduccionDAO` | Operaciones CRUD para registros de produccion |
| `MovimientoDAO` | Operaciones CRUD para movimientos de inventario |
| `ContadorRemisiones` | Genera numeros de remision unicos secuenciales, almacenados en la tabla `configuracion_sistema` |
| `DatabaseSetup` | Creacion basica de tablas (version inicial) |
| `InicializarTablas` | Inicializacion de tablas del sistema |
| `MigradorDeDatos` | Migra datos serializados de archivos `.dat` a SQLite |
| `ListaProductos` | Mantiene en memoria la lista de formulas de productos |

### Capa de Interfaz Grafica (GUI)

Todas las ventanas usan **Java Swing** con el tema del sistema operativo y estilos personalizados definidos en `ThemeUtil`.

| Clase | Tipo | Descripcion |
|---|---|---|
| `LoginDialog` | JDialog (modal) | Dialogo de inicio de sesion |
| `MenuPrincipalGUI` | JFrame | Ventana principal con menu de navegacion |
| `InsumoGUI` | JDialog (modal) | Gestion de insumos con pestanas (gestion + historial) |
| `ProduccionGUI` | JDialog (modal) | Gestion de produccion con pestanas (produccion + rotulos) |
| `VentanaProductoGUI` | JFrame | Formulario para definir nuevos productos |
| `ProductoTerminadoGUI` | JDialog (modal) | Consulta de stock de productos terminados |
| `DespachoGUI` | JDialog (modal) | Gestion de despachos con pestanas (gestion + historial) |
| `VentanaFacturaGUI` | JDialog (modal) | Comprobante de despacho imprimible |
| `GestionUsuariosGUI` | JDialog (modal) | Administracion de usuarios (solo Desarrollador) |
| `RotuloPanel` | JPanel (Printable) | Ficha tecnica de produccion imprimible |
| `HistorialProductosPanel` | JPanel (Printable) | Historial de productos terminados imprimible |

### Utilidades

| Clase | Descripcion |
|---|---|
| `ThemeUtil` | Define colores (`COLOR_PRIMARIO`, `COLOR_EXITO`, `COLOR_PELIGRO`, etc.), fuentes (`FUENTE_TITULO`, `FUENTE_NORMAL`, etc.) y metodos para aplicar estilos consistentes a botones, tablas, campos de texto y paneles |
| `FiltroPanel` | Componente reutilizable que incluye campo de busqueda, boton de limpiar y label de resultados. Usa un Timer de 300ms para busqueda en tiempo real |
| `FiltradorUtil` | Utilidades de filtrado para listas genericas y tablas Swing. Incluye metodos especificos para filtrar insumos, lotes, productos, despachos e historial |
| `ConfirmDialogUtil` | Dialogos estandarizados con formato HTML para confirmaciones de eliminacion, advertencias, errores, exito e informacion |
| `Permisos` | Metodos estaticos para verificar el rol del usuario actual (`esGerente()`, `esAuxiliar()`, `estaLogueado()`) |

---

## Base de Datos

### Esquema de Tablas

El sistema crea automaticamente las siguientes tablas al iniciar:

#### Tabla `insumos`
```sql
CREATE TABLE IF NOT EXISTS insumos (
    lote TEXT PRIMARY KEY,
    nombre TEXT NOT NULL,
    fecha_ingreso TEXT,
    cantidad REAL,
    unidad TEXT,
    proveedor TEXT,
    notas TEXT
);
```

#### Tabla `productos_terminados`
```sql
CREATE TABLE IF NOT EXISTS productos_terminados (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT UNIQUE NOT NULL,
    cantidad INTEGER,
    fecha_produccion TEXT
);
```

#### Tabla `lotes`
```sql
CREATE TABLE IF NOT EXISTS lotes (
    id_lote TEXT PRIMARY KEY,
    nombre_producto TEXT,
    fecha_produccion TEXT,
    estado TEXT,
    unidades_producidas INTEGER
);
```

#### Tabla `lote_insumos`
```sql
CREATE TABLE IF NOT EXISTS lote_insumos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_lote TEXT,
    nombre_insumo TEXT,
    cantidad REAL,
    FOREIGN KEY (id_lote) REFERENCES lotes(id_lote)
);
```

#### Tabla `despachos`
```sql
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
);
```

#### Tabla `despacho_items`
```sql
CREATE TABLE IF NOT EXISTS despacho_items (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    id_despacho INTEGER,
    nombre_producto TEXT,
    cantidad INTEGER,
    precio_unitario REAL,
    FOREIGN KEY (id_despacho) REFERENCES despachos(id)
);
```

#### Tabla `historial_insumos`
```sql
CREATE TABLE IF NOT EXISTS historial_insumos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    fecha TEXT,
    lote TEXT,
    nombre_insumo TEXT,
    accion TEXT,
    cantidad REAL,
    observacion TEXT
);
```

#### Tabla `historial_productos`
```sql
CREATE TABLE IF NOT EXISTS historial_productos (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_producto TEXT,
    tipo TEXT,
    cantidad INTEGER,
    referencia TEXT,
    fecha TEXT
);
```

#### Tabla `usuarios`
```sql
CREATE TABLE IF NOT EXISTS usuarios (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre_usuario TEXT UNIQUE NOT NULL,
    contrasena TEXT NOT NULL,
    rol TEXT NOT NULL
);
```

#### Tabla `configuracion_sistema`
```sql
CREATE TABLE IF NOT EXISTS configuracion_sistema (
    clave TEXT PRIMARY KEY,
    valor TEXT
);
```

#### Tabla `productos_formulas`
```sql
CREATE TABLE IF NOT EXISTS productos_formulas (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT UNIQUE NOT NULL,
    descripcion TEXT,
    insumos_json TEXT
);
```

### Conexion SQLite

La clase `ConexionSQLite` maneja la conexion:

- **Directorio de datos:** `inventario_data/`
- **Archivo de base de datos:** `inventario_data/inventario.db`
- Si el directorio no existe, se crea automaticamente
- Si hay un error al crear el directorio, se usa un fallback al directorio actual

**Uso:**
```java
// Obtener conexion
Connection conn = ConexionSQLite.conectar();

// Cerrar conexion
ConexionSQLite.cerrar(conn);
```

---

## Sistema de Roles y Permisos

El sistema define cuatro roles de usuario:

| Rol | Descripcion | Permisos Especiales |
|---|---|---|
| **Desarrollador** | Acceso total al sistema | Menu Sistema, Gestion de Usuarios, Herramientas BD |
| **Administrador** | Administrador general | Acceso a todas las funciones operativas |
| **Gerente** | Gerente operativo | Acceso a funciones de gestion |
| **Auxiliar** | Auxiliar operativo | Acceso basico a funciones operativas |

La verificacion de permisos se realiza mediante:
- `Usuario.esDesarrollador()` / `Usuario.esGerente()` / `Usuario.esAuxiliar()`
- `Permisos.esGerente()` / `Permisos.esAuxiliar()` / `Permisos.estaLogueado()`
- `DataStore.getUsuarioActual()` para obtener el usuario en sesion

---

## Migracion de Datos

El sistema incluye dos mecanismos de migracion para convertir datos del antiguo sistema basado en archivos `.dat` (serializacion Java) a SQLite:

### Migracion Automatica (DataStore)

Al iniciar el sistema, `DataStore.init()` verifica automaticamente si existen archivos `.dat` en:
- El directorio actual
- La carpeta `backup_archivos_dat/`

Si se detectan, ejecuta la migracion y crea un respaldo con timestamp en `migracion_respaldo_YYYY-MM-DD_HH-mm-ss/`.

### Migracion Detallada (MigradorDeDatos)

La clase `MigradorDeDatos` ofrece una migracion mas detallada:
1. Verifica existencia de archivos `.dat` en la carpeta `data/`
2. Verifica si la base de datos SQLite esta vacia
3. Si hay datos para migrar, deserializa cada archivo y guarda en SQLite
4. Crea backups de los archivos originales en `backup_archivos_dat/`

**Archivos soportados para migracion:**
- `insumos.dat` -> tabla `insumos`
- `lotes.dat` -> tabla `lotes`
- `productos.dat` -> tabla `productos_terminados`
- `despachos.dat` -> tabla `despachos`
- `usuarios.dat` -> tabla `usuarios`
- `historial.dat` -> tabla `historial_insumos`

---

## Exportacion de Datos

El sistema permite exportar datos a formato CSV desde las siguientes ventanas:

| Modulo | Archivo de Salida | Campos |
|---|---|---|
| Insumos | `data/insumos_export.csv` | Lote, Nombre, Fecha, Cantidad, Unidad, Proveedor, Notas |
| Productos Terminados | `data/productos_terminados_export.csv` | Nombre, Cantidad, FechaProduccion |

---

## Impresion de Documentos

El sistema soporta impresion nativa del sistema operativo para:

| Documento | Clase | Descripcion |
|---|---|---|
| **Comprobante de Despacho** | `VentanaFacturaGUI` | Incluye datos de la empresa, numero de remision, datos del cliente, tabla de productos, observaciones y campos de firma |
| **Ficha Tecnica de Produccion** | `RotuloPanel` | Rotulo con logo de empresa, datos del producto, lote, fecha, responsable y estado |
| **Historial de Productos** | `HistorialProductosPanel` | Lista impresa de todos los productos terminados registrados |

---

## Referencia Tecnica de Clases

### DataStore (Clase Central)

`DataStore` es la clase mas importante del sistema (~2000 lineas). Funciona como:

1. **Cache en memoria** con listas de todas las entidades
2. **Capa de persistencia** con operaciones CRUD sobre SQLite
3. **Motor de autenticacion** para validar usuarios
4. **Motor de migracion** para datos antiguos
5. **Configuracion del sistema** para parametros generales

**Metodos principales:**

| Categoria | Metodo | Descripcion |
|---|---|---|
| **Inicializacion** | `init()` | Inicializa todo el sistema |
| **Insumos** | `guardarInsumo(Insumo)` | Guarda un insumo en BD |
| | `eliminarInsumo(String lote)` | Elimina un insumo |
| | `buscarInsumoPorNombre(String)` | Busca insumo por nombre |
| | `buscarInsumoPorLote(String)` | Busca insumo por lote |
| | `reducirStockInsumo(String, double)` | Reduce stock de un insumo |
| | `aumentarStockInsumo(String, double)` | Aumenta stock de un insumo |
| | `getInsumosDisponibles()` | Retorna insumos con stock > 0 |
| | `recargarInsumos()` | Recarga insumos desde BD |
| **Lotes** | `agregarLote(Lote)` | Guarda un lote de produccion |
| | `eliminarLoteConRestauracion(String)` | Elimina lote y devuelve insumos al stock |
| | `buscarLotePorId(String)` | Busca lote por ID |
| **Productos** | `addOrUpdateProductoTerminado(Lote, int)` | Agrega o actualiza producto terminado |
| | `guardarProductoFormula(Producto)` | Guarda formula de producto |
| | `getProductosFormulas()` | Obtiene formulas de productos |
| **Despachos** | `registrarDespacho(Despacho)` | Registra un despacho completo |
| | `generarNumeroRemisionUnico()` | Genera numero de remision unico |
| **Movimientos** | `registrarMovimiento(...)` | Registra movimiento en historial |
| **Usuarios** | `autenticar(String, String)` | Valida credenciales |
| | `agregarUsuario(Usuario)` | Agrega nuevo usuario |
| | `getUsuarioActual()` | Retorna usuario en sesion |
| | `getListaUsuarios()` | Retorna lista de usuarios |

### ThemeUtil (Tema Visual)

Define la paleta de colores y estilos del sistema:

| Constante | Color | Uso |
|---|---|---|
| `COLOR_PRIMARIO` | Azul (#007BFF) | Botones principales, seleccion de tabla |
| `COLOR_SECUNDARIO` | Gris (#6C757D) | Botones secundarios, encabezados de tabla |
| `COLOR_EXITO` | Verde (#28A745) | Mensajes de exito |
| `COLOR_PELIGRO` | Rojo (#DC3545) | Botones de eliminar, errores |
| `COLOR_ADVERTENCIA` | Amarillo (#FFC107) | Advertencias |
| `COLOR_INFO` | Cyan (#17A2B8) | Informacion |
| `COLOR_FONDO` | Gris claro (#F8F9FA) | Fondo general |
| `COLOR_BORDE` | Gris (#DEE2E6) | Bordes de componentes |
| `COLOR_TEXTO` | Gris oscuro (#212529) | Texto principal |
| `COLOR_TEXTO_SECUNDARIO` | Gris (#6C757D) | Texto secundario |

---

## Flujos de Trabajo

### Flujo Completo: De Insumo a Despacho

```
1. REGISTRAR INSUMO
   |-- InsumoGUI > Formulario > "Agregar Insumo"
   |-- Se guarda en tabla 'insumos'
   |-- Se registra movimiento tipo "Entrada" en historial
   |
2. DEFINIR PRODUCTO (una sola vez por producto)
   |-- VentanaProductoGUI > Definir nombre + insumos requeridos
   |-- Se guarda en tabla 'productos_formulas'
   |
3. PRODUCIR
   |-- ProduccionGUI > Crear lote temporal
   |-- Agregar insumos al lote (reduce stock automaticamente)
   |-- Finalizar lote > Estado "Terminado"
   |-- Se crea/actualiza registro en 'productos_terminados'
   |-- Se guarda lote en tabla 'lotes' con sus insumos en 'lote_insumos'
   |
4. DESPACHAR
   |-- DespachoGUI > Seleccionar productos y cantidades
   |-- Completar datos del cliente
   |-- Confirmar despacho
   |-- Se genera numero de remision unico
   |-- Se reduce stock de productos terminados
   |-- Se registra en tablas 'despachos' y 'despacho_items'
   |-- Se abre comprobante de despacho (imprimible)
```

### Flujo de Eliminacion de Lote

```
1. ProduccionGUI > Click derecho sobre lote > "Eliminar Lote"
2. Confirmacion de eliminacion
3. Se consultan los insumos usados en el lote (tabla 'lote_insumos')
4. Para cada insumo usado:
   |-- Se restaura la cantidad al stock del insumo
   |-- Se registra movimiento de restauracion en historial
5. Se elimina el registro del lote y sus insumos asociados
6. Se actualizan las tablas en la interfaz
```

---

## Solucion de Problemas

### La aplicacion no inicia

- **Verificar version de Java:** El sistema requiere JDK 17+ para soportar text blocks (`"""`). Ejecutar `java -version` para verificar.
- **Verificar classpath:** Asegurarse de que `sqlite-jdbc-3.51.2.0.jar` esta en el classpath.

### Error de conexion a base de datos

- **Verificar permisos:** El usuario debe tener permisos de escritura en el directorio de la aplicacion para crear `inventario_data/`.
- **Base de datos corrupta:** Si la base de datos esta corrupta, se puede eliminar `inventario_data/inventario.db` y reiniciar la aplicacion (se recrearan las tablas vacias y los usuarios por defecto).

### No puedo iniciar sesion

- **Primer inicio:** Usar las credenciales por defecto: `admin`/`admin123` o `dev`/`dev123`.
- **Base de datos vacia:** Si no hay usuarios, el sistema los crea automaticamente al reiniciar.

### No aparecen los productos en Produccion

- Los productos deben registrarse primero en **"Nuevo Producto"** antes de poder usarlos en produccion.

### Error al eliminar un insumo

- Si el insumo esta siendo utilizado en un lote de produccion, no se puede eliminar. Primero debe eliminarse el lote que lo utiliza.

### Los datos no se guardan

- Verificar que el directorio `inventario_data/` existe y tiene permisos de escritura.
- Revisar la consola para mensajes de error de SQLite.

---

## Informacion de la Empresa

| Dato | Valor |
|---|---|
| **Empresa** | ECOBRILLA SOLUCIONES S.A.S |
| **NIT** | 901 972 853-4 |
| **Telefono** | (602) 3154920190 |
| **Ubicacion** | Buenaventura, Colombia |
| **Correo** | ecobrillasolucionessas@gmail.com |
| **Version** | 2.0 |
| **Licencia** | Uso privado |

---

*Documentacion generada para ECOBRILLA SOLUCIONES S.A.S - Sistema de Inventario y Produccion v2.0*
