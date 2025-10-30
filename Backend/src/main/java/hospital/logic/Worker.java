package hospital.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Worker {
    Server srv;
    String sid;
    String userId; // ID del usuario autenticado
    boolean continuar;

    Socket s; // síncrono
    Service service;
    ObjectOutputStream os;
    ObjectInputStream is;

    Socket as; // asíncrono
    ObjectOutputStream aos;
    ObjectInputStream ais;

    public Worker(Server srv, Socket s, ObjectOutputStream os, ObjectInputStream is, String sid, Service service){
        this.srv = srv;
        this.s = s;
        this.os = os;
        this.is = is;
        this.service = service;
        this.sid = sid;
        this.userId = null; // Se establecerá al autenticarse
    }

    public void setAs(Socket as, ObjectOutputStream aos, ObjectInputStream ais){
        this.as = as;
        this.aos = aos;
        this.ais = ais;
        System.out.println("Canal asíncrono establecido para SID: " + sid);
    }

    public String getSid(){ return sid; }
    public String getUserId(){ return userId; }

    public void start(){
        try {
            System.out.println("Worker"  + sid + " atendiendo peticiones...");
            Thread t = new Thread(new Runnable(){
                public void run(){
                    listen();
                }
            });
            continuar = true;
            t.start();
        } catch (Exception ex) { }
    }

    public void stop() {
        continuar = false;
        try {
            if (is != null) is.close();
            if (os != null) os.close();
            if (s != null && !s.isClosed()) s.close();

            if (ais != null) ais.close();
            if (aos != null) aos.close();
            if (as != null && !as.isClosed()) as.close();
        } catch (IOException e) {
            System.out.println("Error cerrando conexiones Worker[" + sid + "]: " + e.getMessage());
        }
    }

    public boolean isAsyncReady() { return aos != null;}

    public synchronized void deliverMessage(String message) {
        if (aos != null) {
            try {
                aos.writeInt(Protocol.DELIVER_MESSAGE);
                aos.writeObject(message);
                aos.flush();
            } catch (Exception e) {
                System.err.println("[Worker][ERROR] En deliverMessage: " + e);
            }
        }
    }


    public void listen(){
        int method;

        System.out.println("[Worker] Esperando operación...");
        while (continuar) {


            try {
                method = is.readInt();
                System.out.println("[Worker] Operación recibida: " + method);
                System.out.println("Operacion: "+method);

                if (method < 100 || method > 999) {
                    System.err.println("[Worker][ERROR] Código fuera de rango: " + method);
                    stop();
                    srv.remove(this);
                    break;
                }

                switch(method) {

                    //-------------- CASE PACIENTE ---------------------

                    case Protocol.PACIENTE_CREATE:
                        try {
                            Paciente p = (Paciente) is.readObject();
                            service.createPaciente(p);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            srv.deliver_message(this, "Paciente creado: " + p.getNombre());
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace(); }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.PACIENTE_READ:
                        try {
                            String id = is.readUTF();
                            Paciente e = service.readPaciente(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(e);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.PACIENTE_UPDATE:
                        try {
                            service.updatePaciente((Paciente) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.PACIENTE_DELETE:
                        try {
                            String id = is.readUTF();
                            service.deletePaciente(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.PACIENTE_SEARCH:
                        try {
                            String criterio = is.readUTF();
                            List<Paciente> result = service.searchPacientes(criterio);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(result);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.PACIENTE_GETALL:
                        try {
                            List<Paciente> result = service.getPacientes();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(result);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;

                    //-------------- CASE MEDICO ---------------------

                    case Protocol.MEDICO_CREATE:
                        try {
                            service.createMedico((Medico) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.MEDICO_READ:
                        try {
                            String id = is.readUTF();
                            Medico e = service.readMedico(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(e);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.MEDICO_UPDATE:
                        try {
                            service.updateMedico((Medico) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.MEDICO_DELETE:
                        try {
                            String id = is.readUTF();
                            service.deleteMedico(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.MEDICO_SEARCH:
                        try {
                            String criterio = is.readUTF();
                            List<Medico> result = service.searchMedicos(criterio);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(result);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.MEDICO_GETALL:
                        try {
                            List<Medico> result = service.getMedicos();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(result);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;

                    //-------------- CASE FARMACEUTA ---------------------

                    case Protocol.FARMACEUTA_CREATE:
                        try {
                            service.createFarmaceuta((Farmaceuta) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.FARMACEUTA_READ:
                        try {
                            String id = is.readUTF();
                            Farmaceuta e = service.readFarmaceuta(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(e);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.FARMACEUTA_UPDATE:
                        System.out.println("[Worker] Iniciando FARMACEUTA_UPDATE");
                        try {
                            Farmaceuta f = (Farmaceuta) is.readObject();
                            System.out.println("[Worker] Farmaceuta recibido: id=" + f.getId() + ", nombre=" + f.getNombre());
                            service.updateFarmaceuta(f);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            System.out.println("[Worker] FARMACEUTA_UPDATE completado con éxito");
                        } catch (ClassNotFoundException | IOException readEx) {
                            System.err.println("[Worker][ERROR] Error leyendo Farmaceuta: " + readEx);
                            try { os.writeInt(Protocol.ERROR_ERROR); os.flush(); } catch (Exception ignore) {}
                            stop();
                            srv.remove(this);
                            return;
                        } catch (Exception ex) {
                            System.err.println("[Worker][ERROR] Error actualizando Farmaceuta: " + ex);
                            ex.printStackTrace();
                        } finally {
                            try { os.flush(); } catch (IOException ignore) {}
                        }
                        break;

                    case Protocol.FARMACEUTA_DELETE:
                        try {
                            String id = is.readUTF();
                            System.out.println("Intentando eliminar farmaceuta: " + id);
                            service.deleteFarmaceuta(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.FARMACEUTA_SEARCH:
                        try {
                            String criterio = is.readUTF();
                            List<Farmaceuta> result = service.searchFarmaceutas(criterio);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(result);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.FARMACEUTA_GETALL:
                        try {
                            List<Farmaceuta> result = service.getFarmaceutas();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(result);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;

                    //-------------- CASE MEDICAMENTO ---------------------

                    case Protocol.MEDICAMENTO_CREATE:
                        try {
                            service.createMedicamento((Medicamento) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.MEDICAMENTO_READ:
                        try {
                            String codigo = is.readUTF();
                            Medicamento e = service.readMedicamento(codigo);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(e);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.MEDICAMENTO_UPDATE:
                        try {
                            service.updateMedicamento((Medicamento) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.MEDICAMENTO_DELETE:
                        try {
                            String codigo = is.readUTF();
                            service.deleteMedicamento(codigo);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.MEDICAMENTO_SEARCH:
                        try {
                            String criterio = is.readUTF();
                            List<Medicamento> result = service.searchMedicamentos(criterio);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(result);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.MEDICAMENTO_GETALL:
                        try {
                            List<Medicamento> result = service.getMedicamentos();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(result);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;

                    //-------------- CASE RECETA ---------------------

                    case Protocol.RECETA_CREATE:
                        try {
                            Receta receta = (Receta) is.readObject();
                            LocalDate fechaRetiro = (LocalDate) is.readObject();
                            service.createReceta(receta, fechaRetiro);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.RECETA_READ:
                        try {
                            int id = is.readInt();
                            Receta e = service.readReceta(String.valueOf(id));
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(e);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.RECETA_UPDATE:
                        try {
                            service.updateReceta((Receta) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.RECETA_DELETE:
                        try {
                            int id = is.readInt();
                            service.deleteReceta(String.valueOf(id));
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.RECETA_SEARCH:
                        try {
                            String pacienteId = is.readUTF();
                            List<Receta> result = service.searchRecetasByPaciente(pacienteId);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(result);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    // TODO: Implementar búsqueda de recetas por médico si es necesario
                    // case Protocol.RECETA_GETXMEDICO no existe en Protocol.java

                    case Protocol.RECETA_GETALL:
                        try {
                            List<Receta> result = service.getRecetas();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(result);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;

                    //-------------- CASE DETALLE RECETA ---------------------

                    case Protocol.DETALLE_RECETA_CREATE:
                        try {
                            service.createDetalleReceta((DetalleReceta) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.DETALLE_RECETA_UPDATE:
                        System.out.println("[Worker] Iniciando DETALLERECETA_UPDATE");
                        try {
                            DetalleReceta d = (DetalleReceta) is.readObject();
                            System.out.println("[Worker] DetalleReceta recibido: id=" + d.getId() + ", recetaId=" + d.getRecetaId());
                            service.updateDetalleReceta(d);
                            os.writeInt(200);
                            System.out.println("[Worker] DETALLERECETA_UPDATE completado");
                        } catch (ClassNotFoundException | IOException readEx) {
                            System.err.println("[Worker][ERROR] Error leyendo DetalleReceta: " + readEx);
                            try { os.writeInt(Protocol.ERROR_ERROR); os.flush(); } catch (Exception ignore) {}
                            stop();
                            srv.remove(this);
                            return;
                        } catch (Exception ex) {
                            System.err.println("[Worker][ERROR] Error actualizando DetalleReceta: " + ex);
                            ex.printStackTrace();
                        } finally {
                            try { os.flush(); } catch (IOException ignore) {}
                        }
                        break;
                    case Protocol.DETALLE_RECETA_DELETE:
                        try {
                            int id = is.readInt();
                            service.deleteDetalleReceta(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.DETALLE_RECETA_GETXRECETA:
                        try {
                            int recetaId = is.readInt();
                            List<DetalleReceta> result = service.getDetallesPorReceta(recetaId);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(result);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.DETALLE_RECETA_GETALL:
                        try {
                            List<DetalleReceta> result = service.getAllDetallesReceta();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(result);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;

                    //-------------- AUTENTICACIÓN ---------------------

                    case Protocol.AUTHENTICATE:
                        try {
                            String id = is.readUTF();
                            String clave = is.readUTF();
                            Usuario usuario = service.authenticate(id, clave);

                            // Guardar el userId en el worker
                            this.userId = usuario.getId();
                            System.out.println("[Worker] Usuario autenticado: " + userId);

                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(usuario);

                            // Notificar a todos que este usuario se conectó
                            srv.deliver_message(this, "LOGIN:" + userId);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;

                    case Protocol.CHANGE_PASSWORD:
                        try {
                            String id = is.readUTF();
                            String claveActual = is.readUTF();
                            String claveNueva = is.readUTF();
                            service.cambiarClave(id, claveActual, claveNueva);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;

                    //-------------- MENSAJES ---------------------

                    case Protocol.DELIVER_MESSAGE:
                        try {
                            String message = (String) is.readObject();
                            System.out.println("Mensaje recibido: " + message);

                            // Formato: DESTINATARIO:CONTENIDO
                            String[] partes = message.split(":", 2);
                            if (partes.length == 2) {
                                String destinatarioId = partes[0];
                                String contenido = partes[1];

                                // Enviar solo al destinatario específico
                                String mensajeConRemitente = userId + ":" + contenido;
                                srv.deliver_message_to_user(destinatarioId, mensajeConRemitente);

                                os.writeInt(Protocol.ERROR_NO_ERROR);
                            } else {
                                os.writeInt(Protocol.ERROR_ERROR);
                            }
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            System.err.println("Error procesando mensaje: " + ex.getMessage());
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;


                    case Protocol.DISCONNECT:
                        // Notificar logout antes de desconectar
                        if (userId != null) {
                            srv.deliver_message(this, "LOGOUT:" + userId);
                        }
                        stop();
                        srv.remove(this);
                        break;
                }
                os.flush();
            } catch (IOException e) {
                System.out.println("[Worker] Cliente desconectado (" + sid + ")");
                if (userId != null) {
                    srv.deliver_message(this, "LOGOUT:" + userId);
                }
                stop();
                srv.remove(this);
            }
        }
    }
}
