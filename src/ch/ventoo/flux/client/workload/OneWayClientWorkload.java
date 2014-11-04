package ch.ventoo.flux.client.workload;

import ch.ventoo.flux.client.Workload;
import ch.ventoo.flux.client.MessageService;
import ch.ventoo.flux.exception.*;
import ch.ventoo.flux.model.Message;
import ch.ventoo.flux.profiling.LogWrapper;

/**
 * Created by nano on 02/11/14.
 */
public class OneWayClientWorkload extends Workload {

    private final LogWrapper LOGGER = new LogWrapper(OneWayClientWorkload.class);

    private final static String QUEUE = "publicQueue";

    @Override
    public void start(MessageService service, String[] args) {
        try {
            Message m = service.createAnonymousMessage("0");
            service.register();

            while(!isStopped()) {
                try {
                    service.enqueueMessage(QUEUE, m);
                    m = service.dequeueMessage(QUEUE);
                } catch (NoSuchQueueException e) {
                    try {
                        service.createQueue(QUEUE);
                    } catch (DuplicateQueueException e1) {
                        /* ignore */
                    }
                } catch (NoSuchClientException e) {
                    e.printStackTrace();
                }
                m.setContent(String.valueOf(Integer.parseInt(m.getContent()) + 1));
            }
        } catch (DuplicateClientException e) {
            e.printStackTrace();
        } catch (UnknownErrorException e) {
            e.printStackTrace();
        }
    }
}
