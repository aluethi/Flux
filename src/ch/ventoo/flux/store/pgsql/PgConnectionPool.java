package ch.ventoo.flux.store.pgsql;

import org.postgresql.ds.PGPoolingDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by nano on 19/10/14.
 */
public class PgConnectionPool {

    private static PgConnectionPool INSTANCE_ = null;

    private PGPoolingDataSource _source;

    public static PgConnectionPool getInstance() {
        if(INSTANCE_ == null) {
            INSTANCE_ = new PgConnectionPool();
        }
        return INSTANCE_;
    }

    // TODO: Make configurable
    private PgConnectionPool() {
        _source = new PGPoolingDataSource();
        _source.setServerName("localhost");
        _source.setDatabaseName("flux");
        _source.setUser("nano");
        _source.setPassword("asdf");
        _source.setMaxConnections(10);
        _source.setInitialConnections(10);
    }

    public synchronized Connection getConnection() {
        Connection con = null;
        try {
            con = _source.getConnection();
            //con.setAutoCommit(false);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return con;
    }

}
