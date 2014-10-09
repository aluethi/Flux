package ch.ventoo.flux;

import ch.ventoo.flux.transport.Acceptor;

import java.io.IOException;

public class Flux {

    public static void main(String[] args) {

        Acceptor acceptor = new Acceptor("0.0.0.0", 12345);
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
