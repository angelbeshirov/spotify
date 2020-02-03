package bg.sofia.uni.fmi.mjt.spotify.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @author angel.beshirov
 */
public class Receiver implements Runnable {
    private final Socket socket;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private volatile boolean isRunning = true;

    public Receiver(final Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            while (isRunning) {
                String response = reader.readLine();
                if (response != null) {
                    System.out.println("[" + formatter.format(LocalDateTime.now()) + "] " + response);
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void stop() {
        isRunning = false;
    }
}
