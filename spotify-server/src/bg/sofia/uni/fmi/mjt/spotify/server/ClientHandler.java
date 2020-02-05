package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.server.model.Command;
import bg.sofia.uni.fmi.mjt.spotify.server.model.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.server.model.Song;
import bg.sofia.uni.fmi.mjt.spotify.server.model.User;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
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
    private final CommandHandler commandHandler;

    private volatile boolean isRunning;

    // TODO group all 3 lists into an object
    public ClientHandler(Socket socket, Map<User, ClientHandler> clients, List<User> registeredUsers, List<Playlist> playlists, List<Song> songs) {
        this.socket = socket;
        this.commandHandler = new CommandHandler(this, clients, registeredUsers, playlists, songs);
        this.isRunning = true;
    }

    public Socket getSocket() {
        return socket;
    }

    @Override
    public void run() {
        try (PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String command = reader.readLine();
            while (isRunning && command != null) {
                System.out.println("Command received from client: " + command);
                String[] data = command.split(SPACE);

                Optional<Command> internalCommand = getInternalCommand(data[0]);

                internalCommand
                        .map(x -> commandHandler.handleCommand(x, Arrays.copyOfRange(data, 1, data.length)))
                        .orElseGet(() -> Optional.of("Invalid command"))
                        .ifPresent(printWriter::println);

                command = reader.readLine();
            }
        } catch (final IOException e) {
            System.out.println("Error with the socket." + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error while closing client socket!");
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
