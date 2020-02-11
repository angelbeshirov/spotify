package bg.sofia.uni.fmi.mjt.spotify.server.impl;

import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;
import bg.sofia.uni.fmi.mjt.spotify.server.Server;
import bg.sofia.uni.fmi.mjt.spotify.server.util.IOUtil;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import static org.mockito.Mockito.*;

/**
 * @author angel.beshirov
 */
public class NetServerTest {

    private ServerSocket serverSocket;
    private Socket socket;

    @Before
    public void setUp() throws IOException {
        serverSocket = mock(ServerSocket.class);
        socket = mock(Socket.class);
        when(serverSocket.accept()).thenReturn(socket).thenThrow(IOException.class);
        Message message = new Message(MessageType.TEXT, "this is sample text message".getBytes());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(IOUtil.serialize(message));
        ByteArrayOutputStream bs = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(byteArrayInputStream);
        when(socket.getOutputStream()).thenReturn(bs);
    }

    @Test
    public void testStartClientHandling() {
        Server server = new NetServer(serverSocket);
        try {
            server.start();
            Mockito.verify(socket, times(1)).getInputStream();
            Mockito.verify(socket, times(1)).getOutputStream();
        } catch (IOException e) {
            // do nothing, stops the infinite loop
        }
    }
}
