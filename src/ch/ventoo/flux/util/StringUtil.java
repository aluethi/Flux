package ch.ventoo.flux.util;

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

}
