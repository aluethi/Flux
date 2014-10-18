package ch.ventoo.flux;

import ch.ventoo.flux.server.ServerMain;
import ch.ventoo.flux.transport.Server;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Flux {

    public static void main(String[] args) {

        Logger logger = Logger.getAnonymousLogger();
        // LOG this level to the log
        logger.setLevel(Level.FINER);

        ConsoleHandler handler = new ConsoleHandler();
        // PUBLISH this level
        handler.setLevel(Level.FINER);
        logger.addHandler(handler);

        logger.log(Level.FINE, "Starting server");

        Server server = new Server("0.0.0.0", 12345);
        try {
            server.bind();
            new Thread(server).start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public Flux() {

    }

    public void start() {

    }
}
