package hospital.presentation.Usuario;

import hospital.logic.Usuario;
import hospital.presentation.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

public class Model extends AbstractModel {
    private List<Usuario> usuariosActivos;
    private Usuario usuarioSeleccionado;

    public static final String USUARIOS_ACTIVOS = "usuariosActivos";
    public static final String USUARIO_SELECCIONADO = "usuarioSeleccionado";

    public Model() {
        this.usuariosActivos = new ArrayList<>();
        this.usuarioSeleccionado = null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        super.addPropertyChangeListener(listener);
        firePropertyChange(USUARIOS_ACTIVOS);
        firePropertyChange(USUARIO_SELECCIONADO);
    }

    public List<Usuario> getUsuariosActivos() {
        return usuariosActivos;
    }

    public void setUsuariosActivos(List<Usuario> usuariosActivos) {
        this.usuariosActivos = usuariosActivos;
        firePropertyChange(USUARIOS_ACTIVOS);
    }

    public Usuario getUsuarioSeleccionado() {
        return usuarioSeleccionado;
    }

    public void setUsuarioSeleccionado(Usuario usuarioSeleccionado) {
        this.usuarioSeleccionado = usuarioSeleccionado;
        firePropertyChange(USUARIO_SELECCIONADO);
    }

    public void agregarUsuario(Usuario usuario) {
        if (!usuariosActivos.contains(usuario)) {
            usuariosActivos.add(usuario);
            firePropertyChange(USUARIOS_ACTIVOS);
        }
    }

    public void removerUsuario(Usuario usuario) {
        usuariosActivos.remove(usuario);
        if (usuarioSeleccionado != null && usuarioSeleccionado.equals(usuario)) {
            usuarioSeleccionado = null;
            firePropertyChange(USUARIO_SELECCIONADO);
        }
        firePropertyChange(USUARIOS_ACTIVOS);
    }

    public void removerUsuarioPorId(String id) {
        usuariosActivos.removeIf(u -> u.getId().equals(id));
        if (usuarioSeleccionado != null && usuarioSeleccionado.getId().equals(id)) {
            usuarioSeleccionado = null;
            firePropertyChange(USUARIO_SELECCIONADO);
        }
        firePropertyChange(USUARIOS_ACTIVOS);
    }
}