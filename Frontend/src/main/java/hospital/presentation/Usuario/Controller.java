package hospital.presentation.Usuario;

import hospital.logic.Sesion;
import hospital.logic.Usuario;

import javax.swing.*;
import java.util.List;

public class Controller {
    private View view;
    private Model model;

    public Controller(View view, Model model) {
        this.view = view;
        this.model = model;
        view.setController(this);
        view.setModel(model);
    }

    public void enviarMensaje(Usuario destinatario, String mensaje) throws Exception {
        if (destinatario == null) {
            throw new Exception("Debe seleccionar un usuario destinatario");
        }
        if (mensaje == null || mensaje.trim().isEmpty()) {
            throw new Exception("El mensaje no puede estar vacío");
        }

        Usuario remitente = Sesion.getUsuario();
        if (remitente == null) {
            throw new Exception("No hay sesión activa");
        }

        String mensajeFormateado = destinatario.getId() + ":" + mensaje;

        hospital.logic.Service.instance().enviarMensaje(mensajeFormateado);

        System.out.println("Mensaje enviado de " + remitente.getId() + " a " + destinatario.getId() + ": " + mensaje);
    }

    public void procesarNotificacion(String mensaje) {
        if (mensaje == null || mensaje.trim().isEmpty()) {
            return;
        }

        String[] partes = mensaje.split(":", 2);

        if (partes.length < 2) {
            System.out.println("Formato de mensaje inválido: " + mensaje);
            return;
        }

        String primerToken = partes[0];
        String contenido = partes[1];
        String operacion = primerToken.toUpperCase();

        switch (operacion) {
            case "LOGIN":
                procesarLogin(contenido);
                break;
            case "LOGOUT":
                procesarLogout(contenido);
                break;
            default:
                recibirMensajeEnCola(primerToken, contenido);
                break;
        }
    }

    public void seleccionarUsuario(Usuario usuario) {
        model.setUsuarioSeleccionado(usuario);
    }

    private void procesarLogin(String userId) {
        Usuario nuevoUsuario = crearUsuarioGenerico(userId);
        model.agregarUsuario(nuevoUsuario);
        System.out.println(" Usuario conectado: " + userId);
    }

    private void procesarLogout(String userId) {
        model.removerUsuarioPorId(userId);
        System.out.println(" Usuario desconectado: " + userId);
    }

    private Usuario crearUsuarioGenerico(String userId) {
        if (userId.startsWith("MED-")) {
            hospital.logic.Medico medico = new hospital.logic.Medico();
            medico.setId(userId);
            medico.setNombre(userId);
            return medico;
        } else if (userId.startsWith("FAR-")) {
            hospital.logic.Farmaceuta farmaceuta = new hospital.logic.Farmaceuta();
            farmaceuta.setId(userId);
            farmaceuta.setNombre(userId);
            return farmaceuta;
        } else {
            hospital.logic.Administrador admin = new hospital.logic.Administrador();
            admin.setId(userId);
            admin.setNombre(userId);
            return admin;
        }
    }

    // ============== MÉTODOS PARA MENSAJES ==============

    private void recibirMensajeEnCola(String remitente, String contenido) {
        model.agregarMensajePendiente(remitente, contenido);

        System.out.println("Mensaje recibido de " + remitente + ": " + contenido);
        System.out.println("Mensajes pendientes de " + remitente + ": " +
                model.getCantidadMensajesPendientes(remitente));
    }

    public void mostrarMensajesDe(Usuario remitente) {
        if (remitente == null) {
            throw new IllegalArgumentException("Debe seleccionar un usuario");
        }

        String remitenteId = remitente.getId();
        List<String> mensajes = model.getMensajesPendientesDe(remitenteId);

        if (mensajes.isEmpty()) {
            JOptionPane.showMessageDialog(view.getPanel(),
                    "No hay mensajes de " + remitenteId,
                    "Sin Mensajes",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════\n");
        sb.append("   MENSAJES DE: ").append(remitenteId).append("\n");
        sb.append("═══════════════════════════════════\n\n");

        for (int i = 0; i < mensajes.size(); i++) {
            sb.append("> Mensaje ").append(i + 1).append(":\n");
            sb.append(mensajes.get(i)).append("\n\n");
            sb.append("───────────────────────────────────\n\n");
        }

        JOptionPane.showMessageDialog(view.getPanel(),
                sb.toString(),
                "Mensajes de " + remitenteId + " (" + mensajes.size() + ")",
                JOptionPane.INFORMATION_MESSAGE);

        model.limpiarMensajesDe(remitenteId);
    }

    public int getCantidadMensajesPendientes(String userId) {
        return model.getCantidadMensajesPendientes(userId);
    }

    public void agregarUsuarioActual() {
        if (Sesion.getUsuario() != null) {
            Usuario usuarioActual = Sesion.getUsuario();
            model.agregarUsuario(usuarioActual);
            System.out.println("Usuario actual agregado a la lista: " + usuarioActual.getId());
        }
    }

    public void sincronizarUsuariosIniciales(List<String> usuariosIds) {
        if (usuariosIds == null) return;
        for (String id : usuariosIds) {
            if (Sesion.getUsuario() != null && id.equals(Sesion.getUsuario().getId())) {
                continue;
            }
            Usuario u = crearUsuarioGenerico(id);
            model.agregarUsuario(u);
        }
    }


    public void refrescarDatos() {
        try {
            List<String> usuariosConectadosIds = hospital.logic.Service.instance().getUsuariosConectados();

            List<Usuario> usuariosConectados = new java.util.ArrayList<>();
            for (String userId : usuariosConectadosIds) {
                Usuario usuario = crearUsuarioGenerico(userId);
                usuariosConectados.add(usuario);
            }

            model.setUsuariosActivos(usuariosConectados);
            //System.out.println("✅ Tabla de usuarios refrescada. Total conectados: " + usuariosConectados.size());

        } catch (Exception ex) {
            System.err.println("Error refrescando usuarios conectados: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}