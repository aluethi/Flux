package ch.ventoo.flux.transport;

import java.nio.ByteBuffer;

/**
 * Created by nano on 09/10/14.
 */
public class WireFormat {

    public Frame unmarshal(ByteBuffer buffer) {
        int frameSize = parseFrameSize(buffer);
        Frame frame = new Frame(frameSize);
        byte[] body = parseBody(buffer, frameSize);
        frame.setBody(body);
        return frame;
    }

    public int parseFrameSize(ByteBuffer buffer) {
        int frameSize = buffer.getInt();
        return frameSize;
    }

    public byte[] parseBody(ByteBuffer buffer, int size) {
        byte[] body = new byte[size];
        buffer.get(body);
        return body;
    }

    public void marshal(ByteBuffer buffer, Frame frame) {
        encodeFrameSize(buffer, frame.getSize());
        encodeBody(buffer,frame.getBody());
    }

    public void encodeFrameSize(ByteBuffer buffer, int size) {
        buffer.putInt(size);
    }

    public void encodeBody(ByteBuffer buffer, byte[] body) {
        buffer.put(body);
    }
}
