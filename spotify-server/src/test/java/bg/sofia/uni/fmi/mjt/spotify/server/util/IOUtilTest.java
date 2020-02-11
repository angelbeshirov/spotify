package bg.sofia.uni.fmi.mjt.spotify.server.util;

import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;
import bg.sofia.uni.fmi.mjt.spotify.model.Playlist;
import bg.sofia.uni.fmi.mjt.spotify.model.Song;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author angel.beshirov
 */
public class IOUtilTest {

    @Test
    public void testCollectionSerialization() {
        IOUtil.serializeCollection(Path.of("src\\test\\resources\\collection.bin"),
                Collections.singletonList(new Playlist("asd", "bsd")));
    }

    @Test
    public void testSerialize() {
        Message message = new Message(MessageType.TEXT, "value 1 2 3".getBytes());
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject(message);
            Assert.assertArrayEquals(bos.toByteArray(), IOUtil.serialize(message));
        } catch (IOException e) {
            System.out.println("Error while serializing!");
            Assert.fail();
        }
    }

    @Test
    public void testIterateSongs() {
        File file1 = new File("path1");
        File file2 = new File("path2");
        Path path = mock(Path.class);
        File file = mock(File.class);
        when(path.toFile()).thenReturn(file);
        when(file.isDirectory()).thenReturn(true);
        when(file.listFiles()).thenReturn(new File[]{file1, file2});

        Set<Song> songs = IOUtil.retrieveSongs(path);
        Assert.assertEquals(Set.of(new Song(file1.getName(), file1),
                new Song(file2.getName(), file2)), songs);
    }
}
