package bg.sofia.uni.fmi.mjt.spotify.client.io;

import bg.sofia.uni.fmi.mjt.spotify.client.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.client.model.MessageType;
import bg.sofia.uni.fmi.mjt.spotify.client.model.SongInfo;
import bg.sofia.uni.fmi.mjt.spotify.client.music.MusicPlayer;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author angel.beshirov
 */
public class Receiver implements Runnable {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Gson gson = new Gson();

    private final Socket socket;
    private final MusicPlayer musicPlayer;

    private volatile boolean isRunning;

    public Receiver(Socket socket, MusicPlayer musicPlayer) {
        this.socket = socket;
        this.musicPlayer = musicPlayer;
        this.isRunning = true;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            while (isRunning) {
                String response = reader.readLine();
                if (response != null) {
                    Message message = gson.fromJson(response, Message.class);
                    if (MessageType.TEXT == message.getMessageType()) {
                        System.out.println("[" + formatter.format(LocalDateTime.now()) + "] " + message.getValue());
                    } else if (MessageType.JSON == message.getMessageType()) {
                        System.out.println("Received music info!");
                        SongInfo songInfo = gson.fromJson(message.getValue(), SongInfo.class);
                        this.musicPlayer.start(songInfo); // will block here
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error while reading data from server!" + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("Receiver thread was interrupted while waiting!" + e.getMessage());
        }
    }

    public void stop() {
        isRunning = false;
    }
}
