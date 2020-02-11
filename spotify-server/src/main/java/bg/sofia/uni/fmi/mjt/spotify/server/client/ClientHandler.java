package bg.sofia.uni.fmi.mjt.spotify.server.client;

import bg.sofia.uni.fmi.mjt.spotify.model.Command;
import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;
import bg.sofia.uni.fmi.mjt.spotify.model.ServerData;
import bg.sofia.uni.fmi.mjt.spotify.server.logging.impl.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Optional;

/**
 * Handles client connection in a separate thread.
 *
 * @author angel.beshirov
 */
public class ClientHandler implements Runnable {
    private static final String SPACE = " ";
    private static final String CONNECTION_ERROR_MSG = "Error with the connection.";
    private static final String DESERIALIZATION_ERROR_MSG = "Error with deserialization.";
    private static final String CLIENT_ERROR_MSG = "Error while closing the client resources!";

    private final Socket socket;
    private final ServerData serverData;

    private ObjectOutputStream objectOutputStream;
    private CommandHandler commandHandler;

    private volatile boolean isRunning;

    public ClientHandler(Socket socket, ServerData serverData) {
        this.socket = socket;
        this.serverData = serverData;
        this.isRunning = true;
    }

    public ObjectOutputStream getObjectOutputStream() {
        return objectOutputStream;
    }

    @Override
    public void run() {
        try {
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            // output stream must be created before input stream
            ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream());

            commandHandler = new CommandHandler(this, serverData);

            Message message;

            while (isRunning) {
                message = (Message) objectInputStream.readObject();

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
                }
            }
        } catch (final IOException e) {
            System.out.println(CONNECTION_ERROR_MSG);
            Logger.logError(CONNECTION_ERROR_MSG, e);
        } catch (ClassNotFoundException e) {
            System.out.println(DESERIALIZATION_ERROR_MSG);
            Logger.logError(DESERIALIZATION_ERROR_MSG, e);
        } finally {
            try {
                objectOutputStream.close();
                socket.close();
            } catch (IOException e) {
                System.out.println(CLIENT_ERROR_MSG);
                Logger.logError(CLIENT_ERROR_MSG, e);
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

    public void stop() {
        this.isRunning = false;
    }
}
