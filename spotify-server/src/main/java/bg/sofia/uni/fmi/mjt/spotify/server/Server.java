package bg.sofia.uni.fmi.mjt.spotify.server;

import java.io.IOException;

public interface Server {
    void start() throws IOException;

    void stop();
}
