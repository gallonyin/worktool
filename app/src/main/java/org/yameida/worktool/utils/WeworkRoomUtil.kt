package org.yameida.worktool.utils

import android.view.accessibility.AccessibilityNodeInfo
import org.yameida.worktool.utils.AccessibilityUtil.findOneByClazz
import org.yameida.worktool.utils.AccessibilityUtil.findFrontNode
import org.yameida.worktool.model.WeworkMessageBean
import com.blankj.utilcode.util.LogUtils
import org.yameida.worktool.service.backPress
import org.yameida.worktool.service.getRoot
import org.yameida.worktool.service.goHome
import org.yameida.worktool.service.sleep
import org.yameida.worktool.utils.AccessibilityUtil.findAllOnceByClazz

/**
 * 房间特征分析工具类
 */
object WeworkRoomUtil {

    /**
     * 房间类型 ROOM_TYPE
     * @see WeworkMessageBean.ROOM_TYPE
     */
    fun getRoomType(root: AccessibilityNodeInfo): Int {
        when {
            isExternalSingleChat(root) -> {
                LogUtils.d("ROOM_TYPE: ROOM_TYPE_EXTERNAL_CONTACT")
                return WeworkMessageBean.ROOM_TYPE_EXTERNAL_CONTACT
            }
            isGroupChat(root) -> {
                return if (isExternalGroup(root)) {
                    LogUtils.d("ROOM_TYPE: ROOM_TYPE_EXTERNAL_GROUP")
                    WeworkMessageBean.ROOM_TYPE_EXTERNAL_GROUP
                } else {
                    LogUtils.d("ROOM_TYPE: ROOM_TYPE_INTERNAL_GROUP")
                    WeworkMessageBean.ROOM_TYPE_INTERNAL_GROUP
                }
            }
            isSingleChat(root) -> {
                LogUtils.d("ROOM_TYPE: ROOM_TYPE_INTERNAL_CONTACT")
                return WeworkMessageBean.ROOM_TYPE_INTERNAL_CONTACT
            }
            else -> {
                LogUtils.d("ROOM_TYPE: ROOM_TYPE_UNKNOWN")
                return WeworkMessageBean.ROOM_TYPE_UNKNOWN
            }
        }
    }

    /**
     * 房间类型
     * @see WeworkMessageBean.ROOM_TYPE_UNKNOWN
     * @see WeworkMessageBean.ROOM_TYPE_EXTERNAL_GROUP
     * @see WeworkMessageBean.ROOM_TYPE_EXTERNAL_CONTACT
     * @see WeworkMessageBean.ROOM_TYPE_INTERNAL_GROUP
     * @see WeworkMessageBean.ROOM_TYPE_INTERNAL_CONTACT
     */
    fun getRoomTitle(root: AccessibilityNodeInfo): ArrayList<String> {
        val titleList = arrayListOf<String>()
        val list = AccessibilityUtil.findOnceByClazz(root, Views.ListView)
        if (list != null) {
            val frontNode = findFrontNode(list.parent.parent)
            val textViewList = findAllOnceByClazz(frontNode, Views.TextView)
            for (textView in textViewList) {
                if (!textView.text.isNullOrBlank()) {
                    titleList.add(textView.text.toString().replace("\\(\\d+\\)$".toRegex(), ""))
                }
            }
        }
        LogUtils.d("getRoomTitle: ", titleList)
        return titleList
    }

    /**
     * 进入房间（单聊或群聊）
     */
    fun intoRoom(title: String): Boolean {
        LogUtils.d("intoRoom(): $title")
        val titleList = getRoomTitle(getRoot())
        val roomType = getRoomType(getRoot())
        if (roomType != WeworkMessageBean.ROOM_TYPE_UNKNOWN
            && titleList.count {
                it.replace("…", "").replace("\\(.*?\\)".toRegex(), "") == title.replace("…", "")
                    .replace("\\(.*?\\)".toRegex(), "")
            } > 0
        ) {
            LogUtils.d("当前正在房间")
            return true
        }
        goHome()
        val list = findOneByClazz(getRoot(), Views.ListView)
        if (list != null) {
            val frontNode = findFrontNode(list)
            val textViewList = findAllOnceByClazz(frontNode, Views.TextView)
            if (textViewList.size >= 2) {
                val searchButton: AccessibilityNodeInfo = textViewList[textViewList.size - 2]
                val multiButton: AccessibilityNodeInfo = textViewList[textViewList.size - 1]
                AccessibilityUtil.performClick(searchButton)
                AccessibilityUtil.findTextInput(getRoot(), title.replace("…", ""))
                sleep(1000)
                val selectListView = findOneByClazz(getRoot(), Views.ListView)
                val imageView = AccessibilityUtil.findOnceByClazz(selectListView, Views.ImageView)
                AccessibilityUtil.performClick(imageView)
                sleep(1000)
                return true
            } else {
                LogUtils.e("未找到搜索按钮")
            }
        }
        LogUtils.e("未找到聊天列表")
        return false
    }

