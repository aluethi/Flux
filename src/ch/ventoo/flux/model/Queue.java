package ch.ventoo.flux.model;

import java.sql.Date;

/**
 * Created by nano on 18/10/14.
 */
public class Queue {
    private int _id;
    private String _handle;
    private Date _timestamp;

    public Queue(int id, String handle, Date timestamp) {
        _id = id;
        _handle = handle;
        _timestamp = timestamp;
    }

    public int getId() {
        return _id;
    }


    public String getHandle() {
        return _handle;
    }

    public void setHandle(String handle) {
        _handle = handle;
    }

    public Date getTimestamp() {
        return _timestamp;
    }

    public void setTimestamp(Date timestamp) {
        _timestamp = timestamp;
    }
}
