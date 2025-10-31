package hospital.presentation.Usuario;

import hospital.logic.Usuario;
import hospital.presentation.ThreadListener;

import javax.swing.*;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

public class View implements PropertyChangeListener, ThreadListener {
    private JPanel panel;
    private JTable usuariosTable;
    private JButton enviarButton;
    private JButton recibirButton;
    private JLabel tituloLabel;

    private Model model;
    private Controller controller;

    public View() {
        inicializarComponentes();
        setupEventHandlers();
    }

    private void inicializarComponentes() {
        panel = new JPanel(new BorderLayout(5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tituloLabel = new JLabel("Usuarios", SwingConstants.CENTER);
        tituloLabel.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(tituloLabel, BorderLayout.NORTH);

        usuariosTable = new JTable();
        usuariosTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(usuariosTable);
        scrollPane.setPreferredSize(new Dimension(200, 400));
        panel.add(scrollPane, BorderLayout.CENTER);

        JPanel botonesPanel = new JPanel(new GridLayout(1, 2, 5, 0));
        enviarButton = new JButton("Enviar");
        recibirButton = new JButton("Recibir");
        botonesPanel.add(enviarButton);
        botonesPanel.add(recibirButton);
        panel.add(botonesPanel, BorderLayout.SOUTH);
    }

    private void setupEventHandlers() {
        enviarButton.addActionListener(e -> enviarMensaje());
        recibirButton.addActionListener(e -> recibirMensaje());

        usuariosTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 1) {
                    int row = usuariosTable.getSelectedRow();
                    if (row >= 0) {
                        TableModel tableModel = (TableModel) usuariosTable.getModel();
                        Usuario usuario = tableModel.getRowAt(row);
                        controller.seleccionarUsuario(usuario);
                    }
                }
            }
        });
    }

    private void enviarMensaje() {
        if (model.getUsuarioSeleccionado() == null) {
            JOptionPane.showMessageDialog(panel,
                    "Debe seleccionar un usuario destinatario",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String mensaje = JOptionPane.showInputDialog(panel,
                "Ingrese el mensaje para " + model.getUsuarioSeleccionado().getId() + ":",
                "Enviar Mensaje",
                JOptionPane.PLAIN_MESSAGE);

        if (mensaje != null && !mensaje.trim().isEmpty()) {
            try {
                controller.enviarMensaje(model.getUsuarioSeleccionado(), mensaje);
                JOptionPane.showMessageDialog(panel,
                        "Mensaje enviado exitosamente",
                        "Éxito",
                        JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(panel,
                        ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void recibirMensaje() {
        if (model.getUsuarioSeleccionado() == null) {
            JOptionPane.showMessageDialog(panel,
                    "Debe seleccionar un usuario remitente",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            controller.mostrarMensajesDe(model.getUsuarioSeleccionado());
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(panel,
                    ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void deliver_message(String mensaje) {
        // Este método es llamado por el SocketListener cuando llega una notificación
        controller.procesarNotificacion(mensaje);
    }

    @Override
    public void refresh() {
        // No usar refresco periódico para usuarios; se actualiza por notificaciones async
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

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        switch (evt.getPropertyName()) {
            case Model.USUARIOS_ACTIVOS:
                actualizarTabla();
                break;
            case Model.USUARIO_SELECCIONADO:
                break;
        }
        panel.revalidate();
        panel.repaint();
    }

    private void actualizarTabla() {
        int[] cols = {TableModel.ID, TableModel.MENSAJES};
        TableModel tableModel = new TableModel(cols, model.getUsuariosActivos());
        tableModel.setController(controller);
        usuariosTable.setModel(tableModel);
        usuariosTable.setRowHeight(25);

        if (usuariosTable.getColumnModel().getColumnCount() > 0) {
            TableColumnModel columnModel = usuariosTable.getColumnModel();
            columnModel.getColumn(0).setPreferredWidth(120);
            columnModel.getColumn(1).setPreferredWidth(80);
        }
    }
}