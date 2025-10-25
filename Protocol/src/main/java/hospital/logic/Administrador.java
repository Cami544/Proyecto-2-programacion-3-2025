package hospital.logic;

import java.io.Serializable;
import java.util.Objects;

public class Administrador extends Usuario {
    private String clave;

    public Administrador() {
        this("", "", "", "ADM");
    }

    public Administrador(String id, String nombre, String clave) {
        super(id, nombre, "ADM");
        this.clave = clave;
    }

    public Administrador(String id, String nombre, String clave, String rol) {
        super(id, nombre, rol);
        this.clave = clave;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Administrador administrador = (Administrador) o;
        return Objects.equals(id, administrador.id);
    }

    @Override
    public int hashCode() {return Objects.hash(id, nombre, clave);}


}
