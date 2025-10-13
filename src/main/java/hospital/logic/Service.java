package hospital.logic;

import hospital.data.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import java.util.Comparator;

public class Service {
    private static Service theInstance;

    public static Service instance() {
        if (theInstance == null) theInstance = new Service();
        return theInstance;
    }

    // --- DAO ---
    private PacienteDao pacienteDao;
    private MedicoDao medicoDao;
    private FarmaceutaDao farmaceutaDao;
    private AdministradorDao administradorDao;
    private MedicamentoDao medicamentoDao;
    private RecetaDao recetaDao;
    private DetalleRecetaDao detalleRecetaDao;

    private Service() {
        pacienteDao = new PacienteDao();
        medicoDao = new MedicoDao();
        farmaceutaDao = new FarmaceutaDao();
        administradorDao = new AdministradorDao();
        medicamentoDao = new MedicamentoDao();
        recetaDao = new RecetaDao();
        detalleRecetaDao = new DetalleRecetaDao();
        System.out.println("Servicio conectado a la base de datos (modo SQL)");
    }

    public void stop() {
     //   Database.instance().close();
        System.out.println("Conexión con la base de datos cerrada correctamente.");
    }


    // ========================= PACIENTES ======================

    public void createPaciente(Paciente p) throws Exception {
        if (pacienteDao.read(p.getId()) != null)
            throw new Exception("Paciente ya existe");
        pacienteDao.create(p);
    }

    public Paciente readPaciente(String id) throws Exception {
        Paciente p = pacienteDao.read(id);
        if (p == null) throw new Exception("Paciente no existe");
        return p;
    }

    public void updatePaciente(Paciente p) throws Exception {
        pacienteDao.update(p);
    }

    public void deletePaciente(String id) throws Exception {
        pacienteDao.delete(id);
    }

    public List<Paciente> searchPacientes(String nombre) throws Exception {
        return pacienteDao.search(nombre);
    }

    public List<Paciente> getPacientes() throws Exception {
        return pacienteDao.getAll();
    }

    // ========================= MEDICOS ========================

    public void createMedico(Medico m) throws Exception {
        if (medicoDao.read(m.getId()) != null)
            throw new Exception("Medico ya existe");
        medicoDao.create(m);
    }

    public Medico readMedico(String id) throws Exception {
        Medico m = medicoDao.read(id);
        if (m == null) throw new Exception("Medico no existe");
        return m;
    }

    public void updateMedico(Medico m) throws Exception {
        medicoDao.update(m);
    }

    public void deleteMedico(String id) throws Exception {
        medicoDao.delete(id);
    }

    public List<Medico> searchMedicos(String nombre) throws Exception {
        return medicoDao.search(nombre);
    }

    public List<Medico> getMedicos() throws Exception {
        return medicoDao.getAll();
    }

    // ====================== FARMACEUTAS =======================

    public void createFarmaceuta(Farmaceuta f) throws Exception {
        farmaceutaDao.create(f);
    }

    public Farmaceuta readFarmaceuta(String id) throws Exception {
        return farmaceutaDao.read(id);
    }

    public void updateFarmaceuta(Farmaceuta f) throws Exception {
        farmaceutaDao.update(f);
    }

    public void deleteFarmaceuta(String id) throws Exception {
        farmaceutaDao.delete(id);
    }

    public List<Farmaceuta> searchFarmaceutas(String nombre) {
        return farmaceutaDao.searchFarmaceutas( nombre);
    }

    public List<Farmaceuta> getFarmaceutas() {
        return farmaceutaDao.findAll();
    }

    // ====================== MEDICAMENTOS ======================

    public void createMedicamento(Medicamento m) throws Exception {
        if (medicamentoDao.read(m.getCodigo()) != null)
            throw new Exception("Medicamento ya existe");
        medicamentoDao.create(m);
    }

    public Medicamento readMedicamento(String codigo) throws Exception {
        Medicamento m = medicamentoDao.read(codigo);
        if (m == null) throw new Exception("Medicamento no existe");
        return m;
    }

    public void updateMedicamento(Medicamento m) throws Exception {
        medicamentoDao.update(m);
    }

