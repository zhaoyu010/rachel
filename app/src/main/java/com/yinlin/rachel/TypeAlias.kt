package com.yinlin.rachel

import com.yinlin.rachel.data.LyricsInfo
import com.yinlin.rachel.data.Weibo
import com.yinlin.rachel.data.MusicInfo
import com.yinlin.rachel.data.Playlist
import com.yinlin.rachel.data.WeiboComment
import com.yinlin.rachel.data.WeiboUser

typealias IWeiboList = MutableList<Weibo>
typealias IWeiboCommentList = MutableList<WeiboComment>

typealias WeiboUserMap = LinkedHashMap<String, WeiboUser>
typealias IWeiboUserMap = MutableMap<String, WeiboUser>

typealias ChorusList = List<Long>
typealias LyricsInfoList = List<LyricsInfo>
typealias LyricsFileMap = Map<String, List<String>>

typealias PlaylistMap = HashMap<String, Playlist>
typealias IPlaylistMap = MutableMap<String, Playlist>

typealias MusicInfoMap = HashMap<String, MusicInfo>
typealias IMusicInfoMap = MutableMap<String, MusicInfo>