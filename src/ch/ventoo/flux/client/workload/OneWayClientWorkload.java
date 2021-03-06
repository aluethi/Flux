package ch.ventoo.flux.client.workload;

import ch.ventoo.flux.client.MessageService;
import ch.ventoo.flux.client.Workload;
import ch.ventoo.flux.model.Message;
import ch.ventoo.flux.profiling.LogWrapper;

/**
 * The one way client workload just writes messages into a public queue, that other clients can read
 * and respond to.
 */
public class OneWayClientWorkload extends Workload {

    private final LogWrapper LOGGER = new LogWrapper(OneWayClientWorkload.class);

    private final static String QUEUE = "publicQueue";

    @Override
    public void start(MessageService service, String[] args) {
        int loadSize = 0;
        if(args.length > 0) {
            loadSize = Integer.parseInt(args[0]);
        }

        Message m = null;
        if(loadSize > 2) {
            m = service.createAnonymousMessage("0/" + generatePayload(loadSize-2));
        } else {
            m = service.createAnonymousMessage("0/");
        }

        retryRegister(service);
        retryCreateQueue(service, QUEUE);
        retryEnqueueMessage(service, QUEUE, m);

        while(!isStopped()) {
            m = retryDequeueMessage(service, QUEUE);

            String[] parts = m.getContent().split("/");
            parts[0] = String.valueOf(Integer.parseInt(parts[0]) + 1);
            if(parts.length > 1) {
                if (loadSize - parts[0].length() - 1 > 0) {
                    parts[1] = generatePayload(loadSize - parts[0].length() - 1);
                }
                m.setContent(parts[0] + "/" + parts[1]);
            } else {
                m.setContent(parts[0] + "/");
            }
            retryEnqueueMessage(service, QUEUE, m);
        }
    }
}
