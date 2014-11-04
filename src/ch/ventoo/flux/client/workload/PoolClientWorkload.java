package ch.ventoo.flux.client.workload;

import ch.ventoo.flux.client.MessageService;
import ch.ventoo.flux.client.Workload;
import ch.ventoo.flux.exception.*;
import ch.ventoo.flux.model.Message;

/**
 * Created by nano on 04/11/14.
 */
public class PoolClientWorkload extends Workload {

    private static String PUT_QUEUE = "putQueue";
    private static String GET_QUEUE = "getQueue";

    @Override
    public void start(MessageService service, String[] args) {
        try {
            Message m = service.createAnonymousMessage(service.getClientId() + ":1:0");
            service.register();
            while(true) {
                try {
                    service.enqueueMessage(PUT_QUEUE, m);
                    break;
                } catch (NoSuchQueueException e) {
                    try {
                        service.createQueue(PUT_QUEUE);
                    } catch (DuplicateQueueException e1) {
                    /* ignore */
                    }
                } catch (NoSuchClientException e) {
                    e.printStackTrace();
                }
            }

            while(!isStopped()) {
                try {
                    m = service.dequeueMessage(GET_QUEUE);
                    if(m == Message.NO_MESSAGE) {
                        continue;
                    }
                } catch (NoSuchQueueException e) {
                    try {
                        service.createQueue(GET_QUEUE);
                    } catch (DuplicateQueueException e1) {
                        /* ignored */
                    }
                } catch (NoSuchClientException e) {
                    /* partner is not ready yet. try again. */
                    continue;
                }
                String[] splits = m.getContent().split(":");
                splits[1] = String.valueOf(Integer.parseInt(splits[1]) + 1);
                m = service.createAnonymousMessage(splits[0] + ":" + splits[1] + ":" + splits[2]);

                try {
                    service.enqueueMessage(PUT_QUEUE, m);
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
