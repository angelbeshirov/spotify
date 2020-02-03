package bg.sofia.uni.fmi.mjt.spotify.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author angel.beshirov
 */
public class Server {

    private static final int SERVER_PORT = 4444;
    private static final int MAX_THREADS = 10;
    private final Map<User, ClientHandler> loggedUsers;
    private final ExecutorService executorService;

    private volatile boolean isRunning;

    /**
     * Chat Server default initialization.
     */
    public Server() {
        this.loggedUsers = new ConcurrentHashMap<>();
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
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
                final ClientHandler clientHandler = new ClientHandler(clientSocket, loggedUsers);
                executorService.execute(clientHandler);
            }
        } catch (final IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
