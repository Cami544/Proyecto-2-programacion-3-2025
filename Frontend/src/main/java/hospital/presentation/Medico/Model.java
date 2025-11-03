package hospital.presentation.Medico;

import hospital.Application;
import hospital.logic.Medico;
import hospital.presentation.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class Model extends AbstractModel {
    private List<Medico> filtro;
    private List<Medico> list;
    private Medico current;
    private int mode;
    private String criterioFiltro; // 🔹 NUEVO: Para mantener el filtro activo

    public static final String LIST = "list";
    public static final String CURRENT = "current";
    public static final String FILTER = "filter";

    public Model() {
        init(new ArrayList<>());
        this.criterioFiltro = ""; // 🔹 NUEVO

        try {
            List<Medico> medicos = hospital.logic.Service.instance().getMedicos();
            this.list = medicos;
            this.filtro = new ArrayList<>(medicos);
        } catch (Exception e) {
            System.err.println("Error cargando medicos iniciales: " + e.getMessage());
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        firePropertyChange(LIST);
        firePropertyChange(CURRENT);
        firePropertyChange(FILTER);
    }

    public void init(List<Medico> list) {
        this.list = list;
        this.current = new Medico();
        this.filtro = new ArrayList<>(list);
        this.mode = Application.MODE_CREATE;
        this.criterioFiltro = ""; // 🔹 NUEVO
    }

    public List<Medico> getList() {
        return list;
    }

    public Medico getCurrent() {
        return current;
    }

    public List<Medico> getFiltered() {
        return filtro;
    }

    public int getModel() {
        return mode;
    }

    // 🔹 NUEVO: Getter y Setter para el criterio de filtro
    public String getCriterioFiltro() {
        return criterioFiltro;
    }

    public void setCriterioFiltro(String criterioFiltro) {
        this.criterioFiltro = criterioFiltro != null ? criterioFiltro : "";
    }

    public void setList(List<Medico> list) {
        this.list = list;
        firePropertyChange(LIST);
    }

    public void setFiltered(List<Medico> filter) {
        this.filtro = filter;
        firePropertyChange(FILTER);
    }

    public void setCurrent(Medico current) {
        this.current = current;
        firePropertyChange(CURRENT);
    }

    public void setModel(int mode) {
        this.mode = mode;
    }
}