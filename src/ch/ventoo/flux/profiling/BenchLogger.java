package ch.ventoo.flux.profiling;

import ch.ventoo.flux.util.StringUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Benchmark logger used to log timed events in combination with the Timing class.
 */
public class BenchLogger {

    private static LogWrapper LOGGER = new LogWrapper(BenchLogger.class);

    private static String DELIMITER = "\t";

    private BufferedWriter _logWriter;
    private String _name;

    public BenchLogger(String name) {
        _name = name;
        openLogFile();
    }

    /**
     * Opens a log file.
     */
    public void openLogFile() {
        Date date = new Date(System.currentTimeMillis());
        String dateString = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss").format(date);
        String logFilePath = "log/" + _name + "-" + dateString + ".log";
        init(logFilePath);
    }

    /**
     * Initializes the BenchLogger to log to a specific file path.
     * @param logFilePath
     */
    private void init(String logFilePath) {
        File logFile = new File(logFilePath);
        logFile.getParentFile().mkdirs();
        try {
            logFile.createNewFile();
            _logWriter = new BufferedWriter(new FileWriter(logFile));
        } catch (IOException e) {
            LOGGER.severe("Could not open the log file path.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a log entry with reading in n params and concatenating them to a string delimited by DELIMITER.
     * @param params
     */
    public void addEntry(Object ... params) {
        try {
            _logWriter.write(StringUtil.join(DELIMITER, params) + "\n");
        } catch (IOException e) {
            LOGGER.severe("Could not write to the log file.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Adds a timestamped entry to the log file with reading in n params and concatenating them to a string
     * delimited by DELIMITER.
     * @param params
     */
    public void addTimedEntry(Object ... params) {
        long time = System.currentTimeMillis();
        if(params != null && params.length > 0) {
            params[0] = StringUtil.join(DELIMITER, time, params[0]);
            addEntry(params);
        } else {
            addEntry(time);
        }
    }

    /**
     * Flushes to the log file.
     */
    public void flush() {
        try {
            _logWriter.flush();
        } catch (IOException e) {
            LOGGER.severe("Could not flush to the log file.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Closes the opened log file.
     */
    public void closeFile() {
        try {
            _logWriter.close();
        } catch (IOException e) {
            LOGGER.severe("Could not close the log file.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Destructor used to cleanly close the log file.
     * @throws Throwable
     */
    public void finalize() throws Throwable {
        closeFile();
    }
}
