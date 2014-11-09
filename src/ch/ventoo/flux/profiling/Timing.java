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
    private Object[] _regions = new Object[Region.values().length + 2];

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
        _log.addTimedEntry(_regions);
        _regions = new Object[Region.values().length + 2];
    }

    /**
     * Registers the command type (used for client benchmark logging).
     * @param cmd
     */
    public void setCommand(Command cmd) {
        _regions[_regions.length - 2] = cmd.getType();
    }

    public void setCommand(int cmd) {
        _regions[_regions.length - 2] = cmd;
    }

    /**
     * Registers a response type (used for client benchmark logging).
     * @param response
     */
    public void setResponse(Response response) {
        _regions[_regions.length - 1] = response.getType();
    }

    public void setResponse(int response) {
        _regions[_regions.length - 1] = response;
    }

}
