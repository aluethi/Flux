package ch.ventoo.flux.protocol;

import ch.ventoo.flux.profiling.LogWrapper;
import ch.ventoo.flux.protocol.command.*;
import ch.ventoo.flux.protocol.response.*;
import ch.ventoo.flux.transport.Frame;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Protocol handler implements several static methods that interface between the wire format (frames) and
 * specific commands and responses of the implemented protocol.
 * This handler is used on the server as well as on the client side.
 */
public class ProtocolHandler {

    private static LogWrapper LOGGER = new LogWrapper(ProtocolHandler.class);

    /**
     * Parses a frame into a specific command.
     * @param frame
     * @return
     */
    public static Command parseCommand(Frame frame) {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(frame.getBody()));
        try {
            int action = stream.readInt();
            switch(action) {
                case Protocol.Actions.PING:
                    return new PingCommand(stream);
                case Protocol.Actions.REGISTER:
                    return new RegisterClientCommand(stream);
                case Protocol.Actions.DEREGISTER:
                    return new DeregisterClientCommand(stream);
                case Protocol.Actions.CREATE_QUEUE:
                    return new CreateQueueCommand(stream);
                case Protocol.Actions.IS_QUEUE_EMPTY:
                    return new IsQueueEmptyCommand(stream);
                case Protocol.Actions.DELETE_QUEUE:
                    return new DeleteQueueCommand(stream);
                case Protocol.Actions.ENQUEUE_MESSAGE:
                    return new EnqueueMessageCommand(stream);
                case Protocol.Actions.DEQUEUE_MESSAGE:
                    return new DequeueMessageCommand(stream);
                case Protocol.Actions.DEQUEUE_MESSAGE_FROM_SENDER:
                    return new DequeueMessageFromSenderCommand(stream);
                case Protocol.Actions.PEEK_MESSAGE:
                    return new PeekMessageCommand(stream);
                case Protocol.Actions.PEEK_MESSAGE_FROM_SENDER:
                    return new PeekMessageFromSenderCommand(stream);
                case Protocol.Actions.QUERY_FOR_QUEUES:
                    return new QueryForQueuesCommand(stream);
                case Protocol.Actions.QUERY_FOR_QUEUES_FROM_SENDER:
                    return new QueryForQueuesFromSenderCommand(stream);
                default:
                    LOGGER.warning("Received an illegal command.");
                    throw new RuntimeException();
            }
        } catch (IOException e) {
            LOGGER.warning("Could not read action from the received frame.");
        }
        return null;
    }

    /**
     * Creates a frame from a response.
     * @param response
     * @return
     */
    public static Frame prepareResponse(Response response) {
        byte[] body = response.getBody();
        int frameSize = body.length;
        return new Frame(frameSize, body);
    }

    /**
     * Parses a frame into a response. Used on the client side.
     * @param frame
     * @return
     */
    public static Response parseResponse(Frame frame) {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(frame.getBody()));
        try {
            int type = stream.readInt();
            switch(type) {
                case Protocol.Responses.ACK:
                    return new ResponseAck();
                case Protocol.Responses.ERROR:
                    Response error = new ResponseError();
                    error.initFromStream(stream);
                    return error;
                case Protocol.Responses.MESSAGE:
                    Response message = new ResponseMessage();
                    message.initFromStream(stream);
                    return message;
                case Protocol.Responses.BINARY:
                    Response binary = new ResponseBinary();
                    binary.initFromStream(stream);
                    return binary;
                case Protocol.Responses.QUEUES:
                    Response queues = new ResponseQueues();
                    queues.initFromStream(stream);
                    return queues;
                default:
                    LOGGER.warning("Received an illegal response.");
                    return null;
            }
        } catch (IOException e) {
            LOGGER.warning("Could not read response type from frame.");
        }
        return null;
    }
}
