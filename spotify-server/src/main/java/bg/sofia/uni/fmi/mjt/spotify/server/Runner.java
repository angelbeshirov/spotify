package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.server.impl.NetServer;
import bg.sofia.uni.fmi.mjt.spotify.server.logging.Logger;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * The entry point of the server application.
 *
 * @author angel.beshirov
 */
public class Runner {
    private static final int SERVER_PORT = 4444;
    private static final String SERVER_ERROR = "Error with the server!";

    public static void main(final String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            Server server = new NetServer(serverSocket);
            server.start();
        } catch (IOException e) {
            System.out.println(SERVER_ERROR);
            Logger.logError(SERVER_ERROR, e);
        }
    }
}
