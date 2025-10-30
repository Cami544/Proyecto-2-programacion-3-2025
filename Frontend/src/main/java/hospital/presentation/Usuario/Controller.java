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

        // Formato: DESTINATARIO:CONTENIDO
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

        String operacion = partes[0].toUpperCase();
        String contenido = partes[1];

        switch (operacion) {
            case "LOGIN":
                procesarLogin(contenido);
                break;
            case "LOGOUT":
                procesarLogout(contenido);
                break;
            default:
                // Es un mensaje de usuario (formato REMITENTE:CONTENIDO)
                recibirMensajeEnCola(operacion, contenido);
                break;
        }
    }

    private void procesarLogin(String userId) {
        if (Sesion.getUsuario() != null && Sesion.getUsuario().getId().equals(userId)) {
            return;
        }

        Usuario nuevoUsuario = crearUsuarioGenerico(userId);
        model.agregarUsuario(nuevoUsuario);
        System.out.println("Usuario conectado: " + userId);
    }

    private void procesarLogout(String userId) {
        model.removerUsuarioPorId(userId);
        System.out.println("Usuario desconectado: " + userId);
    }

    public void seleccionarUsuario(Usuario usuario) {
        model.setUsuarioSeleccionado(usuario);
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

    /**
     * Recibir mensaje y guardarlo en el modelo
     */
    private void recibirMensajeEnCola(String remitente, String contenido) {
        model.agregarMensajePendiente(remitente, contenido);

        System.out.println("Mensaje recibido de " + remitente + ": " + contenido);
        System.out.println("Mensajes pendientes de " + remitente + ": " +
                model.getCantidadMensajesPendientes(remitente));
    }

    /**
     * Mostrar mensajes pendientes de un usuario (llamado al presionar "Recibir")
     */
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

        // Mostrar todos los mensajes
        StringBuilder sb = new StringBuilder();
        sb.append("═══════════════════════════════════\n");
        sb.append("   MENSAJES DE: ").append(remitenteId).append("\n");
        sb.append("═══════════════════════════════════\n\n");

        for (int i = 0; i < mensajes.size(); i++) {
            sb.append("► Mensaje ").append(i + 1).append(":\n");
            sb.append(mensajes.get(i)).append("\n\n");
            sb.append("───────────────────────────────────\n\n");
        }

        JOptionPane.showMessageDialog(view.getPanel(),
                sb.toString(),
                "Mensajes de " + remitenteId + " (" + mensajes.size() + ")",
                JOptionPane.INFORMATION_MESSAGE);

        // Marcar como leídos y limpiar
        model.limpiarMensajesDe(remitenteId);
    }

    public int getCantidadMensajesPendientes(String userId) {
        return model.getCantidadMensajesPendientes(userId);
    }
}