package org.yameida.worktool.utils

import android.app.AppOpsManager
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import org.yameida.worktool.utils.envcheck.CheckRoot
import java.lang.reflect.Method

object FlowPermissionHelper {

    fun isXiaoMi(): Boolean {
        return checkManufacturer("xiaomi")
    }

    fun isOppo(): Boolean {
        return checkManufacturer("oppo")
    }

    fun isVivo(): Boolean {
        return checkManufacturer("vivo")
    }

    private fun checkManufacturer(manufacturer: String): Boolean {
        return manufacturer.equals(Build.MANUFACTURER, true)
    }

    fun canBackgroundStart(context: Context): Boolean {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1 || CheckRoot.isDeviceRooted()) {
            return true
        }

        if (isXiaoMi()) {
            return isXiaomiBgStartPermissionAllowed(context)
        }

        if (isVivo()) {
            return isVivoBgStartPermissionAllowed(context)
        }

        if (isOppo() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(context)
        }
        return true
    }


    private fun isXiaomiBgStartPermissionAllowed(context: Context): Boolean {
        val ops = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        try {
            val op = 10021
            val method: Method = ops.javaClass.getMethod("checkOpNoThrow", Int::class.javaPrimitiveType, Int::class.javaPrimitiveType, String::class.java)
            val result = method.invoke(ops, op, android.os.Process.myUid(), context.packageName) as Int
            return result == AppOpsManager.MODE_ALLOWED
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    private fun isVivoBgStartPermissionAllowed(context: Context): Boolean {
        return getVivoBgStartPermissionStatus(context) == 0
    }

    /**
     * 判断Vivo后台弹出界面状态， 1无权限，0有权限
     * @param context context
     */
    private fun getVivoBgStartPermissionStatus(context: Context): Int {
        val uri: Uri = Uri.parse("content://com.vivo.permissionmanager.provider.permission/start_bg_activity")
        val selection = "pkgname = ?"
        val selectionArgs = arrayOf(context.packageName)
        var state = 1
        try {
            context.contentResolver.query(uri, null, selection, selectionArgs, null)?.use {
                if (it.moveToFirst()) {
                    state = it.getInt(it.getColumnIndex("currentstate"))
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return state
    }
}
