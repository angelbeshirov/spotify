package bg.sofia.uni.fmi.mjt.spotify.client.io;

import bg.sofia.uni.fmi.mjt.spotify.client.logging.Logger;
import bg.sofia.uni.fmi.mjt.spotify.client.music.CustomLineListener;
import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;

import javax.sound.sampled.LineListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author angel.beshirov
 */
public class Sender implements Runnable {
    private static final String DISCONNECT = "disconnect";
    private static final String STOP = "stop";
    public static final String ERROR_SENDING_MESSAGE = "Error while sending message to server!";

    private final Receiver receiver;
    private final Socket socket;
    private boolean isRunning;

    public Sender(final Socket socket, final Receiver receiver) {
        this.socket = socket;
        this.receiver = receiver;
        this.isRunning = true;
    }

    @Override
    public void run() {
        try (ObjectOutputStream writer = new ObjectOutputStream(socket.getOutputStream());
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
            LineListener lineListener = new CustomLineListener(writer);
            receiver.addListener(lineListener);
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
                }

                if (DISCONNECT.equalsIgnoreCase(command)) {
                    receiver.stop();
                    this.isRunning = false;
                }

                writer.writeObject(message);
            }
        } catch (IOException e) {
            System.out.println(ERROR_SENDING_MESSAGE + e.getMessage());
            Logger.logError(ERROR_SENDING_MESSAGE, e);
        }
    }
}
