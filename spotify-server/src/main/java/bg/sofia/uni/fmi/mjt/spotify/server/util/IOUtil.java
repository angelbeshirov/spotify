package bg.sofia.uni.fmi.mjt.spotify.server.util;

import bg.sofia.uni.fmi.mjt.spotify.model.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.model.Song;
import bg.sofia.uni.fmi.mjt.spotify.model.User;
import bg.sofia.uni.fmi.mjt.spotify.server.logging.impl.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Contains IO utilify functions for serializing and deserializing the data
 * which is used by the server like collection of {@link User}s, collection
 * and {@link Playlist}s and also for retrieving the names of the songs
 * stored on the server available for streaming to the client.
 *
 * @author angel.beshirov
 */
public class IOUtil {

    private static final String SERIALIZING_ERROR = "Error while serializing!";

    public static synchronized void serializeCollection(Path path, Collection<? extends Serializable> elements) {
        if (elements == null || elements.size() == 0) {
            return;
        }

        // creates the file if it doesn't exist
        try (var oos = new ObjectOutputStream(Files.newOutputStream(path))) {
            oos.writeObject(elements);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<User> deserializeUsers(Path path) {
        List<User> users = new ArrayList<>();
        if (!path.toFile().exists()) {
            return users;
        }

        return deserialize(path)
                .map(object -> (List<User>) object)
                .orElse(users);
    }

    public static List<Playlist> deserializePlaylists(Path path) {
        List<Playlist> playlists = new ArrayList<>();
        if (!path.toFile().exists()) {
            return playlists;
        }

        return deserialize(path)
                .map(object -> (List<Playlist>) object)
                .orElse(playlists);
    }

    public static List<Song> retrieveSongs(Path directory) {
        File file = directory.toFile();
        List<Song> songs = new ArrayList<>();
        if (file.isDirectory()) {
            songs.addAll(iterateSongs(file.listFiles()));
        }

        return songs;
    }

    private static List<Song> iterateSongs(File[] files) {
        List<Song> songs = new ArrayList<>();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    iterateSongs(file.listFiles());
                } else {
                    songs.add(new Song(file.getName(), file));
                }
            }
        }

        return songs;
    }

    public static <T extends Serializable> byte[] serialize(T serializable) {
        byte[] result = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject(serializable);
            result = bos.toByteArray();
        } catch (IOException e) {
            System.out.println(SERIALIZING_ERROR);
            Logger.logError(SERIALIZING_ERROR, e);
        }

        return result;
    }

    private static synchronized Optional<Object> deserialize(Path path) {
        Object result = null;
        try (var ois = new ObjectInputStream(Files.newInputStream(path))) {
            result = ois.readObject();
        } catch (EOFException e) {
            System.out.println("No playlists!");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return Optional.ofNullable(result);
    }
}
