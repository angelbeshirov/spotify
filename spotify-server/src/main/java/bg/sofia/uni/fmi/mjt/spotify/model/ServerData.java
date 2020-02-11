package bg.sofia.uni.fmi.mjt.spotify.model;

import bg.sofia.uni.fmi.mjt.spotify.server.client.ClientHandler;
import bg.sofia.uni.fmi.mjt.spotify.server.util.IOUtil;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Contains the data stored by the server. This is loaded at start-up.
 *
 * @author angel.beshirov
 */
public class ServerData {
    private static final Path USERS_PATH = Path.of("src\\main\\resources\\users.bin");
    private static final Path PLAYLISTS_PATH = Path.of("src\\main\\resources\\playlists.bin");
    private static final Path SONGS_PATH = Path.of("src\\main\\resources\\songs");

    private final List<User> savedUsers;
    private final List<Playlist> playlists;
    private final List<Song> songs;

    private final Map<User, ClientHandler> clients;
    private final Map<Song, Integer> currentlyPlaying;

    public ServerData() {
        this.clients = new ConcurrentHashMap<>();
        this.currentlyPlaying = new ConcurrentHashMap<>();
        this.savedUsers = Collections.synchronizedList(new ArrayList<>(
                IOUtil.deserializeUsers(USERS_PATH)));
        this.playlists = Collections.synchronizedList(new ArrayList<>(
                IOUtil.deserializePlaylists(PLAYLISTS_PATH)));
        this.songs = Collections.synchronizedList(new ArrayList<>(
                IOUtil.retrieveSongs(SONGS_PATH)));
    }

    public void saveUsers() {
        IOUtil.serializeCollection(USERS_PATH, savedUsers);
    }

    public void savePlaylists() {
        IOUtil.serializeCollection(PLAYLISTS_PATH, playlists);
    }

    public void addUser(User user) {
        savedUsers.add(user);
    }

    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
    }

    public User getUser(User user) {
        int index = savedUsers.indexOf(user);

        if (index != -1) {
            return savedUsers.get(index);
        }

        return null;
    }

    public Playlist getPlaylist(Playlist playlist) {
        int index = playlists.indexOf(playlist);

        if (index != -1) {
            return playlists.get(index);
        }

        return null;
    }

    public void addLoggedInUser(User user, ClientHandler clientHandler) {
        clients.put(user, clientHandler);
    }

    public void logOut(User user) {
        ClientHandler clientHandler = clients.remove(user);
        if (clientHandler != null) {
            clientHandler.stop();
        }
    }

    public Song getSongByName(String name) {
        for (Song song : this.songs) {
            if (song.getSongName().equalsIgnoreCase(name)) {
                return song;
            }
        }

        return null;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void addToCurrentlyPlaying(Song songToPlay) {
        currentlyPlaying.put(songToPlay, currentlyPlaying.getOrDefault(songToPlay, 0) + 1);
    }

    public void removeCurrentlyPlaying(Song song) {
        Integer currentCount = currentlyPlaying.get(song);

        if (Objects.compare(currentCount, 0, Comparator.naturalOrder()) > 0) {
            currentlyPlaying.put(song, currentCount - 1);
            if (currentCount == 1) {
                currentlyPlaying.remove(song);
            }
        }
    }

    public List<Map.Entry<Song, Integer>> getTopNCurrentlyPlayingSorted(int n) {
        return currentlyPlaying.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(n)
                .collect(Collectors.toList());
    }
}
