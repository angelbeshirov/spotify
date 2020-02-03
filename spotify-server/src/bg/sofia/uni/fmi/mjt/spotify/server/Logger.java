package bg.sofia.uni.fmi.mjt.spotify.server;

import java.nio.file.Path;

/**
 *
 * TODO think about synchronizing this cuz it will be used by multiple threads
 * @author angel.beshirov
 */
public class Logger {
    private final static String LOG_PATH = "spotify.log";
    public static final String ERROR = "[ERROR] ";
    public static final String INFO = "[INFO] ";
    public static final String WARN = "[WARN] ";

    public Logger() {

    }

    public void logError(String message) {
        IOWorker.writeToFile(Path.of(LOG_PATH), ERROR + message);
    }

    public void logWarning(String message) {
        IOWorker.writeToFile(Path.of(LOG_PATH), WARN + message);
    }

    public void logInfo(String message) {
        IOWorker.writeToFile(Path.of(LOG_PATH), INFO + message);
    }
}
