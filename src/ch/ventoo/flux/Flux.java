package ch.ventoo.flux;

import ch.ventoo.flux.profiling.BenchLogger;
import ch.ventoo.flux.server.Server;
import ch.ventoo.flux.server.SimpleClientHandlerFactory;

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

        BenchLogger log = new BenchLogger("flux-server");
        Server server = new Server("0.0.0.0", 12345);
        SimpleClientHandlerFactory handlerFactory = new SimpleClientHandlerFactory(log);
        server.setClientHandlerFactory(handlerFactory);
        server.startHandlers(4);
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
