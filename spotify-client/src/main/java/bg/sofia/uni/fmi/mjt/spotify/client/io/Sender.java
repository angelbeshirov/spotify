package bg.sofia.uni.fmi.mjt.spotify.client.io;

import bg.sofia.uni.fmi.mjt.spotify.client.logging.Logger;
import bg.sofia.uni.fmi.mjt.spotify.client.music.MusicPlayer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author angel.beshirov
 */
public class Sender implements Runnable {
    private static final String DISCONNECT = "disconnect";
    private static final String STOP = "stop";

    private final Receiver receiver;
    private final Socket socket;
    private final MusicPlayer musicPlayer;
    private boolean isRunning;

    public Sender(final Socket socket, final Receiver receiver, final MusicPlayer musicPlayer) {
        this.socket = socket;
        this.receiver = receiver;
        this.musicPlayer = musicPlayer;
        this.isRunning = true;
    }

    @Override
    public void run() {
        try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {
            boolean isPlaying = false;
            while (isRunning) {
                System.out.println("Enter a command to send to the server:");
                String command = consoleReader.readLine();

                if (command != null) {
                    if (isPlaying && STOP.equalsIgnoreCase(command)) {
                        musicPlayer.stop();
                        isPlaying = false;
                        writer.println(command);
                    } else if (isPlaying) {
                        System.out.println("You have to stop playing the song before "
                                + "you can send any other commands to the server!");
                    } else {
                        writer.println(command);
                    }

                    if (DISCONNECT.equalsIgnoreCase(command)) {
                        receiver.stop();
                        this.isRunning = false;
                    } else if (command.toLowerCase().startsWith("play")) {
                        isPlaying = true;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error while sending message to server!" + e.getMessage());
            Logger.logError(e.toString());
        }
    }
}
