package bg.sofia.uni.fmi.mjt.spotify.server;

import java.io.*;
import java.net.Socket;
import java.util.*;

/**
 * @author angel.beshirov
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final CommandHandler commandHandler;

    private boolean isLoggedIn;

    private volatile boolean isRunning;

    public ClientHandler(Socket socket, Map<User, ClientHandler> clients, Set<User> registeredUsers, Set<Playlist> playlists) {
        this.socket = socket;
        this.commandHandler = new CommandHandler(this, clients, registeredUsers, playlists);
        this.isRunning = true;
        this.isLoggedIn = false;
    }

    public Socket getSocket() {
        return socket;
    }

    public void logIn() {
        this.isLoggedIn = true;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    @Override
    public void run() {
        try (final PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
             final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String command = reader.readLine();
            while (isRunning && command != null) {
                System.out.println("Command received from client: " + command);
                String[] data = command.split(" ");

                Optional<Command> internalCommand = getInternalCommand(data[0]);

                // TODO u know what has to be done
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

    private Optional<Command> getInternalCommand(String command) {
        String processedCommand = command.replaceAll("-", "_");
        for (Command each : Command.values()) {
            if (each.name().compareToIgnoreCase(processedCommand) == 0) {
                return Optional.of(each);
            }
        }

        return Optional.empty();
    }

    private void testStreaming(ObjectOutputStream objectOutputStream, File song) {
        try (FileInputStream inputStream = new FileInputStream(song)) {
            int k;
            byte[] buff = new byte[2048];
            while ((k = inputStream.read(buff)) != -1) {
                objectOutputStream.write(buff, 0, k);
            }
        } catch (IOException e) {
            System.out.println(e.toString());
            Logger.logError("Error while playing song to client!");
        }
    }
}
