package ch.ventoo.flux.client;

/**
 * Created by nano on 25/10/14.
 */
public class FluxClient {

    private final MessageService _messageService;

    public static void main(String[] args) {
        new FluxClient("0.0.0.0", 12345);
    }

    public FluxClient(String host, int port) {
        _messageService = new MessageService(host, port);
        _messageService.register(1);
        _messageService.register(2);
        _messageService.deregister(1);
    }

}
