package ch.ventoo.flux.client;

/**
 * Created by nano on 02/11/14.
 */
public abstract class Workload {

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
}
