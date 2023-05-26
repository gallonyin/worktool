package org.yameida.worktool.utils

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ShellUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * 系统属性工具类
 */
object PropUtil {

    const val propVideo = "persist.lg.sourc_video"

    fun getProp(propName: String = propVideo) {
        try {
            val process = Runtime.getRuntime().exec("getprop $propName")
            val ir = InputStreamReader(process.inputStream)
            val input = BufferedReader(ir)
            var str: String? = null
            while (input.readLine().also { str = it } != null) {
                LogUtils.i("$propName: $str")
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun setProp(propName: String = propVideo, propValue: String = "/storage/emulated/0/Download/1.mp4") {
        try {
            ShellUtils.execCmd("setprop $propName $propValue", true, false)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        getProp(propName)
    }

}