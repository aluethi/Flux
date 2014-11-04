package ch.ventoo.flux.client.workload;

import ch.ventoo.flux.client.MessageService;
import ch.ventoo.flux.client.Workload;
import ch.ventoo.flux.exception.*;
import ch.ventoo.flux.model.Message;

/**
 * The dialog client workload simulates the client part of a request / response communication between
 * a client and a server via message passing system.
 * The client sends a request to a specific receiver that acts as a server and the server responds.
 */
public class DialogClientWorkload extends Workload {

    private final static String QUEUE = "dialogQueue";

    @Override
    public void start(MessageService service, String[] args) {
        try {
            int partnerId = service.getClientId() + 1;
            Message m = service.createDirectedMessage(partnerId, "1-0"), temp;
            service.register();
            while(true) {
                try {
                    service.enqueueMessage(QUEUE, m);
                    break;
                } catch (NoSuchQueueException e) {
                    try {
                        service.createQueue(QUEUE);
                    } catch (DuplicateQueueException e1) {
                    /* ignore */
                    }
                } catch (NoSuchClientException e) {
                    e.printStackTrace();
                }
            }

            while(!isStopped()) {
                try {
                    m = service.dequeueMessage(QUEUE);
                    if(m == Message.NO_MESSAGE) {
                        continue;
                    }
                } catch (NoSuchQueueException e) {
                    /* ignore */
                } catch (NoSuchClientException e) {
                    /* partner is not ready yet. try again. */
                    continue;
                }
                String[] splits = m.getContent().split("-");
                splits[0] = String.valueOf(Integer.parseInt(splits[0]) + 1);
                m = service.createDirectedMessage(partnerId, splits[0] + "-" + splits[1]);

                try {
                    service.enqueueMessage(QUEUE, m);
                } catch (NoSuchQueueException e) {
                    /* ignore. should not happen. */
                } catch (NoSuchClientException e) {
                    /* ignore. should not happen. */
                }
            }
        } catch (DuplicateClientException e) {
            e.printStackTrace();
        } catch (UnknownErrorException e) {
            e.printStackTrace();
        }
    }
}
