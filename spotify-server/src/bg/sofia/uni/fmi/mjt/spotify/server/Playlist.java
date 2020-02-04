package bg.sofia.uni.fmi.mjt.spotify.server;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Playlist implements Serializable {
    private static final long serialVersionUID = -4681974495940264540L;

    // TODO think how to restore the state from song name to playlists
    // TODO and songs currently in the playlist list, when serialized back if the song location
    // TODO is moved it will fail.
    private final List<Song> songs;
    private final String name;
    private final String createdBy;

    public Playlist(String name, String createdBy) {
        this.name = name;
        this.createdBy = createdBy;
        songs = new ArrayList<>();
    }

    // TODO maybe make a one-liner
    public boolean addSong(Song song) {
        if (!songs.contains(song)) {
            songs.add(song);
        }

        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Playlist name: ").append(name).append(System.lineSeparator());
        sb.append("Created by:").append(createdBy);

        sb.append("Songs: ");
        for (Song song : songs) {
            sb.append(song.toString()).append(",");
        }

        return sb.toString();
    }
}
