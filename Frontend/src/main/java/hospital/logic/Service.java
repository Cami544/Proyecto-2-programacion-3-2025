package hospital.logic;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.util.List;

public class Service{
    private static Service theInstance;
    public static Service instance(){
        if (theInstance == null) theInstance = new Service();
        return theInstance;
    }
    Socket s;
    ObjectOutputStream os;
    ObjectInputStream is;

    String sid; // Session Id

    public Service() {
        try {
            s = new Socket(Protocol.SERVER, Protocol.PORT);
            os = new ObjectOutputStream(s.getOutputStream());
            is = new ObjectInputStream(s.getInputStream());

            os.writeInt(Protocol.SYNC);
            os.flush();
            sid = (String) is.readObject();
        } catch (Exception e) {System.exit(-1);}
    }

    public String getSid() {
        return sid;
    }

    // ========================= PACIENTES ======================

    public void createPaciente(Paciente e) throws Exception {
        os.writeInt(Protocol.PACIENTE_CREATE);
        os.writeObject(e);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {
            System.out.println("Paciente creado exitosamente");
        }
        else throw new Exception("PACIENTE DUPLICADO");
    }

    public Paciente readPaciente(String id) throws Exception {
        os.writeInt(Protocol.PACIENTE_READ);
        os.writeUTF(id);
        os.flush();

        int errorCode = is.readInt();
        if (errorCode == Protocol.ERROR_NO_ERROR){
            return (Paciente) is.readObject();
        }
        else throw new Exception("PACIENTE NO EXISTE");
    }

    public void updatePaciente(Paciente e) throws Exception {
        os.writeInt(Protocol.PACIENTE_UPDATE);
        os.writeObject(e);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("PACIENTE NO EXISTE");
    }

    public void deletePaciente(String id) throws Exception {
        os.writeInt(Protocol.PACIENTE_DELETE);
        os.writeUTF(id);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("PACIENTE NO EXISTE");
    }

