package hospital.logic;

import java.io.Serializable;
import java.util.Objects;

public abstract class Usuario implements Serializable {
    protected String id;
    protected String nombre;
    protected String rol;

    public Usuario() {}

    public Usuario(String id, String nombre, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.rol =rol;
    }
    public String getId() {return id;}
    public void setId(String id) {this.id = id;}
    public String getNombre() {return nombre;}
    public void setNombre(String nombre) {this.nombre = nombre;}
    public String getRol() {return rol;}
    public void setRol(String rol) {this.rol = rol;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Usuario usuario = (Usuario) o;
        return Objects.equals(id, usuario.id);
    }

    @Override
    public int hashCode() {return Objects.hash(id);}

    @Override
    public String toString() {return "Usuario{" + "id=" + id + ", nombre=" + nombre + ", rol=" + rol + '}';}

}
