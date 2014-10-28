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
    private final WireFormat _format;

    public Client(SocketChannel channel) {
        _channel = channel;
        // TODO: Make configurable parameter
        _buffer = ByteBuffer.allocateDirect(8096);
        _format = new WireFormat();
    }

    public int read() throws IOException {
        int readBytes = 0;
        while(true) {
            int readSize = _channel.read(_buffer);
            readBytes += readSize;
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
        return readBytes;
    }

    public Frame readFrame() {
        Frame frame = _format.unmarshal(_buffer);
        return frame;
    }

    public void write(ByteBuffer buffer) throws IOException {
        _channel.write(buffer);
    }

    public void writeFrame(Frame frame) {
        _buffer.clear();
        _format.marshal(_buffer, frame);
        try {
            _channel.write(_buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void shutdown() {
    }
}
