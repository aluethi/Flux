package ch.ventoo.flux.client;

import ch.ventoo.flux.exception.*;
import ch.ventoo.flux.model.Message;
import ch.ventoo.flux.model.Queue;
import ch.ventoo.flux.profiling.BenchLogger;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.ProtocolHandler;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.command.*;
import ch.ventoo.flux.protocol.response.ResponseBinary;
import ch.ventoo.flux.protocol.response.ResponseError;
import ch.ventoo.flux.protocol.response.ResponseMessage;
import ch.ventoo.flux.protocol.response.ResponseQueues;

import java.io.IOException;

/**
 * Created by nano on 25/10/14.
 */
public class MessageService {

    private final Connection _connection;

    public MessageService(String host, int port, BenchLogger log) {
        _connection = new Connection(host, port, log);
    }

    public void close() {
        try {
            _connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

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
            throw new RuntimeException(e);
        }
    }

    public boolean register(int clientId) throws DuplicateClientException, UnknownErrorException {
        try {
            RegisterClientCommand cmd = new RegisterClientCommand(clientId);
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
            throw new RuntimeException(e);
        }
        return false;
    }

    public boolean deregister(int clientId) throws NoSuchClientException, UnknownErrorException {
        try {
            DeregisterClientCommand cmd = new DeregisterClientCommand(clientId);
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
            throw new RuntimeException(e);
        }
        return false;
    }

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
            throw new RuntimeException(e);
        }
        return false;
    }

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
            throw new RuntimeException(e);
        }
        return false;
    }

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
            throw new RuntimeException(e);
        }
        return false;
    }

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
            throw new RuntimeException(e);
        }
    }

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
            throw new RuntimeException(e);
        }
    }

    public boolean enqueueMessage(String queueName, Message message) throws NoSuchQueueException, UnknownErrorException {
        try {
            EnqueueMessageCommand cmd = new EnqueueMessageCommand(queueName, message);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.ACK) {
                return true;
            } else {
                if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_QUEUE) {
                    throw new NoSuchQueueException();
                } else {
                    throw new UnknownErrorException();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Message dequeueMessage(String queueName) throws NoSuchQueueException, UnknownErrorException {
        try {
            DequeueMessageCommand cmd = new DequeueMessageCommand(queueName);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.MESSAGE) {
                return (((ResponseMessage)response).getMessage());
            } else {
                if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_QUEUE) {
                    throw new NoSuchQueueException();
                } else {
                    throw new UnknownErrorException();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Message dequeueMessageFromSender(String queueName, int senderId) throws NoSuchQueueException, NoSuchClientException, UnknownErrorException {
        try {
            DequeueMessageFromSenderCommand cmd = new DequeueMessageFromSenderCommand(queueName, senderId);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.MESSAGE) {
                return (((ResponseMessage)response).getMessage());
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
            throw new RuntimeException(e);
        }
    }

    public Message peekMessage(String queueName) throws NoSuchQueueException, UnknownErrorException {
        try {
            PeekMessageCommand cmd = new PeekMessageCommand(queueName);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.MESSAGE) {
                return (((ResponseMessage)response).getMessage());
            } else {
                if(((ResponseError)response).getErrorCode() == Protocol.ErrorCodes.NO_SUCH_QUEUE) {
                    throw new NoSuchQueueException();
                } else {
                    throw new UnknownErrorException();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Message peekMessageFromSender(String queueName, int senderId) throws NoSuchQueueException, NoSuchClientException, UnknownErrorException {
        try {
            PeekMessageFromSenderCommand cmd = new PeekMessageFromSenderCommand(queueName, senderId);
            _connection.writeCommand(cmd);
            Response response = _connection.readResponse();

            if(response.getType() == Protocol.Responses.MESSAGE) {
                return (((ResponseMessage)response).getMessage());
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
            throw new RuntimeException(e);
        }
    }



}
