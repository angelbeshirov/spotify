package bg.sofia.uni.fmi.mjt.spotify.server.client;

import bg.sofia.uni.fmi.mjt.spotify.model.*;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author angel.beshirov
 */
public class ClientHandler implements Runnable {
    private static final String SPACE = " ";
    private final Socket socket;
    private CommandHandler commandHandler;
    private Map<User, ClientHandler> clients;
    private Map<Song, Integer> currentlyPlaying;
    private List<Song> songs;
    private List<Playlist> playlists;
    private List<User> registeredUsers;
    private ObjectOutputStream objectOutputStream;


    private volatile boolean isRunning;

    // TODO group all 3 lists into an object
    public ClientHandler(Socket socket,
                         Map<User, ClientHandler> clients,
                         List<User> registeredUsers,
                         List<Playlist> playlists,
                         List<Song> songs,
                         Map<Song, Integer> currentlyPlaying) {
        this.socket = socket;
        this.clients = clients;
        this.registeredUsers = registeredUsers;
        this.playlists = playlists;
        this.songs = songs;
        this.currentlyPlaying = currentlyPlaying;
        this.isRunning = true;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    @Override
    public void run() {
        try {
            this.objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            this.commandHandler = new CommandHandler(this, clients, registeredUsers, playlists, songs, currentlyPlaying);


            Message message = (Message) objectInputStream.readObject();

            while (isRunning) {
                if (message != null && message.getMessageType() == MessageType.TEXT) {
                    String command = new String(message.getValue(), Charset.defaultCharset());

                    System.out.println("Command received from client: " + command);
                    String[] data = command.split(SPACE);

                    Optional<Command> internalCommand = getInternalCommand(data[0]);

                    internalCommand
                            .map(x -> commandHandler.handleCommand(x, Arrays.copyOfRange(data, 1, data.length)))
                            .orElseGet(() -> Optional.of(new Message(MessageType.TEXT, "Invalid command!".getBytes())))
                            .ifPresent(obj -> {
                                try {
                                    objectOutputStream.writeObject(obj);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            });

                    message = (Message) objectInputStream.readObject();
                }
            }
        } catch (final IOException e) {
            System.out.println("Error with the connection." + e.getMessage());
        } catch (ClassNotFoundException e) {
            System.out.println("Error with deserialization." + e.getMessage());
        } finally {
            try {
                socket.close(); // TODO close streams
            } catch (IOException e) {
                System.out.println("Error while closing the client socket!");
            }
        }
    }

    private Optional<Command> getInternalCommand(String externalCommand) {
        String processedCommand = externalCommand.replaceAll("-", "_");
        for (Command command : Command.values()) {
            if (command.name().compareToIgnoreCase(processedCommand) == 0) {
                return Optional.of(command);
            }
        }

        return Optional.empty();
    }
}
