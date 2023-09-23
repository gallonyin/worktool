package org.yameida.worktool.utils

import android.view.accessibility.AccessibilityNodeInfo
import org.yameida.worktool.utils.AccessibilityUtil.findOneByClazz
import org.yameida.worktool.utils.AccessibilityUtil.findFrontNode
import org.yameida.worktool.model.WeworkMessageBean
import com.blankj.utilcode.util.LogUtils
import org.yameida.worktool.Constant
import org.yameida.worktool.observer.MultiFileObserver
import org.yameida.worktool.service.*
import org.yameida.worktool.utils.AccessibilityUtil.findAllOnceByClazz

/**
 * 房间特征分析工具类
 */
object WeworkRoomUtil {

    /**
     * 房间类型 ROOM_TYPE
     * @see WeworkMessageBean.ROOM_TYPE_UNKNOWN
     * @see WeworkMessageBean.ROOM_TYPE_EXTERNAL_GROUP
     * @see WeworkMessageBean.ROOM_TYPE_EXTERNAL_CONTACT
     * @see WeworkMessageBean.ROOM_TYPE_INTERNAL_GROUP
     * @see WeworkMessageBean.ROOM_TYPE_INTERNAL_CONTACT
     */
    fun getRoomType(print: Boolean = true): Int {
        val roomTitle = getRoomTitle(noCut = true, print = false)
        when {
            isExternalSingleChat(roomTitle) -> {
                LogUtils.d("ROOM_TYPE: ROOM_TYPE_EXTERNAL_CONTACT")
                return WeworkMessageBean.ROOM_TYPE_EXTERNAL_CONTACT
            }
            isExternalGroup() -> {
                LogUtils.d("ROOM_TYPE: ROOM_TYPE_EXTERNAL_GROUP")
                return WeworkMessageBean.ROOM_TYPE_EXTERNAL_GROUP
            }
            isGroupChat(roomTitle) -> {
                LogUtils.d("ROOM_TYPE: ROOM_TYPE_INTERNAL_GROUP")
                return WeworkMessageBean.ROOM_TYPE_INTERNAL_GROUP
            }
            isSingleChat() -> {
                LogUtils.d("ROOM_TYPE: ROOM_TYPE_INTERNAL_CONTACT")
                return WeworkMessageBean.ROOM_TYPE_INTERNAL_CONTACT
            }
            else -> {
                if (print) LogUtils.d("ROOM_TYPE: ROOM_TYPE_UNKNOWN")
                return WeworkMessageBean.ROOM_TYPE_UNKNOWN
            }
        }
    }

    /**
     * 房间标题
     */
    fun getRoomTitle(print: Boolean = true, noCut: Boolean = false): ArrayList<String> {
        val titleList = arrayListOf<String>()
        //聊天消息列表 1ListView 0RecycleView xViewGroup
        val list = AccessibilityUtil.findOnceByClazz(getRoot(), Views.ListView)
        if (list != null) {
            val frontNode = findFrontNode(list.parent?.parent)
            val textViewList = findAllOnceByClazz(frontNode, Views.TextView)
            for (textView in textViewList) {
                if (!textView.text.isNullOrBlank()) {
                    val text = textView.text.toString()
                    titleList.add(text.replace(Constant.digitalRegex, ""))
                    if (noCut && text.contains(Constant.digitalRegex)) {
                            titleList.add(text)
                    }
                }
            }
        }
        if (print) LogUtils.v("getRoomTitle: ", titleList)
        return titleList
    }

