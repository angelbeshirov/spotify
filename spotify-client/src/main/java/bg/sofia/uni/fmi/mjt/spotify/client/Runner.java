package bg.sofia.uni.fmi.mjt.spotify.client;

import bg.sofia.uni.fmi.mjt.spotify.client.io.Client;

/**
 * The main class to start the .net client.
 * @author angel.beshirov
 */
public class Runner {

    public static void main(String... args) {
        Client client = new Client();
        client.start();
    }
}
