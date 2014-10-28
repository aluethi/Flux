package ch.ventoo.flux.client;

import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.Protocol;
import ch.ventoo.flux.protocol.command.DeregisterClientCommand;
import ch.ventoo.flux.protocol.command.RegisterClientCommand;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by nano on 25/10/14.
 */
public class MessageServiceImpl {

    private final String _host;
    private final int _port;
    private Socket _socket;
    private DataInputStream _in;
    private DataOutputStream _out;

    public MessageServiceImpl(String host, int port) {
        _host = host;
        _port = port;
        init();
    }

    protected void init() {
        try {
            _socket = new Socket(_host, _port);
            _in = new DataInputStream(_socket.getInputStream());
            _out = new DataOutputStream(_socket.getOutputStream());
        } catch (IOException e) {
            //LOGGER_.log(Level.SEVERE, "Could not open socket. Stopping.");
            throw new RuntimeException(e);
        }
    }

    private void tearDown() throws IOException {
        _in.close();
        _out.close();
        _socket.close();
    }

    public boolean register(int clientId) {
        try {
            RegisterClientCommand cmd = new RegisterClientCommand(clientId);
            writeCommand(cmd);

            int msgType = _in.readInt();
            if(msgType != Protocol.Responses.ACK) {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    public boolean deregister(int clientId) {
        try {
            DeregisterClientCommand cmd = new DeregisterClientCommand(clientId);
            writeCommand(cmd);

            int msgType = _in.readInt();
            if(msgType != Protocol.Responses.ACK) {
                return false;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    private void writeCommand(Command cmd) throws IOException {
        byte[] body = cmd.getBody();
        _out.writeInt(body.length); // Message body size
        _out.write(body);
        _out.flush();
    }

}
