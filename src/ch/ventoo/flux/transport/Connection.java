package ch.ventoo.flux.transport;

import java.nio.channels.SelectionKey;

/**
 * Created by nano on 09/10/14.
 */
public class Connection {

    private final SelectionKey _key;

    public Connection(SelectionKey key) {
        _key = key;
    }

    public void receive() {

    }

    public void send() {

    }

}
