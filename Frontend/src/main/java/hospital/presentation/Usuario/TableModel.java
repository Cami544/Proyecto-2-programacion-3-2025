package hospital.presentation.Usuario;

import hospital.logic.Usuario;
import hospital.presentation.AbstractTableModel;

import java.util.List;

public class TableModel extends AbstractTableModel<Usuario> implements javax.swing.table.TableModel {

    public static final int ID = 0;
    public static final int MENSAJES = 1;

    private Controller controller;

    public TableModel(int[] cols, List<Usuario> rows) {
        super(cols, rows);
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    @Override
    protected Object getPropertyAt(Usuario usuario, int col) {
        switch (cols[col]) {
            case ID:
                return usuario.getId();
            case MENSAJES:
                if (controller != null) {
                    int cantidad = controller.getCantidadMensajesPendientes(usuario.getId());
                    return cantidad > 0 ? String.valueOf(cantidad) : "-";
                }
                return "-";
            default:
                return "";
        }
    }

    @Override
    protected void initColNames() {
        colNames = new String[2];
        colNames[ID] = "Id";
        colNames[MENSAJES] = "Mensajes";
    }
}