package ch.ventoo.flux.protocol.command;

import ch.ventoo.flux.exception.DuplicateClientException;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.response.ResponseAck;
import ch.ventoo.flux.protocol.response.ResponseError;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.SQLException;

/**
 * Command to register a client at the message passing system.
 */
public class RegisterClientCommand extends Command {

    private DataInputStream _stream;
    private int _clientId;

    public RegisterClientCommand(int clientId) {
        _clientId = clientId;
    }

    public RegisterClientCommand(DataInputStream stream) {
        _stream = stream;
    }

    @Override
    public int getType() {
        return Protocol.Actions.REGISTER;
    }

    @Override
    public byte[] getBody() {
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.putInt(getType());
        buffer.putInt(_clientId);
        return buffer.array();
    }

    @Override
    public Response execute() throws IOException {
        _clientId = _stream.readInt();
        _manager.beginConnectionScope();
        try {
            _manager.beginTransaction();
            _manager.getStore().registerClient(_clientId);
            _manager.endTransaction();
        } catch (DuplicateClientException e) {
            _manager.abortTransaction();
            return new ResponseError(Protocol.ErrorCodes.CLIENT_WITH_ID_EXISTS);
        } catch (SQLException e) {
            _manager.abortTransaction();
            return new ResponseError(Protocol.ErrorCodes.DATABASE_ERROR);
        } finally {
            _manager.endConnectionScope();
        }
        return new ResponseAck();
    }
}
