package bg.sofia.uni.fmi.mjt.spotify.client.io;

import bg.sofia.uni.fmi.mjt.spotify.client.music.MusicPlayer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author angel.beshirov
 */
public class SenderTest {

    public static final int MILLIS = 1000;
    public static final int TIMEOUT = 5000;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUpStreams() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test(timeout = TIMEOUT)
    public void testSendingCommand() throws IOException, InterruptedException {
        System.setIn(new ByteArrayInputStream("sample command to send\n disconnect\n".getBytes()));
        Socket socket = mock(Socket.class);
        Receiver receiver = mock(Receiver.class);
        MusicPlayer musicPlayer = mock(MusicPlayer.class);
        Sender sender = new Sender(socket, receiver, musicPlayer);

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(bs);

        executorService.execute(sender);

        Thread.sleep(MILLIS);

        assertTrue(bs.toString().contains("sample command to send"));
        assertTrue(bs.toString().contains("disconnect"));
    }
}
