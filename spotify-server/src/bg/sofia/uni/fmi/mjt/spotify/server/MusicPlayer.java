package bg.sofia.uni.fmi.mjt.spotify.server;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author angel.beshirov
 */
public class MusicPlayer implements Runnable {
    public static final int BUFFER_SIZE = 2048;
    private File song;
    private volatile boolean shouldPlay;
    private final PrintWriter outputStream;
    private List<Song> songs;

    public MusicPlayer(File song, OutputStream outputStream) {
        this.song = song;
        this.shouldPlay = true;
        this.outputStream = new PrintWriter(outputStream);
    }

    @Override
    public void run() {
        try (FileInputStream inputStream = new FileInputStream(song)) {
            int k;
            byte[] buff = new byte[BUFFER_SIZE];
            while ((k = inputStream.read(buff)) != -1 && shouldPlay) {
//                outputStream.println(Base64.encodeBase64String(base64Encoded));
            }
        } catch (IOException e) {
            Logger.logError("Error while playing song to client!");
        }
    }

    public void stop() {
        this.shouldPlay = false;
    }

    public List<Song> findSongs(String... keywords) {
        List<Song> matchedSongs = new ArrayList<>();
        if (keywords != null) {
            for (String keyword : keywords) {
                matchedSongs.addAll(songs.stream()
                        .filter(v -> v.getSongName().toLowerCase().contains(keyword.toLowerCase()))
                        .collect(Collectors.toList()));
            }
        }
        return matchedSongs;
    }
}
