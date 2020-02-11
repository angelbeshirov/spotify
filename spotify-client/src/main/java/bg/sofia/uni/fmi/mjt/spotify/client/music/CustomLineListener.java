package bg.sofia.uni.fmi.mjt.spotify.client.music;

import bg.sofia.uni.fmi.mjt.spotify.client.logging.Logger;
import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;

import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author angel.beshirov
 */
public class CustomLineListener implements LineListener {

    private static final String ERROR_WHILE_SENDING_SONG_FINISHED_MESSAGE =
            "Error while sending song finished message";
    private static final String SONG_FINISHED = "song-finished";
    private final ObjectOutputStream objectOutputStream;

    public CustomLineListener(ObjectOutputStream objectOutputStream) {
        this.objectOutputStream = objectOutputStream;
    }

    @Override
    public void update(LineEvent event) {
        try {
            if (event.getType() == LineEvent.Type.STOP) {
                objectOutputStream.writeObject(new Message(MessageType.TEXT, SONG_FINISHED.getBytes()));
            }
        } catch (IOException e) {
            System.out.println(ERROR_WHILE_SENDING_SONG_FINISHED_MESSAGE);
            Logger.logError(ERROR_WHILE_SENDING_SONG_FINISHED_MESSAGE, e);
        }

    }
}
