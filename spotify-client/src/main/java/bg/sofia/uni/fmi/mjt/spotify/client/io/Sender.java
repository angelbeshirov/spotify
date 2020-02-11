package bg.sofia.uni.fmi.mjt.spotify.client.io;

import bg.sofia.uni.fmi.mjt.spotify.client.logging.Logger;
import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;

/**
 * Reads commands from the user and sends them to the server.
 *
 * @author angel.beshirov
 */
public class Sender implements Runnable {
    public static final String STOP = "stop";
    private static final String DISCONNECT = "disconnect";
    private static final String ERROR_SENDING_MESSAGE = "Error while sending message to server!";

    private final Receiver receiver;
    private final ObjectOutputStream writer;
    private boolean isRunning;

    public Sender(final ObjectOutputStream writer, final Receiver receiver) {
        this.writer = writer;
        this.receiver = receiver;
        this.isRunning = true;
    }

    @Override
    public void run() {
        try (BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
            while (isRunning) {
                System.out.println("Enter a command to send to the server:");
                String command = consoleReader.readLine();
                Message message = new Message(MessageType.TEXT, command.getBytes());

                if (receiver.isPlaying() && !STOP.equalsIgnoreCase(command)) {
                    System.out.println("You have to stop playing the song before "
                            + "you can send any other commands to the server!");
                    continue;
                } else if (receiver.isPlaying() && STOP.equalsIgnoreCase(command)) {
                    receiver.stopPlaying();
                    continue;
                }

                if (DISCONNECT.equalsIgnoreCase(command)) {
                    receiver.stop();
                    this.isRunning = false;
                }

                writer.writeObject(message);
            }
        } catch (IOException e) {
            System.out.println(ERROR_SENDING_MESSAGE);
            Logger.logError(ERROR_SENDING_MESSAGE, e);
        }
    }
}
