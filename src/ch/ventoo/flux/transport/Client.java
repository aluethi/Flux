package ch.ventoo.flux.transport;

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
        // TODO: Make configurable parameter
        _buffer = ByteBuffer.allocateDirect(8096);
        _format = new WireFormat();
    }

    /**
     * Reads inbound bytes from the network connection to the client.
     * @return
     * @throws IOException
     */
    public int read() throws IOException {
        int readBytes = 0;
        while(true) {
            int readSize = _channel.read(_buffer);
            readBytes += readSize;
            if(readSize == -1) {
                LOGGER.warning("The connection has been closed.");
                break;
            }

            if(readSize == 0) {
                break;
            }

            if(_buffer.hasRemaining()) {
                continue;
            }
        }
        _buffer.flip();
        return readBytes;
    }

    /**
     * Unmarshals the connection byte buffer into a frame.
     * @return
     * @throws IOException
     */
    public Frame readFrame() throws IOException {
        Frame frame = _format.unmarshal(_buffer);
        return frame;
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
    }

    public void shutdown() {
    }
}
