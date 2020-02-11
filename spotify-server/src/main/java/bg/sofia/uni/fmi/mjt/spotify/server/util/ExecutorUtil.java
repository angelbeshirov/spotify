package bg.sofia.uni.fmi.mjt.spotify.server.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Shuts down the executor service to allow reclamation of its resources.
 *
 * @author angel.beshirov
 */
public class ExecutorUtil {

    private static final int TIMEOUT = 800;

    private ExecutorUtil() {

    }

    public static void shutdown(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(TIMEOUT, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }
}
