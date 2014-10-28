package ch.ventoo.flux.protocol.command;

import ch.ventoo.flux.exception.NoSuchClientException;
import ch.ventoo.flux.exception.NoSuchQueueException;
import ch.ventoo.flux.model.Message;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.response.ResponseAck;
import ch.ventoo.flux.protocol.response.ResponseError;
import ch.ventoo.flux.store.PostgresStore;
import ch.ventoo.flux.store.pgsql.PgConnectionPool;
import ch.ventoo.flux.transport.Frame;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Date;

/**
 * Created by nano on 22/10/14.
 */
public class EnqueueMessageCommand implements Command {

    private DataInputStream _stream;
    private String _queueHandle;
    private Message _message;

    public EnqueueMessageCommand(String queueHandle, Message message) {
        _queueHandle = queueHandle;
        _message = message;
    }

    public EnqueueMessageCommand(DataInputStream stream) {
        _stream = stream;
    }

    @Override
    public byte[] getBody() {
        Date timestamp = _message.getTimestamp();
        String dateString = timestamp.toString(); // TODO: correct formating
        int dateLength = dateString.getBytes().length;

        String content = _message.getContent();
        int contentLength = content.getBytes().length;

        int queueHandleLength = _queueHandle.getBytes().length;

        ByteBuffer buffer = ByteBuffer.allocate(7 * 4 + dateLength + contentLength + queueHandleLength);
        buffer.putInt(Protocol.Actions.ENQUEUE_MESSAGE);
        buffer.putInt(queueHandleLength);
        buffer.put(_queueHandle.getBytes());

        buffer.putInt(_message.getSender());
        buffer.putInt(_message.getReceiver());
        buffer.putInt(_message.getPriority());

        buffer.putInt(dateLength);
        buffer.put(dateString.getBytes());

        buffer.putInt(contentLength);
        buffer.put(content.getBytes());

        return buffer.array();
    }


    @Override
    public Response execute() throws IOException {
        int handleLength = _stream.readInt();
        byte[] rawHandle = new byte[handleLength];
        _stream.read(rawHandle);
        _queueHandle = new String(rawHandle);

        int sender = _stream.readInt();
        int receiver = _stream.readInt();
        int priority = _stream.readInt();

        int dateLength = _stream.readInt();
        byte[] rawDate = new byte[dateLength];
        _stream.read(rawDate);
        String dateString = new String(rawDate);
        Date date = Date.valueOf(dateString);

        int contentLength = _stream.readInt();
        byte[] rawContent = new byte[contentLength];
        _stream.read(rawContent);
        String content = new String(rawContent);

        _message = new Message(0, sender, receiver, priority, date, content);

        PostgresStore store = new PostgresStore(PgConnectionPool.getInstance().getConnection());
        try {
            store.enqueueMessage(_queueHandle, _message);
            return new ResponseAck();
        } catch (NoSuchQueueException e) {
            return new ResponseError(Protocol.ErrorCodes.NO_SUCH_QUEUE);
        } catch (NoSuchClientException e) {
            return new ResponseError(Protocol.ErrorCodes.NO_SUCH_CLIENT);
        }
    }
}
