package ch.ventoo.flux.store.pgsql;

import ch.ventoo.flux.config.Configuration;
import ch.ventoo.flux.profiling.LogWrapper;
import org.postgresql.ds.PGPoolingDataSource;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Connection pool for use by the PostgresStore data access object.
 */
public class PgConnectionPool {

    private static LogWrapper LOGGER = new LogWrapper(PgConnectionPool.class);
    private static PgConnectionPool INSTANCE = null;

    private PGPoolingDataSource _source;

    public static PgConnectionPool getInstance() {
        if(INSTANCE == null) {
            INSTANCE = new PgConnectionPool();
        }
        return INSTANCE;
    }

    /**
     * Initializes the connection pool.
     */
    private PgConnectionPool() {
        _source = new PGPoolingDataSource();
        _source.setServerName(Configuration.getProperty("db.server.name"));
        _source.setDatabaseName(Configuration.getProperty("db.database.name"));
        _source.setUser(Configuration.getProperty("db.user"));
        _source.setPassword(Configuration.getProperty("db.password"));
        _source.setMaxConnections(Integer.parseInt(Configuration.getProperty("db.pool.max")));
        _source.setInitialConnections(Integer.parseInt(Configuration.getProperty("db.pool.initial")));
    }

    /**
     * Returns a connection to the database.
     * @return
     */
    public synchronized Connection getConnection() {
        Connection con = null;
        try {
            con = _source.getConnection();
            con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        } catch (SQLException e) {
            LOGGER.severe("Could not retrieve connection from the connection pool.");
            throw new RuntimeException(e);
        }
        return con;
    }

}
