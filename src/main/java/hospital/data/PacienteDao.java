package hospital.data;

import hospital.logic.Paciente;
import hospital.logic.Receta;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PacienteDao {
    private Database db;

    public PacienteDao() {
        db = Database.instance();
        if (db == null) {
            System.err.println("No se pudo establecer la conexión con la base de datos (PacienteDao).");
        } else {
            System.out.println("Conexión establecida correctamente con la base de datos (PacienteDao).");
        }
    }

    // Crear un nuevo paciente
    public void create(Paciente p) throws Exception {
        String sql = "INSERT INTO Paciente (id, nombre, fechaNacimiento, numeroTelefono) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            if (p.getId() == null || p.getNombre() == null || p.getFechaNacimiento() == null || p.getNumeroTelefono() == null) {
                throw new Exception("Datos del paciente incompletos.");
            }

            stmt.setString(1, p.getId());
            stmt.setString(2, p.getNombre());
            stmt.setDate(3, Date.valueOf(p.getFechaNacimiento()));
            stmt.setString(4, p.getNumeroTelefono());
            db.executeUpdate(stmt);
        } catch (SQLException ex) {
            throw new Exception("Error al crear paciente: " + ex.getMessage(), ex);
        }
    }

    // Leer un paciente por ID
    public Paciente read(String id) throws Exception {
        String sql = "SELECT * FROM Paciente WHERE id = ?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return from(rs);
                } else {
                    throw new Exception("Paciente no encontrado con ID: " + id);
                }
            }
        }
    }

    // Actualizar un paciente existente
    public void update(Paciente p) throws Exception {
        String sql = "UPDATE Paciente SET nombre = ?, fechaNacimiento = ?, numeroTelefono = ? WHERE id = ?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, p.getNombre());
            stmt.setDate(2, Date.valueOf(p.getFechaNacimiento()));
            stmt.setString(3, p.getNumeroTelefono());
            stmt.setString(4, p.getId());

            int count = stmt.executeUpdate();
            if (count == 0) {
                throw new Exception("Paciente no existe para actualizar.");
            }
        }
    }

    public void delete(String id) throws Exception {
        // Eliminar primero las recetas y sus detalles del paciente
        RecetaDao recetaDao = new RecetaDao();
        List<Receta> recetas = recetaDao.filterByPaciente(id);
        for (Receta r : recetas) {
            recetaDao.delete(r.getId());
        }

        // Luego eliminar el paciente
        String sql = "DELETE FROM Paciente WHERE id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, id);
            db.executeUpdate(stm);
        }
    }



    // Buscar pacientes por nombre
    public List<Paciente> search(String filtro) throws Exception {
        List<Paciente> resultado = new ArrayList<>();
        String sql = "SELECT * FROM Paciente WHERE nombre LIKE ? ORDER BY nombre";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, "%" + filtro + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(from(rs));
                }
            }
        }
        return resultado;
    }

    // Listar todos los pacientes
    public List<Paciente> getAll() throws Exception {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM Paciente ORDER BY nombre";
        try (PreparedStatement stmt = db.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(from(rs));
            }
        }
        return lista;
    }

    // Convertir ResultSet en objeto Paciente
    private Paciente from(ResultSet rs) throws Exception {
        Paciente p = new Paciente();
        p.setId(rs.getString("id"));
        p.setNombre(rs.getString("nombre"));
        Date fecha = rs.getDate("fechaNacimiento");
        if (fecha != null) {
            p.setFechaNacimiento(fecha.toLocalDate());
        }
        p.setNumeroTelefono(rs.getString("numeroTelefono"));
        return p;
    }
}