    public void deleteMedicamento(String codigo) throws Exception {
        medicamentoDao.delete(codigo);
    }

    public List<Medicamento> searchMedicamentos(String nombre) throws Exception {
        return medicamentoDao.search(nombre);
    }


    public List<Medicamento> getMedicamentos() throws Exception {
        return medicamentoDao.getAll();
    }

    // ========================= RECETAS ========================

    public void createReceta(Receta receta) throws Exception {
        if (recetaDao.read(receta.getId()) != null)
            throw new Exception("Receta ya existe");
        recetaDao.create(receta);
    }

    public Receta readReceta(String id) throws Exception {
        Receta r = recetaDao.read(id);
        if (r == null) throw new Exception("Receta no existe");
        return r;
    }

    public void updateReceta(Receta r) throws Exception {
        recetaDao.update(r);
    }

    public void deleteReceta(String id) throws Exception {
        recetaDao.delete(id);
    }

    public List<Receta> searchRecetasByPaciente(String pacienteId) throws Exception {
        return recetaDao.filterByPaciente( pacienteId);
    }

    public List<Receta> getRecetas() throws Exception {
        return recetaDao.getAll();
    }
/*
    public void clearAllData() {
        data.getPacientes().clear();
        data.getMedicos().clear();
        data.getFarmaceutas().clear();
        data.getMedicamentos().clear();
        data.getRecetas().clear();

        stop();

        System.out.println("Todos los datos han sido eliminados");
    }*/

    // ====================== ADMINISTRADORES ======================

    public void createAdministrador(Administrador a) throws Exception {
        administradorDao.create(a);
    }

    public Administrador readAdministrador(String id) throws Exception {
        return administradorDao.read(id);
    }

    public void updateAdministrador(Administrador a) throws Exception {
        administradorDao.update(a);
    }

    public void deleteAdministrador(String id) throws Exception {
        administradorDao.delete(id);
    }

    public List<Administrador> getAllAdministradores() throws Exception {
        return administradorDao.findAll();
    }
    public List<Administrador> getAdministradores() {
        return administradorDao.findAll();
    }

// ====================== MÉTODOS DE AUTENTICACIÓN ======================

    public Usuario authenticate(String id, String clave) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new Exception("ID requerido");
        }
        if (clave == null || clave.trim().isEmpty()) {
            throw new Exception("Clave requerida");
        }

        try {
            Medico medico = readMedico(id);
            if (medico.getClave() != null && medico.getClave().equals(clave)) {
                return medico;
            }
        } catch (Exception e) {
        }

        try {
            Farmaceuta farmaceuta = readFarmaceuta(id);
            if (farmaceuta.getClave() != null && farmaceuta.getClave().equals(clave)) {
                return farmaceuta;
            }
        } catch (Exception e) {
        }

        try {
            Administrador admin = readAdministrador(id);
            if (admin.getClave() != null && admin.getClave().equals(clave)) {
                return admin;
            }
        } catch (Exception e) {
        }

        throw new Exception("Usuario o clave incorrectos");
    }





    public void cambiarClave(String id, String claveActual, String claveNueva) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new Exception("ID requerido");
        }
        if (claveActual == null || claveActual.trim().isEmpty()) {
            throw new Exception("Contraseña actual requerida");
        }
        if (claveNueva == null || claveNueva.trim().isEmpty()) {
            throw new Exception("Nueva contraseña requerida");
        }

        Usuario usuario = authenticate(id, claveActual);

        if (usuario instanceof Medico) {
            Medico medico = (Medico) usuario;
            medico.setClave(claveNueva);
            updateMedico(medico);
        } else if (usuario instanceof Farmaceuta) {
            Farmaceuta farmaceuta = (Farmaceuta) usuario;
            farmaceuta.setClave(claveNueva);
            updateFarmaceuta(farmaceuta);
        } else if (usuario instanceof Administrador) {
            Administrador admin = (Administrador) usuario;
            admin.setClave(claveNueva);
            updateAdministrador(admin);
        } else {
            throw new Exception("Tipo de usuario no valido para cambio de contraseña");
        }
    }
}