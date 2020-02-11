package bg.sofia.uni.fmi.mjt.spotify.server.client;

import bg.sofia.uni.fmi.mjt.spotify.model.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * @author angel.beshirov
 */
public class CommandHandlerTest {
    private static final String EMAIL = "ivan@abv.bg";
    private static final String PASSWORD = "pass123";
    private static final double DELTA = 0.001;
    private static final String TOP_N = "3";
    private static final int TOP_N_SONGS = 3;
    private static final int TIMES = 4;

    private ServerData serverData;
    private CommandHandler commandHandler;

    private User user;

    @Before
    public void setUp() {
        serverData = mock(ServerData.class);
        ClientHandler clientHandler = mock(ClientHandler.class);
        commandHandler = new CommandHandler(clientHandler, serverData);

        user = new User(EMAIL, PASSWORD);
    }

    @Test
    public void testLoginValidUser() {
        when(serverData.getUser(eq(user))).thenReturn(user);

        Optional<Message> msg =
                commandHandler.handleCommand(Command.LOGIN, EMAIL, PASSWORD);

        Assert.assertTrue(msg.isPresent());
        Assert.assertEquals(MessageType.TEXT, msg.get().getMessageType());
        String expected = "Successfully logged in!";
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testLoginWrongPassword() {
        when(serverData.getUser(eq(user))).thenReturn(new User(EMAIL, "wrongpass"));

        Optional<Message> msg =
                commandHandler.handleCommand(Command.LOGIN, EMAIL, PASSWORD);

        Assert.assertTrue(msg.isPresent());
        Assert.assertEquals(MessageType.TEXT, msg.get().getMessageType());
        String expected = "Wrong password";
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testInvalidArgumentsLogin() {
        when(serverData.getUser(eq(user))).thenReturn(user);

        Optional<Message> msg =
                commandHandler.handleCommand(Command.LOGIN, EMAIL);

        Assert.assertTrue(msg.isPresent());
        String expected = "Invalid arguments for login!";
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testRegister() {
        Optional<Message> msg =
                commandHandler.handleCommand(Command.REGISTER, EMAIL, PASSWORD);

        Assert.assertTrue(msg.isPresent());
        String expected = "Registration was successful";
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testRegisterExistingUser() {
        when(serverData.getUser(eq(user))).thenReturn(user);
        Optional<Message> msg =
                commandHandler.handleCommand(Command.REGISTER, EMAIL, PASSWORD);

        Assert.assertTrue(msg.isPresent());
        String expected = "User with this email address already exists!";
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testInvalidArgumentsRegister() {
        Optional<Message> msg =
                commandHandler.handleCommand(Command.REGISTER, EMAIL);

        Assert.assertTrue(msg.isPresent());
        String expected = "Invalid arguments count for register!";
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testDisconnect() {
        login();

        commandHandler.handleCommand(Command.DISCONNECT);
        Mockito.verify(serverData, times(1)).logOut(eq(user));
    }

    @Test
    public void testInvalidArgumentsSongPlaying() {
        Optional<Message> msg =
                commandHandler.handleCommand(Command.PLAY);

        Assert.assertTrue(msg.isPresent());
        String expected = "Invalid arguments for song playing!";
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testSongPlaying() {
        login();

        when(serverData.getSongByName("test.wav"))
                .thenReturn(new Song("test.wav",
                        new File("src\\test\\resources\\songs\\test.wav")));

        Optional<Message> msg =
                commandHandler.handleCommand(Command.PLAY, "test.wav");

        SongInfo expected = new SongInfo();
        expected.setBigEndian(false);
        expected.setChannels(2);
        expected.setFrameRate(8000);
        expected.setEncoding("PCM_SIGNED");
        expected.setFrameSize(4);
        expected.setSampleRate(8000);
        expected.setSampleSizeInBits(16);

        Assert.assertTrue(msg.isPresent());
        Assert.assertEquals(MessageType.SONG_INFO, msg.get().getMessageType());

        SongInfo actual = (SongInfo) deserialize(msg.get().getValue());

        Assert.assertEquals(expected.getChannels(), actual.getChannels());
        Assert.assertEquals(expected.isBigEndian(), actual.isBigEndian());
        Assert.assertEquals(expected.getEncoding(), actual.getEncoding());
        Assert.assertEquals(expected.getFrameSize(), actual.getFrameSize());
        Assert.assertEquals(expected.getFrameRate(), actual.getFrameRate(), DELTA);
        Assert.assertEquals(expected.getSampleSizeInBits(), actual.getSampleSizeInBits());
        Assert.assertEquals(expected.getSampleRate(), actual.getSampleRate(), DELTA);
    }

    @Test
    public void testPlayMissingSong() {
        login();

        Optional<Message> msg =
                commandHandler.handleCommand(Command.PLAY, "test.wav");

        Assert.assertTrue(msg.isPresent());
        String expected = "There is no song with this name!";
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testTopNSongs() {
        login();
        Song topSong = new Song("song name", new File("src\\test"));

        Map<Song, Integer> topSongs = new HashMap<>();
        topSongs.put(topSong, TIMES);

        when(serverData.getTopNCurrentlyPlayingSorted(TOP_N_SONGS))
                .thenReturn(new HashSet<>(topSongs.entrySet()));

        Optional<Message> msg =
                commandHandler.handleCommand(Command.TOP, TOP_N);

        Assert.assertTrue(msg.isPresent());
        Assert.assertEquals(MessageType.TEXT, msg.get().getMessageType());
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        String expected = "Top playing songs are: " + topSong.getSongName() + ": " +
                TIMES + System.lineSeparator();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testInvalidArgumentsTopSongs() {
        login();

        Optional<Message> msg =
                commandHandler.handleCommand(Command.TOP);

        Assert.assertTrue(msg.isPresent());
        Assert.assertEquals(MessageType.TEXT, msg.get().getMessageType());
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        String expected = "Invalid arguments for generating top songs!";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testCreatePlaylist() {
        login();
        Optional<Message> msg =
                commandHandler.handleCommand(Command.CREATE_PLAYLIST, "new-playlist");

        Assert.assertTrue(msg.isPresent());
        Assert.assertEquals(MessageType.TEXT, msg.get().getMessageType());
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        String expected = "Playlist was created successfully!";
        Assert.assertEquals(expected, actual);

        Mockito.verify(serverData, times(1))
                .addPlaylist(eq(new Playlist("new-playlist", EMAIL)));
    }

    @Test
    public void testCreateExistingPlaylist() {
        login();
        Playlist playlist = new Playlist("new-playlist", EMAIL);


        when(serverData.getPlaylist(playlist)).thenReturn(playlist);

        Optional<Message> msg =
                commandHandler.handleCommand(Command.CREATE_PLAYLIST, "new-playlist");

        Assert.assertTrue(msg.isPresent());
        Assert.assertEquals(MessageType.TEXT, msg.get().getMessageType());
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        String expected = "Playlist with this name already exists!";
        Assert.assertEquals(expected, actual);

        Mockito.verify(serverData, never()).addPlaylist(any());
    }

    @Test
    public void testShowPlaylist() {
        login();
        Playlist playlist = new Playlist("new-playlist", EMAIL);


        when(serverData.getPlaylist(playlist)).thenReturn(playlist);

        Optional<Message> msg =
                commandHandler.handleCommand(Command.SHOW_PLAYLIST, "new-playlist");

        Assert.assertTrue(msg.isPresent());
        Assert.assertEquals(MessageType.TEXT, msg.get().getMessageType());
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        String expected = playlist.toString();
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testShowPlaylistNotLoggedIn() {
        Playlist playlist = new Playlist("new-playlist", EMAIL);

        when(serverData.getPlaylist(playlist)).thenReturn(playlist);

        Optional<Message> msg =
                commandHandler.handleCommand(Command.SHOW_PLAYLIST, "new-playlist");

        Assert.assertTrue(msg.isPresent());
        Assert.assertEquals(MessageType.TEXT, msg.get().getMessageType());
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        String expected = "You have to log in before you can request info about playlist!";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAddSongToPlaylistNotLoggedIn() {
        Playlist playlist = new Playlist("new-playlist", EMAIL);

        when(serverData.getPlaylist(playlist)).thenReturn(playlist);

        Optional<Message> msg =
                commandHandler.handleCommand(Command.ADD_SONG_TO, "new-playlist", "test-song");

        Assert.assertTrue(msg.isPresent());
        Assert.assertEquals(MessageType.TEXT, msg.get().getMessageType());
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        String expected = "You have to log in before you can add song to playlist!";
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testAddSongToPlaylist() {
        login();
        Playlist playlist = new Playlist("new-playlist", EMAIL);
        Song song = new Song("test-song", new File("src\\test"));

        when(serverData.getPlaylist(playlist)).thenReturn(playlist);
        when(serverData.getSongByName("test-song")).thenReturn(song);

        Optional<Message> msg =
                commandHandler.handleCommand(Command.ADD_SONG_TO, "new-playlist", "test-song");

        Assert.assertTrue(msg.isPresent());
        Assert.assertEquals(MessageType.TEXT, msg.get().getMessageType());
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        String expected = "Song was added successfully";
        Assert.assertEquals(expected, actual);

        Mockito.verify(serverData, times(1)).savePlaylists();
    }

    @Test
    public void testSearch() {
        login();
        Song song1 = new Song("test 1 2 3", new File("src\\main"));
        Song song2 = new Song("not 1", new File("src\\main"));


        when(serverData.getSongs()).thenReturn(Set.of(song1, song2));
        Optional<Message> msg =
                commandHandler.handleCommand(Command.SEARCH, "test");

        Assert.assertTrue(msg.isPresent());
        Assert.assertEquals(MessageType.TEXT, msg.get().getMessageType());
        String actual = new String(msg.get().getValue(), Charset.defaultCharset());
        String expected = "Found songs: " + song1.getSongName() + ",";
        Assert.assertEquals(expected, actual);


    }

    private void login() {
        when(serverData.getUser(eq(user))).thenReturn(user);

        Optional<Message> msg =
                commandHandler.handleCommand(Command.LOGIN, EMAIL, PASSWORD);

        Assert.assertTrue(msg.isPresent());
        Assert.assertEquals(MessageType.TEXT, msg.get().getMessageType());

        Assert.assertArrayEquals("Successfully logged in!".getBytes(), msg.get().getValue());
    }

    private static Object deserialize(byte[] data) {
        Object result = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(data);
             ObjectInput in = new ObjectInputStream(bis)) {
            result = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Assert.fail();
        }

        return result;
    }
}
