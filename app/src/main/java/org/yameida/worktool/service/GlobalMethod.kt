package org.yameida.worktool.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.*
import com.hjq.toast.ToastUtils
import org.yameida.worktool.Constant
import org.yameida.worktool.MyApplication
import org.yameida.worktool.model.ExecCallbackBean
import org.yameida.worktool.model.WeworkMessageBean
import org.yameida.worktool.model.WeworkMessageListBean
import org.yameida.worktool.utils.AccessibilityUtil
import org.yameida.worktool.utils.FloatWindowHelper
import org.yameida.worktool.utils.Views
import java.lang.Exception

var requestCode = 1000000
fun fastStartActivity(context: Context, clazz: Class<*>, flags: Int = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP, i: Intent? = null) {
    val intent = i ?: Intent(context, clazz)
    intent.flags = flags
    val pendingIntent = PendingIntent.getActivity(context, requestCode++, intent, 0)
    try {
        pendingIntent.send()
    } catch (e: PendingIntent.CanceledException) {
        e.printStackTrace()
        context.startActivity(intent)
    }
}

/**
 * 进入首页-消息页
 */
fun goHome() {
    goHomeTab("消息")
}

/**
 * 进入首页tab
 * 1.检查是否有底部tab
 * 2.回退到首页
 * @param title 消息/文档/通讯录/工作台/我
 * 可能因为管理员排版首页Tab而导致找不到匹配title
 */
fun goHomeTab(title: String): Boolean {
    var find = false
    while (!find) {
        val list = AccessibilityUtil.findAllOnceByText(getRoot(), title, exact = true)
        for (item in list) {
            val childCount = item.parent?.parent?.parent?.childCount
            if (childCount == 4 || childCount == 5) {
                //处理侧边栏抽屉打开
                if (title == "消息") {
                    val rect = Rect()
                    item.getBoundsInScreen(rect)
                    if (rect.left > ScreenUtils.getScreenWidth() / 2) {
                        return goHomeTab("工作台") && goHomeTab("消息")
                    }
                }
                if (!item.isSelected) {
                    AccessibilityUtil.performClick(item)
                    sleep(300)
                }
                find = true
            }
        }
        if (!find) {
            if (isAtHome()) {
                return false
            } else {
                backPress()
                //如果在登录页面就提示关闭worktool主功能
                if (AccessibilityUtil.findOnceByText(getRoot(), "手机号登录", exact = true) != null) {
                    LogUtils.e("登录前请先关闭WorkTool主功能！")
                    ToastUtils.show("登录前请先关闭WorkTool主功能！")
                    MyApplication.launchIntent()
                    sleep(5000)
                }
            }
        }
    }
    LogUtils.v("进入首页-${title}页")
    return find
}

/**
 * 当前是否在首页
 */
fun isAtHome(): Boolean {
    val list = AccessibilityUtil.findAllOnceByText(getRoot(), "消息", exact = true)
    val item = list.firstOrNull {
        val childCount = it.parent?.parent?.parent?.childCount
        (childCount == 4 || childCount == 5)
    } ?: return false
    if (!item.isSelected) {
        AccessibilityUtil.performClick(item)
        sleep(300)
    }
    return true
}

/**
 * 获取企业微信窗口
 */
fun getRoot(): AccessibilityNodeInfo {
    return getRoot(false)
}

/**
 * 获取前台窗口
 * @param ignoreCheck false 必须等待前台为企业微信 true 直接返回当前前台窗口
 */
fun getRoot(ignoreCheck: Boolean): AccessibilityNodeInfo {
    while (true) {
        val tempRoot = WeworkController.weworkService.rootInActiveWindow
        val root = WeworkController.weworkService.rootInActiveWindow
        if (tempRoot != root) {
            LogUtils.e("tempRoot != root")
        } else if (root != null) {
            if (root.packageName == Constant.PACKAGE_NAMES) {
                return root
            } else {
                LogUtils.e("当前不在企业微信: ${root.packageName}")
                if (root.packageName == "com.android.systemui") {
                    if (AccessibilityUtil.findTextAndClick(root, "立即开始", exact = true, timeout = 0)) {
                        LogUtils.i("点击立即开始投屏")
                        log("点击立即开始投屏")
                    }
                }
                if (root.packageName == "android") {
                    if (AccessibilityUtil.findTextAndClick(root, "关闭应用", exact = true, timeout = 0)) {
                        LogUtils.e("点击关闭应用ANR")
                        error("点击关闭应用ANR")
                    }
                }
                WeworkController.weworkService.currentPackage = root.packageName?.toString() ?: ""
                if (System.currentTimeMillis() % 30 == 0L) {
                    error("当前不在企业微信: ${root.packageName}")
                    if (!FloatWindowHelper.isPause) {
                        ToastUtils.show("当前不在企业微信: ${root.packageName}\n尝试跳转到企业微信")
                        Utils.getApp().packageManager.getLaunchIntentForPackage(Constant.PACKAGE_NAMES)
                            ?.apply {
                                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                Utils.getApp().startActivity(this)
                            }
                    }
                }
                if (ignoreCheck) {
                    return root
                }
            }
        }
        sleep(Constant.CHANGE_PAGE_INTERVAL)
    }
}

