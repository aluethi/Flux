package ch.ventoo.flux.server;

import ch.ventoo.flux.profiling.LogWrapper;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.ProtocolHandler;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.transport.Client;
import ch.ventoo.flux.transport.Frame;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

/**
 * Created by nano on 18/10/14.
 */
public class SimpleClientHandler implements ClientHandler {

    private static LogWrapper LOGGER = new LogWrapper(Server.class);

    private BlockingQueue<Client> _clientQueue = null;
    private boolean _stopped = false;

    @Override
    public void init(BlockingQueue<Client> clientQueue) {
        _clientQueue = clientQueue;
    }

    @Override
    public void run() {
        while(!isStopped()) {
            Client client = getNextClient();
            LOGGER.info("Reading frame...");
            Frame frame = client.readFrame();
            if(frame != null) {
                LOGGER.info("Parsing frame...");
                Command command = ProtocolHandler.parseFrame(frame);
                LOGGER.info("Executing command...");
                Response response = null;
                try {
                    response = command.execute();
                } catch (IOException e) {
                    e.printStackTrace(); // TODO: Log
                }
                LOGGER.info("Writing response...");
                Frame responseFrame = ProtocolHandler.parseResponse(response);
                client.writeFrame(responseFrame);
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
                e.printStackTrace();
                continue;
            }
        }
        return client;
    }

    public void handleFrame() {

    }

}
