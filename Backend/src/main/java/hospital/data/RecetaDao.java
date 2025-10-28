package hospital.data;

import hospital.logic.Paciente;
import hospital.logic.Receta;
import hospital.logic.DetalleReceta;
import java.sql.*;
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
        String sql = "INSERT INTO receta (pacienteId, farmaceutaId, estadoReceta, fecha, fechaRetiro) VALUES (?, ?, ?, ?, ?)";
        Connection cnx = db.getConnection();
        boolean previousAutoCommit = cnx.getAutoCommit();
        try {
            cnx.setAutoCommit(false);

            try (PreparedStatement stmt = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                // Debug: imprimir lo que vamos a insertar
                System.out.println("Insertando receta - pacienteId: " + receta.getPacienteId()
                        + " farmaceutaId: " + receta.getFarmaceutaId()
                        + " estado: " + receta.getEstadoReceta()
                        + " fecha: " + receta.getFecha()
                        + " fechaRetiro: " + receta.getFechaRetiro());

                stmt.setString(1, receta.getPacienteId());

                // Manejo seguro de farmaceutaId: null/"" => SQL NULL
                String farmId = receta.getFarmaceutaId();
                if (farmId == null || farmId.trim().isEmpty() || farmId.equalsIgnoreCase("Sin asignar")) {
                    stmt.setNull(2, Types.VARCHAR);
                } else {
                    stmt.setString(2, farmId);
                }


                stmt.setString(3, receta.getEstadoReceta() != null ? receta.getEstadoReceta() : ""); // ajustar según tu lógica

                if (receta.getFecha() != null) {
                    stmt.setDate(4, Date.valueOf(receta.getFecha()));
                } else {
                    stmt.setNull(4, Types.DATE);
                }

                if (receta.getFechaRetiro() != null) {
                    stmt.setDate(5, Date.valueOf(receta.getFechaRetiro()));
                } else {
                    stmt.setNull(5, Types.DATE);
                }

                int filas = stmt.executeUpdate();
                if (filas == 0) {
                    throw new SQLException("No se insertó la receta (0 filas afectadas).");
                }

                // Obtener ID autogenerado
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        int idGenerado = rs.getInt(1);
                        receta.setId(idGenerado);
                        System.out.println("Receta creada con ID autogenerado: " + idGenerado);
//Rastreo de error
                        for (DetalleReceta detalle : receta.getDetalles()) {
                            System.out.println("[DEBUG] Insertando detalle con recetaId asignado: " + detalle.getRecetaId());
                        }

                    } else {
                        throw new SQLException("No se obtuvo el ID generado para la receta.");
                    }
                }
            }

            // Si la receta trae detalles, insertarlos con receta.getId() (ya asignado)
            if (receta.getDetalles() != null && !receta.getDetalles().isEmpty()) {
                DetalleRecetaDao detalleDao = new DetalleRecetaDao();
                for (DetalleReceta detalle : receta.getDetalles()) {
                    detalle.setRecetaId(receta.getId());
                    detalleDao.create(detalle); // asegurate que este método usa la misma conexión o que detalleDao no hace commit por separado
                }
            }

            cnx.commit();
        } catch (SQLException ex) {
            try { cnx.rollback(); } catch (SQLException r) { System.err.println("Rollback falló: " + r.getMessage()); }
            throw new Exception("Error al crear receta: " + ex.getMessage(), ex);
        } finally {
            try { cnx.setAutoCommit(previousAutoCommit); } catch (SQLException ignored) {}
        }
    }



    public Receta read(int id) throws Exception {
        String sql = "SELECT * FROM Receta WHERE id=?";
        try (PreparedStatement stmt = db.prepareStatement(sql)) {
            stmt.setInt(1, id);
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
            stmt.setInt(6, receta.getId());

            int count = stmt.executeUpdate();
            if (count == 0) {
                throw new Exception("Receta no existe para actualizar.");
            }
        } catch (SQLException ex) {
            throw new Exception("Error al actualizar receta: " + ex.getMessage(), ex);
        }
    }

    public void delete(int id) throws Exception {
        try {
            // Primero eliminar detalles
            DetalleRecetaDao detalleDao = new DetalleRecetaDao();
            detalleDao.deleteByReceta(id);

            // Luego eliminar receta
            String sql = "DELETE FROM Receta WHERE id=?";
            try (PreparedStatement stmt = db.prepareStatement(sql)) {
                stmt.setInt(1, id);
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

    public List<Object[]> getCantidadMedicamentosPorMes() {
        List<Object[]> resultados = new ArrayList<>();

        // Cadena SQL en formato clásico (compatible con Java 8+)
        String sql =
                "SELECT DATE_FORMAT(r.fecha, '%m/%Y') AS periodo, " +
                "       SUM(dr.cantidad) AS total " +
                "FROM receta r " +
                "JOIN detallereceta dr ON r.id = dr.recetaId " +
                "GROUP BY periodo " +
                "ORDER BY STR_TO_DATE(CONCAT('01/', periodo), '%d/%m/%Y');";

        Database db = Database.instance();
        try (PreparedStatement ps = db.prepareStatement(sql);
             ResultSet rs = db.executeQuery(ps)) {

            while (rs != null && rs.next()) {
                String periodo = rs.getString("periodo");
                int total = rs.getInt("total");
                resultados.add(new Object[]{ periodo, total });
            }

        } catch (SQLException e) {
            // imprime traza para ver el problema en detalle
            e.printStackTrace();
        }

        System.out.println("[DEBUG] getCantidadMedicamentosPorMes -> filas: " + resultados.size());
        return resultados;
    }

    public List<Receta> filterByPaciente(String paciente) throws Exception {
        String pacienteId = paciente;
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
            r.setId(Integer.parseInt(rs.getString("id")));
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
