package org.yameida.worktool.service

import android.view.accessibility.AccessibilityNodeInfo
import androidx.core.text.isDigitsOnly
import com.blankj.utilcode.util.LogUtils
import org.yameida.worktool.Constant
import org.yameida.worktool.Demo
import org.yameida.worktool.model.WeworkMessageBean
import org.yameida.worktool.service.WeworkController.mainLoopRunning
import org.yameida.worktool.utils.*
import java.lang.Exception
import java.lang.StringBuilder

/**
 * 获取数据类型 201 202 主循环
 */
object WeworkLoopImpl {

    val stopWords = arrayListOf("解析中")
    var logIndex = 0

    fun mainLoop() {
        if (!WeworkController.enableLoopRunning)
            return
        mainLoopRunning = true
        try {
            while (mainLoopRunning) {
                if (WeworkRoomUtil.getRoomType(false) != WeworkMessageBean.ROOM_TYPE_UNKNOWN
                    && getChatMessageList()) {
                }
                if (!mainLoopRunning) break
                goHomeTab("消息")
                if (!mainLoopRunning) break
                if (getChatroomList()) {
                }
                if (!mainLoopRunning) break
                if (getFriendRequest()) {
                }
                sleep(500)
            }
        } catch (e: Exception) {
            mainLoopRunning = false
            error("ERROR mainLoop: " + e.message)
        }
    }

    /**
     * 读取通讯录好友请求
     */
    fun getFriendRequest(): Boolean {
        val list = AccessibilityUtil.findAllOnceByText(getRoot(), "通讯录", exact = true)
        for (item in list) {
            if (item.parent?.parent?.parent?.childCount == 5) {
                if (item.parent != null && item.parent.childCount > 1) {
                    LogUtils.d("通讯录有红点")
                    AccessibilityUtil.performClick(item)
                    val hasRecommendFriend = AccessibilityUtil.findOneByText(getRoot(), "可能的", timeout = Constant.POP_WINDOW_INTERVAL)
                    if (hasRecommendFriend != null) {
                        LogUtils.d("有可能认识的人")
                        AccessibilityUtil.performClick(hasRecommendFriend)
                        goHome()
                        return false
                    }
                    val addButton = AccessibilityUtil.findOneByText(getRoot(), "添加")
                    val backNode = AccessibilityUtil.findBackNode(addButton)
                    if (backNode?.className == Views.TextView) {
                        LogUtils.d("有待添加客户")
                        AccessibilityUtil.performClick(backNode)
                        AccessibilityUtil.findTextAndClick(getRoot(), "新的")
                        sleep(Constant.POP_WINDOW_INTERVAL)
                        var retry = 5
                        while (retry-- > 0) {
                            val checkButton = AccessibilityUtil.findOneByText(getRoot(), "查看", timeout = 2000)
                            if (checkButton == null) {
                                break
                            } else {
                                AccessibilityUtil.performClick(checkButton)
                                sleep(Constant.CHANGE_PAGE_INTERVAL)
                                val nameList = passFriendRequest()
                                if (nameList.isEmpty())
                                    break
                                //todo 可自定义执行任务
//                                Demo.test2(nameList[0])
                            }
                        }
                        return true
                    } else {
                        LogUtils.d("未发现待添加客户")
                    }
                } else {
                    LogUtils.v("通讯录无红点")
                }
            }
        }
        return false
    }

