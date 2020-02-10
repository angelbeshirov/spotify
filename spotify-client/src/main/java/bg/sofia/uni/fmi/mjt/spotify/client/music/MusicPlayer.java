package bg.sofia.uni.fmi.mjt.spotify.client.music;

import bg.sofia.uni.fmi.mjt.spotify.client.logging.Logger;
import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.SongInfo;

import javax.sound.sampled.*;
import java.io.*;
import java.util.Arrays;
import java.util.Objects;

/**
 * TODO listener or something.
 *
 * @author angel.beshirov
 */
public class MusicPlayer {
    private final ObjectInputStream objectInputStream;
    private Player player;

    public MusicPlayer(ObjectInputStream objectInputStream) {
        this.objectInputStream = objectInputStream;
    }

    public void start(SongInfo songInfo) throws InterruptedException {
        this.player = new Player(songInfo, objectInputStream);
        Thread musicPlayerThread = new Thread(this.player);

        musicPlayerThread.start();
        musicPlayerThread.join();

        System.out.println("Music player exited successfully!");
    }

    public void stop() {
        this.player.stop();
    }

    private static class Player implements Runnable {
        private static final String STOP = "STOP";
        private static final int BUFFER_SIZE = 2048;

        private final SongInfo songInfo;
        private ObjectInputStream objectInputStream;

        private volatile boolean isPlaying;

        private Player(SongInfo songInfo, ObjectInputStream objectInputStream) {
            this.songInfo = songInfo;
            this.objectInputStream = objectInputStream;
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
                final byte[] buffer = new byte[BUFFER_SIZE];

                while (isPlaying) {
                    Message message = (Message) objectInputStream.readObject();
                    boolean end = false;
                    int allBytes = 0;
                    int bytesRead;

//                    while (!end) {
//                        bytesRead = inputStream.read(buffer, 0, size);
//
//                        allBytes += bytesRead;
//                        if (allBytes >= size) {
//                            //System.out.println("Out for " + allBytes);
//                            end = true;
//                        }
//                    }

//                    if (Objects.deepEquals(Arrays.copyOf(buffer, allBytes), STOP.getBytes())) {
//                        endOfSong = true;
//                        break;
//                    }

                    sourceDataLine.write(message.getValue(), 0, message.getValue().length);
                }
//                int k;
//
//                System.out.println("End while 1!");
//
//                while (!endOfSong) {
//                    int size = inputStream.readInt();
//                    System.out.println("Size is" + size);
//                    boolean end = false;
//                    int bytesRead = 0;
//                    int allBytes = 0;
//                    while (!end) {
//                        bytesRead = inputStream.read(buffer);
//
//                        allBytes += bytesRead;
//                        if (allBytes >= size) {
//                            end = true;
//                        }
//                    }
//
//                    if (Objects.deepEquals(Arrays.copyOf(buffer, allBytes), STOP.getBytes())) {
//                        endOfSong = true;
//                    }


                // TODO client shouldn't be able to send any command except stop to the server while playing song
                sourceDataLine.drain();
                sourceDataLine.stop();

                System.out.println("Stopping player!");
            } catch (LineUnavailableException | IOException | ClassNotFoundException e) {
                System.out.println("Error while playing music!" + e.getMessage());
                Logger.logError("Error while playing music!", e);
            }
        }

        public void stop() {
            this.isPlaying = false;
        }
    }
}
