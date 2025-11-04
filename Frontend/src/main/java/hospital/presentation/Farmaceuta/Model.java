package hospital.presentation.Farmaceuta;

import hospital.Application;
import hospital.logic.Farmaceuta;
import hospital.presentation.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class Model extends AbstractModel {
    private List<Farmaceuta> filtro;
    private List<Farmaceuta> list;
    private Farmaceuta current;
    private int mode;
    private String criterioFiltro;

    public static final String LIST = "list";
    public static final String CURRENT = "current";
    public static final String FILTER = "filter";

    public Model() {
        init(new ArrayList<>());
        this.criterioFiltro = "";

        try {
            List<Farmaceuta> farmaceutas = hospital.logic.Service.instance().getFarmaceutas();
            this.list = farmaceutas;
            this.filtro = new ArrayList<>(farmaceutas);
        } catch (Exception e) {
            System.err.println("Error cargando farmaceutas iniciales: " + e.getMessage());
        }
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        firePropertyChange(LIST);
        firePropertyChange(CURRENT);
        firePropertyChange(FILTER);
    }

    public void init(List<Farmaceuta> list) {
        this.list = list;
        this.current = new Farmaceuta();
        this.filtro = new ArrayList<>(list);
        this.mode = Application.MODE_CREATE;
        this.criterioFiltro = ""; // 🔹 NUEVO
    }

    public List<Farmaceuta> getList() {
        return list;
    }

    public Farmaceuta getCurrent() {
        return current;
    }

    public List<Farmaceuta> getFiltered() {
        return filtro;
    }

    public int getModel() {
        return mode;
    }

    // 🔹 NUEVO
    public String getCriterioFiltro() {
        return criterioFiltro;
    }

    public void setCriterioFiltro(String criterioFiltro) {
        this.criterioFiltro = criterioFiltro != null ? criterioFiltro : "";
    }

    public void setList(List<Farmaceuta> list) {
        this.list = list;
        firePropertyChange(LIST);
    }

    public void setFiltered(List<Farmaceuta> filter) {
        this.filtro = filter;
        firePropertyChange(FILTER);
    }

    public void setCurrent(Farmaceuta current) {
        this.current = current;
        firePropertyChange(CURRENT);
    }

    public void setModel(int mode) {
        this.mode = mode;
    }
}