package ch.ventoo.flux.server;

import ch.ventoo.flux.transport.Client;

import java.util.concurrent.BlockingQueue;

/**
 * A factory interface that is used to implement a factory returning ClientHandlers.
 */
public interface ClientHandlerFactory {

    public ClientHandler createHandler(BlockingQueue<Client> clientQueue);

}
