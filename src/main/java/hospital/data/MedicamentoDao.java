package hospital.data;

import hospital.logic.Medicamento;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicamentoDao {
    private Database db;

    public MedicamentoDao() {
        db = Database.instance();
        if (db == null) {
            System.err.println("No se pudo establecer la conexión con la base de datos.");
        } else {
            System.out.println("Conexión con la base de datos establecida para MedicamentoDao.");
        }
    }

    public void create(Medicamento m) throws Exception {
        String sql = "INSERT INTO Medicamento (codigo, nombre, presentacion) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {

            // Validación antes de insertar
            if (m.getCodigo() == null || m.getNombre() == null || m.getPresentacion() == null) {
                throw new Exception("Datos del medicamento incompletos.");
            }

            stmt.setString(1, m.getCodigo());
            stmt.setString(2, m.getNombre());
            stmt.setString(3, m.getPresentacion());
            db.executeUpdate(stmt);

        } catch (SQLException ex) {
            throw new Exception("Error al crear medicamento: " + ex.getMessage(), ex);
        }
    }

    public Medicamento read(String codigo) throws Exception {
        String sql = "SELECT * FROM Medicamento WHERE codigo = ?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, codigo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return from(rs);
                } else {
                    throw new Exception("Medicamento no encontrado con código: " + codigo);
                }
            }
        }
    }

    public void update(Medicamento m) throws Exception {
        String sql = "UPDATE Medicamento SET nombre = ?, presentacion = ? WHERE codigo = ?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, m.getNombre());
            stmt.setString(2, m.getPresentacion());
            stmt.setString(3, m.getCodigo());

            int count = stmt.executeUpdate();
            if (count == 0) {
                throw new Exception("Medicamento no existe para actualizar.");
            }
        }
    }

    public void delete(String codigo) throws Exception {
        String sql = "DELETE FROM Medicamento WHERE codigo = ?";
        try (PreparedStatement stm = db.prepareStatement(sql)) {
            stm.setString(1, codigo);
            int count = db.executeUpdate(stm);
            if (count == 0) {
                throw new Exception("No se encontró un medicamento con el código: " + codigo);
            }
        } catch (SQLException ex) {
            throw new Exception("Error al eliminar medicamento: " + ex.getMessage());
        }
    }



    public List<Medicamento> search(String filtro) throws Exception {
        List<Medicamento> resultado = new ArrayList<>();
        String sql = "SELECT * FROM Medicamento WHERE nombre LIKE ? OR presentacion LIKE ? ORDER BY nombre";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setString(1, "%" + filtro + "%");
            stmt.setString(2, "%" + filtro + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    resultado.add(from(rs));
                }
            }
        }
        return resultado;
    }

    public List<Medicamento> getAll() throws Exception {
        List<Medicamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM Medicamento ORDER BY nombre";
        try (PreparedStatement stmt = db.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(from(rs));
            }
        }
        return lista;
    }

    private Medicamento from(ResultSet rs) throws Exception {
        Medicamento m = new Medicamento();
        m.setCodigo(rs.getString("codigo"));
        m.setNombre(rs.getString("nombre"));
        m.setPresentacion(rs.getString("presentacion"));
        return m;
    }
}
