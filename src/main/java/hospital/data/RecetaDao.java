package hospital.data;

import hospital.logic.Receta;
import hospital.logic.DetalleReceta;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RecetaDao {
    private Database db;

    public RecetaDao() {
        db = Database.instance();
        if (db == null) {
            System.err.println("No se pudo establecer la conexión con la base de datos (RecetaDao).");
        } else {
            System.out.println("Conexión establecida correctamente con la base de datos (RecetaDao).");
        }
    }

    public void create(Receta receta) throws Exception {
        String sql = "INSERT INTO Receta (id, pacienteId, farmaceutaId, estadoReceta, fecha, fechaRetiro) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, receta.getId());
            stmt.setString(2, receta.getPacienteId());
            stmt.setString(3, receta.getFarmaceutaId());
            stmt.setString(4, receta.getEstadoReceta());
            stmt.setDate(5, Date.valueOf(receta.getFecha()));
            if (receta.getFechaRetiro() != null) {
                stmt.setDate(6, Date.valueOf(receta.getFechaRetiro()));
            } else {
                stmt.setNull(6, Types.DATE);
            }

            db.executeUpdate(stmt);

            // Crear los detalles de la receta
            if (receta.getDetalles() != null && !receta.getDetalles().isEmpty()) {
                DetalleRecetaDao detalleDao = new DetalleRecetaDao();
                for (DetalleReceta detalle : receta.getDetalles()) {
                    detalleDao.create(receta.getId(), detalle);
                }
            }
        } catch (SQLException ex) {
            throw new Exception("Error al crear receta: " + ex.getMessage(), ex);
        }
    }

    public Receta read(String id) throws Exception {
        String sql = "SELECT * FROM Receta WHERE id=?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = db.executeQuery(stmt);
            if (rs.next()) {
                Receta r = from(rs);
                // Cargar los detalles de la receta
                DetalleRecetaDao ddao = new DetalleRecetaDao();
                r.setDetalles(ddao.findByReceta(id));
                return r;
            } else {
                throw new Exception("Receta no encontrada con ID: " + id);
            }
        } catch (SQLException ex) {
            throw new Exception("Error al leer receta: " + ex.getMessage(), ex);
        }
    }

    public void update(Receta receta) throws Exception {
        String sql = "UPDATE Receta SET pacienteId=?, farmaceutaId=?, estadoReceta=?, fecha=?, fechaRetiro=? WHERE id=?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, receta.getPacienteId());
            stmt.setString(2, receta.getFarmaceutaId());
            stmt.setString(3, receta.getEstadoReceta());
            stmt.setDate(4, Date.valueOf(receta.getFecha()));
            if (receta.getFechaRetiro() != null) {
                stmt.setDate(5, Date.valueOf(receta.getFechaRetiro()));
            } else {
                stmt.setNull(5, Types.DATE);
            }
            stmt.setString(6, receta.getId());

            int count = stmt.executeUpdate();
            if (count == 0) {
                throw new Exception("Receta no existe para actualizar.");
            }
        } catch (SQLException ex) {
            throw new Exception("Error al actualizar receta: " + ex.getMessage(), ex);
        }
    }

    public void delete(String id) throws Exception {
        try {
            // Primero eliminar detalles
            DetalleRecetaDao detalleDao = new DetalleRecetaDao();
            detalleDao.deleteByReceta(id);

            // Luego eliminar receta
            String sql = "DELETE FROM Receta WHERE id=?";
            try (PreparedStatement stmt = db.prepareStatement(sql)) {
                stmt.setString(1, id);
                int count = db.executeUpdate(stmt);
                if (count == 0) {
                    throw new Exception("No se encontró una receta con ID: " + id);
                }
            }
        } catch (SQLException ex) {
            throw new Exception("Error al eliminar receta: " + ex.getMessage(), ex);
        }
    }

    public List<Receta> getAll() throws Exception {
        List<Receta> lista = new ArrayList<>();
        String sql = "SELECT * FROM Receta ORDER BY fecha DESC";
        try (PreparedStatement stmt = db.prepareStatement(sql);
             ResultSet rs = db.executeQuery(stmt)) {
            while (rs.next()) {
                Receta r = from(rs);
                // Cargar detalles para cada receta
                DetalleRecetaDao ddao = new DetalleRecetaDao();
                r.setDetalles(ddao.findByReceta(r.getId()));
                lista.add(r);
            }
        } catch (SQLException ex) {
            throw new Exception("Error al obtener todas las recetas: " + ex.getMessage(), ex);
        }
        return lista;
    }

    public List<Receta> filterByPaciente(String pacienteId) throws Exception {
        List<Receta> lista = new ArrayList<>();
        String sql = "SELECT * FROM Receta WHERE pacienteId=? ORDER BY fecha DESC";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, pacienteId);
            ResultSet rs = db.executeQuery(stmt);
            while (rs.next()) {
                Receta r = from(rs);
                // Cargar detalles para cada receta
                DetalleRecetaDao ddao = new DetalleRecetaDao();
                r.setDetalles(ddao.findByReceta(r.getId()));
                lista.add(r);
            }
        } catch (SQLException ex) {
            throw new Exception("Error al filtrar recetas por paciente: " + ex.getMessage(), ex);
        }
        return lista;
    }

    private Receta from(ResultSet rs) throws Exception {
        try {
            Receta r = new Receta();
            r.setId(rs.getString("id"));
            r.setPacienteId(rs.getString("pacienteId"));
            r.setFarmaceutaId(rs.getString("farmaceutaId"));
            r.setEstadoReceta(rs.getString("estadoReceta"));

            Date fecha = rs.getDate("fecha");
            if (fecha != null) {
                r.setFecha(fecha.toLocalDate());
            }

            Date fechaRetiro = rs.getDate("fechaRetiro");
            if (fechaRetiro != null) {
                r.setFechaRetiro(fechaRetiro.toLocalDate());
            }

            return r;
        } catch (SQLException ex) {
            throw new Exception("Error al mapear receta desde ResultSet: " + ex.getMessage(), ex);
        }
    }
}