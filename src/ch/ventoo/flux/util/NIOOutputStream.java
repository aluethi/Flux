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

    private final ByteBuffer _output;

    public NIOOutputStream(ByteBuffer output) {
        _output = output;
    }

    @Override
    public void write(int i) throws IOException {
        _output.put((byte)(i & 0xff));
    }
}
