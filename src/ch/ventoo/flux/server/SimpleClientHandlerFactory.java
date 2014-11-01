package ch.ventoo.flux.server;

import ch.ventoo.flux.profiling.BenchLogger;
import ch.ventoo.flux.transport.Client;

import java.util.concurrent.BlockingQueue;

/**
 * Created by nano on 18/10/14.
 */
public class SimpleClientHandlerFactory implements ClientHandlerFactory {

    public BenchLogger _log;

    public SimpleClientHandlerFactory(BenchLogger log) {
        _log = log;
    }

    @Override
    public ClientHandler createHandler(BlockingQueue<Client> clientQueue) {
        SimpleClientHandler clientHandler = new SimpleClientHandler();
        clientHandler.init(clientQueue, _log);
        return clientHandler;
    }
}
