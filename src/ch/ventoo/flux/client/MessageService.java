package ch.ventoo.flux.client;

import ch.ventoo.flux.exception.*;
import ch.ventoo.flux.model.Message;
import ch.ventoo.flux.model.Queue;
import ch.ventoo.flux.profiling.BenchLogger;
import ch.ventoo.flux.profiling.LogWrapper;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.command.*;
import ch.ventoo.flux.protocol.response.ResponseBinary;
import ch.ventoo.flux.protocol.response.ResponseError;
import ch.ventoo.flux.protocol.response.ResponseMessage;
import ch.ventoo.flux.protocol.response.ResponseQueues;

import java.io.IOException;

/**
 * Provides an object based interface to the message passing system.
 */
public class MessageService {

    private static LogWrapper LOGGER = new LogWrapper(MessageService.class);
    private final int _clientId;
    private final Connection _connection;

    public MessageService(int clientId, String host, int port, BenchLogger log) {
        _connection = new Connection(host, port, log);
        _clientId = clientId;
    }

    /**
     * Closes the connection to the server.
     */
    public void close() {
        try {
            _connection.close();
        } catch (IOException e) {
            LOGGER.severe("There was an error closing the client connection.");
        }
    }

    /**
     * Sends a ping message to the message passing server. Used to measure RTT.
     * @return
     * @throws UnknownErrorException
     */
    public boolean ping() throws UnknownErrorException {
        try {
            PingCommand cmd = new PingCommand();
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.ACK) {
                return true;
            } else {
                throw new UnknownErrorException();
            }
        } catch (IOException e) {
            LOGGER.severe("There was an I/O error.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Registers a client at the message passing system.
     * @return
     * @throws DuplicateClientException
     * @throws UnknownErrorException
     */
    public boolean register() throws DuplicateClientException, UnknownErrorException {
        try {
            RegisterClientCommand cmd = new RegisterClientCommand(_clientId);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.ACK) {
                return true;
            } else if (response.getType() == Protocol.Responses.ERROR) {
                if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.CLIENT_WITH_ID_EXISTS) {
                    throw new DuplicateClientException();
                } else {
                    throw new UnknownErrorException();
                }
            }
        } catch (IOException e) {
            LOGGER.severe("There was an I/O error.");
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * Deregisters a client at the message passing system.
     * @return
     * @throws NoSuchClientException
     * @throws UnknownErrorException
     */
    public boolean deregister() throws NoSuchClientException, UnknownErrorException {
        try {
            DeregisterClientCommand cmd = new DeregisterClientCommand(_clientId);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.ACK) {
                return true;
            } else if (response.getType() == Protocol.Responses.ERROR) {
                if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_CLIENT) {
                    throw new NoSuchClientException();
                } else {
                    throw new UnknownErrorException();
                }
            }
        } catch (IOException e) {
            LOGGER.severe("There was an I/O error.");
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * Creates a new queue in the message passing system with the given queue handle.
     * @param queueHandle
     * @return
     * @throws DuplicateQueueException
     * @throws UnknownErrorException
     */
    public boolean createQueue(String queueHandle) throws DuplicateQueueException, UnknownErrorException {
        try {
            CreateQueueCommand cmd = new CreateQueueCommand(queueHandle);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.ACK) {
                return true;
            } else if (response.getType() == Protocol.Responses.ERROR) {
                if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.DUPLICATE_QUEUE) {
                    throw new DuplicateQueueException();
                } else {
                    throw new UnknownErrorException();
                }
            }
        } catch (IOException e) {
            LOGGER.severe("There was an I/O error.");
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * Deletes an existing queue in the message passing system with the given queue handle.
     * @param queueHandle
     * @return
     * @throws NoSuchQueueException
     * @throws UnknownErrorException
     */
    public boolean deleteQueue(String queueHandle) throws NoSuchQueueException, UnknownErrorException {
        try {
            DeleteQueueCommand cmd = new DeleteQueueCommand(queueHandle);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.ACK) {
                return true;
            } else if (response.getType() == Protocol.Responses.ERROR) {
                if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_QUEUE) {
                    throw new NoSuchQueueException();
                } else {
                    throw new UnknownErrorException();
                }
            }
        } catch (IOException e) {
            LOGGER.severe("There was an I/O error.");
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * Checks whether a queue with the given queue handle is empty.
     * @param queueHandle
     * @return
     * @throws NoSuchQueueException
     * @throws UnknownErrorException
     */
    public boolean isQueueEmpty(String queueHandle) throws NoSuchQueueException, UnknownErrorException {
        try {
            IsQueueEmptyCommand cmd = new IsQueueEmptyCommand(queueHandle);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.BINARY) {
                return ((ResponseBinary)response).getState();
            } else if (response.getType() == Protocol.Responses.ERROR) {
                if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_QUEUE) {
                    throw new NoSuchQueueException();
                } else {
                    throw new UnknownErrorException();
                }
            }
        } catch (IOException e) {
            LOGGER.severe("There was an I/O error.");
            throw new RuntimeException(e);
        }
        return false;
    }

