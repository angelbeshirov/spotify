package bg.sofia.uni.fmi.mjt.spotify.server.client;

import bg.sofia.uni.fmi.mjt.spotify.model.*;
import bg.sofia.uni.fmi.mjt.spotify.server.io.IOUtil;
import bg.sofia.uni.fmi.mjt.spotify.server.logging.Logger;
import bg.sofia.uni.fmi.mjt.spotify.server.music.MusicPlayer;
import com.google.gson.Gson;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author angel.beshirov
 * TODO in properties the directory path
 */
public class CommandHandler {
    private static final Gson GSON = new Gson();
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
    private final Map<Song, Integer> currentlyPlaying;

    private MusicPlayer musicPlayer;

    public CommandHandler(ClientHandler clientHandler, Map<User, ClientHandler> loggedInUsers, List<User> registeredUsers, List<Playlist> playlists, List<Song> songs, Map<Song, Integer> currentlyPlaying) {
        this.clientHandler = clientHandler;
        this.loggedInUsers = loggedInUsers;
        this.registeredUsers = registeredUsers;
        this.playlists = playlists;
        this.songs = songs;
        this.musicPlayer = null; // TODO somehow improve with play method
        this.currentlyPlaying = currentlyPlaying;
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
                return handleAddSongTo(args);
            case SEARCH:
                return handleSearch(args);
        }

