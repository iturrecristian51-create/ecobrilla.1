package inventario;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo Despacho compatible con distintas partes del sistema:
 * - contiene fechaHora (LocalDateTime) y también accesos por String (getFecha)
 * - soporta cliente (nombre, nit, telefono, direccion, ciudad)
 * - lista de Items con getters esperados (getNombreProducto, getCantidad)
 * - addItem sobrecargado: (nombre, cantidad) y (nombre, cantidad, precio)
 * - métodos auxiliares: getTotal(), setItems(List), etc.
 */
public class Despacho implements Serializable {
    private int id;
    private String clienteNombre = "";
    private String clienteNIT = "";
    private String clienteTelefono = "";
    private String clienteDireccion = "";
    private String clienteCiudad = "";
    private String notas = "";
    // Se inicializa cuando se crea o se carga desde la BD
    private LocalDateTime fechaHora;
    private List<Item> items = new ArrayList<>();
    private String numeroRemision; // NUEVO CAMPO PARA REMISIÓN ÚNICA

    Object getCiudad() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }


    // === Clase interna Item ===
    public static class Item implements Serializable {
        private String nombreProducto;
        private int cantidad;
        private double precioUnitario;

        // constructor completo
        public Item(String nombreProducto, int cantidad, double precioUnitario) {
            this.nombreProducto = nombreProducto != null ? nombreProducto : "";
            this.cantidad = Math.max(0, cantidad);
            this.precioUnitario = Math.max(0.0, precioUnitario);
        }

        // constructor sin precio (precio = 0)
        public Item(String nombreProducto, int cantidad) {
            this(nombreProducto, cantidad, 0.0);
        }

        // getters esperados por el resto del código
        public String getNombreProducto() { return nombreProducto; }
        public int getCantidad() { return cantidad; }
        public double getPrecioUnitario() { return precioUnitario; }

        // setters (por si acaso)
        public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
        public void setCantidad(int cantidad) { this.cantidad = Math.max(0, cantidad); }
        public void setPrecioUnitario(double precioUnitario) { this.precioUnitario = Math.max(0.0, precioUnitario); }

        public double getSubtotal() { return cantidad * precioUnitario; }

        @Override
        public String toString() {
            return nombreProducto + " x" + cantidad + (precioUnitario > 0 ? String.format(" (%.2f)", precioUnitario) : "");
        }
    }

    // === Constructores ===
    public Despacho() { /* fechaHora inicializada arriba */ }

    // constructor corto
    public Despacho(int id, String clienteNombre) {
        this();
        this.id = id;
        this.clienteNombre = clienteNombre != null ? clienteNombre : "";
    }

    // constructor completo con fecha en String (ISO) o LocalDateTime
    public Despacho(int id, LocalDateTime fechaHora, String clienteNombre, String clienteNIT,
                    String clienteTelefono, String clienteDireccion, String clienteCiudad, String notas) {
        this(id, clienteNombre);
        if (fechaHora != null) this.fechaHora = fechaHora;
        this.clienteNIT = clienteNIT != null ? clienteNIT : "";
        this.clienteTelefono = clienteTelefono != null ? clienteTelefono : "";
        this.clienteDireccion = clienteDireccion != null ? clienteDireccion : "";
        this.clienteCiudad = clienteCiudad != null ? clienteCiudad : "";
        this.notas = notas != null ? notas : "";
    }

    // === Getters / Setters estándar ===
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // cliente: alias y compatibilidad con versiones antiguas
    public String getCliente() { return clienteNombre; }
    public void setCliente(String cliente) { this.clienteNombre = cliente != null ? cliente : ""; }

    public String getClienteNombre() { return clienteNombre; }
    public void setClienteNombre(String clienteNombre) { this.clienteNombre = clienteNombre != null ? clienteNombre : ""; }

    public String getClienteNIT() { return clienteNIT; }
    public void setClienteNIT(String clienteNIT) { this.clienteNIT = clienteNIT != null ? clienteNIT : ""; }

    public String getClienteTelefono() { return clienteTelefono; }
    public void setClienteTelefono(String clienteTelefono) { this.clienteTelefono = clienteTelefono != null ? clienteTelefono : ""; }

    public String getClienteDireccion() { return clienteDireccion; }
    public void setClienteDireccion(String clienteDireccion) { this.clienteDireccion = clienteDireccion != null ? clienteDireccion : ""; }

    public String getClienteCiudad() { return clienteCiudad; }
    public void setClienteCiudad(String clienteCiudad) { this.clienteCiudad = clienteCiudad != null ? clienteCiudad : ""; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas != null ? notas : ""; }

    // Fecha: compatibilidad en String y LocalDateTime
    public LocalDateTime getFechaHora() { return fechaHora; }

    // Setter desde LocalDateTime (usado en GUI al crear un despacho nuevo)
    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    // Setter que acepta String con formato "yyyy-MM-dd HH:mm:ss" (usado al cargar desde SQLite)
    public void setFechaHora(String fechaHoraStr) {
        if (fechaHoraStr == null || fechaHoraStr.isBlank()) return;
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            this.fechaHora = LocalDateTime.parse(fechaHoraStr, formatter);
        } catch (DateTimeParseException e) {
            // Si no se puede parsear, dejamos la fecha como está (puede ser null)
        }
    }

    // método que devuelve la fecha como String (para código que espera getFecha())
    public String getFecha() {
        return fechaHora != null ? fechaHora.toString() : "";
    }

    // === Items ===
    public List<Item> getItems() { return items; }
    public void setItems(List<Item> items) { this.items = items != null ? items : new ArrayList<>(); }

    // addItem sobrecargado: sin precio (precio 0) o con precio
    public void addItem(String nombreProducto, int cantidad) {
        this.items.add(new Item(nombreProducto, cantidad, 0.0));
    }

    public void addItem(String nombreProducto, int cantidad, double precioUnitario) {
        this.items.add(new Item(nombreProducto, cantidad, precioUnitario));
    }

    // getTotal y calcular total (compatibilidad con diferentes nombres)
    public double getTotal() { return calcularTotal(); }

    public double calcularTotal() {
        return items.stream().mapToDouble(Item::getSubtotal).sum();
    }

    public String getNumeroRemision() {
        return numeroRemision;
    }

    public void setNumeroRemision(String numeroRemision) {
        this.numeroRemision = numeroRemision;
    }

    // utilidades para compatibilidad con código antiguo
    public void setNit(String nit) { setClienteNIT(nit); }
    public void setTelefono(String tel) { setClienteTelefono(tel); }
    public void setDireccion(String dir) { setClienteDireccion(dir); }
    public void setCiudad(String ciudad) { setClienteCiudad(ciudad); }

    @Override
    public String toString() {
        return "Despacho{" +
                "id=" + id +
                ", cliente='" + clienteNombre + '\'' +
                ", fecha=" + (fechaHora != null ? fechaHora.toString() : "") +
                ", items=" + items.size() +
                ", total=" + calcularTotal() +
                '}';
    }
}
