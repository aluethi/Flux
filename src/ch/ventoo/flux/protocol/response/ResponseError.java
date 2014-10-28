package ch.ventoo.flux.protocol.response;

import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;

import java.nio.ByteBuffer;

/**
 * Created by nano on 27/10/14.
 */
public class ResponseError implements Response {

    private final int _errorCode;

    public ResponseError(int errorCode) {
        _errorCode = errorCode;
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
