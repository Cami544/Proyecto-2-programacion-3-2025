package hospital.logic;


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
}