    /**
     * 聊天页
     * 1.获取群名
     * 2.获取消息列表
     * @param needInfer 是否需要推断@me并等待回复
     * @param timeout 在房间内等待回复的时长
     */
    fun getChatMessageList(needInfer: Boolean = true, timeout: Long = 3000): Boolean {
        if (Constant.autoReply == 0) return true
        AccessibilityUtil.performScrollDown(getRoot(), 0)
        val roomType = WeworkRoomUtil.getRoomType()
        var titleList = WeworkRoomUtil.getRoomTitle()
        if (titleList.contains("对方正在输入…")) {
            titleList = WeworkRoomUtil.getFriendName()
        }
        if (titleList.size > 0) {
            val title = titleList.joinToString()
            LogUtils.v("聊天: $title")
            log("聊天: $title")
            //聊天消息列表 1ListView 0RecycleView xViewGroup
            val list = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
            if (list != null) {
                LogUtils.v("消息条数: " + list.childCount)
                val messageList = arrayListOf<WeworkMessageBean.SubMessageBean>()
                for (i in 0 until list.childCount) {
                    val item = list.getChild(i)
                    if (item != null && item.childCount > 0) {
                        messageList.add(parseChatMessageItem(item, roomType))
                    }
                }
                WeworkController.weworkService.webSocketManager.send(
                    WeworkMessageBean(
                        null, null,
                        WeworkMessageBean.TYPE_RECEIVE_MESSAGE_LIST,
                        roomType,
                        titleList,
                        messageList,
                        null
                    )
                )
                //推测是否回复并在房间等待指令
                if (needInfer) {
                    val lastMessage = messageList.lastOrNull { it.sender == 0 }
                    if (lastMessage != null) {
                        var tempContent = ""
                        for (itemMessage in lastMessage.itemMessageList) {
                            if (itemMessage.text.contains("@" + Constant.myName)
                                || itemMessage.text.isDigitsOnly()
                            ) {
                                tempContent = itemMessage.text
                            }
                        }
                        if (roomType == WeworkMessageBean.ROOM_TYPE_EXTERNAL_CONTACT
                            || roomType == WeworkMessageBean.ROOM_TYPE_INTERNAL_CONTACT
                            || tempContent.isNotBlank()
                        ) {
                            LogUtils.v("推测需要回复: $tempContent")
                            val startTime = System.currentTimeMillis()
                            var currentTime = startTime
                            while (mainLoopRunning && currentTime - startTime < timeout) {
                                sleep(Constant.POP_WINDOW_INTERVAL / 5)
                                currentTime = System.currentTimeMillis()
                            }
                            if (mainLoopRunning) {
                                return getChatMessageList(needInfer = false)
                            }
                        }
                    }
                }
                return true
            } else {
                LogUtils.e("未找到聊天消息列表")
                error("未找到聊天消息列表")
            }
        }
        return false
    }

    /**
     * 查看好友请求并通过
     */
    private fun passFriendRequest(): List<String> {
        val nameList = arrayListOf<String>()
        val imageView = AccessibilityUtil.findOneByClazz(getRoot(), Views.ImageView)
        if (imageView != null) {
            val textViewList = AccessibilityUtil.findAllOnceByClazz(imageView.parent, Views.TextView)
            val filter = textViewList.filter { it.text != null && it.text.toString() != "微信" }
            if (filter.isNotEmpty()) {
                val tvNick = filter[0]
                LogUtils.d("好友请求: " + tvNick.text)
                //设置标签
                if (AccessibilityUtil.findTextAndClick(getRoot(), "标签")) {
                    WeworkOperationImpl.setFriendTags(arrayListOf("worktool自动通过"))
                }
                AccessibilityUtil.findTextAndClick(getRoot(), "通过验证")
                AccessibilityUtil.findTextAndClick(getRoot(), "完成")
                if (AccessibilityUtil.findTextAndClick(getRoot(), "确定")) {
                    LogUtils.d("添加好友失败")
                } else {
                    val weworkMessageBean = WeworkMessageBean()
                    weworkMessageBean.type = WeworkMessageBean.GET_FRIEND_INFO
                    weworkMessageBean.friend = WeworkMessageBean.Friend().apply {
                        name = tvNick.text.toString()
                        newFriend = true
                    }
                    WeworkController.weworkService.webSocketManager.send(weworkMessageBean)
                    nameList.add(tvNick.text.toString())
                }
                //回到上一页
                var retry = 5
                while (retry-- > 0 && !isAtHome()) {
                    val textView = AccessibilityUtil.findOnceByText(getRoot(), "新的客户", "新的居民", "新的学员", exact = true)
                    if (textView == null) {
                        backPress()
                    }
                }
            }
        }
        return nameList
    }