    /**
     * 进入房间（单聊或群聊）
     */
    fun intoRoom(title: String, fastIn: Boolean = true): Boolean {
        if (checkRoom(title, strict = true)) {
            return true
        }
        goHome()
        val list = findOneByClazz(getRoot(), Views.RecyclerView, Views.ListView, Views.ViewGroup)
        if (fastIn) {
            if (list != null && list.childCount >= 2) {
                for (i in 0 until list.childCount) {
                    val item = list.getChild(i)
                    val tvList = findAllOnceByClazz(item, Views.TextView).mapNotNull { it.text }
                    if (tvList.isNotEmpty() && title == tvList[0].toString()) {
                        intoRoomPreInit()
                        AccessibilityUtil.performClick(item)
                        LogUtils.d("快捷进入房间: $title")
                        AccessibilityUtil.waitForPageMissing("WwMainActivity", "GlobalSearchActivity")
                        AccessibilityExtraUtil.loadingPage("ExternalGroupMessageListActivity", "ExternalWechatUserMessageListActivity", "MessageListActivity", timeout = Constant.CHANGE_PAGE_INTERVAL)
                        return checkRoom(title, strict = false)
                    }
                }
            }
        }
        if (list != null) {
            val frontNode = findFrontNode(list)
            val textViewList = findAllOnceByClazz(frontNode, Views.TextView)
            if (textViewList.size >= 2) {
                val searchButton: AccessibilityNodeInfo = textViewList[textViewList.size - 2]
                val multiButton: AccessibilityNodeInfo = textViewList[textViewList.size - 1]
                AccessibilityUtil.performClick(searchButton)
                val needTrim = title.contains(Constant.regTrimTitle)
                val trimTitle = title.replace(Constant.regTrimTitle, "")
                AccessibilityUtil.findTextInput(getRoot(), trimTitle)
                if (!AccessibilityExtraUtil.loadingPage("GlobalSearchActivity")) {
                    LogUtils.e("未找到搜索页")
                    return false
                }
                //消息页搜索结果列表
                val selectListView = findOneByClazz(getRoot(), Views.ListView)
                val reverseRegexTitle = RegexHelper.reverseRegexTitle(trimTitle)
                val regex1 = (if (Constant.friendRemarkStrict) "^$reverseRegexTitle" else "^(微信昵称:)|((企业)?邮箱:)?$reverseRegexTitle") +
                        (if (needTrim) ".*?" else Constant.suffixString)
                val regex2 = ".*?\\($reverseRegexTitle\\)$"
                val regex = "($regex1)|($regex2)"
                val searchResult = AccessibilityUtil.findAllByTextRegex(
                    selectListView,
                    regex,
                    timeout = Constant.CHANGE_PAGE_INTERVAL * 3,
                    root = false
                )
                if (searchResult.isNotEmpty()) {
                    //过滤已退出的群聊
                    val searchItem = searchResult.firstOrNull {
                        it.parent != null && it.parent.childCount < 3
                    }
                    if (searchItem != null) {
                        intoRoomPreInit()
                        AccessibilityUtil.performClick(searchItem)
                        val text = AccessibilityUtil.findAllOnceByClazz(searchItem.parent, Views.TextView).firstOrNull { it.text != null && !it.text.isNullOrBlank() }
                        val title = text?.text?.toString() ?: title
                        LogUtils.d("进入房间: $title")
                        AccessibilityUtil.waitForPageMissing("WwMainActivity", "GlobalSearchActivity")
                        AccessibilityExtraUtil.loadingPage("ExternalGroupMessageListActivity", "ExternalWechatUserMessageListActivity", "MessageListActivity", timeout = Constant.CHANGE_PAGE_INTERVAL)
                        return checkRoom(title, strict = false)
                    } else {
                        LogUtils.e("搜索到已退出群聊")
                    }
                } else {
                    LogUtils.e("未搜索到结果")
                    val noResult = AccessibilityUtil.findOnceByText(getRoot(), "无搜索结果", exact = true) != null
                    LogUtils.e("企微: 无搜索结果: $noResult")
                }
            } else {
                LogUtils.e("未找到搜索按钮")
            }
        } else {
            LogUtils.e("未找到聊天列表")
        }
        return false
    }

