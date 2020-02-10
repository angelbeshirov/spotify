package bg.sofia.uni.fmi.mjt.spotify.server.music;

import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;
import bg.sofia.uni.fmi.mjt.spotify.server.logging.Logger;
import bg.sofia.uni.fmi.mjt.spotify.model.Song;

import java.io.*;
import java.util.Arrays;

/**
 * @author angel.beshirov
 */
public class MusicPlayer implements Runnable {
    public static final int BUFFER_SIZE = 2048;
    public static final String STOP = "STOP";
    private final Song song;
    private final ObjectOutputStream objectOutputStream;
    private final int frameSize;

    private volatile boolean shouldPlay;

    public MusicPlayer(Song song, ObjectOutputStream objectOutputStream, int frameSize) {
        this.song = song;
        this.shouldPlay = true;
        this.objectOutputStream = objectOutputStream;
        this.frameSize = frameSize;
    }

    public Song getSong() {
        return song;
    }

    @Override
    public void run() {
        System.out.println(song.getPath().toFile().length());
        try (FileInputStream fileInputStream = new FileInputStream(song.getPath().toFile())) {

            byte[] buff = new byte[BUFFER_SIZE];
            int k;
            while ((k = fileInputStream.read(buff)) > 0 && shouldPlay) {
                int mod = k % frameSize;
                objectOutputStream.writeObject(new Message(MessageType.SONG_PAYLOAD, Arrays.copyOfRange(buff, 0, k - mod)));
            }
            objectOutputStream.writeObject(new Message(MessageType.TEXT, STOP.getBytes()));
            objectOutputStream.flush();
            System.out.println("Finishing song playing!");
        } catch (IOException e) {
            Logger.logError("Error while sending song data to client!");
        }
    }

    public void stop() {
        this.shouldPlay = false;
    }
}
