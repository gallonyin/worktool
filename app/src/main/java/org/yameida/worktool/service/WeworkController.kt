package org.yameida.worktool.service

import com.blankj.utilcode.util.*
import org.yameida.worktool.Demo
import org.yameida.worktool.annotation.RequestMapping
import org.yameida.worktool.model.WeworkMessageBean

/**
 * 企业微信客服端反转
 * 被服务端远程调用的服务Controller类
 */
object WeworkController {

    lateinit var weworkService: WeworkService
    var enableLoopRunning = false
    var mainLoopRunning = false

    /**
     * 停止所有任务并返回首页待命
     * @see WeworkMessageBean.STOP_AND_GO_HOME
     */
    @RequestMapping
    fun stopAndGoHome() {
        LogUtils.d("stopAndGoHome()")
        enableLoopRunning = false
        mainLoopRunning = false
        goHome()
    }

    /**
     * 回到首页等待接收新消息
     * @see WeworkMessageBean.LOOP_RECEIVE_NEW_MESSAGE
     */
    @RequestMapping
    fun loopReceiveNewMessage() {
        LogUtils.d("loopReceiveNewMessage() enableLoopRunning: $enableLoopRunning")
        WeworkLoopImpl.mainLoop()
    }

    /**
     * 在房间内发送消息
     * @see WeworkMessageBean.SEND_MESSAGE
     * @param message#titleList 房间名称
     * @param message#receivedContent 回复内容
     * @see WeworkMessageBean.TEXT_TYPE
     */
    @RequestMapping
    fun sendMessage(message: WeworkMessageBean): Boolean {
        LogUtils.d("sendMessage(): ${message.titleList} ${message.receivedContent}")
        return WeworkOperationImpl.sendMessage(message.titleList, message.receivedContent)
    }

    /**
     * 在房间内指定回复消息
     * @see WeworkMessageBean.REPLY_MESSAGE
     * @param message#titleList 房间名称
     * @param message#receivedName 原始消息的发送者姓名
     * @param message#originalContent 原始消息的内容
     * @param message#textType 原始消息的消息类型
     * @param message#receivedContent 回复内容
     * @see WeworkMessageBean.TEXT_TYPE
     */
    @RequestMapping
    fun replyMessage(message: WeworkMessageBean): Boolean {
        LogUtils.d("replyMessage(): ${message.receivedName} ${message.originalContent} ${message.receivedContent}")
        return WeworkOperationImpl.replyMessage(
            message.titleList,
            message.receivedName,
            message.originalContent,
            message.textType,
            message.receivedContent
        )
    }

    /**
     * 在房间内转发消息
     * @see WeworkMessageBean.RELAY_MESSAGE
     * @param message#titleList 房间名称
     * @param message#receivedName 原始消息的发送者姓名
     * @param message#originalContent 原始消息的内容
     * @param message#textType 原始消息的消息类型
     * @param message#nameList 待转发姓名列表
     * @param message#extraText 附加留言 选填
     * @see WeworkMessageBean.TEXT_TYPE
     */
    @RequestMapping
    fun relayMessage(message: WeworkMessageBean): Boolean {
        LogUtils.d("relayMessage(): ${message.titleList} ${message.receivedName} ${message.originalContent} ${message.textType} ${message.nameList} ${message.extraText}")
        return WeworkOperationImpl.relayMessage(
            message.titleList,
            message.receivedName,
            message.originalContent,
            message.textType,
            message.nameList,
            message.extraText
        )
    }

    /**
     * 初始化群设置
     * @see WeworkMessageBean.INIT_GROUP
     * @param message#groupName 修改群名称
     * @param message#selectList 添加群成员名称列表 选填
     * @param message#groupAnnouncement 修改群公告 选填
     */
    @RequestMapping
    fun initGroup(message: WeworkMessageBean): Boolean {
        LogUtils.d("initGroup(): ${message.groupName} ${message.selectList} ${message.groupAnnouncement}")
        return WeworkOperationImpl.initGroup(
            message.groupName,
            message.selectList,
            message.groupAnnouncement
        )
    }

    /**
     * 机器人接口测试
     * @see WeworkMessageBean.ROBOT_CONTROLLER_TEST
     */
    @RequestMapping
    fun test(message: WeworkMessageBean? = null) {
        LogUtils.d(message)
        Demo.test(true)
    }

    /**
     * 进入群聊并修改群配置
     * 群名称、群公告、拉人、踢人
     * @see WeworkMessageBean.INTO_GROUP_AND_CONFIG
     * @param message#groupName 待修改的群
     * @param message#newGroupName 修改群名 选填
     * @param message#newGroupAnnouncement 修改群公告 选填
     * @param message#selectList 添加群成员名称列表/拉人 选填
     * @param message#showMessageHistory 拉人是否附带历史记录 选填
     * @param message#removeList 移除群成员名称列表/踢人 选填
     */
    @RequestMapping
    fun intoGroupAndConfig(message: WeworkMessageBean): Boolean {
        LogUtils.d("intoGroupAndConfig(): ${message.groupName} ${message.newGroupName} ${message.newGroupAnnouncement} ${message.selectList} ${message.showMessageHistory} ${message.removeList}")
        return WeworkOperationImpl.intoGroupAndConfig(
            message.groupName,
            message.newGroupName,
            message.newGroupAnnouncement,
            message.selectList,
            message.showMessageHistory,
            message.removeList
        )
    }

