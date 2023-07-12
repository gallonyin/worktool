package org.yameida.worktool.utils

import android.accessibilityservice.AccessibilityService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.Message
import android.view.View
import android.widget.ImageView
import com.blankj.utilcode.util.FileUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.bumptech.glide.Glide
import org.yameida.floatwindow.FloatWindowManager
import org.yameida.floatwindow.DefaultFloatService
import org.yameida.floatwindow.listener.OnClickListener
import org.yameida.worktool.Constant
import org.yameida.worktool.R
import org.yameida.worktool.activity.ListenActivity
import org.yameida.worktool.activity.SettingsActivity
import org.yameida.worktool.model.WeworkMessageBean
import org.yameida.worktool.service.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.thread

object FloatWindowHelper {

    var isPause = false

    fun showWindow() {
        LogUtils.d("FloatWindowHelper.showWindow()")

        FloatWindowManager.show(DefaultFloatService::class.java)

        val app = Utils.getApp()
        val intent = Intent(app, DefaultFloatService::class.java)
        app.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    /**
     * 主功能继续
     */
    private fun accessibilityServiceResume() {
        if (PermissionHelper.isAccessibilitySettingOn()) {
            LogUtils.i("主功能继续")
            ToastUtils.showShort("主功能继续~")
            //隐藏软键盘模式
            WeworkController.weworkService.softKeyboardController.showMode = AccessibilityService.SHOW_MODE_HIDDEN
            isPause = false
            MyLooper.getInstance().removeMessages(WeworkMessageBean.LOOP_RECEIVE_NEW_MESSAGE)
            MyLooper.getInstance().sendMessage(Message.obtain().apply {
                what = WeworkMessageBean.LOOP_RECEIVE_NEW_MESSAGE
                obj = WeworkMessageBean().apply { type = WeworkMessageBean.LOOP_RECEIVE_NEW_MESSAGE }
            })
        } else {
            LogUtils.e("请先打开WorkTool主功能~")
        }
    }

    /**
     * 主功能暂停
     */
    private fun accessibilityServicePause() {
        if (PermissionHelper.isAccessibilitySettingOn()) {
            LogUtils.i("主功能暂停")
            ToastUtils.showShort("主功能暂停~")
            //显示软键盘模式
            WeworkController.weworkService.softKeyboardController.showMode = AccessibilityService.SHOW_MODE_AUTO
            isPause = true
            WeworkController.mainLoopRunning = false
        } else {
            LogUtils.e("请先打开WorkTool主功能~")
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
            LogUtils.i("DefaultFloatService 服务连接")
            val service = (iBinder as DefaultFloatService.DefaultFloatServiceBinder).getService()
            service.onClickListener = object : OnClickListener {
                override fun onClick(v: View, event: Int) {
                    when (event) {
                        1 -> {
                            if (PermissionHelper.isAccessibilitySettingOn()) {
                                if (!isPause) {
                                    ToastUtils.showShort("请先暂停WorkTool主功能~")
                                    return
                                }
                                thread {
                                    val printNodeClazzTree =
                                        AccessibilityUtil.printNodeClazzTree(getRoot(true))
                                    val df = SimpleDateFormat("MMdd_HHmmss")
                                    val filePath = "${
                                        Utils.getApp().getExternalFilesDir("share")
                                    }/${df.format(Date())}/${df.format(Date())}_printNode.txt"
                                    val newFile = File(filePath)
                                    val create = FileUtils.createFileByDeleteOldFile(newFile)
                                    if (create && newFile.canWrite()) {
                                        printNodeClazzTree.append("\n")
                                            .append(WeworkController.weworkService.currentPackage)
                                            .append("\n")
                                            .append(WeworkController.weworkService.currentClass)
                                        newFile.writeBytes(printNodeClazzTree.toString().toByteArray())
                                        LogUtils.i("打印节点文件存储本地成功 $filePath", "当前页面: ${WeworkController.weworkService.currentClass}")
                                    }
                                    ShareUtil.share("*/*", newFile)
                                }
                            } else {
                                ToastUtils.showShort("请先打开WorkTool主功能~")
                            }
                        }
                        2 -> {
                            if (PermissionHelper.isAccessibilitySettingOn()) {
                                if (isPause) {
                                    Glide.with(Utils.getApp()).load(R.drawable.float_icon_pause).into(v as ImageView)
                                    accessibilityServiceResume()
                                } else {
                                    Glide.with(Utils.getApp()).load(R.drawable.float_icon_play).into(v as ImageView)
                                    accessibilityServicePause()
                                }
                            } else {
                                ToastUtils.showShort("请先打开WorkTool主功能~")
                            }
                        }
                        3 -> {
                            Utils.getApp().packageManager.getLaunchIntentForPackage(Constant.PACKAGE_NAMES)?.apply {
                                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                Utils.getApp().startActivity(this)
                            }
                        }
                        4 -> {
                            ListenActivity.enterActivity(Utils.getApp(), 0)
                        }
                    }
                }
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            LogUtils.i("DefaultFloatService 服务断开")
        }
    }

}