package org.yameida.worktool.notification

import android.annotation.SuppressLint
import android.content.Intent
import com.blankj.utilcode.util.Utils
import org.yameida.worktool.utils.startServiceSafe
import org.yameida.worktool.service.PlayNotifyService
import java.lang.reflect.Method


/**
 * Created by Gallon on 2019/8/7.
 */
object PlayNotifyManager {
    private val TAG = PlayNotifyManager::class.java.simpleName

    private var context = Utils.getApp()
    @SuppressLint("WrongConstant")
    private val statusBarManager = context.getSystemService("statusbar")

    var isShow = false

    fun show() {
        context.startServiceSafe(Intent(context, PlayNotifyService::class.java))
    }

    private var collapse: Method? = null
    fun collapseStatusBar() {
        try {
            if (collapse == null) collapse = statusBarManager::class.java.getMethod("collapsePanels").apply { isAccessible = true }
            collapse?.invoke(statusBarManager)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}