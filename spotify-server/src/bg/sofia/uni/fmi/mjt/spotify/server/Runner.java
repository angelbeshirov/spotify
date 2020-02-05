package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.server.io.Server;

/**
 * @author angel.beshirov
 */
public class Runner {
    public static void main(final String[] args) {
        final Server server = new Server();
        server.start();
    }
}
