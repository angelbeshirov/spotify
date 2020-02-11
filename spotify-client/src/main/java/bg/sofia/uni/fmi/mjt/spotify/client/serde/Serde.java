package bg.sofia.uni.fmi.mjt.spotify.client.serde;

import bg.sofia.uni.fmi.mjt.spotify.client.logging.Logger;

import java.io.*;

/**
 * Contains serialization and deserialization functions.
 *
 * @author angel.beshirov
 */
public class Serde {

    private static final String DESERIALIZING_ERROR = "Error while deserializing!";
    private static final String SERIALIZING_ERROR = "Error while serializing!";

    public static <T extends Serializable> byte[] serialize(T serializable) {
        byte[] result = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject(serializable);
            result = bos.toByteArray();
        } catch (IOException e) {
            System.out.println(SERIALIZING_ERROR);
            Logger.logError(SERIALIZING_ERROR, e);
        }

        return result;
    }

    public static Object deserialize(byte[] data) {
        Object result = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInput in = new ObjectInputStream(bis)) {
            result = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(DESERIALIZING_ERROR);
            Logger.logError(DESERIALIZING_ERROR, e);
        }

        return result;
    }
}
