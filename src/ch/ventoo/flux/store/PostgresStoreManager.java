package ch.ventoo.flux.store;

import ch.ventoo.flux.profiling.LogWrapper;
import ch.ventoo.flux.store.pgsql.PgConnectionPool;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by nano on 05/11/14.
 */
public class PostgresStoreManager {

    private static final LogWrapper LOGGER = new LogWrapper(PostgresStoreManager.class);

    private Connection _connection;
    private PostgresStore _store = null;

    public void beginConnectionScope() {
        _connection = PgConnectionPool.getInstance().getConnection();
    }

    public void endConnectionScope() {
        try {
            _connection.close();
        } catch (SQLException e) {
            LOGGER.severe("Could not close DB connection.");
            throw new RuntimeException(e);
        }
    }

    public void beginTransaction() {
        try {
            _connection.setAutoCommit(false);
            _connection.setTransactionIsolation(_connection.TRANSACTION_SERIALIZABLE);
        } catch (SQLException e) {
            LOGGER.severe("Could not begin transaction.");
            throw new RuntimeException(e);
        }
    }

    public void endTransaction() throws SQLException {
        _connection.commit();
    }

    public void abortTransaction() {
        try {
            _connection.rollback();
        } catch (SQLException e) {
            LOGGER.severe("Could not abort transaction.");
            throw new RuntimeException(e);
        }
    }

    public Store getStore() {
        if(_store == null) {
            _store = new PostgresStore(_connection);
        }
        return _store;
    }

}
