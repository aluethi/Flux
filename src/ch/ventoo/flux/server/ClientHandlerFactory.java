package ch.ventoo.flux.server;

import ch.ventoo.flux.profiling.BenchLogger;
import ch.ventoo.flux.transport.Client;

import java.util.concurrent.BlockingQueue;

/**
 * Created by nano on 15/10/14.
 */
public interface ClientHandlerFactory {

    public ClientHandler createHandler(BlockingQueue<Client> clientQueue);

}
