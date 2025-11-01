package hospital.presentation.Despacho;

import hospital.logic.Farmaceuta;
import hospital.logic.Receta;

import hospital.logic.Service;
import hospital.presentation.ThreadListener;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class View implements PropertyChangeListener, ThreadListener {
    private JTable list;
    private JTextField buscarIdText;
    private JLabel idPacienteLabel;
    private JButton buscarButton;
    private JComboBox<Farmaceuta> farmaceutaComboBox;
    private JLabel estadoRecetaLabel;
    private JComboBox<String> recetaComboBox;
    private JButton guardarButton;
    private JButton limpiarButton;
    private JPanel panel;

    private Model model;
    private Controller controller;

    public View() {
        setupEventHandlers();
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setModel(Model model) {
        this.model = model;
        model.addPropertyChangeListener(this);
    }

    public void setController(Controller controller) {
        this.controller = controller;
    }

    private void setupEventHandlers() {

        buscarButton.addActionListener(e -> {
            try {
                String idPaciente = buscarIdText.getText().trim();
                controller.buscarRecetasPorPaciente(idPaciente);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel, "Error en busqueda: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        guardarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    Farmaceuta farmaceuta = (Farmaceuta) farmaceutaComboBox.getSelectedItem();
                    String estado = (String) recetaComboBox.getSelectedItem();
                    Receta receta = model.getRecetaSeleccionada();
                    if (receta == null) {
                        JOptionPane.showMessageDialog(panel,
                                "Debe seleccionar una receta primero.",
                                "Advertencia",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    if (farmaceuta == null) {
                        JOptionPane.showMessageDialog(panel,
                                "Debe seleccionar un farmaceuta antes de guardar.",
                                "Advertencia",
                                JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    String farmaceutaId = farmaceuta.getId();
                    if (farmaceutaId == null || farmaceutaId.trim().isEmpty()) {
                        JOptionPane.showMessageDialog(panel,
                                "El farmaceuta seleccionado no tiene un ID válido.",
                                "Error de datos",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    // Validar secuencia
                    String estadoActual = receta.getEstadoReceta();
                    if (!esCambioValido(estadoActual, estado)) {
                        JOptionPane.showMessageDialog(panel,
                                "No se puede cambiar el estado de '" + estadoActual +
                                        "' a '" + estado + "'.\nDebe seguir la secuencia: " +
                                        "Confeccionada → En proceso → Lista → Entregada.",
                                "Cambio no permitido",
                                JOptionPane.WARNING_MESSAGE);
                        recetaComboBox.setSelectedItem(estadoActual);
                        return;
                    }
                    System.out.println("[Despacho][DEBUG] Farmaceuta seleccionado: " +
                            (farmaceuta != null ? (farmaceuta.getId() + " - " + farmaceuta.getNombre()) : "null"));
                    controller.guardarCambiosReceta(farmaceutaId, estado);
                    JOptionPane.showMessageDialog(panel,
                            "La receta se actualizó correctamente.",
                            "Información",
                            JOptionPane.INFORMATION_MESSAGE);

                    if ("Entregada".equalsIgnoreCase(estado)) {
                        recetaComboBox.setEnabled(false);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel,
                            "Error al guardar cambios: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = list.getSelectedRow();
                    if (row >= 0) {
                        try {
                            controller.seleccionarRecetaPaciente(row);

                            Receta receta = model.getRecetaSeleccionada();
                            if (receta != null && "Entregada".equalsIgnoreCase(receta.getEstadoReceta())) {
                                recetaComboBox.setEnabled(false);
                            } else {
                                recetaComboBox.setEnabled(true);
                            }

                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(panel, "Error al seleccionar receta: " + ex.getMessage(),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                }
            }
        });

        limpiarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                buscarIdText.setText("");
                try {
                    controller.refrecarDatos();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(panel, "Error al refrescar datos: " + ex.getMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private boolean esCambioValido(String estadoActual, String nuevoEstado) {
        if (estadoActual == null) return true; // Si es nueva o sin estado previo

        switch (estadoActual) {
            case "Confeccionada":
                return nuevoEstado.equals("Confeccionada") || nuevoEstado.equals("En proceso");
            case "En proceso":
                return nuevoEstado.equals("En proceso") || nuevoEstado.equals("Lista");
            case "Lista":
                return nuevoEstado.equals("Lista") || nuevoEstado.equals("Entregada");
            case "Entregada":
                return nuevoEstado.equals("Entregada"); // No se puede cambiar más
            default:
                return false;
        }
    }

    private void actualizarCombos() {
        farmaceutaComboBox.removeAllItems();
        for (Farmaceuta f : model.getListFarmaceutas()) {
            farmaceutaComboBox.addItem(f);
        }
        System.out.println("[Despacho] Cargando farmaceutas: " + model.getListFarmaceutas().size());

        recetaComboBox.removeAllItems();
        recetaComboBox.addItem("Confeccionada");
        recetaComboBox.addItem("En proceso");
        recetaComboBox.addItem("Lista");
        recetaComboBox.addItem("Entregada");
    }

    private void actualizarDetalleReceta() {
        Receta receta = model.getRecetaSeleccionada();
        if (receta != null) {
            buscarIdText.setText(receta.getPacienteId());

            if (receta.getFarmaceutaId() == null || receta.getFarmaceutaId().trim().isEmpty()) {
                farmaceutaComboBox.setSelectedIndex(-1);
            } else {
                for (int i = 0; i < farmaceutaComboBox.getItemCount(); i++) {
                    Farmaceuta f = (Farmaceuta) farmaceutaComboBox.getItemAt(i);
                    if (f != null && receta.getFarmaceutaId().equals(f.getId())) {
                        farmaceutaComboBox.setSelectedItem(f);
                        break;
                    }
                }
            }
            if (receta.getEstadoReceta() != null) {
                recetaComboBox.setSelectedItem(receta.getEstadoReceta());
            } else {
                recetaComboBox.setSelectedIndex(-1);
            }

            if ("Entregada".equalsIgnoreCase(receta.getEstadoReceta())) {
                recetaComboBox.setEnabled(false);
                farmaceutaComboBox.setEnabled(false);
                guardarButton.setEnabled(false);
            } else {
                recetaComboBox.setEnabled(true);
                farmaceutaComboBox.setEnabled(true);
                guardarButton.setEnabled(true);
            }
        } else {
            buscarIdText.setText("");
            farmaceutaComboBox.setSelectedIndex(-1);
            recetaComboBox.setSelectedIndex(-1);

            recetaComboBox.setEnabled(true);
            farmaceutaComboBox.setEnabled(true);
            guardarButton.setEnabled(true);
        }
    }

    private void actualizarTabla() {
        int[] cols = {TableModel.FARNACEUTA, TableModel.ID_RECETA, TableModel.PACIENTE,
                TableModel.FECHA_RETIRO, TableModel.ESTADO};

        if (model.getRecetasFiltradasPaciente() != null && !model.getRecetasFiltradasPaciente().isEmpty()) {
            list.setModel(new TableModel(cols, model.getRecetasFiltradasPaciente()));
        } else {
            list.setModel(new TableModel(cols, model.getListRecetas()));
        }

        list.setRowHeight(30);
        if (list.getColumnModel().getColumnCount() > 0) {
            TableColumnModel columnModel = list.getColumnModel();
            columnModel.getColumn(0).setPreferredWidth(150);
            columnModel.getColumn(1).setPreferredWidth(80);
            columnModel.getColumn(2).setPreferredWidth(150);
            columnModel.getColumn(3).setPreferredWidth(80);
            columnModel.getColumn(4).setPreferredWidth(150);
        }
    }

    @Override
    public void refresh() {
        if (controller == null) return;
        new Thread(() -> {
            if (!Service.instance().tryStartRefresh()) return;
            try {
                controller.refrecarDatos();
            } catch (Exception e) {
                System.err.println("Error refrescando despacho: " + e.getMessage());
            } finally {
                Service.instance().endRefresh();
            }
        }, "Despacho-Refresh-Thread").start();
    }

    @Override
    public void deliver_message(String message) {}

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case Model.LIST_RECETA, Model.RECETA_FILTRADO:
                actualizarTabla();
                break;
            case Model.LIST_FARMACIA:
                actualizarCombos();
                break;
            case Model.RECETA_SELECCIONADO:
                actualizarDetalleReceta();
                break;
        }
        this.panel.revalidate();
    }
}