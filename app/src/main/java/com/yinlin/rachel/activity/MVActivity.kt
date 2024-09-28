package com.yinlin.rachel.activity

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.shuyu.gsyvideoplayer.GSYVideoManager
import com.shuyu.gsyvideoplayer.cache.CacheFactory
import com.shuyu.gsyvideoplayer.cache.ProxyCacheManager
import com.shuyu.gsyvideoplayer.player.IjkPlayerManager
import com.shuyu.gsyvideoplayer.player.PlayerFactory
import com.shuyu.gsyvideoplayer.utils.GSYVideoType
import com.shuyu.gsyvideoplayer.video.StandardGSYVideoPlayer
import com.yinlin.rachel.R

class MVActivity : AppCompatActivity() {
    private lateinit var player: StandardGSYVideoPlayer

    companion object {
        init {
            PlayerFactory.setPlayManager(IjkPlayerManager::class.java)
            CacheFactory.setCacheManager(ProxyCacheManager::class.java)
            GSYVideoType.setShowType(GSYVideoType.SCREEN_TYPE_FULL)
            GSYVideoType.setRenderType(GSYVideoType.GLSURFACE)
            GSYVideoType.enableMediaCodec()
            GSYVideoType.enableMediaCodecTexture()
            GSYVideoType.isMediaCodec()
            GSYVideoType.isMediaCodecTexture()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mv)

        val uri = intent.getStringExtra("uri")
        player = findViewById(R.id.player)
        player.apply {
            isNeedOrientationUtils = false
            backButton.visibility = View.GONE
            titleTextView.visibility = View.GONE
            fullscreenButton.visibility = View.GONE
            isRotateWithSystem = false
            isAutoFullWithSize = false
            isShowFullAnimation = false
            isLockLand = true
            isNeedShowWifiTip = false
            isLooping = true
            setIsTouchWigetFull(true)
            setUp(uri, true, "").toString()
            startPlayLogic()
        }
    }

    override fun onPause() {
        super.onPause()
        player.onVideoPause()
    }

    override fun onResume() {
        super.onResume()
        player.onVideoResume()
    }

    override fun onDestroy() {
        GSYVideoManager.releaseAllVideos()
        player.setVideoAllCallBack(null)
        player.release()
        super.onDestroy()
    }
}