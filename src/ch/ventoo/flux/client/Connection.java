package ch.ventoo.flux.client;

import ch.ventoo.flux.profiling.BenchLogger;
import ch.ventoo.flux.profiling.LogWrapper;
import ch.ventoo.flux.profiling.Timing;
import ch.ventoo.flux.protocol.Command;
import ch.ventoo.flux.protocol.ProtocolHandler;
import ch.ventoo.flux.protocol.Response;
import ch.ventoo.flux.transport.Frame;
import ch.ventoo.flux.transport.WireFormat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Connection - provides the basic network I/O to the client.
 */
public class Connection {

    private static LogWrapper LOGGER = new LogWrapper(Connection.class);

    private final String _host;
    private final int _port;
    private final Timing _timer;
    private Socket _socket;
    private DataInputStream _in;
    private DataOutputStream _out;
    private WireFormat _format = new WireFormat();

    public Connection(String host, int port, BenchLogger log) {
        _host = host;
        _port = port;
        _timer = new Timing(log);
        init();
    }

    protected void init() {
        try {
            _socket = new Socket(_host, _port);
            _in = new DataInputStream(_socket.getInputStream());
            _out = new DataOutputStream(_socket.getOutputStream());
        } catch (IOException e) {
            LOGGER.severe("Could not open client socket. Stopping.");
            throw new RuntimeException(e);
        }
    }

    /**
     * Reads a response from the network.
     * Registers the response at the timing infrastructure for benchmarking.
     * @return Response
     * @throws IOException
     */
    public Response readResponse() throws IOException {
        Response response = ProtocolHandler.parseResponse(readFrame());
        _timer.setResponse(response);
        _timer.enterRegion(Timing.Region.WAITING);
        return response;
    }

    /**
     * Reads the binary data from the network and unmarshalls it into Frame object.
     * @return Frame
     * @throws IOException
     */
    public Frame readFrame() throws IOException {
        Frame frame = _format.unmarshal(_in);
        return frame;
    }

    /**
     * Wrapper method to write a Command to the network.
     * Registers the outbound Command at the timing infrastructure.
     * @param cmd
     * @throws IOException
     */
    public void writeCommand(Command cmd) throws IOException {
        _timer.setCommand(cmd);
        _timer.enterRegion(Timing.Region.DATABASE);
        writeFrame(cmd.getFrame());
    }

    /**
     * Marshalls a frame into a binary stream to the network.
     * @param frame
     * @throws IOException
     */
    public void writeFrame(Frame frame) throws IOException {
        _format.marshal(_out, frame);
    }

    /**
     * Closes the I/O-streams and the socket.
     * @throws IOException
     */
    public void close() throws IOException {
        _in.close();
        _out.close();
        _socket.close();
    }

}
