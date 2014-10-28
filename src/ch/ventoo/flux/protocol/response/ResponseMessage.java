package ch.ventoo.flux.protocol.response;

import ch.ventoo.flux.model.Message;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;

import java.nio.ByteBuffer;
import java.sql.Date;

/**
 * Created by nano on 27/10/14.
 */
public class ResponseMessage implements Response {

    private final int _id, _senderId, _receiverId, _priority;
    private final String _content;
    private final Date _timestamp;

    public ResponseMessage(Message message) {
        this(message.getId(), message.getSender(), message.getReceiver(),
                message.getPriority(), message.getTimestamp(), message.getContent());
    }

    public ResponseMessage(int id, int senderId, int receiverId, int priority,
                           Date timestamp, String content) {
        _id = id;
        _senderId = senderId;
        _receiverId = receiverId;
        _priority = priority;
        _timestamp = timestamp;
        _content = content;
    }

    @Override
    public int getType() {
        return Protocol.Responses.ERROR;
    }

    public int getId() {
        return _id;
    }

    public int getSenderId() {
        return _senderId;
    }

    public int getReceiverId() {
        return _receiverId;
    }

    public int getPriority() {
        return _priority;
    }

    public Date getTimestamp() {
        return _timestamp;
    }

    public String getContent() {
        return _content;
    }

    @Override
    public byte[] getBody() {
        Date timestamp = getTimestamp();
        String dateString = timestamp.toString(); // TODO: correct formating
        int dateLength = dateString.getBytes().length;

        String content = getContent();
        int contentLength = content.getBytes().length;

        ByteBuffer buffer = ByteBuffer.allocate(7 * 4 + dateLength + contentLength);
        buffer.putInt(getType());
        buffer.putInt(getId());
        buffer.putInt(getSenderId());
        buffer.putInt(getReceiverId());
        buffer.putInt(getPriority());
        buffer.putInt(dateLength);
        buffer.put(dateString.getBytes());
        buffer.putInt(contentLength);
        buffer.put(content.getBytes());
        return buffer.array();
    }
}
