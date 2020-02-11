package bg.sofia.uni.fmi.mjt.spotify.client.io;

import bg.sofia.uni.fmi.mjt.spotify.client.serde.Serde;
import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;
import bg.sofia.uni.fmi.mjt.spotify.model.SongInfo;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author angel.beshirov
 */
public class ReceiverTest {

    private static final int MILLIS = 1000;
    private static final int TIMEOUT = 5000;
    public static final String ENCODING = "PCM_SIGNED";
    public static final int SAMPLE_RATE = 8000;
    public static final int SAMPLE_SIZE_IN_BITS = 16;
    public static final int CHANNELS = 2;
    public static final int FRAME_SIZE = 4;
    public static final int FRAME_RATE = 8000;
    public static final boolean IS_BIG_INDIAN = false;
    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private Socket socket;

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        socket = mock(Socket.class);
    }

    @After
    public void restoreStreams() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test(timeout = TIMEOUT)
    public void testTextReading() throws IOException, InterruptedException {
        Message message = new Message(MessageType.TEXT, "this is sample text message".getBytes());

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Serde.serialize(message));
        ByteArrayOutputStream bs = new ByteArrayOutputStream();

        Receiver receiver = new Receiver(new ObjectInputStream(byteArrayInputStream),
                new ObjectOutputStream(bs));

        executorService.execute(receiver);
        Thread.sleep(MILLIS);
        receiver.stop();

        assertTrue(outContent.toString().contains("this is sample text message"));
    }

    @Test(timeout = TIMEOUT)
    public void testStarting() throws IOException, InterruptedException {
        SongInfo songInfo = new SongInfo();
        songInfo.setBigEndian(IS_BIG_INDIAN);
        songInfo.setChannels(CHANNELS);
        songInfo.setEncoding(ENCODING);
        songInfo.setFrameRate(FRAME_RATE);
        songInfo.setSampleRate(SAMPLE_RATE);
        songInfo.setSampleSizeInBits(SAMPLE_SIZE_IN_BITS);
        songInfo.setFrameSize(FRAME_SIZE);
        Message message = new Message(MessageType.SONG_INFO, Serde.serialize(songInfo));

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Serde.serialize(message));
        ByteArrayOutputStream bs = new ByteArrayOutputStream();

        Receiver receiver = new Receiver(new ObjectInputStream(byteArrayInputStream),
                new ObjectOutputStream(bs));
        executorService.execute(receiver);
        Thread.sleep(MILLIS);

        Assert.assertTrue(receiver.isPlaying());
    }

    @Test(timeout = TIMEOUT)
    public void testStopping() throws IOException, InterruptedException {
        SongInfo songInfo = new SongInfo();
        songInfo.setBigEndian(IS_BIG_INDIAN);
        songInfo.setChannels(CHANNELS);
        songInfo.setEncoding(ENCODING);
        songInfo.setFrameRate(FRAME_RATE);
        songInfo.setSampleRate(SAMPLE_RATE);
        songInfo.setSampleSizeInBits(SAMPLE_SIZE_IN_BITS);
        songInfo.setFrameSize(FRAME_SIZE);
        Message message = new Message(MessageType.SONG_INFO, Serde.serialize(songInfo));

        ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(Serde.serialize(message));
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        when(socket.getInputStream()).thenReturn(byteArrayInputStream);


        Receiver receiver = new Receiver(new ObjectInputStream(byteArrayInputStream),
                new ObjectOutputStream(bs));
        executorService.execute(receiver);
        Thread.sleep(MILLIS);
        receiver.stopPlaying();

        Assert.assertFalse(receiver.isPlaying());
    }


}
