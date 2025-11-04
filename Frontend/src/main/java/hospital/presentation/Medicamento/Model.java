package hospital.presentation.Medicamento;

import hospital.Application;
import hospital.logic.Medicamento;
import hospital.presentation.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class Model extends AbstractModel {
    private List<Medicamento> filtro;
    private List<Medicamento> list;
    private Medicamento current;
    private int mode;
    private String criterioFiltro; // 🔹 NUEVO

    public static final String LIST = "list";
    public static final String CURRENT = "current";
    public static final String FILTERED = "filtered";

    public Model() {
        init(new ArrayList<>());
        this.criterioFiltro = ""; // 🔹 NUEVO

        try {
            List<Medicamento> medicamentos = hospital.logic.Service.instance().getMedicamentos();
            this.list = medicamentos;
            this.filtro = new ArrayList<>(medicamentos);
        } catch (Exception e) {
            System.err.println("Error cargando medicamentos iniciales: " + e.getMessage());
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        firePropertyChange(LIST);
        firePropertyChange(CURRENT);
        firePropertyChange(FILTERED);
    }

    public void init(List<Medicamento> list) {
        this.list = list;
        this.current = new Medicamento();
        this.filtro = new ArrayList<>(list);
        this.mode = Application.MODE_CREATE;
        this.criterioFiltro = ""; // 🔹 NUEVO
    }

    public List<Medicamento> getList() {
        return list;
    }

    public Medicamento getCurrent() {
        return current;
    }

    public List<Medicamento> getFiltered() {
        return filtro;
    }

    public int getModel() {
        return mode;
    }

    public String getCriterioFiltro() {
        return criterioFiltro;
    }

    public void setCriterioFiltro(String criterioFiltro) {
        this.criterioFiltro = criterioFiltro != null ? criterioFiltro : "";
    }

    public void setList(List<Medicamento> list) {
        this.list = list;
        firePropertyChange(LIST);
    }

    public void setFiltered(List<Medicamento> filter) {
        this.filtro = filter;
        firePropertyChange(FILTERED);
    }

    public void setCurrent(Medicamento current) {
        this.current = current;
        firePropertyChange(CURRENT);
    }

    public void setModel(int mode) {
        this.mode = mode;
    }
}