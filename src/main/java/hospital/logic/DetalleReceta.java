package hospital.logic;

public class DetalleReceta {
    private int id;
    private int recetaId;
    private String medicamentoCodigo;
    private int cantidad;
    private String indicaciones;

    public DetalleReceta() {
    }

    public DetalleReceta(String medicamentoCodigo, int cantidad, String indicaciones) {
        this.medicamentoCodigo = medicamentoCodigo;
        this.cantidad = cantidad;
        this.indicaciones = indicaciones;
    }

    public DetalleReceta(int recetaId, String medicamentoCodigo, int cantidad, String indicaciones) {
        this.recetaId = recetaId;
        this.medicamentoCodigo = medicamentoCodigo;
        this.cantidad = cantidad;
        this.indicaciones = indicaciones;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRecetaId() {
        return recetaId;
    }

    public void setRecetaId(int recetaId) {
        this.recetaId = recetaId;
    }

    public String getMedicamentoCodigo() {
        return medicamentoCodigo;
    }

    public void setMedicamentoCodigo(String medicamentoCodigo) {
        this.medicamentoCodigo = medicamentoCodigo;
    }

    public String getIndicaciones() {
        return indicaciones;
    }

    public void setIndicaciones(String indicaciones) {
        this.indicaciones = indicaciones;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    @Override
    public String toString() {
        return "DetalleReceta{" +
                "id=" + id +
                ", recetaId='" + recetaId + '\'' +
                ", medicamentoCodigo='" + medicamentoCodigo + '\'' +
                ", cantidad=" + cantidad +
                ", indicaciones='" + indicaciones + '\'' +
                '}';
    }
}