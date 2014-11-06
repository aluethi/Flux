package ch.ventoo.flux.protocol.command;

import ch.ventoo.flux.exception.NoSuchQueueException;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.response.ResponseBinary;
import ch.ventoo.flux.protocol.response.ResponseError;
import ch.ventoo.flux.util.StringUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;

/**
 * Command to check whether a message queue is empty.
 */
public class IsQueueEmptyCommand extends Command {

    private DataInputStream _stream;
    private String _queueHandle;

    public IsQueueEmptyCommand(String queueHandle) {
        _queueHandle = queueHandle;
    }

    public IsQueueEmptyCommand(DataInputStream stream) {
        _stream = stream;
    }

    @Override
    public int getType() {
        return Protocol.Actions.IS_QUEUE_EMPTY;
    }

    @Override
    public byte[] getBody() {
        int length = _queueHandle.getBytes().length;
        ByteBuffer buffer = ByteBuffer.allocate(length + 8);
        buffer.putInt(getType());
        buffer.putInt(length);
        buffer.put(_queueHandle.getBytes());
        return buffer.array();
    }

    @Override
    public Response execute() throws IOException {
        _queueHandle = StringUtil.readStringFromStream(_stream);
        _manager.beginConnectionScope();
        boolean isEmpty = false;
        try {
            _manager.beginTransaction();
            isEmpty = _manager.getStore().isQueueEmpty(_queueHandle);
            _manager.endTransaction();
        } catch (NoSuchQueueException e) {
            _manager.abortTransaction();
            return new ResponseError(Protocol.ErrorCodes.NO_SUCH_QUEUE);
        } catch (SQLException e) {
            _manager.abortTransaction();
            return new ResponseError(Protocol.ErrorCodes.DATABASE_ERROR);
        } finally {
            _manager.endConnectionScope();
        }
        return new ResponseBinary(isEmpty);
    }
}
