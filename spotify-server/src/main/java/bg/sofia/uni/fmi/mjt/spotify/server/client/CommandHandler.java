package bg.sofia.uni.fmi.mjt.spotify.server.client;

import bg.sofia.uni.fmi.mjt.spotify.model.*;
import bg.sofia.uni.fmi.mjt.spotify.server.logging.impl.Logger;
import bg.sofia.uni.fmi.mjt.spotify.server.music.MusicPlayer;
import bg.sofia.uni.fmi.mjt.spotify.server.util.ExecutorUtil;
import bg.sofia.uni.fmi.mjt.spotify.server.util.IOUtil;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Contains the logic for handling commands from the client.
 *
 * @author angel.beshirov
 */
public class CommandHandler {
    private static final int MUSIC_PLAY_DELAY_SECONDS = 1;

    private final ScheduledThreadPoolExecutor executor;
    private final ClientHandler clientHandler;
    private final ServerData serverData;
    private User user;

    private MusicPlayer musicPlayer;

    public CommandHandler(ClientHandler clientHandler, ServerData serverData) {
        this.clientHandler = clientHandler;
        this.serverData = serverData;
        this.executor = new ScheduledThreadPoolExecutor(1);
        this.musicPlayer = null;
    }

    public Optional<Message> handleCommand(Command command, String... args) {
        switch (command) {
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
                return handleTopSongs(args);
            case CREATE_PLAYLIST:
                return handleCreatePlaylist(args);
            case SHOW_PLAYLIST:
                return handleShowPlaylist(args);
            case ADD_SONG_TO:
                return handleAddSongToPlaylist(args);
            case SEARCH:
                return handleSearch(args);
        }

        return Optional.empty();
    }

    public void close() {
        ExecutorUtil.shutdown(executor);
    }

    private Optional<Message> handleRegister(String... args) {
        if (args == null || args.length != 2) {
            return Optional.of(new Message(MessageType.TEXT,
                    "Invalid arguments count for register!".getBytes()));
        } else if (this.user != null) {
            return Optional.of(new Message(MessageType.TEXT,
                    "You are already logged in!".getBytes()));
        }

        String email = args[0];
        String password = args[1];

        if (email == null || password == null) {
            return Optional.of(new Message(MessageType.TEXT,
                    "Email or password is invalid!".getBytes()));
        }

        User user = new User(email, password);
        if (serverData.getUser(user) != null) {
            return Optional.of(new Message(MessageType.TEXT,
                    "User with this email address already exists!".getBytes()));
        }

        serverData.addUser(user);
        serverData.saveUsers();

        return Optional.of(new Message(MessageType.TEXT,
                "Registration was successful".getBytes()));
    }

    private Optional<Message> handleLogin(String... args) {
        if (args == null || args.length != 2) {
            return Optional.of(new Message(MessageType.TEXT,
                    "Invalid arguments for login!".getBytes()));
        } else if (this.user != null) {
            return Optional.of(new Message(MessageType.TEXT,
                    "You are already logged in!".getBytes()));
        }

        User user = new User(args[0], args[1]);
        User savedUser = serverData.getUser(user);

        if (savedUser == null) {
            return Optional.of(new Message(MessageType.TEXT,
                    "User with this email does not exist!".getBytes()));
        }

        if (!Objects.equals(savedUser.getPassword(), user.getPassword())) {
            return Optional.of(new Message(MessageType.TEXT, "Wrong password".getBytes()));
        }

        this.user = savedUser;
        serverData.addLoggedInUser(this.user, clientHandler);

        return Optional.of(new Message(MessageType.TEXT, "Successfully logged in!".getBytes()));
    }

    private Optional<Message> handleDisconnect() {
        if (user != null) {
            serverData.logOut(user);
        }

        return Optional.of(new Message(MessageType.TEXT, "Successfully disconnected!".getBytes()));
    }

    private Optional<Message> handleSongPlaying(String... args) {
        if (args == null || args.length != 1) {
            return Optional.of(new Message(MessageType.TEXT,
                    "Invalid arguments for song playing!".getBytes()));
        } else if (this.user == null) {
            return Optional.of(new Message(MessageType.TEXT,
                    "You have to first log in before you can listen to songs!".getBytes()));
        }

        String songName = args[0];
        Song songToPlay = serverData.getSongByName(songName);

        if (songToPlay == null) {
            return Optional.of(new Message(MessageType.TEXT,
                    "There is no song with this name!".getBytes()));
        }


        SongInfo songInfo;
        AudioFormat format;
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(songToPlay.getFile())) {
            format = stream.getFormat();
            songInfo = new SongInfo();
            songInfo.setBigEndian(format.isBigEndian());
            songInfo.setChannels(format.getChannels());
            songInfo.setEncoding(format.getEncoding().toString());
            songInfo.setFrameRate(format.getFrameRate());
            songInfo.setFrameSize(format.getFrameSize());
            songInfo.setSampleRate(format.getSampleRate());
            songInfo.setSampleSizeInBits(format.getSampleSizeInBits());

            musicPlayer = new MusicPlayer(songToPlay,
                    clientHandler.getObjectOutputStream(),
                    serverData,
                    format.getFrameSize());

            executor.schedule(musicPlayer, MUSIC_PLAY_DELAY_SECONDS, TimeUnit.SECONDS);
        } catch (IOException e) {
            Logger.logError("IOException. ", e);
            return Optional.of(new Message(MessageType.TEXT,
                    "Internal server error!".getBytes()));
        } catch (UnsupportedAudioFileException e) {
            Logger.logError("Unsupported format exception. ", e);
            return Optional.of(new Message(MessageType.TEXT,
                    "Unsupported format!".getBytes()));
        }

