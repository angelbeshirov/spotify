package bg.sofia.uni.fmi.mjt.spotify.server.io;

import bg.sofia.uni.fmi.mjt.spotify.server.model.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.server.model.User;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/**
 * TODO improve
 *
 * @author angel.beshirov
 */
public class IOWorker {
    public static synchronized void writeToFile(Path file, Collection<? extends Serializable> elemenets) {
        if (elemenets == null || elemenets.size() == 0) {
            return;
        }

        try (var oos = new ObjectOutputStream(Files.newOutputStream(file))) {
            oos.writeObject(elemenets);
            oos.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO make sure file is created;
    public static synchronized List<User> deserializeUsers(Path file) {
        List<User> users = new ArrayList<>();
        try (var ois = new ObjectInputStream(Files.newInputStream(file))) {
            users = (List<User>) ois.readObject();
        } catch (EOFException e) {
            System.out.println("No registered users!");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return users;
    }

    public static synchronized List<Playlist> deserializePlaylists(Path file) {
        List<Playlist> playlists = new ArrayList<>();
        try (var ois = new ObjectInputStream(Files.newInputStream(file))) {
            playlists = (List<Playlist>) ois.readObject();
        } catch (EOFException e) {
            // TODO ???
            System.out.println("No playlists!");
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return playlists;
    }

    public static synchronized void writeToFile(Path file, String data) {
        try (var oos = new OutputStreamWriter(Files.newOutputStream(file))) {
            oos.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
