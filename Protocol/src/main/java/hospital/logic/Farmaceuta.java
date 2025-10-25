package hospital.logic;


import java.util.Objects;

public class Farmaceuta extends Usuario {
    private String clave;

    public Farmaceuta() {
        this("", "", "", "FAR");
    }

    public Farmaceuta(String id, String nombre, String clave) {
        super(id, nombre, "FAR");
        this.clave = clave;
    }

    public Farmaceuta(String id, String nombre, String clave, String rol) {
        super(id, nombre, rol);
        this.clave = clave;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String toString() {
        return id + "-" + nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Farmaceuta farmaceuta = (Farmaceuta) o;
        return Objects.equals(id, farmaceuta.id);
    }

    @Override
    public int hashCode() {return Objects.hash(id, nombre, clave);}

}