    /**
     * 进入群管理页
     * @return true 成功进入群管理页
     */
    fun intoGroupManager(): Boolean {
        if (AccessibilityUtil.findOneByText(getRoot(), "全部群成员", "微信用户创建", timeout = Constant.CHANGE_PAGE_INTERVAL) != null) {
            return true
        }
        //群详情列表
        val list = findOneByClazz(getRoot(), Views.ListView)
        if (list != null) {
            val frontNode = AccessibilityUtil.findFrontNode(list.parent?.parent)
            val textViewList = findAllOnceByClazz(frontNode, Views.TextView)
            if (textViewList.size >= 2) {
                val multiButton = textViewList.lastOrNull()
                AccessibilityUtil.performClick(multiButton)
                sleep(Constant.CHANGE_PAGE_INTERVAL)
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
        if (AccessibilityUtil.findOneByText(getRoot(), "设置聊天背景", timeout = Constant.CHANGE_PAGE_INTERVAL) != null) {
            return true
        }
        //同群详情列表
        val list = findOneByClazz(getRoot(), Views.ListView)
        if (list != null) {
            val frontNode = AccessibilityUtil.findFrontNode(list.parent?.parent)
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
            if (AccessibilityUtil.findOneByText(getRoot(), "设置聊天背景", timeout = Constant.CHANGE_PAGE_INTERVAL) != null) {
                LogUtils.e("获取好友名称失败 校验未进入好友详情页")
                return titleList
            }
            val gridView = AccessibilityUtil.findOnceByClazz(getRoot(), Views.GridView)
            if (gridView != null && gridView.childCount >= 2) {
                LogUtils.i("获取好友名称 使用GridView")
                val tvList = findAllOnceByClazz(gridView.getChild(0), Views.TextView)
                for (textView in tvList) {
                    if (textView.text != null) {
                        titleList.add(textView.text.toString())
                    }
                }
            } else {
                LogUtils.i("获取好友名称 使用RecyclerView")
                val recyclerViewList = AccessibilityUtil.findAllOnceByClazz(getRoot(), Views.RecyclerView)
                if (recyclerViewList.size >= 2 && recyclerViewList[1].childCount >= 2) {
                    val rvList = recyclerViewList[1]
                    val tvList = findAllOnceByClazz(rvList.getChild(0), Views.TextView)
                    for (textView in tvList) {
                        if (textView.text != null) {
                            titleList.add(textView.text.toString())
                        }
                    }
                }
            }
            backPress()
            LogUtils.d("获取好友名称成功 ${titleList.joinToString()}")
        } else {
            LogUtils.e("获取好友名称失败")
        }
        return titleList
    }

    /**
     * 获取当前群名并返回房间
     * 解决title为对方正在输入中问题
     * @return name 单聊对方姓名
     */
    fun getGroupName(): ArrayList<String> {
        val titleList = arrayListOf<String>()
        if (intoGroupManager()) {
            val groupInfo = WeworkGetImpl.getGroupInfoDetail()
            titleList.add(groupInfo.groupName)
        }
        return titleList
    }

    /**
     * 群名是否存在
     */
    fun isGroupExists(groupName: String): Boolean {
        return intoRoom(groupName)
    }

    /**
     * 检查当前房间
     */
    private fun checkRoom(title: String, strict: Boolean = false): Boolean {
        LogUtils.d("checkRoom(): $title")
        val titleList = getRoomTitle(print = false)
        if (titleList.isEmpty()) {
            return false
        }
        val roomType = getRoomType()
        val dealTitle = title.replace(Constant.suffixRegex, "")
        LogUtils.v("dealTitle: $dealTitle", "titleList: ${titleList.joinToString()}")
        if (roomType != WeworkMessageBean.ROOM_TYPE_UNKNOWN
            && (titleList.count { dealTitle == it.replace(Constant.suffixRegex, "") } > 0
                    || (!strict && titleList.count { dealTitle.contains(it.replace(Constant.suffixRegex, "")) } > 0))
        ) {
            intoRoomPreInit()
            LogUtils.d("当前正在房间")
            return true
        }
        return false
    }

    /**
     * 是否是群聊
     * 群名最后有(\d)显示群人数
     */
    private fun isGroupChat(roomTitle: ArrayList<String>): Boolean {
        return roomTitle.size > 1 && roomTitle[1].contains(Constant.digitalRegex)
    }

    /**
     * 是否是外部群
     * listview前兄弟控件 && text包含外部群
     */
    private fun isExternalGroup(): Boolean {
        //聊天消息列表 1ListView 0RecycleView xViewGroup
        val listView = AccessibilityUtil.findOnceByClazz(getRoot(), Views.ListView, limitDepth = null, depth = 0)
        if (listView != null) {
            val frontNode = findFrontNode(listView)
            if (frontNode != null) {
                val nodeList = AccessibilityUtil.findAllOnceByText(frontNode, "外部群")
                return nodeList.isNotEmpty()
            }
        }
        return false
    }

    /**
     * 是否是单聊
     * 有列表和输入框
     */
    private fun isSingleChat(): Boolean {
        //聊天消息列表 1ListView 0RecycleView xViewGroup
        val list = AccessibilityUtil.findOnceByClazz(getRoot(), Views.ListView)
        val editText = AccessibilityUtil.findOnceByClazz(getRoot(), Views.EditText)
            ?: AccessibilityUtil.findOnceByText(getRoot(), "按住 说话", "按住说话", exact = true)
        if (list != null && editText != null) {
            return true
        }
        return false
    }

    /**
     * 是否是外部单聊
     * 姓名下面有@xx
     */
    private fun isExternalSingleChat(roomTitle: ArrayList<String>): Boolean {
        return roomTitle.size > 1 && roomTitle.count { it.matches("^[@＠].*?".toRegex()) } > 0
    }

    /**
     * 初始化进入房间前的事件
     */
    private fun intoRoomPreInit(): Boolean {
        MultiFileObserver.createSet.clear()
        MultiFileObserver.finishSet.clear()
        return true
    }

}