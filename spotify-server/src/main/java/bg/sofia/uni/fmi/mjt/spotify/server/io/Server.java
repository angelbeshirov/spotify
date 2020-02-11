package bg.sofia.uni.fmi.mjt.spotify.server.io;

import bg.sofia.uni.fmi.mjt.spotify.model.ServerData;
import bg.sofia.uni.fmi.mjt.spotify.server.client.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author angel.beshirov
 */
public class Server {

    private static final int SERVER_PORT = 4444;
    private static final int MAX_THREADS = 15;
    private final ExecutorService executorService;
    private final ServerData serverData;

    private volatile boolean isRunning;

    /**
     * Spotify server default initialization.
     */
    public Server() {
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
        this.serverData = new ServerData();
        this.isRunning = false;
    }

    /**
     * Starts the server on the specified port.
     */
    public void start() {
        this.isRunning = true;
        try (final ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server started and listening for connection requests!");

            Socket clientSocket;

            while (isRunning) {
                clientSocket = serverSocket.accept();

                System.out.println("Accepted connection request from client " + clientSocket.getInetAddress());
                final ClientHandler clientHandler = new ClientHandler(clientSocket, serverData);

                executorService.execute(clientHandler);
            }
        } catch (final IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