    /**
     * 读取聊天列表
     */
    private fun getChatroomList(): Boolean {
        if (Constant.autoReply == 0) return true
        if (!isAtHome()) return true

        val list = AccessibilityUtil.findAllOnceByText(getRoot(), "消息", exact = true)
        for (item in list) {
            if (item.parent?.parent?.parent?.childCount == 5) {
                if (item.parent != null && item.parent.childCount > 1) {
                    LogUtils.d("消息有红点")
                    AccessibilityUtil.clickByNode(WeworkController.weworkService, item)
                    sleep(100)
                    AccessibilityUtil.clickByNode(WeworkController.weworkService, item)
                }
            }
        }
        if (logIndex % 120 == 0) {
            goHomeTab("通讯录")
            goHomeTab("消息")
        }
        if (!isAtHome()) return true
        if (logIndex++ % 30 == 0) {
            LogUtils.i("读取首页聊天列表")
            if (logIndex % 120 == 0) log("读取首页聊天列表")
        }
        val listview = AccessibilityUtil.findOneByClazz(getRoot(), Views.RecyclerView, Views.ListView, Views.ViewGroup)
        if (listview != null) {
            if (listview.childCount >= 2) {
                if (checkUnreadChatRoom(listview)) {
                    //进入聊天页
                    return true
                }
            } else {
                LogUtils.e("读取聊天列表失败")
                error("读取聊天列表失败")
            }
        } else {
            LogUtils.e("读取聊天列表失败")
            error("读取聊天列表失败")
        }
        return false
    }

    /**
     * 检查首页-聊天列表是否有未读红点并点击进入
     * 获取红点
     */
    private fun checkUnreadChatRoom(list: AccessibilityNodeInfo): Boolean {
        val spotNodeList = arrayListOf<AccessibilityNodeInfo>()
        for (i in 0 until list.childCount) {
            val item = list.getChild(i)
            if (item != null && Views.RelativeLayout.equals(item.className)) {
                if (item.childCount >= 2) {
                    val spotNode = item.getChild(1)
                    if (spotNode != null
                        && Views.TextView.equals(spotNode.className)
                        && spotNode.text != null
                        && spotNode.text.toString().replace("+", "").isDigitsOnly()
                    ) {
                        spotNodeList.add(spotNode)
                    }
                }
            }
        }
        if (spotNodeList.size > 0) {
            LogUtils.i("发现未读消息: " + spotNodeList.size + "条")
            log("发现未读消息: " + spotNodeList.size + "条")
            if (AccessibilityUtil.performClick(spotNodeList.first())) {
                //进入聊天页 下一步 getChatMessageList
            } else {
                AccessibilityUtil.clickByNode(WeworkController.weworkService, spotNodeList.first().parent)
            }
            return true
        } else {
            return false
        }
    }

