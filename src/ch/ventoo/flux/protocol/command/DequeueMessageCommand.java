package ch.ventoo.flux.protocol.command;

import ch.ventoo.flux.exception.NoSuchClientException;
import ch.ventoo.flux.exception.NoSuchQueueException;
import ch.ventoo.flux.model.Message;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.response.ResponseError;
import ch.ventoo.flux.protocol.response.ResponseMessage;
import ch.ventoo.flux.store.PostgresStore;
import ch.ventoo.flux.store.StoreUtil;
import ch.ventoo.flux.store.pgsql.PgConnectionPool;
import ch.ventoo.flux.util.StringUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;

/**
 * Command to dequeue a message from the message passing system.
 */
public class DequeueMessageCommand extends Command {

    private DataInputStream _stream;
    private String _queueHandle;
    private int _receiverId;

    public DequeueMessageCommand(String queueHandle, int receiverId) {
        _queueHandle = queueHandle;
        _receiverId = receiverId;
    }

    public DequeueMessageCommand(DataInputStream stream) {
        _stream = stream;
    }

    @Override
    public int getType() {
        return Protocol.Actions.DEQUEUE_MESSAGE;
    }

    @Override
    public byte[] getBody() {
        int length = _queueHandle.getBytes().length;
        ByteBuffer buffer = ByteBuffer.allocate(length + 12);
        buffer.putInt(getType());
        buffer.putInt(_receiverId);
        buffer.putInt(length);
        buffer.put(_queueHandle.getBytes());
        return buffer.array();
    }

    @Override
    public Response execute() throws IOException {
        _receiverId = _stream.readInt();
        _queueHandle = StringUtil.readStringFromStream(_stream);
        Connection con = PgConnectionPool.getInstance().getConnection();
        PostgresStore store = new PostgresStore(con);
        try {
            Message message = store.dequeueMessage(_queueHandle, _receiverId);
            return new ResponseMessage(message);
        } catch (NoSuchQueueException e) {
            return new ResponseError(Protocol.ErrorCodes.NO_SUCH_QUEUE);
        } catch (NoSuchClientException e) {
            return new ResponseError(Protocol.ErrorCodes.NO_SUCH_CLIENT);
        } finally {
            StoreUtil.closeQuietly(con);
        }
    }
}
