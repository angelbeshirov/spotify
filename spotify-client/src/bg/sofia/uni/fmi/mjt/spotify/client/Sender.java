package bg.sofia.uni.fmi.mjt.spotify.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author angel.beshirov
 */
public class Sender implements Runnable {
    private static final String DISCONNECT = "disconnect";
    private final Receiver receiver;
    private final Socket socket;

    public Sender(final Socket socket, final Receiver receiver) {
        this.socket = socket;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        try (PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader consoleReader = new BufferedReader(new InputStreamReader(System.in))) {

            while (true) {
                System.out.println("Enter a command to send to the server:");
                String command = consoleReader.readLine();
                writer.println(command);

                if (DISCONNECT.equals(command)) {
                    receiver.stop();
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
