package ch.ventoo.flux;

import ch.ventoo.flux.config.Configuration;
import ch.ventoo.flux.profiling.BenchLogger;
import ch.ventoo.flux.profiling.LogWrapper;
import ch.ventoo.flux.server.Server;
import ch.ventoo.flux.server.SimpleClientHandlerFactory;

import java.io.IOException;

public class Flux {

    private static LogWrapper LOGGER = new LogWrapper(Flux.class);

    private final Server _server;

    public static void main(String[] args) {
        Flux flux = new Flux(Configuration.getProperty("mw.iface.ip"), Integer.parseInt(Configuration.getProperty("mw.iface.port")));
        flux.start();
    }

    public Flux(String host, int port) {
        LOGGER.info("Starting server.");
        _server = new Server(host, port);
        _server.setClientHandlerFactory(new SimpleClientHandlerFactory(new BenchLogger("flux-server")));
    }

    public void start() {
        _server.startHandlers(Integer.parseInt(Configuration.getProperty("mw.handler")));
        try {
            _server.bind();
        } catch (IOException e) {
            LOGGER.severe("Could not bind server to given interface.");
            throw new RuntimeException();
        }
        new Thread(_server).start();
    }
}
