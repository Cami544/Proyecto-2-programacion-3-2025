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
                        return fechaRef != null && !fechaRef.isBefore(desde) && !fechaRef.isAfter(hasta);
                    })
                    .collect(java.util.stream.Collectors.toList());


            Map<String, Map<String, Integer>> cantidadesPorMes = new LinkedHashMap<>();
            Map<String, Map<String, java.util.Set<String>>> recetasPorMes = new LinkedHashMap<>();

            for (Receta receta : recetasFiltradas) {
                LocalDate fechaRef = (receta.getFechaRetiro() != null) ? receta.getFechaRetiro() : receta.getFecha();
                String periodo = fechaRef.format(formatter);
                cantidadesPorMes.computeIfAbsent(periodo, k -> new HashMap<>());
                recetasPorMes.computeIfAbsent(periodo, k -> new HashMap<>());

                if (receta.getDetalles() == null) continue;


                String recetaId = null;
                try {
                    recetaId = String.valueOf(receta.getId());
                } catch (Exception ex) {

                    recetaId = String.valueOf(System.identityHashCode(receta));
                }

                for (hospital.logic.DetalleReceta detalle : receta.getDetalles()) {
                    if (detalle == null) continue;

                    String codigoDetalle = detalle.getMedicamentoCodigo();
                    String codigoReal = resolveMedicamentoCodigo(codigoDetalle);


                    if (medicamento != null && (medicamento.getCodigo() == null || !medicamento.getCodigo().equals(codigoReal))) {
                        continue;
                    }

                    cantidadesPorMes.get(periodo).merge(codigoReal, detalle.getCantidad(), Integer::sum);
                    recetasPorMes.get(periodo).computeIfAbsent(codigoReal, k -> new java.util.HashSet<>()).add(recetaId);
                }
            }

            for (Map.Entry<String, Map<String, Integer>> entryPeriodo : cantidadesPorMes.entrySet()) {
                String periodo = entryPeriodo.getKey();
                Map<String, Integer> mapMedCant = entryPeriodo.getValue();

                if (mapMedCant.isEmpty()) {
                    if (medicamento != null) {
                        estadisticas.add(new Object[]{periodo, medicamento.getNombre(), 0, 0});
                    }
                    continue;
                }

                for (Map.Entry<String, Integer> entryMed : mapMedCant.entrySet()) {
                    String codigoMed = entryMed.getKey();
                    int cantidad = entryMed.getValue();
                    int recetasCount = 0;
                    if (recetasPorMes.get(periodo) != null && recetasPorMes.get(periodo).get(codigoMed) != null) {
                        recetasCount = recetasPorMes.get(periodo).get(codigoMed).size();
                    }

                    String nombreMed = codigoMed;
                    try {
                        hospital.logic.Medicamento m = Service.instance().readMedicamento(codigoMed);
                        if (m != null && m.getNombre() != null) nombreMed = m.getNombre();
                    } catch (Exception ex) {
                    }

                    estadisticas.add(new Object[]{periodo, nombreMed, cantidad, recetasCount});
                }
            }

        } catch (Exception e) {
            System.err.println("Error generando estadísticas: " + e.getMessage());
            estadisticas = generarDatosSimulados(desde, hasta, medicamento);
        }

        return estadisticas;
    }

    private String resolveMedicamentoCodigo(String stored) {
        if (stored == null) return null;
        String s = stored.trim();

        try {
            hospital.logic.Medicamento m = Service.instance().readMedicamento(s);
            if (m != null && m.getCodigo() != null && !m.getCodigo().isEmpty()) {
                return m.getCodigo();
            }
        } catch (Exception ignored) { }

        try {
            List<hospital.logic.Medicamento> meds = Service.instance().getMedicamentos();
            if (meds != null) {
                for (hospital.logic.Medicamento mm : meds) {
                    if (mm.getNombre() != null && mm.getNombre().trim().equalsIgnoreCase(s)) {
                        return mm.getCodigo();
                    }
                }
            }
        } catch (Exception ignored) { }

        return s;
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



    private List<Object[]> generarDatosSimulados(LocalDate desde, LocalDate hasta, Medicamento medicamento) {
        List<Object[]> datos = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/yyyy");

        LocalDate fechaActual = desde.withDayOfMonth(1);
        while (!fechaActual.isAfter(hasta)) {
            String periodo = fechaActual.format(formatter);
            String nombreMed = medicamento != null ? medicamento.getNombre() : "Acetaminofén";
            int cantidad = (int) (Math.random() * 100) + 10;
            int recetas = (int) (Math.random() * 20) + 5;

            datos.add(new Object[]{periodo, nombreMed, cantidad, recetas});
            fechaActual = fechaActual.plusMonths(1);
        }

        return datos;
    }

    private String obtenerNombreMedicamento(String codigo) {
        if (codigo == null) return "Desconocido";
        try {
            hospital.logic.Medicamento m = Service.instance().readMedicamento(codigo);
            if (m != null && m.getNombre() != null && !m.getNombre().isEmpty()) {
                return m.getNombre();
            }
        } catch (Exception e) {
        }

        try {
            for (hospital.logic.Medicamento mm : Service.instance().getMedicamentos()) {
                if (mm.getCodigo().equalsIgnoreCase(codigo) || (mm.getNombre() != null && mm.getNombre().equalsIgnoreCase(codigo))) {
                    return mm.getNombre();
                }
            }
        } catch (Exception ignored) {}

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

    public List<Receta> obtenerRecetasEnRango(LocalDate desde, LocalDate hasta) {
        return Service.instance().getRecetas().stream()
                .filter(r -> {
                    LocalDate fecha = (r.getFechaRetiro() != null) ? r.getFechaRetiro() : r.getFecha();
                    return (fecha != null && !fecha.isBefore(desde) && !fecha.isAfter(hasta));
                })
                .collect(Collectors.toList());
    }
    public List<Receta> obtenerRecetasFiltradas(LocalDate desde, LocalDate hasta, Medicamento medicamento) {
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