package ch.ventoo.flux.util;

import java.io.EOFException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;

/**
 * Created by nano on 25/10/14.
 */
public class NIOOutputStream extends OutputStream {

    private static final int BUFFER_SIZE = 8096;

    private final WritableByteChannel _out;
    private final byte[] _buffer;
    private final ByteBuffer _byteBuffer;

    private boolean _closed = false;
    private int _count = 0;

    public NIOOutputStream(WritableByteChannel out) {
        this(out, BUFFER_SIZE);
    }

    public NIOOutputStream(WritableByteChannel out, int size) {
        _out = out;
        _buffer = new byte[size];
        _byteBuffer = ByteBuffer.wrap(_buffer);
    }

    @Override
    public void write(int i) throws IOException {
        checkClosed();
        if(avaliableBufferToWrite() < 1) {
            flush();
        }
        _buffer[_count++] = (byte) i;
    }

    @Override
    public void flush() throws IOException {
        if(_count > 0 && _out != null) {
            _byteBuffer.position(0);
            _byteBuffer.limit(_count);
            write(_byteBuffer);
            _count = 0;
        }
    }

    public void write(ByteBuffer data) throws IOException {
        int remaining = data.remaining();
        long delay = 1;
        int lastWriteSize = -1;
        while(remaining > 0) {
            // Exponential back-off
            if(lastWriteSize == 0) {
                try {
                    Thread.sleep(delay);
                    delay *= 2;
                    if(delay > 1000) {
                        delay = 1000;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                delay = 1;
            }

            lastWriteSize = _out.write(data);
            remaining = data.remaining();
        }
    }

    @Override
    public void close() throws IOException {
        super.close();
        _closed = true;
    }

    protected void checkClosed() throws IOException {
        if(_closed) {
            throw new EOFException("Cannot write to the stream any more.");
        }
    }

    private int avaliableBufferToWrite() {
        return _buffer.length - _count;
    }
}
