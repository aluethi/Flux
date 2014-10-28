package ch.ventoo.flux.client;

/**
 * Created by nano on 25/10/14.
 */
public class MessageService {

    private final MessageServiceImpl _messageService;

    public MessageService(String host, int port) {
        _messageService = new MessageServiceImpl(host, port);
    }

    public boolean register(int clientId) {
        return _messageService.register(clientId);
    }

    public boolean deregister(int clientId) {
        return _messageService.deregister(clientId);
    }

}
