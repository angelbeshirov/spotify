package bg.sofia.uni.fmi.mjt.spotify.server;

import com.google.gson.Gson;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author angel.beshirov
 * TODO in properties
 */
public class CommandHandler {
    private static final String USERS_FILE_NAME = "src\\main\\resources\\users.bin";
    private User user;
    private Map<User, ClientHandler> loggedInUsers;
    private final List<User> registeredUsers;
    private final ClientHandler clientHandler;
    private final Gson gson;

    private MusicPlayer musicPlayer;

    public CommandHandler(ClientHandler clientHandler, Map<User, ClientHandler> loggedInUsers, List<User> registeredUsers) {
        this.gson = new Gson(); // TODO may be from constructor? thread safe?
        this.clientHandler = clientHandler;
        this.loggedInUsers = loggedInUsers;
        this.registeredUsers = registeredUsers;
        this.musicPlayer = null; // TODO somehow improve with play method
    }

    public Optional<Object> handleCommand(Command command, String... args) {
        switch (command) {
            // TODO return vs break is switch statement
            case LOGIN:
                if (args == null || args.length != 2) {
                    return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Invalid arguments for login!")));
                } else if (this.user != null) {
                    return Optional.of(gson.toJson(new Message(MessageType.TEXT, "You are already logged in!")));
                }

                return handleLogin(args[0], args[1]);
            case REGISTER:
                if (args == null || args.length != 2) {
                    return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Invalid arguments count for register!")));
                } else if (this.user != null) {
                    return Optional.of(gson.toJson(new Message(MessageType.TEXT, "You are already logged in!")));
                }

                return handleRegister(args[0], args[1]);
            case DISCONNECT:
                if (user != null) {
                    loggedInUsers.remove(user);
                }

                return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Successfully disconnected!")));
            case PLAY:
                if (args == null || args.length != 1) {
                    return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Invalid arguments for song playing!")));
                } else if (this.user == null) {
                    return Optional.of(gson.toJson(new Message(MessageType.TEXT, "You have to first log in before you can listen to songs!")));
                }

                return handleSongPlaying(args[0]);

            case STOP:
                if (this.musicPlayer != null) {
                    this.musicPlayer.stop();
                }
            case TOP:
        }

        return Optional.empty();
    }

    private Optional<Object> handleRegister(String email, String password) {
        if (email == null || password == null) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Email or password is invalid!"))); // TODO possibly improve this error handling
        }

        User user = new User(email, password);
        if (registeredUsers.contains(user)) {
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "User with this email address already exists!")));
        }

        registeredUsers.add(user);
        IOWorker.writeUsersToFile(Path.of(USERS_FILE_NAME), user);

        return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Registration was successful")));
    }

    private Optional<Object> handleLogin(String email, String password) {
        User user = new User(email, password);
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

    private Optional<Object> handleSongPlaying(String songName) {
        File song = new File("src\\main\\resources\\" + songName);
        SongInfo songInfo;
        AudioFormat format;
        try (AudioInputStream stream = AudioSystem.getAudioInputStream(song)) {
            format = stream.getFormat();
            songInfo = new SongInfo();
            songInfo.setBigEndian(format.isBigEndian());
            songInfo.setChannels(format.getChannels());
            songInfo.setEncoding(format.getEncoding().toString());
            songInfo.setFrameRate(format.getFrameRate());
            songInfo.setFrameSize(format.getFrameSize());
            songInfo.setSampleRate(format.getSampleRate());
            songInfo.setSampleSizeInBits(format.getSampleSizeInBits());

            this.musicPlayer = new MusicPlayer(song, this.clientHandler.getSocket().getOutputStream());
            new Thread(this.musicPlayer).start();
        } catch (IOException e) {
            Logger.logError("IOException. " + e.getMessage());
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "IOException")));
        } catch (UnsupportedAudioFileException e) {
            Logger.logError("Unsupported format exception. " + e.getMessage());
            return Optional.of(gson.toJson(new Message(MessageType.TEXT, "Unsupported format!")));
        }
        String value = gson.toJson(songInfo, SongInfo.class);

        return Optional.of(gson.toJson(new Message(MessageType.JSON, value)));
    }
}
