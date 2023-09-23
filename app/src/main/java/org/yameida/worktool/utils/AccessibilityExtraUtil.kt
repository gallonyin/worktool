package org.yameida.worktool.utils

import android.util.Log
import java.lang.Thread.sleep
import org.yameida.worktool.service.WeworkController


/**
 * 无障碍服务扩展类
 * 注意：操作均为阻塞式，原则上本工具类所有操作都应在子线程执行
 */
object AccessibilityExtraUtil {
    private const val tag = "AccessibilityExtraUtil"
    private const val SHORT_INTERVAL = 150L
    private const val SCROLL_INTERVAL_NATIVE = 500L
    private const val SCROLL_INTERVAL = 800L

    /**
     * 等待进入页面
     * @param clazzList 页面Class
     * @param timeout 检查超时时间
     */
    fun loadingPage(
        vararg clazzList: String,
        timeout: Long = 5000
    ): Boolean {
        val service = WeworkController.weworkService
        val startTime = System.currentTimeMillis()
        var currentTime = startTime
        while (currentTime - startTime <= timeout) {
            if (service.currentClass in clazzList || service.currentClass.split(".").last() in clazzList) {
                Log.v(tag, "loadingPage: ${clazzList.joinToString()}")
                return true
            }
            sleep(SHORT_INTERVAL)
            currentTime = System.currentTimeMillis()
        }
        Log.e(tag, "loadingPage: not found: ${clazzList.joinToString()} current: ${service.currentClass}")
        return false
    }

}