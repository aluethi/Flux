package ch.ventoo.flux.server;

import ch.ventoo.flux.profiling.BenchLogger;
import ch.ventoo.flux.transport.Client;

import java.util.concurrent.BlockingQueue;

/**
 * Interface to implement a client handler.
 */
public interface ClientHandler extends Runnable {

    public void init(BlockingQueue<Client> clientQueue, BenchLogger log);

}
