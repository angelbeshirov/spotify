package bg.sofia.uni.fmi.mjt.spotify.server.io;

import bg.sofia.uni.fmi.mjt.spotify.server.client.ClientHandler;
import bg.sofia.uni.fmi.mjt.spotify.server.model.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.server.model.Song;
import bg.sofia.uni.fmi.mjt.spotify.server.model.User;

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
    // TODO if later not need to be list change to set;
    private List<User> savedUsers;
    private List<Playlist> playlists;
    private List<Song> songs;

    private Map<Song, Integer> currentlyPlaying;

    private volatile boolean isRunning;

    /**
     * Chat Server default initialization.
     */
    public Server() {
        this.users = new ConcurrentHashMap<>();
        // TODO check if this file exists otherwise it fails;
        this.savedUsers = Collections.synchronizedList(new ArrayList<>(IOUtil.deserializeUsers(Path.of("src\\main\\resources\\users.bin"))));
        this.playlists = Collections.synchronizedList(new ArrayList<>(IOUtil.deserializePlaylists(Path.of("src\\main\\resources\\playlists.bin"))));
        this.executorService = Executors.newFixedThreadPool(MAX_THREADS);
        this.isRunning = false;
        this.songs = Collections.synchronizedList(new ArrayList<>());
        this.currentlyPlaying = new ConcurrentHashMap<>();
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
                final ClientHandler clientHandler = new ClientHandler(clientSocket, users, savedUsers, playlists, songs, currentlyPlaying);

                executorService.execute(clientHandler);
            }
        } catch (final IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private void retrieveSongs(String directory) {
        File file = new File(directory);
        if (file.isDirectory()) {
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
