package hospital.logic;

import hospital.data.*;

import java.time.LocalDate;
import java.util.List;


public class Service {
    private static Service theInstance;

    public static Service instance() {
        if (theInstance == null) theInstance = new Service();
        return theInstance;
    }

    // --- DAO ---
    private final PacienteDao pacienteDao;
    private final MedicoDao medicoDao;
    private final FarmaceutaDao farmaceutaDao;
    private final AdministradorDao administradorDao;
    private final MedicamentoDao medicamentoDao;
    private final RecetaDao recetaDao;
    private final DetalleRecetaDao detalleRecetaDao;

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
        // Database.instance().close();
        System.out.println("Conexión con la base de datos cerrada correctamente.");
    }

    // ========================= PACIENTES ======================

    public void createPaciente(Paciente p) throws Exception {
        try {
            Paciente existente = pacienteDao.read(p.getId());
            if (existente != null) {
                throw new Exception("Paciente ya existe");
            }
        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado")) {
                // Si no existe, podemos crearlo
                pacienteDao.create(p);
                return;
            }
            throw e;
        }
        pacienteDao.create(p);
    }

    public Paciente readPaciente(String id) throws Exception {
        return pacienteDao.read(id);
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
        try {
            Medico existente = medicoDao.read(m.getId());
            if (existente != null) {
                throw new Exception("Medico ya existe");
            }
        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado")) {
                medicoDao.create(m);
                return;
            }
            throw e;
        }
        medicoDao.create(m);
    }

    public Medico readMedico(String id) throws Exception {
        return medicoDao.read(id);
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

    public List<Farmaceuta> searchFarmaceutas(String nombre) throws Exception {
        return farmaceutaDao.searchFarmaceutas(nombre);
    }

    public List<Farmaceuta> getFarmaceutas() throws Exception {
        return farmaceutaDao.findAll();
    }

    // ====================== MEDICAMENTOS ======================

    public void createMedicamento(Medicamento m) throws Exception {
        try {
            Medicamento existente = medicamentoDao.read(m.getCodigo());
            if (existente != null) {
                throw new Exception("Medicamento ya existe");
            }
        } catch (Exception e) {
            if (e.getMessage().contains("no encontrado")) {
                medicamentoDao.create(m);
                return;
            }
            throw e;
        }
        medicamentoDao.create(m);
    }

    public Medicamento readMedicamento(String codigo) throws Exception {
        return medicamentoDao.read(codigo);
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

    public void createReceta(Receta receta, LocalDate fechaRetiro) throws Exception {

        readPaciente(receta.getPacienteId());
        for (DetalleReceta detalle : receta.getDetalles()) {
            readMedicamento(detalle.getMedicamentoCodigo());
        }

        Receta existente = null;
        try {
            existente = recetaDao.read(receta.getId());
        } catch (Exception ignored) {}

        if (existente != null) {
            throw new Exception("Receta con id " + receta.getId() + " ya existe");
        }

        if (receta.getFecha() == null) receta.setFecha(LocalDate.now());
        if (fechaRetiro != null) receta.setFechaRetiro(fechaRetiro);
        else if (receta.getFechaRetiro() == null)
            receta.setFechaRetiro(receta.getFecha().plusDays(1));
            recetaDao.create(receta);

        System.out.println("Receta creada exitosamente con ID: " + receta.getId());
    }

    public Receta readReceta(Receta r) throws Exception {
        return recetaDao.read(r.getId());
    }

    public void updateReceta(Receta r) throws Exception {
        recetaDao.update(r);
    }

    public void deleteReceta(Receta r) throws Exception {
        recetaDao.delete(r.getId());
    }

    public List<Receta> searchRecetasByPaciente(Paciente paciente) throws Exception {
        return recetaDao.filterByPaciente(paciente);
    }

    public List<Receta> getRecetas() throws Exception {
        return recetaDao.getAll();
    }

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

    public List<Administrador> getAdministradores() throws Exception {
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
            if (medico.getClave().equals(clave)) {
                return medico;
            }
        } catch (Exception e) {
            // Continuar buscando en otras tablas
        }

        try {
            Farmaceuta farmaceuta = readFarmaceuta(id);
            if (farmaceuta.getClave().equals(clave)) {
                return farmaceuta;
            }
        } catch (Exception e) {
            // Continuar buscando en otras tablas
        }

        try {
            Administrador admin = readAdministrador(id);
            if (admin.getClave().equals(clave)) {
                return admin;
            }
        } catch (Exception e) {
            // Si llegamos aquí, el usuario no existe
        }

        throw new Exception("Credenciales inválidas");
    }

    public void cambiarClave(String id, String claveActual, String claveNueva) throws Exception {
        if (id == null || id.trim().isEmpty()) {
            throw new Exception("ID requerido");
        }
        if (claveActual == null || claveActual.trim().isEmpty()) {
            throw new Exception("Clave actual requerida");
        }
        if (claveNueva == null || claveNueva.trim().isEmpty()) {
            throw new Exception("Clave nueva requerida");
        }

        try {
            Medico medico = readMedico(id);
            if (!medico.getClave().equals(claveActual)) {
                throw new Exception("Clave actual incorrecta");
            }
            medico.setClave(claveNueva);
            updateMedico(medico);
            return;
        } catch (Exception e) {
            if (!e.getMessage().equals("Clave actual incorrecta")) {
                // Continuar buscando en otras tablas
            } else {
                throw e;
            }
        }

        try {
            Farmaceuta farmaceuta = readFarmaceuta(id);
            if (!farmaceuta.getClave().equals(claveActual)) {
                throw new Exception("Clave actual incorrecta");
            }
            farmaceuta.setClave(claveNueva);
            updateFarmaceuta(farmaceuta);
            return;
        } catch (Exception e) {
            if (!e.getMessage().equals("Clave actual incorrecta")) {
                // Continuar buscando en otras tablas
            } else {
                throw e;
            }
        }

        try {
            Administrador admin = readAdministrador(id);
            if (!admin.getClave().equals(claveActual)) {
                throw new Exception("Clave actual incorrecta");
            }
            admin.setClave(claveNueva);
            updateAdministrador(admin);
            return;
        } catch (Exception e) {
            if (!e.getMessage().equals("Clave actual incorrecta")) {
                throw new Exception("Usuario no encontrado");
            } else {
                throw e;
            }
        }
    }

    // ====================== DETALLES DE RECETA ======================

    public void createDetalleReceta(DetalleReceta detalle) throws Exception {
        detalleRecetaDao.create(detalle);
    }

    public List<DetalleReceta> getDetallesPorReceta(int recetaId) throws Exception {
        return detalleRecetaDao.findByReceta(recetaId);
    }

    public void updateDetalleReceta(DetalleReceta detalle) throws Exception {
        detalleRecetaDao.update(detalle);
    }

    public void deleteDetalleReceta(int id) throws Exception {
        detalleRecetaDao.delete(id);
    }

    public List<DetalleReceta> getAllDetallesReceta() throws Exception {
        return detalleRecetaDao.getAll();
    }

}