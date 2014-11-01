package ch.ventoo.flux.server;

import ch.ventoo.flux.profiling.BenchLogger;
import ch.ventoo.flux.transport.Client;

import java.util.concurrent.BlockingQueue;

/**
 * A simple implementation of the ClientHandlerFactory that initializes ClientHandlers with a BenchLogger for benchmarking.
 */
public class SimpleClientHandlerFactory implements ClientHandlerFactory {

    public BenchLogger _log;

    public SimpleClientHandlerFactory(BenchLogger log) {
        _log = log;
    }

    /**
     * Returns a newly created SimpleClientHandler that is initialized with the clientQueue and a BenchLogger.
     * @param clientQueue
     * @return
     */
    @Override
    public ClientHandler createHandler(BlockingQueue<Client> clientQueue) {
        SimpleClientHandler clientHandler = new SimpleClientHandler();
        clientHandler.init(clientQueue, _log);
        return clientHandler;
    }
}
