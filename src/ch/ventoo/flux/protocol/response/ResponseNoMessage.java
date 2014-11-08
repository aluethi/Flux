package ch.ventoo.flux.protocol.response;

import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by nano on 04/11/14.
 */
public class ResponseNoMessage implements Response {

    public ResponseNoMessage() {
    }

    @Override
    public void initFromStream(DataInputStream stream) throws IOException {
        // dummy method
        // nothing to initialize here
    }

    @Override
    public int getType() {
        return Protocol.Responses.NO_MESSAGE;
    }

    @Override
    public byte[] getBody() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(getType());
        return buffer.array();
    }
}
