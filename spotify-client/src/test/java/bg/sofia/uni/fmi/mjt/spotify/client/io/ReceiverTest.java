package bg.sofia.uni.fmi.mjt.spotify.client.io;

import bg.sofia.uni.fmi.mjt.spotify.client.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.client.model.MessageType;
import bg.sofia.uni.fmi.mjt.spotify.client.model.SongInfo;
import bg.sofia.uni.fmi.mjt.spotify.client.music.MusicPlayer;
import com.google.gson.Gson;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author angel.beshirov
 */
public class ReceiverTest {

    private static final int MILLIS = 1000;
    private static final int TIMEOUT = 5000;
    private static final Gson GSON = new Gson();
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private Socket socket;
    private MusicPlayer musicPlayer;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        socket = mock(Socket.class);
        musicPlayer = mock(MusicPlayer.class);
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test(timeout = TIMEOUT)
    public void testTextReading() throws IOException, InterruptedException {
        Message message = new Message();
        message.setMessageType(MessageType.TEXT);
        message.setValue("this is some sample text message");

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(GSON.toJson(message).getBytes());
        when(socket.getInputStream()).thenReturn(byteArrayInputStream);

        Receiver receiver = new Receiver(socket, musicPlayer);
        executorService.execute(receiver);
        Thread.sleep(MILLIS);
        receiver.stop();

        assertTrue(outContent.toString().contains("this is some sample text message"));
    }

    @Test(timeout = TIMEOUT)
    public void testStartingMusicPlayer() throws IOException, InterruptedException {
        Message message = new Message();
        message.setMessageType(MessageType.JSON);
        message.setValue(GSON.toJson(new SongInfo()));

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(GSON.toJson(message).getBytes());
        when(socket.getInputStream()).thenReturn(byteArrayInputStream);

        Receiver receiver = new Receiver(socket, musicPlayer);
        executorService.execute(receiver);
        Thread.sleep(MILLIS);
        receiver.stop();

        Mockito.verify(musicPlayer).start(any(SongInfo.class));
    }

}
