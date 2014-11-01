package ch.ventoo.flux.protocol;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by nano on 09/10/14.
 */
public interface Response {
    public void initFromStream(DataInputStream stream) throws IOException;
    public int getType();
    public byte[] getBody();
}
