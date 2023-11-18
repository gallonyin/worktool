package org.yameida.worktool

import com.blankj.utilcode.util.SPUtils

object Constant {

    val AVAILABLE_VERSION = arrayListOf("4.0.2", "4.0.6", "4.0.8", "4.0.10", "4.0.12", "4.0.16", "4.0.18", "4.0.19", "4.0.20", "4.1.0", "4.1.2", "4.1.3", "4.1.6", "4.1.7", "4.1.8", "4.1.9", "4.1.10")
    val AVAILABLE_VERSION_MAP = mapOf(Pair("4.0.2", 40002), Pair("4.0.6", 40006), Pair("4.0.8", 40008), Pair("4.0.10", 40010), Pair("4.0.12", 40012), Pair("4.0.16", 40016), Pair("4.0.18", 40018), Pair("4.0.19", 40019), Pair("4.0.20", 40020), Pair("4.1.0", 40100), Pair("4.1.2", 40102), Pair("4.1.3", 40103), Pair("4.1.6", 40106), Pair("4.1.7", 40107), Pair("4.1.8", 40108), Pair("4.1.9", 40109), Pair("4.1.9", 40110))
    const val PACKAGE_NAMES = "com.tencent.wework"
    const val WEWORK_NOTIFY = "wework_notify"
    const val BASE_LONG_INTERVAL = 5000L
    const val BASE_CHANGE_PAGE_INTERVAL = 1000L
    const val BASE_POP_WINDOW_INTERVAL = 500L
    var LONG_INTERVAL = BASE_LONG_INTERVAL
    var CHANGE_PAGE_INTERVAL = BASE_CHANGE_PAGE_INTERVAL
    var POP_WINDOW_INTERVAL = BASE_POP_WINDOW_INTERVAL
    private const val DEFAULT_HOST = "wss://api.worktool.ymdyes.cn"

