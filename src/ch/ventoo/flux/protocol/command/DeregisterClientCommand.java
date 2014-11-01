package ch.ventoo.flux.protocol.command;

import ch.ventoo.flux.exception.NoSuchClientException;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.response.ResponseAck;
import ch.ventoo.flux.protocol.response.ResponseError;
import ch.ventoo.flux.store.PostgresStore;
import ch.ventoo.flux.store.pgsql.PgConnectionPool;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by nano on 22/10/14.
 */
public class DeregisterClientCommand extends Command {

    private DataInputStream _stream;
    private int _clientId;

    public DeregisterClientCommand(int clientId) {
        _clientId = clientId;
    }

    public DeregisterClientCommand(DataInputStream stream) {
        _stream = stream;
    }

    @Override
    public int getType() {
        return Protocol.Actions.DEREGISTER;
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
        PostgresStore store = new PostgresStore(PgConnectionPool.getInstance().getConnection());
        try {
            store.deregisterClient(_clientId);
            return new ResponseAck();
        } catch (NoSuchClientException e) {
            return new ResponseError(Protocol.ErrorCodes.NO_SUCH_CLIENT);
        }
    }
}
