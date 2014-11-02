package ch.ventoo.flux.client.experiment;

import ch.ventoo.flux.client.Experiment;
import ch.ventoo.flux.client.MessageService;
import ch.ventoo.flux.exception.DuplicateClientException;
import ch.ventoo.flux.exception.DuplicateQueueException;
import ch.ventoo.flux.exception.NoSuchQueueException;
import ch.ventoo.flux.exception.UnknownErrorException;
import ch.ventoo.flux.model.Message;

/**
 * Created by nano on 02/11/14.
 */
public class SimpleClientExperiment extends Experiment {

    private final static String QUEUE = "publicQueue";

    @Override
    public void start(MessageService service, String[] args) {
        int clientId = Integer.parseInt(args[0]);
        try {
            Message m = new Message(clientId, "0");
            service.register(clientId);

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
                }
                System.out.println(m.getContent());
                m.setContent(String.valueOf(Integer.parseInt(m.getContent()) + 1));
            }
        } catch (DuplicateClientException e) {
            e.printStackTrace();
        } catch (UnknownErrorException e) {
            e.printStackTrace();
        }
    }
}