/**
 * 后退
 */
fun backPress() {
    val clazz = WeworkController.weworkService.currentClass
    val textView = AccessibilityUtil.findOnceByClazz(getRoot(), Views.TextView)
    if (textView != null && textView.text.isNullOrBlank() && AccessibilityUtil.performClick(textView, retry = false)) {
        LogUtils.v("找到回退按钮")
    } else {
        val ivButton = AccessibilityUtil.findOnceByClazz(getRoot(), Views.ImageView)
        if (ivButton != null && ivButton.isClickable && AccessibilityUtil.findFrontNode(ivButton) == null) {
            LogUtils.d("未找到回退按钮 点击第一个IV按钮")
            AccessibilityUtil.performClick(ivButton, retry = false)
        } else {
            LogUtils.d("未找到回退按钮 点击第一个BT按钮")
            val button = AccessibilityUtil.findOnceByClazz(getRoot(), Views.Button)
            if (button != null && button.childCount > 0) {
                AccessibilityUtil.performClick(button.getChild(0), retry = false)
            } else if (button != null) {
                AccessibilityUtil.performClick(button, retry = false)
            } else {
                LogUtils.d("未找到BT按钮")
                val confirm = AccessibilityUtil.findOnceByText(getRoot(), "确定", "我知道了", "暂不进入", "不用了", "取消", "暂不", "关闭", "留在企业微信", exact = true)
                if (confirm != null) {
                    LogUtils.d("尝试点击确定/我知道了/暂不进入")
                    AccessibilityUtil.performClick(confirm)
                } else {
                    val stayButton = AccessibilityUtil.findOnceByText(getRoot(true), "关闭应用", "等待", exact = true)
                    if (stayButton != null) {
                        LogUtils.d("疑似ANR 尝试点击等待")
                        AccessibilityUtil.performClick(stayButton)
                    } else {
                        LogUtils.d("未找到对话框 点击bar中心")
                        AccessibilityUtil.performXYClick(WeworkController.weworkService, ScreenUtils.getScreenWidth() / 2F, BarUtils.getStatusBarHeight() * 2F)
                        sleep(Constant.CHANGE_PAGE_INTERVAL * 2)
                        if (WeworkController.weworkService.currentClass == clazz) {
                            val firstEmptyTextView = AccessibilityUtil.findAllByClazz(getRoot(), Views.TextView).firstOrNull { it.text.isNullOrEmpty() }
                            if (firstEmptyTextView != null && firstEmptyTextView.isClickable) {
                                AccessibilityUtil.performClick(firstEmptyTextView)
                            }
                            sleep(Constant.CHANGE_PAGE_INTERVAL)
                            if (WeworkController.weworkService.currentClass == clazz
                                && WeworkController.weworkService.currentPackage == Constant.PACKAGE_NAMES) {
                                AccessibilityUtil.globalGoBack(WeworkController.weworkService)
                            }
                        }
                    }
                }
            }
        }
    }
    sleep(Constant.POP_WINDOW_INTERVAL)
}

/**
 * 上传执行指令结果
 */
fun uploadCommandResult(message: WeworkMessageBean, errorCode: Int, errorReason: String, startTime: Long, successList: List<String> = listOf(), failList: List<String> = listOf()) {
    if ((message.fileBase64?.length ?: 0) > 100) {
        message.fileBase64 = message.fileBase64.substring(0, 100)
    }
    WeworkController.weworkService.webSocketManager.send(
        WeworkMessageListBean(
            ExecCallbackBean(GsonUtils.toJson(message), errorCode, errorReason, startTime, (System.currentTimeMillis() - startTime) / 1000.0, successList, failList),
            WeworkMessageListBean.SOCKET_TYPE_RAW_CONFIRM,
            messageId = message.messageId
        ), true
    )
    if (errorCode != 0) {
        if (message.apiSend == 1) {
            ToastUtils.show("错误提示 错误码: $errorCode 错误信息: $errorReason")
        }
        LogUtils.v("错误提示 错误码: $errorCode 错误信息: $errorReason")
    }
}

/**
 * 上传运行日志
 */
fun log(message: Any?, type: Int = WeworkMessageBean.ROBOT_LOG) {
    WeworkController.weworkService.webSocketManager.send(
        WeworkMessageListBean(
            WeworkMessageBean(
                null, null,
                type,
                null,
                null,
                null,
                if (message is String) message else GsonUtils.toJson(message)
            ),
            WeworkMessageListBean.SOCKET_TYPE_MESSAGE_LIST
        ), true
    )
}

/**
 * 上传运行日志
 */
fun error(message: Any?) {
    log(message, WeworkMessageBean.ROBOT_ERROR_LOG)
}

/**
 * 简单封装 sleep
 */
fun sleep(time: Long) {
    try {
        Thread.sleep(time)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}