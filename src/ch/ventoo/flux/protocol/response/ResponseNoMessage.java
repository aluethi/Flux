package ch.ventoo.flux.protocol.response;

import ch.ventoo.flux.protocol.Response;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by nano on 04/11/14.
 */
public class ResponseNoMessage implements Response {

    ResponseNoMessage() {
    }

    @Override
    public void initFromStream(DataInputStream stream) throws IOException {

    }

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public byte[] getBody() {
        return new byte[0];
    }
}
