package hospital.data;

import hospital.logic.DetalleReceta;
import java.sql.*;
import java.util.*;

public class DetalleRecetaDao {
    private Database db;

    public DetalleRecetaDao() {
        db = Database.instance();
        if (db == null) {
            System.err.println("No se pudo establecer la conexión con la base de datos (DetalleRecetaDao).");
        } else {
            System.out.println("Conexión establecida correctamente con la base de datos (DetalleRecetaDao).");
        }
    }

    public void create(DetalleReceta d) throws Exception {
        String sql = "INSERT INTO DetalleReceta (recetaId, medicamentoCodigo, cantidad, indicaciones) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setInt(1, d.getRecetaId());
            stm.setString(2, d.getMedicamentoCodigo());
            stm.setInt(3, d.getCantidad());
            stm.setString(4, d.getIndicaciones());
            db.executeUpdate(stm);
        } catch (SQLException ex) {
            throw new Exception("Error al crear detalle de receta: " + ex.getMessage(), ex);
        }
    }

    public void create(int recetaId, DetalleReceta d) throws Exception {
        String sql = "INSERT INTO DetalleReceta (recetaId, medicamentoCodigo, cantidad, indicaciones) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setInt(1, recetaId);
            stm.setString(2, d.getMedicamentoCodigo());
            stm.setInt(3, d.getCantidad());
            stm.setString(4, d.getIndicaciones());
            db.executeUpdate(stm);
        } catch (SQLException ex) {
            throw new Exception("Error al crear detalle de receta: " + ex.getMessage(), ex);
        }
    }

    public List<DetalleReceta> findByReceta(int recetaId) throws Exception {
        List<DetalleReceta> lista = new ArrayList<>();
        String sql = "SELECT * FROM DetalleReceta WHERE recetaId=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setInt(1, recetaId);
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                DetalleReceta d = new DetalleReceta(
                        rs.getString("medicamentoCodigo"),
                        rs.getInt("cantidad"),
                        rs.getString("indicaciones")
                );
                lista.add(d);
            }
        } catch (SQLException ex) {
            throw new Exception("Error al buscar detalles de receta: " + ex.getMessage(), ex);
        }
        return lista;
    }

    public void update(DetalleReceta detalle) throws Exception {
        String sql = "UPDATE DetalleReceta SET medicamentoCodigo=?, cantidad=?, indicaciones=? WHERE id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, detalle.getMedicamentoCodigo());
            stm.setInt(2, detalle.getCantidad());
            stm.setString(3, detalle.getIndicaciones());
            stm.setInt(4, detalle.getId());

            int count = db.executeUpdate(stm);
            if (count == 0) {
                throw new Exception("Detalle de receta no existe para actualizar.");
            }
        } catch (SQLException ex) {
            throw new Exception("Error al actualizar detalle de receta: " + ex.getMessage(), ex);
        }
    }

    public void delete(int id) throws Exception {
        String sql = "DELETE FROM DetalleReceta WHERE id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setInt(1, id);
            int count = db.executeUpdate(stm);
            if (count == 0) {
                throw new Exception("No se encontró un detalle de receta con ID: " + id);
            }
        } catch (SQLException ex) {
            throw new Exception("Error al eliminar detalle de receta: " + ex.getMessage(), ex);
        }
    }

    public void deleteByReceta(int recetaId) throws Exception {
        String sql = "DELETE FROM DetalleReceta WHERE recetaId=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setInt(1, recetaId);
            db.executeUpdate(stm);
        } catch (SQLException ex) {
            throw new Exception("Error al eliminar detalles de receta: " + ex.getMessage(), ex);
        }
    }

    public List<DetalleReceta> getAll() throws Exception {
        List<DetalleReceta> lista = new ArrayList<>();
        String sql = "SELECT * FROM DetalleReceta";
        try (PreparedStatement stm = db.prepareStatement(sql);
             ResultSet rs = db.executeQuery(stm)) {
            while (rs.next()) {
                DetalleReceta d = from(rs);
                lista.add(d);
            }
        } catch (SQLException ex) {
            throw new Exception("Error al obtener todos los detalles de receta: " + ex.getMessage(), ex);
        }
        return lista;
    }

    private DetalleReceta from(ResultSet rs) throws Exception {
        try {
            DetalleReceta d = new DetalleReceta();
            d.setId(rs.getInt("id"));
            d.setRecetaId(rs.getInt("recetaId"));
            d.setMedicamentoCodigo(rs.getString("medicamentoCodigo"));
            d.setCantidad(rs.getInt("cantidad"));
            d.setIndicaciones(rs.getString("indicaciones"));
            return d;
        } catch (SQLException ex) {
            throw new Exception("Error al mapear detalle de receta desde ResultSet: " + ex.getMessage(), ex);
        }
    }
}