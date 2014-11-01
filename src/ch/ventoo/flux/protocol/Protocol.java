package ch.ventoo.flux.protocol;

/**
 * Constants used within the protocol.
 */
public interface Protocol {
    public static interface Actions {
        int REGISTER = 1;
        int DEREGISTER = 2;

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
        int PING = 100;
    }

    public static interface Responses {
        int MESSAGE = 1;
        int ACK = 2;
        int ERROR = 3;
        int BINARY = 4;
        int QUEUES = 5;
    }

    public static interface ErrorCodes {
        int CLIENT_WITH_ID_EXISTS = 1;
        int NO_SUCH_CLIENT = 2;
        int DUPLICATE_QUEUE = 3;
        int NO_SUCH_QUEUE = 4;
    }
}
