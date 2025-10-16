package hospital;

import hospital.logic.Sesion;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;
import java.awt.*;
import java.util.Objects;

public class Application {
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            initializeControllers(); // ← Moverlo aquí dentro del try
        } catch (Exception ex) {
            ex.printStackTrace(); // Para ver el error si ocurre
        }

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

    private static void doLogin() {
        hospital.presentation.Login.Model loginModel = new hospital.presentation.Login.Model();
        hospital.presentation.Login.View loginView = new hospital.presentation.Login.View();
        hospital.presentation.Login.Controller loginController = new hospital.presentation.Login.Controller(loginView, loginModel);

        loginView.showDialog();
    }

    public static void doRun() {
        JTabbedPane tabbedPane = new JTabbedPane();
        window.setContentPane(tabbedPane);

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

        window.setVisible(true);
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
                window.setSize(1200, 800);
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
    }

    public static hospital.presentation.Paciente.Controller pacientesControllers;
    public static hospital.presentation.Medico.Controller medicoControllers;
    public static hospital.presentation.Farmaceuta.Controller farmaceutaControllers;
    public static hospital.presentation.Medicamento.Controller medicamentoController;
    public static hospital.presentation.Dashboard.Controller dashboardController;
    public static hospital.presentation.Historico.Controller historicoController;
    public static hospital.presentation.Preescribir.Controller preescribirController;
    public static hospital.presentation.Despacho.Controller despachoController;

    private static hospital.presentation.Paciente.View pacienteView;
    private static hospital.presentation.Medico.View medicoView;
    private static hospital.presentation.Farmaceuta.View farmaceutaView;
    private static hospital.presentation.Medicamento.View medicamentoView;
    private static hospital.presentation.Dashboard.View dashboardView;
    private static hospital.presentation.Historico.View historicoView;
    private static hospital.presentation.Preescribir.View preescribirView;
    private static hospital.presentation.Despacho.View despachoView;

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
    /*
    
    Grupo de las 8am Progra 3

    Integrantes:
    Camila Fallas Jiménez  305510256
    Hermann Hidalgo Araya  118400891
    Jeancarlo Blanco Mora  703110431

    */