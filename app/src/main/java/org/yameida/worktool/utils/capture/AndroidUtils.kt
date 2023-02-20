package org.yameida.worktool.utils.capture

import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.graphics.drawable.StateListDrawable
import android.media.MediaMetadataRetriever
import android.os.Process
import android.text.TextUtils
import android.view.View
import android.view.inputmethod.InputMethodManager
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import java.io.BufferedOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by gallon on 2019/7/9.
 */
class AndroidUtils {

    companion object {
        private val TAG = AndroidUtils::class.java.simpleName

        @JvmStatic
        fun hideKeyboard(view: View?) {
            if (view != null) {
                val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                if (imm.isActive) {
                    imm.hideSoftInputFromWindow(view.windowToken, 0)
                }
            }
        }

        @JvmStatic
        fun getMediaDurationTime(filePath: String): Long {
            var durationTime: Long = 0
            val retriever = MediaMetadataRetriever()
            try {
                retriever.setDataSource(filePath)
                val v1 = retriever.extractMetadata(9)
                if (TextUtils.isEmpty(v1)) {
                    LogUtils.d(TAG, "Extract metadata failed.")
                }
                durationTime = java.lang.Long.parseLong(v1)
            } catch (e: Exception) {
                e.printStackTrace()
                LogUtils.d(TAG, "exception = " + e.message)
            }

            try {
                retriever.release()
            } catch (e: Exception) {
                LogUtils.d(TAG, "retriever.release() Exception = " + e.message)
            }

            return durationTime
        }

        @JvmStatic
        fun hasPermission(context: Context, permission: String): Boolean {
            return try {
                context.checkPermission(permission, Process.myPid(), Process.myUid()) == PackageManager.PERMISSION_GRANTED
            } catch (e: SecurityException) {
                false
            }

        }

        @JvmStatic
        fun getTimeFileName(time: Long, type: String): String {
            return try {
                SimpleDateFormat(type).format(Date(time))
            } catch (e: Exception) {
                ""
            }

        }

        @JvmStatic
        fun getRunningAppProcesses(context: Context, packageName: String): Boolean {
            val appProcesses = (context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager).runningAppProcesses ?: return false
            for (appProcess in appProcesses) {
                if (appProcess.importance == 100 && appProcess.processName == packageName) {
                    return true
                }
            }
            return false
        }

        @JvmStatic
        fun setPressedBg(view: View, normal: Drawable?, focused: Drawable?, pressed: Drawable?) {
            val bg = StateListDrawable()
            val states = arrayOfNulls<IntArray>(6)
            states[0] = intArrayOf(16842919, 16842910)
            states[1] = intArrayOf(16842910, 16842908)
            states[2] = intArrayOf(16842910)
            states[3] = intArrayOf(16842908, 16842909)
            states[4] = intArrayOf(16842909)
            bg.addState(states[0], pressed)
            bg.addState(states[3], focused)
            bg.addState(states[2], normal)
            view.background = bg
        }

        fun bitmapToFile(bitmap: Bitmap, fileName: String, path: String = "screenShot"): String {
            val dir = File(Utils.getApp().getExternalFilesDir("capture"), path)
            if (!dir.exists()) {
                dir.mkdirs()
            }
            val myCaptureFile = File(dir, fileName)
            if (!myCaptureFile.exists()) {
                myCaptureFile.createNewFile()
            }
            val bos = BufferedOutputStream(FileOutputStream(myCaptureFile))
            bitmap.compress(Bitmap.CompressFormat.JPEG, 75, bos)
            bos.flush()
            bos.close()
            return myCaptureFile.absolutePath
        }
    }

}