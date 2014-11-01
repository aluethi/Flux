package ch.ventoo.flux.protocol.command;

import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.protocol.response.ResponseAck;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Command to ping the message passing system. Used to measure RTT.
 */
public class PingCommand extends Command {

    public PingCommand() { }
    public PingCommand(DataInputStream stream) { }

    @Override
    public int getType() {
        return Protocol.Actions.PING;
    }

    @Override
    public byte[] getBody() {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(getType());
        return buffer.array();
    }

    @Override
    public Response execute() throws IOException {
        return new ResponseAck();
    }
}
