package ch.ventoo.flux.client;

/**
 * Created by nano on 02/11/14.
 */
public abstract class Experiment {

    private boolean _stopped = false;

    public abstract void start(MessageService service, String[] args);

    public boolean isStopped() {
        return _stopped;
    }

    public void shutdown() {
        _stopped = true;
    }
}
