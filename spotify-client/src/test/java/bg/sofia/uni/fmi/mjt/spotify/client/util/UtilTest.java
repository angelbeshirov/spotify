package bg.sofia.uni.fmi.mjt.spotify.client.util;

import bg.sofia.uni.fmi.mjt.spotify.client.logging.Logger;
import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * @author angel.beshirov
 */
public class UtilTest {

    @Test
    public void testSerialize() {
        Message message = new Message(MessageType.TEXT, "value 1 2 3".getBytes());
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject(message);
            Assert.assertArrayEquals(bos.toByteArray(), Util.serialize(message));
        } catch (IOException e) {
            System.out.println("Error while serializing!");
            Logger.logError("Error while deserializing object!", e);
            Assert.fail();
        }
    }

    @Test
    public void testDeserialize() {
        Message message = new Message(MessageType.TEXT, "value 1 2 3".getBytes());
        byte[] payload = Util.serialize(message);
        Message actual = (Message) Util.deserialize(payload);

        Assert.assertEquals(message.getMessageType(), actual.getMessageType());
        Assert.assertArrayEquals(message.getValue(), actual.getValue());
    }
}
