package bg.sofia.uni.fmi.mjt.spotify.server;

/**
 * @author angel.beshirov
 */
public class Runner {
    public static void main(final String[] args) {
        final Server chatServer = new Server();
        chatServer.start();
    }
}
