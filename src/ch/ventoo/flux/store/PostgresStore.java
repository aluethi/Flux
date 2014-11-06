package ch.ventoo.flux.store;

import ch.ventoo.flux.exception.DuplicateClientException;
import ch.ventoo.flux.exception.DuplicateQueueException;
import ch.ventoo.flux.exception.NoSuchClientException;
import ch.ventoo.flux.exception.NoSuchQueueException;
import ch.ventoo.flux.model.Message;
import ch.ventoo.flux.model.Queue;
import ch.ventoo.flux.profiling.LogWrapper;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Data access object for a PostgreSQL database. Implements the Store interface.
 */
public class PostgresStore implements Store {

    private static LogWrapper LOGGER = new LogWrapper(PostgresStore.class);

    public final Connection _con;

    public PostgresStore(Connection connection) {
        _con = connection;
    }

    /**
     * See Store interface.
     * @param clientId
     * @return
     * @throws DuplicateClientException
     */
    @Override
    public boolean registerClient(int clientId) throws DuplicateClientException, SQLException {
        CallableStatement stmt = null;
        try {
            stmt = _con.prepareCall("{ call registerClient(?) }");
            stmt.setInt(1, clientId);
            stmt.execute();
        } catch (SQLException e) {
            StoreUtil.closeQuietly(stmt);
            if(e.getSQLState().equals("23505")) { // Duplicate key exception
                throw new DuplicateClientException();
            }
            throw e;
        } finally {
            StoreUtil.closeQuietly(stmt);
        }
        return true;
    }


    /**
     * See Store interface.
     * @param clientId
     * @return
     * @throws NoSuchClientException
     */
    @Override
    public boolean deregisterClient(int clientId) throws NoSuchClientException, SQLException {
        CallableStatement stmt = null;
        try {
            stmt = _con.prepareCall("{ call deregisterClient(?) }");
            stmt.setInt(1, clientId);
            stmt.execute();
        } catch (SQLException e) {
            StoreUtil.closeQuietly(stmt);
            if(e.getSQLState().equals("F0001")) { // No client with this id found
                throw new NoSuchClientException();
            }
            throw e;
        } finally {
            StoreUtil.closeQuietly(stmt);
        }
        return true;
    }

    /**
     * See Store interface.
     * @param queueName
     * @return
     * @throws DuplicateQueueException
     */
    @Override
    public Queue createQueue(String queueName) throws DuplicateQueueException, SQLException {
        CallableStatement stmt = null;
        ResultSet rs = null;
        try {
            stmt = _con.prepareCall("{ call createQueue(?) }");
            stmt.setString(1, queueName);
            rs = stmt.executeQuery();
            rs.next();
            Queue q = new Queue(rs.getInt(1), rs.getString(2), rs.getDate(3));
            return q;
        } catch (SQLException e) {
            StoreUtil.closeQuietly(rs);
            StoreUtil.closeQuietly(stmt);
            if(e.getSQLState().equals("F0002")) { // Queue with given handle already exists
                throw new DuplicateQueueException();
            }
            throw e;
        } finally {
            StoreUtil.closeQuietly(rs);
            StoreUtil.closeQuietly(stmt);
        }
    }

    /**
     * See Store interface.
     * @param queueName
     * @return
     * @throws NoSuchQueueException
     */
    @Override
    public boolean deleteQueue(String queueName) throws NoSuchQueueException, SQLException {
        CallableStatement stmt = null;
        try {
            stmt = _con.prepareCall("{ call deleteQueue(?) }");
            stmt.setString(1, queueName);
            stmt.execute();
        } catch (SQLException e) {
            StoreUtil.closeQuietly(stmt);
            if(e.getSQLState().equals("F0003")) { // No queue found with given handle
                throw new NoSuchQueueException();
            }
            throw e;
        } finally {
            StoreUtil.closeQuietly(stmt);
        }
        return false;
    }

