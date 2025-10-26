package hospital.data;

import hospital.logic.Paciente;
import hospital.logic.Receta;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.*;
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
        } catch (SQLException ex) {
            throw new Exception("Error al leer paciente: " + ex.getMessage(), ex);
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
        } catch (SQLException ex) {
            throw new Exception("Error al actualizar paciente: " + ex.getMessage(), ex);
        }
    }

    // Eliminar un paciente
    public void delete(String id) throws Exception {
        String sql = "DELETE FROM Paciente WHERE id = ?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, id);
            int count = db.executeUpdate(stmt);
            if (count == 0) {
                throw new Exception("No se encontró un paciente con ID: " + id);
            }
        } catch (SQLException ex) {
            throw new Exception("Error al eliminar paciente: " + ex.getMessage(), ex);
        }
    }

    // Buscar pacientes por filtro
    public List<Paciente> search(String filtro) throws Exception {
        List<Paciente> resultado = new ArrayList<>();
        String sql = "SELECT * FROM Paciente WHERE nombre LIKE ? OR numeroTelefono LIKE ? ORDER BY nombre";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, "%" + filtro + "%");
            stmt.setString(2, "%" + filtro + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(from(rs));
                }
            }
        } catch (SQLException ex) {
            throw new Exception("Error al buscar pacientes: " + ex.getMessage(), ex);
        }
        return resultado;
    }

    // Obtener todos los pacientes
    public List<Paciente> getAll() throws Exception {
        List<Paciente> lista = new ArrayList<>();
        String sql = "SELECT * FROM Paciente ORDER BY nombre";
        try (PreparedStatement stmt = db.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(from(rs));
            }
        } catch (SQLException ex) {
            throw new Exception("Error al obtener todos los pacientes: " + ex.getMessage(), ex);
        }
        return lista;
    }

    // Obtener recetas de un paciente
    public List<Receta> getRecetas(String pacienteId) throws Exception {
        List<Receta> lista = new ArrayList<>();
        String sql = "SELECT * FROM Receta WHERE pacienteId = ? ORDER BY fecha DESC";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, pacienteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    // Aquí deberías delegar a RecetaDao o implementar fromReceta
                }
            }
        } catch (SQLException ex) {
            throw new Exception("Error al obtener recetas del paciente: " + ex.getMessage(), ex);
        }
        return lista;
    }

    private Paciente from(ResultSet rs) throws Exception {
        try {
            Paciente p = new Paciente();
            p.setId(rs.getString("id"));
            p.setNombre(rs.getString("nombre"));
            Date fecha = rs.getDate("fechaNacimiento");
            if (fecha != null) {
                p.setFechaNacimiento(fecha.toLocalDate());
            }
            p.setNumeroTelefono(rs.getString("numeroTelefono"));
            return p;
        } catch (SQLException ex) {
            throw new Exception("Error al mapear paciente desde ResultSet: " + ex.getMessage(), ex);
        }
    }
}