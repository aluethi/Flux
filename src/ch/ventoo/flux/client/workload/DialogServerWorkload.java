package ch.ventoo.flux.client.workload;

import ch.ventoo.flux.client.MessageService;
import ch.ventoo.flux.client.Workload;
import ch.ventoo.flux.exception.*;
import ch.ventoo.flux.model.Message;

/**
 * The dialog server workload simulates the server part of a request / response communication between
 * a client and a server via message passing system.
 * The client sends a request to a specific receiver that acts as a server and the server responds.
 */
public class DialogServerWorkload extends Workload {

    private final static String QUEUE = "dialogQueue";

    @Override
    public void start(MessageService service, String[] args) {
        try {
            int partnerId = service.getClientId() - 1;
            service.register();

            while(!isStopped()) {
                try {
                    Message m = service.dequeueMessage(QUEUE);
                    if(m == Message.NO_MESSAGE) continue;
                    String[] splits = m.getContent().split("-");
                    splits[1] = String.valueOf(Integer.parseInt(splits[1]) + 1);
                    Message n = service.createDirectedMessage(partnerId, splits[0] + "-" + splits[1]);
                    service.enqueueMessage(QUEUE, n);
                } catch (NoSuchQueueException e) {
                    try {
                        service.createQueue(QUEUE);
                    } catch (DuplicateQueueException e1) {
                        /* ignore */
                    }
                } catch (NoSuchClientException e) {
                    /* ignore */
                }
            }
        } catch (DuplicateClientException e) {
            e.printStackTrace();
        } catch (UnknownErrorException e) {
            e.printStackTrace();
        }
    }
}
