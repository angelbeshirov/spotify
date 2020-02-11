package bg.sofia.uni.fmi.mjt.spotify.server.music;

import bg.sofia.uni.fmi.mjt.spotify.model.ServerData;
import bg.sofia.uni.fmi.mjt.spotify.model.Song;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

/**
 * @author angel.beshirov
 */
public class MusicPlayerTest {
    private static final int FRAME_SIZE = 4;
    private static final int TIMEOUT = 5000;
    private static final int SLEEP = 2000;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private MusicPlayer musicPlayer;
    private ByteArrayOutputStream bos;
    private ObjectOutputStream objectOutputStream;
    private ServerData serverData;
    private Song song;

    @Before
    public void setUp() throws IOException {
        song = new Song("test.wav",
                new File("src\\test\\resources\\songs\\test.wav"));

        objectOutputStream = mock(ObjectOutputStream.class);
        serverData = mock(ServerData.class);
        bos = new ByteArrayOutputStream();
        objectOutputStream = new ObjectOutputStream(bos);
        musicPlayer = new MusicPlayer(song, objectOutputStream, serverData, FRAME_SIZE);
    }

    @Test(timeout = TIMEOUT)
    public void testReading() throws InterruptedException {
        executorService.execute(musicPlayer);

        Thread.sleep(SLEEP);

        Mockito.verify(serverData, times(1)).addToCurrentlyPlaying(song);
    }

    @Test(timeout = TIMEOUT)
    public void testStopping() throws InterruptedException {
        executorService.execute(musicPlayer);

        Thread.sleep(SLEEP);

        Mockito.verify(serverData, times(1)).addToCurrentlyPlaying(song);

        musicPlayer.stop();

        Mockito.verify(serverData, times(1)).removeCurrentlyPlaying(song);
    }
}
