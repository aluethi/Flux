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
 * Created by nano on 18/10/14.
 */
public class SimpleClientHandler implements ClientHandler {

    private static LogWrapper LOGGER = new LogWrapper(Server.class);

    private BlockingQueue<Client> _clientQueue = null;
    private Timing _timing;
    private boolean _stopped = false;

    @Override
    public void init(BlockingQueue<Client> clientQueue, BenchLogger log) {
        _clientQueue = clientQueue;
        _timing = new Timing(log);
    }

    @Override
    public void run() {
        while(!isStopped()) {
            _timing.enterRegion(Timing.Region.WAITING);
            Client client = getNextClient();
            _timing.enterRegion(Timing.Region.MARSHALLING);
            Frame frame = null;
            try {
                frame = client.readFrame();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if(frame != null) {
                Command command = ProtocolHandler.parseCommand(frame);
                Response response = null;
                _timing.enterRegion(Timing.Region.DATABASE);
                try {
                    response = command.execute();
                } catch (IOException e) {
                    e.printStackTrace(); // TODO: Log
                }
                _timing.enterRegion(Timing.Region.RESPONSE);
                Frame responseFrame = ProtocolHandler.prepareResponse(response);
                try {
                    client.writeFrame(responseFrame);
                } catch (IOException e) {
                    e.printStackTrace();
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
                e.printStackTrace();
                continue;
            }
        }
        return client;
    }

}
