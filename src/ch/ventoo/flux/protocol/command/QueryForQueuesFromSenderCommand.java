package ch.ventoo.flux.protocol.command;

import ch.ventoo.flux.model.Queue;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.response.ResponseQueues;
import ch.ventoo.flux.store.PostgresStore;
import ch.ventoo.flux.store.StoreUtil;
import ch.ventoo.flux.store.pgsql.PgConnectionPool;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.Connection;

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
        Connection con = PgConnectionPool.getInstance().getConnection();
        PostgresStore store = new PostgresStore(con);
        Queue[] queues = store.queryForQueuesFromSender(_senderId);
        StoreUtil.closeQuietly(con);
        return new ResponseQueues(queues);
    }
}
