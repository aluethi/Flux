package ch.ventoo.flux.profiling;

import ch.ventoo.flux.util.StringUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;

/**
 * Created by nano on 10/10/14.
 */
public class BenchLogger {

    private static LogWrapper LOG = new LogWrapper(BenchLogger.class);
    private static String EXECUTION_ID;

    private static BenchLogger INSTANCE = new BenchLogger();
    private static String DELIMITER = "\t";

    private BufferedWriter _logWriter;

    public static void setExecutionId(String executionId) {
        EXECUTION_ID = executionId;
    }

    public void openLogFile() {
        Time time = new Time(System.currentTimeMillis());
        String logFilePath = "log/" + time.toString() + ".log";
        init(logFilePath);
    }

    private void init(String logFilePath) {
        File logFile = new File(logFilePath);
        logFile.getParentFile().mkdirs();
        try {
            logFile.createNewFile();
            _logWriter = new BufferedWriter(new FileWriter(logFile));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addEntry(Object ... params) {
        try {
            _logWriter.write(StringUtil.join(DELIMITER, params) + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addTimedEntry(Object ... params) {
        long time = System.currentTimeMillis();
        if(params != null && params.length > 0) {
            params[0] = StringUtil.join(DELIMITER, time, params[0]);
            addEntry(params);
        } else {
            addEntry(time);
        }
    }

    public void flush() {
        try {
            _logWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void closeFile() {
        try {
            _logWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void finalize() throws Throwable {
        closeFile();
        super.finalize();
    }
}
