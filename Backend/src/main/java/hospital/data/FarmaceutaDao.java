package hospital.data;

import hospital.logic.Farmaceuta;
import java.sql.*;
import java.util.*;

public class FarmaceutaDao {
    private Database db;

    public FarmaceutaDao() {
        db = Database.instance();
        if (db == null) {
            System.err.println("No se pudo establecer la conexión con la base de datos (FarmaceutaDao).");
        } else {
            System.out.println("Conexión establecida correctamente con la base de datos (FarmaceutaDao).");
        }
    }

    public void create(Farmaceuta f) throws Exception {
        String sql = "INSERT INTO Farmaceuta (id, nombre, clave, rol) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            if (f.getId() == null || f.getNombre() == null || f.getClave() == null) {
                throw new Exception("Datos del farmaceuta incompletos.");
            }

            stm.setString(1, f.getId());
            stm.setString(2, f.getNombre());
            stm.setString(3, f.getClave());
            stm.setString(4, f.getRol());
            db.executeUpdate(stm);
        } catch (SQLException ex) {
            throw new Exception("Error al crear farmaceuta: " + ex.getMessage(), ex);
        }
    }

    public Farmaceuta read(String id) throws Exception {
        String sql = "SELECT * FROM Farmaceuta f WHERE TRIM(f.id) = ?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, id != null ? id.trim() : "");
            ResultSet rs = db.executeQuery(stm);
            if (rs.next()) {
                return from(rs, "f");
            } else {
                System.err.println("[FarmaceutaDao] No existe farmaceuta con ID: '" + id + "'");
                throw new Exception("Farmaceuta no existe: " + id);
            }
        } catch (SQLException ex) {
            throw new Exception("Error al leer farmaceuta: " + ex.getMessage(), ex);
        }
    }

    public void update(Farmaceuta f) throws Exception {
        String sql = "UPDATE Farmaceuta SET nombre=?, clave=?, rol=? WHERE id=?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, f.getNombre());
            stm.setString(2, f.getClave());
            stm.setString(3, f.getRol());
            stm.setString(4, f.getId());

            int count = db.executeUpdate(stm);
            if (count == 0) {
                throw new Exception("Farmaceuta no existe para actualizar.");
            }
        } catch (SQLException ex) {
            throw new Exception("Error al actualizar farmaceuta: " + ex.getMessage(), ex);
        }
    }

    // CORREGIDO: Cambiar cedula por id para ser consistente con la tabla
    public void delete(String id) throws Exception {
        String sql = "DELETE FROM Farmaceuta WHERE id = ?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, id);
            int count = db.executeUpdate(stm);
            if (count == 0) {
                throw new Exception("No se encontró un farmaceuta con ID: " + id);
            }
        } catch (SQLException ex) {
            if (ex.getMessage().contains("a foreign key constraint fails")) {
                throw new Exception("No se puede eliminar el farmaceuta porque tiene recetas asociadas.");
            } else {
                throw new Exception("Error al eliminar farmaceuta: " + ex.getMessage(), ex);
            }
        }
    }


    public List<Farmaceuta> findAll() throws Exception {
        List<Farmaceuta> lista = new ArrayList<>();
        String sql = "SELECT * FROM Farmaceuta f ORDER BY f.nombre";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                lista.add(from(rs, "f"));
            }
        } catch (SQLException ex) {
            throw new Exception("Error listando farmaceutas: " + ex.getMessage(), ex);
        }
        return lista;
    }

    public List<Farmaceuta> searchFarmaceutas(String nombre) throws Exception {
        List<Farmaceuta> lista = new ArrayList<>();
        String sql = "SELECT * FROM Farmaceuta f WHERE f.nombre LIKE ? ORDER BY f.nombre";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, "%" + nombre + "%");
            ResultSet rs = db.executeQuery(stm);
            while (rs.next()) {
                lista.add(from(rs, "f"));
            }
        } catch (SQLException ex) {
            throw new Exception("Error buscando farmaceutas: " + ex.getMessage(), ex);
        }
        return lista;
    }

    private Farmaceuta from(ResultSet rs, String alias) throws Exception {
        try {
            Farmaceuta f = new Farmaceuta();
            // Quita el alias, ya que las columnas vienen sin prefijo
            f.setId(rs.getString("id"));
            f.setNombre(rs.getString("nombre"));
            f.setClave(rs.getString("clave"));
            f.setRol(rs.getString("rol"));
            return f;
        } catch (SQLException ex) {
            throw new Exception("Error al mapear farmaceuta desde ResultSet: " + ex.getMessage(), ex);
        }
    }

}