package ch.ventoo.flux.transport;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

/**
 * Created by nano on 09/10/14.
 */
public class Client {

    private final SocketChannel _channel;
    private final ByteBuffer _buffer;

    public Client(SocketChannel channel) {
        _channel = channel;
        // TODO: Make configurable parameter
        _buffer = ByteBuffer.allocateDirect(8096);
    }

    public int read() throws IOException {
        int readSize = 0;
        while(true) {
            readSize += _channel.read(_buffer);
            if(readSize == -1) {
                // TODO: Throw an exception
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
        return readSize;
    }

    public void send(ByteBuffer buffer) throws IOException {
        _channel.write(buffer);
    }

    public void shutdown() {
    }
}
