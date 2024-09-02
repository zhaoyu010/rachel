package com.yinlin.rachel.fragment

import android.net.Uri
import android.widget.SimpleAdapter
import androidx.annotation.ColorRes
import com.yinlin.rachel.R
import com.yinlin.rachel.RachelMessage
import com.yinlin.rachel.annotation.NewThread
import com.yinlin.rachel.backgroundColor
import com.yinlin.rachel.databinding.FragmentImportModBinding
import com.yinlin.rachel.model.RachelFragment
import com.yinlin.rachel.model.RachelMod
import com.yinlin.rachel.model.RachelOnClickListener
import com.yinlin.rachel.model.RachelPages
import com.yinlin.rachel.pathMusic
import com.yinlin.rachel.rachelClick


class FragmentImportMod(pages: RachelPages, private val uri: Uri) : RachelFragment<FragmentImportModBinding>(pages) {
    private var canCancel = true

    override fun bindingClass() = FragmentImportModBinding::class.java

    @NewThread
    override fun init() {
        var releaser: RachelMod.Releaser? = null
        try {
            releaser = RachelMod.Releaser(pages.context.contentResolver.openInputStream(uri)!!)
            val metadata = releaser.getMetadata()
            if (metadata.empty) throw Exception()
            val ids = ArrayList<String>()
            val listData = ArrayList<Map<String, String>>()
            for ((id, value) in metadata.items) {
                listData += hashMapOf("line1" to "${value.name} - $id", "line2" to "version ${value.version}")
                ids.add(id)
            }
            v.list.adapter = SimpleAdapter(
                pages.context, listData, android.R.layout.simple_list_item_2,
                arrayOf("line1", "line2"), intArrayOf(android.R.id.text1, android.R.id.text2)
            )
            val totalNum = metadata.totalCount
            v.tvTotalNum.text = totalNum.toString()
            v.buttonOk.rachelClick {
                // 如果播放器开启则先停止
                pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_STOP_PLAYER)
                setButtonStatus("导入中...", R.color.orange)
                canCancel = false
                Thread {
                    var runResult = false
                    try {
                        runResult = releaser.run(pathMusic) { id, res, index ->
                            post {
                                val curIndex = index + 1
                                val percent = curIndex * 100 / totalNum
                                v.tvRes.text = "Loading: ${metadata.items[id]!!.name}$res"
                                v.tvPercent.text = "$percent %"
                                v.tvCurNum.text = curIndex.toString()
                                v.progress.progress = percent
                            }
                        }
                    }
                    catch (ignored: Exception) { }
                    canCancel = true
                    releaser.close()
                    if (runResult) {
                        setButtonStatus("导入成功", R.color.green)
                        // 提醒播放器更新
                        pages.sendMessage(RachelPages.music, RachelMessage.MUSIC_NOTIFY_ADD_MUSIC, ids)
                    }
                    else setButtonStatus("导入失败", R.color.red)
                }.start()
            }
        } catch (e: Exception) {
            releaser?.close()
            setButtonStatus("导入失败", R.color.red)
        }
    }

    override fun back() = canCancel

    private fun setButtonStatus(text: String, @ColorRes color: Int) {
        v.buttonOk.isEnabled = false
        v.buttonOk.backgroundColor = pages.getResColor(color)
        v.buttonOk.text = text
    }
}