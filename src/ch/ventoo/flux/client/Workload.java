package ch.ventoo.flux.client;

import ch.ventoo.flux.exception.*;
import ch.ventoo.flux.model.Message;
import ch.ventoo.flux.profiling.BenchLogger;
import ch.ventoo.flux.profiling.LogWrapper;
import ch.ventoo.flux.profiling.Timing;
import ch.ventoo.flux.protocol.Protocol;

/**
 * Created by nano on 02/11/14.
 */
public abstract class Workload {

    private static final LogWrapper LOGGER = new LogWrapper(Workload.class);

    private boolean _stopped = false;
    private String _cachedPad = null;
    private int _cachedSize = 0;
    private final Timing _timer;

    public Workload(BenchLogger log) {
        _timer = new Timing(log);
    }

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

        _timer.setCommand(Protocol.Actions.REGISTER);
        _timer.enterRegion(Timing.Region.DATABASE);
        while(true) {
            try {
                service.register();
                _timer.enterRegion(Timing.Region.WAITING);
                _timer.setResponse(Protocol.Responses.ACK);
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
        _timer.setCommand(Protocol.Actions.CREATE_QUEUE);
        _timer.enterRegion(Timing.Region.DATABASE);
        while(true) {
            try {
                service.createQueue(queueHandle);
                _timer.enterRegion(Timing.Region.WAITING);
                _timer.setResponse(Protocol.Responses.ACK);
                break;
            } catch (DuplicateQueueException e) {
                break;
            } catch (UnknownErrorException e) {
                /* retry */
            }
        }
    }

    public void retryEnqueueMessage(MessageService service, String queueHandle, Message m) {
        _timer.setCommand(Protocol.Actions.ENQUEUE_MESSAGE);
        _timer.enterRegion(Timing.Region.DATABASE);
        while(true) {
            try {
                service.enqueueMessage(queueHandle, m);
                _timer.enterRegion(Timing.Region.WAITING);
                _timer.setResponse(Protocol.Responses.ACK);
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
        _timer.setCommand(Protocol.Actions.DEQUEUE_MESSAGE);
        _timer.enterRegion(Timing.Region.DATABASE);
        while(true) {
            try {
                Message m = service.dequeueMessage(queueHandle);
                if (m == Message.NO_MESSAGE)
                    continue;
                _timer.enterRegion(Timing.Region.WAITING);
                _timer.setResponse(Protocol.Responses.MESSAGE);
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
