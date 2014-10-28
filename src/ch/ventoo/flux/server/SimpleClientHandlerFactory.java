package ch.ventoo.flux.server;

import ch.ventoo.flux.transport.Client;

import java.util.concurrent.BlockingQueue;

/**
 * Created by nano on 18/10/14.
 */
public class SimpleClientHandlerFactory implements ClientHandlerFactory {

    @Override
    public ClientHandler createHandler(BlockingQueue<Client> clientQueue) {
        SimpleClientHandler clientHandler = new SimpleClientHandler();
        clientHandler.init(clientQueue);
        return clientHandler;
    }
}
