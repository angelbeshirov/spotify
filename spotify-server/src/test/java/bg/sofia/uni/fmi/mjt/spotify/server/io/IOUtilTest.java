package bg.sofia.uni.fmi.mjt.spotify.server.io;

import bg.sofia.uni.fmi.mjt.spotify.model.Playlist;
import org.junit.Test;

import java.nio.file.Path;
import java.util.Collections;

/**
 * @author angel.beshirov
 */
public class IOUtilTest {

    @Test
    public void testCollectionSerialization() {
        IOUtil.serializeCollection(Path.of("src\\test\\resources\\collection.bin"),
                Collections.singletonList(new Playlist("asd", "bsd")));
    }
}
