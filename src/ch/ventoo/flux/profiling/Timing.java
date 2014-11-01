package ch.ventoo.flux.profiling;

import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Response;

/**
 * Created by nano on 29/10/14.
 */
public class Timing {

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

    public void flush() {
        if(_types != null) {
            _log.addTimedEntry(_regions, _types);
            _types = new Object[2];
        } else {
            _log.addTimedEntry(_regions);
        }
        _regions = new Object[Region.values().length];
    }

    public void setCommand(Command cmd) {
        setType(cmd.getType(), 0);
    }

    public void setResponse(Response response) {
        setType(response.getType(), 1);
    }

    public void setType(int type, int pos) {
        if(_types == null) {
            _types = new Object[2];
        }
        _types[pos] = type;
    }

}
