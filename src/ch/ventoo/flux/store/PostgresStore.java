package ch.ventoo.flux.store;

import ch.ventoo.flux.exception.DuplicateClientException;
import ch.ventoo.flux.exception.DuplicateQueueException;
import ch.ventoo.flux.exception.NoSuchClientException;
import ch.ventoo.flux.exception.NoSuchQueueException;
import ch.ventoo.flux.model.Message;
import ch.ventoo.flux.model.Queue;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by nano on 18/10/14.
 */
public class PostgresStore implements Store {

    public final Connection _con;

    public PostgresStore(Connection connection) {
        _con = connection;
    }

    // TODO: Get some feedback from the DB
    @Override
    public boolean registerClient(int clientId) throws DuplicateClientException {
        try {
            CallableStatement stmt = _con.prepareCall("{ call registerClient(?) }");
            stmt.setInt(1, clientId);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            if(e.getSQLState().equals("23505")) { // Duplicate key exception
                throw new DuplicateClientException();
            }
            // Can't gracefully handle other exceptions (e.g. missing database connection)
            throw new RuntimeException(e);
        }
        return true;
    }


    // TODO: Get some feedback from the DB
    @Override
    public boolean deregisterClient(int clientId) throws NoSuchClientException {
        try {
            CallableStatement stmt = _con.prepareCall("{ call deregisterClient(?) }");
            stmt.setInt(1, clientId);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            if(e.getSQLState().equals("F0001")) { // No client with this id found
                throw new NoSuchClientException();
            }
            // Can't gracefully handle other exceptions (e.g. missing database connection)
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public Queue createQueue(String queueName) throws DuplicateQueueException {
        try {
            CallableStatement stmt = _con.prepareCall("{ call createQueue(?) }");
            stmt.setString(1, queueName);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            Queue q = new Queue(rs.getInt(1), rs.getString(2), rs.getDate(3));
            rs.close();
            stmt.close();
            return q;
        } catch (SQLException e) {
            if(e.getSQLState().equals("F0002")) { // Queue with given handle already exists
                throw new DuplicateQueueException();
            }
            // Can't gracefully handle other exceptions (e.g. missing database connection)
            throw new RuntimeException(e);
        }
    }

    // TODO: Get some feedback from the DB
    @Override
    public boolean deleteQueue(String queueName) throws NoSuchQueueException {
        try {
            CallableStatement stmt = _con.prepareCall("{ call deleteQueue(?) }");
            stmt.setString(1, queueName);
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            if(e.getSQLState().equals("F0003")) { // No queue found with given handle
                throw new NoSuchQueueException();
            }
            // Can't gracefully handle other exceptions (e.g. missing database connection)
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public boolean isQueueEmpty(String queueName) throws NoSuchQueueException {
        boolean result = false;
        try {
            CallableStatement stmt = _con.prepareCall("{ call isQueueEmpty(?) }");
            stmt.setString(1, queueName);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            result = rs.getBoolean(0);
            rs.close();
            stmt.close();
            return result;
        } catch (SQLException e) {
            if(e.getSQLState().equals("F0003")) { // No queue found with given handle
                throw new NoSuchQueueException();
            }
            // Can't gracefully handle other exceptions (e.g. missing database connection)
            throw new RuntimeException(e);
        }
    }

    @Override
    public Queue[] queryForQueues() {
        CallableStatement stmt = null;
        try {
            stmt = _con.prepareCall("{ call queryForQueues() }");
            return getQueues(stmt);
        } catch (SQLException e) {
            // Can't gracefully handle exceptions (e.g. missing database connection)
            throw new RuntimeException(e);
        }
    }

    @Override
    public Queue[] queryForQueuesFromSender(int senderId) {
        CallableStatement stmt = null;
        try {
            stmt = _con.prepareCall("{ call queryForQueuesFromSender(?) }");
            stmt.setInt(1, senderId);
            return getQueues(stmt);
        } catch (SQLException e) {
            // Can't gracefully handle exceptions (e.g. missing database connection)
            throw new RuntimeException(e);
        }
    }

    protected Queue[] getQueues(CallableStatement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery();

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

        rs.close();
        stmt.close();
        return queues;
    }

    @Override
    public boolean enqueueMessage(String queueName, Message message) throws NoSuchQueueException, NoSuchClientException {
        try {
            CallableStatement stmt = _con.prepareCall("{ call enqueueMessage(?, ?, ?, ?, ?) }");
            stmt.setString(1, queueName);
            stmt.setInt(2, message.getSender());
            stmt.setInt(3, message.getReceiver());
            stmt.setInt(4, message.getPriority());
            stmt.setString(5, message.getContent());
            stmt.execute();
            stmt.close();
        } catch (SQLException e) {
            if(e.getSQLState().equals("F0003")) { // No queue found with given handle
                throw new NoSuchQueueException();
            } else if (e.getSQLState().equals("F0001")) {
                throw new NoSuchClientException();
            }
            // Can't gracefully handle other exceptions (e.g. missing database connection)
            throw new RuntimeException(e);
        }
        return false;
    }

    @Override
    public Message dequeueMessage(String queueName) throws NoSuchQueueException {
        try {
            CallableStatement stmt = _con.prepareCall("{ call dequeueMessage(?) }");
            stmt.setString(1, queueName);
            return getMessage(stmt);
        } catch (SQLException e) {
            if(e.getSQLState().equals("F0003")) { // No queue found with given handle
                throw new NoSuchQueueException();
            }
            // Can't gracefully handle other exceptions (e.g. missing database connection)
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message dequeueMessageFromSender(String queueName, int senderId) throws NoSuchQueueException, NoSuchClientException {
        try {
            CallableStatement stmt = _con.prepareCall("{ call dequeueMessageFromSender(?) }");
            stmt.setString(1, queueName);
            stmt.setInt(2, senderId);
            return getMessage(stmt);
        } catch (SQLException e) {
            if(e.getSQLState().equals("F0003")) { // No queue found with given handle
                throw new NoSuchQueueException();
            } else if (e.getSQLState().equals("F0001")) {
                throw new NoSuchClientException();
            }
            // Can't gracefully handle other exceptions (e.g. missing database connection)
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message peekMessage(String queueName) throws NoSuchQueueException {
        try {
            CallableStatement stmt = _con.prepareCall("{ call peekMessage(?) }");
            stmt.setString(1, queueName);
            return getMessage(stmt);
        } catch (SQLException e) {
            if(e.getSQLState().equals("F0003")) { // No queue found with given handle
                throw new NoSuchQueueException();
            }
            // Can't gracefully handle other exceptions (e.g. missing database connection)
            throw new RuntimeException(e);
        }
    }

    @Override
    public Message peekMessageFromSender(String queueName, int senderId) throws NoSuchQueueException, NoSuchClientException {
        try {
            CallableStatement stmt = _con.prepareCall("{ call peekMessageFromSender(?) }");
            stmt.setString(1, queueName);
            stmt.setInt(2, senderId);
            return getMessage(stmt);
        } catch (SQLException e) {
            if(e.getSQLState().equals("F0003")) { // No queue found with given handle
                throw new NoSuchQueueException();
            } else if (e.getSQLState().equals("F0001")) {
                throw new NoSuchClientException();
            }
            // Can't gracefully handle other exceptions (e.g. missing database connection)
            throw new RuntimeException(e);
        }
    }

    private Message getMessage(CallableStatement stmt) throws SQLException {
        ResultSet rs = stmt.executeQuery();
        rs.next();
        Message m = new Message(rs.getInt(1), rs.getInt(2), rs.getInt(3), rs.getInt(5), rs.getDate(6), rs.getString(7));
        rs.close();
        stmt.close();
        return m;
    }
}
