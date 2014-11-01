package ch.ventoo.flux.transport;

import java.nio.ByteBuffer;

/**
 * Created by nano on 18/10/14.
 */
public class Frame {
    private final int _size;
    private byte[] _body;

    public Frame(int size, byte[] body) {
        _size = size;
        _body = body;
    }

    public int getSize() {
        return _size;
    }

    public byte[] getBody() {
        return _body;
    }
}
