package ch.ventoo.flux.profiling;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by nano on 12/10/14.
 */
public class LogWrapper {
    private Logger _logger;

    public LogWrapper(Logger logger) {
        _logger = logger;
    }

    public LogWrapper(Class<?> cls) {
        this(Logger.getLogger(cls.getName()));
    }

    public void fine(String format, Object ... params) {
        log(Level.FINE, format, params);
    }

    public void config(String format, Object ... params) {
        log(Level.CONFIG, format, params);
    }

    public void info(String format, Object ... params) {
        log(Level.INFO, format, params);
    }

    public void warning(String format, Object ... params) {
        log(Level.WARNING, format, params);
    }

    public void severe(String format, Object ... params) {
        log(Level.SEVERE, format, params);
    }

    public void log(Level level, String format, Object ... params) {
        _logger.log(level, format, params);
    }
}