        return Optional.empty();
    }

    private Optional<Message> handleRegister(String... args) {
        if (args == null || args.length != 2) {
            return Optional.of(new Message(MessageType.TEXT, "Invalid arguments count for register!".getBytes()));
        } else if (this.user != null) {
            return Optional.of(new Message(MessageType.TEXT, "You are already logged in!".getBytes()));
        }

        String email = args[0];
        String password = args[1];

        if (email == null || password == null) {
            return Optional.of(new Message(MessageType.TEXT, "Email or password is invalid!".getBytes())); // TODO possibly improve this error handling
        }

        User user = new User(email, password);
        if (registeredUsers.contains(user)) {
            return Optional.of(new Message(MessageType.TEXT, "User with this email address already exists!".getBytes()));
        }

        registeredUsers.add(user);
        IOUtil.writeToFile(Path.of(USERS_FILE_NAME), registeredUsers);

        return Optional.of(new Message(MessageType.TEXT, "Registration was successful".getBytes()));
    }

    private Optional<Message> handleLogin(String... args) {
        if (args == null || args.length != 2) {
            return Optional.of(new Message(MessageType.TEXT, "Invalid arguments for login!".getBytes()));
        } else if (this.user != null) {
            return Optional.of(new Message(MessageType.TEXT, "You are already logged in!".getBytes()));
        }

        User user = new User(args[0], args[1]);
        int index = registeredUsers.indexOf(user);

        if (index == -1) {
            return Optional.of(new Message(MessageType.TEXT, "User with this email does not exist!".getBytes()));
        }

        User registeredUser = registeredUsers.get(index);

        if (!registeredUser.getPassword().equals(user.getPassword())) {
            return Optional.of(new Message(MessageType.TEXT, "Wrong password".getBytes()));
        }

        this.user = registeredUser;
        loggedInUsers.put(this.user, clientHandler);

        return Optional.of(new Message(MessageType.TEXT, "Successfully logged in!".getBytes()));
    }

    private Optional<Message> handleDisconnect() {
        if (user != null) {
            loggedInUsers.remove(user);
        }

        return Optional.of(new Message(MessageType.TEXT, "Successfully disconnected!".getBytes()));
    }

    private Optional<Message> handleSongPlaying(String... args) {
        if (args == null || args.length != 1) {
            return Optional.of(new Message(MessageType.TEXT, "Invalid arguments for song playing!".getBytes()));
        } else if (this.user == null) {
            return Optional.of(new Message(MessageType.TEXT, "You have to first log in before you can listen to songs!".getBytes()));
        }

        String songName = args[0];
        Song songToPlay = null;
        for (Song song : this.songs) {
            if (song.getSongName().equalsIgnoreCase(songName)) {
                songToPlay = song;
            }
        }

        if (songToPlay == null) {
            return Optional.of(new Message(MessageType.TEXT, "There is no song with this name!".getBytes()));
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

            System.out.println(songInfo);

            musicPlayer = new MusicPlayer(songToPlay, clientHandler.getObjectOutputStream(), format.getFrameSize());
            currentlyPlaying.put(songToPlay, currentlyPlaying.getOrDefault(songToPlay, 0) + 1);

            executor.schedule(musicPlayer, MUSIC_PLAY_DELAY, TimeUnit.SECONDS);
        } catch (IOException e) {
            Logger.logError("IOException. " + e.getMessage());
            return Optional.of(new Message(MessageType.TEXT, "Internal server error!".getBytes()));
        } catch (UnsupportedAudioFileException e) {
            Logger.logError("Unsupported format exception. " + e.getMessage());
            return Optional.of(new Message(MessageType.TEXT, "Unsupported format!".getBytes()));
        }
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject(songInfo);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Optional.of(new Message(MessageType.SONG_INFO, bos.toByteArray()));
    }

    private Optional<Message> handleStopPlaying() {
        if (this.musicPlayer != null) {
            Song song = this.musicPlayer.getSong();
            if (song != null && Objects.compare(currentlyPlaying.get(song), 0, Comparator.naturalOrder()) > 0) {
                currentlyPlaying.put(this.musicPlayer.getSong(),
                        currentlyPlaying.get(song) - 1);
            }

            this.musicPlayer.stop();
        }

        return Optional.empty();
    }

    private Optional<Message> handleTopSongs(String... args) {
        if (args == null || args.length != 1) {
            return Optional.of(new Message(MessageType.TEXT, "Invalid arguments for generating top playing songs!".getBytes()));
        } else if (this.user == null) {
            return Optional.of(new Message(MessageType.TEXT, "You have to log in before you can get information about top playing songs!".getBytes()));
        }

        List<Map.Entry<Song, Integer>> sorted = currentlyPlaying.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(10)
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder("Top playing songs are: ");
        for (Map.Entry<Song, Integer> entry : sorted) {
            sb.append(entry.getKey().getSongName()).append(": ").append(entry.getValue()).append(System.lineSeparator());
        }

        return Optional.of(new Message(MessageType.TEXT, sb.toString().getBytes()));
    }

    private Optional<Message> handleCreatePlaylist(String... args) {
        if (args == null || args.length != 1) {
            return Optional.of(new Message(MessageType.TEXT, "Invalid arguments for creating playlist!".getBytes()));
        } else if (this.user == null) {
            return Optional.of(new Message(MessageType.TEXT, "You have to log in before you can create playlists!".getBytes()));
        }

        playlists.add(new Playlist(args[0], user.getEmail())); // TODO multiple arguments can be concatenated

        IOUtil.writeToFile(Path.of(PLAYLISTS_FILE_NAME), playlists);
        return Optional.of(new Message(MessageType.TEXT, "Playlist was created successfully!".getBytes()));
    }

    private Optional<Message> handleShowPlaylist(String... args) {
        if (args == null || args.length != 1) {
            return Optional.of(new Message(MessageType.TEXT, "Invalid arguments for creating playlist!".getBytes()));
        } else if (this.user == null) {
            return Optional.of(new Message(MessageType.TEXT, "You have to log in before you can request info about playlist!".getBytes()));
        }

        int index = playlists.indexOf(new Playlist(args[0], this.user.getEmail()));
        if (index == -1) {
            return Optional.of(new Message(MessageType.TEXT, ("You don't have playlist with name " + args[0]).getBytes()));
        }

        // TODO playlist should be uniquely identified by name + email
        return Optional.of(new Message(MessageType.TEXT, playlists.get(index).toString().getBytes()));
    }

    private Optional<Message> handleAddSongTo(String... args) {
        if (args == null || args.length != 2) {
            return Optional.of(new Message(MessageType.TEXT, "Invalid arguments for adding song to playlist!".getBytes()));
        } else if (this.user == null) {
            return Optional.of(new Message(MessageType.TEXT, "You have to log in before you can request info about playlist!".getBytes()));
        }

        int index1 = playlists.indexOf(new Playlist(args[0], this.user.getEmail()));
        if (index1 == -1) {
            return Optional.of(new Message(MessageType.TEXT, ("You don't have playlist with name " + args[0]).getBytes()));
        }

        Playlist playlist = playlists.get(index1);

        boolean k = false;
        for (Song song : songs) {
            if (song.getSongName().equals(args[1])) {
                playlist.addSong(song); // TODO what if it already exists in the playlist
                k = true;
            }
        }

        return Optional.of(new Message(MessageType.TEXT, k ? "Song was added successfully".getBytes() : "There is not a song with that name!".getBytes()));
    }

    private Optional<Message> handleSearch(String... args) {
        if (args == null || args.length == 0) {
            return Optional.of(new Message(MessageType.TEXT, "Invalid arguments. Command has the following syntax 'search <words>'".getBytes()));
        }

        StringBuilder sb = new StringBuilder("Found songs: ");
        List<Song> foundSongs = new ArrayList<>();
        for (String keyword : args) {
            foundSongs.addAll(songs.stream().filter(x -> x.getSongName().toLowerCase().contains(keyword)).collect(Collectors.toList()));
        }

        for (Song song : foundSongs) {
            sb.append(song.getSongName()).append(",");
        }

        return Optional.of(new Message(MessageType.TEXT, sb.toString().getBytes()));
    }
}
