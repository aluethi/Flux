package ch.ventoo.flux.server;

import ch.ventoo.flux.profiling.BenchLogger;
import ch.ventoo.flux.profiling.LogWrapper;
import ch.ventoo.flux.profiling.Timing;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.ProtocolHandler;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.transport.Client;
import ch.ventoo.flux.transport.Frame;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * A simple implementation of the ClientHandler interface that uses a BenchLogger and the Timing class to
 * create timed log entries for benchmarking.
 */
public class SimpleClientHandler implements ClientHandler {

    private static LogWrapper LOGGER = new LogWrapper(SimpleClientHandler.class);

    private BlockingQueue<Client> _clientQueue = null;
    private Timing _timing;
    private boolean _stopped = false;

    /**
     * Initializes the client handler with the clientQueue and a BenchLogger.
     * @param clientQueue
     * @param log
     */
    @Override
    public void init(BlockingQueue<Client> clientQueue, BenchLogger log) {
        _clientQueue = clientQueue;
        _timing = new Timing(log);
    }

    /**
     * Run loop that processes client connections and measures the time used for each step.
     */
    @Override
    public void run() {
        while(!isStopped()) {
            _timing.enterRegion(Timing.Region.WAITING);
            Client client = getNextClient();
            _timing.enterRegion(Timing.Region.MARSHALLING);
            Frame frame = client.readFrame();
            if(frame != null) {
                Command command = ProtocolHandler.parseCommand(frame);
                Response response = null;
                _timing.enterRegion(Timing.Region.DATABASE);
                try {
                    response = command.execute();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                _timing.enterRegion(Timing.Region.RESPONSE);

                Frame responseFrame = ProtocolHandler.prepareResponse(response);
                try {
                    client.writeFrame(responseFrame);
                } catch (IOException e) {
                    LOGGER.warning("Could not write frame to client.");
                }
            }
        }
    }

    public boolean isStopped() {
        return _stopped;
    }

    protected Client getNextClient() {
        Client client = null;
        while(client == null) {
            try {
                client = _clientQueue.take();
            } catch (InterruptedException e) {
                LOGGER.warning("Thread was interrupted while waiting for a new queue.");
            }
        }
        return client;
    }

}
