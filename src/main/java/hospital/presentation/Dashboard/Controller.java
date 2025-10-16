package hospital.presentation.Dashboard;

import hospital.logic.Medicamento;
import hospital.logic.Receta;
import hospital.logic.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Controller {
    private View view;
    private Model model;

    public Controller(View view, Model model) {
        this.view = view;
        this.model = model;
        view.setController(this);
        view.setModel(model);

        cargarMedicamentos();

        try {
            List<Receta> recetasOriginales = Service.instance().getRecetas();
            model.setRecetasDashboard(new ArrayList<>(recetasOriginales));
        } catch (Exception e) {
            model.setRecetasDashboard(new ArrayList<>());
        }
    }

    public void setFechaDesde(LocalDate fecha) throws Exception {
        if (fecha == null) {
            throw new Exception("La fecha desde no puede ser nula");
        }

        if (model.getFechaHasta() != null && fecha.isAfter(model.getFechaHasta())) {
            throw new Exception("La fecha desde no puede ser posterior a la fecha hasta");
        }

        model.setFechaDesde(fecha);
    }

    public void setFechaHasta(LocalDate fecha) throws Exception {
        if (fecha == null) {
            throw new Exception("La fecha hasta no puede ser nula");
        }

        if (model.getFechaDesde() != null && fecha.isBefore(model.getFechaDesde())) {
            throw new Exception("La fecha hasta no puede ser anterior a la fecha desde");
        }

        model.setFechaHasta(fecha);
    }

    public void setMedicamentoSeleccionado(Medicamento medicamento) {
        model.setMedicamentoSeleccionado(medicamento);
    }

    public List<Medicamento> obtenerMedicamentos() {
        return model.getMedicamentosDisponibles();
    }

    public void actualizarEstadisticas() throws Exception {
        LocalDate fechaDesde = model.getFechaDesde();
        LocalDate fechaHasta = model.getFechaHasta();
        Medicamento medicamento = model.getMedicamentoSeleccionado();

        List<Object[]> estadisticas = generarEstadisticasMedicamentos(fechaDesde, fechaHasta, medicamento);
        model.setDatosEstadisticas(estadisticas);

        Map<String, Integer> estadisticasRecetas = generarEstadisticasRecetas();
        model.setEstadisticasRecetas(estadisticasRecetas);
    }

    private List<Object[]> generarEstadisticasMedicamentos(LocalDate desde, LocalDate hasta, Medicamento medicamento) {
        List<Object[]> estadisticas = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        try {
            List<Receta> recetas = model.getRecetasDashboard();
            if (recetas == null) recetas = new ArrayList<>();

            if (desde == null || hasta == null) return estadisticas;

            List<Receta> recetasFiltradas = recetas.stream()
                    .filter(r -> {
                        LocalDate fechaRef = (r.getFechaRetiro() != null) ? r.getFechaRetiro() : r.getFecha();
                        if (fechaRef == null) return false;
                        return !fechaRef.isBefore(desde) && !fechaRef.isAfter(hasta);
                    })
                    .collect(Collectors.toList());

            Map<String, Map<String, Integer>> agrupados = new HashMap<>();

            for (Receta r : recetasFiltradas) {
                LocalDate fechaRef = (r.getFechaRetiro() != null) ? r.getFechaRetiro() : r.getFecha();
                if (fechaRef == null) continue;

                String periodo = fechaRef.format(formatter);

                if (r.getDetalles() != null) {
                    for (hospital.logic.DetalleReceta d : r.getDetalles()) {
                        String codigoMed = d.getMedicamentoCodigo();

                        if (medicamento != null && !medicamento.getCodigo().equals(codigoMed)) {
                            continue;
                        }

                        agrupados.putIfAbsent(periodo, new HashMap<>());
                        Map<String, Integer> medMap = agrupados.get(periodo);
                        medMap.put(codigoMed, medMap.getOrDefault(codigoMed, 0) + d.getCantidad());
                    }
                }
            }

            for (Map.Entry<String, Map<String, Integer>> entry : agrupados.entrySet()) {
                String periodo = entry.getKey();
                for (Map.Entry<String, Integer> medEntry : entry.getValue().entrySet()) {
                    String codigo = medEntry.getKey();
                    int cantidad = medEntry.getValue();
                    String nombre = obtenerNombreMedicamento(codigo);

                    long numRecetas = recetasFiltradas.stream()
                            .filter(r -> {
                                LocalDate fechaRef = (r.getFechaRetiro() != null) ? r.getFechaRetiro() : r.getFecha();
                                if (fechaRef == null) return false;
                                if (!fechaRef.format(formatter).equals(periodo)) return false;
                                if (r.getDetalles() == null) return false;
                                return r.getDetalles().stream()
                                        .anyMatch(d -> d.getMedicamentoCodigo().equals(codigo));
                            })
                            .count();

                    estadisticas.add(new Object[]{periodo, nombre, cantidad, (int) numRecetas});
                }
            }

            estadisticas.sort((a, b) -> ((String) a[0]).compareTo((String) b[0]));

        } catch (Exception e) {
            System.err.println("Error generando estadisticas de medicamentos: " + e.getMessage());
        }

        return estadisticas;
    }

    private Map<String, Integer> generarEstadisticasRecetas() {
        Map<String, Integer> estadisticas = new HashMap<>();

        try {
            List<Receta> recetas = model.getRecetasDashboard();
            if (recetas == null) return estadisticas;

            long confeccionadas = recetas.stream()
                    .filter(r -> "Confeccionada".equalsIgnoreCase(r.getEstadoReceta()))
                    .count();

            long despachadas = recetas.stream()
                    .filter(r -> "Despachada".equalsIgnoreCase(r.getEstadoReceta()))
                    .count();

            estadisticas.put("Confeccionadas", (int) confeccionadas);
            estadisticas.put("Despachadas", (int) despachadas);
            estadisticas.put("Total", recetas.size());

        } catch (Exception e) {
            System.err.println("Error generando estadisticas de recetas: " + e.getMessage());
        }

        return estadisticas;
    }

    private String obtenerNombreMedicamento(String codigo) {
        if (codigo == null) return "Desconocido";

        try {
            hospital.logic.Medicamento m = Service.instance().readMedicamento(codigo);
            if (m != null && m.getNombre() != null && !m.getNombre().isEmpty()) {
                return m.getNombre();
            }
        } catch (Exception e) {
            // Intentar buscar en la lista de medicamentos disponibles
        }

        try {
            for (hospital.logic.Medicamento mm : Service.instance().getMedicamentos()) {
                if (mm.getCodigo().equalsIgnoreCase(codigo) ||
                        (mm.getNombre() != null && mm.getNombre().equalsIgnoreCase(codigo))) {
                    return mm.getNombre();
                }
            }
        } catch (Exception ignored) {
        }

        return codigo;
    }

    public void cargarMedicamentos() {
        try {
            List<Medicamento> medicamentos = Service.instance().getMedicamentos();
            model.setMedicamentosDisponibles(medicamentos);
            System.out.println("Medicamentos cargados: " + medicamentos.size());
        } catch (Exception e) {
            System.err.println("Error cargando medicamentos: " + e.getMessage());
            model.setMedicamentosDisponibles(new ArrayList<>());
        }
    }

    public List<Receta> obtenerRecetasEnRango(LocalDate desde, LocalDate hasta) throws Exception {
        return Service.instance().getRecetas().stream()
                .filter(r -> {
                    LocalDate fecha = (r.getFechaRetiro() != null) ? r.getFechaRetiro() : r.getFecha();
                    return (fecha != null && !fecha.isBefore(desde) && !fecha.isAfter(hasta));
                })
                .collect(Collectors.toList());
    }

    public List<Receta> obtenerRecetasFiltradas(LocalDate desde, LocalDate hasta, Medicamento medicamento) throws Exception {
        return Service.instance().getRecetas().stream()
                .filter(r -> {
                    LocalDate fecha = (r.getFechaRetiro() != null) ? r.getFechaRetiro() : r.getFecha();
                    if (fecha == null) return false;
                    if (fecha.isBefore(desde) || fecha.isAfter(hasta)) return false;

                    if (medicamento != null) {
                        return r.getDetalles().stream()
                                .anyMatch(d -> d.getMedicamentoCodigo().equals(medicamento.getCodigo()));
                    }
                    return true;
                })
                .collect(Collectors.toList());
    }
}