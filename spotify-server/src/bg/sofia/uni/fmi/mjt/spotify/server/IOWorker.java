package bg.sofia.uni.fmi.mjt.spotify.server;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * TODO synchronize
 *
 * @author angel.beshirov
 */
public class IOWorker {
    public static synchronized void writeUsersToFile(Path file, User... users) {
        try (var oos = new ObjectOutputStream(Files.newOutputStream(file))) {
            for (User user : users) {
                oos.writeObject(user);
                oos.flush();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // TODO make sure file is created;
    public static synchronized List<User> readStudentsFromFile(Path file) {
        List<User> users = new ArrayList<>();
        try (var ois = new ObjectInputStream(Files.newInputStream(file))) {
            while (true) {
                User user = (User) ois.readObject();
                users.add(user);
            }
        } catch (EOFException e) {
            // EMPTY BODY
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        return users;
    }

    public static synchronized void writeToFile(Path file, String data) {
        try (var oos = new OutputStreamWriter(Files.newOutputStream(file))) {
            oos.write(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
