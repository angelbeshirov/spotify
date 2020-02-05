package bg.sofia.uni.fmi.mjt.spotify.client;

import bg.sofia.uni.fmi.mjt.spotify.client.io.Client;

/**
 * @author angel.beshirov
 */
public class Runner {
    public static void main(String... args) {
        Client client = new Client();
        client.start();
    }
}
