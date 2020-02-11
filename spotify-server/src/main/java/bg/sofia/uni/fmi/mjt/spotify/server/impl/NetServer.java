package bg.sofia.uni.fmi.mjt.spotify.server.impl;

import bg.sofia.uni.fmi.mjt.spotify.model.ServerData;
import bg.sofia.uni.fmi.mjt.spotify.server.Server;
import bg.sofia.uni.fmi.mjt.spotify.server.client.ClientHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Multi-threaded net server implementation.
 * Accepts client connections and handles them in a separate thread.
 *
 * @author angel.beshirov
 */
public class NetServer implements Server {

    private static final int MAX_THREADS = 15;
    private final ExecutorService executorService;
    private final ServerSocket serverSocket;
    private final ServerData serverData;

    private volatile boolean isRunning;

    /**
     * Spotify server default initialization.
     */
    public NetServer(ServerSocket serverSocket) {
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
        this.serverData = new ServerData();
        this.isRunning = false;
        this.serverSocket = serverSocket;
    }

    /**
     * Listens for connections and starts them in a separate thread.
     */
    @Override
    public void start() throws IOException {
        this.isRunning = true;
        System.out.println("Server started and listening for connection requests!");

        Socket clientSocket;

        while (isRunning) {
            clientSocket = serverSocket.accept();

            System.out.println("Accepted connection request from client " + clientSocket.getInetAddress());
            final ClientHandler clientHandler = new ClientHandler(clientSocket, serverData);

            executorService.execute(clientHandler);
        }

    }

    @Override
    public void stop() {
        this.isRunning = false;
    }

}
