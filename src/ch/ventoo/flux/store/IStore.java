package ch.ventoo.flux.store;

import ch.ventoo.flux.model.ICommand;

/**
 * Created by nano on 25/09/14.
 */
public interface IStore {

    /**
     * Client management methods
     *
     * A client can connect to the message passing system.
     * A client can disconnect from the message passing system.
     */
    public void registerClient(String clientName) throws IllegalStateException;
    public void deregisterClient(String clientName);
    public void signClientOn(String clientName);
    public void signClientOff(String clientName);


    /**
     * Queue management methods
     *
     * A client can create a new queue.
     * A client can delete an existing queue.
     * A client can check whether a queue is empty.
     * A client can query for queues where any messages are waiting. [Anonymous messages]
     * A client can query for queues where messages are waiting for them specifically.
     */
    public void createQueue(String queueName);
    public void deleteQueue(String queueName); // What if not empty?
    public void isQueueEmpty(String queueName);
    public void queryForQueues();
    public void queryForQueuesFromSender(int senderId);


    /**
     * Messaging methods
     *
     * A client can receive the message that is the oldest in a queue (topmost message)
     * A client can peek into the message that is the oldest in a queue (topmost message)
     * A client can post a message to a particular queue (if it exists) [Anonymous message]
     * A client can post a message to a particular queue (if it exists), that can only be accessed by a specified receiver
     * A client can query a queue (if it exists) for messages from a particular sender
     */
    public void enqueueMessage(String queueName, ICommand message);
    public ICommand dequeueMessage(String queueName); // [Anonymous messages]
    public ICommand dequeueMessageFromSender(String queueName, int senderId); // [Directed messages]
    public ICommand peekMessage(String queueName); // [Anonymous messages]
    public ICommand peekMessageFromSender(String queueName, int senderId); // [Directed messages]

}