    /**
     * 推送微盘图片
     * @see WeworkMessageBean.PUSH_MICRO_DISK_IMAGE
     * @param message#titleList 待发送姓名列表
     * @param message#objectName 图片名称
     * @param message#extraText 附加留言 可选
     */
    @RequestMapping
    fun pushMicroDiskImage(message: WeworkMessageBean): Boolean {
        LogUtils.d("pushMicroDiskImage(): ${message.titleList} ${message.objectName} ${message.extraText}")
        return WeworkOperationImpl.pushMicroDiskImage(
            message.titleList,
            message.objectName,
            message.extraText
        )
    }

    /**
     * 推送微盘文件
     * @see WeworkMessageBean.PUSH_MICRO_DISK_FILE
     * @param message#titleList 待发送姓名列表
     * @param message#objectName 文件名称
     * @param message#extraText 附加留言 可选
     */
    @RequestMapping
    fun pushMicroDiskFile(message: WeworkMessageBean): Boolean {
        LogUtils.d("pushMicroDiskFile(): ${message.titleList} ${message.objectName} ${message.extraText}")
        return WeworkOperationImpl.pushMicroDiskFile(
            message.titleList,
            message.objectName,
            message.extraText
        )
    }

    /**
     * 推送任意小程序
     * @see WeworkMessageBean.PUSH_MICROPROGRAM
     * @param message#titleList 待发送姓名列表
     * @param message#objectName 小程序名称
     * @param message#extraText 附加留言 可选
     */
    @RequestMapping
    fun pushMicroprogram(message: WeworkMessageBean): Boolean {
        LogUtils.d("pushMicroprogram(): ${message.titleList} ${message.objectName} ${message.extraText}")
        return WeworkOperationImpl.pushMicroprogram(
            message.titleList,
            message.objectName,
            message.extraText
        )
    }

    /**
     * 推送腾讯文档
     * @see WeworkMessageBean.PUSH_OFFICE
     * TODO 自己的文档分享时可选择权限级别
     * @param message#titleList 待发送姓名列表
     * @param message#objectName 腾讯文档名称
     * @param message#extraText 附加留言 可选
     */
    @RequestMapping
    fun pushOffice(message: WeworkMessageBean): Boolean {
        LogUtils.d("pushOffice(): ${message.titleList} ${message.objectName} ${message.extraText}")
        return WeworkOperationImpl.pushOffice(
            message.titleList,
            message.objectName,
            message.extraText
        )
    }

    /**
     * 按手机号添加好友
     * @see WeworkMessageBean.ADD_FRIEND_BY_PHONE
     * @param message#friend 待添加用户列表
     */
    @RequestMapping
    fun addFriendByPhone(message: WeworkMessageBean): Boolean {
        LogUtils.d("addFriendByPhone(): ${message.friend}")
        return WeworkOperationImpl.addFriendByPhone(message.friend)
    }

    /**
     * 展示群信息
     * @see WeworkMessageBean.SHOW_GROUP_INFO
     * @param message#titleList 待查询群名
     * @param message#receivedName 原始消息的发送者姓名
     * @param message#originalContent 原始消息的内容
     * @param message#textType 原始消息的消息类型
     */
    @RequestMapping
    fun showGroupInfo(message: WeworkMessageBean): Boolean {
        LogUtils.d("showGroupInfo(): ${message.titleList} ${message.receivedName} ${message.originalContent} ${message.textType}")
        return WeworkOperationImpl.showGroupInfo(
            message.titleList,
            message.receivedName,
            message.originalContent,
            message.textType
        )
    }

    /**
     * 获取群信息
     * @see WeworkMessageBean.GET_GROUP_INFO
     * @param message#selectList 群名列表 为空时去群管理页查询并返回群聊页
     */
    @RequestMapping
    fun getGroupInfo(message: WeworkMessageBean): Boolean {
        LogUtils.d("getGroupInfo(): ${message.selectList}")
        return WeworkGetImpl.getGroupInfo(message.selectList)
    }

    /**
     * 获取好友信息
     * @see WeworkMessageBean.GET_FRIEND_INFO
     * TODO
     * @param message#selectList 好友名列表
     */
    @RequestMapping
    fun getFriendInfo(message: WeworkMessageBean): Boolean {
        LogUtils.d("getFriendInfo(): ${message.selectList}")
        return WeworkGetImpl.getFriendInfo(message.selectList)
    }

    /**
     * 获取我的信息
     * @see WeworkMessageBean.GET_MY_INFO
     */
    @RequestMapping
    fun getMyInfo(): Boolean {
        LogUtils.d("getMyInfo():")
        return WeworkGetImpl.getMyInfo()
    }

}