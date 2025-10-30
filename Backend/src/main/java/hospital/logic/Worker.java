package hospital.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;

public class Worker {
    Server srv;
    String sid;
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
    }

    public void setAs(Socket as, ObjectOutputStream aos, ObjectInputStream ais){
        this.as = as;
        this.aos = aos;
        this.ais = ais;
        System.out.println("Canal asíncrono establecido para SID: " + sid);
    }
    public String getSid(){ return sid; }

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
                    System.err.println("[Worker][ERROR] Código fuera de rango: " + method);// cerrar conexión corrupta
                    stop();
                    srv.remove(this);
                    break; // salir del while
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
                            String nombre = is.readUTF();
                            List<Paciente> le=service.searchPacientes(nombre);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
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
                            List<Paciente> le = service.getPacientes();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;


                        //------------------- CASE MEDICO ------------------

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
                            String nombre = is.readUTF();
                            List<Medico> le=service.searchMedicos(nombre);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
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
                            List<Medico> le = service.getMedicos();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;

                    //-------------- CASE FARMACEUTAS ---------------------

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
                            // conexión corrupta: cerramos el worker para forzar reconexión del cliente
                            stop();
                            srv.remove(this);
                            return; // salir del listener
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
                            System.out.println("Intentando eliminar farmaceuta con id: '" + id + "'");
                            service.deleteFarmaceuta(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            System.out.println("Eliminado correctamente: " + id);
                        } catch (Exception ex) {
                            System.out.println("Error eliminando farmaceuta: " + ex.getMessage());
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        } finally {
                            os.flush();
                        }
                        break;

                    case Protocol.FARMACEUTA_SEARCH:
                        try {
                            String nombre = is.readUTF();
                            List<Farmaceuta> le=service.searchFarmaceutas(nombre);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
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
                            List<Farmaceuta> le = service.getFarmaceutas();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;


                    //-------------- CASE MEDICAMENTOS ---------------------

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
                            String id = is.readUTF();
                            Medicamento e = service.readMedicamento(id);
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
                            String id = is.readUTF();
                            service.deleteMedicamento(id);
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
                            String nombre = is.readUTF();
                            List<Medicamento> le=service.searchMedicamentos(nombre);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
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
                            List<Medicamento> le = service.getMedicamentos();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;

                    //-------------- CASE RECETAS ---------------------

                    case Protocol.RECETA_CREATE:
                        try {
                            Receta receta = (Receta) is.readObject();
                            LocalDate fecha = (LocalDate) is.readObject();
                            service.createReceta(receta, fecha);
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
                            String id = is.readUTF();
                            Receta e = service.readReceta(id);
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
                            Receta receta = (Receta) is.readObject();
                            service.updateReceta(receta);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            System.err.println("[Worker][ERROR][UPDATE_RECETA] " + ex);
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        } finally {
                            os.flush();
                        }
                        break;
                    case Protocol.RECETA_DELETE:
                        try {
                            String id = (String) is.readObject();
                            service.deleteReceta(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        } finally {
                            os.flush();
                        }
                        break;
                    case Protocol.RECETA_SEARCH:
                        try {
                            String nombre = is.readUTF();
                            List<Receta> le = service.searchRecetasByPaciente(nombre);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.RECETA_GETALL:
                        try {
                            List<Receta> recetas = service.getRecetas();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(recetas);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;

                    //-------------- CASE ADMINISTRADOR ---------------------

                    case Protocol.ADMINISTRADOR_CREATE:
                        try {
                            service.createAdministrador((Administrador) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.ADMINISTRADOR_READ:
                        try {
                            String id = is.readUTF();
                            Administrador e = service.readAdministrador(id);
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
                    case Protocol.ADMINISTRADOR_UPDATE:
                        try {
                            service.updateAdministrador((Administrador) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.ADMINISTRADOR_DELETE:
                        try {
                            String id = is.readUTF();
                            service.deleteAdministrador(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;
                    case Protocol.ADMINISTRADOR_GETALL:
                        try {
                            List<Administrador> le = service.getAllAdministradores();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
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
                            System.out.println("[Worker] Iniciando DETALLERECETA_UPDATE");
                            DetalleReceta d = (DetalleReceta) is.readObject();
                            service.createDetalleReceta(d);
                            System.out.println("[Worker] Detalle recibido id=" + d.getId() + ", recetaId=" + d.getRecetaId());

                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                            System.out.println("[Worker] Confirmación detalle rece enviada (0)");
                        }
                        break;
                    case Protocol.DETALLE_RECETA_UPDATE:
                        try {
                            service.updateDetalleReceta((DetalleReceta) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
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
                            List<DetalleReceta> le = service.getDetallesPorReceta(recetaId);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
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
                            List<DetalleReceta> le = service.getAllDetallesReceta();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                            ex.printStackTrace();
                        }
                        finally {
                            os.flush();
                        }
                        break;


                    //-------------- CASE AUTENTICACIÓN ---------------------

                    case Protocol.AUTHENTICATE:
                        try {
                            String id = is.readUTF();
                            String clave = is.readUTF();
                            Usuario usuario = service.authenticate(id, clave);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(usuario);
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
                    case Protocol.DELIVER_MESSAGE:
                        try {
                            String message = (String) is.readObject();
                            System.out.println("Mensaje recibido para broadcast: " + message);
                            srv.deliver_message(this, message);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
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
                        stop();
                        srv.remove(this);
                        break;
                }
                os.flush();
            } catch (IOException e) {
                System.out.println("[Worker] Cliente desconectado (" + sid + ")");
                stop();
                srv.remove(this);
            }
        }
    }
}