package ch.ventoo.flux.profiling;

import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Response;

/**
 * Used to time code execution paths.
 */
public class Timing {

    /**
     * Code region markers.
     */
    public static enum Region {
        WAITING,
        MARSHALLING,
        DATABASE,
        RESPONSE
    }

    private BenchLogger _log;
    private long _lastTime = 0;
    private Region _lastRegion;
    private Object[] _regions = new Object[Region.values().length];
    private Object[] _types = null;

    public Timing(BenchLogger log) {
        _log = log;
    }

    /**
     * Method that gets called while entering a new timed region.
     * @param newRegion
     * @return
     */
    public synchronized long enterRegion(Region newRegion) {
        long currentTime = System.nanoTime();
        long timePassed = currentTime - _lastTime;
        if(_lastRegion != null) {
            _regions[_lastRegion.ordinal()] = timePassed;
            if(newRegion == Region.WAITING) {
                flush();
            }
        }
        _lastTime = currentTime;
        _lastRegion = newRegion;
        return timePassed;
    }

    /**
     * Flushes an entry to the log.
     */
    public void flush() {
        if(_types != null) {
            _log.addTimedEntry(_regions, _types);
            _types = new Object[2];
        } else {
            _log.addTimedEntry(_regions);
        }
        _regions = new Object[Region.values().length];
    }

    /**
     * Registers the command type (used for client benchmark logging).
     * @param cmd
     */
    public void setCommand(Command cmd) {
        setType(cmd.getType(), 0);
    }

    /**
     * Registers a response type (used for client benchmark logging).
     * @param response
     */
    public void setResponse(Response response) {
        setType(response.getType(), 1);
    }

    /**
     * Sets a message type that should be logged.
     * @param type
     * @param pos
     */
    public void setType(int type, int pos) {
        if(_types == null) {
            _types = new Object[2];
        }
        _types[pos] = type;
    }

}
