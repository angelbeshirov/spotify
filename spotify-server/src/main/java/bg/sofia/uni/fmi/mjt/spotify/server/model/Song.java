package bg.sofia.uni.fmi.mjt.spotify.server.model;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(songName, song.songName) &&
                Objects.equals(path, song.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(songName, path);
    }

    @Override
    public String toString() {
        return "Song{" +
                "songName='" + songName +
                '}';
    }
}
