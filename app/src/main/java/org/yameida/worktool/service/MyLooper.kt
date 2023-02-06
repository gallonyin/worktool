package org.yameida.worktool.service

import android.os.Handler
import android.os.Looper
import android.os.Message
import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.google.gson.reflect.TypeToken
import okhttp3.WebSocket
import org.yameida.worktool.Constant
import org.yameida.worktool.model.ExecCallbackBean
import org.yameida.worktool.model.WeworkMessageBean
import org.yameida.worktool.model.WeworkMessageListBean
import org.yameida.worktool.utils.FloatWindowHelper
import java.nio.charset.StandardCharsets
import java.util.LinkedHashSet
import kotlin.concurrent.thread

object MyLooper {

    private var threadHandler: Handler? = null

    val looper = thread {
        LogUtils.i("myLooper starting...")
        Looper.prepare()
        val myLooper = Looper.myLooper()
        if (myLooper != null) {
            threadHandler = object : Handler(myLooper) {
                override fun handleMessage(msg: Message) {
                    while (FloatWindowHelper.isPause) {
                        LogUtils.i("主功能暂停...")
                        sleep(Constant.CHANGE_PAGE_INTERVAL)
                    }
                    LogUtils.d("handle message: " + Thread.currentThread().name, msg)
                    try {
                        dealWithMessage(msg.obj as WeworkMessageBean)
                    } catch (e: Exception) {
                        LogUtils.e(e)
                        uploadCommandResult(msg.obj as WeworkMessageBean, ExecCallbackBean.ERROR_ILLEGAL_OPERATION, e.message ?: "", 0L)
                    }
                }
            }
        } else {
            LogUtils.e("myLooper is null!")
        }
        Looper.loop()
    }

    fun init() {
        LogUtils.i("init myLooper...")
        SPUtils.getInstance("noTipMessage").clear()
        SPUtils.getInstance("lastSyncMessage").clear()
        SPUtils.getInstance("noSyncMessage").clear()
        SPUtils.getInstance("limit").clear()
    }

    fun getInstance(): Handler {
        while (true) {
            threadHandler?.let { return it }
            LogUtils.e("threadHandler is not ready...")
            sleep(Constant.POP_WINDOW_INTERVAL / 5)
        }
    }

    fun onMessage(webSocket: WebSocket?, text: String) {
        val messageList: WeworkMessageListBean<WeworkMessageBean> =
            GsonUtils.fromJson<WeworkMessageListBean<WeworkMessageBean>>(text, object : TypeToken<WeworkMessageListBean<ExecCallbackBean>>(){}.type)
        if (messageList.socketType == WeworkMessageListBean.SOCKET_TYPE_HEARTBEAT) {
            return
        }
        if (messageList.socketType == WeworkMessageListBean.SOCKET_TYPE_MESSAGE_CONFIRM) {
            return
        }
        if (messageList.socketType == WeworkMessageListBean.SOCKET_TYPE_MESSAGE_LIST) {
            val confirm = WeworkController.weworkService.webSocketManager.confirm(messageList.messageId)
            if (!confirm) return
            if (messageList.encryptType == 1) {
                val decryptHexStringAES = EncryptUtils.decryptHexStringAES(
                    messageList.encryptedList,
                    Constant.key,
                    Constant.transformation,
                    Constant.iv
                )
                messageList.list =
                    GsonUtils.fromJson(
                        String(decryptHexStringAES, StandardCharsets.UTF_8),
                        object : TypeToken<ArrayList<WeworkMessageBean>>() {}.type
                    )
            }
            //去重处理 丢弃之前的重复指令 丢弃之前的获取新消息指令
            for (message in LinkedHashSet(messageList.list)) {
                if (message.type == WeworkMessageBean.LOOP_RECEIVE_NEW_MESSAGE) {
                    WeworkController.enableLoopRunning = true
                } else {
                    WeworkController.mainLoopRunning = false
                    LogUtils.v("加入指令到执行队列", if (message.fileBase64.isNullOrEmpty()) GsonUtils.toJson(message) else message.type)
                    getInstance().sendMessage(Message.obtain().apply {
                        what = message.type * message.hashCode()
                        obj = message.apply {
                            messageId = messageList.messageId
                            apiSend = messageList.apiSend
                        }
                    })
                }
                getInstance().removeMessages(WeworkMessageBean.LOOP_RECEIVE_NEW_MESSAGE)
                getInstance().sendMessage(Message.obtain().apply {
                    what = WeworkMessageBean.LOOP_RECEIVE_NEW_MESSAGE
                    obj = WeworkMessageBean().apply { type = WeworkMessageBean.LOOP_RECEIVE_NEW_MESSAGE }
                })
            }
        }
    }

