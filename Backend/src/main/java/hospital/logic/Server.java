package hospital.logic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Server {
    ServerSocket ss;
    List<Worker> workers;
    public Server() {
        try {
            ss = new ServerSocket(Protocol.PORT);
            workers = Collections.synchronizedList(new ArrayList<Worker>());
            System.out.println("Servidor iniciado...");
        } catch (IOException ex) { System.out.println(ex);}
    }
    public void run() {
        Service service = Service.instance();
        boolean continuar = true;
        Socket s;
        Worker worker;
        while (continuar) {
            try {
                s = ss.accept();
                System.out.println("Conexion Establecida...");
                worker = new Worker(this, s, service);
                workers.add(worker);
                System.out.println("Quedan: " + workers.size());
                worker.start();
            } catch (Exception ex) { System.out.println(ex);}
        }
    }

    public void remove(Worker w) {
        workers.remove(w);
        System.out.println("Quedan: " +workers.size());
    }
}