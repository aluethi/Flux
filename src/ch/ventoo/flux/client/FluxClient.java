package ch.ventoo.flux.client;

import ch.ventoo.flux.exception.DuplicateClientException;
import ch.ventoo.flux.exception.NoSuchClientException;
import ch.ventoo.flux.exception.UnknownErrorException;
import ch.ventoo.flux.profiling.BenchLogger;

/**
 * Created by nano on 25/10/14.
 */
public class FluxClient {

    private final MessageService _messageService;

    public static void main(String[] args) {

        if(args.length <= 3) {
            System.err.println("Usage: java FluxClient.jar <name> <test>");
            System.exit(1);
        }

        String name = args[1];
        String test = args[2];

        new FluxClient("0.0.0.0", 12345, name);
    }

    public FluxClient(String host, int port, String name) {
        BenchLogger log = new BenchLogger("client-" + name);

        _messageService = new MessageService(host, port, log);
        try {
            _messageService.register(1);
            _messageService.register(2);
            _messageService.deregister(1);
        } catch (DuplicateClientException e) {
            e.printStackTrace();
        } catch (UnknownErrorException e) {
            e.printStackTrace();
        } catch (NoSuchClientException e) {
            e.printStackTrace();
        }
    }

}
