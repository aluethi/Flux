package ch.ventoo.flux.protocol;

import ch.ventoo.flux.transport.Frame;

import java.io.IOException;

/**
 * Abstract command class. Commands implement the protocol between client and middleware.
 */
public abstract class Command {

    public abstract int getType();

    public Frame getFrame() {
        byte[] body = getBody();
        Frame frame = new Frame(body.length, body);
        return frame;
    }

    public abstract byte[] getBody();
    public abstract Response execute() throws IOException;
}