    var version = Int.MAX_VALUE
    var myName = ""
    var myCorp = ""
    //    var regTrimTitle = "(…$)|(-.*$)|(\\(.*?\\)$)".toRegex()
    var regMail = "\\S+@\\S+\\.\\S+".toRegex()
    var regTrimTitle = "(…$)".toRegex()
    var key = "9876543210abcdef".toByteArray()
    var iv = "0123456789abcdef".toByteArray()
    var lastUseMultiSender = 0L
    val transformation = "AES/CBC/PKCS7Padding"
    val wssRegex = "^wss".toRegex()
    val wsRegex = "^ws".toRegex()
    val suffixString = "(-.*)?(…)?(\\(\\d+\\))?$"
    val suffixRegex = "(-.*)?(…)?(\\(\\d+\\))?$".toRegex()
    val digitalRegex = "\\(\\d+\\)\$".toRegex()
    var weworkCorpName: String
        get() = SPUtils.getInstance().getString("weworkCorpName", "")
        set(value) {
            SPUtils.getInstance().put("weworkCorpName", value)
        }
    var weworkCorpId: String
        get() = SPUtils.getInstance().getString(weworkCorpName + "weworkCorpId", "")
        set(value) {
            SPUtils.getInstance().put(weworkCorpName + "weworkCorpId", value)
        }
    var weworkAgentId: String
        get() = SPUtils.getInstance().getString(weworkCorpName + "weworkAgentId", "")
        set(value) {
            SPUtils.getInstance().put(weworkCorpName + "weworkAgentId", value)
        }
    var weworkSchema: String
        get() = SPUtils.getInstance().getString(weworkCorpName + "weworkSchema", "")
        set(value) {
            SPUtils.getInstance().put(weworkCorpName + "weworkSchema", value)
        }
    var weworkMP: String
        get() = SPUtils.getInstance().getString(weworkCorpName + "weworkMP", "")
        set(value) {
            SPUtils.getInstance().put(weworkCorpName + "weworkMP", value)
        }
    var encryptType: Int = SPUtils.getInstance().getInt("encryptType", 0)
    var autoReply: Int = SPUtils.getInstance().getInt("autoReply", 1)
    var groupStrict: Boolean
        get() = SPUtils.getInstance().getBoolean("groupStrict", false)
        set(value) = SPUtils.getInstance().put("groupStrict", value)
    var friendRemarkStrict: Boolean
        get() = SPUtils.getInstance().getBoolean("friendRemarkStrict", false)
        set(value) = SPUtils.getInstance().put("friendRemarkStrict", value)
    var pushImage = false
    var autoPublishComment: Boolean
        get() = SPUtils.getInstance().getBoolean("autoPublishComment", true)
        set(value) = SPUtils.getInstance().put("autoPublishComment", value)
    var groupQrCode: Boolean
        get() = SPUtils.getInstance().getBoolean("groupQrCode", false)
        set(value) = SPUtils.getInstance().put("groupQrCode", value)
    var enableMediaProject = false
    var enableSdkShare = false
    var fullGroupName: Boolean
        get() = SPUtils.getInstance().getBoolean("fullGroupName", true)
        set(value) = SPUtils.getInstance().put("fullGroupName", value)
    var customLink: Boolean
        get() = SPUtils.getInstance().getBoolean("customLink", false)
        set(value) = SPUtils.getInstance().put("customLink", value)
    var customMP: Boolean
        get() = SPUtils.getInstance().getBoolean("customMP", false)
        set(value) = SPUtils.getInstance().put("customMP", value)
    var robotId: String
        get() = SPUtils.getInstance().getString("robotId", SPUtils.getInstance().getString("LISTEN_CHANNEL_ID", ""))
        set(value) {
            SPUtils.getInstance().put("robotId", value)
        }
    //replyStrategy=replyAll+1   replyStrategy=0不回复 replyStrategy=1回复at replyStrategy=2回复所有
    var replyStrategy: Int
        get() = SPUtils.getInstance().getInt("replyStrategy", 1)
        set(value) {
            SPUtils.getInstance().put("replyStrategy", value)
        }
    var qaUrl: String
        get() = SPUtils.getInstance().getString("qaUrl", "")
        set(value) {
            SPUtils.getInstance().put("qaUrl", value)
        }
    var openCallback: Int
        get() = SPUtils.getInstance().getInt("openCallback", 0)
        set(value) {
            SPUtils.getInstance().put("openCallback", value)
        }
    var host: String
        get() = SPUtils.getInstance().getString("host", DEFAULT_HOST)
        set(value) {
            SPUtils.getInstance().put("host", value)
        }
    var oldDevice: Boolean
        get() = SPUtils.getInstance().getBoolean("oldDevice", false)
        set(value) = SPUtils.getInstance().put("oldDevice", value)
    var autoPassFriendRequest: Boolean
        get() = SPUtils.getInstance().getBoolean("autoPassFriendRequest", true)
        set(value) = SPUtils.getInstance().put("autoPassFriendRequest", value)
    var duplicationFilter: Boolean
        get() = SPUtils.getInstance().getBoolean("apiDuplicationFilter", true)
        set(value) {
            SPUtils.getInstance().put("apiDuplicationFilter", value)
        }

    fun getWsUrl() = "$host/webserver/wework/$robotId"

    fun getCheckUpdateUrl() = "${getBaseUrl()}/appUpdate/checkUpdate"

    fun getMasterCheckUpdateUrl() = "https://worktool.asrtts.cn/appUpdate/checkUpdate"

    fun getMyConfig() = "${getBaseUrl()}/robot/robotInfo/get?robotId=$robotId"

    fun getRobotUpdateUrl() = "${getBaseUrl()}/robot/robotInfo/update?robotId=$robotId"

    fun getTestUrl() = "${getBaseUrl()}/test"

    fun getPushLocalFileUrl() = "${getBaseUrl()}/fileUpload/upload?robotId=$robotId"

    private fun getBaseUrl() = host.replace(wssRegex, "https").replace(wsRegex, "http")

}
