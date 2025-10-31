package hospital.presentation;

import javax.swing.*;

public class Refresher {
    ThreadListener listener;

    public Refresher(ThreadListener listener) {
        this.listener = listener;
    }

    private Thread hilo;
    private boolean condition = false;
    private volatile boolean running = false; // evita superposición de refresh

    public void start() {
        if (condition) {
            System.out.println("Refresher ya está corriendo");
            return;
        }

        Runnable task = new Runnable() {
            public void run() {
                while (condition) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                    if (!running) {
                        refresh();
                    }
                }
            }
        };
        hilo = new Thread(task);
        hilo.setDaemon(true);
        condition = true;
        hilo.start();
        System.out.println("Refresher iniciado - actualizando cada 3 segundos");
    }

    public void stop() {
        condition = false;
        if (hilo != null) {
            hilo.interrupt();
        }
        System.out.println("Refresher detenido");
    }

    long c = 0;
    private void refresh() {
        System.out.println("Refresh #" + c++);
        running = true;
        SwingUtilities.invokeLater(
                new Runnable() {
                    public void run() {
                        try {
                            listener.refresh();
                        } finally {
                            running = false;
                        }
                    }
                }
        );
    }
}