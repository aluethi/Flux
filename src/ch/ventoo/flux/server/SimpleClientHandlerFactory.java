package ch.ventoo.flux.server;

/**
 * Created by nano on 18/10/14.
 */
public class SimpleClientHandlerFactory implements ClientHandlerFactory {
    @Override
    public ClientHandler getHandler() {
        return new SimpleClientHandler();
    }
}
