package ch.ventoo.flux.transport;

import ch.ventoo.flux.util.NIOInputStream;
import ch.ventoo.flux.util.NIOOutputStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by nano on 09/10/14.
 */
public class WireFormat {

    public Frame unmarshal(ByteBuffer buffer) throws IOException {
        DataInputStream stream = new DataInputStream(new NIOInputStream(buffer));
        return unmarshal(stream);
    }

    public Frame unmarshal(DataInputStream stream) throws IOException {
        int size = parseFrameSize(stream);
        byte[] body = parseBody(stream, size);
        return new Frame(size, body);
    }

    public int parseFrameSize(DataInputStream stream) throws IOException {
        int frameSize = stream.readInt();
        return frameSize;
    }

    public byte[] parseBody(DataInputStream stream, int size) throws IOException {
        byte[] body = new byte[size];
        stream.read(body);
        return body;
    }

    public void marshal(ByteBuffer buffer, Frame frame) throws IOException {
        DataOutputStream stream = new DataOutputStream(new NIOOutputStream(buffer));
        marshal(stream, frame);
    }

    public void marshal(DataOutputStream stream, Frame frame) throws IOException {
        encodeFrameSize(stream, frame.getSize());
        encodeBody(stream, frame.getBody());
        stream.flush();
    }

    public void encodeFrameSize(DataOutputStream stream, int size) throws IOException {
        stream.writeInt(size);
    }

    public void encodeBody(DataOutputStream stream, byte[] body) throws IOException {
        stream.write(body);
    }
}