    /**
     * 进入群管理页
     * @return true 成功进入群管理页
     */
    fun intoGroupManager(): Boolean {
        if (AccessibilityUtil.findOnceByText(getRoot(), "全部群成员") != null
            || AccessibilityUtil.findOnceByText(getRoot(), "微信用户创建") != null) {
            return true
        }
        val list = findOneByClazz(getRoot(), Views.ListView)
        if (list != null) {
            val frontNode = AccessibilityUtil.findFrontNode(list.parent.parent)
            val textViewList = findAllOnceByClazz(frontNode, Views.TextView)
            if (textViewList.size >= 2) {
                val multiButton = textViewList.lastOrNull()
                AccessibilityUtil.performClick(multiButton)
                return true
            } else {
                LogUtils.e("未找到群管理按钮")
            }
        }
        return false
    }

    /**
     * 进入好友详情页
     * @return true 成功进入好友详情页
     */
    fun intoFriendDetail(): Boolean {
        if (AccessibilityUtil.findOneByText(getRoot(), "设置聊天背景") != null) {
            return true
        }
        val list = findOneByClazz(getRoot(), Views.ListView)
        if (list != null) {
            val frontNode = AccessibilityUtil.findFrontNode(list.parent.parent)
            val textViewList = findAllOnceByClazz(frontNode, Views.TextView)
            if (textViewList.size >= 2) {
                val multiButton = textViewList.lastOrNull()
                AccessibilityUtil.performClick(multiButton)
                return true
            } else {
                LogUtils.e("未找到好友详情按钮")
            }
        }
        return false
    }

    /**
     * 获取当前聊天人姓名并返回房间
     * 解决title为对方正在输入中问题
     * @return name 单聊对方姓名
     */
    fun getFriendName(): ArrayList<String> {
        val titleList = arrayListOf<String>()
        if (intoFriendDetail()) {
            val gridView = findOneByClazz(getRoot(), Views.GridView)
            if (gridView != null && gridView.childCount >= 2) {
                val tvList = findAllOnceByClazz(gridView.getChild(0), Views.TextView)
                for (textView in tvList) {
                    if (textView.text != null) {
                        titleList.add(textView.text.toString())
                        backPress()
                    }
                }
            }
        }
        return titleList
    }

    /**
     * 是否是群聊
     * 群右上角有两个按钮（快速会议按钮、更多按钮）
     */
    private fun isGroupChat(root: AccessibilityNodeInfo): Boolean {
        val list = AccessibilityUtil.findOnceByClazz(root, Views.ListView)
        if (list != null) {
            val frontNode = findFrontNode(list.parent.parent)
            val textViewList = findAllOnceByClazz(frontNode, Views.TextView)
            if (textViewList.size >= 2) {
                val buttonList = findAllOnceByClazz(textViewList.last().parent.parent, Views.TextView)
                return buttonList.size == 2
            } else {
                LogUtils.d("未找到群管理按钮")
            }
        } else {
            LogUtils.d("未找到消息列表")
        }
        return false
    }

    /**
     * 是否是外部群
     * listview前兄弟控件 && text包含外部群
     */
    private fun isExternalGroup(root: AccessibilityNodeInfo): Boolean {
        val listView = AccessibilityUtil.findOnceByClazz(root, Views.ListView, null, 0)
        if (listView != null) {
            val frontNode = findFrontNode(listView)
            if (frontNode != null) {
                val nodeList = AccessibilityUtil.findAllByText(frontNode, "外部群", timeout = 0)
                return nodeList.isNotEmpty()
            }
        }
        return false
    }

    /**
     * 是否是单聊
     * 有列表和输入框
     */
    private fun isSingleChat(root: AccessibilityNodeInfo): Boolean {
        val list = AccessibilityUtil.findOnceByClazz(root, Views.ListView)
        val editText = AccessibilityUtil.findOnceByClazz(root, Views.EditText)
        if (list != null && editText != null) {
            return true
        }
        return false
    }

    /**
     * 是否是外部单聊
     * 姓名下面有@xx
     */
    private fun isExternalSingleChat(root: AccessibilityNodeInfo): Boolean {
        val roomTitle = getRoomTitle(root)
        return roomTitle.size > 1 && roomTitle.count { it.matches("^[@＠].*?".toRegex()) } > 0
    }

}