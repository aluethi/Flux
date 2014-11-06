package ch.ventoo.flux.protocol.command;

import ch.ventoo.flux.exception.DuplicateQueueException;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.response.ResponseAck;
import ch.ventoo.flux.protocol.response.ResponseError;
import ch.ventoo.flux.util.StringUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Command to create a new queue with a given queue handle.
 */
public class CreateQueueCommand extends Command {

    private DataInputStream _stream;
    private String _queueHandle;

    public CreateQueueCommand(String queueHandle) {
        _queueHandle = queueHandle;
    }

    public CreateQueueCommand(DataInputStream stream) {
        _stream = stream;
    }

    @Override
    public int getType() {
        return Protocol.Actions.CREATE_QUEUE;
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
        try {
            _manager.beginTransaction();
            _manager.getStore().createQueue(_queueHandle);
            _manager.endTransaction();
        } catch (DuplicateQueueException e) {
            _manager.abortTransaction();
            return new ResponseError(Protocol.ErrorCodes.DUPLICATE_QUEUE);
        } finally {
            _manager.endConnectionScope();
        }
        return new ResponseAck();
    }
}
