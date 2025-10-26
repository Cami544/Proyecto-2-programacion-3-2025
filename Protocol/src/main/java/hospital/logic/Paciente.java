package hospital.logic;
import java.time.LocalDate;

public class Paciente extends Usuario {

    private LocalDate fechaNacimiento;
    private String numeroTelefono;

    public Paciente(){}

    public Paciente(String id, String nombre, LocalDate fechaNacimiento, String numeroTelefono) {
        this.id = id;
        this.nombre = nombre;
        this.fechaNacimiento = fechaNacimiento;
        this.numeroTelefono = numeroTelefono;
    }

    public LocalDate getFechaNacimiento() {return fechaNacimiento;}
    public void setFechaNacimiento(LocalDate fechaNacimiento) {this.fechaNacimiento = fechaNacimiento;}
    public String getNumeroTelefono() {return numeroTelefono;}
    public void setNumeroTelefono(String numeroTelefono) {this.numeroTelefono = numeroTelefono;}

}
