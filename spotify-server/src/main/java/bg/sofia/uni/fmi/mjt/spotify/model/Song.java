package bg.sofia.uni.fmi.mjt.spotify.model;

import java.io.File;
import java.io.Serializable;
import java.util.Objects;

/**
 * Object representation of a song. Contains songName and
 * path to where the file is located. Two songs are considered equal if
 * their names and file paths are equal.
 */
public class Song implements Serializable {
    private static final long serialVersionUID = 1766804756793966797L;
    private final String songName;
    private final File file;

    public Song(String songName, File file) {
        this.songName = songName;
        this.file = file;
    }

    public String getSongName() {
        return songName;
    }

    public File getFile() {
        return file;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Song song = (Song) o;
        return Objects.equals(songName, song.songName) &&
                Objects.equals(file, song.file);
    }

    @Override
    public int hashCode() {
        return Objects.hash(songName, file);
    }

    @Override
    public String toString() {
        return songName;
    }
}
