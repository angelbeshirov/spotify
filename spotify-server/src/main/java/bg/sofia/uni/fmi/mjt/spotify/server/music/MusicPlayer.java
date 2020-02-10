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
    private final int frameSize;

    private volatile boolean shouldPlay;

    public MusicPlayer(Song song, OutputStream outputStream, int frameSize) {
        this.song = song;
        this.shouldPlay = true;
        this.outputStream = outputStream;
        this.frameSize = frameSize;
    }

    public Song getSong() {
        return song;
    }

    @Override
    public void run() {
        System.out.println(song.getPath().toFile().length());
        try (FileInputStream fileInputStream = new FileInputStream(song.getPath().toFile())) {

            DataOutputStream dataOutputStream = new DataOutputStream(outputStream);

            byte[] buff = new byte[BUFFER_SIZE];
            int k;
            while (( k = fileInputStream.read(buff)) > 0 && shouldPlay) {
                int mod = k % frameSize;
                dataOutputStream.writeInt(k - mod);
                outputStream.write(buff, 0, k - mod);
            }
            dataOutputStream.writeInt(STOP.getBytes().length);
            outputStream.write(STOP.getBytes(), 0, STOP.getBytes().length);
            outputStream.flush();
            System.out.println("Finishing song playing!");
        } catch (IOException e) {
            Logger.logError("Error while playing song to client!");
        }
    }

    public void stop() {
        this.shouldPlay = false;
    }
}
