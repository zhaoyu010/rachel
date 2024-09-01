package com.yinlin.rachel

enum class RachelMessage {
    // FragmentMusic
    MUSIC_START_PLAYER,  // Playlist
    MUSIC_STOP_PLAYER,  // void
    MUSIC_CREATE_PLAYLIST,  // String -> boolean
    MUSIC_RENAME_PLAYLIST,  // Playlist, String -> boolean
    MUSIC_DELETE_PLAYLIST,  // Playlist
    MUSIC_DELETE_MUSIC_FROM_PLAYLIST,  // Playlist, int
    MUSIC_ADD_MUSIC_INTO_PLAYLIST,  // Playlist, List<String> -> int
    MUSIC_DELETE_MUSIC,  // List<String>
    MUSIC_GOTO_MUSIC,  // String
    MUSIC_NOTIFY_ADD_MUSIC,  // List<String>

    MUSIC_USE_LYRICS_ENGINE,  // String


    // FragmentMe
    ME_UPDATE_USER_INFO, // void
}