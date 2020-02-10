package bg.sofia.uni.fmi.mjt.spotify.client.logging;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * TODO think about synchronizing this cuz it will be used by multiple threads
 *
 * @author angel.beshirov
 */
public class Logger {

    private final static String LOG_PATH = "src\\main\\resources\\spotify-client.log";
    private static final String ERROR = "[ERROR] ";
    private static final String INFO = "[INFO] ";
    private static final String WARN = "[WARN] ";

    static {
        File f = new File(LOG_PATH);
        try {
            f.createNewFile();
        } catch (IOException e) {
            System.out.println("IO Exception while trying to initialize logger!" + e.getMessage());
        }
    }

    public static void logError(String message) {
        writeToFile(Path.of(LOG_PATH), ERROR + message);
    }

    public static void logWarning(String message) {
        writeToFile(Path.of(LOG_PATH), WARN + message);
    }

    public static void logInfo(String message) {
        writeToFile(Path.of(LOG_PATH), INFO + message);
    }

    private static synchronized void writeToFile(Path file, String message) {
        try (var oos = new OutputStreamWriter(Files.newOutputStream(file))) {
            oos.write(message);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
