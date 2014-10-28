package ch.ventoo.flux.protocol.response;

import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;

import java.nio.ByteBuffer;

/**
 * Created by nano on 27/10/14.
 */
public class ResponseAck implements Response {

    public ResponseAck() { }

    @Override
    public int getType() {
        return Protocol.Responses.ACK;
    }

    @Override
    public byte[] getBody() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(getType());
        return buffer.array();
    }
}
