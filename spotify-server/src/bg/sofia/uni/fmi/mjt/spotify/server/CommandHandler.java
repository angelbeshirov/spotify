package bg.sofia.uni.fmi.mjt.spotify.server;

import bg.sofia.uni.fmi.mjt.spotify.server.io.IOWorker;
import bg.sofia.uni.fmi.mjt.spotify.server.logging.Logger;
import bg.sofia.uni.fmi.mjt.spotify.server.model.*;
import bg.sofia.uni.fmi.mjt.spotify.server.music.MusicPlayer;
import com.google.gson.Gson;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author angel.beshirov
 * TODO in properties the directory path
 */
public class CommandHandler {
    private static final Gson gson = new Gson();
    private static final String USERS_FILE_NAME = "src\\main\\resources\\users.bin";
    private static final String PLAYLISTS_FILE_NAME = "src\\main\\resources\\playlists.bin";
    private static final int MUSIC_PLAY_DELAY = 1;
    final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1); // TODO this should come in the constructor maybe

    private Map<User, ClientHandler> loggedInUsers;
    private final ClientHandler clientHandler;
    private User user;
    private final List<User> registeredUsers;
    private final List<Playlist> playlists;
    private final List<Song> songs;

    private MusicPlayer musicPlayer;

    public CommandHandler(ClientHandler clientHandler, Map<User, ClientHandler> loggedInUsers, List<User> registeredUsers, List<Playlist> playlists, List<Song> songs) {
        this.clientHandler = clientHandler;
        this.loggedInUsers = loggedInUsers;
        this.registeredUsers = registeredUsers;
        this.playlists = playlists;
        this.songs = songs;
        this.musicPlayer = null; // TODO somehow improve with play method
    }

    public Optional<String> handleCommand(Command command, String... args) {
        switch (command) {
            // TODO return vs break is switch statement
            case LOGIN:
                return handleLogin(args);
            case REGISTER:
                return handleRegister(args);
            case DISCONNECT:
                return handleDisconnect();
            case PLAY:
                return handleSongPlaying(args);
            case STOP:
                return handleStopPlaying();
            case TOP:
            case CREATE_PLAYLIST:
                return handleCreatePlaylist(args);
            case SHOW_PLAYLIST:
                return handleShowPlaylist(args);
            case ADD_SONG_TO:
                return handleAddSongTo(args);
            case SEARCH:
                return handleSearch(args);
        }

        return Optional.empty();
    }

    private Optional<String> handleRegister(String... args) {
        if (args == null || args.length != 2) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Invalid arguments count for register!")));
        } else if (this.user != null) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "You are already logged in!")));
        }

        String email = args[0];
        String password = args[1];

        if (email == null || password == null) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Email or password is invalid!"))); // TODO possibly improve this error handling
        }

        User user = new User(email, password);
        if (registeredUsers.contains(user)) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "User with this email address already exists!")));
        }

        registeredUsers.add(user);
        IOWorker.writeToFile(Path.of(USERS_FILE_NAME), registeredUsers);

        return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Registration was successful")));
    }

    private Optional<String> handleLogin(String... args) {
        if (args == null || args.length != 2) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Invalid arguments for login!")));
        } else if (this.user != null) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "You are already logged in!")));
        }

        User user = new User(args[0], args[1]);
        int index = registeredUsers.indexOf(user);

        if (index == -1) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "User with this email does not exist!")));
        }

        User registeredUser = registeredUsers.get(index);

        if (!registeredUser.getPassword().equals(user.getPassword())) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Wrong password")));
        }

        this.user = registeredUser;
        loggedInUsers.put(this.user, clientHandler);

        return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Successfully logged in!")));
    }

    private Optional<String> handleDisconnect() {
        if (user != null) {
            loggedInUsers.remove(user);
        }

        return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Successfully disconnected!")));
    }

    private Optional<String> handleSongPlaying(String... args) {
        if (args == null || args.length != 1) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Invalid arguments for song playing!")));
        } else if (this.user == null) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "You have to first log in before you can listen to songs!")));
        }

        String songName = args[0];
        Song songToPlay = null;
        for (Song song : this.songs) {
            if (song.getSongName().equalsIgnoreCase(songName)) {
                songToPlay = song;
            }
        }

        if (songToPlay == null) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "There is no song with this name!")));
        }


        SongInfo songInfo;
        AudioFormat format;
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(songToPlay.getPath().toFile())) {
            format = stream.getFormat();
            songInfo = new SongInfo();
            songInfo.setBigEndian(format.isBigEndian());
            songInfo.setChannels(format.getChannels());
            songInfo.setEncoding(format.getEncoding().toString());
            songInfo.setFrameRate(format.getFrameRate());
            songInfo.setFrameSize(format.getFrameSize());
            songInfo.setSampleRate(format.getSampleRate());
            songInfo.setSampleSizeInBits(format.getSampleSizeInBits());

            this.musicPlayer = new MusicPlayer(songToPlay, this.clientHandler.getSocket().getOutputStream());
            executor.schedule(this.musicPlayer, MUSIC_PLAY_DELAY, TimeUnit.SECONDS);
        } catch (IOException e) {
            Logger.logError("IOException. " + e.getMessage());
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Internal server error!")));
        } catch (UnsupportedAudioFileException e) {
            Logger.logError("Unsupported format exception. " + e.getMessage());
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Unsupported format!")));
        }
        String value = gson.toJson(songInfo, SongInfo.class);

        return Optional.of(gson.toJson(new Message(MessageType.JSON, value)));
    }

    private Optional<String> handleStopPlaying() {
        if (this.musicPlayer != null) {
            this.musicPlayer.stop();
        }

        return Optional.empty();
    }

    private Optional<String> handleCreatePlaylist(String... args) {
        if (args == null || args.length != 1) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Invalid arguments for creating playlist!")));
        } else if (this.user == null) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "You have to log in before you can create playlists!")));
        }

        playlists.add(new Playlist(args[0], user.getEmail())); // TODO multiple arguments can be concatenated

        IOWorker.writeToFile(Path.of(PLAYLISTS_FILE_NAME), playlists);
        return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Playlist was created successfully!")));
    }

    private Optional<String> handleShowPlaylist(String... args) {
        if (args == null || args.length != 1) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Invalid arguments for creating playlist!")));
        } else if (this.user == null) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "You have to log in before you can request info about playlist!")));
        }

        int index = playlists.indexOf(new Playlist(args[0], this.user.getEmail()));
        if (index == -1) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "You don't have playlist with name " + args[0])));
        }

        // TODO playlist should be uniquely identified by name + email
        return Optional.of(gson.toJson(new Message(MessageType.TEXT, playlists.get(index).toString())));
    }

    private Optional<String> handleAddSongTo(String... args) {
        if (args == null || args.length != 2) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Invalid arguments for adding song to playlist!")));
        } else if (this.user == null) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "You have to log in before you can request info about playlist!")));
        }

        int index1 = playlists.indexOf(new Playlist(args[0], this.user.getEmail()));
        if (index1 == -1) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "You don't have playlist with name " + args[0])));
        }

        Playlist playlist = playlists.get(index1);

        boolean k = false;
        for (Song song : songs) {
            if (song.getSongName().equals(args[1])) {
                playlist.addSong(song); // TODO what if it already exists in the playlist
                k = true;
            }
        }

        return Optional.of(gson.toJson(new Message(MessageType.TEXT, k ? "Song was added successfully" : "There is not a song with that name!")));
    }

    private Optional<String> handleSearch(String... args) {
        if (args == null || args.length == 0) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Invalid arguments. Command has the following syntax 'search <words>'")));
        }

        StringBuilder sb = new StringBuilder("Found songs: ");
        List<Song> foundSongs = new ArrayList<>();
        for (String keyword : args) {
            foundSongs.addAll(songs.stream().filter(x -> x.getSongName().toLowerCase().contains(keyword)).collect(Collectors.toList()));
        }

        for (Song song : foundSongs) {
            sb.append(song.getSongName()).append(",");
        }

        return Optional.of(gson.toJson(new Message(MessageType.TEXT, sb.toString())));
    }
}
