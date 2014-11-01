package ch.ventoo.flux.server;

import ch.ventoo.flux.profiling.LogWrapper;
import ch.ventoo.flux.transport.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Main server class of the Flux message passing system.
 */
public class Server implements Runnable {

    private static LogWrapper LOGGER = new LogWrapper(Server.class);

    private final String _host;
    private final int _port;

    private boolean _stopped = false;
    private Selector _selector = null;
    private ServerSocketChannel _serverChannel = null;
    // TODO: Make type and size of queue configurable
    private BlockingQueue<Client> _clientQueue = new ArrayBlockingQueue<Client>(1000);
    private ClientHandlerFactory _clientHandlerFactory = null;
    private Thread _handlerThreads[] = null;

    public Server(final String host, final int port) {
        _host = host;
        _port = port;
    }

    /**
     * Sets the client handler factory to use. Can be different for e.g. different protocols.
     * @param clientHandlerFactory
     */
    public void setClientHandlerFactory(ClientHandlerFactory clientHandlerFactory) {
        _clientHandlerFactory = clientHandlerFactory;
    }

    /**
     * Starts the given number of client handlers.
     * @param handlers
     */
    public void startHandlers(int handlers) {
        if(_clientHandlerFactory != null) {
            _handlerThreads = new Thread[handlers];
            for(int i = 0; i < handlers; i++) {
                _handlerThreads[i] = new Thread(_clientHandlerFactory.createHandler(_clientQueue));
                _handlerThreads[i].start();
            }
        }
    }

    /**
     * Bind the Flux server to the given host interface and port.
     * @throws IOException
     */
    public void bind() throws IOException {
        if(_selector == null) {
            _selector = Selector.open();
        }
        if(_serverChannel == null) {
            _serverChannel = ServerSocketChannel.open();
        }
        _serverChannel.configureBlocking(false);
        _serverChannel.socket().bind(new InetSocketAddress(_host, _port));
        _serverChannel.register(_selector, SelectionKey.OP_ACCEPT);

        LOGGER.info("Bound server to " + _host + ":" + _port);
    }

    /**
     * Run loop that handles acceptions and reads from the network interface.
     */
    @Override
    public void run() {
        try {
            while(!isStopped()) {
                _selector.select();
                Set<SelectionKey> selectedKeys = _selector.selectedKeys();
                Iterator<SelectionKey> iter = selectedKeys.iterator();
                while(iter.hasNext()) {
                    SelectionKey key = iter.next();
                    iter.remove();

                    if(key.isAcceptable()) {
                        handleAccept(key);
                    } else if(key.isReadable()) {
                        handleRead(key);
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.severe("Error while executing select on the server socket channel.");
            throw new RuntimeException(e);
        }
    }

    public void shutdown() {
        _stopped = true;
    }

    public boolean isStopped() {
        return _stopped;
    }

    /**
     * Handles an accept on the network interface.
     * @param key
     * @throws IOException
     */
    protected void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel channel = serverChannel.accept();
        channel.configureBlocking(false);
        Client con = new Client(channel);
        channel.register(_selector, SelectionKey.OP_READ, con);
    }

    /**
     * Handles a read on the network interface.
     * @param key
     */
    protected void handleRead(SelectionKey key) {
        Client client = (Client) key.attachment();
        try {
            int readSize = 0;
            readSize = client.read();
            if(readSize > 0) {
                _clientQueue.put(client);
            } else {
                client.shutdown();
            }
        } catch (IOException e) {
            LOGGER.warning("Error while reading from the client.");
        } catch (InterruptedException e) {
            LOGGER.severe("Interruption while putting a client into the client queue.");
        }
    }
}
