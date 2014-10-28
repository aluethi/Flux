package ch.ventoo.flux.protocol.command;

import ch.ventoo.flux.model.Queue;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.response.ResponseQueues;
import ch.ventoo.flux.store.PostgresStore;
import ch.ventoo.flux.store.pgsql.PgConnectionPool;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by nano on 22/10/14.
 */
public class QueryForQueuesFromSenderCommand implements Command {

    private DataInputStream _stream;
    private int _senderId;

    public QueryForQueuesFromSenderCommand(int senderId) {
        _senderId = senderId;
    }

    public QueryForQueuesFromSenderCommand(DataInputStream stream) {
        _stream = stream;
    }

    @Override
    public byte[] getBody() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(Protocol.Actions.QUERY_FOR_QUEUES_FROM_SENDER);
        buffer.putInt(_senderId);
        return buffer.array();
    }

    @Override
    public Response execute() throws IOException {
        _senderId = _stream.readInt();
        PostgresStore store = new PostgresStore(PgConnectionPool.getInstance().getConnection());
        Queue[] queues = store.queryForQueuesFromSender(_senderId);
        return new ResponseQueues(queues);
    }
}
