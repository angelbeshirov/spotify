package bg.sofia.uni.fmi.mjt.spotify.server.logging;

import bg.sofia.uni.fmi.mjt.spotify.server.io.IOUtil;

import java.nio.file.Path;

/**
 * TODO think about synchronizing this cuz it will be used by multiple threads
 *
 * @author angel.beshirov
 */
public class Logger {
    private final static String LOG_PATH = "src\\main\\resources\\spotify.log";
    public static final String ERROR = "[ERROR] ";
    public static final String INFO = "[INFO] ";
    public static final String WARN = "[WARN] ";

    private final String name;

    public Logger(String name) {
        this.name = name;
    }

    public static void logError(String message) {
        IOUtil.writeToFile(Path.of(LOG_PATH), ERROR + message);
    }

    public static void logWarning(String message) {
        IOUtil.writeToFile(Path.of(LOG_PATH), WARN + message);
    }

    public static void logInfo(String message) {
        IOUtil.writeToFile(Path.of(LOG_PATH), INFO + message);
    }
}
