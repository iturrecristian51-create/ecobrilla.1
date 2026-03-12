package inventario;

import java.io.Serializable;

public class Usuario implements Serializable {
    private String nombreUsuario;
    private String contrasena;
    private String rol; // "Desarrollador", "Gerente", "Auxiliar"

    public Usuario(String nombreUsuario, String contrasena, String rol) {
        this.nombreUsuario = nombreUsuario;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    public String getNombreUsuario() { return nombreUsuario; }
    public String getContrasena() { return contrasena; }
    public String getRol() { return rol; }

    public boolean esDesarrollador() { return "Desarrollador".equalsIgnoreCase(rol); }
    public boolean esGerente() { return "Gerente".equalsIgnoreCase(rol); }
    public boolean esAuxiliar() { return "Auxiliar".equalsIgnoreCase(rol); }

    @Override
    public String toString() {
        return nombreUsuario + " (" + rol + ")";
    }
}
