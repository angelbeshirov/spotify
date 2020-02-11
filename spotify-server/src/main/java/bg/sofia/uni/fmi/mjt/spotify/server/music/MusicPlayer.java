package bg.sofia.uni.fmi.mjt.spotify.server.music;

import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;
import bg.sofia.uni.fmi.mjt.spotify.model.ServerData;
import bg.sofia.uni.fmi.mjt.spotify.model.Song;
import bg.sofia.uni.fmi.mjt.spotify.server.logging.Logger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 * Music player which sends song data in a separate thread.
 *
 * @author angel.beshirov
 */
public class MusicPlayer implements Runnable {
    private static final String SENDING_SONG_DATA_ERROR_MSG =
            "Error while sending song data to client!";
    private static final String STOP = "STOP";
    private static final int BUFFER_SIZE = 2048;
    private final Song song;
    private final ObjectOutputStream objectOutputStream;
    private final ServerData serverData;
    private final int frameSize;

    private volatile boolean shouldPlay;

    public MusicPlayer(Song song, ObjectOutputStream objectOutputStream,
                       ServerData serverData, int frameSize) {
        this.song = song;
        this.objectOutputStream = objectOutputStream;
        this.serverData = serverData;
        this.frameSize = frameSize;
        this.shouldPlay = true;
    }

    @Override
    public void run() {
        try (FileInputStream fileInputStream = new FileInputStream(song.getFile())) {
            serverData.addToCurrentlyPlaying(song);
            byte[] buff = new byte[BUFFER_SIZE];
            int k;
            while ((k = fileInputStream.read(buff)) > 0 && shouldPlay) {
                int mod = k % frameSize;
                objectOutputStream.writeObject(new Message(MessageType.SONG_PAYLOAD,
                        Arrays.copyOfRange(buff, 0, k - mod)));
            }
            objectOutputStream.writeObject(new Message(MessageType.TEXT, STOP.getBytes()));
            objectOutputStream.flush();
        } catch (IOException e) {
            System.out.println(SENDING_SONG_DATA_ERROR_MSG);
            Logger.logError(SENDING_SONG_DATA_ERROR_MSG, e);
        }
    }

    public void stop() {
        this.shouldPlay = false;
        removeFromPlaying();
    }

    public void removeFromPlaying() {
        serverData.removeCurrentlyPlaying(song);
    }
}
