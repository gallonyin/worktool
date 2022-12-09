package org.yameida.floatwindow

import android.content.Intent
import android.os.Build
import com.blankj.utilcode.util.Utils

/**
 * Created by Gallon on 2019/9/5.
 */
object FloatWindowManager {
    private val TAG = FloatWindowManager::class.java.simpleName

    private var context = Utils.getApp()

    fun show(service: Class<out BaseFloatWindow>, intent: Intent? = null) {
        startServiceSafe(Intent(context, service).apply {
            if (intent != null) {
                this.putExtras(intent)
            }
        })
    }

    fun hide(service: Class<out BaseFloatWindow>, intent: Intent? = null) {
        startServiceSafe(Intent(context, service).apply {
            if (intent != null) {
                this.putExtras(intent)
            }
        })
    }

    private fun startServiceSafe(intent: Intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

}