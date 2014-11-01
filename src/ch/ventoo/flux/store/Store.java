package ch.ventoo.flux.store;

import ch.ventoo.flux.exception.DuplicateClientException;
import ch.ventoo.flux.exception.DuplicateQueueException;
import ch.ventoo.flux.exception.NoSuchClientException;
import ch.ventoo.flux.exception.NoSuchQueueException;
import ch.ventoo.flux.model.Message;
import ch.ventoo.flux.model.Queue;

import java.sql.SQLException;

/**
 * Store interface to abstract a specific store (e.g. database) from the rest of the system.
 */
public interface Store {

    /**
     * Client management methods
     *
     * A client can connect to the message passing system.
     * A client can disconnect from the message passing system.
     * @param clientName
     */
    public boolean registerClient(int clientName) throws DuplicateClientException;
    public boolean deregisterClient(int clientName) throws NoSuchClientException;


    /**
     * Queue management methods
     *
     * A client can create a new queue.
     * A client can delete an existing queue.
     * A client can check whether a queue is empty.
     * A client can query for queues where any messages are waiting. [Anonymous messages]
     * A client can query for queues where messages are waiting for them specifically.
     */
    public Queue createQueue(String queueName) throws DuplicateQueueException;
    public boolean deleteQueue(String queueName) throws NoSuchQueueException; // What if not empty?
    public boolean isQueueEmpty(String queueName) throws NoSuchQueueException;
    public Queue[] queryForQueues();
    public Queue[] queryForQueuesFromSender(int senderId);


    /**
     * Messaging methods
     *
     * A client can receive the message that is the oldest in a queue (topmost message)
     * A client can peek into the message that is the oldest in a queue (topmost message)
     * A client can post a message to a particular queue (if it exists) [Anonymous message]
     * A client can post a message to a particular queue (if it exists), that can only be accessed by a specified receiver
     * A client can query a queue (if it exists) for messages from a particular sender
     */
    public boolean enqueueMessage(String queueName, Message message) throws NoSuchQueueException, NoSuchClientException;
    public Message dequeueMessage(String queueName) throws NoSuchQueueException; // [Anonymous messages]
    public Message dequeueMessageFromSender(String queueName, int senderId) throws NoSuchQueueException, NoSuchClientException; // [Directed messages]
    public Message peekMessage(String queueName) throws NoSuchQueueException; // [Anonymous messages]
    public Message peekMessageFromSender(String queueName, int senderId) throws NoSuchQueueException, NoSuchClientException; // [Directed messages]

}
