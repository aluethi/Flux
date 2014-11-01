package ch.ventoo.flux.transport;

import ch.ventoo.flux.util.NIOInputStream;
import ch.ventoo.flux.util.NIOOutputStream;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Container format for different network protocols.
 */
public class WireFormat {

    /**
     * Unmarshalls a ByteBuffer into a Frame.
     * @param buffer
     * @return
     * @throws IOException
     */
    public Frame unmarshal(ByteBuffer buffer) throws IOException {
        DataInputStream stream = new DataInputStream(new NIOInputStream(buffer));
        return unmarshal(stream);
    }

    /**
     * Unmarshalls a DataInputStream into a Frame.
     * @param stream
     * @return
     * @throws IOException
     */
    public Frame unmarshal(DataInputStream stream) throws IOException {
        int size = parseFrameSize(stream);
        byte[] body = parseBody(stream, size);
        return new Frame(size, body);
    }

    /**
     * Parses the frame size.
     * @param stream
     * @return
     * @throws IOException
     */
    public int parseFrameSize(DataInputStream stream) throws IOException {
        int frameSize = stream.readInt();
        return frameSize;
    }

    /**
     * Parses the remaining body.
     * @param stream
     * @param size
     * @return
     * @throws IOException
     */
    public byte[] parseBody(DataInputStream stream, int size) throws IOException {
        byte[] body = new byte[size];
        stream.read(body);
        return body;
    }

    /**
     * Marshalls a ByteBuffer into a Frame.
     * @param buffer
     * @param frame
     * @throws IOException
     */
    public void marshal(ByteBuffer buffer, Frame frame) throws IOException {
        DataOutputStream stream = new DataOutputStream(new NIOOutputStream(buffer));
        marshal(stream, frame);
    }

    /**
     * Marshalls a DataOutputStream into a Frame.
     * @param stream
     * @param frame
     * @throws IOException
     */
    public void marshal(DataOutputStream stream, Frame frame) throws IOException {
        encodeFrameSize(stream, frame.getSize());
        encodeBody(stream, frame.getBody());
        stream.flush();
    }

    /**
     * Encodes the frame size.
     * @param stream
     * @param size
     * @throws IOException
     */
    public void encodeFrameSize(DataOutputStream stream, int size) throws IOException {
        stream.writeInt(size);
    }

    /**
     * Encodes the rest of the frame body.
     * @param stream
     * @param body
     * @throws IOException
     */
    public void encodeBody(DataOutputStream stream, byte[] body) throws IOException {
        stream.write(body);
    }
}
