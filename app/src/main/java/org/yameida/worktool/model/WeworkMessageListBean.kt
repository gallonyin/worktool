package org.yameida.worktool.model

import com.blankj.utilcode.util.EncryptUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.TimeUtils
import org.yameida.worktool.Constant
import java.util.*
import kotlin.collections.ArrayList

class WeworkMessageListBean {

    companion object {
        const val SOCKET_TYPE_HEARTBEAT = 0
        const val SOCKET_TYPE_MESSAGE_CONFIRM = 1
        const val SOCKET_TYPE_MESSAGE_LIST = 2
    }

    /**
     * type
     * TYPE_HEARTBEAT 心跳检测
     * TYPE_MESSAGE_CONFIRM 消息确认
     * TYPE_MESSAGE_LIST 消息列表
     */
    var socketType = SOCKET_TYPE_HEARTBEAT

    //消息id
    var messageId = TimeUtils.date2String(Date()).replace(" ", "#") + "#" + UUID.randomUUID()

    //消息列表
    var list: ArrayList<WeworkMessageBean> = arrayListOf()

    //加密消息列表
    var encryptedList: String = ""

    //消息加密 0不加密 1AES
    var encryptType = Constant.encryptType

    constructor(weworkMessageBean: WeworkMessageBean, type: Int) {
        if (encryptType == 0) {
            list.add(weworkMessageBean)
        } else if (encryptType == 1) {
            encryptedList = EncryptUtils.encryptAES2HexString(
                GsonUtils.toJson(arrayListOf(weworkMessageBean)).toByteArray(),
                Constant.key,
                Constant.transformation,
                Constant.iv
            )
        }
        this.socketType = type
    }

    constructor(messageId: String, type: Int) {
        this.messageId = messageId
        this.socketType = type
    }

    constructor(weworkMessageBeanList: List<WeworkMessageBean>, type: Int) {
        if (encryptType == 0) {
            list.addAll(weworkMessageBeanList)
        } else if (encryptType == 1) {
            encryptedList = EncryptUtils.encryptAES2HexString(
                GsonUtils.toJson(weworkMessageBeanList).toByteArray(),
                Constant.key,
                Constant.transformation,
                Constant.iv
            )
        }
        this.socketType = type
    }
}