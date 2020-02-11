package bg.sofia.uni.fmi.mjt.spotify.server.client;

import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;
import bg.sofia.uni.fmi.mjt.spotify.model.ServerData;
import bg.sofia.uni.fmi.mjt.spotify.server.util.ExecutorUtil;
import bg.sofia.uni.fmi.mjt.spotify.server.util.IOUtil;
import org.junit.*;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.*;

/**
 * @author angel.beshirov
 */
public class ClientHandlerTest {

    public static final int SLEEP = 1500;
    public static final int TIMEOUT = 5000;
    private static ExecutorService executorService;
    private Socket socket;
    private ServerData serverData;

    @BeforeClass
    public static void init() {
        executorService = Executors.newFixedThreadPool(1);
    }

    @AfterClass
    public static void close() {
        ExecutorUtil.shutdown(executorService);
    }

    @Before
    public void setUp() {
        socket = mock(Socket.class);
        serverData = mock(ServerData.class);
    }

    @Test(timeout = TIMEOUT)
    public void testCommandReading() throws IOException, InterruptedException {
        Message message = new Message(MessageType.TEXT, "this is sample text message".getBytes());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(IOUtil.serialize(message));
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ClientHandler clientHandler = new ClientHandler(socket, serverData);

        when(socket.getInputStream()).thenReturn(byteArrayInputStream);
        when(socket.getOutputStream()).thenReturn(bs);

        Message expected = new Message(MessageType.TEXT, "Invalid command!".getBytes());

        executorService.execute(clientHandler);
        Thread.sleep(SLEEP);
        clientHandler.stop();

        Assert.assertArrayEquals(bs.toByteArray(), IOUtil.serialize(expected));
        Mockito.verify(socket, times(1)).close();
    }
}
