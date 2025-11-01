package hospital.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {
    private ServerSocket ss;
    private List<Worker> workers;

    private boolean continuar = true;
    private Service service;

    public Server() {
        try {
            ss = new ServerSocket(Protocol.PORT);
            workers = Collections.synchronizedList(new ArrayList<Worker>());
            service = new Service();
            System.out.println("Servidor iniciado en puerto " + Protocol.PORT + "...");
        } catch (IOException ex) {
            System.out.println("Error iniciando servidor: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public void run() {
        while (continuar) {
            try {
                Socket s = ss.accept();
                ObjectOutputStream os = new ObjectOutputStream(s.getOutputStream());
                ObjectInputStream is = new ObjectInputStream(s.getInputStream());
                String sid;
                int tipoConexion = is.readInt();

                switch (tipoConexion) {
                    case Protocol.SYNC:
                        sid =  UUID.randomUUID().toString();
                        Worker worker = new Worker(this, s, os, is, sid, service);
                        synchronized (workers) {
                            workers.add(worker);
                        }
                        os.writeObject(sid);
                        os.flush();
                        worker.start();
                        System.out.println("[Server] Nueva conexión SYNC. SID: " + sid);

                        break;

                    case Protocol.ASYNC:
                        sid = (String) is.readObject();
                        join(s, os, is, sid);
                        break;
                    default: {
                        System.out.println("[Server][WARN] Tipo de conexión desconocido: " + tipoConexion);
                        s.close();
                    }
                }
            } catch (Exception ex) {
                if (continuar)
                    System.out.println("[Server][ERROR] Fallo al aceptar conexión: " + ex.getMessage());
            }
        }
        System.out.println("[Server] Hilo principal detenido.");
    }

    public void stop() {
        continuar = false;
        try {
            if (ss != null && !ss.isClosed()) ss.close();
        } catch (IOException e) {
            System.out.println("[Server][ERROR] No se pudo cerrar socket: " + e.getMessage());
        }

        synchronized (workers) {
            for (Worker w : new ArrayList<>(workers)) {
                try {
                    w.stop(); // detiene el Worker y cierra conexiones
                } catch (Exception ignored) {}
            }
            workers.clear();
        }
        System.out.println("[Server] Servidor detenido y todas las conexiones cerradas.");
    }

    public void remove(Worker w) {
        synchronized (workers) {
            workers.remove(w);
            System.out.println("[Server] Worker eliminado. Quedan: " + workers.size());
        }
    }

    public void join(Socket as, ObjectOutputStream aos, ObjectInputStream ais, String sid) {
        boolean found = false;
        synchronized (workers) {
            for (Worker w : workers) {
                if (w.sid.equals(sid)) {
                    w.setAs(as, aos, ais);
                    System.out.println("[Server] Canal asíncrono asociado a " + sid);
                    found = true;
                    break;
                }
            }
        }
        if (!found) {
            System.out.println("[Server][WARN] No se encontró Worker para SID: " + sid);
        }
    }



    // Broadcast a todos excepto al remitente (para notificaciones generales)
    public synchronized void deliver_message(Worker from, String message) {
        synchronized (workers) {
            for (Worker w : new ArrayList<>(workers)) {
                if (w != from) {
                    w.deliverMessage(message);
                }
            }
        }
    }

    // Enviar mensaje a un usuario específico
    public synchronized void deliver_message_to_user(String destinatarioId, String message) {
        System.out.println("[Server] Enviando mensaje a usuario: " + destinatarioId);
        for (Worker w : workers) {
            if (w.getUserId() != null && w.getUserId().equals(destinatarioId) ) {
                w.deliverMessage(message);
                System.out.println("[Server] Mensaje entregado a: " + destinatarioId);
                return;
            }
        }
        System.out.println("[Server] Usuario " + destinatarioId + " no encontrado o no conectado");
    }

    public Worker getWorkerBySid(String sid) {
        synchronized (workers) {
            for (Worker w : workers) {
                if (w.getSid().equals(sid)) return w;
            }
        }
        return null;
    }

    /**
     * Obtiene la lista de IDs de usuarios conectados (autenticados)
     */
    public synchronized List<String> getUsuariosConectados() {
        synchronized (workers) {
            List<String> usuarios = new ArrayList<>();
            for (Worker w : workers) {
                if (w.getUserId() != null) {
                    usuarios.add(w.getUserId());
                }
            }
            return usuarios;
        }
    }
}