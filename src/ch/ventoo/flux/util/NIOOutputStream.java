package ch.ventoo.flux.util;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * NIO ByteBuffer OutputStream wrapper.
 * Used to write a stream to a ByteBuffer.
 */
public class NIOOutputStream extends OutputStream {

    private final ByteBuffer _output;

    public NIOOutputStream(ByteBuffer output) {
        _output = output;
    }

    @Override
    public void write(int i) throws IOException {
        _output.put((byte)(i & 0xff));
    }
}
