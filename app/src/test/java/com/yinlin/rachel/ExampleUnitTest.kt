package com.yinlin.rachel

import com.yinlin.rachel.model.RachelMod
import org.junit.Test
import java.io.File
import java.io.FileOutputStream


class ExampleUnitTest {
    @Test @Throws(Exception::class)
    fun generateMod() {
        basePath = "D:\\银临的小银库\\银临茶舍"
        val id = "无题雪"
        val src = File("D:\\银临的小银库\\银临茶舍\\music")
        val des = File("C:\\Users\\Administrator\\Desktop\\${id}1.0.rachel")
        val ids = listOf(id)
        val merger = RachelMod.Merger(src)
        val metadata = merger.getMetadata(ids, emptyList()) { songId, songRes, -> println("Error $songId $songRes") }
        val stream = FileOutputStream(des)
        merger.run(stream, metadata, null)
    }

    @Test
    fun test() {

    }
}