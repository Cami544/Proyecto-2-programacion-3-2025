package hospital.presentation.Usuario;

import hospital.logic.Usuario;
import hospital.presentation.AbstractModel;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model extends AbstractModel {
    private List<Usuario> usuariosActivos;
    private Usuario usuarioSeleccionado;

    private Map<String, List<String>> mensajesPendientes;

    public static final String USUARIOS_ACTIVOS = "usuariosActivos";
    public static final String USUARIO_SELECCIONADO = "usuarioSeleccionado";

    public Model() {
        this.usuariosActivos = new ArrayList<>();
        this.usuarioSeleccionado = null;
        this.mensajesPendientes = new HashMap<>();
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
        if (usuario == null || usuario.getId() == null) return;
        boolean yaExiste = usuariosActivos.stream().anyMatch(u -> u.getId().equals(usuario.getId()));
        if (!yaExiste) {
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

    // ============== MÉTODOS PARA MENSAJES ==============

    public void agregarMensajePendiente(String remitenteId, String contenido) {
        mensajesPendientes.computeIfAbsent(remitenteId, k -> new ArrayList<>()).add(contenido);
        firePropertyChange(USUARIOS_ACTIVOS); // Refrescar tabla
    }

    public List<String> getMensajesPendientesDe(String remitenteId) {
        return mensajesPendientes.getOrDefault(remitenteId, new ArrayList<>());
    }

    public int getCantidadMensajesPendientes(String remitenteId) {
        List<String> mensajes = mensajesPendientes.get(remitenteId);
        return (mensajes == null) ? 0 : mensajes.size();
    }

    public void limpiarMensajesDe(String remitenteId) {
        mensajesPendientes.remove(remitenteId);
        firePropertyChange(USUARIOS_ACTIVOS); // Refrescar tabla
    }
}