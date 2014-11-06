package ch.ventoo.flux.protocol.command;

import ch.ventoo.flux.model.Queue;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.response.ResponseError;
import ch.ventoo.flux.protocol.response.ResponseQueues;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;

/**
 * Command to query for queues with messages from a specific sender.
 */
public class QueryForQueuesFromSenderCommand extends Command {

    private DataInputStream _stream;
    private int _senderId;

    public QueryForQueuesFromSenderCommand(int senderId) {
        _senderId = senderId;
    }

    public QueryForQueuesFromSenderCommand(DataInputStream stream) {
        _stream = stream;
    }

    @Override
    public int getType() {
        return Protocol.Actions.QUERY_FOR_QUEUES_FROM_SENDER;
    }

    @Override
    public byte[] getBody() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(getType());
        buffer.putInt(_senderId);
        return buffer.array();
    }

    @Override
    public Response execute() throws IOException {
        _senderId = _stream.readInt();
        _manager.beginConnectionScope();
        Queue[] queues;
        try {
            _manager.beginTransaction();
            queues = _manager.getStore().queryForQueuesFromSender(_senderId);
            _manager.endTransaction();
        } catch (SQLException e) {
            _manager.abortTransaction();
            return new ResponseError(Protocol.ErrorCodes.DATABASE_ERROR);
        } finally {
            _manager.endConnectionScope();
        }
        return new ResponseQueues(queues);
    }
}
