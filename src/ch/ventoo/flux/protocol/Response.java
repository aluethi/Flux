package ch.ventoo.flux.protocol;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Response interface. Responses are the counterpart to commands.
 */
public interface Response {
    public void initFromStream(DataInputStream stream) throws IOException;
    public int getType();
    public byte[] getBody();
}
