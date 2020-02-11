package bg.sofia.uni.fmi.mjt.spotify.client;

import bg.sofia.uni.fmi.mjt.spotify.client.io.Client;
import bg.sofia.uni.fmi.mjt.spotify.client.logging.Logger;

import java.io.IOException;
import java.net.Socket;

/**
 * The main class for the .net client.
 *
 * @author angel.beshirov
 */
public class Runner {
    private static final int SERVER_PORT = 4444;
    private static final String SERVER_ERROR = "Error with the server!";

    public static void main(String... args) {
        try (Socket socket = new Socket("localhost", SERVER_PORT)) {
            Client client = new Client(socket);
            client.start();
        } catch (IOException e) {
            System.out.println(SERVER_ERROR);
            Logger.logError(SERVER_ERROR, e);
        }
    }
}
