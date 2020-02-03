package bg.sofia.uni.fmi.mjt.spotify.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * @author angel.beshirov
 */
public class ClientHandler implements Runnable {
    private final Socket socket;
    private final CommandHandler commandHandler;

    private final Map<User, ClientHandler> clients;
    private final User user;
    private boolean isLoggedIn;

    private volatile boolean isRunning;

    public ClientHandler(final Socket socket, final Map<User, ClientHandler> clients) {
        this.socket = socket;
        this.clients = clients;
        this.user = new User();
        this.commandHandler = new CommandHandler(clients);
        this.isRunning = true;
        this.isLoggedIn = false;
        this.clients.put(user, this);
    }

    public Socket getSocket() {
        return socket;
    }

    public void logIn() {
        this.isLoggedIn = true;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    @Override
    public void run() {
        try (final PrintWriter printWriter = new PrintWriter(socket.getOutputStream(), true);
             final BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
            String command = reader.readLine();
            while (isRunning && command != null) {
                System.out.println("Command received from client: " + command);
                String[] data = command.split(" ");
                // TODO here user should be retrieved
                Optional<String> result = commandHandler.handleCommand(null, Command.valueOf(data[0]), Arrays.copyOfRange(data, 1, data.length));
                result.ifPresent(printWriter::println);
                command = reader.readLine();
            }
        } catch (final IOException e) {
            System.out.println("Error with the socket.");
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                System.out.println("Error while closing client socket!");
            }
        }
    }

//    private Optional<String> executeCommand(final String command, final String[] arguments) {
//        switch (command) {
//            case COMMAND_NICK:
//                if (arguments.length == 1) {
//                    setNick(arguments[0]);
//                }
//                break;
//            case COMMAND_SEND:
//                if (arguments.length >= 2 && this.nick != null) {
//                    final StringBuilder payload = new StringBuilder();
//                    for (int i = 1; i < arguments.length; i++) {
//                        payload.append(arguments[i]).append(SPACE);
//                    }
//                    sendToClient(this, arguments[0], payload.toString());
//                }
//                break;
//            case COMMAND_SEND_ALL:
//                if (arguments.length >= 1 && this.nick != null) {
//                    final StringBuilder payload = new StringBuilder();
//                    for (final String argument : arguments) {
//                        payload.append(argument).append(SPACE);
//                    }
//                    sendToAll(nick, payload.toString());
//                }
//                break;
//            case COMMAND_LIST_USERS:
//                final StringBuilder sb = new StringBuilder();
//                sb.append(ACTIVE_USERS);
//                for (final User client : clients.keySet()) {
//                    sb.append(client).append(SPACE);
//                }
//                return Optional.of(sb.toString());
//            case COMMAND_DISCONNECT:
//                if (this.nick != null) {
//                    System.out.println(this.nick + " has disconnected!");
//                    clients.remove(this.nick);
//                }
//
//                isRunning = false;
//                break;
//        }
//
//        return Optional.empty();
//    }
//
//    private void sendToClient(final ClientHandler from, final String to, final String message) {
//        final ClientHandler clientHandler = clients.get(to);
//        if (clientHandler != null) {
//            send(from, clientHandler, message);
//        }
//    }
//
//    private void sendToAll(final String from, final String message) {
//        for (final ClientHandler clientHandler : clients.values()) {
//            final String nick = clientHandler.getNick();
//            if (nick != null && !Objects.equals(nick, from)) {
//                sendToClient(this, nick, message);
//            }
//        }
//    }
//
//    private void send(final ClientHandler from, final ClientHandler to, final String message) {
//        final Socket socket = to.getSocket();
//        try {
//            final PrintWriter pr = new PrintWriter(socket.getOutputStream(), true);
//            final String nick = from.getNick() != null ? from.getNick() : UNKNOWN;
//            pr.println(nick + SAID + message);
//        } catch (final IOException e) {
//            System.out.println("Error while sending message to client");
//        }
//    }
}
