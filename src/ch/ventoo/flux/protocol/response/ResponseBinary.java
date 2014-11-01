package ch.ventoo.flux.protocol.response;

import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Response that returns a binary state.
 */
public class ResponseBinary implements Response {

    private byte _state;

    public ResponseBinary() { }

    public ResponseBinary(boolean state) {
        _state = (byte)(state ? 1 : 0);
    }

    @Override
    public void initFromStream(DataInputStream stream) throws IOException {
        _state = stream.readByte();
    }

    @Override
    public int getType() {
        return Protocol.Responses.ACK;
    }

    public boolean getState() {
        return (_state == 1);
    }

    @Override
    public byte[] getBody() {
        ByteBuffer buffer = ByteBuffer.allocate(5);
        buffer.putInt(getType());
        buffer.put((byte) (getState() ? 1 : 0));
        return buffer.array();
    }
}
