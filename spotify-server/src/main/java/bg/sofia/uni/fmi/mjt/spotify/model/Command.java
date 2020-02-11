package bg.sofia.uni.fmi.mjt.spotify.model;

/**
 * Contains all commands to which the server can respond.
 */
public enum Command {
    REGISTER,
    LOGIN,
    DISCONNECT,
    SEARCH,
    TOP,
    CREATE_PLAYLIST,
    ADD_SONG_TO,
    SHOW_PLAYLIST,
    PLAY,
    STOP,
    SONG_FINISHED
}
