package hospital.logic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Server {
    private ServerSocket ss;
    private List<Worker> workers; //Nose si requiera "final" revisar

    private boolean continuar = true;
    private Service service;

    public Server() {
        try {
            ss = new ServerSocket(Protocol.PORT);
            workers = Collections.synchronizedList(new ArrayList<Worker>());
            service = Service.instance();
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

                int tipoConexion = is.readInt(); // Saber si es SYNC o ASYNC

                switch (tipoConexion) {
                    case Protocol.SYNC:
                        String sid = UUID.randomUUID().toString();    // Crear sesión única para el cliente
                        Worker worker = new Worker(this, s, os, is, sid, service);
                        workers.add(worker);
                        System.out.println("Nueva conexión SYNC. SID: " + sid);
                        os.writeObject(sid);
                        os.flush();
                        worker.start();
                        break;

                    case Protocol.ASYNC:
                        String sidAsync = (String) is.readObject(); // Registrar socket asíncrono de un worker existente
                        Worker existing = getWorkerBySid(sidAsync);
                        if (existing != null) {
                            existing.setAs(s, os, is);
                            System.out.println("Conexión ASYNC registrada para SID: " + sidAsync);
                        } else {
                            System.out.println("No se encontró worker para SID: " + sidAsync);
                            s.close();
                        }
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
                break;
            }
        }
    }

    public void deliver_message(Worker from, String message){
        for(Worker w:workers){
            if (w!=from) w.deliver_message(message);
        }
    }

    public Worker getWorkerBySid(String sid) {
        synchronized (workers) {
            for (Worker w : workers) {
                if (w.getSid().equals(sid)) return w;
            }
        }
        return null;
    }
}