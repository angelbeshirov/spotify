package bg.sofia.uni.fmi.mjt.spotify.client.logging;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author angel.beshirov
 */
public class Logger {

    private final static String LOG_PATH = "src\\main\\resources\\spotify-client.log";
    private static final String ERROR = "[ERROR] ";
    private static final String INFO = "[INFO] ";

    static {
        File f = new File(LOG_PATH);
        try {
            f.createNewFile();
        } catch (IOException e) {
            System.out.println("IO Exception while trying to initialize logger!" + e.getMessage());
        }
    }

    public static void logError(String message, Throwable throwable) {
        writeToFile(ERROR + message + throwable.getMessage());
    }

    public static void logInfo(String message) {
        writeToFile(INFO + message);
    }

    private static synchronized void writeToFile(String message) {
        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_PATH, true))) {
            out.println(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
