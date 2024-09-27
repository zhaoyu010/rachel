package com.yinlin.rachel.model.engine

import androidx.annotation.DrawableRes

@JvmRecord
data class LyricsEngineInfo(
    val name: String,
    val description: String,
    @DrawableRes val icon: Int,
)