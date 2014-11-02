package ch.ventoo.flux.transport;

import ch.ventoo.flux.config.Configuration;
import ch.ventoo.flux.profiling.LogWrapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Server-side representation of the client connection.
 */
public class Client {

    private static LogWrapper LOGGER = new LogWrapper(Client.class);

    private final SocketChannel _channel;
    private final ByteBuffer _buffer;
    private final WireFormat _format;

    public Client(SocketChannel channel) {
        _channel = channel;
        _buffer = ByteBuffer.allocateDirect(Integer.parseInt(Configuration.getProperty("mw.client.buffer")));
        _format = new WireFormat();
    }

    /**
     * Reads inbound bytes from the network connection to the client and
     * demarshals the connection byte buffer into a frame.
     * @return
     * @throws IOException
     */
    public Frame readFrame() {
        try {
            int readBytes = 0, readSize;

            _buffer.clear();

            do {
                if ((readSize = _channel.read(_buffer)) < 0) {
                    LOGGER.warning("The connection has been closed.");
                    shutdown();
                    return null;
                }
                readBytes += readSize;
            } while (readBytes < 4);

            ByteBuffer header = _buffer.duplicate();
            header.flip();
            int frameSize = header.getInt();

            while(readBytes < frameSize + 4) {
                if ((readSize = _channel.read(_buffer)) < 0) {
                    LOGGER.warning("The connection has been closed.");
                    shutdown();
                    return null;
                }
                readBytes += readSize;
            }

            byte[] body = new byte[frameSize];
            _buffer.flip();
            _buffer.position(4);
            _buffer.get(body);

            return new Frame(frameSize, body);
        } catch (IOException e) {
            LOGGER.warning("The connection has been closed.");
            shutdown();
            return null;
        }
    }

    /**
     * Writes a byte buffer to the client connection.
     * @param buffer
     * @throws IOException
     */
    public void write(ByteBuffer buffer) throws IOException {
        _channel.write(buffer);
    }

    /**
     * Writes a frame to the client.
     * @param frame
     * @throws IOException
     */
    public void writeFrame(Frame frame) throws IOException {
        _buffer.clear();
        _format.marshal(_buffer, frame);
        _buffer.flip();
        try {
            _channel.write(_buffer);
        } catch (IOException e) {
            LOGGER.warning("There was an exception while writing to the client.");
        }
        _buffer.clear();
    }

    public void shutdown() {
        LOGGER.info("Shutting down client connection.");
        try {
            _channel.close();
        } catch (IOException e) {
            LOGGER.severe("Could not close the client connection.");
            throw new RuntimeException(e);
        }
    }
}
