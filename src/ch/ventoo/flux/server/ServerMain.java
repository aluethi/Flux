package ch.ventoo.flux.server;

import ch.ventoo.flux.profiling.LogWrapper;
import ch.ventoo.flux.transport.AcceptListener;
import ch.ventoo.flux.transport.Acceptor;
import ch.ventoo.flux.transport.Client;

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
    private static LogWrapper LOGGER = new LogWrapper(Acceptor.class);

    private final String _host;
    private final int _port;
    private final Object _lock = new Object();

    private boolean _stopped = true;

    private Selector _selector = null;
    private Acceptor _acceptor = null;
    private Thread _acceptorThread = null;
    // TODO: Make type and size of queue configurable
    private BlockingQueue<Client> _clientQueue = new ArrayBlockingQueue<Client>(1000);

    public ServerMain(String host, int port) {
        _host = host;
        _port = port;
    }

    public void start() throws IOException {
        LOGGER.info("Starting ServerMain.");
        _selector = Selector.open();
        if(_acceptor == null) {
            _acceptor = new Acceptor(_host, _port);
        }
        _acceptor.setAcceptListener(new AcceptListener() {
            @Override
            public void onAccept(SocketChannel channel) {
                try {
                    Client con = new Client(channel);
                    synchronized (_lock) {
                        _selector.wakeup();
                    }
                    channel.register(_selector, SelectionKey.OP_READ, con);
                    LOGGER.info("Registered on R/W selector.");
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
                synchronized (_lock) {}
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
        Client con = (Client) key.attachment();
        LOGGER.info("Handling read.");
        try {
            _clientQueue.put(con);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
