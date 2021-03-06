package bg.sofia.uni.fmi.mjt.spotify.client.impl;


import bg.sofia.uni.fmi.mjt.spotify.client.Client;
import bg.sofia.uni.fmi.mjt.spotify.client.serde.Serde;
import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;

import static org.mockito.Mockito.*;


/**
 * @author angel.beshirov
 */
public class NetClientTest {

    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private final InputStream originalIn = System.in;
    private final PrintStream originalOut = System.out;
    private final PrintStream originalErr = System.err;

    private Client client;
    private Socket socket;

    @Before
    public void setUpStreams() {
        socket = mock(Socket.class);
        client = new NetClient(socket);
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
    }

    @After
    public void restoreStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @Test
    public void testClientStart() throws Exception {
        Message message = new Message(MessageType.TEXT, "disconnect\n".getBytes());
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Serde.serialize(message));
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        System.setIn(byteArrayInputStream);
        when(socket.getInputStream()).thenReturn(byteArrayInputStream);
        when(socket.getOutputStream()).thenReturn(bs);
        client.start();

        Mockito.verify(socket, times(1)).getInputStream();
        Mockito.verify(socket, times(1)).getOutputStream();
    }
}
