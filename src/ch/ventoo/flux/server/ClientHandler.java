package ch.ventoo.flux.server;

import ch.ventoo.flux.transport.Client;

import java.util.concurrent.BlockingQueue;

/**
 * Created by nano on 15/10/14.
 */
public interface ClientHandler extends Runnable {

    public void init(BlockingQueue<Client> clientQueue);

}
