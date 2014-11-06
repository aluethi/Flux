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
        int loadSize = 0;
        if(args.length > 0) {
            loadSize = Integer.parseInt(args[0]);
        }

        int partnerId = service.getClientId() + 1;

        Message m = null, temp;
        if(loadSize > 4) {
            m = service.createDirectedMessage(partnerId, "1-0/" + generatePayload(loadSize-4));
        } else {
            m = service.createDirectedMessage(partnerId, "1-0/");
        }

        retryRegister(service);
        retryCreateQueue(service, QUEUE);
        retryEnqueueMessage(service, QUEUE, m);

        while(!isStopped()) {
            m = retryDequeueMessage(service, QUEUE);

            String[] parts = m.getContent().split("/");

            String[] splits = parts[0].split("-");
            splits[0] = String.valueOf(Integer.parseInt(splits[0]) + 1);
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

            m = service.createDirectedMessage(partnerId, content);

            retryEnqueueMessage(service, QUEUE, m);
        }
    }
}
