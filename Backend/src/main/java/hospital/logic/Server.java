package hospital.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
                        sid = s.getRemoteSocketAddress().toString();
                        Worker worker = new Worker(this, s, os, is, sid, service);
                        workers.add(worker);
                        worker.start();
                        System.out.println("Nueva conexión SYNC. SID: " + sid);
                        os.writeObject(sid);
                        os.flush();
                        break;

                    case Protocol.ASYNC:
                        sid= (String) is.readObject();
                        join(s, os, is, sid);
                        break;
                }
            } catch (Exception ex) {
                System.out.println("Error en conexión: " + ex.getMessage());
            }
        }
    }

    public void stop() {
        continuar = false;
        try {
            ss.close();
            for (Worker w : workers) w.stop();
        } catch (IOException e) {
            System.out.println("Error al cerrar servidor: " + e.getMessage());
        }
    }

    public void remove(Worker w) {
        workers.remove(w);
        System.out.println("Quedan: " +workers.size());
    }

    public void join(Socket as,ObjectOutputStream aos, ObjectInputStream ais, String sid){
        for(Worker w:workers){
            if(w.sid.equals(sid)){
                w.setAs(as,aos,ais);
                System.out.println("[Server] Canal asíncrono asociado a " + sid);
                break;
            }
        }
    }

    // Broadcast a todos excepto al remitente (para notificaciones generales)
    public synchronized void deliver_message(Worker from, String message){
        for(Worker w:workers) {
            if (w != from && w.isAsyncReady()) {
                w.deliverMessage(message);
            }
        }
    }

    // Enviar mensaje a un usuario específico
    public synchronized void deliver_message_to_user(String destinatarioId, String message){
        System.out.println("[Server] Enviando mensaje a usuario: " + destinatarioId);
        for(Worker w:workers) {
            if (w.getUserId() != null && w.getUserId().equals(destinatarioId) && w.isAsyncReady()) {
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
        List<String> usuarios = new ArrayList<>();
        for (Worker w : workers) {
            if (w.getUserId() != null) {
                usuarios.add(w.getUserId());
            }
        }
        return usuarios;
    }
}