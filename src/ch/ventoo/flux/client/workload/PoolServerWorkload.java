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

        retryRegister(service);

        while(!isStopped()) {
            Message m = retryDequeueMessage(service, PUT_QUEUE);

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

            m = service.createDirectedMessage(Integer.parseInt(splits[0]), content);

            retryEnqueueMessage(service, GET_QUEUE, m);
        }
    }
}
