package bg.sofia.uni.fmi.mjt.spotify.client.music;

import bg.sofia.uni.fmi.mjt.spotify.client.model.SongInfo;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Objects;

/**
 * TODO listener or something
 *
 * @author angel.beshirov
 */
public class MusicPlayer {
    private final InputStream inputStream;
    private Player player;

    public MusicPlayer(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public void start(SongInfo songInfo) throws InterruptedException {
        this.player = new Player(songInfo, inputStream);
        Thread musicPlayerThread = new Thread(this.player);

        musicPlayerThread.start();
        musicPlayerThread.join();
    }

    public void stop() {
        this.player.stop();
    }

    private static class Player implements Runnable {
        private static final String STOP = "STOP";

        private final SongInfo songInfo;
        private final InputStream inputStream;

        private volatile boolean isPlaying;

        public Player(SongInfo songInfo, InputStream inputStream) {
            this.songInfo = songInfo;
            this.inputStream = inputStream;
            this.isPlaying = true;
        }

        @Override
        public void run() {
            System.out.println("Started music player!");
            AudioFormat format = new AudioFormat(new AudioFormat.Encoding(songInfo.getEncoding()),
                    songInfo.getSampleRate(),
                    songInfo.getSampleSizeInBits(),
                    songInfo.getChannels(),
                    songInfo.getFrameSize(),
                    songInfo.getFrameRate(),
                    songInfo.isBigEndian());
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            try (SourceDataLine sourceDataLine = (SourceDataLine) AudioSystem.getLine(info)) {
                sourceDataLine.open();
                // this starts a new thread
                sourceDataLine.start();

                boolean endOfSong = false;
                final byte[] buffer = new byte[2048];
                int k;
                while ((k = inputStream.read(buffer)) != -1 && isPlaying) {
                    if (Objects.deepEquals(Arrays.copyOf(buffer, k), STOP.getBytes())) {
                        endOfSong = true;
                        break;
                    }
                    sourceDataLine.write(buffer, 0, k);
                }

                while ((k = inputStream.read(buffer)) != -1 && !endOfSong) {
                    if (Objects.deepEquals(Arrays.copyOf(buffer, k), STOP.getBytes())) {
                        break;
                    }
                }

                System.out.println("Stopping player!");

                // TODO client shouldnt be able to send any command except stop to the server while playing song
                sourceDataLine.stop();
            } catch (LineUnavailableException | IOException e) {
                System.out.println("Error while playing music!" + e.getMessage());
            }
        }

        public void stop() {
            this.isPlaying = false;
        }
    }
}
