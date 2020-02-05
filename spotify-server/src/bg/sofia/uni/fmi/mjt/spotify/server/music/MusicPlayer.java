package bg.sofia.uni.fmi.mjt.spotify.server.music;

import bg.sofia.uni.fmi.mjt.spotify.server.logging.Logger;

import java.io.*;

/**
 * @author angel.beshirov
 */
public class MusicPlayer implements Runnable {
    public static final int BUFFER_SIZE = 2048;
    public static final String STOP = "STOP";
    private final File song;
    private final OutputStream outputStream;

    private volatile boolean shouldPlay;

    public MusicPlayer(File song, OutputStream outputStream) {
        this.song = song;
        this.shouldPlay = true;
        this.outputStream = outputStream;
    }

    @Override
    public void run() {
        try (InputStream inputStream = new FileInputStream(song)) {
            int k;
            byte[] buff = new byte[BUFFER_SIZE];
            while ((k = inputStream.read(buff)) != -1 && shouldPlay) {
                outputStream.write(buff, 0, k);
            }

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
