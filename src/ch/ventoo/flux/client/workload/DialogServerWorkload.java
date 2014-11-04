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
        int loadSize = 0;
        if(args.length > 0) {
            loadSize = Integer.parseInt(args[0]);
        }
        try {
            int partnerId = service.getClientId() - 1;
            service.register();

            while(!isStopped()) {
                try {
                    Message m = service.dequeueMessage(QUEUE);
                    if(m == Message.NO_MESSAGE) continue;

                    String[] parts = m.getContent().split("/");

                    String[] splits = parts[0].split("-");
                    splits[1] = String.valueOf(Integer.parseInt(splits[1]) + 1);
                    parts[0] = splits[0] + "-" + splits[1];

                    String content = "";
                    if(parts.length > 1) {
                        if (loadSize - parts[0].length() - 1 > 0) {
                            parts[1] = generatePayload(loadSize - parts[0].length() - 1);
                        }
                        content = parts[0] + "/" + parts[1];
                    } else {
                        content = parts[0] + "/";
                    }

                    Message n = service.createDirectedMessage(partnerId, content);

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
