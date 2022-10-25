package org.yameida.worktool

import com.blankj.utilcode.util.SPUtils

object Constant {

    val AVAILABLE_VERSION = arrayListOf("4.0.2", "4.0.6", "4.0.8", "4.0.10", "4.0.12", "4.0.16", "4.0.18", "4.0.19")
    const val PACKAGE_NAMES = "com.tencent.wework"
    const val LISTEN_CHANNEL_ID = "LISTEN_CHANNEL_ID"
    const val WEWORK_NOTIFY = "wework_notify"
    const val CHANGE_PAGE_INTERVAL = 1000L
    const val POP_WINDOW_INTERVAL = 500L
    private const val DEFAULT_HOST = "wss://worktool.asrtts.cn"

    var myName = ""
    var regTrimTitle = "(…$)|(-.*$)|(\\(.*?\\)$)".toRegex()
    var key = "9876543210abcdef".toByteArray()
    var iv = "0123456789abcdef".toByteArray()
    val transformation = "AES/CBC/PKCS7Padding"
    var encryptType = SPUtils.getInstance().getInt("encryptType", 1)
    var autoReply = SPUtils.getInstance().getInt("autoReply", 1)
    var host: String
        get() = SPUtils.getInstance().getString("host", DEFAULT_HOST)
        set(value) {
            SPUtils.getInstance().put("host", value)
        }

    fun getWsUrl() = "$host/webserver/wework/" + SPUtils.getInstance().getString(Constant.LISTEN_CHANNEL_ID)

    fun getCheckUpdateUrl() = "${getBaseUrl()}/appUpdate/checkUpdate"

    private fun getBaseUrl() = host.replace("wss", "https").replace("ws", "http")

}
