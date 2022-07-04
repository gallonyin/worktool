package org.yameida.worktool

import com.blankj.utilcode.util.SPUtils
import org.yameida.worktool.config.WebConfig

object Constant {

    const val PACKAGE_NAMES = "com.tencent.wework"
    val BASE_URL = WebConfig.HOST.replace("wss", "https").replace("ws", "http")
    val URL_CHECK_UPDATE = "$BASE_URL/appUpdate/checkUpdate"
    const val CHANGE_PAGE_INTERVAL = 1000L
    const val POP_WINDOW_INTERVAL = 500L

    var myName = ""
    var key = "9876543210abcdef".toByteArray()
    var iv = "0123456789abcdef".toByteArray()
    val transformation = "AES/CBC/PKCS7Padding"
    var encryptType = SPUtils.getInstance().getInt("encryptType", 0)
}
