package ch.ventoo.flux.transport;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by nano on 02/10/14.
 */
public class Server implements Runnable {

    private static int WORKERS = 0;

    private final int _port;
    private final String _host;

    private ExecutorService _executorService = null;
    private Acceptor _acceptor;
    private Selector _selector;
    private boolean _stopped = false;


    public Server(int workers, String host, int port) {
        _port = port;
        _host = host;
        _executorService = Executors.newFixedThreadPool(workers, new ThreadFactory() {
            @Override
            public Thread newThread(Runnable runnable) {
                return new Thread(runnable, "[Flex] Execution worker " + WORKERS++);
            }
        });
    }

    public void start() {
        _acceptor = new Acceptor(_host, _port);
        _acceptor.setAcceptListener(new IAcceptListener() {
            @Override
            public void onAccept(SocketChannel channel) {
                try {
                    channel.register(_selector, SelectionKey.OP_READ);

                } catch (ClosedChannelException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void stop() {
        _stopped = true;
    }

    public boolean isStopped() {
        return _stopped;
    }

    @Override
    public void run() {
        while(!isStopped()) {
            try {
                _selector.select();
                Set<SelectionKey> selected = _selector.selectedKeys();
                Iterator<SelectionKey> iter = selected.iterator();
                while(iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
