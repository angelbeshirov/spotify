package bg.sofia.uni.fmi.mjt.spotify.client.io;

import bg.sofia.uni.fmi.mjt.spotify.client.logging.Logger;
import bg.sofia.uni.fmi.mjt.spotify.client.serde.Serde;
import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;
import bg.sofia.uni.fmi.mjt.spotify.model.SongInfo;

import javax.sound.sampled.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Receives data from the server and acts accordingly.
 *
 * @author angel.beshirov
 */
public class Receiver implements Runnable {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String STOP = "STOP";
    private static final String IO_ERROR_MSG = "IO Error while reading data from server!";
    private static final String DESERIALIZING_ERROR_MSG =
            "Error while deserializing message from server!";
    private static final String ERROR_WHILE_PLAYING_SONG = "Error while playing song!";

    private final ObjectInputStream objectInputStream;
    private final ObjectOutputStream objectOutputStream;

    private SourceDataLine sourceDataLine;

    private volatile boolean isRunning;
    private volatile boolean isPlaying;

    public Receiver(ObjectInputStream objectInputStream, ObjectOutputStream objectOutputStream) {
        this.objectInputStream = objectInputStream;
        this.objectOutputStream = objectOutputStream;
        this.isRunning = true;
        this.isPlaying = false;
        this.sourceDataLine = null;
    }

    @Override
    public void run() {
        try {
            while (isRunning) {
                Message message = (Message) objectInputStream.readObject();
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
                    SongInfo songInfo = (SongInfo) Serde.deserialize(message.getValue());

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
            System.out.println(IO_ERROR_MSG);
            Logger.logError(IO_ERROR_MSG, e);
        } catch (ClassNotFoundException e) {
            System.out.println(DESERIALIZING_ERROR_MSG);
            Logger.logError(DESERIALIZING_ERROR_MSG, e);
        } catch (LineUnavailableException e) {
            System.out.println(ERROR_WHILE_PLAYING_SONG);
            Logger.logError(ERROR_WHILE_PLAYING_SONG, e);
        }
    }

    public void stopPlaying() throws IOException {
        isPlaying = false;
        if (sourceDataLine != null) {
            sourceDataLine.stop();
            sourceDataLine = null;
            objectOutputStream.writeObject(new Message(MessageType.TEXT,
                    Sender.STOP.getBytes()));

        }
    }

    public void stop() throws IOException {
        if (isPlaying) {
            stopPlaying();
        }

        isRunning = false;
    }

    public boolean isPlaying() {
        return isPlaying;
    }
}
