package hospital.presentation.Preescribir;


import hospital.logic.Medicamento;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AgregarMedicamentoDialog extends JDialog {
    private JPanel contentPane;
    private JButton agregarButton;
    private JButton cancelarButton;
    private JLabel nombreLabel;
    private JTable medicamentoTable;
    private JTextField nombreText;
    private JButton buscarButton;
    private JLabel cantidadLabel;
    private JTextField cantidadText;
    private JTextField indicacionesText;
    private JPanel MedicamentosDisponibles;
    private JScrollPane listMedicamento;

    private Controller controller;
    private Model model;

    private DefaultTableModel tableModel;

    public AgregarMedicamentoDialog(Controller controller, Model model) {
        this.controller = controller;
        this.model = model;

        setContentPane(contentPane);
        setModal(true);
        setTitle("Agregar Medicamento a Receta");
        setSize(700, 500);
        setLocationRelativeTo(null);

        initTable();
        setupEventHandlers();

        cargarMedicamentos(controller.obtenerMedicamentos());
    }

    private void initTable() {
        tableModel = new DefaultTableModel(new Object[]{"Código", "Nombre", "Presentación"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        medicamentoTable.setModel(tableModel);
        medicamentoTable.setRowHeight(25);
    }

    private void setupEventHandlers() {
        // Cierra con el botón cancelar
        cancelarButton.addActionListener(e -> dispose());

        /*
        nombreText.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { filtrarMedicamentos(); }
            public void removeUpdate(DocumentEvent e) { filtrarMedicamentos(); }
            public void changedUpdate(DocumentEvent e) { filtrarMedicamentos(); }
        });*/

        buscarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String texto = nombreText.getText().trim().toLowerCase();
                List<Medicamento> resultados;

                if (texto.isEmpty()) {
                    resultados = controller.obtenerMedicamentos();
                }
                else{
                    resultados = controller.buscarMedicamentos(texto);
                }
                SwingUtilities.invokeLater(() -> cargarMedicamentos(resultados));
            }
        });
        agregarButton.addActionListener(e -> agregarMedicamentoSeleccionado());
    }

    private void cargarMedicamentos(List<Medicamento> medicamentos) {
        tableModel.setRowCount(0);
        for (Medicamento m : medicamentos) {
            tableModel.addRow(new Object[]{
                    m.getCodigo(),
                    m.getNombre(),
                    m.getPresentacion()
            });
        }
    }



    private void agregarMedicamentoSeleccionado() {
        int fila = medicamentoTable.getSelectedRow();
        nombreText.setText(medicamentoTable.getValueAt(fila, 0).toString());
        if (fila < 0) {
            JOptionPane.showMessageDialog(this,
                    "Debe seleccionar un medicamento de la lista",
                    "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String codigo = medicamentoTable.getValueAt(fila, 0).toString();
        String cantidadTexto = cantidadText.getText().trim();
        String indicaciones = indicacionesText.getText().trim();

        if (cantidadTexto.isEmpty() || indicaciones.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Debe ingresar cantidad e indicaciones",
                    "Campos incompletos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            int cantidad = Integer.parseInt(cantidadTexto);
            controller.agregarMedicamento(codigo, cantidad, indicaciones);
            JOptionPane.showMessageDialog(this,
                    "Medicamento agregado exitosamente",
                    "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "La cantidad debe ser un número válido",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error al agregar medicamento: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}