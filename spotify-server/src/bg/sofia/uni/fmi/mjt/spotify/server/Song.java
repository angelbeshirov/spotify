package bg.sofia.uni.fmi.mjt.spotify.server;

import java.io.Serializable;
import java.nio.file.Path;

public class Song implements Serializable {
    private static final long serialVersionUID = 1766804756793966797L;
    private String songName;
    private Path path;

    public Song(String songName, Path path) {
        this.songName = songName;
        this.path = path;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public Path getPath() {
        return path;
    }

    public void setPath(Path path) {
        this.path = path;
    }
}
