package bg.sofia.uni.fmi.mjt.spotify.server.client;

import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;
import bg.sofia.uni.fmi.mjt.spotify.model.ServerData;
import bg.sofia.uni.fmi.mjt.spotify.server.io.IOUtil;
import bg.sofia.uni.fmi.mjt.spotify.server.io.Server;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author angel.beshirov
 */
public class ClientHandlerTest {

    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private ClientHandler clientHandler;
    private Socket socket;
    private ServerSocket serverSocket;
    private ServerData serverData;

    @Before
    public void setUp() throws IOException {
        serverSocket = mock(ServerSocket.class);
        socket = mock(Socket.class);
        serverData = mock(ServerData.class);

        when(serverSocket.accept()).thenReturn(socket).thenThrow();
    }

    @Test
    public void testCommandReading() throws IOException {
        Message message = new Message(MessageType.TEXT, "this is sample text message".getBytes());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(IOUtil.serialize(message));
        ByteArrayOutputStream bs = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(byteArrayInputStream);
        when(socket.getOutputStream()).thenReturn(bs);

//        executorService.execute(new Thread(new ClientHandler(socket, serverData)));


        executorService.execute(() -> {
            Server server = new Server(serverSocket);
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

//        executorService.execute(clientHandler);

    }
}
