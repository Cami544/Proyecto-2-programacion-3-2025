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
        try (PreparedStatement stm = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            System.out.println("[DEBUG] Insertando DetalleReceta -> recetaId: " + d.getRecetaId()
                    + ", medicamentoCodigo: " + d.getMedicamentoCodigo()
                    + ", cantidad: " + d.getCantidad()
                    + ", indicaciones: " + d.getIndicaciones());

            stm.setInt(1, d.getRecetaId());
            stm.setString(2, d.getMedicamentoCodigo());
            stm.setInt(3, d.getCantidad());
            stm.setString(4, d.getIndicaciones());

            int filas = db.executeUpdate(stm);
            if (filas == 0) {
                throw new Exception("No se insertó ningún detalle de receta.");
            }

            try (ResultSet rs = stm.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    d.setId(idGenerado);
                    System.out.println("[DEBUG] ID generado automáticamente para DetalleReceta: " + idGenerado);
                } else {
                    System.err.println("[WARN] No se pudo obtener el ID generado del detalle de receta.");
                }
            }

            System.out.println("[DEBUG] DetalleReceta insertado correctamente en la base de datos.");

        } catch (SQLException ex) {
            System.err.println("[ERROR] Error SQL al crear detalle de receta: " + ex.getMessage());
            ex.printStackTrace();
            throw new Exception("Error al crear detalle de receta: " + ex.getMessage(), ex);
        }
    }


    public void create(int recetaId, DetalleReceta d) throws Exception {
        String sql = "INSERT INTO DetalleReceta (recetaId, medicamentoCodigo, cantidad, indicaciones) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stm = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            System.out.println("[DEBUG] Insertando DetalleReceta (usando recetaId externo) -> recetaId: " + recetaId
                    + ", medicamentoCodigo: " + d.getMedicamentoCodigo()
                    + ", cantidad: " + d.getCantidad()
                    + ", indicaciones: " + d.getIndicaciones());

            stm.setInt(1, recetaId);
            stm.setString(2, d.getMedicamentoCodigo());
            stm.setInt(3, d.getCantidad());
            stm.setString(4, d.getIndicaciones());

            int filas = db.executeUpdate(stm);
            if (filas == 0) {
                throw new Exception("No se insertó ningún detalle de receta.");
            }

            try (ResultSet rs = stm.getGeneratedKeys()) {
                if (rs.next()) {
                    int idGenerado = rs.getInt(1);
                    d.setId(idGenerado);
                    System.out.println("[DEBUG] ID generado automáticamente para DetalleReceta: " + idGenerado);
                } else {
                    System.err.println("[WARN] No se pudo obtener el ID generado del detalle de receta.");
                }
            }

            System.out.println("[DEBUG] DetalleReceta insertado correctamente en la base de datos.");

        } catch (SQLException ex) {
            System.err.println("[ERROR] Error SQL al crear detalle de receta (recetaId externo): " + ex.getMessage());
            ex.printStackTrace();
            throw new Exception("Error al crear detalle de receta: " + ex.getMessage(), ex);
        }
    }

    public List<DetalleReceta> findByReceta(int recetaId) throws Exception {

        System.out.println("[DEBUG] Buscando detalles para recetaId: " + recetaId);

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

                System.out.println("[DEBUG] -> Detalle encontrado: medicamento=" + rs.getString("medicamentoCodigo") +
                        ", cantidad=" + rs.getInt("cantidad") +
                        ", indicaciones=" + rs.getString("indicaciones"));


            }
        } catch (SQLException ex) {
            throw new Exception("Error al buscar detalles de receta: " + ex.getMessage(), ex);
        }
        System.out.println("[DEBUG] Total detalles encontrados para recetaId " + recetaId + ": " + lista.size());

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

            System.out.println("[DEBUG] Mapeando detalle -> id: " + d.getId() +
                    ", recetaId: " + d.getRecetaId() +
                    ", medicamento: " + d.getMedicamentoCodigo() +
                    ", cantidad: " + d.getCantidad());

            return d;
        } catch (SQLException ex) {
            throw new Exception("Error al mapear detalle de receta desde ResultSet: " + ex.getMessage(), ex);
        }
    }
}