package bg.sofia.uni.fmi.mjt.spotify.server.logging;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Custom logger.
 *
 * @author angel.beshirov
 */
public class Logger {
    private static final String LOG_PATH = "src\\main\\resources\\spotify-server.log";
    private static final String ERROR = "[ERROR] ";
    private static final String INFO = "[INFO] ";

    static {
        File f = new File(LOG_PATH);
        try {
            f.createNewFile();
        } catch (IOException e) {
            System.out.println("IO Exception while trying to initialize logger!" +
                    e.getMessage());
        }
    }

    private Logger() {

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
