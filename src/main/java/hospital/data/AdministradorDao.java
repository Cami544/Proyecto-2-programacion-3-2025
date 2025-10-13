package hospital.data;

import hospital.logic.Administrador;
import java.sql.*;
import java.util.*;

public class AdministradorDao {
    Database db;

    public AdministradorDao() {
        db = Database.instance();
    }

    public void create(Administrador a) throws Exception {
        String sql = "INSERT INTO Administrador (id, nombre, clave, rol) VALUES (?, ?, ?, ?)";
        PreparedStatement stm = db.prepareStatement(sql);
        stm.setString(1, a.getId());
        stm.setString(2, a.getNombre());
        stm.setString(3, a.getClave());
        stm.setString(4, a.getRol());
        db.executeUpdate(stm);
    }

    public Administrador read(String id) throws Exception {
        String sql = "SELECT * FROM Administrador a WHERE a.id=?";
        PreparedStatement stm = db.prepareStatement(sql);
        stm.setString(1, id);
        ResultSet rs = db.executeQuery(stm);
        if (rs.next()) return from(rs, "a");
        else throw new Exception("Administrador no existe");
    }

    public void update(Administrador a) throws Exception {
        String sql = "UPDATE Administrador SET nombre=?, clave=?, rol=? WHERE id=?";
        PreparedStatement stm = db.prepareStatement(sql);
        stm.setString(1, a.getNombre());
        stm.setString(2, a.getClave());
        stm.setString(3, a.getRol());
        stm.setString(4, a.getId());
        db.executeUpdate(stm);
    }

    public void delete(String id) throws Exception {
        String sql = "DELETE FROM Administrador WHERE id=?";
        PreparedStatement stm = db.prepareStatement(sql);
        stm.setString(1, id);
        db.executeUpdate(stm);
    }

    public List<Administrador> findAll() {
        List<Administrador> lista = new ArrayList<>();
        try {
            String sql = "SELECT * FROM Administrador a";
            PreparedStatement stm = db.prepareStatement(sql);
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) lista.add(from(rs, "a"));
        } catch (SQLException ex) {
            System.out.println("Error listando administradores: " + ex.getMessage());
        }
        return lista;
    }

    public List<Administrador> filterByName(String nombre) {
        List<Administrador> lista = new ArrayList<>();
        try {
            String sql = "SELECT * FROM Administrador a WHERE a.nombre LIKE ?";
            PreparedStatement stm = db.prepareStatement(sql);
            stm.setString(1, "%" + nombre + "%");
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) lista.add(from(rs, "a"));
        } catch (SQLException ex) {
            System.out.println("Error filtrando administradores: " + ex.getMessage());
        }
        return lista;
    }

    private Administrador from(ResultSet rs, String alias) {
        try {
            Administrador a = new Administrador();
            a.setId(rs.getString(alias + ".id"));
            a.setNombre(rs.getString(alias + ".nombre"));
            a.setClave(rs.getString(alias + ".clave"));
            a.setRol(rs.getString(alias + ".rol"));
            return a;
        } catch (SQLException ex) {
            return null;
        }
    }
}
