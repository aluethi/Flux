package ch.ventoo.flux.client.workload;

import ch.ventoo.flux.client.MessageService;
import ch.ventoo.flux.client.Workload;
import ch.ventoo.flux.exception.*;
import ch.ventoo.flux.model.Message;

/**
 * Created by nano on 04/11/14.
 */
public class PoolServerWorkload extends Workload {

    private static String PUT_QUEUE = "putQueue";
    private static String GET_QUEUE = "getQueue";

    @Override
    public void start(MessageService service, String[] args) {
        int loadSize = 0;
        if(args.length > 0) {
            loadSize = Integer.parseInt(args[0]);
        }

        try {
            service.register();

            while(!isStopped()) {
                try {
                    Message m = service.dequeueMessage(PUT_QUEUE);
                    if(m == Message.NO_MESSAGE) continue;

                    String[] parts = m.getContent().split("/");

                    String[] splits = parts[0].split(":");
                    splits[2] = String.valueOf(Integer.parseInt(splits[2]) + 1);
                    parts[0] = splits[0] + ":" + splits[1] + ":" + splits[2];

                    String content = "";
                    if(parts.length > 1) {
                        if (loadSize - parts[0].length() - 1 > 0) {
                            parts[1] = generatePayload(loadSize - parts[0].length() - 1);
                        }
                        content = parts[0] + "/" + parts[1];
                    } else {
                        content = parts[0] + "/";
                    }

                    Message n = service.createDirectedMessage(Integer.parseInt(splits[0]), content);
                    service.enqueueMessage(GET_QUEUE, n);
                } catch (NoSuchQueueException e) {
                    try {
                        service.createQueue(GET_QUEUE);
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
