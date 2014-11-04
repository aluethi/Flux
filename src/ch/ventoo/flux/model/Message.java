package ch.ventoo.flux.model;

import java.sql.Date;

/**
 * Model class for a message with getters and setters for its values.
 */
public class Message {

    public static final Message NO_MESSAGE = new Message(0, null);

    private int _id;
    private int _sender;
    private int _receiver;
    private int _priority;
    private Date _timestamp;
    private String _content;

    public Message(int sender, String content) {
        this(sender, 0, content);
    }

    public Message(int sender, int receiver, String content) {
        this(0, sender, receiver, 0, new Date(new java.util.Date().getTime()), content);
    }

    public Message(int id, int sender, int receiver, int priority, Date timestamp, String content) {
        _id = id;
        _sender = sender;
        _receiver = receiver;
        _priority = priority;
        _timestamp = timestamp;
        _content = content;
    }

    public int getId() {
        return _id;
    }

    public int getSender() {
        return _sender;
    }

    public void setSender(int sender) {
        _sender = sender;
    }

    public int getReceiver() {
        return _receiver;
    }

    public void setReceiver(int receiver) {
        _receiver = receiver;
    }

    public int getPriority() {
        return _priority;
    }

    public void setPriority(int priority) {
        _priority = priority;
    }

    public Date getTimestamp() {
        return _timestamp;
    }

    public void setTimestamp(Date timestamp) {
        _timestamp = timestamp;
    }

    public String getContent() {
        return _content;
    }

    public void setContent(String content) {
        _content = content;
    }
}
