package bg.sofia.uni.fmi.mjt.spotify.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author angel.beshirov
 */
public class Server {

    private static final int SERVER_PORT = 4444;
    private static final int MAX_THREADS = 15;
    private final Map<User, ClientHandler> users;
    private final ExecutorService executorService;
    private List<User> savedUsers;

    private volatile boolean isRunning;

    /**
     * Chat Server default initialization.
     */
    public Server() {
        this.users = new ConcurrentHashMap<>();
        this.savedUsers = new CopyOnWriteArrayList<>(IOWorker.readUsersFromFile(Path.of("src\\main\\resources\\users.bin")));
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
                final ClientHandler clientHandler = new ClientHandler(clientSocket, users, savedUsers);

                executorService.execute(clientHandler);
            }
        } catch (final IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
