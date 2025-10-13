package hospital.logic;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class Receta {
    private String id;
    private String pacienteId;
    private String farmaceutaId;
    private String estadoReceta;

    private LocalDate fecha;
    private LocalDate fechaRetiro;

    private List<DetalleReceta> detalles = new ArrayList<>();

    public Receta() {
        estadoReceta = "Confeccionada";
        farmaceutaId = "Sin asignar";
    }

    public Receta(String id, String pacienteId, LocalDate fecha) {
        this.id = id;
        this.pacienteId = pacienteId;
        this.fecha = fecha;
        this.fechaRetiro = fecha.plusDays(1);
        this.estadoReceta = "Confeccionada";
        this.farmaceutaId = "Sin asignar";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPacienteId() {
        return pacienteId;
    }

    public void setPacienteId(String pacienteId) {
        this.pacienteId = pacienteId;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDate getFechaRetiro() {
        return fechaRetiro;
    }

    public void setFechaRetiro(LocalDate fechaRetiro) {
        this.fechaRetiro = fechaRetiro;
    }

    public List<DetalleReceta> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleReceta> detalles) {
        this.detalles = detalles;
    }

    public String getEstadoReceta() { return estadoReceta; }

    public void setEstadoReceta(String estadoReceta) { this.estadoReceta = estadoReceta; }

    public String getFarmaceutaId() { return farmaceutaId; }

    public void setFarmaceutaId(String farmaceutaId) { this.farmaceutaId = farmaceutaId; }
}