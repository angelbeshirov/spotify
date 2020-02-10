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
    public static final int SERVER_PORT = 4444;

    public Client() {

    }

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
        } catch (IOException e) {
            System.out.println("Error with the server!");
            Logger.logError("Error with the server!", e);
        } catch (InterruptedException e) {
            System.out.println("Error with the client!");
            Logger.logError("Error with the client!", e);
        }
    }
}
