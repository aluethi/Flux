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
 * Command to query for queues with waiting messages.
 */
public class QueryForQueuesCommand extends Command {

    private DataInputStream _stream;

    public QueryForQueuesCommand() {  }

    public QueryForQueuesCommand(DataInputStream stream) {
        _stream = stream;
    }

    @Override
    public int getType() {
        return Protocol.Actions.QUERY_FOR_QUEUES;
    }

    @Override
    public byte[] getBody() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(getType());
        return buffer.array();
    }

    @Override
    public Response execute() throws IOException {
        _manager.beginConnectionScope();
        Queue[] queues;
        try {
            _manager.beginTransaction();
            queues = _manager.getStore().queryForQueues();
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
