package ch.ventoo.flux.protocol;

import ch.ventoo.flux.protocol.command.*;
import ch.ventoo.flux.protocol.response.*;
import ch.ventoo.flux.transport.Frame;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * Created by nano on 09/10/14.
 */
public class ProtocolHandler {

    public static Command parseCommand(Frame frame) {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(frame.getBody()));
        int action = 0;
        try {
            action = stream.readInt();
        } catch (IOException e) {
            e.printStackTrace(); // TODO
        }
        switch(action) {
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
                // should never happen
                return null;
        }
    }

    public static Frame prepareResponse(Response response) {
        byte[] body = response.getBody();
        int frameSize = body.length;
        Frame frame = new Frame(frameSize, body);
        return frame;
    }

    public static Response parseResponse(Frame frame) {
        DataInputStream stream = new DataInputStream(new ByteArrayInputStream(frame.getBody()));
        int type = 0;
        try {
            type = stream.readInt();
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
                    // should never happen
                    return null;
            }
        } catch (IOException e) {
            e.printStackTrace(); // TODO
        }
        return null;
    }
}
