package hospital.presentation;

import javax.swing.*;

public class Refresher {
    private final ThreadListener listener;
    private Thread hilo;
    private boolean condition = false;
    private volatile boolean running = false;
    private long c = 0;
    private static final int REFRESH_INTERVAL_MS = 3000; // 20 segundos


    public Refresher(ThreadListener listener) {
        this.listener = listener;
    }

    public void start() {
        if (condition) {
            System.out.println("[Refresher] Ya está corriendo");
            return;
        }

        condition = true;
        hilo = new Thread(() -> {
            System.out.println("[Refresher] Iniciado - actualizando cada 3 segundos");
            while (condition) {
                try {
                    Thread.sleep(REFRESH_INTERVAL_MS);
                    if (!running) refresh();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }, "Refresher-Main");
        hilo.setDaemon(true);
        hilo.start();
    }

    public void stop() {
        condition = false;
        if (hilo != null) hilo.interrupt();
        System.out.println("[Refresher] Detenido");
    }

    private void refresh() {
        running = true;
        long num = c++;
        new Thread(() -> {
            try {
                listener.refresh();  // Ejecuta el trabajo en segundo plano
            } catch (Exception e) {
                System.err.println("[Refresher] Error en refresh #" + num + ": " + e.getMessage());
            } finally {
                running = false;
            }
        }, "Refresher-Worker-" + num).start();
    }
}
