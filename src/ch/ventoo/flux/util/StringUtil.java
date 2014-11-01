package ch.ventoo.flux.util;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * String utilities.
 */
public class StringUtil {

    private StringUtil() {

    }

    /**
     * Join multiple strings to one large string delimited by a given delimiter.
     * @param delim
     * @param params
     * @return
     */
    public static String join(String delim, Object ... params) {
        if(params.length > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(params[0]);
            for(int i = 1; i < params.length; i++) {
                sb.append(params[i]);
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * Read a string from the network (leading size as int and following byte array as string content).
     * @param stream
     * @return
     * @throws IOException
     */
    public static String readStringFromStream(DataInputStream stream) throws IOException {
        int length = stream.readInt();
        byte[] raw = new byte[length];
        stream.read(raw);
        return new String(raw);
    }

}
