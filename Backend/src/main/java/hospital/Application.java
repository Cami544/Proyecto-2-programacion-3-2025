package hospital;

import hospital.logic.Server;

public class Application {

    public static void main(String[] args) {
        Server server = new Server();
        server.run();
    }
}
