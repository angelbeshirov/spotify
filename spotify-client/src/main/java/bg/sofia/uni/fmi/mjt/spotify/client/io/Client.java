package bg.sofia.uni.fmi.mjt.spotify.client.io;

import bg.sofia.uni.fmi.mjt.spotify.client.logging.Logger;
import bg.sofia.uni.fmi.mjt.spotify.client.music.MusicPlayer;
import bg.sofia.uni.fmi.mjt.spotify.model.Message;
import bg.sofia.uni.fmi.mjt.spotify.model.MessageType;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author angel.beshirov
 */
public final class Client {
    private static final int SERVER_PORT = 4444;
    private static final String SERVER_ERROR = "Error with the server!";
    private static final String CLIENT_ERROR = "Error with the client!";

    public void start() {
        try (Socket socket = new Socket("localhost", SERVER_PORT)) {
            Receiver receiver = new Receiver(socket);
            Sender sender = new Sender(socket, receiver);

            Thread senderThread = new Thread(sender);
            Thread readerThread = new Thread(receiver);

            senderThread.start();
            readerThread.start();

            senderThread.join();
            readerThread.join();
            System.out.println("waiting");
        } catch (IOException e) {
            System.out.println(SERVER_ERROR);
            Logger.logError(SERVER_ERROR, e);
        } catch (InterruptedException e) {
            System.out.println(CLIENT_ERROR);
            Logger.logError(CLIENT_ERROR, e);
        }
    }
}
