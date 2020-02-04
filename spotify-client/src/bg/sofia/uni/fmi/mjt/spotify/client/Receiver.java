package bg.sofia.uni.fmi.mjt.spotify.client;

import com.google.gson.Gson;

import javax.sound.sampled.*;
import java.io.*;
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
    private Gson gson;

    public Receiver(final Socket socket) {
        this.socket = socket;
        this.gson = new Gson();
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            while (isRunning) {
                String response = reader.readLine();
                if (response != null) {
                    Message message = gson.fromJson(reader, Message.class);
                    if (MessageType.TEXT == message.getMessageType()) {
                        System.out.println("[" + formatter.format(LocalDateTime.now()) + "] " + response);
                    } else {
                        SongInfo songInfo = gson.fromJson(message.getValue(), SongInfo.class);
                        AudioFormat format = new AudioFormat(new AudioFormat.Encoding(songInfo.getEncoding()),
                                songInfo.getSampleRate(),
                                songInfo.getSampleSizeInBits(),
                                songInfo.getChannels(),
                                songInfo.getFrameSize(),
                                songInfo.getFrameRate(),
                                songInfo.isBigEndian());
                        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

                        SourceDataLine dataLine = (SourceDataLine) AudioSystem.getLine(info);
                        dataLine.open();
                        dataLine.start();
                    }
                }
            }
        } catch (IOException | LineUnavailableException e) {
            System.out.println(e.getMessage());
        }
    }

    public void stop() {
        isRunning = false;
    }
}
