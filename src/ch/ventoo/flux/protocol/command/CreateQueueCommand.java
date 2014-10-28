package ch.ventoo.flux.protocol.command;

import ch.ventoo.flux.exception.DuplicateQueueException;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.response.ResponseAck;
import ch.ventoo.flux.protocol.response.ResponseError;
import ch.ventoo.flux.store.PostgresStore;
import ch.ventoo.flux.store.pgsql.PgConnectionPool;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by nano on 22/10/14.
 */
public class CreateQueueCommand implements Command {

    private DataInputStream _stream;
    private String _queueHandle;

    public CreateQueueCommand(String queueHandle) {
        _queueHandle = queueHandle;
    }

    public CreateQueueCommand(DataInputStream stream) {
        _stream = stream;
    }

    @Override
    public byte[] getBody() {
        int length = _queueHandle.getBytes().length;
        ByteBuffer buffer = ByteBuffer.allocate(length + 8);
        buffer.putInt(Protocol.Actions.CREATE_QUEUE);
        buffer.putInt(length);
        buffer.put(_queueHandle.getBytes());
        return buffer.array();
    }

    @Override
    public Response execute() throws IOException {
        int length = _stream.readInt();
        byte[] data = new byte[length];
        _stream.read(data);
        _queueHandle = new String(data);
        PostgresStore store = new PostgresStore(PgConnectionPool.getInstance().getConnection());
        try {
            store.createQueue(_queueHandle);
            return new ResponseAck();
        } catch (DuplicateQueueException e) {
            return new ResponseError(Protocol.ErrorCodes.QUEUE_WITH_HANDLE_EXISTS);
        }
    }
}
