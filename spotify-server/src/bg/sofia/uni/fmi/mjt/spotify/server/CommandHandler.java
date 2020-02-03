package bg.sofia.uni.fmi.mjt.spotify.server;

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
    private List<User> users;
    private Map<User, ClientHandler> loggedInUsers;

    public CommandHandler(Map<User, ClientHandler> loggedInUsers) {
        // TODO what if file doesnt exist?
        this.users = IOWorker.readStudentsFromFile(Path.of(USERS_FILE_NAME));
        this.loggedInUsers = loggedInUsers;
    }

    public Optional<String> handleCommand(User user, Command command, String... args) {
        switch (command) {
            case LOGIN:
                if (args == null || args.length != 2) {
                    return Optional.of("Invalid arguments for login!");
                }
                return handleLogin(new User(args[0], args[1]));
            case REGISTER:
                if (args == null || args.length != 2) {
                    return Optional.of("Invalid arguments count for register!");
                }
                return handleRegister(new User(args[0], args[1]));
            case DISCONNECT:
                loggedInUsers.remove(user);
                // TODO what if it is not yet logged?
            case PLAY:
                // TODO
        }

        return Optional.empty();
    }

    private Optional<String> handleRegister(User user) {
        if (users.contains(user)) {
            return Optional.of("User with this email already exists");
        }

        users.add(user);
        IOWorker.writeUsersToFile(Path.of(USERS_FILE_NAME), user);

        return Optional.of("Registration was successful");
    }

    private Optional<String> handleLogin(User user) {
        int index = users.indexOf(user);

        if (index == -1) {
            return Optional.of("User with this email does not exist!");
        }

        User registeredUser = users.get(index);

        if (!registeredUser.getPassword().equals(user.getPassword())) {
            return Optional.of("Wrong password");
        }

        return Optional.of("Successfully logged in!");
    }
}
