package ch.ventoo.flux.transport;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Created by nano on 02/10/14.
 */
public class Acceptor implements Runnable {

    public static final int DEFAULT_PORT = 12121;
    public static final String DEFAULT_HOST = "0.0.0.0";

    private final int _port;
    private final String _host;
    private Selector _selector = null;
    private ServerSocketChannel _serverChannel;
    private IAcceptListener _acceptListener;
    private boolean _stopped = false;

    private final boolean _useChannelQueue;
    private BlockingQueue<SelectionKey> _channelQueue = new LinkedBlockingDeque<SelectionKey>();
    private Thread _channelHandlerThread;

    public Acceptor(final String host, final int port) {
        this(host, port, false); // default to not using a channel queue
    }

    public Acceptor(final String host, final int port, final boolean useChannelQueue) {
        _host = (host == null) ? DEFAULT_HOST : host;
        _port = (port == 0) ? DEFAULT_PORT : port;
        _useChannelQueue = useChannelQueue;
    }

    public void bind() throws IOException {
        if(_selector == null)
            _selector = Selector.open();
        if(_serverChannel == null)
            _serverChannel = ServerSocketChannel.open();
        _serverChannel.configureBlocking(false);
        _serverChannel.socket().bind(new InetSocketAddress(_host, _port));
        _serverChannel.register(_selector, SelectionKey.OP_ACCEPT);
    }

    public void stop() {
        if(_channelHandlerThread != null) {
            _channelHandlerThread.interrupt();
        }
        _stopped = true;
    }

    public boolean isStopped() {
        return _stopped;
    }

    public void setAcceptListener(IAcceptListener listener) {
        _acceptListener = listener;
    }

    public IAcceptListener getAcceptListener() {
        if(_acceptListener == null); // Exception!
        return _acceptListener;
    }

    private void prepareStart() {
        if(_useChannelQueue) {
            Runnable channelHandler = new Runnable() {
                @Override
                public void run() {
                    try {
                        while(!isStopped()) {
                            SelectionKey key = _channelQueue.poll(100, TimeUnit.MILLISECONDS);
                            if(key != null) {
                                handleChannel(key);
                            }
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            _channelHandlerThread = new Thread(channelHandler, "[Flex] Accept channel handler");
            _channelHandlerThread.start();
        }
    }

    @Override
    public void run() {
        prepareStart();
        try {
            while(!isStopped()) {
                _selector.select();
                Set<SelectionKey> selected = _selector.selectedKeys();
                Iterator<SelectionKey> iter = selected.iterator();
                while(iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    if(_useChannelQueue) {
                        _channelQueue.put(key);
                    } else {
                        handleChannel(key);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void handleChannel(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverSocket.accept();
        channel.configureBlocking(false);
        getAcceptListener().onAccept(channel);
    }
}
