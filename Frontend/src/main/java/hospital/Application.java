package hospital;

import hospital.logic.Sesion;
import hospital.presentation.Refresher;
import hospital.presentation.ThreadListener;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import java.awt.*;

public class Application {

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            initializeControllers();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        window = new JFrame();
        window.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Detener el refresher si existe
                if (refresher != null) {
                    refresher.stop();
                }
                hospital.logic.Service.instance().stop();
                System.exit(0);
            }
        });

        window.setSize(1350, 600);
        window.setResizable(true);
        window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        window.setTitle("HOSPITAL - Sistema de Prescripcion y Despacho");
        window.setLocationRelativeTo(null);

        doLogin();
    }

    private static void doLogin() {
        hospital.presentation.Login.Model loginModel = new hospital.presentation.Login.Model();
        hospital.presentation.Login.View loginView = new hospital.presentation.Login.View();
        hospital.presentation.Login.Controller loginController = new hospital.presentation.Login.Controller(loginView, loginModel);

        loginView.showDialog();
    }

    public static void doRun() {
        tabbedPane = new JTabbedPane();

        // Crear panel principal con BorderLayout
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        mainPanel.add(usuarioView.getPanel(), BorderLayout.EAST);
        window.setContentPane(mainPanel);

        createMenuBar();

        String rol = Sesion.getUsuario() != null ? Sesion.getUsuario().getRol() : "ADM";

        window.setTitle("Recetas - " + Sesion.getUsuario().getId() + " (" + Sesion.getUsuario().getRol() + ")");

        switch (rol) {
            case "ADM":
                tabbedPane.addTab("Medicos", medicosIcon, medicoView.getPanel());
                tabbedPane.addTab("Farmaceutas", farmaceutasIcon, farmaceutaView.getPanel());
                tabbedPane.addTab("Pacientes", pacientesIcon, pacienteView.getPanel());
                tabbedPane.addTab("Medicamentos", medicamentosIcon, medicamentoView.getPanel());
                tabbedPane.addTab("Preescribir", preescribirIcon, preescribirView.getPanel());
                tabbedPane.addTab("Despachos", despachosIcon, despachoView.getPanel());
                tabbedPane.addTab("Dashboard", estadisticasIcon, dashboardView.getPanel());
                tabbedPane.addTab("Historico", historicoIcon, historicoView.getPanel());
                break;

            case "MED":
                tabbedPane.addTab("Preescribir", preescribirIcon, preescribirView.getPanel());
                tabbedPane.addTab("Dashboard", estadisticasIcon, dashboardView.getPanel());
                tabbedPane.addTab("Historico", historicoIcon, historicoView.getPanel());
                break;

            case "FAR":
                tabbedPane.addTab("Despachos", despachosIcon, despachoView.getPanel());
                tabbedPane.addTab("Dashboard", estadisticasIcon, dashboardView.getPanel());
                tabbedPane.addTab("Historico", historicoIcon, historicoView.getPanel());
                break;

            default:
                tabbedPane.addTab("Medicos", medicoView.getPanel());
                tabbedPane.addTab("Pacientes", pacienteView.getPanel());
                tabbedPane.addTab("Farmaceutas", farmaceutaView.getPanel());
                tabbedPane.addTab("Medicamentos", medicamentoView.getPanel());
                tabbedPane.addTab("Preescribir", preescribirView.getPanel());
                tabbedPane.addTab("Despachos", despachoView.getPanel());
                tabbedPane.addTab("Dashboard", dashboardView.getPanel());
                tabbedPane.addTab("Historico", historicoView.getPanel());
                break;
        }

        // ============================================
        // PASO 1: INICIAR SocketListener (para mensajes entre usuarios)
        // ============================================
        try {
            String sid = hospital.logic.Service.instance().getSid();
            socketListener = new hospital.presentation.SocketListener(usuarioView, sid);
            socketListener.start();
            System.out.println(" SocketListener iniciado con SID: " + sid);
            // La lista de usuarios se actualiza automáticamente mediante Refresher
        } catch (Exception ex) {
            System.err.println(" Error iniciando SocketListener: " + ex.getMessage());
        }

        // Añadir al usuario actual para que se vea a sí mismo en la lista (los demás llegan por notificación)
        try {
            if (Sesion.getUsuario() != null && usuarioController != null) {
                usuarioController.agregarUsuarioActual();
            }
        } catch (Exception ignore) {}

        // Sincronizar lista inicial de conectados ya presentes en el servidor
        try {
            java.util.List<String> conectados = hospital.logic.Service.instance().getUsuariosConectados();
            if (usuarioController != null && conectados != null) {
                usuarioController.sincronizarUsuariosIniciales(conectados);
            }
        } catch (Exception ex) {
            System.err.println("No se pudo sincronizar usuarios iniciales: " + ex.getMessage());
        }

        // ============================================
        // PASO 2: INICIAR REFRESHER para actualizar TODAS las tablas (EXCEPTO usuarios)
        // ============================================
        iniciarRefresher();

        window.setVisible(true);
    }

    /**
     * Inicializa el Refresher que actualiza TODAS las vistas cada 2 segundos
     */
    private static void iniciarRefresher() {
        ThreadListener compositeListener = new ThreadListener() {
            @Override
            public void refresh() {
                if (tabbedPane == null) return;

                Component selected = tabbedPane.getSelectedComponent();
                if (selected == null) return;

                // Solo refrescar la pestaña visible (ejecución segura en background)
                try {
                    if (selected == medicoView.getPanel()) medicoView.refresh();
                    else if (selected == pacienteView.getPanel()) pacienteView.refresh();
                    else if (selected == farmaceutaView.getPanel()) farmaceutaView.refresh();
                    else if (selected == medicamentoView.getPanel()) medicamentoView.refresh();
                    else if (selected == dashboardView.getPanel()) dashboardView.refresh();
                    else if (selected == historicoView.getPanel()) historicoView.refresh();
                    else if (selected == preescribirView.getPanel()) preescribirView.refresh();
                    else if (selected == despachoView.getPanel()) despachoView.refresh();
                } catch (Exception ex) {
                    System.err.println("[Refresher] Error refrescando pestaña visible: " + ex.getMessage());
                }
            }

            @Override
            public void deliver_message(String message) {
                // No necesario aquí
            }
        };

        // 🔹 Creamos y lanzamos el refresher global (solo refresca la pestaña activa)
        refresher = new Refresher(compositeListener);
        refresher.start();

        System.out.println(" Refresher iniciado - refrescando solo pestaña visible cada 3 segundos");

        // 🔹 Opcional: detener / reanudar cuando cambies de pestaña
        tabbedPane.addChangeListener(e -> {
            Component selected = tabbedPane.getSelectedComponent();
            if (selected == null) return;

            String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
            System.out.println("[UI] Pestaña actual: " + title);
        });
    }


    private static void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu usuarioMenu = getJMenu();
        menuBar.add(usuarioMenu);

        window.setJMenuBar(menuBar);
    }

    private static JMenu getJMenu() {
        JMenu usuarioMenu = new JMenu("Usuario");

        JMenuItem logoutItem = new JMenuItem("Cerrar Sesion");
        logoutItem.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(window,
                    "Esta seguro de cerrar sesion?",
                    "Confirmar",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                // Detener Refresher
                if (refresher != null) {
                    refresher.stop();
                }

                // Detener SocketListener
                try {
                    if (socketListener != null) {
                        socketListener.stop();
                    }
                    // La lista de usuarios se actualiza automáticamente mediante Refresher
                } catch (Exception ex) {
                    System.err.println("Error en logout: " + ex.getMessage());
                }

                Sesion.logout();
                window.dispose();

                window = new JFrame();
                window.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        hospital.logic.Service.instance().stop();
                        System.exit(0);
                    }
                });
                window.setSize(1350, 600);
                window.setResizable(true);
                window.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
                window.setTitle("HOSPITAL - Sistema de Prescripcion y Despacho");
                window.setLocationRelativeTo(null);

                doLogin();
            }
        });

        usuarioMenu.add(logoutItem);
        return usuarioMenu;
    }

    private static String getRolDescripcion(String rol) {
        switch (rol) {
            case "ADM":
                return "Administrador";
            case "MED":
                return "Medico";
            case "FAR":
                return "Farmaceuta";
            case "PAC":
                return "Paciente";
            default:
                return rol;
        }
    }

    private static void initializeControllers() throws Exception {
        hospital.presentation.Paciente.Model pacienteModel = new hospital.presentation.Paciente.Model();
        pacienteView = new hospital.presentation.Paciente.View();
        pacientesControllers = new hospital.presentation.Paciente.Controller(pacienteView, pacienteModel);

        hospital.presentation.Medico.Model medicoModel = new hospital.presentation.Medico.Model();
        medicoView = new hospital.presentation.Medico.View();
        medicoControllers = new hospital.presentation.Medico.Controller(medicoView, medicoModel);

        hospital.presentation.Farmaceuta.Model farmaceutaModel = new hospital.presentation.Farmaceuta.Model();
        farmaceutaView = new hospital.presentation.Farmaceuta.View();
        farmaceutaControllers = new hospital.presentation.Farmaceuta.Controller(farmaceutaView, farmaceutaModel);

        hospital.presentation.Medicamento.Model medicamentoModel = new hospital.presentation.Medicamento.Model();
        medicamentoView = new hospital.presentation.Medicamento.View();
        medicamentoController = new hospital.presentation.Medicamento.Controller(medicamentoView, medicamentoModel);

        hospital.presentation.Dashboard.Model dashboardModel = new hospital.presentation.Dashboard.Model();
        dashboardView = new hospital.presentation.Dashboard.View();
        dashboardController = new hospital.presentation.Dashboard.Controller(dashboardView, dashboardModel);

        hospital.presentation.Historico.Model historicoModel = new hospital.presentation.Historico.Model();
        historicoView = new hospital.presentation.Historico.View();
        historicoController = new hospital.presentation.Historico.Controller(historicoView, historicoModel);

        hospital.presentation.Preescribir.Model preescribirModel = new hospital.presentation.Preescribir.Model();
        preescribirView = new hospital.presentation.Preescribir.View();
        preescribirController = new hospital.presentation.Preescribir.Controller(preescribirView, preescribirModel);

        hospital.presentation.Despacho.Model despachoModel = new hospital.presentation.Despacho.Model();
        despachoView = new hospital.presentation.Despacho.View();
        despachoController = new hospital.presentation.Despacho.Controller(despachoView, despachoModel);

        // Inicializar módulo de Usuarios
        hospital.presentation.Usuario.Model usuarioModel = new hospital.presentation.Usuario.Model();
        usuarioView = new hospital.presentation.Usuario.View();
        usuarioController = new hospital.presentation.Usuario.Controller(usuarioView, usuarioModel);
    }

    public static hospital.presentation.Paciente.Controller pacientesControllers;
    public static hospital.presentation.Medico.Controller medicoControllers;
    public static hospital.presentation.Farmaceuta.Controller farmaceutaControllers;
    public static hospital.presentation.Medicamento.Controller medicamentoController;
    public static hospital.presentation.Dashboard.Controller dashboardController;
    public static hospital.presentation.Historico.Controller historicoController;
    public static hospital.presentation.Preescribir.Controller preescribirController;
    public static hospital.presentation.Despacho.Controller despachoController;
    public static hospital.presentation.Usuario.Controller usuarioController;

    private static hospital.presentation.Paciente.View pacienteView;
    private static hospital.presentation.Medico.View medicoView;
    private static hospital.presentation.Farmaceuta.View farmaceutaView;
    private static hospital.presentation.Medicamento.View medicamentoView;
    private static hospital.presentation.Dashboard.View dashboardView;
    private static hospital.presentation.Historico.View historicoView;
    private static hospital.presentation.Preescribir.View preescribirView;
    private static hospital.presentation.Despacho.View despachoView;
    private static hospital.presentation.Usuario.View usuarioView;
    private static hospital.presentation.SocketListener socketListener;
    private static javax.swing.JTabbedPane tabbedPane;

    private static Refresher refresher;

    private static ImageIcon medicosIcon = new ImageIcon(Application.class.getResource("/icons/icons8-care-16.png"));
    private static ImageIcon farmaceutasIcon = new ImageIcon(Application.class.getResource("/icons/icons8-pharmacist-16.png"));
    private static ImageIcon pacientesIcon = new ImageIcon(Application.class.getResource("/icons/icons8-sick-16.png"));
    private static ImageIcon medicamentosIcon = new ImageIcon(Application.class.getResource("/icons/icons8-pill-bottle-16.png"));
    private static ImageIcon estadisticasIcon = new ImageIcon(Application.class.getResource("/icons/icons8-health-graph-16.png"));
    private static ImageIcon historicoIcon = new ImageIcon(Application.class.getResource("/icons/icons8-medical-history-16.png"));
    private static ImageIcon preescribirIcon = new ImageIcon(Application.class.getResource("/icons/icons8-treatment-16.png"));
    private static ImageIcon despachosIcon = new ImageIcon(Application.class.getResource("/icons/icons8-check-16.png"));

    public static JFrame window;
    public final static int MODE_CREATE = 1;
    public final static int MODE_EDIT = 2;

    public static final Color BACKGROUND_ERROR = new Color(255, 102, 102);
}