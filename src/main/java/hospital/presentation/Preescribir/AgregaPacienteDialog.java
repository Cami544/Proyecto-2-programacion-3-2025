package hospital.presentation.Preescribir;

import hospital.logic.Paciente;
import hospital.logic.Service;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.event.*;
import java.util.List;

public class AgregaPacienteDialog extends JDialog {
    private JPanel contentPane;
    private JButton agregarButton;
    private JButton cancelarButton;
    private JTable table1;
    private JLabel nombreLabel;
    private JTextField nombreText;
    private JButton buscarButton;

    private Paciente pacienteSeleccionado;
    private DefaultTableModel tableModel;

    public AgregaPacienteDialog() {
        setContentPane(contentPane);
        setModal(true);
        setSize(700, 500);
        setLocationRelativeTo(null);


        configurarTabla();
        cargarPacientes(Service.instance().getPacientes());
        setupListeners();
    }


    private void configurarTabla() {
        tableModel = new DefaultTableModel(new Object[]{"ID", "Nombre", "Teléfono"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table1.setModel(tableModel);
        table1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    }
    private void setupListeners() {

        agregarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    onOK();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        cancelarButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        buscarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filtrarPacientes();
            }
        });

        table1.getSelectionModel().addListSelectionListener(e -> {
            int fila = table1.getSelectedRow();
            if (fila != -1) {
                String id = table1.getValueAt(fila, 0).toString();
                Paciente p = null;
                try {
                    p = Service.instance().readPaciente(id);
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
                if (p != null) {
                    nombreText.setText(p.getNombre());
                }
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void cargarPacientes(List<Paciente> pacientes) {
        tableModel.setRowCount(0);
        for (Paciente p : pacientes) {
            tableModel.addRow(new Object[]{p.getId(), p.getNombre(), p.getNumeroTelefono()});
        }
    }

    private void filtrarPacientes() {
        String texto = nombreText.getText().trim().toLowerCase();
        List<Paciente> filtrados;

        if (texto.isEmpty()) {
            filtrados = Service.instance().getPacientes();
        } else {
            filtrados = Service.instance().searchPacientes( texto);
        }
        cargarPacientes(filtrados);
    }

    private void onOK() throws Exception {
        int filaSeleccionada = table1.getSelectedRow();
        if (filaSeleccionada != -1) {
            String id = table1.getValueAt(filaSeleccionada, 0).toString();
            pacienteSeleccionado = Service.instance().readPaciente(id);
        }
        dispose();
    }

    private void onCancel() {
        pacienteSeleccionado = null;
        dispose();
    }

    public Paciente getPacienteSeleccionado() {
        return pacienteSeleccionado;
    }
}
