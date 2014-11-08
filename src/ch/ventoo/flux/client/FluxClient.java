package ch.ventoo.flux.client;

import ch.ventoo.flux.profiling.BenchLogger;

import java.util.Arrays;

/**
 * Created by nano on 25/10/14.
 */
public class FluxClient {

    private WorkloadExecutor _executor;

    public static void main(String[] args) {

        if(args.length < 4) {
            System.err.println("Usage: java -jar flux-client.jar <client id> <host> <port> <experiment> [args]");
            System.exit(1);
        }

        int clientId = Integer.parseInt(args[0]);
        String host = args[1];
        String port = args[2];
        String experiment = args[3];
        String[] arguments = Arrays.copyOfRange(args, 4, args.length);

        new FluxClient(clientId, host, Integer.parseInt(port), experiment, arguments);
    }

    public FluxClient(int clientId, String host, int port, String experiment, String[] args) {
        BenchLogger log = new BenchLogger("client-" + clientId);
        _executor = new WorkloadExecutor(clientId, host, port, log);
        _executor.executeWorkload(experiment, args);
    }

}
