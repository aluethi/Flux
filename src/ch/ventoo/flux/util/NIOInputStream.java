package ch.ventoo.flux.util;

import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * NIO ByteBuffer InputStream wrapper. (Inspired by Apache Active MQ)
 * Used to read from a ByteBuffer like it was a stream.
 */
public class NIOInputStream extends InputStream {
    private final ByteBuffer _input;

    public NIOInputStream(ByteBuffer input) {
        _input = input;
    }

    @Override
    public int read() {
        int rc = _input.get() & 0xff;
        return rc;
    }


    @Override
    public int read(byte b[], int off, int len) {
        if(_input.hasRemaining()) {
            int rc = Math.min(len, _input.remaining());
            _input.get(b, off, rc);
            return rc;
        } else {
            return len == 0 ? 0 : -1;
        }
    }

    @Override
    public long skip(long n) {
        int rc = Math.min((int)n, _input.remaining());
        _input.position(_input.position() + rc);
        return rc;
    }

    @Override
    public int available() {
        return _input.remaining();
    }

    @Override
    public boolean markSupported() {
        return false;
    }

    @Override
    public void close() {

    }
}
