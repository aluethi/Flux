package ch.ventoo.flux.transport;

import java.nio.channels.SocketChannel;

/**
 * Created by nano on 02/10/14.
 */
public interface IAcceptListener {
    public void onAccept(SocketChannel channel);
}
