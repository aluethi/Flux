package ch.ventoo.flux;

import ch.ventoo.flux.transport.AcceptorOld;

import java.io.IOException;

public class Flux {

    public static void main(String[] args) {

        AcceptorOld acceptor = new AcceptorOld("0.0.0.0", 12345);
        try {
            acceptor.bind();
        } catch (IOException e) {
            e.printStackTrace();
        }

        new Thread(acceptor, "[Flex] Acceptor thread").start();
    }

    public Flux() {

    }

    public void start() {

    }
}
