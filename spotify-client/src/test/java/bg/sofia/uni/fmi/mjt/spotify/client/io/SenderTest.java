package bg.sofia.uni.fmi.mjt.spotify.client.io;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

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
        Sender sender = new Sender(socket, receiver);

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(bs);

        executorService.execute(sender);

        Thread.sleep(MILLIS);

        assertTrue(bs.toString().contains("sample command to send"));
        assertTrue(bs.toString().contains("disconnect"));
    }

    @Test(timeout = TIMEOUT)
    public void testStoppingPlayingMusic() throws IOException, InterruptedException {
        System.setIn(new ByteArrayInputStream("stop\n disconnect\n".getBytes()));
        Socket socket = mock(Socket.class);
        Receiver receiver = mock(Receiver.class);
        Sender sender = new Sender(socket, receiver);

        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        when(socket.getOutputStream()).thenReturn(bs);
        when(receiver.isPlaying()).thenReturn(true);

        executorService.execute(sender);

        Thread.sleep(MILLIS);

//        ObjectInputStream objectInputStream = new ObjectInputStream(new ByteArrayInputStream(convertToBytes()))
//        bs.toByteArray();
//        assertTrue(bs.toString().contains("stop"));
//        assertTrue(bs.toString().contains("disconnect"));
        Mockito.verify(receiver, times(1)).stopPlaying();
    }

    private <T extends Serializable> byte[] convertToBytes(T serializable) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject(serializable);
        } catch (IOException e) {
            System.out.println("Error while serializing!" + e.getMessage());
        }

        return bos.toByteArray();
    }
}
