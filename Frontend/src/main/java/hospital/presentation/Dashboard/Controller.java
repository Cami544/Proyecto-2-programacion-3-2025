package hospital.presentation.Dashboard;

import hospital.logic.DetalleReceta;
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
            System.out.println("Recetas cargadas al Dashboard: " + recetasOriginales.size());
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

            // Filtrar recetas dentro del rango de fechas
            List<Receta> recetasFiltradas = recetas.stream()
                    .filter(r -> {
                        LocalDate fechaRef = (r.getFechaRetiro() != null) ? r.getFechaRetiro() : r.getFecha();
                        return fechaRef != null && !fechaRef.isBefore(desde) && !fechaRef.isAfter(hasta);
                    })
                    .collect(Collectors.toList());

            // Mapa: periodo -> (codigoMedicamento -> cantidad total)
            Map<String, Map<String, Integer>> cantidadesPorMes = new LinkedHashMap<>();
            // Mapa: periodo -> (codigoMedicamento -> set de recetas únicas)
            Map<String, Map<String, Set<String>>> recetasPorMes = new LinkedHashMap<>();

            for (Receta receta : recetasFiltradas) {
                LocalDate fechaRef = (receta.getFechaRetiro() != null) ? receta.getFechaRetiro() : receta.getFecha();
                if (fechaRef == null) continue;
                String periodo = fechaRef.format(formatter);

                cantidadesPorMes.computeIfAbsent(periodo, k -> new HashMap<>());
                recetasPorMes.computeIfAbsent(periodo, k -> new HashMap<>());

                if (receta.getDetalles() == null) continue;

                // Obtener ID único de receta (en caso de error, usar identidad de objeto)
                String recetaId;
                try {
                    recetaId = String.valueOf(receta.getId());
                } catch (Exception ex) {
                    recetaId = String.valueOf(System.identityHashCode(receta));
                }

                for (DetalleReceta detalle : receta.getDetalles()) {
                    if (detalle == null) continue;
                    String codigoMed = detalle.getMedicamentoCodigo();
                    if (codigoMed == null) continue;

                    // Si hay filtro por medicamento, se aplica
                    if (medicamento != null && medicamento.getCodigo() != null &&
                            !medicamento.getCodigo().equals(codigoMed)) {
                        continue;
                    }

                    // Acumular cantidad total
                    cantidadesPorMes.get(periodo).merge(codigoMed, detalle.getCantidad(), Integer::sum);
                    // Registrar receta única por medicamento
                    recetasPorMes.get(periodo).computeIfAbsent(codigoMed, k -> new HashSet<>()).add(recetaId);
                }
            }

            // Construcción final de las estadísticas
            for (String periodo : cantidadesPorMes.keySet()) {
                Map<String, Integer> mapMedCant = cantidadesPorMes.get(periodo);

                for (Map.Entry<String, Integer> entry : mapMedCant.entrySet()) {
                    String codigo = entry.getKey();
                    int cantidad = entry.getValue();

                    int numRecetas = 0;
                    if (recetasPorMes.containsKey(periodo) && recetasPorMes.get(periodo).containsKey(codigo)) {
                        numRecetas = recetasPorMes.get(periodo).get(codigo).size();
                    }

                    String nombre = codigo;
                    try {
                        Medicamento med = Service.instance().readMedicamento(codigo);
                        if (med != null && med.getNombre() != null) {
                            nombre = med.getNombre();
                        }
                    } catch (Exception ignored) {
                    }

                    estadisticas.add(new Object[]{periodo, nombre, cantidad, numRecetas});
                }
            }

            // Ordenar cronológicamente por periodo (MM/yyyy)
            estadisticas.sort((a, b) -> ((String) a[0]).compareTo((String) b[0]));

        } catch (Exception e) {
            System.err.println("Error generando estadísticas de medicamentos: " + e.getMessage());
        }

        System.out.println("Datos estadísticos generados: " + estadisticas.size());
        return estadisticas;
    }


    private Map<String, Integer> generarEstadisticasRecetas() {
        Map<String, Integer> estadisticas = new HashMap<>();

        try {
            List<Receta> todasRecetas = Service.instance().getRecetas();

            int confeccionadas = 0;
            int enProceso = 0;
            int listas = 0;
            int entregadas = 0;

            for (Receta r : todasRecetas) {
                switch (r.getEstadoReceta()) {
                    case "Confeccionada" -> confeccionadas++;
                    case "En proceso" -> enProceso++;
                    case "Lista" -> listas++;
                    case "Entregada" -> entregadas++;
                }
            }

            estadisticas.put("Confeccionadas", confeccionadas);
            estadisticas.put("En Proceso", enProceso);
            estadisticas.put("Listas", listas);
            estadisticas.put("Entregadas", entregadas);

        } catch (Exception e) {
            System.err.println("Error generando estadísticas de recetas: " + e.getMessage());
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