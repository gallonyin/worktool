package org.yameida.worktool.utils

import android.provider.Settings
import android.text.TextUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import org.yameida.worktool.service.WeworkService

/**
 * 无障碍服务开启辅助类
 */
object PermissionHelper {

    fun isAccessibilitySettingOn(): Boolean {
        val context = Utils.getApp()
        var enable = 0
        val canonicalName = WeworkService::class.java.canonicalName ?: ""
        val serviceName = context.packageName + "/" + canonicalName
        val serviceShortName = context.packageName + "/" + canonicalName.replace(context.packageName, "")
        try {
            enable = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED,
                0
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        var flag = false
        if (enable == 1) {
            val stringSplitter = TextUtils.SimpleStringSplitter(':')
            val settingVal = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingVal != null) {
                stringSplitter.setString(settingVal)
                while (stringSplitter.hasNext()) {
                    val accessibilityService = stringSplitter.next()
                    if (accessibilityService == serviceName || accessibilityService == serviceShortName) {
                        flag = true
                        break
                    }
                }
            }
        }
        LogUtils.v("isAccessibilitySettingOn: $serviceName $serviceShortName $flag")
        return flag
    }

}