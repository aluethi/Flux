package ch.ventoo.flux.util;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by nano on 12/10/14.
 */
public class StringUtil {

    private StringUtil() {

    }

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

    public static String readStringFromStream(DataInputStream stream) throws IOException {
        int length = stream.readInt();
        byte[] raw = new byte[length];
        stream.read(raw);
        return new String(raw);
    }

}
