package ch.ventoo.flux.server;

import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.ProtocolHandler;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.WireFormat;
import ch.ventoo.flux.transport.Client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.BlockingQueue;

/**
 * Created by nano on 09/10/14.
 */
public class ConnectionHandler implements Runnable {

    private boolean _stopped = true;

    private final BlockingQueue<Client> _clientQueue;
    private final ProtocolHandler _protocolHandler;
    private final WireFormat _format;

    public ConnectionHandler(final BlockingQueue<Client> clientQueue, ProtocolHandler protocolHandler, WireFormat format) {
        _clientQueue = clientQueue;
        _protocolHandler = protocolHandler;
        _format = format;
    }

    public void start() {
        _stopped = false;
    }

    @Override
    public void run() {
        while(!isStopped()) {
            try {
                Client con = _clientQueue.take();
                ByteBuffer buffer = con.receive();
                Command command = _format.unmarshal(buffer);
                Response response = _protocolHandler.handle(command);
                _format.marshal(response, buffer);
                con.send(buffer);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void stop() {
        _stopped = true;
    }

    public boolean isStopped() {
        return _stopped;
    }
}
