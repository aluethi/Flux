package ch.ventoo.flux.protocol.command;

import ch.ventoo.flux.exception.NoSuchClientException;
import ch.ventoo.flux.exception.NoSuchQueueException;
import ch.ventoo.flux.model.Message;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.response.ResponseAck;
import ch.ventoo.flux.protocol.response.ResponseError;
import ch.ventoo.flux.protocol.response.ResponseMessage;
import ch.ventoo.flux.protocol.response.ResponseNoMessage;
import ch.ventoo.flux.util.StringUtil;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;

/**
 * Command to receive a message from a queue given a specific sender without removing it from the queue.
 */
public class PeekMessageFromSenderCommand extends Command {

    private DataInputStream _stream;
    private int _senderId;
    private String _queueHandle;
    private int _receiverId;

    public PeekMessageFromSenderCommand(String queueHandle, int senderId, int receiverId) {
        _queueHandle = queueHandle;
        _senderId = senderId;
        _receiverId = receiverId;
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
        ByteBuffer buffer = ByteBuffer.allocate(length + 16);
        buffer.putInt(getType());
        buffer.putInt(_receiverId);
        buffer.putInt(length);
        buffer.put(_queueHandle.getBytes());
        buffer.putInt(_senderId);
        return buffer.array();
    }

    @Override
    public Response execute() throws IOException {
        _receiverId = _stream.readInt();
        _queueHandle = StringUtil.readStringFromStream(_stream);
        _senderId = _stream.readInt();
        _manager.beginConnectionScope();
        try {
            _manager.beginTransaction();
            Message message = _manager.getStore().peekMessageFromSender(_queueHandle, _senderId, _receiverId);
            _manager.endTransaction();
            if(message == Message.NO_MESSAGE) {
                return new ResponseNoMessage();
            } else {
                return new ResponseMessage(message);
            }
        } catch (NoSuchQueueException e) {
            _manager.abortTransaction();
            return new ResponseError(Protocol.ErrorCodes.NO_SUCH_QUEUE);
        } catch (NoSuchClientException e) {
            _manager.abortTransaction();
            return new ResponseError(Protocol.ErrorCodes.NO_SUCH_CLIENT);
        } catch (SQLException e) {
            _manager.abortTransaction();
            return new ResponseError(Protocol.ErrorCodes.DATABASE_ERROR);
        } finally {
            _manager.endConnectionScope();
        }
    }
}
