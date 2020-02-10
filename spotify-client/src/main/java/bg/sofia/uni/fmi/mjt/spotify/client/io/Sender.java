package bg.sofia.uni.fmi.mjt.spotify.client.io;

import bg.sofia.uni.fmi.mjt.spotify.client.logging.Logger;
import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;
import bg.sofia.uni.fmi.mjt.spotify.client.music.MusicPlayer;

import java.io.*;
import java.net.Socket;

/**
 * @author angel.beshirov
 */
public class Sender implements Runnable {
    private static final String DISCONNECT = "disconnect";
    private static final String STOP = "stop";

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
            while (isRunning) {
                System.out.println("Enter a command to send to the server:");
                String command = consoleReader.readLine();
                Message message = new Message(MessageType.TEXT, command.getBytes());

                if (receiver.isPlaying() && STOP.equalsIgnoreCase(command)) {
                    receiver.stopPlaying();
                    writer.writeObject(message);
                } else if (receiver.isPlaying() && !STOP.equalsIgnoreCase(command)) {
                    System.out.println("You have to stop playing the song before "
                            + "you can send any other commands to the server!");
                } else {
                    writer.writeObject(message);
                }

                if (DISCONNECT.equalsIgnoreCase(command)) {
                    receiver.stop();
                    this.isRunning = false;
                }
            }
        } catch (IOException e) {
            System.out.println("Error while sending message to server!" + e.getMessage());
            Logger.logError(e.toString(), e);
        }
    }
}
