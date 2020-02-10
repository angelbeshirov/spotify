package bg.sofia.uni.fmi.mjt.spotify.server;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

// TODO consider appending serialized objects
public class AppendObjectOutputStream extends ObjectOutputStream {

    public AppendObjectOutputStream(OutputStream out) throws IOException {
        super(out);
    }

    @Override
    protected void writeStreamHeader() throws IOException {
        reset();
    }
}