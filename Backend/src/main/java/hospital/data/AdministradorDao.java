package hospital.data;

import hospital.logic.Administrador;
import java.sql.*;
import java.util.*;

public class AdministradorDao {
    private Database db;

    public AdministradorDao() {
        db = Database.instance();
        if (db == null) {
            System.err.println("No se pudo establecer la conexión con la base de datos (AdministradorDao).");
        } else {
            System.out.println("Conexión establecida correctamente con la base de datos (AdministradorDao).");
        }
    }

    public void create(Administrador a) throws Exception {
        String sql = "INSERT INTO Administrador (id, nombre, clave, rol) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            if (a.getId() == null || a.getNombre() == null || a.getClave() == null) {
                throw new Exception("Datos del administrador incompletos.");
            }

            stm.setString(1, a.getId());
            stm.setString(2, a.getNombre());
            stm.setString(3, a.getClave());
            stm.setString(4, a.getRol());
            db.executeUpdate(stm);
        } catch (SQLException ex) {
            throw new Exception("Error al crear administrador: " + ex.getMessage(), ex);
        }
    }

    public Administrador read(String id) throws Exception {
        String sql = "SELECT * FROM Administrador a WHERE a.id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, id);
            ResultSet rs = db.executeQuery(stm);
            if (rs.next()) {
                return from(rs, "a");
            } else {
                throw new Exception("Administrador no existe");
            }
        } catch (SQLException ex) {
            throw new Exception("Error al leer administrador: " + ex.getMessage(), ex);
        }
    }

    public void update(Administrador a) throws Exception {
        String sql = "UPDATE Administrador SET nombre=?, clave=?, rol=? WHERE id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, a.getNombre());
            stm.setString(2, a.getClave());
            stm.setString(3, a.getRol());
            stm.setString(4, a.getId());

            int count = db.executeUpdate(stm);
            if (count == 0) {
                throw new Exception("Administrador no existe para actualizar.");
            }
        } catch (SQLException ex) {
            throw new Exception("Error al actualizar administrador: " + ex.getMessage(), ex);
        }
    }

    public void delete(String id) throws Exception {
        String sql = "DELETE FROM Administrador WHERE id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, id);
            int count = db.executeUpdate(stm);
            if (count == 0) {
                throw new Exception("No se encontró un administrador con ID: " + id);
            }
        } catch (SQLException ex) {
            throw new Exception("Error al eliminar administrador: " + ex.getMessage(), ex);
        }
    }

    public List<Administrador> findAll() throws Exception {
        List<Administrador> lista = new ArrayList<>();
        String sql = "SELECT * FROM Administrador a ORDER BY a.nombre";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                lista.add(from(rs, "a"));
            }
        } catch (SQLException ex) {
            throw new Exception("Error listando administradores: " + ex.getMessage(), ex);
        }
        return lista;
    }

    public List<Administrador> filterByName(String nombre) throws Exception {
        List<Administrador> lista = new ArrayList<>();
        String sql = "SELECT * FROM Administrador a WHERE a.nombre LIKE ? ORDER BY a.nombre";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, "%" + nombre + "%");
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                lista.add(from(rs, "a"));
            }
        } catch (SQLException ex) {
            throw new Exception("Error filtrando administradores por nombre: " + ex.getMessage(), ex);
        }
        return lista;
    }

    private Administrador from(ResultSet rs, String alias) throws Exception {
        try {
            Administrador a = new Administrador();
            a.setId(rs.getString(alias + ".id"));
            a.setNombre(rs.getString(alias + ".nombre"));
            a.setClave(rs.getString(alias + ".clave"));
            a.setRol(rs.getString(alias + ".rol"));
            return a;
        } catch (SQLException ex) {
            throw new Exception("Error al mapear administrador desde ResultSet: " + ex.getMessage(), ex);
        }
    }
}