        return Optional.of(new Message(MessageType.SONG_INFO, IOUtil.serialize(songInfo)));
    }

    private Optional<Message> handleStopPlaying() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer = null;
        }

        return Optional.empty();
    }

    private Optional<Message> handleTopSongs(String... args) {
        if (args == null || args.length != 1) {
            return Optional.of(new Message(MessageType.TEXT,
                    "Invalid arguments for generating top songs!".getBytes()));
        } else if (this.user == null) {
            return Optional.of(new Message(MessageType.TEXT,
                    "You have to log in before you can get information about top songs!".getBytes()));
        }

        List<Map.Entry<Song, Integer>> sorted =
                serverData.getTopNCurrentlyPlayingSorted(Integer.parseInt(args[0]));

        StringBuilder sb = new StringBuilder("Top playing songs are: ");
        for (Map.Entry<Song, Integer> entry : sorted) {
            sb.append(entry.getKey()
                    .getSongName())
                    .append(": ")
                    .append(entry.getValue())
                    .append(System.lineSeparator());
        }

        return Optional.of(new Message(MessageType.TEXT, sb.toString().getBytes()));
    }

    private Optional<Message> handleCreatePlaylist(String... args) {
        if (args == null || args.length != 1) {
            return Optional.of(new Message(MessageType.TEXT,
                    "Invalid arguments for creating playlist!".getBytes()));
        } else if (this.user == null) {
            return Optional.of(new Message(MessageType.TEXT,
                    "You have to log in before you can create playlists!".getBytes()));
        }

        Playlist playlist = new Playlist(args[0], user.getEmail());
        Playlist savedPlaylist = serverData.getPlaylist(playlist);

        if (savedPlaylist == null) {
            serverData.addPlaylist(playlist);
            serverData.savePlaylists();
            return Optional.of(new Message(MessageType.TEXT,
                    "Playlist was created successfully!".getBytes()));
        } else {
            return Optional.of(new Message(MessageType.TEXT,
                    "Playlist with this name already exists!".getBytes()));
        }
    }

    private Optional<Message> handleShowPlaylist(String... args) {
        if (args == null || args.length != 1) {
            return Optional.of(new Message(MessageType.TEXT,
                    "Invalid arguments for creating playlist!".getBytes()));
        } else if (this.user == null) {
            return Optional.of(new Message(MessageType.TEXT,
                    "You have to log in before you can request info about playlist!".getBytes()));
        }

        Playlist playlistToCheck = new Playlist(args[0], this.user.getEmail());

        Playlist playlist = serverData.getPlaylist(playlistToCheck);
        if (playlist == null) {
            return Optional.of(new Message(MessageType.TEXT,
                    ("You don't have playlist with name " + args[0]).getBytes()));
        }

        return Optional.of(new Message(MessageType.TEXT, playlist.toString().getBytes()));
    }

    private Optional<Message> handleAddSongToPlaylist(String... args) {
        if (args == null || args.length != 2) {
            return Optional.of(new Message(MessageType.TEXT,
                    "Invalid arguments for adding song to playlist!".getBytes()));
        } else if (this.user == null) {
            return Optional.of(new Message(MessageType.TEXT,
                    "You have to log in before you can add song to playlist!".getBytes()));
        }

        Playlist playlist = serverData.getPlaylist(new Playlist(args[0], this.user.getEmail()));
        if (playlist == null) {
            return Optional.of(new Message(MessageType.TEXT,
                    ("You don't have playlist with name " + args[0]).getBytes()));
        }

        Song song = serverData.getSongByName(args[1]);

        if (song == null) {
            return Optional.of(new Message(MessageType.TEXT,
                    "Song with this name does not exist!".getBytes()));
        }

        if (playlist.addSong(song)) {
            serverData.savePlaylists();
            return Optional.of(new Message(MessageType.TEXT,
                    "Song was added successfully".getBytes()));
        }

        return Optional.of(new Message(MessageType.TEXT,
                "Playlist already contains this song".getBytes()));
    }

    private Optional<Message> handleSearch(String... args) {
        if (args == null || args.length == 0) {
            return Optional.of(new Message(MessageType.TEXT,
                    "Invalid arguments. Command has the following syntax 'search <words>'".getBytes()));
        }

        StringBuilder sb = new StringBuilder("Found songs: ");
        List<Song> foundSongs = new ArrayList<>();
        for (String keyword : args) {
            foundSongs.addAll(serverData.getSongs()
                    .stream()
                    .filter(x -> x.getSongName().toLowerCase().contains(keyword))
                    .collect(Collectors.toList()));
        }

        for (Song song : foundSongs) {
            sb.append(song.getSongName()).append(",");
        }

        return Optional.of(new Message(MessageType.TEXT, sb.toString().getBytes()));
    }
}
