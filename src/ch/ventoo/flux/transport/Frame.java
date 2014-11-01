package ch.ventoo.flux.transport;

/**
 * A Frame is the basic building block of the network communication and protocols are built on top of it.
 * It contains a leading body size (int) as header and a rest frame body.
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
