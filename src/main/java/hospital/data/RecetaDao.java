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
    }

    public void create(Receta receta) throws Exception {
        String sql = "INSERT INTO Receta (id, pacienteId, farmaceutaId, estadoReceta, fecha, fechaRetiro) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, receta.getId());
            stmt.setString(2, receta.getPacienteId());
            stmt.setString(3, receta.getFarmaceutaId());
            stmt.setString(4, receta.getEstadoReceta());
            stmt.setDate(5, Date.valueOf(receta.getFecha()));
            if (receta.getFechaRetiro() != null)
                stmt.setDate(6, Date.valueOf(receta.getFechaRetiro()));
            else
                stmt.setNull(6, Types.DATE);

            db.executeUpdate(stmt);
        }
    }

    public Receta read(String id) throws Exception {
        String sql = "SELECT * FROM Receta WHERE id=?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = db.executeQuery(stmt);
            if (rs.next()) {
                Receta r = from(rs);
                DetalleRecetaDao ddao = new DetalleRecetaDao();
                r.setDetalles(ddao.findByRecetaId(id));
                return r;
            } else throw new Exception("Receta no encontrada");
        }
    }

    public void update(Receta receta) throws Exception {
        String sql = "UPDATE Receta SET pacienteId=?, farmaceutaId=?, estadoReceta=?, fecha=?, fechaRetiro=? WHERE id=?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, receta.getPacienteId());
            stmt.setString(2, receta.getFarmaceutaId());
            stmt.setString(3, receta.getEstadoReceta());
            stmt.setDate(4, Date.valueOf(receta.getFecha()));
            if (receta.getFechaRetiro() != null)
                stmt.setDate(5, Date.valueOf(receta.getFechaRetiro()));
            else
                stmt.setNull(5, Types.DATE);
            stmt.setString(6, receta.getId());
            db.executeUpdate(stmt);
        }
    }

    public void delete(String id) throws Exception {
        // Primero eliminar detalles
        String sqlDetalles = "DELETE FROM DetalleReceta WHERE recetaId=?";
        try (PreparedStatement stmt = db.prepareStatement(sqlDetalles)) {
            stmt.setString(1, id);
            db.executeUpdate(stmt);
        }

        // Luego eliminar receta
        String sql = "DELETE FROM Receta WHERE id=?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, id);
            db.executeUpdate(stmt);
        }
    }

    public List<Receta> getAll() throws Exception {
        List<Receta> lista = new ArrayList<>();
        String sql = "SELECT * FROM Receta ORDER BY fecha DESC";
        try (PreparedStatement stmt = db.prepareStatement(sql);
             ResultSet rs = db.executeQuery(stmt)) {
            while (rs.next()) {
                lista.add(from(rs));
            }
        }
        return lista;
    }

    public List<Receta> filterByPaciente(String pacienteId) throws Exception {
        List<Receta> lista = new ArrayList<>();
        String sql = "SELECT * FROM Receta WHERE pacienteId=?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, pacienteId);
            ResultSet rs = db.executeQuery(stmt);
            while (rs.next()) lista.add(from(rs));
        }
        return lista;
    }

    private Receta from(ResultSet rs) throws Exception {
        Receta r = new Receta();
        r.setId(rs.getString("id"));
        r.setPacienteId(rs.getString("pacienteId"));
        r.setFarmaceutaId(rs.getString("farmaceutaId"));
        r.setEstadoReceta(rs.getString("estadoReceta"));
        r.setFecha(rs.getDate("fecha").toLocalDate());
        Date fr = rs.getDate("fechaRetiro");
        if (fr != null) r.setFechaRetiro(fr.toLocalDate());
        return r;
    }
}
