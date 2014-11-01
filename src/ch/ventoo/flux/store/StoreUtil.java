package ch.ventoo.flux.store;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Utility methods for the database connection.
 */
public class StoreUtil {

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

}
