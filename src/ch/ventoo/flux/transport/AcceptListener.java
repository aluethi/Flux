package ch.ventoo.flux.transport;

import java.nio.channels.SelectionKey;

/**
 * Created by nano on 09/10/14.
 */
public interface AcceptListener {
    public void onAccept(SelectionKey key);
}
