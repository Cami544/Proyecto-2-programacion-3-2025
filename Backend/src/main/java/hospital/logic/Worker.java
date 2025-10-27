package hospital.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;

public class Worker {
    Server srv;
    Socket s;
    Service service;
    ObjectOutputStream os;
    ObjectInputStream is;

    public Worker(Server srv, Socket s, Service service) {
        try{
            this.srv=srv;
            this.s=s;
            os = new ObjectOutputStream(s.getOutputStream());
            is = new ObjectInputStream(s.getInputStream());
            this.service=service;
        } catch (IOException ex) { System.out.println(ex); }
    }

    boolean continuar;
    public void start(){
        try {
            System.out.println("Worker atendiendo peticiones...");
            Thread t = new Thread(new Runnable(){
                public void run(){
                    listen();
                }
            });
            continuar = true;
            t.start();
        } catch (Exception ex) { }
    }

    public void stop(){
        continuar=false;
        System.out.println("Conexion cerrada...");
    }

    public void listen(){
        int method;
        while (continuar) {
            try {
                method = is.readInt();
                System.out.println("Operacion: "+method);
                switch(method) {

                    //-------------- CASE PACIENTE ---------------------

                    case Protocol.PACIENTE_CREATE:
                        try {
                            service.createPaciente((Paciente) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) { os.writeInt(Protocol.ERROR_ERROR); }
                        break;
                    case Protocol.PACIENTE_READ:
                        try {
                            String id = is.readUTF();
                            Paciente e = service.readPaciente(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(e);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;
                    case Protocol.PACIENTE_UPDATE:
                        try {
                            service.updatePaciente((Paciente) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;
                    case Protocol.PACIENTE_DELETE:
                        try {
                            String id = is.readUTF();
                            service.deletePaciente(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
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
                        }
                        break;

                    case Protocol.PACIENTE_GETALL:
                        try {
                            List<Paciente> le = service.getPacientes();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;


                        //------------------- CASE MEDICO ------------------

                    case Protocol.MEDICO_CREATE:
                        try {
                            service.createMedico((Medico) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) { os.writeInt(Protocol.ERROR_ERROR); }
                        break;
                    case Protocol.MEDICO_READ:
                        try {
                            String id = is.readUTF();
                            Medico e = service.readMedico(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(e);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;
                    case Protocol.MEDICO_UPDATE:
                        try {
                            service.updateMedico((Medico) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;
                    case Protocol.MEDICO_DELETE:
                        try {
                            String id = is.readUTF();
                            service.deleteMedico(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
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
                        }
                        break;


                    case Protocol.MEDICO_GETALL:
                        try {
                            List<Medico> le = service.getMedicos();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;

                    //-------------- CASE FARMACEUTAS ---------------------

                    case Protocol.FARMACEUTA_CREATE:
                        try {
                            service.createFarmaceuta((Farmaceuta) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) { os.writeInt(Protocol.ERROR_ERROR); }
                        break;
                    case Protocol.FARMACEUTA_READ:
                        try {
                            String id = is.readUTF();
                            Farmaceuta e = service.readFarmaceuta(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(e);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;
                    case Protocol.FARMACEUTA_UPDATE:
                        try {
                            service.updateFarmaceuta((Farmaceuta) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;
                    case Protocol.FARMACEUTA_DELETE:
                        try {
                            String id = is.readUTF();
                            service.deleteFarmaceuta(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
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
                        }
                        break;


                    case Protocol.FARMACEUTA_GETALL:
                        try {
                            List<Farmaceuta> le = service.getFarmaceutas();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;


                    //-------------- CASE MEDICAMENTOS ---------------------

                    case Protocol.MEDICAMENTO_CREATE:
                        try {
                            service.createMedicamento((Medicamento) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) { os.writeInt(Protocol.ERROR_ERROR); }
                        break;
                    case Protocol.MEDICAMENTO_READ:
                        try {
                            String id = is.readUTF();
                            Medicamento e = service.readMedicamento(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(e);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;
                    case Protocol.MEDICAMENTO_UPDATE:
                        try {
                            service.updateMedicamento((Medicamento) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;
                    case Protocol.MEDICAMENTO_DELETE:
                        try {
                            String id = is.readUTF();
                            service.deleteMedicamento(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
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
                        }
                        break;

                    case Protocol.MEDICAMENTO_GETALL:
                        try {
                            List<Medicamento> le = service.getMedicamentos();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;

                    //-------------- CASE RECETAS ---------------------

                    case Protocol.RECETA_CREATE:
                        try {
                            Receta receta = (Receta) is.readObject();
                            LocalDate fecha = (LocalDate) is.readObject();
                            service.createReceta(receta, fecha);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) { os.writeInt(Protocol.ERROR_ERROR); }
                        break;
                    case Protocol.RECETA_READ:
                        try {
                            Receta e = service.readReceta((Receta) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(e);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;
                    case Protocol.RECETA_UPDATE:
                        try {
                            service.updateReceta((Receta) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;
                    case Protocol.RECETA_DELETE:
                        try {
                            service.deleteReceta((Receta) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;
                    case Protocol.RECETA_SEARCH:
                        try {
                            List<Receta> le=service.searchRecetasByPaciente((Paciente) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;


                    case Protocol.RECETA_GETALL:
                        try {
                            List<Receta> le = service.getRecetas();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;

                    //-------------- CASE ADMINISTRADOR ---------------------

                    case Protocol.ADMINISTRADOR_CREATE:
                        try {
                            service.createAdministrador((Administrador) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) { os.writeInt(Protocol.ERROR_ERROR); }
                        break;
                    case Protocol.ADMINISTRADOR_READ:
                        try {
                            String id = is.readUTF();
                            Administrador e = service.readAdministrador(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(e);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;
                    case Protocol.ADMINISTRADOR_UPDATE:
                        try {
                            service.updateAdministrador((Administrador) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;
                    case Protocol.ADMINISTRADOR_DELETE:
                        try {
                            String id = is.readUTF();
                            service.deleteAdministrador(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;

                    case Protocol.ADMINISTRADOR_GETALL:
                        try {
                            List<Administrador> le = service.getAllAdministradores();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;

                    //-------------- CASE DETALLE RECETA ---------------------

                    case Protocol.DETALLE_RECETA_CREATE:
                        try {
                            service.createDetalleReceta((DetalleReceta) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;

                    case Protocol.DETALLE_RECETA_UPDATE:
                        try {
                            service.updateDetalleReceta((DetalleReceta) is.readObject());
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
                        }
                        break;

                    case Protocol.DETALLE_RECETA_DELETE:
                        try {
                            int id = is.readInt();
                            service.deleteDetalleReceta(id);
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
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
                        }
                        break;

                    case Protocol.DETALLE_RECETA_GETALL:
                        try {
                            List<DetalleReceta> le = service.getAllDetallesReceta();
                            os.writeInt(Protocol.ERROR_NO_ERROR);
                            os.writeObject(le);
                        } catch (Exception ex) {
                            os.writeInt(Protocol.ERROR_ERROR);
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
                        }
                        break;

                    case Protocol.DISCONNECT:
                        stop();
                        srv.remove(this);
                        break;
                }
                os.flush();
            } catch (IOException e) {
                stop();
            }
        }
    }

}