    /**
     * See Store interface.
     * @param queueName
     * @return
     * @throws NoSuchQueueException
     */
    @Override
    public boolean isQueueEmpty(String queueName) throws NoSuchQueueException, SQLException {
        CallableStatement stmt = null;
        ResultSet rs = null;
        boolean result = false;
        try {
            stmt = _con.prepareCall("{ call isQueueEmpty(?) }");
            stmt.setString(1, queueName);
            rs = stmt.executeQuery();
            rs.next();
            result = rs.getBoolean(0);
            return result;
        } catch (SQLException e) {
            StoreUtil.closeQuietly(rs);
            StoreUtil.closeQuietly(stmt);
            if(e.getSQLState().equals("F0003")) { // No queue found with given handle
                throw new NoSuchQueueException();
            }
            throw e;
        } finally {
            StoreUtil.closeQuietly(rs);
            StoreUtil.closeQuietly(stmt);
        }
    }

    /**
     * See Store interface.
     * @return
     */
    @Override
    public Queue[] queryForQueues() throws SQLException {
        CallableStatement stmt = null;
        try {
            stmt = _con.prepareCall("{ call queryForQueues() }");
            return getQueues(stmt);
        } catch (SQLException e) {
            StoreUtil.closeQuietly(stmt);
            throw e;
        } finally {
            StoreUtil.closeQuietly(stmt);
        }
    }

    /**
     * See Store interface.
     * @param senderId
     * @return
     */
    @Override
    public Queue[] queryForQueuesFromSender(int senderId) throws SQLException {
        CallableStatement stmt = null;
        try {
            stmt = _con.prepareCall("{ call queryForQueuesFromSender(?) }");
            stmt.setInt(1, senderId);
            return getQueues(stmt);
        } catch (SQLException e) {
            StoreUtil.closeQuietly(stmt);
            throw e;
        } finally {
            StoreUtil.closeQuietly(stmt);
        }
    }

