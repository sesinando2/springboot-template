package net.dlcruz.util

object TestHelper {

    fun readAsStream(filePath: String) =
        javaClass.classLoader.getResourceAsStream("__files/$filePath")
}