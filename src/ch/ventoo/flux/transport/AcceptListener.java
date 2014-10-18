package ch.ventoo.flux.transport;

import java.nio.channels.SocketChannel;

/**
 * Created by nano on 09/10/14.
 */
public interface AcceptListener {
    public void onAccept(SocketChannel channel);
}