    /**
     * 解析消息列表里的一条消息
     */
    private fun parseChatMessageItem(
        node: AccessibilityNodeInfo,
        roomType: Int
    ): WeworkMessageBean.SubMessageBean {
        val message: WeworkMessageBean.SubMessageBean
        val nameList = arrayListOf<String>()
        val itemMessageList = arrayListOf<WeworkMessageBean.ItemMessageBean>()
        LogUtils.v("开始解析一条消息...")
        //消息头(在消息主体上方 如时间信息)
        val linearLayoutItem = AccessibilityUtil.findOnceByClazz(node, Views.LinearLayout, limitDepth = 1)
        if (linearLayoutItem != null) {
            val sb = StringBuilder("消息头: ")
            val tvList = AccessibilityUtil.findAllOnceByClazz(linearLayoutItem, Views.TextView)
            for (item in tvList.filter { it.text != null && !it.text.isNullOrBlank() }) {
                val text = item.text.toString()
                val itemMessage = WeworkMessageBean.ItemMessageBean(0, text)
                sb.append(text).append("\t")
                itemMessageList.add(itemMessage)
            }
            LogUtils.v(sb.toString())
        }
        //消息主体
        val relativeLayoutItem = AccessibilityUtil.findOnceByClazz(node, Views.RelativeLayout, limitDepth = 1)
        if (relativeLayoutItem != null && relativeLayoutItem.childCount >= 2) {
            if (Views.ImageView.equals(relativeLayoutItem.getChild(0).className)) {
                LogUtils.v("头像在左边 本条消息发送者为其他联系人")
                nameList.addAll(WeworkTextUtil.getNameList(node))
                var textType = WeworkMessageBean.TEXT_TYPE_UNKNOWN
                val relativeLayoutContent =
                    AccessibilityUtil.findOnceByClazz(relativeLayoutItem, Views.RelativeLayout, limitDepth = 2)
                if (relativeLayoutContent != null) {
                    textType = WeworkTextUtil.getTextType(relativeLayoutContent)
                    LogUtils.v("textType: $textType")
                    val tvList =
                        AccessibilityUtil.findAllOnceByClazz(relativeLayoutContent, Views.TextView)
                    for (item in tvList.filter { it.text != null && !it.text.isNullOrBlank() }) {
                        val text = item.text.toString()
                        LogUtils.v(text)
                        if (text !in stopWords) {
                            val itemMessage = WeworkMessageBean.ItemMessageBean(2, text)
                            itemMessageList.add(itemMessage)
                        }
                    }
                }
                message = WeworkMessageBean.SubMessageBean(0, textType, itemMessageList, nameList)
            } else if (Views.ImageView.equals(relativeLayoutItem.getChild(1).className)) {
                LogUtils.v("头像在右边 本条消息发送者为自己")
                val subLayout = relativeLayoutItem.getChild(0)
                if (subLayout.childCount > 0) {
                    val tvList = AccessibilityUtil.findAllOnceByClazz(
                        subLayout.getChild(subLayout.childCount - 1),
                        Views.TextView
                    )
                    for (item in tvList.filter { it.text != null && !it.text.isNullOrBlank() }) {
                        val text = item.text.toString()
                        LogUtils.v(text)
                        if (text !in stopWords) {
                            val itemMessage = WeworkMessageBean.ItemMessageBean(2, text)
                            itemMessageList.add(itemMessage)
                        }
                    }
                }
                message = WeworkMessageBean.SubMessageBean(1, 0, itemMessageList, nameList)
            } else {
                // 没有头像的消息（撤销消息、其他可能的系统消息）
                val tvList = AccessibilityUtil.findAllOnceByClazz(node, Views.TextView)
                for (item in tvList.filter { it.text != null && !it.text.isNullOrBlank() }) {
                    val text = item.text.toString()
                    LogUtils.v(text)
                    val itemMessage = WeworkMessageBean.ItemMessageBean(1, text)
                    itemMessageList.add(itemMessage)
                }
                message = WeworkMessageBean.SubMessageBean(2, 0, itemMessageList, nameList)
                LogUtils.e("消息解析异常 未知异常")
            }
        } else {
            // 没有头像的消息（撤销消息、其他可能的系统消息）
            val sb = StringBuilder("未发现头像 本条消息发送者未知")
            val tvList = AccessibilityUtil.findAllOnceByClazz(node, Views.TextView)
            for (item in tvList.filter { it.text != null && !it.text.isNullOrBlank() }) {
                val text = item.text.toString()
                sb.append(text).append("/t")
                val itemMessage = WeworkMessageBean.ItemMessageBean(0, text)
                if (itemMessageList.count { it.feature == 0 && it.text == text } == 0) {
                    itemMessageList.add(itemMessage)
                }
            }
            LogUtils.v(sb.toString())
            message = WeworkMessageBean.SubMessageBean(2, 0, itemMessageList, nameList)
        }
        return message
    }

}