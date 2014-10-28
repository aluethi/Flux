package ch.ventoo.flux.protocol;

import java.io.IOException;

/**
 * Created by nano on 22/10/14.
 */
public interface Command {
    public byte[] getBody();
    public Response execute() throws IOException;
}
