package hospital.presentation;

import hospital.logic.Protocol;
import hospital.presentation.ThreadListener;
import javax.swing.*;
import java.io.*;
import java.net.Socket;

public class SocketListener {
    ThreadListener listener;
    String sid;
    Socket as;
    ObjectOutputStream aos;
    ObjectInputStream ais;
    boolean condition = true;

    public SocketListener(ThreadListener listener, String sid) throws Exception {
        this.listener = listener;
        this.sid = sid;

        as = new Socket(Protocol.SERVER, Protocol.PORT);
        aos = new ObjectOutputStream(as.getOutputStream());
        ais = new ObjectInputStream(as.getInputStream());

        aos.writeInt(Protocol.ASYNC);
        aos.writeObject(sid);
        aos.flush();
    }

    public void start() {
        new Thread(this::listen).start();
    }

    public void stop() {
        condition = false;
    }

    public void listen() {
        int method;
        while (condition) {
            try {
                method = ais.readInt();
                switch (method) {
                    case Protocol.DELIVER_MESSAGE:
                        String msg = (String) ais.readObject();
                        SwingUtilities.invokeLater(() -> listener.deliver_message(msg));
                        break;
                }
            } catch (Exception e) {
                condition = false;
            }
        }
        try {
            as.shutdownOutput();
            as.close();
        } catch (IOException ignored) {}
    }
}

