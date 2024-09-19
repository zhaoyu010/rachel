package com.yinlin.rachel

import com.yinlin.rachel.model.RachelMod
import org.junit.Test
import java.io.File
import java.io.FileOutputStream


class ExampleUnitTest {
    @Test @Throws(Exception::class)
    fun generateMod() {
        basePath = "D:\\银临的小银库\\银临茶舍"
        val src = File("D:\\银临的小银库\\银临茶舍\\music")
        val des = File("C:\\Users\\Administrator\\Desktop\\棠梨煎雪.rachel")
        val ids = listOf("棠梨煎雪")
        val filter = listOf(RachelMod.RES_VIDEO)
        val merger = RachelMod.Merger(src)
        val metadata = merger.getMetadata(ids, filter)
        val stream = FileOutputStream(des)
        merger.run(stream, metadata, null)
    }

    @Test
    fun test() {

    }
}