# Spotify

### Description
Spotify is a platform for streaming music, which gives users access to millions of songs around the world. This is an application similar to Spotify which has two parts -
server and a client. The music is being sent from the server to the client on fixed chunks of bytes over some period of time. Both the server and the client are multi-threaded
which allows constant two-way communication between them.

### Functionality
The application supports the following functions:
- Registering using email and password
- Login in the platform
- Saving songs which are available for listening by the users
- Searching of songs
- Creating statistics of most listened songs
- Creating playlists which are stored in files
- Adding songs to playlists
- Returning information about playlists
- Streaming songs

### Spotify Client

'Spotify client' has command line interface with the following commands:

```bash
register <email> <password>
login <email> <password>
disconnect
search <words> - returns all songs which contain any of the words
top <number> - returns a list of top number most listened songs at the moment
create-playlist <name_of_the_playlist>
add-song-to <name_of_the_playlist> <song>
show-playlist <name_of_the_playlist>
play <song>
stop
```

### Notes
For streaming songs I use the 'javax.sound.sampled' API which allows songs only in '.wav' format. (for more information [click here](https://docs.oracle.com/javase/tutorial/sound/playing.html))
