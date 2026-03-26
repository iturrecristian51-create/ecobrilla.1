package inventario;

import java.io.Serializable;

/**
 * Modelo de Cliente.
 * Todos los campos que se usan en DespachoGUI están aquí centralizados.
 */
public class Cliente implements Serializable {

    private int    id;
    private String nombre;
    private String nit;
    private String telefono;
    private String direccion;
    private String ciudad;

    public Cliente() {}

    public Cliente(String nombre, String nit, String telefono,
                   String direccion, String ciudad) {
        this.nombre    = nombre;
        this.nit       = nit;
        this.telefono  = telefono;
        this.direccion = direccion;
        this.ciudad    = ciudad;
    }

    // ── Getters / Setters ──────────────────────────────────────────────────
    public int    getId()        { return id; }
    public void   setId(int id)  { this.id = id; }

    public String getNombre()              { return nombre; }
    public void   setNombre(String n)      { this.nombre = n; }

    public String getNit()                 { return nit; }
    public void   setNit(String n)         { this.nit = n; }

    public String getTelefono()            { return telefono; }
    public void   setTelefono(String t)    { this.telefono = t; }

    public String getDireccion()           { return direccion; }
    public void   setDireccion(String d)   { this.direccion = d; }

    public String getCiudad()              { return ciudad; }
    public void   setCiudad(String c)      { this.ciudad = c; }

    /** Lo que muestra el JComboBox */
    @Override
    public String toString() {
        return nombre + " - " + nit;
    }
}