    private fun dealWithMessage(message: WeworkMessageBean) {
        when (message.type) {
            WeworkMessageBean.TYPE_CONSOLE_TOAST -> {
                WeworkController.consoleToast(message as ExecCallbackBean)
            }
            WeworkMessageBean.STOP_AND_GO_HOME -> {
                WeworkController.stopAndGoHome()
            }
            WeworkMessageBean.LOOP_RECEIVE_NEW_MESSAGE -> {
                WeworkController.loopReceiveNewMessage()
            }
            WeworkMessageBean.SEND_MESSAGE -> {
                WeworkController.sendMessage(message)
            }
            WeworkMessageBean.REPLY_MESSAGE -> {
                WeworkController.replyMessage(message)
            }
            WeworkMessageBean.RELAY_MESSAGE -> {
                WeworkController.relayMessage(message)
            }
            WeworkMessageBean.INIT_GROUP -> {
                WeworkController.initGroup(message)
            }
            WeworkMessageBean.INTO_GROUP_AND_CONFIG -> {
                WeworkController.intoGroupAndConfig(message)
            }
            WeworkMessageBean.PUSH_MICRO_DISK_IMAGE -> {
                WeworkController.pushMicroDiskImage(message)
            }
            WeworkMessageBean.PUSH_MICRO_DISK_FILE -> {
                WeworkController.pushMicroDiskFile(message)
            }
            WeworkMessageBean.PUSH_MICROPROGRAM -> {
                WeworkController.pushMicroprogram(message)
            }
            WeworkMessageBean.PUSH_OFFICE -> {
                WeworkController.pushOffice(message)
            }
            WeworkMessageBean.PASS_ALL_FRIEND_REQUEST -> {
            }
            WeworkMessageBean.ADD_FRIEND_BY_PHONE -> {
                WeworkController.addFriendByPhone(message)
            }
            WeworkMessageBean.PUSH_FILE -> {
                WeworkController.pushFile(message)
            }
            WeworkMessageBean.DISMISS_GROUP -> {
                WeworkController.dismissGroup(message)
            }
            WeworkMessageBean.ADD_FRIEND_BY_GROUP -> {
                WeworkController.addFriendByGroup(message)
            }
            WeworkMessageBean.ADD_NEED_DEAL -> {
                WeworkController.addNeedDeal(message)
            }
            WeworkMessageBean.CLOCK_IN -> {
                WeworkController.clockIn(message)
            }
            WeworkMessageBean.SWITCH_CORP -> {
                WeworkController.switchCorp(message)
            }
            WeworkMessageBean.SHOW_GROUP_INFO -> {
                WeworkController.showGroupInfo(message)
            }
            WeworkMessageBean.GET_GROUP_INFO -> {
                WeworkController.getGroupInfo(message)
            }
            WeworkMessageBean.GET_FRIEND_INFO -> {
                WeworkController.getFriendInfo(message)
            }
            WeworkMessageBean.GET_MY_INFO -> {
                WeworkController.getMyInfo(message)
            }
            WeworkMessageBean.GET_RECENT_LIST -> {
                WeworkController.getRecentList(message)
            }
            WeworkMessageBean.GET_CORP_LIST -> {
                WeworkController.getCorpList(message)
            }
            WeworkMessageBean.ROBOT_CONTROLLER_TEST -> {
                WeworkController.test(message)
            }
        }
    }
}