    public List<Paciente> searchPacientes(String nombre) {
        try {
            os.writeInt(Protocol.PACIENTE_SEARCH);
            os.writeUTF(nombre);
            os.flush();
            if (is.readInt() == Protocol.ERROR_NO_ERROR) {
                return (List<Paciente>) is.readObject();
            }
            else return List.of();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Paciente> getPacientes(){
        try {
            os.writeInt(Protocol.PACIENTE_GETALL);
            os.flush();
            if (is.readInt() == Protocol.ERROR_NO_ERROR) {
                return (List<Paciente>) is.readObject();
            }
            else return List.of();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // ========================= MEDICOS ========================

    public void createMedico(Medico e) throws Exception {
        os.writeInt(Protocol.MEDICO_CREATE);
        os.flush();
        os.writeObject(e);
        os.flush();
        System.out.println("Medico en frontend antes de enviar: " + e.getNombre() + e.getId());
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("MEDICO DUPLICADO");
    }

    public Medico readMedico(String id) throws Exception {
        os.writeInt(Protocol.MEDICO_READ);
        os.writeUTF(id);
        os.flush();
        int errorCode = is.readInt();

        if (errorCode == Protocol.ERROR_NO_ERROR){
            return (Medico) is.readObject();
        }
        else throw new Exception("MEDICO NO EXISTE");
    }

    public void updateMedico(Medico e) throws Exception {
        os.writeInt(Protocol.MEDICO_UPDATE);
        os.writeObject(e);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("MEDICO NO EXISTE");
    }

    public void deleteMedico(String id) throws Exception {
        os.writeInt(Protocol.MEDICO_DELETE);
        os.writeUTF(id);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("MEDICO NO EXISTE");
    }

    public List<Medico> searchMedicos(String nombre) {
        try {
            os.writeInt(Protocol.MEDICO_SEARCH);
            os.writeUTF(nombre);
            os.flush();
            if (is.readInt() == Protocol.ERROR_NO_ERROR) {
                return (List<Medico>) is.readObject();
            }
            else return List.of();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    public List<Medico> getMedicos(){
        try {
            os.writeInt(Protocol.MEDICO_GETALL);
            os.flush();
            if (is.readInt() == Protocol.ERROR_NO_ERROR) {
                return (List<Medico>) is.readObject();
            }
            else return List.of();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // ====================== FARMACEUTAS =======================

    public void createFarmaceuta(Farmaceuta e) throws Exception {
        os.writeInt(Protocol.FARMACEUTA_CREATE);
        os.writeObject(e);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("FARMACEUTA DUPLICADO");
    }

    public Farmaceuta readFarmaceuta(String id) throws Exception {
        os.writeInt(Protocol.FARMACEUTA_READ);
        os.writeUTF(id);
        os.flush();
        int errorCode = is.readInt();

        if (errorCode == Protocol.ERROR_NO_ERROR){
            return (Farmaceuta) is.readObject();
        }
        else throw new Exception("FARMACEUTA NO EXISTE");
    }
/*
    public void updateFarmaceuta(Farmaceuta e) throws Exception {
        os.writeInt(Protocol.FARMACEUTA_UPDATE);
        os.writeObject(e);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("FARMACEUTA NO EXISTE");
    }
    */
    public void updateFarmaceuta(Farmaceuta f) throws Exception {
        System.out.println("[Service] Enviando FARMACEUTA_UPDATE...");
        os.writeInt(Protocol.FARMACEUTA_UPDATE);
        os.writeObject(f);
        os.flush();
        System.out.println("[Service] Farmaceuta enviado id=" + f.getId() + ", nombre=" + f.getNombre());

        int response = is.readInt();
        System.out.println("[Service] Respuesta backend: " + response);
        if (response != Protocol.ERROR_NO_ERROR)
            throw new Exception("Error actualizando farmaceuta");
    }

    public void deleteFarmaceuta(String id) throws Exception {
        os.writeInt(Protocol.FARMACEUTA_DELETE);
        os.writeUTF(id);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("FARMACEUTA NO EXISTE");
    }

    public List<Farmaceuta> searchFarmaceutas(String nombre) {
        try {
            os.writeInt(Protocol.FARMACEUTA_SEARCH);
            os.writeUTF(nombre);
            os.flush();
            if (is.readInt() == Protocol.ERROR_NO_ERROR) {
                return (List<Farmaceuta>) is.readObject();
            }
            else return List.of();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Farmaceuta> getFarmaceutas(){
        try {
            os.writeInt(Protocol.FARMACEUTA_GETALL);
            os.flush();
            if (is.readInt() == Protocol.ERROR_NO_ERROR) {
                return (List<Farmaceuta>) is.readObject();
            }
            else return List.of();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // ====================== MEDICAMENTOS =======================

    public void createMedicamento(Medicamento e) throws Exception {
        os.writeInt(Protocol.MEDICAMENTO_CREATE);
        os.writeObject(e);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("MEDICAMENTO DUPLICADO");
    }

    public Medicamento readMedicamento(String id) throws Exception {
        os.writeInt(Protocol.MEDICAMENTO_READ);
        os.writeUTF(id);
        os.flush();
        int errorCode = is.readInt();

        if (errorCode == Protocol.ERROR_NO_ERROR){
            return (Medicamento) is.readObject();
        }
        else throw new Exception("MEDICAMENTO NO EXISTE");
    }

    public void updateMedicamento(Medicamento e) throws Exception {
        os.writeInt(Protocol.MEDICAMENTO_UPDATE);
        os.writeObject(e);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("MEDICAMENTO NO EXISTE");
    }

    public void deleteMedicamento(String id) throws Exception {
        os.writeInt(Protocol.MEDICAMENTO_DELETE);
        os.writeUTF(id);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("MEDICAMENTO NO EXISTE");
    }

    public List<Medicamento> searchMedicamentos(String nombre) {
        try {
            os.writeInt(Protocol.MEDICAMENTO_SEARCH);
            os.writeUTF(nombre);
            os.flush();
            if (is.readInt() == Protocol.ERROR_NO_ERROR) {
                return (List<Medicamento>) is.readObject();
            }
            else return List.of();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Medicamento> getMedicamentos(){
        try {
            os.writeInt(Protocol.MEDICAMENTO_GETALL);
            os.flush();
            if (is.readInt() == Protocol.ERROR_NO_ERROR) {
                return (List<Medicamento>) is.readObject();
            }
            else return List.of();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    // ====================== RECETAS =======================

    public void createReceta(Receta receta, LocalDate fechaRetiro) throws Exception {
        os.writeInt(Protocol.RECETA_CREATE);
        os.writeObject(receta);
        os.writeObject(fechaRetiro);
        os.flush();
        System.out.println("Paciente en frontend antes de enviar: " + receta.getPacienteId());
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("RECETA DUPLICADO");
    }

    public Receta readReceta(String id) throws Exception {
        os.writeInt(Protocol.RECETA_READ);
        os.writeUTF(id);
        os.flush();
        int errorCode = is.readInt();

        if (errorCode == Protocol.ERROR_NO_ERROR){
            return (Receta) is.readObject();
        }
        else throw new Exception("RECETA NO EXISTE");
    }

public void updateReceta(Receta r) throws Exception {
    System.out.println("[Service] Enviando RECETA_UPDATE...");
    os.writeInt(Protocol.RECETA_UPDATE);
    os.writeObject(r);
    os.flush();
    System.out.println("[Service] Receta enviada id=" + r.getId() + ", estado=" + r.getEstadoReceta());

    if (is.readInt() != Protocol.ERROR_NO_ERROR)
        throw new Exception("Error actualizando detalle receta");

}


    public void deleteReceta(String id) throws Exception {
        os.writeInt(Protocol.RECETA_DELETE);
        os.writeUTF(id);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("RECETA NO EXISTE");
    }

    public List<Receta> searchRecetasByPaciente(String nombre) {
        try {
            os.writeInt(Protocol.RECETA_SEARCH);
            os.writeUTF(nombre);
            os.flush();
            if (is.readInt() == Protocol.ERROR_NO_ERROR) {
                return (List<Receta>) is.readObject();
            }
            else return List.of();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Receta> getRecetas()  throws Exception{
        try {
            os.writeInt(Protocol.RECETA_GETALL);
            os.flush();
            if (is.readInt() == Protocol.ERROR_NO_ERROR) {
                return (List<Receta>) is.readObject();
            }
            else throw new Exception("Error al obtener recetas");
        } catch (Exception ex) {
            throw new Exception("Error al obtener recetas");
        }
    }

    // ====================== ADMINISTRADORES ======================

    public void createAdministrador(Administrador e) throws Exception {
        os.writeInt(Protocol.ADMINISTRADOR_CREATE);
        os.writeObject(e);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {
        } else throw new Exception("ADMINISTRADOR DUPLICADO");
    }

    public Administrador readAdministrador(String id) throws Exception {
        os.writeInt(Protocol.ADMINISTRADOR_READ);
        os.writeUTF(id);
        os.flush();
        int errorCode = is.readInt();

        if (errorCode == Protocol.ERROR_NO_ERROR){
            return (Administrador) is.readObject();
        }
        else throw new Exception("ADMINISTRADOR NO EXISTE");
    }

    public void updateAdministrador(Administrador e) throws Exception {
        os.writeInt(Protocol.ADMINISTRADOR_UPDATE);
        os.writeObject(e);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("ADMINISTRADOR NO EXISTE");
    }

    public void deleteAdministrador(String id) throws Exception {
        os.writeInt(Protocol.ADMINISTRADOR_DELETE);
        os.writeUTF(id);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("ADMINISTRADOR NO EXISTE");
    }

    public List<Administrador> searchAdministrador(String nombre) {
        try {
            os.writeInt(Protocol.ADMINISTRADOR_GETALL);
            os.writeObject(nombre);
            os.flush();
            if (is.readInt() == Protocol.ERROR_NO_ERROR) {
                return (List<Administrador>) is.readObject();
            }
            else return List.of();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Administrador> getAdministradores(){
        return List.of();
    }

    // ====================== MÉTODOS DE AUTENTICACIÓN ======================

    public Usuario authenticate(String id, String clave) throws Exception {
        os.writeInt(Protocol.AUTHENTICATE);
        os.writeUTF(id);
        os.writeUTF(clave);
        os.flush();
        int errorCode = is.readInt();

        if (errorCode == Protocol.ERROR_NO_ERROR){
            return (Usuario) is.readObject();
        }
        else throw new Exception("CREDENCIALES INVÁLIDAS");
    }

    public void cambiarClave(String id, String claveActual, String claveNueva) throws Exception {
        os.writeInt(Protocol.CHANGE_PASSWORD);
        os.writeUTF(id);
        os.writeUTF(claveActual);
        os.writeUTF(claveNueva);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {
        } else throw new Exception("USUARIO NO EXISTE");
    }

    // ====================== DETALLES DE RECETA ======================

    public void createDetalleReceta(DetalleReceta e) throws Exception {
        os.writeInt(Protocol.DETALLE_RECETA_CREATE);
        os.writeObject(e);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {
        } else throw new Exception("ADMINISTRADOR DUPLICADO");
    }

/*
    public void updateDetalleReceta(DetalleReceta e) throws Exception {
        os.writeInt(Protocol.DETALLE_RECETA_UPDATE);
        os.writeObject(e);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("UPDATE DETALLE RECETA NO EXISTE");
    }
*/
public void updateDetalleReceta(DetalleReceta d) throws Exception {
    System.out.println("[Service] Enviando DETALLERECETA_UPDATE...");
    os.writeInt(Protocol.DETALLE_RECETA_UPDATE);
    os.writeObject(d);
    os.flush();
    System.out.println("[Service] Detalle enviado id=" + d.getId() + ", recetaId=" + d.getRecetaId());

    int response = is.readInt();
    System.out.println("[Service] Respuesta backend: " + response);
    if (response != 200) throw new Exception("Error actualizando detalle receta");
}

    public void deleteDetalleReceta(int id) throws Exception {
        os.writeInt(Protocol.DETALLE_RECETA_DELETE);
        os.writeInt(id);
        os.flush();
        if (is.readInt() == Protocol.ERROR_NO_ERROR) {}
        else throw new Exception("DETALLE RECETA NO EXISTE");
    }

    public List<DetalleReceta> getDetallesPorReceta(int recetaId) {
        try {
            os.writeInt(Protocol.DETALLE_RECETA_GETXRECETA);
            os.writeInt(recetaId);
            os.flush();
            if (is.readInt() == Protocol.ERROR_NO_ERROR) {
                return (List<DetalleReceta>) is.readObject();
            }
            else return List.of();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<DetalleReceta> getAllDetallesReceta(){

        return List.of();
    }

    private void disconnect() throws Exception {
        os.writeInt(Protocol.DISCONNECT);
        os.flush();
        s.shutdownOutput();
        s.close();
    }

    public void enviarMensaje(String mensaje) throws Exception {
        os.writeInt(Protocol.DELIVER_MESSAGE);
        os.writeObject(mensaje);
        os.flush();
        System.out.println("Mensaje enviado: " + mensaje);
    }

    public void stop() {
        try {
            disconnect();
        } catch (Exception e) {
            System.exit(-1);
        }
    }
}