    /**
     * Queries for queues with pending messages.
     * @return
     * @throws UnknownErrorException
     */
    public Queue[] queryForQueues() throws UnknownErrorException {
        try {
            QueryForQueuesCommand cmd = new QueryForQueuesCommand();
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.QUEUES) {
                return ((ResponseQueues)response).getQueues();
            } else {
                throw new UnknownErrorException();
            }
        } catch (IOException e) {
            LOGGER.severe("There was an I/O error.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Queries for queues with pending messages form a given sender.
     * @param senderId
     * @return
     * @throws UnknownErrorException
     */
    public Queue[] queryForQueuesFromSender(int senderId) throws UnknownErrorException {
        try {
            QueryForQueuesFromSenderCommand cmd = new QueryForQueuesFromSenderCommand(senderId);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.QUEUES) {
                return ((ResponseQueues)response).getQueues();
            } else {
                throw new UnknownErrorException();
            }
        } catch (IOException e) {
            LOGGER.severe("There was an I/O error.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Enqueues a message into the queue with the given queue handle.
     * @param queueName
     * @param message
     * @return
     * @throws NoSuchQueueException
     * @throws UnknownErrorException
     */
    public boolean enqueueMessage(String queueName, Message message) throws NoSuchQueueException, UnknownErrorException, NoSuchClientException {
        try {

            message.setSender(_clientId);
            EnqueueMessageCommand cmd = new EnqueueMessageCommand(queueName, message);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.ACK) {
                return true;
            } else {
                if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_QUEUE) {
                    throw new NoSuchQueueException();
                } else if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_CLIENT) {
                    throw new NoSuchClientException();
                } else {
                    throw new UnknownErrorException();
                }
            }
        } catch (IOException e) {
            LOGGER.severe("There was an I/O error.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Dequeues a message from a queue with the given queue handle.
     * @param queueName
     * @return
     * @throws NoSuchQueueException
     * @throws UnknownErrorException
     */
    public Message dequeueMessage(String queueName) throws NoSuchQueueException, UnknownErrorException, NoSuchClientException {
        try {
            DequeueMessageCommand cmd = new DequeueMessageCommand(queueName, _clientId);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.MESSAGE) {
                return (((ResponseMessage)response).getMessage());
            } else if (response.getType() == Protocol.Responses.ACK) {
                return Message.NO_MESSAGE;
            } else {
                if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_QUEUE) {
                    throw new NoSuchQueueException();
                } else if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_CLIENT) {
                    throw new NoSuchClientException();
                } else {
                    throw new UnknownErrorException();
                }
            }
        } catch (IOException e) {
            LOGGER.severe("There was an I/O error.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Dequeues a message from a queue with the given queue handle and from a specific sender with given sender id.
     * @param queueName
     * @param senderId
     * @return
     * @throws NoSuchQueueException
     * @throws NoSuchClientException
     * @throws UnknownErrorException
     */
    public Message dequeueMessageFromSender(String queueName, int senderId) throws NoSuchQueueException, NoSuchClientException, UnknownErrorException {
        try {
            DequeueMessageFromSenderCommand cmd = new DequeueMessageFromSenderCommand(queueName, senderId, _clientId);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.MESSAGE) {
                return (((ResponseMessage)response).getMessage());
            } else if (response.getType() == Protocol.Responses.ACK) {
                return Message.NO_MESSAGE;
            } else {
                if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_QUEUE) {
                    throw new NoSuchQueueException();
                } else if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_CLIENT) {
                    throw new NoSuchClientException();
                } else {
                    throw new UnknownErrorException();
                }
            }
        } catch (IOException e) {
            LOGGER.severe("There was an I/O error.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a message from a queue with a given queue handle.
     * The message stays in the queue and will not be deleted until it will be dequeued.
     * @param queueName
     * @return
     * @throws NoSuchQueueException
     * @throws UnknownErrorException
     */
    public Message peekMessage(String queueName) throws NoSuchQueueException, UnknownErrorException, NoSuchClientException {
        try {
            PeekMessageCommand cmd = new PeekMessageCommand(queueName, _clientId);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.MESSAGE) {
                return (((ResponseMessage)response).getMessage());
            } else if (response.getType() == Protocol.Responses.ACK) {
                return Message.NO_MESSAGE;
            } else {
                if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_QUEUE) {
                    throw new NoSuchQueueException();
                } else if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_CLIENT) {
                    throw new NoSuchClientException();
                } else {
                    throw new UnknownErrorException();
                }
            }
        } catch (IOException e) {
            LOGGER.severe("There was an I/O error.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a message from a queue with a given queue handle and from a specific sender with the given sender id.
     * The message stays in the queue and will not be deleted until it will be dequeued.
     * @param queueName
     * @param senderId
     * @return
     * @throws NoSuchQueueException
     * @throws NoSuchClientException
     * @throws UnknownErrorException
     */
    public Message peekMessageFromSender(String queueName, int senderId) throws NoSuchQueueException, NoSuchClientException, UnknownErrorException {
        try {
            PeekMessageFromSenderCommand cmd = new PeekMessageFromSenderCommand(queueName, senderId, _clientId);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.MESSAGE) {
                return (((ResponseMessage)response).getMessage());
            } else if (response.getType() == Protocol.Responses.ACK) {
                return Message.NO_MESSAGE;
            } else {
                if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_QUEUE) {
                    throw new NoSuchQueueException();
                } else if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_CLIENT) {
                    throw new NoSuchClientException();
                } else {
                    throw new UnknownErrorException();
                }
            }
        } catch (IOException e) {
            LOGGER.severe("There was an I/O error.");
            throw new RuntimeException(e);
        }
    }

    public Message createAnonymousMessage(String content) {
        return new Message(_clientId, content);
    }

    public Message createDirectedMessage(int receiverId, String content) {
        return new Message(_clientId, receiverId, content);
    }

    public int getClientId() {
        return _clientId;
    }

}
