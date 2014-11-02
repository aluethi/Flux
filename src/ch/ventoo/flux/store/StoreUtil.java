package ch.ventoo.flux.store;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Utility methods for the database connection.
 */
public class StoreUtil {

    private static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    public static void closeQuietly(ResultSet rs) {
        try {
            rs.close();
        } catch (Exception e) {
            /* ignored */
        }
    }

    public static void closeQuietly(Statement stmt) {
        try {
            stmt.close();
        } catch (Exception e) {
            /* ignored */
        }
    }

    public static void closeQuietly(Connection con) {
        try {
            con.close();
        } catch (Exception e) {
            /* ignored */
        }
    }

    public static Date convertStringToDate(String date) {
        try {
            return new Date(new SimpleDateFormat(DATE_FORMAT).parse(date).getTime());
        } catch (ParseException e) {
            /* Ignored */
        }
        return null;
    }

    public static String convertDateToString(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

}
