package com.yinlin.rachel.fragment

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.annotation.OptIn
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ShuffleOrder.DefaultShuffleOrder
import com.xuexiang.xui.utils.XToastUtils
import com.yinlin.rachel.Config
import com.yinlin.rachel.MusicInfoMap
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.RachelMessage.MUSIC_ADD_MUSIC_INTO_PLAYLIST
import com.yinlin.rachel.RachelMessage.MUSIC_CREATE_PLAYLIST
import com.yinlin.rachel.RachelMessage.MUSIC_DELETE_MUSIC
import com.yinlin.rachel.RachelMessage.MUSIC_DELETE_MUSIC_FROM_PLAYLIST
import com.yinlin.rachel.RachelMessage.MUSIC_DELETE_PLAYLIST
import com.yinlin.rachel.RachelMessage.MUSIC_GOTO_MUSIC
import com.yinlin.rachel.RachelMessage.MUSIC_NOTIFY_ADD_MUSIC
import com.yinlin.rachel.RachelMessage.MUSIC_RENAME_PLAYLIST
import com.yinlin.rachel.RachelMessage.MUSIC_START_PLAYER
import com.yinlin.rachel.RachelMessage.MUSIC_STOP_PLAYER
import com.yinlin.rachel.RachelMessage.MUSIC_UPDATE_PLAYLIST
import com.yinlin.rachel.RachelMessage.MUSIC_USE_LYRICS_ENGINE
import com.yinlin.rachel.activity.MVActivity
import com.yinlin.rachel.bold
import com.yinlin.rachel.clear
import com.yinlin.rachel.clearAddAll
import com.yinlin.rachel.data.LyricsInfo
import com.yinlin.rachel.data.MusicInfo
import com.yinlin.rachel.data.Playlist
import com.yinlin.rachel.databinding.FragmentMusicBinding
import com.yinlin.rachel.deleteFilter
import com.yinlin.rachel.dialog.BottomDialogCurrentPlaylist
import com.yinlin.rachel.dialog.BottomDialogLyricsEngine
import com.yinlin.rachel.dialog.BottomDialogLyricsInfo
import com.yinlin.rachel.dialog.BottomDialogMusicInfo
import com.yinlin.rachel.div
import com.yinlin.rachel.gotoQQGroup
import com.yinlin.rachel.load
import com.yinlin.rachel.model.RachelDialog
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelMod
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.model.RachelRotateAnimator
import com.yinlin.rachel.model.engine.LyricsEngineFactory
import com.yinlin.rachel.pathMusic
import com.yinlin.rachel.rachelClick
import com.yinlin.rachel.readJson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class FragmentMusic(pages: RachelPages) : RachelFragment<FragmentMusicBinding>(pages), Player.Listener {
    companion object {
        const val UPDATE_FREQUENCY: Long = 100L // 更新频率
    }

    val musicInfos = MusicInfoMap() // 曲库集
    private val playlists = Config.playlist // 歌单集
    private val loadMusics = ArrayList<String>() // 加载媒体集
    private var currentPlaylist: Playlist? = null // 当前播放列表
    private var savedMusic: MusicInfo? = null // 当前记录音乐

    private lateinit var recordAnimation: RachelRotateAnimator // 唱片旋转动画
    private lateinit var onTimeUpdate: Runnable
    private val player = ExoPlayer.Builder(pages.context).build()

    private val bottomDialogLyricsEngine = BottomDialogLyricsEngine(this)
    private val bottomDialogCurrentPlaylist = BottomDialogCurrentPlaylist(this)
    private val bottomDialogMusicInfo = BottomDialogMusicInfo(this)
    private val bottomDialogLyricsInfo = BottomDialogLyricsInfo(this)

    override fun bindingClass() = FragmentMusicBinding::class.java

    override fun init() {
        // 加载曲库
        lifecycleScope.launch {
            pages.loadingDialog.show()
            withContext(Dispatchers.IO) {
                pathMusic.listFiles { file ->
                    file.isFile() && file.getName().lowercase().endsWith(RachelMod.RES_INFO)
                }?.apply {
                    for (f in this) {
                        try {
                            val info: MusicInfo = f.readJson()
                            if (info.isCorrect) {
                                info.parseLyricsText()
                                musicInfos[info.id] = info
                            }
                        }
                        catch (ignored: Exception) { }
                    }
                }
            }
            pages.loadingDialog.dismiss()
            initView()
        }
    }

    @OptIn(UnstableApi::class)
    private fun initView() {
        // 底部框
        bottomDialogLyricsEngine.init()
        bottomDialogCurrentPlaylist.init()
        bottomDialogMusicInfo.init()
        bottomDialogLyricsInfo.init()

        // 播放器
        player.addListener(this)
        player.repeatMode = Player.REPEAT_MODE_ALL
        // 更新播放进度回调
        onTimeUpdate = object : Runnable {
            override fun run() {
                val position = player.currentPosition
                v.progress.updateProgress(position, false) // 更新进度条
                v.lyrics.update(position) // 更新歌词
                postDelay(UPDATE_FREQUENCY, this) // 更新消息
            }
        }
        // 唱片
        recordAnimation = RachelRotateAnimator(v.record, 15000)
        // 曲库
        v.toolLibrary.rachelClick { pages.navigate(FragmentLibrary(pages, musicInfos, playlists)) }
        // 歌单
        v.toolPlaylist.rachelClick { pages.navigate(FragmentPlaylist(pages, musicInfos, playlists, currentPlaylist)) }
        // 歌词引擎
        v.toolLyricsEngine.rachelClick { bottomDialogLyricsEngine.update().show() }
        // 工坊
        v.toolWorkshop.rachelClick {
            RachelDialog.choice(pages.context, "跳转工坊资源QQ群",
                listOf("MOD0群", "MOD1群", "MOD2群")) { position, _ ->
                gotoQQGroup(pages.context, pages.getResString(when (position) {
                    0 -> R.string.qqgroup_mod0
                    1 -> R.string.qqgroup_mod1
                    2 -> R.string.qqgroup_mod2
                    else -> R.string.qqgroup_main
                }))
            }
        }
        // 样式
        v.title.bold = true
        // 进度条
        v.progress.setOnProgressChangedListener { percent ->
            // 计算按下位置占总进度条的百分比, 来同比例到时长上
            player.seekTo((player.duration * percent).toLong())
            if (!player.isPlaying) player.play()
        }
        // 模式
        val modeOrder = "order"
        val modeLoop = "loop"
        val modeRandom = "random"
        v.buttonMode.tag = modeOrder
        v.buttonMode.rachelClick(300) { view: View ->
            when (view.tag as String) {
                modeOrder -> { // 顺序播放
                    view.tag = modeLoop
                    v.buttonMode.load(pages.ril, R.drawable.svg_music_loop)
                    player.repeatMode = Player.REPEAT_MODE_ONE
                    player.shuffleModeEnabled = false
                }
                modeLoop -> { // 单曲循环
                    view.tag = modeRandom
                    v.buttonMode.load(pages.ril, R.drawable.svg_music_random)
                    player.repeatMode = Player.REPEAT_MODE_ALL
                    player.shuffleModeEnabled = true
                    // 更新随机池
                    if (player.shuffleModeEnabled) {
                        player.setShuffleOrder(DefaultShuffleOrder(player.mediaItemCount, System.currentTimeMillis()))
                    }
                }
                modeRandom -> { // 随机播放
                    view.tag = modeOrder
                    v.buttonMode.load(pages.ril, R.drawable.svg_music_order)
                    player.repeatMode = Player.REPEAT_MODE_ALL
                    player.shuffleModeEnabled = false
                }
            }
        }
        // 上一首
        v.buttonPrevious.rachelClick(300) {
            if (isLoadMusic) player.seekToPreviousMediaItem()
        }
        // 下一首
        v.buttonNext.rachelClick(300) {
            if (isLoadMusic) player.seekToNextMediaItem()
        }
        // 播放
        v.buttonPlay.rachelClick(300) {
            if (isLoadMusic) {
                if (player.isPlaying) player.pause()
                else player.play()
            }
        }
        // 播放列表
        v.buttonPlaylist.rachelClick {
            if (isLoadMusic && currentPlaylist != null) {
                bottomDialogCurrentPlaylist.update(currentPlaylist!!, currentMusic!!).show()
            }
        }
        // AN
        v.buttonAn.rachelClick(500) {
            currentMusic?.apply {
                if (this.bgd) {
                    val isBgd = v.bg.tag as Boolean
                    v.bg.load(pages.ril, if (isBgd) this.bgsPath else this.bgdPath)
                    v.bg.tag = !isBgd
                }
                else XToastUtils.warning("此歌曲不支持壁纸动画")
            }
        }
        // MV
        v.buttonMv.rachelClick {
            currentMusic?.apply {
                if (this.video) {
                    player.pause()
                    val intent = Intent(pages.context, MVActivity::class.java)
                    intent.putExtra("uri", this.videoPath.absolutePath)
                    startActivity(intent)
                }
                else XToastUtils.warning("此歌曲不支持MV视频")
            }
        }
        // 歌词
        v.buttonLyrics.rachelClick {
            currentMusic?.lyrics?.apply {
                val arr = ArrayList<LyricsInfo>()
                for ((engineName, nameList) in this) {
                    val available = LyricsEngineFactory.hasEngine(engineName)
                    for (name in nameList) arr += LyricsInfo(engineName, name, available)
                }
                bottomDialogLyricsInfo.update(arr).show()
            }
        }
        // 评论
        v.buttonComment.rachelClick {

        }
        // 分享
        v.buttonShare.rachelClick {

        }
        // 信息
        v.buttonInfo.rachelClick {
            currentMusic?.apply { bottomDialogMusicInfo.update(this).show() }
        }
    }

    override fun quit() {
        bottomDialogLyricsEngine.release()
        bottomDialogLyricsInfo.release()
        bottomDialogCurrentPlaylist.release()
        bottomDialogMusicInfo.release()
        endTimeUpdate()
        player.removeListener(this)
        player.release()
    }

    override fun update() {
        // 进入前台时需要更新
        updateForeground()
        // 进入前台如果播放器是播放状态则启动更新回调
        if (player.isPlaying) startTimeUpdate()
    }

    override fun hidden() {
        // 离开前台如果播放器是播放状态则停止更新回调
        if (player.isPlaying) endTimeUpdate()
    }

    override fun message(msg: RachelMessage, vararg args: Any?) {
        when (msg) {
            MUSIC_START_PLAYER -> startPlayer(args[0] as Playlist)
            MUSIC_STOP_PLAYER -> stopPlayer()
            MUSIC_DELETE_PLAYLIST -> {
                val playlist = args[0] as Playlist
                // 检查歌单是否正在播放
                if (isLoadPlaylist(playlist)) stopPlayer()
                // UI更新
                playlists.remove(playlist.name)
                // 数据存储
                Config.playlist = playlists
            }
            MUSIC_UPDATE_PLAYLIST -> playlists.clearAddAll(Config.playlist)
            MUSIC_DELETE_MUSIC_FROM_PLAYLIST -> {
                val playlist = args[0] as Playlist
                val position = args[1] as Int
                // 检查歌单是否正在播放
                if (isLoadPlaylist(playlist)) {
                    val mediaIndex = loadMusics.indexOf(playlist.items[position])
                    // 如果是单曲循环, 则直接结束播放
                    if (player.repeatMode == Player.REPEAT_MODE_ONE) stopPlayer()
                    else {
                        loadMusics.removeAt(mediaIndex)
                        player.removeMediaItem(mediaIndex)
                    }
                }
                // UI更新
                playlist.items.removeAt(position)
                // 数据存储
                Config.playlist = playlists
            }
            MUSIC_DELETE_MUSIC -> {
                stopPlayer() // 删除音乐前停止播放器
                for (id in args[0] as List<*>) {
                    // 更新UI
                    musicInfos.remove(id as String)
                    // 数据存储
                    pathMusic.deleteFilter(id)
                }
            }
            MUSIC_GOTO_MUSIC -> {
                val index = loadMusics.indexOf(args[0] as String)
                if (player.currentMediaItemIndex != index) {
                    player.seekTo(index, 0)
                    if (!player.isPlaying) player.play()
                }
            }
            MUSIC_NOTIFY_ADD_MUSIC -> {
                for (id in args[0] as List<*>) {
                    // 更新UI
                    val info: MusicInfo = (pathMusic / (id as String + RachelMod.RES_INFO)).readJson()
                    if (info.isCorrect) {
                        info.parseLyricsText()
                        musicInfos[info.id] = info
                    }
                    else musicInfos.remove(id)
                }
            }
            MUSIC_USE_LYRICS_ENGINE -> {
                val engineName = args[0] as String
                val name = args[1] as String
                currentMusic?.apply {
                    if (!v.lyrics.switchEngine(this, engineName, name)) XToastUtils.error("加载歌词引擎失败")
                }
            }
            else -> {}
        }
    }

    override fun messageForResult(msg: RachelMessage, vararg args: Any?): Any? {
        when (msg) {
            MUSIC_CREATE_PLAYLIST -> {
                val newName = args[0] as String
                // 校验歌单
                if (newName.isEmpty() || playlists.containsKey(newName) ||
                    newName == pages.getResString(R.string.default_playlist_name)) return false
                // UI更新
                playlists[newName] = Playlist(newName, ArrayList())
                // 数据存储
                Config.playlist = playlists
                return true
            }
            MUSIC_RENAME_PLAYLIST -> {
                val playlist = args[0] as Playlist
                val newName = args[1] as String
                // 校验歌单
                if (newName.isEmpty() || playlists.containsKey(newName) ||
                    newName == pages.getResString(R.string.default_playlist_name)) return false
                // UI更新
                playlists.remove(playlist.name)
                playlists[newName] = Playlist(newName, playlist.items)
                // 数据存储
                Config.playlist = playlists
                return true
            }
            MUSIC_ADD_MUSIC_INTO_PLAYLIST -> {
                val playlist = args[0] as Playlist
                // 更新UI
                val ids = playlist.items
                var num = 0
                val isPlaying = isLoadPlaylist(playlist)
                for (id in args[1] as List<*>) {
                    if (!ids.contains(id as String)) { // 重复过滤
                        ++num
                        ids.add(id)
                        // 检查当前是否在播放此列表
                        if (isPlaying) {
                            val uri = Uri.fromFile(musicInfos[id]!!.audioPath)
                            player.addMediaItem(MediaItem.Builder().setUri(uri).setMediaId(id).build())
                            loadMusics.add(id)
                        }
                    }
                }
                // 数据存储
                if (num > 0) Config.playlist = playlists
                return num
            }
            else -> return null
        }
    }

    // 媒体切换
    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        if (mediaItem == null) return

        // 加载歌词引擎
        musicInfos[mediaItem.mediaId]?.apply {
            if(!v.lyrics.loadEngine(this)) XToastUtils.error("加载歌词引擎失败")
        }

        // 处于前台时更新前台信息
        if (isForeground) updateForeground()
    }

    // 媒体状态改变 (指播放器播放或停止的状态)
    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_READY) {
            if (isForeground) updateForeground() // 处于前台时更新前台信息
        } else if (playbackState == Player.STATE_ENDED) {
            if (isForeground) updateForeground() // 处于前台时更新前台信息
            // 置空当前播放歌单与歌曲
            currentPlaylist = null
            savedMusic = null
            // 停止歌词引擎
            v.lyrics.releaseEngine()
        }
    }

    // 播放状态改变 (指播放器进入播放或暂停的状态)
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        if (isPlaying) {
            v.buttonPlay.load(pages.ril, R.drawable.svg_music_play)
            if (isForeground) startTimeUpdate()
        } else {
            v.buttonPlay.load(pages.ril, R.drawable.svg_music_pause)
            if (isForeground) endTimeUpdate()
        }
    }

    // 是否在前台
    private val isForeground get() = pages.isForeground(RachelPages.music)
    // 取当前播放音乐
    private val currentMusic: MusicInfo? get() = player.currentMediaItem?.let { musicInfos[it.mediaId] }
    // 是否加载音乐 (即player加载了music的状态, music处于播放或暂停)
    private val isLoadMusic get() = player.playbackState == Player.STATE_READY && currentMusic != null
    // 是否在播放某个歌单
    private fun isLoadPlaylist(playlist: Playlist) = isLoadMusic && currentPlaylist == playlist

    // 开始前台更新 (播放器启动时间刻更新)
    private fun startTimeUpdate() {
        post(onTimeUpdate)
        recordAnimation.start()
    }

    // 停止前台更新 (播放器停止时间刻更新)
    private fun endTimeUpdate() {
        removePost(onTimeUpdate)
        recordAnimation.pause()
    }

    // 播放歌单
    @OptIn(UnstableApi::class)
    private fun startPlayer(playlist: Playlist) {
        loadMusics.clear() // 清空已装载音乐信息
        val mediaItems = ArrayList<MediaItem>()
        for (id in playlist.items) {
            musicInfos[id]?.apply {
                loadMusics += id
                mediaItems += MediaItem.Builder().setUri(Uri.fromFile(this.audioPath)).setMediaId(id).build()
            }
        }
        currentPlaylist = playlist // 设置当前播放歌单

        // 更新随机池
        if (player.shuffleModeEnabled) {
            player.setShuffleOrder(DefaultShuffleOrder(player.mediaItemCount, System.currentTimeMillis()))
        }

        // 启动 Player
        player.setMediaItems(mediaItems)
        player.prepare()
        player.play()
    }

    // 停止播放
    private fun stopPlayer() {
        if (isLoadMusic) {
            // 清理已装载音乐信息
            player.clearMediaItems()
            loadMusics.clear()
        }
    }

    // 更新前台
    private fun updateForeground() {
        // 更新背景
        val info = currentMusic
        v.progress.setInfo(info?.chorus ?: emptyList(), if (player.duration == C.TIME_UNSET) 0 else player.duration)
        if (info == null) { // 停止播放状态, 更新
            // 更新歌曲信息
            v.title.text = pages.getResString(R.string.no_audio_source)
            v.singer.text = ""
            v.record.clear(pages.ril)
            v.bg.tag = false
            v.bg.clear(pages.ril)
            v.buttonAn.load(pages.ril, R.drawable.svg_an_off)
            v.buttonMv.load(pages.ril, R.drawable.svg_mv_off)
            // 更新已播放进度与进度条
            v.progress.updateProgress(0L, true)
        }
        else {
            v.progress.updateProgress(player.currentPosition, true)
            if (info != savedMusic) { // 只有与之前音乐不同才更新
                savedMusic = info
                // 更新歌曲信息
                v.title.text = info.name
                v.singer.text = info.singer
                v.record.load(pages.ril, info.recordPath)
                v.bg.tag = info.bgd
                v.bg.load(pages.ril, if (info.bgd) info.bgdPath else info.bgsPath)
                v.buttonAn.load(pages.ril, if (info.bgd) R.drawable.svg_an_on else R.drawable.svg_an_off)
                v.buttonMv.load(pages.ril, if (info.video) R.drawable.svg_mv_on else R.drawable.svg_mv_off)
                // 已播放进度和进度条由onTimeUpdate更新, 不用在此更新
            }
        }
    }
}