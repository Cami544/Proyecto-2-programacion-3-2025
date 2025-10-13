package hospital.data;

import hospital.logic.Farmaceuta;
import java.sql.*;
import java.util.*;

public class FarmaceutaDao {
    Database db;

    public FarmaceutaDao() {
        db = Database.instance();
    }

    public void create(Farmaceuta f) throws Exception {
        String sql = "INSERT INTO Farmaceuta (id, nombre, clave, rol) VALUES (?, ?, ?, ?)";
        PreparedStatement stm = db.prepareStatement(sql);
        stm.setString(1, f.getId());
        stm.setString(2, f.getNombre());
        stm.setString(3, f.getClave());
        stm.setString(4, f.getRol());
        db.executeUpdate(stm);
    }

    public Farmaceuta read(String id) throws Exception {
        String sql = "SELECT * FROM Farmaceuta f WHERE f.id=?";
        PreparedStatement stm = db.prepareStatement(sql);
        stm.setString(1, id);
        ResultSet rs = db.executeQuery(stm);
        if (rs.next()) return from(rs, "f");
        else throw new Exception("Farmaceuta no existe");
    }

    public void update(Farmaceuta f) throws Exception {
        String sql = "UPDATE Farmaceuta SET nombre=?, clave=?, rol=? WHERE id=?";
        PreparedStatement stm = db.prepareStatement(sql);
        stm.setString(1, f.getNombre());
        stm.setString(2, f.getClave());
        stm.setString(3, f.getRol());
        stm.setString(4, f.getId());
        db.executeUpdate(stm);
    }

    public void delete(String cedula) throws Exception {
        String sql = "DELETE FROM Farmaceuta WHERE cedula = ?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, cedula);
            int count = db.executeUpdate(stm);
            if (count == 0) throw new Exception("No se encontró un farmaceuta con cédula: " + cedula);
        } catch (SQLException ex) {
            throw new Exception("Error al eliminar farmaceuta: " + ex.getMessage());
        }
    }


    public List<Farmaceuta> findAll() {
        List<Farmaceuta> lista = new ArrayList<>();
        try {
            String sql = "SELECT * FROM Farmaceuta f";
            PreparedStatement stm = db.prepareStatement(sql);
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) lista.add(from(rs, "f"));
        } catch (SQLException ex) {
            System.out.println("Error listando farmaceutas: " + ex.getMessage());
        }
        return lista;
    }


    public List<Farmaceuta> searchFarmaceutas(String nombre) {
        List<Farmaceuta> lista = new ArrayList<>();
        try {
            String sql = "SELECT * FROM Farmaceuta WHERE nombre LIKE ?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, "%" + nombre + "%");
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) lista.add(from(rs));
        } catch (SQLException ex) {
            System.out.println("Error filtrando farmaceutas: " + ex.getMessage());
        }
        return lista;
    }

    private Farmaceuta from(ResultSet rs) {
        try {
            Farmaceuta f = new Farmaceuta();
            f.setId(rs.getString("id"));
            f.setNombre(rs.getString("nombre"));
            f.setClave(rs.getString("clave"));
            f.setRol(rs.getString("rol"));
            return f;
        } catch (SQLException ex) {
            return null;
        }
    }



    private Farmaceuta from(ResultSet rs, String alias) {
        try {
            Farmaceuta f = new Farmaceuta();
            f.setId(rs.getString(alias + ".id"));
            f.setNombre(rs.getString(alias + ".nombre"));
            f.setClave(rs.getString(alias + ".clave"));
            f.setRol(rs.getString(alias + ".rol"));
            return f;
        } catch (SQLException ex) {
            return null;
        }
    }
}
