package ch.ventoo.flux.protocol.response;

import ch.ventoo.flux.model.Queue;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;

import java.nio.ByteBuffer;

/**
 * Created by nano on 28/10/14.
 */
public class ResponseQueues implements Response {

    private final Queue[] _queues;

    public ResponseQueues(Queue[] queues) {
        _queues = queues;
    }

    @Override
    public int getType() {
        return Protocol.Responses.ACK;
    }

    public Queue[] getQueues() {
        return _queues;
    }

    @Override
    public byte[] getBody() {
        int size = calculateNeededSize() + 8;
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.putInt(getType());
        buffer.putInt(getQueues().length);

        for(Queue q : getQueues()) {
            buffer.putInt(q.getId());
            buffer.putInt(q.getHandle().getBytes().length);
            buffer.put(q.getHandle().getBytes());
            String date = q.getTimestamp().toString(); // TODO: Fix this conversion
            buffer.putInt(date.getBytes().length);
            buffer.put(date.getBytes());
        }

        return buffer.array();
    }

    private int calculateNeededSize() {
        int allocSize = 0;
        for(Queue q : getQueues()) {
            allocSize += 4;
            allocSize += q.getHandle().getBytes().length + 4; // accommodate space for size
            String date = q.getTimestamp().toString(); // TODO: Fix this conversion
            allocSize += date.getBytes().length + 4;
        }
        return allocSize;
    }
}
