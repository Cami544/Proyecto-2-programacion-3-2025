package hospital.data;

import hospital.logic.Medico;
import hospital.logic.Receta;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicoDao {
    private Database db;

    public MedicoDao() {
        db = Database.instance();
        if (db == null) {
            System.err.println("No se pudo establecer la conexión con la base de datos (MedicoDao).");
        } else {
            System.out.println("Conexión establecida correctamente con la base de datos (MedicoDao).");
        }
    }

    // Crear nuevo médico
    public void create(Medico m) throws Exception {
        String sql = "INSERT INTO Medico (id, nombre, clave, especialidad, rol) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            if (m.getId() == null || m.getNombre() == null || m.getClave() == null || m.getEspecialidad() == null) {
                throw new Exception("Datos del médico incompletos.");
            }

            stmt.setString(1, m.getId());
            stmt.setString(2, m.getNombre());
            stmt.setString(3, m.getClave());
            stmt.setString(4, m.getEspecialidad());
            stmt.setString(5, m.getRol());
            db.executeUpdate(stmt);
        } catch (SQLException ex) {
            throw new Exception("Error al crear médico: " + ex.getMessage(), ex);
        }
    }

    // Leer médico por ID
    public Medico read(String id) throws Exception {
        String sql = "SELECT * FROM Medico WHERE id = ?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return from(rs);
                } else {
                    throw new Exception("Médico no encontrado con ID: " + id);
                }
            }
        }
    }

    // Actualizar médico
    public void update(Medico m) throws Exception {
        String sql = "UPDATE Medico SET nombre = ?, clave = ?, especialidad = ?, rol = ? WHERE id = ?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, m.getNombre());
            stmt.setString(2, m.getClave());
            stmt.setString(3, m.getEspecialidad());
            stmt.setString(4, m.getRol());
            stmt.setString(5, m.getId());

            int count = stmt.executeUpdate();
            if (count == 0) {
                throw new Exception("Médico no existe para actualizar.");
            }
        }
    }

    public void delete(String id) throws Exception {
        // Eliminar recetas asociadas (si aplica)
        RecetaDao recetaDao = new RecetaDao();
        List<Receta> recetas = recetaDao.getAll(); // o filterByMedico(id)
        for (Receta r : recetas) {
            if (r.getFarmaceutaId().equals(id)) {
                recetaDao.delete(r.getId());
            }
        }

        // Luego eliminar el médico
        String sql = "DELETE FROM Medico WHERE id=?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, id);
            db.executeUpdate(stmt);
        }
    }


    // Buscar médicos por nombre o especialidad
    public List<Medico> search(String filtro) throws Exception {
        List<Medico> resultado = new ArrayList<>();
        String sql = "SELECT * FROM Medico WHERE nombre LIKE ? OR especialidad LIKE ? ORDER BY nombre";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, "%" + filtro + "%");
            stmt.setString(2, "%" + filtro + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(from(rs));
                }
            }
        }
        return resultado;
    }

    // Listar todos los médicos
    public List<Medico> getAll() throws Exception {
        List<Medico> lista = new ArrayList<>();
        String sql = "SELECT * FROM Medico ORDER BY nombre";
        try (PreparedStatement stmt = db.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(from(rs));
            }
        }
        return lista;
    }

    // Convertir ResultSet en objeto Medico
    private Medico from(ResultSet rs) throws Exception {
        Medico m = new Medico();
        m.setId(rs.getString("id"));
        m.setNombre(rs.getString("nombre"));
        m.setClave(rs.getString("clave"));
        m.setEspecialidad(rs.getString("especialidad"));
        m.setRol(rs.getString("rol"));
        return m;
    }
}
