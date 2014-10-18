package ch.ventoo.flux.transport;

import ch.ventoo.flux.profiling.LogWrapper;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

public class Acceptor implements Runnable {

    private static LogWrapper LOGGER = new LogWrapper(Acceptor.class);

    private final String _host;
    private final int _port;

    private boolean _stopped = true;

    private Selector _selector = null;
    private ServerSocketChannel _serverChannel = null;
    private AcceptListener _listener = null;

    public Acceptor(final String host, final int port) {
        _host = host;
        _port = port;
    }

    public void bind() throws IOException {
        if(_selector == null) {
            _selector = Selector.open();
        }
        if(_serverChannel == null) {
            _serverChannel = ServerSocketChannel.open();
        }

        _serverChannel.configureBlocking(false);
        _serverChannel.socket().bind(new InetSocketAddress(_host, _port));
        LOGGER.info("Acceptor bound to "+_host+":"+_port);
        _serverChannel.register(_selector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws IOException {
        _stopped = false;
        bind();
        LOGGER.fine("Acceptor started.");
    }

    @Override
    public void run() {
        try {
            while(!isStopped()) {
                _selector.select();
                LOGGER.info("Selector released.");
                Set<SelectionKey> selected = _selector.selectedKeys();
                Iterator<SelectionKey> iter = selected.iterator();
                while(iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();
                    handleAccept(key);
                }
            }
        } catch (IOException e) {
            // TODO: Do some logging
            e.printStackTrace();
        }
    }

    public void stop() {
        _stopped = true;
    }

    public boolean isStopped() {
        return _stopped;
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverSocket = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverSocket.accept();
        LOGGER.info("Accepted client on " + channel.socket().getRemoteSocketAddress());
        channel.configureBlocking(false);
        getAcceptListener().onAccept(channel);
    }

    public void setAcceptListener(AcceptListener listener) {
        _listener = listener;
    }

    public AcceptListener getAcceptListener() {
        if(_listener != null)
            return _listener;
        throw new NullPointerException();
    }
}