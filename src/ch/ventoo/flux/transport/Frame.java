package ch.ventoo.flux.transport;

import java.nio.ByteBuffer;

/**
 * Created by nano on 18/10/14.
 */
public class Frame {
    private final int _size;
    private byte[] _body;

    public Frame(int size) {
        _size = size;
    }

    public int getSize() {
        return _size;
    }

    public void setBody(byte[] body) {
        this._body = body;
    }

    public byte[] getBody() {
        return _body;
    }
}
