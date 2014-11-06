package ch.ventoo.flux.client;

import ch.ventoo.flux.exception.*;
import ch.ventoo.flux.model.Message;
import ch.ventoo.flux.profiling.LogWrapper;

/**
 * Created by nano on 02/11/14.
 */
public abstract class Workload {

    private static final LogWrapper LOGGER = new LogWrapper(Workload.class);

    private boolean _stopped = false;
    private String _cachedPad = null;
    private int _cachedSize = 0;

    public abstract void start(MessageService service, String[] args);

    public boolean isStopped() {
        return _stopped;
    }

    public void shutdown() {
        _stopped = true;
    }

    public String generatePayload(int size) {
        if(_cachedSize < size) {
            StringBuilder sb = new StringBuilder();
            while (size > 0) {
                sb.append("a");
                size--;
            }
            _cachedPad = sb.toString();
        }
        return _cachedPad;
    }

    public void retryRegister(MessageService service) {
        while(true) {
            try {
                service.register();
                break;
            } catch (DuplicateClientException e) {
                LOGGER.severe("Client with this ID already registered.");
                throw new RuntimeException(e);
            } catch (UnknownErrorException e) {
                /* retry */
            }
        }
    }

    public void retryCreateQueue(MessageService service, String queueHandle) {
        while(true) {
            try {
                service.createQueue(queueHandle);
                break;
            } catch (DuplicateQueueException e) {
                break;
            } catch (UnknownErrorException e) {
                /* retry */
            }
        }
    }

    public void retryEnqueueMessage(MessageService service, String queueHandle, Message m) {
        while(true) {
            try {
                service.enqueueMessage(queueHandle, m);
                break;
            } catch (NoSuchQueueException e) {
                /* ignore & retry */
            } catch (UnknownErrorException e) {
                /* retry */
            } catch (NoSuchClientException e) {
                LOGGER.severe("Client with this ID already registered.");
                throw new RuntimeException(e);
            }
        }
    }

    public Message retryDequeueMessage(MessageService service, String queueHandle) {
        while(true) {
            try {
                Message m = service.dequeueMessage(queueHandle);
                if (m == Message.NO_MESSAGE)
                    continue;
                return m;
            } catch (NoSuchQueueException e) {
                /* ignore & retry */
            } catch (UnknownErrorException e) {
                /* retry */
            } catch (NoSuchClientException e) {
                LOGGER.severe("Client with this ID already registered.");
                throw new RuntimeException(e);
            }
        }
    }
}
