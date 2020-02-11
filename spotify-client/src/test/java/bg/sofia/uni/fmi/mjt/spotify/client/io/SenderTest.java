package bg.sofia.uni.fmi.mjt.spotify.client.io;

import bg.sofia.uni.fmi.mjt.spotify.client.util.Util;
import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author angel.beshirov
 */
public class SenderTest {

    private static final int MILLIS = 1000;
    private static final int TIMEOUT = 5000;
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
        System.setIn(new ByteArrayInputStream("sample command to send\n"
                .getBytes()));
        Receiver receiver = mock(Receiver.class);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        Sender sender = new Sender(new ObjectOutputStream(bs), receiver);

        executorService.execute(sender);

        Thread.sleep(MILLIS);
        Message sampleCommand = new Message(MessageType.TEXT, "sample command to send"
                .getBytes());

        byte[] arr = bs.toByteArray();

        assertTrue(isSubArray(arr, Util.serialize(sampleCommand)));
    }

    @Test(timeout = TIMEOUT)
    public void testStoppingPlayingMusic() throws IOException, InterruptedException {
        System.setIn(new ByteArrayInputStream("stop\n".getBytes()));
        Receiver receiver = mock(Receiver.class);
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        Sender sender = new Sender(new ObjectOutputStream(bs), receiver);

        when(receiver.isPlaying()).thenReturn(true);

        executorService.execute(sender);

        Thread.sleep(MILLIS);

        Mockito.verify(receiver, times(1)).stopPlaying();
    }

    static boolean isSubArray(byte[] arr, byte[] subArr) {
        int n = arr.length;
        int m = subArr.length;
        int i = 0, j = 0;

        while (i < n && j < m) {
            if (arr[i] == subArr[j]) {

                i++;
                j++;

                if (j == m)
                    return true;
            } else {
                i = i - j + 1;
                j = 0;
            }
        }
        return false;
    }
}
