package bg.sofia.uni.fmi.mjt.spotify.client;

import java.io.IOException;
import java.net.Socket;

/**
 * @author angel.beshirov
 */
public class Client {
    public static final int SERVER_PORT = 4444;

    public Client() {

    }

    public void start() {
        try (final Socket socket = new Socket("localhost", SERVER_PORT)) {
            Receiver serverReader = new Receiver(socket);
            Sender serverWriter = new Sender(socket, serverReader);

            Thread serverReaderThread = new Thread(serverReader);
            Thread serverWriterThread = new Thread(serverWriter);

            serverReaderThread.start();
            serverWriterThread.start();

            serverReaderThread.join();
            serverWriterThread.join();

        } catch (IOException e) {
            System.out.println("Error with the server");
        } catch (InterruptedException e) {
            System.out.println("Error with the client");
        }
    }
}
