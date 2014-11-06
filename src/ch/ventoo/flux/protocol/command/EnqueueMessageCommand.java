package ch.ventoo.flux.protocol.command;

import ch.ventoo.flux.exception.NoSuchClientException;
import ch.ventoo.flux.exception.NoSuchQueueException;
import ch.ventoo.flux.model.Message;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.response.ResponseAck;
import ch.ventoo.flux.protocol.response.ResponseError;
import ch.ventoo.flux.store.StoreUtil;
import ch.ventoo.flux.util.StringUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Date;

/**
 * Command to enqueue a new message into the message passing system.
 */
public class EnqueueMessageCommand extends Command {

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
    public int getType() {
        return Protocol.Actions.ENQUEUE_MESSAGE;
    }

    @Override
    public byte[] getBody() {
        Date timestamp = _message.getTimestamp();
        String dateString = StoreUtil.convertDateToString(timestamp);
        int dateLength = dateString.getBytes().length;

        String content = _message.getContent();
        int contentLength = content.getBytes().length;

        int queueHandleLength = _queueHandle.getBytes().length;

        ByteBuffer buffer = ByteBuffer.allocate(7 * 4 + dateLength + contentLength + queueHandleLength);
        buffer.putInt(getType());
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
        _queueHandle = StringUtil.readStringFromStream(_stream);
        int sender = _stream.readInt();
        int receiver = _stream.readInt();
        int priority = _stream.readInt();
        String dateString = StringUtil.readStringFromStream(_stream);
        Date date = StoreUtil.convertStringToDate(dateString);
        String content = StringUtil.readStringFromStream(_stream);
        _message = new Message(0, sender, receiver, priority, date, content);

        _manager.beginConnectionScope();
        try {
            _manager.beginTransaction();
            _manager.getStore().enqueueMessage(_queueHandle, _message);
            _manager.endTransaction();
        } catch (NoSuchQueueException e) {
            _manager.abortTransaction();
            return new ResponseError(Protocol.ErrorCodes.NO_SUCH_QUEUE);
        } catch (NoSuchClientException e) {
            _manager.abortTransaction();
            return new ResponseError(Protocol.ErrorCodes.NO_SUCH_CLIENT);
        } finally {
            _manager.endConnectionScope();
        }
        return new ResponseAck();
    }
}
