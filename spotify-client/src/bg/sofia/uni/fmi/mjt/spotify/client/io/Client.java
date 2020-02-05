package bg.sofia.uni.fmi.mjt.spotify.client.io;

import bg.sofia.uni.fmi.mjt.spotify.client.music.MusicPlayer;

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
            MusicPlayer musicPlayer = new MusicPlayer(socket.getInputStream());
            Receiver receiver = new Receiver(socket, musicPlayer);
            Sender sender = new Sender(socket, receiver, musicPlayer);

            Thread serverReaderThread = new Thread(receiver);
            Thread serverWriterThread = new Thread(sender);

            serverReaderThread.start();
            serverWriterThread.start();

            serverReaderThread.join();
            serverWriterThread.join();
        } catch (IOException e) {
            System.out.println("Error with the server!");
        } catch (InterruptedException e) {
            System.out.println("Error with the client!");
        }
    }
}
