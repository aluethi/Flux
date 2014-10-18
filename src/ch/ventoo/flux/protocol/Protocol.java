package ch.ventoo.flux.protocol;

/**
 * Created by nano on 09/10/14.
 */
public interface Protocol {
    public static interface Commands {
        int REGISTER = 1;
        int DEREGISTER = 2;
        int SIGN_ON = 3;
        int SIGN_OFF = 4;

        int CREATE_QUEUE = 11;
        int DELETE_QUEUE = 12;
        int IS_QUEUE_EMPTY = 13;
        int QUERY_FOR_QUEUES = 14;
        int QUERY_FOR_QUEUES_FROM_SENDER = 15;

        int ENQUEUE_MESSAGE = 21;
        int DEQUEUE_MESSAGE = 22;
        int DEQUEUE_MESSAGE_FROM_SENDER = 23;
        int PEEK_MESSAGE = 24;
        int PEEK_MESSAGE_FROM_SENDER = 25;
    }

    public static interface Responses {
        int MESSAGE = 1;
        int ACK = 2;
        int ERROR = 3;
    }
}
