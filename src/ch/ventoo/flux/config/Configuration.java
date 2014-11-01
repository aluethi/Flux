package ch.ventoo.flux.config;

import ch.ventoo.flux.profiling.LogWrapper;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * Configuration - Reads in the configuration while being statically
 * initialized the first time the class is used.
 *
 * @auhor aluethi
 */
public class Configuration {

    private static String CONFIG_PROPS_FILE = "config.prop";
    private static LogWrapper LOGGER = new LogWrapper(Configuration.class);

    private static Properties PROPERTIES = new Properties();

    static {
        try {
            BufferedInputStream stream = new BufferedInputStream(new FileInputStream(CONFIG_PROPS_FILE));
            PROPERTIES.load(stream);
            stream.close();
        } catch (FileNotFoundException e) {
            LOGGER.severe("No configuration file found.");
            throw new RuntimeException(e);
        } catch (IOException e) {
            LOGGER.severe("Could not read configuration file.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a named property value
     * @param name
     * @return
     */
    public static String getProperty(String name) {
        return PROPERTIES.getProperty(name);
    }

}
