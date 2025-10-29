package hospital.presentation.Usuario;

import hospital.logic.Usuario;
import hospital.presentation.AbstractTableModel;

import java.util.List;

public class TableModel extends AbstractTableModel<Usuario> implements javax.swing.table.TableModel {

    public static final int ID = 0;
    public static final int MENSAJES = 1;

    public TableModel(int[] cols, List<Usuario> rows) {
        super(cols, rows);
    }

    @Override
    protected Object getPropertyAt(Usuario usuario, int col) {
        switch (cols[col]) {
            case ID:
                return usuario.getId();
            case MENSAJES:
                return "?";
            default:
                return "";
        }
    }

    @Override
    protected void initColNames() {
        colNames = new String[2];
        colNames[ID] = "Id";
        colNames[MENSAJES] = "Mensajes?";
    }
}