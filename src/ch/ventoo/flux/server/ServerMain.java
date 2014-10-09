package ch.ventoo.flux.server;

import ch.ventoo.flux.transport.AcceptListener;
import ch.ventoo.flux.transport.Acceptor;
import ch.ventoo.flux.transport.Connection;

import java.io.IOException;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Created by nano on 09/10/14.
 */
public class ServerMain implements Runnable {

    private final String _host;
    private final int _port;

    private boolean _stopped = true;

    private Selector _selector = null;
    private Acceptor _acceptor = null;
    private Thread _acceptorThread = null;
    // TODO: Make type and size of queue configurable
    private BlockingQueue<Connection> _clientQueue = new ArrayBlockingQueue<Connection>(1000);

    public ServerMain(String host, int port) {
        _host = host;
        _port = port;
    }

    public void start() throws IOException {
        if(_acceptor == null) {
            _acceptor = new Acceptor(_host, _port);
        }
        _acceptor.setAcceptListener(new AcceptListener() {
            @Override
            public void onAccept(SelectionKey key) {
                try {
                    Connection con = new Connection(key);
                    SelectableChannel channel = key.channel();
                    channel.register(_selector, SelectionKey.OP_READ, con);
                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }
        });
        _acceptor.start();
        _acceptorThread = new Thread(_acceptor);
        _acceptorThread.start();
        _stopped = false;
    }

    @Override
    public void run() {
        try {
            while(!isStopped()) {
                _selector.select();
                Set<SelectionKey> selected = _selector.selectedKeys();
                Iterator<SelectionKey> iter = selected.iterator();
                while(iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    handleRead(key);
                }
            }
        } catch (IOException e) {
            // TODO: Do some logging
        }
    }

    public void stop() {
        _acceptor.stop();
        _acceptorThread.interrupt();
        _stopped = true;
    }

    public boolean isStopped() {
        return _stopped;
    }

    private void handleRead(SelectionKey key) {
        Connection con = (Connection) key.attachment();
        _clientQueue.add(con);
    }
}
