package bg.sofia.uni.fmi.mjt.spotify.server;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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
    private Set<User> savedUsers;
    private Set<Playlist> playlists;
    private List<Song> songs;

    private volatile boolean isRunning;

    /**
     * Chat Server default initialization.
     */
    public Server() {
        this.users = new ConcurrentHashMap<>();
        // TODO check if this file exists otherwise it fails;
        this.savedUsers = Collections.synchronizedSet(new HashSet<>(IOWorker.readUsersFromFile(Path.of("src\\main\\resources\\users.bin"))));
        this.playlists = Collections.synchronizedSet(new HashSet<>(IOWorker.readPlaylistsFromFile(Path.of("src\\main\\resources\\playlists.bin"))));
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
        this.isRunning = false;
        retrieveSongs("src\\main\\resources\\songs");
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
                final ClientHandler clientHandler = new ClientHandler(clientSocket, users, savedUsers, playlists);

                executorService.execute(clientHandler);
            }
        } catch (final IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void retrieveSongs(String directory) {
        File file = new File(directory);
        if (!file.isDirectory()) {
            iterateSongs(file.listFiles());
        }
    }

    public void iterateSongs(File[] files) {
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                iterateSongs(file.listFiles());
            } else {
                songs.add(new Song(file.getName(), Path.of(file.getAbsolutePath())));
            }
        }
    }
}
