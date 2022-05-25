package org.yameida.worktool.service

import android.view.accessibility.AccessibilityNodeInfo
import com.blankj.utilcode.util.LogUtils
import org.yameida.worktool.model.WeworkMessageBean
import org.yameida.worktool.utils.AccessibilityUtil
import org.yameida.worktool.utils.Views
import org.yameida.worktool.utils.WeworkRoomUtil
import org.yameida.worktool.utils.WeworkTextUtil

/**
 * 全局操作类型 200 实现类
 */
object WeworkOperationImpl {

    /**
     * 在房间内发送消息
     * @param titleList 房间名称
     * @param receivedContent 回复内容
     * @see WeworkMessageBean.TEXT_TYPE
     */
    fun sendMessage(titleList: List<String>, receivedContent: String): Boolean {
        for (title in titleList) {
            if (WeworkRoomUtil.intoRoom(title)) {
                sendChatMessage(receivedContent)
                LogUtils.d("$title: 发送成功")
            } else {
                LogUtils.d("$title: 发送失败")
                error("$title: 发送失败 $receivedContent")
            }
        }
        return true
    }

    /**
     * 在房间内指定回复消息
     * @param titleList 房间名称
     * @param receivedName 原始消息的发送者姓名
     * @param originalContent 原始消息的内容
     * @param textType 原始消息的消息类型
     * @param receivedContent 回复内容
     * @see WeworkMessageBean.TEXT_TYPE
     */
    fun replyMessage(
        titleList: List<String>,
        receivedName: String,
        originalContent: String,
        textType: Int,
        receivedContent: String
    ): Boolean {
        for (title in titleList) {
            if (WeworkRoomUtil.intoRoom(title)) {
                if (WeworkTextUtil.longClickMessageItem(
                        AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView),
                        textType,
                        receivedName,
                        originalContent,
                        "回复"
                    )
                ) {
                    LogUtils.d("开始回复")
                    sleep(1000)
                    sendChatMessage(receivedContent, "[自动回复]")
                    LogUtils.d("$title: 回复成功")
                    return true
                } else {
                    LogUtils.d("$title: 回复失败")
                    error("$title: 回复失败 $receivedContent")
                }
            } else {
                LogUtils.d("$title: 回复失败")
                error("$title: 回复失败 $receivedContent")
            }
        }
        return false
    }

    /**
     * 在房间内转发消息
     * @param titleList 房间名称
     * @param receivedName 原始消息的发送者姓名
     * @param originalContent 原始消息的内容
     * @param textType 原始消息的消息类型
     * @param nameList 待转发姓名列表
     * @param extraText 附加留言 选填
     * @see WeworkMessageBean.TEXT_TYPE
     */
    fun relayMessage(
        titleList: List<String>,
        receivedName: String,
        originalContent: String,
        textType: Int,
        nameList: List<String>,
        extraText: String? = null
    ): Boolean {
        for (title in titleList) {
            if (WeworkRoomUtil.intoRoom(title)) {
                if (WeworkTextUtil.longClickMessageItem(
                        AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView),
                        textType,
                        receivedName,
                        originalContent,
                        "转发"
                    )
                ) {
                    LogUtils.d("开始转发")
                    sleep(1000)
                    relaySelectTarget(nameList, extraText)
                }
                LogUtils.d("$title: 转发成功")
            } else {
                LogUtils.d("$title: 转发失败")
                error("$title: 转发失败 $originalContent")
            }
        }
        return true
    }

    /**
     * 初始化群设置
     * 1.修改群名
     * 2.添加群成员 默认为空
     * 3.修改群公告 默认为空
     * 4.成员改群名 默认禁止
     * 5.私自邀请 默认开启
     * 6.设置群管理员 延迟开发
     * 7.设置入群欢迎语 默认为空
     * 8.拉入机器人 暂不开发
     * 9.防骚扰 默认警告模式
     * 10.使用群配置模板 延迟开发
     * 11.消息免打扰 默认禁止
     * 12.保存到通讯录 默认开启
     * 注：群配置模板 1.群名称 2.禁群改名(使用) 3.设置管理员 4.入群欢迎语(使用) 5.自动回复 6.防骚扰规则(使用)
     * 必须人工给机器人预先设置防骚扰规则 规则名"机器人"
     * 必须人工给机器人预先设置群配置模板 模板名"机器人"
     * @param groupName 修改群名称
     * @param selectList 添加群成员名称列表 选填
     * @param groupAnnouncement 修改群公告 选填
     */
    fun initGroup(
        groupName: String,
        selectList: List<String>?,
        groupAnnouncement: String?
    ): Boolean {
        if (!createGroup() || !groupRename(groupName) || !groupAddMember(selectList)
            || !groupChangeAnnouncement(groupAnnouncement)
        ) return false
        backPress()
        return true
    }

    /**
     * 进入群聊并修改群配置
     * 群名称、群公告、拉人、踢人
     * @param groupName 待修改的群
     * @param newGroupName 修改群名 选填
     * @param newGroupAnnouncement 修改群公告 选填
     * @param selectList 添加群成员名称列表/拉人 选填
     * @param showMessageHistory 拉人是否附带历史记录 选填
     * @param removeList 移除群成员名称列表/踢人 选填
     */
    fun intoGroupAndConfig(
        groupName: String,
        newGroupName: String?,
        newGroupAnnouncement: String?,
        selectList: List<String>?,
        showMessageHistory: Boolean = false,
        removeList: List<String>?
    ): Boolean {
        if (WeworkRoomUtil.intoRoom(groupName)) {
            if (newGroupName != null) {
                groupRename(newGroupName)
            }
            if (selectList != null) {
                groupAddMember(selectList, showMessageHistory)
            }
            if (removeList != null) {
                groupRemoveMember(removeList)
            }
            if (newGroupAnnouncement != null) {
                groupChangeAnnouncement(newGroupAnnouncement)
            }
            backPress()
            return true
        }
        return false
    }

    /**
     * 推送微盘图片
     * @param titleList 待发送姓名列表
     * @param objectName 图片名称
     * @param extraText  附加留言 可选
     */
    fun pushMicroDiskImage(
        titleList: List<String>,
        objectName: String,
        extraText: String? = null
    ): Boolean {
        goHomeTab("工作台")
        val node = AccessibilityUtil.scrollAndFindByText(getRoot(), "微盘")
        if (node != null) {
            AccessibilityUtil.performClick(node)
            sleep(2000)
            val buttonList = AccessibilityUtil.findAllByClazz(getRoot(), Views.Button)
            if (buttonList.size >= 4) {
                AccessibilityUtil.performClick(buttonList[2])
                sleep(1000)
                AccessibilityUtil.findTextInput(getRoot(), objectName)
                sleep(2000)
                val editText = AccessibilityUtil.findOneByClazz(getRoot(), Views.EditText)
                val backNode = AccessibilityUtil.findBackNode(editText)
                val imageViewList = AccessibilityUtil.findAllByClazz(backNode, Views.ImageView)
                if (imageViewList.size >= 2) {
                    AccessibilityUtil.performClick(imageViewList[1])
                    sleep(2000)
                    val shareFileButton = AccessibilityUtil.findOneByDesc(getRoot(), "以原文件分享")
                    AccessibilityUtil.performClick(shareFileButton)
                    sleep(2000)
                    val shareToWorkButton = AccessibilityUtil.findOneByText(getRoot(true), "发送给同事")
                    AccessibilityUtil.performClick(shareToWorkButton)
                    sleep(2000)
                    relaySelectTarget(titleList, extraText)
                    sleep(2000)
                    val stayButton = AccessibilityUtil.findOneByText(getRoot(), "留在企业微信")
                    AccessibilityUtil.performClick(stayButton)
                    return true
                } else {
                    LogUtils.e("微盘未搜索到相关文件: $objectName")
                }
            } else {
                LogUtils.e("未找到微盘内搜索")
            }
        } else {
            LogUtils.e("未找到微盘")
        }
        return false
    }

    /**
     * 推送微盘文件
     * @param titleList 待发送姓名列表
     * @param objectName 文件名称
     * @param extraText  附加留言 可选
     */
    fun pushMicroDiskFile(
        titleList: List<String>,
        objectName: String,
        extraText: String? = null
    ): Boolean {
        goHomeTab("工作台")
        val node = AccessibilityUtil.scrollAndFindByText(getRoot(), "微盘")
        if (node != null) {
            AccessibilityUtil.performClick(node)
            sleep(2000)
            val buttonList = AccessibilityUtil.findAllByClazz(getRoot(), Views.Button)
            if (buttonList.size >= 4) {
                AccessibilityUtil.performClick(buttonList[2])
                sleep(1000)
                AccessibilityUtil.findTextInput(getRoot(), objectName)
                sleep(2000)
                val editText = AccessibilityUtil.findOneByClazz(getRoot(), Views.EditText)
                val backNode = AccessibilityUtil.findBackNode(editText)
                val imageViewList = AccessibilityUtil.findAllByClazz(backNode, Views.ImageView)
                if (imageViewList.size >= 2) {
                    AccessibilityUtil.performClick(imageViewList[1])
                    sleep(2000)
                    val shareFileButton = AccessibilityUtil.findOneByDesc(getRoot(), "转发")
                    AccessibilityUtil.performClick(shareFileButton)
                    sleep(2000)
                    relaySelectTarget(titleList, extraText)
                    sleep(2000)
                    return true
                } else {
                    LogUtils.e("微盘未搜索到相关文件: $objectName")
                }
            } else {
                LogUtils.e("未找到微盘内搜索")
            }
        } else {
            LogUtils.e("未找到微盘")
        }
        return false
    }

    /**
     * 推送任意小程序
     * @param titleList 待发送姓名列表
     * @param objectName 小程序名称
     * @param extraText  附加留言 可选
     */
    fun pushMicroprogram(
        titleList: List<String>,
        objectName: String,
        extraText: String? = null
    ): Boolean {
        goHomeTab("工作台")
        val node = AccessibilityUtil.scrollAndFindByText(getRoot(), "用过的小程序")
        if (node != null) {
            AccessibilityUtil.performClick(node)
            sleep(2000)
            val textViewList = AccessibilityUtil.findAllByClazz(getRoot(), Views.TextView)
            if (textViewList.size > 3) {
                AccessibilityUtil.performClick(textViewList[2])
                sleep(1000)
                AccessibilityUtil.findTextInput(getRoot(), objectName)
                sleep(2000)
                AccessibilityUtil.findListOneAndClick(getRoot(), 1)
                sleep(2000)
                //todo 转发小程序
                return true
            } else {
                LogUtils.e("未找到小程序内搜索")
            }
        } else {
            LogUtils.e("未找到小程序")
        }
        return false
    }

    /**
     * 推送腾讯文档
     * @param titleList 待发送姓名列表
     * @param objectName 小程序名称
     * @param extraText  附加留言 可选
     */
    fun pushOffice(
        titleList: List<String>,
        objectName: String,
        extraText: String? = null
    ): Boolean {
        goHomeTab("文档")
        val allButton = AccessibilityUtil.findOneByText(getRoot(), "全部")
        if (allButton == null) {
            LogUtils.e("未找到全部按钮")
            return false
        }
        AccessibilityUtil.performClick(allButton)
        sleep(1000)
        val myFileButton = AccessibilityUtil.findOneByText(getRoot(), "共享空间")
        if (myFileButton == null) {
            LogUtils.e("未找到共享空间按钮")
            return false
        }
        AccessibilityUtil.performClick(myFileButton)
        sleep(2000)
        val buttonList = AccessibilityUtil.findAllByClazz(getRoot(), Views.Button)
        if (buttonList.size >= 4) {
            AccessibilityUtil.performClick(buttonList[3])
            sleep(1000)
            AccessibilityUtil.findTextInput(getRoot(), objectName)
            sleep(2000)
            val editText = AccessibilityUtil.findOneByClazz(getRoot(), Views.EditText)
            val backNode = AccessibilityUtil.findBackNode(editText)
            val imageViewList = AccessibilityUtil.findAllByClazz(backNode, Views.ImageView)
            if (imageViewList.size >= 2) {
                AccessibilityUtil.performClick(imageViewList[1])
                sleep(2000)
                val shareFileButton = AccessibilityUtil.findOneByDesc(getRoot(), "转发")
                AccessibilityUtil.performClick(shareFileButton)
                sleep(2000)
                relaySelectTarget(titleList, extraText)
                sleep(2000)
                return true
            } else {
                LogUtils.e("文档未搜索到相关文件: $objectName")
            }
        } else {
            LogUtils.e("未找到文档搜索按钮")
        }
        return false
    }

    /**
     * 展示群信息
     * @see WeworkMessageBean.SHOW_GROUP_INFO
     * @param titleList 待查询群名
     * @param receivedName 原始消息的发送者姓名
     * @param originalContent 原始消息的内容
     * @param textType 原始消息的消息类型
     */
    fun showGroupInfo(
        titleList: MutableList<String>,
        receivedName: String,
        originalContent: String,
        textType: Int
    ): Boolean {
        for (groupName in titleList) {
            if (WeworkRoomUtil.intoRoom(groupName) && WeworkRoomUtil.intoGroupManager()) {
                val groupInfo = WeworkGetImpl.getGroupInfoDetail()
                groupInfo.titleList = arrayListOf(groupName)
                groupInfo.type = WeworkMessageBean.SHOW_GROUP_INFO
                groupInfo.receivedName = receivedName
                groupInfo.originalContent = originalContent
                groupInfo.textType = textType
                WeworkController.weworkService.webSocketManager.send(groupInfo)
            }
        }
        return true
    }

    /**
     * 转发消息到目标列表
     * 支持场景：长按消息转发、微盘图片转发
     * selectList 昵称或群名列表
     * extraText 转发是否附加一条文本
     */
    private fun relaySelectTarget(selectList: List<String>, extraText: String? = null): Boolean {
        val list = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
        if (list != null) {
            val frontNode = AccessibilityUtil.findFrontNode(list)
            val textViewList = AccessibilityUtil.findAllByClazz(frontNode, Views.TextView)
            if (textViewList.size >= 2) {
                val searchButton: AccessibilityNodeInfo = textViewList[textViewList.size - 2]
                val multiButton: AccessibilityNodeInfo = textViewList[textViewList.size - 1]
                AccessibilityUtil.performClick(multiButton)
                sleep(1000)
                AccessibilityUtil.performClick(searchButton)
                sleep(1000)
                for (select in selectList) {
                    AccessibilityUtil.findTextInput(getRoot(), select)
                    sleep(2000)
                    val selectListView = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
                    val imageView =
                        AccessibilityUtil.findOneByClazz(selectListView, Views.ImageView)
                    if (imageView != null) {
                        AccessibilityUtil.performClick(imageView)
                    }
                    sleep(1000)
                }
                val confirmButton =
                    AccessibilityUtil.findOneByText(getRoot(), "确定(${selectList.size})")
                if (confirmButton != null) {
                    AccessibilityUtil.performClick(confirmButton)
                    sleep(1000)
                    if (!extraText.isNullOrBlank()) {
                        LogUtils.d("extraText: $extraText")
                        AccessibilityUtil.findTextInput(getRoot(), extraText)
                        sleep(1000)
                    }
                    val sendButtonList = getRoot().findAccessibilityNodeInfosByText("发送")
                    for (sendButton in sendButtonList.filter { it.text != null }) {
                        if (sendButton.text == "发送" || sendButton.text == "发送(${selectList.size})") {
                            AccessibilityUtil.performClick(sendButton)
                            return true
                        }
                    }
                    LogUtils.e("未发现发送按钮: ")
                    return false
                } else {
                    LogUtils.e("未发现确认按钮: ")
                    return false
                }
            } else {
                LogUtils.e("未发现搜索和多选按钮: ")
                return false
            }
        }
        LogUtils.e("未知错误: ")
        return false
    }

    /**
     * 创建一个外部群
     */
    private fun createGroup(): Boolean {
        goHomeTab("工作台")
        val textViewGroup = AccessibilityUtil.scrollAndFindByText(getRoot(), "客户群")
        if (AccessibilityUtil.performClick(textViewGroup)) {
            LogUtils.d("进入客户群应用")
            sleep(2000)
            val textView = AccessibilityUtil.findOneByText(getRoot(), "创建一个客户群")
            AccessibilityUtil.performClick(textView)
            sleep(3000)
            return true
        } else {
            LogUtils.d("未找到客户群应用")
            return false
        }
    }

    /**
     * 修改群名称
     */
    private fun groupRename(groupName: String): Boolean {
        if (WeworkRoomUtil.intoGroupManager()) {
            val textView =
                AccessibilityUtil.findOneByText(getRoot(), "由企业微信用户创建，可邀请微信用户进群", "该群由企业微信用户创建")
            val button = AccessibilityUtil.findFrontNode(textView)
            if (button != null) {
                AccessibilityUtil.performClick(button)
                sleep(1000)
                AccessibilityUtil.findTextInput(getRoot(), groupName)
                val confirmButton = AccessibilityUtil.findOneByText(getRoot(), "确定")
                AccessibilityUtil.performClick(confirmButton)
                sleep(2000)
                return true
            } else {
                LogUtils.e("未找到填写群名按钮")
                return false
            }
        }
        return false
    }

    /**
     * 添加群成员/拉人
     * 默认不附带历史记录
     */
    private fun groupAddMember(
        selectList: List<String>? = null,
        showMessageHistory: Boolean = false
    ): Boolean {
        if (selectList.isNullOrEmpty()) return true
        if (WeworkRoomUtil.intoGroupManager()) {
            val gridView = AccessibilityUtil.findOneByClazz(getRoot(), Views.GridView)
            if (gridView != null && gridView.childCount >= 2) {
                if (gridView.childCount == 2) {
                    AccessibilityUtil.performClick(gridView.getChild(gridView.childCount - 1))
                } else {
                    AccessibilityUtil.performClick(gridView.getChild(gridView.childCount - 2))
                }
                sleep(1000)
            } else {
                LogUtils.e("未找到添加成员按钮")
                return false
            }
            val list = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
            if (list != null) {
                val frontNode = AccessibilityUtil.findFrontNode(list)
                val textViewList = AccessibilityUtil.findAllByClazz(frontNode, Views.TextView)
                if (textViewList.size >= 2) {
                    val multiButton = textViewList.lastOrNull()
                    AccessibilityUtil.performClick(multiButton)
                    sleep(1000)
                    for (select in selectList) {
                        AccessibilityUtil.findTextInput(getRoot(), select)
                        sleep(2000)
                        val selectListView =
                            AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
                        val imageView =
                            AccessibilityUtil.findOneByClazz(selectListView, Views.ImageView)
                        if (imageView != null) {
                            AccessibilityUtil.performClick(imageView)
                        }
                        sleep(1000)
                    }
                    if (showMessageHistory) {
                        val button = AccessibilityUtil.findOneByText(getRoot(), "附带聊天记录")
                        if (button != null) AccessibilityUtil.performClick(button)
                    }
                    val confirmButton =
                        AccessibilityUtil.findOneByText(getRoot(), "确定(${selectList.size})")
                    if (confirmButton != null) {
                        AccessibilityUtil.performClick(confirmButton)
                        sleep(1000)
                    } else {
                        LogUtils.e("未发现确认按钮: ")
                        return false
                    }
                } else {
                    LogUtils.e("未找到搜索按钮")
                    return false
                }
            } else {
                LogUtils.e("未找到成员列表")
                return false
            }
        }
        return true
    }

    /**
     * 移除群成员/踢人
     */
    private fun groupRemoveMember(removeList: List<String>): Boolean {
        if (WeworkRoomUtil.intoGroupManager()) {
            val gridView = AccessibilityUtil.findOneByClazz(getRoot(), Views.GridView)
            if (gridView != null && gridView.childCount >= 2) {
                if (gridView.childCount == 2) {
                    return true
                } else {
                    AccessibilityUtil.performClick(gridView.getChild(gridView.childCount - 1))
                }
                sleep(1000)
            } else {
                LogUtils.e("未找到删除成员按钮")
                return false
            }
            val list = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
            if (list != null) {
                val frontNode = AccessibilityUtil.findFrontNode(list)
                val textViewList = AccessibilityUtil.findAllByClazz(frontNode, Views.TextView)
                if (textViewList.size >= 2) {
                    val multiButton = textViewList.lastOrNull()
                    AccessibilityUtil.performClick(multiButton)
                    sleep(1000)
                    for (select in removeList) {
                        AccessibilityUtil.findTextInput(getRoot(), select)
                        sleep(2000)
                        val selectListView =
                            AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
                        val imageView =
                            AccessibilityUtil.findOneByClazz(selectListView, Views.ImageView)
                        if (imageView != null) {
                            AccessibilityUtil.performClick(imageView)
                        }
                        sleep(1000)
                    }
                    val confirmButton =
                        AccessibilityUtil.findOneByText(getRoot(), "移出(${removeList.size})")
                    if (confirmButton != null) {
                        AccessibilityUtil.performClick(confirmButton)
                        sleep(1000)
                    } else {
                        LogUtils.e("未发现移出按钮: ")
                        return false
                    }
                } else {
                    LogUtils.e("未找到搜索按钮")
                    return false
                }
            } else {
                LogUtils.e("未找到成员列表")
                return false
            }
        }
        return true
    }

    /**
     * 修改群公告
     * 注：首次为发布 后续为编辑
     */
    private fun groupChangeAnnouncement(groupAnnouncement: String? = null): Boolean {
        if (groupAnnouncement == null) return true
        if (WeworkRoomUtil.intoGroupManager()) {
            val textView = AccessibilityUtil.findOneByText(getRoot(), "群公告")
            if (textView != null) {
                AccessibilityUtil.performClick(textView)
                sleep(1000)
                val editButton = AccessibilityUtil.findOneByText(getRoot(), "编辑")
                if (editButton != null) {
                    LogUtils.d("群公告编辑中: $groupAnnouncement")
                    AccessibilityUtil.performClick(editButton)
                    sleep(1000)
                }
                if (AccessibilityUtil.findTextInput(getRoot(), groupAnnouncement)) {
                    LogUtils.d("群公告发布中: $groupAnnouncement")
                    sleep(500)
                    val button = AccessibilityUtil.findOneByText(getRoot(), "发布")
                    AccessibilityUtil.performClick(button)
                    sleep(1000)
                    val publishButtonList = getRoot().findAccessibilityNodeInfosByText("发布")
                    if (publishButtonList.size >= 2) {
                        AccessibilityUtil.performClick(publishButtonList[1])
                    }
                    sleep(3000)
                } else {
                    LogUtils.e("无法进行群公告发布和编辑: ")
                    return false
                }
            } else {
                LogUtils.e("未找到群公告按钮")
                return false
            }
        }
        return true
    }

    /**
     * 发送消息
     */
    private fun sendChatMessage(text: String, prefix: String = "") {
        val editText = AccessibilityUtil.findOneByClazz(getRoot(), Views.EditText)
        if (editText != null) {
            AccessibilityUtil.editTextInput(editText, prefix + text)
            //输入完文字等待出现发送按钮
            sleep(500)
        } else {
            LogUtils.e("未找到输入框")
            error("未找到输入框")
        }
        var index = 0
        while (index++ < 5) {
            val buttonList = getRoot().findAccessibilityNodeInfosByText("发送")
            var sendButton: AccessibilityNodeInfo? = null
            for (button in buttonList) {
                if (button.className == Views.Button) {
                    sendButton = button
                }
            }
            if (sendButton != null) {
                LogUtils.i("发送消息: \n$text")
                log("发送消息: \n$text")
                AccessibilityUtil.performClick(sendButton)
                sleep(500)
                break
            } else {
                LogUtils.e("未找到发送按钮")
                error("未找到发送按钮")
            }
            sleep(500)
        }
    }

}