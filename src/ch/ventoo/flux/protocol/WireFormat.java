package ch.ventoo.flux.protocol;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by nano on 09/10/14.
 */
public class WireFormat {

    public Command unmarshal(ByteBuffer buffer) throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(buffer.array());
        DataInputStream dataStream = new DataInputStream(stream);
        return unmarshal(dataStream);
    }

    public Command unmarshal(DataInputStream stream) throws IOException {
        int action = parseAction(stream);
        Command command = new Command(action);
        byte[] body = parseBody(stream);
        command.setBody(body);
        return command;
    }

    public int parseAction(DataInputStream stream) throws IOException {
        int action = stream.readInt();
        return action;
    }

    public byte[] parseBody(DataInputStream stream) throws IOException {
        int length = stream.readInt();
        byte body[] = new byte[length];
        stream.readFully(body);
        return body;
    }

    public void marshal(Response response, ByteBuffer buffer) {

    }

    public void encodeAction(int action) {

    }
}
