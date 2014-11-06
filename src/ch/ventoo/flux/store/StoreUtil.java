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
            if(rs != null) {
                rs.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeQuietly(Statement stmt) {
        try {
            if(stmt != null) {
                stmt.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void closeQuietly(Connection con) {
        try {
            if(con != null) {
                con.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Date convertStringToDate(String date) {
        try {
            return new Date(new SimpleDateFormat(DATE_FORMAT).parse(date).getTime());
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static String convertDateToString(Date date) {
        return new SimpleDateFormat(DATE_FORMAT).format(date);
    }

}
