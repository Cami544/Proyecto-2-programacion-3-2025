package hospital.presentation.Usuario;

import hospital.logic.Sesion;
import hospital.logic.Usuario;

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

        String mensajeFormateado = remitente.getId() + ":" + mensaje;

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
                view.mostrarMensajeRecibido(operacion, contenido);
                break;
        }
    }

    private void procesarLogin(String userId) {
        if (Sesion.getUsuario() != null && Sesion.getUsuario().getId().equals(userId)) {
            return;
        }

        // Crear un usuario genérico usando una clase concreta
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
        // Intentar determinar el tipo de usuario por el prefijo del ID
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
            // Por defecto, crear un Administrador
            hospital.logic.Administrador admin = new hospital.logic.Administrador();
            admin.setId(userId);
            admin.setNombre(userId);
            return admin;
        }
    }
}