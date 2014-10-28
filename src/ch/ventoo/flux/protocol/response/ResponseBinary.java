package ch.ventoo.flux.protocol.response;

import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;

import java.nio.ByteBuffer;

/**
 * Created by nano on 28/10/14.
 */
public class ResponseBinary implements Response {

    private final byte _state;

    public ResponseBinary(boolean state) {
        _state = (byte)(state ? 1 : 0);
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
        buffer.put((byte)(getState() ? 1 : 0));
        return buffer.array();
    }
}
