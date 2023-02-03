package org.yameida.worktool.utils

import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader

object RuntimeUtil {

    @Throws(Exception::class)
    fun exec(cmd: String): String? {
        var ret: String? = null
        val p = Runtime.getRuntime().exec(arrayOf("sh", "-c", cmd))
        val inputStream: InputStream = p.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        var line: String? = reader.readLine()
        while (line != null) {
            ret = line
            line = reader.readLine()
        }
        p.waitFor()
        inputStream.close()
        reader.close()
        p.destroy()
        return ret
    }
}