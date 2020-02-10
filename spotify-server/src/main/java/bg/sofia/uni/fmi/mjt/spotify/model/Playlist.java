package bg.sofia.uni.fmi.mjt.spotify.model;

import java.io.Serializable;
import java.util.*;

public class Playlist implements Serializable {
    private static final long serialVersionUID = -4681974495940264540L;

    private final Set<Song> songs;
    private final String name;
    private final String createdBy;

    public Playlist(String name, String createdBy) {
        this.name = name;
        this.createdBy = createdBy;
        songs = new HashSet<>();
    }

    public boolean addSong(Song song) {
        return songs.add(song);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Playlist playlist = (Playlist) o;
        return Objects.equals(name, playlist.name) &&
                Objects.equals(createdBy, playlist.createdBy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, createdBy);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Playlist name: ").append(name).append(System.lineSeparator());
        sb.append("Created by:").append(createdBy).append(System.lineSeparator());

        sb.append("Songs: ");
        for (Song song : songs) {
            sb.append(song.toString()).append(",");
        }

        return sb.append(System.lineSeparator()).toString();
    }
}
