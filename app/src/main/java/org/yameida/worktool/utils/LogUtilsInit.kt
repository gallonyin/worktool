package org.yameida.worktool.utils

import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.LogUtils
import org.yameida.worktool.Constant
import java.lang.Exception
import java.util.*

object LogUtilsInit {

    /**
     * 参考
     *
    LogUtils.Config config =
    LogUtils.getConfig()
    .setLogSwitch(true) // 设置 log 总开关，包括输出到控制台和文件，默认开
    .setConsoleSwitch(AppUtils.isAppDebug()) // 设置是否输出到控制台开关，默认开
    .setGlobalTag(null) // 设置 log 全局标签，默认为空
    // 当全局标签不为空时，我们输出的 log 全部为该 tag，
    // 为空时，如果传入的 tag 为空那就显示类名，否则显示 tag
    .setLogHeadSwitch(true) // 设置 log 头信息开关，默认为开
    .setLog2FileSwitch(true) // 打印 log 时是否存到文件的开关，默认关
    .setDir("") // 当自定义路径为空时，写入应用的/cache/log/目录中
    .setFilePrefix("LYan") // 当文件前缀为空时，默认为"util"，即写入文件为"util-yyyy-MM-dd$fileExtension"
    .setFileExtension(".log") // 设置日志文件后缀
    .setBorderSwitch(AppUtils.isAppDebug()) // 输出日志是否带边框开关，默认开
    .setSingleTagSwitch(true) // 一条日志仅输出一条，默认开，为美化 AS 3.1 的 Logcat
    .setConsoleFilter(LogUtils.V) // log 的控制台过滤器，和 logcat 过滤器同理，默认 Verbose
    .setFileFilter(LogUtils.I) // log 文件过滤器，和 logcat 过滤器同理，默认 Verbose
    .setStackDeep(1) // log 栈深度，默认为 1
    .setStackOffset(0) // 设置栈偏移，比如二次封装的话就需要设置，默认为 0
    .setSaveDays(7) // 设置日志可保留天数，默认为 -1 表示无限时长
     *
     */
    fun init() {
        val prefix = try {
            AppUtils.getAppSignaturesMD5().firstOrNull()?.replace(":", "")
                ?.substring(0, 2)?.toLowerCase(Locale.ROOT)
        } catch (e: Exception) {
            null
        }
        LogUtils.getConfig().apply {
            isLog2FileSwitch = true
            saveDays = 7
            filePrefix = prefix
        }
        if (FlowPermissionHelper.isBlueCloud()) {
            Constant.customLink = true
            Constant.customMP = true
        }
    }

}