    /**
     * Parses a database result set into an array of queues.
     * @param stmt
     * @return
     * @throws SQLException
     */
    protected Queue[] getQueues(CallableStatement stmt) throws SQLException {
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();
            int rowCount = 0;
            if(rs.last()) {
                rowCount = rs.getRow();
                rs.beforeFirst();
            }

            Queue queues[] = new Queue[rowCount];

            for(int i = 0; i < rowCount; i++) {
                rs.next();
                queues[i] = new Queue(rs.getInt(1), rs.getString(2), rs.getDate(3));
            }
            return queues;
        } catch (SQLException e) {
            StoreUtil.closeQuietly(rs);
            throw e;
        } finally {
            StoreUtil.closeQuietly(rs);
        }
    }

    /**
     * See Store interface.
     * @param queueName
     * @param message
     * @return
     * @throws NoSuchQueueException
     * @throws NoSuchClientException
     */
    @Override
    public boolean enqueueMessage(String queueName, Message message) throws NoSuchQueueException, NoSuchClientException, SQLException {
        CallableStatement stmt = null;
        try {
            stmt = _con.prepareCall("{ call enqueueMessage(?, ?, ?, ?, ?) }");
            stmt.setString(1, queueName);
            stmt.setInt(2, message.getSender());
            stmt.setInt(3, message.getReceiver());
            stmt.setInt(4, message.getPriority());
            stmt.setString(5, message.getContent());
            stmt.execute();
        } catch (SQLException e) {
            StoreUtil.closeQuietly(stmt);
            if(e.getSQLState().equals("F0003")) { // No queue found with given handle
                throw new NoSuchQueueException();
            } else if (e.getSQLState().equals("F0001")) {
                throw new NoSuchClientException();
            }
            throw e;
        } finally {
            StoreUtil.closeQuietly(stmt);
        }
        return false;
    }

    /**
     * See Store interface.
     * @param queueName
     * @return
     * @throws NoSuchQueueException
     */
    @Override
    public Message dequeueMessage(String queueName, int receiverId) throws NoSuchQueueException, NoSuchClientException, SQLException {
        CallableStatement stmt = null;
        try {
            stmt = _con.prepareCall("{ call dequeueMessage(?, ?) }");
            stmt.setString(1, queueName);
            stmt.setInt(2, receiverId);
            return getMessage(stmt);
        } catch (SQLException e) {
            StoreUtil.closeQuietly(stmt);
            if(e.getSQLState().equals("F0003")) { // No queue found with given handle
                throw new NoSuchQueueException();
            } else if (e.getSQLState().equals("F0001")) {
                throw new NoSuchClientException();
            }
            throw e;
        } finally {
            StoreUtil.closeQuietly(stmt);
        }
    }

    /**
     * See Store interface.
     * @param queueName
     * @param senderId
     * @return
     * @throws NoSuchQueueException
     * @throws NoSuchClientException
     */
    @Override
    public Message dequeueMessageFromSender(String queueName, int senderId, int receiverId) throws NoSuchQueueException, NoSuchClientException, SQLException {
        CallableStatement stmt = null;
        try {
            stmt = _con.prepareCall("{ call dequeueMessageFromSender(?, ?, ?) }");
            stmt.setString(1, queueName);
            stmt.setInt(2, senderId);
            stmt.setInt(3, receiverId);
            return getMessage(stmt);
        } catch (SQLException e) {
            StoreUtil.closeQuietly(stmt);
            if(e.getSQLState().equals("F0003")) { // No queue found with given handle
                throw new NoSuchQueueException();
            } else if (e.getSQLState().equals("F0001")) {
                throw new NoSuchClientException();
            }
            throw e;
        } finally {
            StoreUtil.closeQuietly(stmt);
        }
    }

    /**
     * See Store interface.
     * @param queueName
     * @return
     * @throws NoSuchQueueException
     */
    @Override
    public Message peekMessage(String queueName, int receiverId) throws NoSuchQueueException, NoSuchClientException, SQLException {
        CallableStatement stmt = null;
        try {
            stmt = _con.prepareCall("{ call peekMessage(?, ?) }");
            stmt.setString(1, queueName);
            stmt.setInt(2, receiverId);
            return getMessage(stmt);
        } catch (SQLException e) {
            StoreUtil.closeQuietly(stmt);
            if(e.getSQLState().equals("F0003")) { // No queue found with given handle
                throw new NoSuchQueueException();
            } else if (e.getSQLState().equals("F0001")) {
                throw new NoSuchClientException();
            }
            throw e;
        } finally {
            StoreUtil.closeQuietly(stmt);
        }
    }

    /**
     * See Store interface.
     * @param queueName
     * @param senderId
     * @return
     * @throws NoSuchQueueException
     * @throws NoSuchClientException
     */
    @Override
    public Message peekMessageFromSender(String queueName, int senderId, int receiverId) throws NoSuchQueueException, NoSuchClientException, SQLException {
        CallableStatement stmt = null;
        try {
            stmt = _con.prepareCall("{ call peekMessageFromSender(?, ?, ?) }");
            stmt.setString(1, queueName);
            stmt.setInt(2, senderId);
            stmt.setInt(3, receiverId);
            return getMessage(stmt);
        } catch (SQLException e) {
            StoreUtil.closeQuietly(stmt);
            if(e.getSQLState().equals("F0003")) { // No queue found with given handle
                throw new NoSuchQueueException();
            } else if (e.getSQLState().equals("F0001")) {
                throw new NoSuchClientException();
            }
            throw e;
        } finally {
            StoreUtil.closeQuietly(stmt);
        }
    }

    /**
     * Parses a result set into a message object.
     * @param stmt
     * @return
     * @throws SQLException
     */
    private Message getMessage(CallableStatement stmt) throws SQLException {
        ResultSet rs = null;
        try {
            rs = stmt.executeQuery();
            rs.next();
            Message m = new Message(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(5), rs.getDate(6), rs.getString(7));
            if(m.getId() == 0) {
                return Message.NO_MESSAGE;
            } else {
                return m;
            }
        } catch (SQLException e) {
            StoreUtil.closeQuietly(rs);
            throw e;
        } finally {
            StoreUtil.closeQuietly(rs);
        }
    }
}
