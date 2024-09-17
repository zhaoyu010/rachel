package com.yinlin.rachel

import com.yinlin.rachel.data.MsgInfo
import com.yinlin.rachel.data.MusicInfo
import com.yinlin.rachel.data.Playlist
import com.yinlin.rachel.data.WeiboUser

typealias IMsgInfoList = MutableList<MsgInfo>

typealias WeiboUserMap = LinkedHashMap<String, WeiboUser>
typealias IWeiboUserMap = MutableMap<String, WeiboUser>

typealias PlaylistMap = HashMap<String, Playlist>
typealias IPlaylistMap = MutableMap<String, Playlist>

typealias MusicInfoMap = HashMap<String, MusicInfo>
typealias IMusicInfoMap = MutableMap<String, MusicInfo>