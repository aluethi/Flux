package ch.ventoo.flux.protocol.response;

import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by nano on 27/10/14.
 */
public class ResponseError implements Response {

    private int _errorCode;

    public ResponseError() { }

    public ResponseError(int errorCode) {
        _errorCode = errorCode;
    }

    @Override
    public void initFromStream(DataInputStream stream) throws IOException {
        _errorCode = stream.readInt();
    }

    @Override
    public int getType() {
        return Protocol.Responses.ERROR;
    }

    public int getErrorCode() {
        return _errorCode;
    }

    @Override
    public byte[] getBody() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(getType());
        buffer.putInt(getErrorCode());
        return buffer.array();
    }
}
