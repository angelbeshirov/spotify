package bg.sofia.uni.fmi.mjt.spotify.client.io;

import bg.sofia.uni.fmi.mjt.spotify.client.logging.Logger;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * @author angel.beshirov
 */
public final class Client {
    private static final String CLIENT_ERROR = "Error with the client!";

    private final Socket socket;

    public Client(Socket socket) {
        this.socket = socket;
    }

    public void start() throws IOException {
        try (ObjectOutputStream objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream objectInputStream = new ObjectInputStream(socket.getInputStream())) {
            Receiver receiver = new Receiver(objectInputStream, objectOutputStream);
            Sender sender = new Sender(objectOutputStream, receiver);

            Thread senderThread = new Thread(sender);
            Thread readerThread = new Thread(receiver);

            senderThread.start();
            readerThread.start();

            senderThread.join();
            readerThread.join();
        } catch (InterruptedException e) {
            System.out.println(CLIENT_ERROR);
            Logger.logError(CLIENT_ERROR, e);
        }
    }
}
