package org.yameida.worktool.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.view.View
import com.blankj.utilcode.util.EncryptUtils



fun getFileMD5(file: java.io.File): String = file.getMD5()

fun java.io.File.getMD5(): String {
  val digest = EncryptUtils.encryptMD5File(this)
  val strHex = arrayOf("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "a", "b", "c", "d", "e", "f")
  val sb = StringBuilder()
  for (byte in digest) {
    var d = byte.toInt()
    if (d < 0) d += 256
    val d1 = d / 16
    val d2 = d % 16
    sb.append(strHex[d1] + strHex[d2])
  }
  return sb.toString()
}

fun Long.toTimeString(): String {
  val i = this.toInt() / 1000
  return String.format("%02d:%02d", Integer.valueOf(i / 60), Integer.valueOf(i % 60))
}

fun Context.startServiceSafe(intent: Intent) {
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    startForegroundService(intent)
  } else {
    startService(intent)
  }
}

fun Activity.hideBottomNav() {
  val decorView = this.window.decorView
  decorView.systemUiVisibility = 0
  val uiOptions = (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
          or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or View.SYSTEM_UI_FLAG_FULLSCREEN or
          View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
  decorView.setSystemUiVisibility(uiOptions)
}

fun Activity.showBottomNav() {
  val decorView = this.window.decorView
  decorView.systemUiVisibility = 0
}
