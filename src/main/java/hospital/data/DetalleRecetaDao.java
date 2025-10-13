package hospital.data;

import hospital.logic.DetalleReceta;
import java.sql.*;
import java.util.*;

public class DetalleRecetaDao {
    private Database db;

    public DetalleRecetaDao() {
        db = Database.instance();
    }

    public void create(String recetaId, DetalleReceta d) throws Exception {
        String sql = "INSERT INTO DetalleReceta (recetaId, medicamentoCodigo, cantidad, indicaciones) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, recetaId);
            stm.setString(2, d.getMedicamentoCodigo());
            stm.setInt(3, d.getCantidad());
            stm.setString(4, d.getIndicaciones());
            db.executeUpdate(stm);
        }
    }

    public List<DetalleReceta> findByRecetaId(String recetaId) throws Exception {
        List<DetalleReceta> lista = new ArrayList<>();
        String sql = "SELECT * FROM DetalleReceta WHERE recetaId=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, recetaId);
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                DetalleReceta d = new DetalleReceta(
                        rs.getString("medicamentoCodigo"),
                        rs.getInt("cantidad"),
                        rs.getString("indicaciones")
                );
                lista.add(d);
            }
        }
        return lista;
    }

    public void deleteByRecetaId(String recetaId) throws Exception {
        String sql = "DELETE FROM DetalleReceta WHERE recetaId=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, recetaId);
            db.executeUpdate(stm);
        }
    }
}

