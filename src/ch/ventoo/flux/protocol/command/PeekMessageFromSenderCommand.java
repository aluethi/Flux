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

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;

/**
 * Command to receive a message from a queue given a specific sender without removing it from the queue.
 */
public class PeekMessageFromSenderCommand extends Command {

    private DataInputStream _stream;
    private int _senderId;
    private String _queueHandle;

    public PeekMessageFromSenderCommand(String queueHandle, int senderId) {
        _queueHandle = queueHandle;
        _senderId = senderId;
    }

    public PeekMessageFromSenderCommand(DataInputStream stream) {
        _stream = stream;
    }

    @Override
    public int getType() {
        return Protocol.Actions.PEEK_MESSAGE_FROM_SENDER;
    }

    @Override
    public byte[] getBody() {
        int length = _queueHandle.getBytes().length;
        ByteBuffer buffer = ByteBuffer.allocate(length + 8);
        buffer.putInt(getType());
        buffer.putInt(length);
        buffer.put(_queueHandle.getBytes());
        buffer.putInt(_senderId);
        return buffer.array();
    }

    @Override
    public Response execute() throws IOException {
        int length = _stream.readInt();
        byte[] data = new byte[length];
        _stream.read(data);
        _queueHandle = new String(data);
        int senderId = _stream.readInt();
        Connection con = PgConnectionPool.getInstance().getConnection();
        PostgresStore store = new PostgresStore(con);
        try {
            Message message = store.peekMessageFromSender(_queueHandle, _senderId);
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
