package bg.sofia.uni.fmi.mjt.spotify.client.io;

import bg.sofia.uni.fmi.mjt.spotify.client.logging.Logger;
import bg.sofia.uni.fmi.mjt.spotify.client.util.Util;
import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;
import bg.sofia.uni.fmi.mjt.spotify.model.SongInfo;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author angel.beshirov
 */
public class Receiver implements Runnable {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String STOP = "STOP";

    private final Socket socket;
    private LineListener lineListener;
    private SourceDataLine sourceDataLine;

    private volatile boolean isRunning;
    private volatile boolean isPlaying;

    public Receiver(final Socket socket) {
        this.socket = socket;
        this.isRunning = true;
        this.isPlaying = false;
        this.sourceDataLine = null;
    }

    @Override
    public void run() {
        try (ObjectInputStream obj = new ObjectInputStream(socket.getInputStream())) {
            while (isRunning) {
                Message message = (Message) obj.readObject();
                if (MessageType.TEXT == message.getMessageType()) {
                    String textMessage = new String(message.getValue(), Charset.defaultCharset());
                    System.out.println("[" + FORMATTER.format(LocalDateTime.now()) + "] "
                            + textMessage);

                    if (STOP.equals(textMessage) && isPlaying && sourceDataLine != null) {
                        sourceDataLine.drain();
                        stopPlaying();
                    }
                } else if (MessageType.SONG_INFO == message.getMessageType()) {
                    System.out.println("Playing...");
                    SongInfo songInfo = (SongInfo) Util.deserialize(message.getValue());

                    AudioFormat format = new AudioFormat(new AudioFormat.Encoding(
                            songInfo.getEncoding()),
                            songInfo.getSampleRate(),
                            songInfo.getSampleSizeInBits(),
                            songInfo.getChannels(),
                            songInfo.getFrameSize(),
                            songInfo.getFrameRate(),
                            songInfo.isBigEndian());

                    DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
                    sourceDataLine = (SourceDataLine) AudioSystem.getLine(info);
                    sourceDataLine.addLineListener(lineListener);
                    sourceDataLine.open();
                    sourceDataLine.start();
                    isPlaying = true;
                } else if (MessageType.SONG_PAYLOAD == message.getMessageType()
                        && sourceDataLine != null
                        && isPlaying) {
                    sourceDataLine.write(message.getValue(), 0, message.getValue().length);
                }
            }
        } catch (IOException e) {
            System.out.println("IO Error while reading data from server!");
            Logger.logError("IO Error while reading data from server!", e);
        } catch (ClassNotFoundException e) {
            System.out.println("Error while deserializing message from server!");
            Logger.logError("Error while deserializing message from server!", e);
        } catch (LineUnavailableException e) {
            System.out.println("Error while playing song!");
            Logger.logError("Error while playing song!", e);
        }
    }

    public void stopPlaying() {
        isPlaying = false;
        if (sourceDataLine != null) {
            sourceDataLine.stop();
            sourceDataLine = null;
        }
    }

    public void stop() {
        if (isPlaying) {
            stopPlaying();
        }

        isRunning = false;
    }

    public void addListener(LineListener lineListener) {
        this.lineListener = lineListener;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
