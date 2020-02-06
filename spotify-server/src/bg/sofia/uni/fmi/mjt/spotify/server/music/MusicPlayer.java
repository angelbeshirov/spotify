package bg.sofia.uni.fmi.mjt.spotify.server.music;

import bg.sofia.uni.fmi.mjt.spotify.server.logging.Logger;
import bg.sofia.uni.fmi.mjt.spotify.server.model.Song;

import java.io.*;

/**
 * @author angel.beshirov
 */
public class MusicPlayer implements Runnable {
    public static final int BUFFER_SIZE = 2048;
    public static final String STOP = "STOP";
    private final Song song;
    private final OutputStream outputStream;

    private volatile boolean shouldPlay;

    public MusicPlayer(Song song, OutputStream outputStream) {
        this.song = song;
        this.shouldPlay = true;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        try (FileInputStream inputStream = new FileInputStream(song.getPath().toFile())) {

            byte[] buff = new byte[BUFFER_SIZE];
            while (inputStream.available() != 0 && shouldPlay) {
                int k = inputStream.read(buff);
                outputStream.write(buff, 0, k);
            }
            outputStream.flush();
            outputStream.write(STOP.getBytes());
            outputStream.flush();
        } catch (IOException e) {
            Logger.logError("Error while playing song to client!");
        }
    }

    public void stop() {
        this.shouldPlay = false;
    }
}
