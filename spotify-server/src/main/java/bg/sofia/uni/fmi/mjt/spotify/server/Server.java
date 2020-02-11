package bg.sofia.uni.fmi.mjt.spotify.server;

import java.io.IOException;

/**
 * Server interface which contains 2 methods - start and stop.
 *
 * @author angel.beshirov
 */
public interface Server {
    void start() throws IOException;

    void stop();
}
