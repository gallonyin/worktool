package org.yameida.worktool.service

import android.view.accessibility.AccessibilityNodeInfo
import org.yameida.worktool.Constant
import org.yameida.worktool.model.WeworkMessageBean
import com.github.yoojia.qrcode.qrcode.QRCodeDecoder
import com.blankj.utilcode.util.*
import com.lzy.okgo.OkGo
import org.yameida.worktool.utils.*
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


/**
 * 全局操作类型 200 实现类
 */
object WeworkOperationImpl {

    /**
     * 在房间内发送消息
     * @param titleList 房间名称
     * @param receivedContent 回复内容
     * @param at 要at的昵称
     * @see WeworkMessageBean.TEXT_TYPE
     */
    fun sendMessage(
        titleList: List<String>,
        receivedContent: String?,
        at: String? = null,
        atList: List<String>? = null
    ): Boolean {
        if (receivedContent.isNullOrEmpty()) {
            LogUtils.d("未发现发送内容")
            return false
        }
        for (title in titleList) {
            if (WeworkRoomUtil.intoRoom(title)) {
                if (sendChatMessage(receivedContent, at = at, atList = atList)) {
                    LogUtils.d("$title: 发送成功")
                    return true
                } else {
                    LogUtils.d("$title: 发送失败")
                }
            } else {
                LogUtils.d("$title: 发送失败")
                error("$title: 发送失败 $receivedContent")
            }
        }
        return false
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
        receivedName: String?,
        originalContent: String,
        textType: Int,
        receivedContent: String?
    ): Boolean {
        if (receivedContent.isNullOrEmpty()) {
            LogUtils.d("未发现回复内容")
            return false
        }
        for (title in titleList) {
            if (WeworkRoomUtil.intoRoom(title)) {
                if (WeworkTextUtil.longClickMessageItem(
                        //聊天消息列表 1ListView 0RecycleView xViewGroup
                        AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView),
                        textType,
                        receivedName,
                        originalContent,
                        "回复"
                    )
                ) {
                    LogUtils.v("开始回复")
                    sendChatMessage(receivedContent, reply = true)
                    LogUtils.d("$title: 回复成功")
                    WeworkLoopImpl.getChatMessageList()
                    return true
                } else {
                    LogUtils.d("$title: 回复失败 直接发送答案")
                    error("$title: 回复失败 直接发送答案 $receivedContent")
                    if (originalContent.isNotEmpty()) {
                        if (receivedName == null) {
                            sendChatMessage("【$originalContent】\n$receivedContent")
                        } else {
                            sendChatMessage("【$originalContent】\n$receivedContent", receivedName)
                        }
                    } else {
                        if (receivedName == null) {
                            sendChatMessage(receivedContent)
                        } else {
                            sendChatMessage(receivedContent, receivedName)
                        }
                    }
                    WeworkLoopImpl.getChatMessageList()
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
                        //聊天消息列表 1ListView 0RecycleView xViewGroup
                        AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView),
                        textType,
                        receivedName,
                        originalContent,
                        "转发"
                    )
                ) {
                    LogUtils.d("开始转发")
                    sleep(1000)
                    if (relaySelectTarget(nameList, extraText)) {
                        LogUtils.d("$title: 转发成功")
                    } else {
                        LogUtils.d("$title: 转发失败")
                        error("$title: 转发失败 $originalContent")
                    }
                }
            } else {
                LogUtils.d("$title: 转发失败 未找到房间")
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
     * @param groupRemark 修改群备注 选填
     */
    fun initGroup(
        groupName: String,
        selectList: List<String>?,
        groupAnnouncement: String?,
        groupRemark: String?
    ): Boolean {
        if (!WeworkRoomUtil.isGroupExists(groupName)) {
            if (!createGroup() || !groupRename(groupName) || !groupAddMember(selectList)
                || !groupChangeAnnouncement(groupAnnouncement)
            ) return false
        } else {
            if (!groupRename(groupName) || !groupAddMember(selectList)
                || !groupChangeAnnouncement(groupAnnouncement)
            ) return false
        }
        if (groupRemark != null) {
            groupChangeRemark(groupRemark)
        }
//        getGroupQrcode(groupName, groupRemark)
        return true
    }

    /**
     * 进入群聊并修改群配置
     * 群名称、群公告、拉人、踢人
     * @param groupName 待修改的群
     * @param newGroupName 修改群名 选填
     * @param newGroupAnnouncement 修改群公告 选填
     * @param groupRemark 修改群备注 选填
     * @param selectList 添加群成员名称列表/拉人 选填
     * @param showMessageHistory 拉人是否附带历史记录 选填
     * @param removeList 移除群成员名称列表/踢人 选填
     */
    fun intoGroupAndConfig(
        groupName: String,
        newGroupName: String?,
        newGroupAnnouncement: String?,
        groupRemark: String?,
        selectList: List<String>?,
        showMessageHistory: Boolean = false,
        removeList: List<String>?
    ): Boolean {
        if (WeworkRoomUtil.intoRoom(groupName)) {
            if (newGroupName != null) {
                groupRename(newGroupName)
            }
            if (!selectList.isNullOrEmpty()) {
                groupAddMember(selectList, showMessageHistory)
            }
            if (!removeList.isNullOrEmpty()) {
                groupRemoveMember(removeList)
            }
            if (newGroupAnnouncement != null) {
                groupChangeAnnouncement(newGroupAnnouncement)
            }
            if (groupRemark != null) {
                groupChangeRemark(groupRemark)
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
        val node = AccessibilityUtil.scrollAndFindByText(WeworkController.weworkService, getRoot(), "微盘")
        if (node != null) {
            AccessibilityUtil.performClick(node)
            sleep(Constant.POP_WINDOW_INTERVAL)
            val buttonList = AccessibilityUtil.findAllByClazz(getRoot(), Views.Button)
            if (buttonList.size >= 4) {
                AccessibilityUtil.performClick(buttonList[2])
                AccessibilityUtil.findTextInput(getRoot(), objectName)
                val imageViewList = AccessibilityUtil.findAllByClazz(getRoot(), Views.ImageView)
                if (imageViewList.size >= 2) {
                    AccessibilityUtil.performClick(imageViewList[1])
                    val shareFileButton = AccessibilityUtil.findOneByDesc(getRoot(), "以原文件分享")
                    AccessibilityUtil.performClick(shareFileButton)
                    val shareToWorkButton = AccessibilityUtil.findOneByText(getRoot(true), "发送给同事")
                    AccessibilityUtil.performClick(shareToWorkButton)
                    if (relaySelectTarget(titleList, extraText)) {
                        val stayButton = AccessibilityUtil.findOneByText(getRoot(), "留在企业微信")
                        AccessibilityUtil.performClick(stayButton)
                        return true
                    } else {
                        LogUtils.e("微盘文件转发失败: $objectName")
                    }
                } else {
                    LogUtils.e("微盘未搜索到相关图片: $objectName")
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
        val node = AccessibilityUtil.scrollAndFindByText(WeworkController.weworkService, getRoot(), "微盘")
        if (node != null) {
            AccessibilityUtil.performClick(node)
            sleep(Constant.POP_WINDOW_INTERVAL)
            val buttonList = AccessibilityUtil.findAllByClazz(getRoot(), Views.Button)
            if (buttonList.size >= 4) {
                AccessibilityUtil.performClick(buttonList[2])
                AccessibilityUtil.findTextInput(getRoot(), objectName)
                val imageViewList = AccessibilityUtil.findAllByClazz(getRoot(), Views.ImageView)
                if (imageViewList.size >= 2) {
                    AccessibilityUtil.performClick(imageViewList[1])
                    val shareFileButton = AccessibilityUtil.findOneByDesc(getRoot(), "转发")
                    AccessibilityUtil.performClick(shareFileButton)
                    if (relaySelectTarget(titleList, extraText)) {
                        return true
                    } else {
                        LogUtils.e("微盘文件转发失败: $objectName")
                    }
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
        val node = AccessibilityUtil.scrollAndFindByText(WeworkController.weworkService, getRoot(), "用过的小程序")
        if (node != null) {
            AccessibilityUtil.performClick(node)
            sleep(Constant.CHANGE_PAGE_INTERVAL)
            val textViewList = AccessibilityUtil.findAllByClazz(getRoot(), Views.TextView)
            if (textViewList.size > 3) {
                AccessibilityUtil.performClick(textViewList[2])
                AccessibilityUtil.findTextInput(getRoot(), objectName)
                AccessibilityUtil.findOneByClazz(getRoot(), Views.RecyclerView)
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
        val myFileButton = AccessibilityUtil.findOneByText(getRoot(), "共享空间")
        if (myFileButton == null) {
            LogUtils.e("未找到共享空间按钮")
            return false
        }
        AccessibilityUtil.performClick(myFileButton)
        val buttonList = AccessibilityUtil.findAllByClazz(getRoot(), Views.Button)
        if (buttonList.size >= 4) {
            AccessibilityUtil.performClick(buttonList[3])
            AccessibilityUtil.findTextInput(getRoot(), objectName)
            val imageViewList = AccessibilityUtil.findAllByClazz(getRoot(), Views.ImageView)
            if (imageViewList.size >= 2) {
                AccessibilityUtil.performClick(imageViewList[1])
                val shareFileButton = AccessibilityUtil.findOneByDesc(getRoot(), "转发")
                AccessibilityUtil.performClick(shareFileButton)
                if (relaySelectTarget(titleList, extraText)) {
                    return true
                } else {
                    LogUtils.e("微盘文件转发失败: $objectName")
                }
            } else {
                LogUtils.e("文档未搜索到相关文件: $objectName")
            }
        } else {
            LogUtils.e("未找到文档搜索按钮")
        }
        return false
    }

    /**
     * 推送文件(网络图片视频和文件等)
     * @see WeworkMessageBean.PUSH_FILE
     * @param titleList 待发送姓名列表
     * @param objectName 文件名称
     * @param fileUrl 文件网络地址
     * @param fileType 文件类型
     * @param extraText 附加留言 可选
     */
    fun pushFile(
        titleList: List<String>,
        objectName: String,
        fileUrl: String,
        fileType: String,
        extraText: String? = null
    ): Boolean {
        LogUtils.i("下载开始 $fileUrl")
        val execute = OkGo.get<File>(fileUrl).execute()
        LogUtils.i("下载成功 $fileUrl")
        val body = execute.body()
        if (body != null) {
            val df = SimpleDateFormat("yyyy-MM-dd")
            val filePath = "${Utils.getApp().getExternalFilesDir("share")}/${df.format(Date())}/$objectName"
            val newFile = File(filePath)
            val create = FileUtils.createFileByDeleteOldFile(newFile)
            if (create && newFile.canWrite()) {
                newFile.writeBytes(body.bytes())
                LogUtils.i("文件存储本地成功 $filePath")
                ShareUtil.share("${if (fileType.isBlank()) "*" else fileType}/*", newFile)
                val shareToWorkButton = AccessibilityUtil.findOneByText(getRoot(true), "发送给同事")
                AccessibilityUtil.performClick(shareToWorkButton)
                if (relaySelectTarget(titleList, extraText)) {
                    val stayButton = AccessibilityUtil.findOneByText(getRoot(), "留在企业微信")
                    AccessibilityUtil.performClick(stayButton)
                    return true
                } else {
                    LogUtils.e("文件转发失败: $objectName")
                }
            } else {
                LogUtils.e("文件存储本地失败 $filePath")
                error("文件存储本地失败 $filePath")
            }
        }
        return false
    }

    /**
     * 手机号添加好友
     * @see WeworkMessageBean.ADD_FRIEND_BY_PHONE
     * @param friend 待添加用户列表
     */
    fun addFriendByPhone(
        friend: WeworkMessageBean.Friend
    ): Boolean {
        goHome()
        val list = AccessibilityUtil.findOneByClazz(getRoot(), Views.RecyclerView, Views.ListView, Views.ViewGroup)
        if (list != null) {
            val frontNode = AccessibilityUtil.findFrontNode(list)
            val textViewList = AccessibilityUtil.findAllOnceByClazz(frontNode, Views.TextView)
            if (textViewList.size >= 2) {
                val searchButton: AccessibilityNodeInfo = textViewList[textViewList.size - 2]
                val multiButton: AccessibilityNodeInfo = textViewList[textViewList.size - 1]
                AccessibilityUtil.performClick(multiButton)
                sleep(Constant.POP_WINDOW_INTERVAL)
                val list = AccessibilityUtil.findAllByClazz(getRoot(), Views.ListView).lastOrNull()
                if (list != null) {
                    val button = AccessibilityUtil.findOneByText(list, "添加客户", "添加居民", "加微信", "加学员", exact = true)
                    if (button != null) {
                        AccessibilityUtil.performClick(button)
                        sleep(Constant.POP_WINDOW_INTERVAL)
                        AccessibilityUtil.findTextAndClick(getRoot(), "搜索手机号添加")
                        AccessibilityUtil.findTextInput(getRoot(), friend.phone.trim())
                        if (AccessibilityUtil.findTextAndClick(getRoot(), "网络查找手机")) {
                            val bothUsedTv = AccessibilityUtil.findOneByText(getRoot(), "对方同时使用", "标签", "电话")
                            val bothUsedText = bothUsedTv?.text
                            if (bothUsedText != null && bothUsedText.contains("对方同时使用")) {
                                AccessibilityUtil.performClick(
                                    AccessibilityUtil.findOnceByClazz(
                                        AccessibilityUtil.findBackNode(bothUsedTv),
                                        Views.ImageView
                                    )
                                )
                            }
                            if (AccessibilityUtil.findOneByText(getRoot(), "标签", "电话") != null) {
                                var markTv = AccessibilityUtil.findOnceByText(getRoot(), "设置备注和描述", exact = true)
                                if (markTv == null) {
                                    markTv = AccessibilityUtil.findOnceByText(getRoot(), "企业", exact = true)
                                }
                                if (markTv == null) {
                                    markTv = AccessibilityUtil.findOnceByText(getRoot(), "描述", exact = true)
                                }
                                //设置备注
                                if (markTv != null && (friend.markName != null
                                            || friend.markCorp != null || friend.markExtra != null)
                                ) {
                                    AccessibilityUtil.performClick(markTv)
                                    val etList =
                                        AccessibilityUtil.findAllByClazz(getRoot(), Views.EditText, minSize = 5)
                                    if (etList.size >= 5) {
                                        if (friend.markName != null) {
                                            AccessibilityUtil.editTextInput(etList[0], friend.markName)
                                        }
                                        if (friend.markCorp != null) {
                                            AccessibilityUtil.editTextInput(etList[1], friend.markCorp)
                                        }
                                        if (friend.markExtra != null) {
                                            AccessibilityUtil.editTextInput(etList[4], friend.markExtra)
                                        }
                                    }
                                    AccessibilityUtil.findTextAndClick(getRoot(), "保存")
                                }
                                //设置标签
                                if (!friend.tagList.isNullOrEmpty()) {
                                    if (AccessibilityUtil.findTextAndClick(getRoot(), "标签")) {
                                        setFriendTags(friend.tagList)
                                    }
                                }
                                //添加联系人
                                val imageView =
                                    AccessibilityUtil.findOneByClazz(getRoot(), Views.ImageView)
                                if (imageView != null) {
                                    val textViewList = AccessibilityUtil.findAllOnceByClazz(
                                        imageView.parent,
                                        Views.TextView
                                    )
                                    val filter =
                                        textViewList.filter { it.text != null && it.text.toString() != "微信" }
                                    if (filter.isNotEmpty()) {
                                        val tvNick = filter[0]
                                        LogUtils.d("好友昵称或备注名: " + tvNick.text)
                                    }
                                }
                                if (AccessibilityUtil.findTextAndClick(getRoot(), "添加为联系人")) {
                                    LogUtils.d("准备发送好友邀请: " + friend.phone)
                                    if (!friend.leavingMsg.isNullOrEmpty()) {
                                        AccessibilityUtil.findTextInput(getRoot(), friend.leavingMsg)
                                    }
                                    if (AccessibilityUtil.findTextAndClick(getRoot(), "发送添加邀请", "发送申请")) {
                                        LogUtils.d("发送添加邀请成功: " + friend.phone)
                                    }
                                } else {
                                    if (AccessibilityUtil.findOnceByText(getRoot(), "发消息", exact = true) != null) {
                                        LogUtils.e("已经添加联系人，请勿重复添加")
                                    } else {
                                        LogUtils.e("未找到添加为联系人")
                                    }
                                }
                            } else {
                                LogUtils.e("未找到标签")
                            }
                        } else {
                            LogUtils.e("未找到查找手机选项")
                        }
                    } else {
                        LogUtils.e("未找到添加客户按钮")
                    }
                } else {
                    LogUtils.e("未找到添加客户列表")
                }
                return true
            } else {
                LogUtils.e("未找到搜索按钮")
            }
        }
        LogUtils.e("未找到聊天列表")
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
        //聊天消息列表 1ListView 0RecycleView xViewGroup
        val list = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
        if (list != null) {
            val frontNode = AccessibilityUtil.findFrontNode(list, 2)
            val textViewList = AccessibilityUtil.findAllOnceByClazz(frontNode, Views.TextView)
            if (textViewList.size >= 2) {
                val searchButton: AccessibilityNodeInfo = textViewList[textViewList.size - 2]
                val multiButton: AccessibilityNodeInfo = textViewList[textViewList.size - 1]
                AccessibilityUtil.performClick(multiButton)
                AccessibilityUtil.performClick(searchButton)
                for (select in selectList) {
                    val needTrim = select.contains(Constant.regTrimTitle)
                    val trimTitle = select.replace(Constant.regTrimTitle, "")
                    AccessibilityUtil.findTextInput(getRoot(), trimTitle)
                    sleep(Constant.CHANGE_PAGE_INTERVAL)
                    val selectListView = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView, Views.RecyclerView, Views.ViewGroup, minChildCount = 2)
                    val regex = "^$trimTitle(-.*)?(…)?(\\(.*?\\))?" + if (needTrim) "" else "$"
                    val matchSelect = AccessibilityUtil.findOneByTextRegex(
                        selectListView,
                        regex,
                        timeout = 2000,
                        root = false
                    )
                    if (selectListView != null && matchSelect != null) {
                        for (i in 0 until selectListView.childCount) {
                            val item = selectListView.getChild(i)
                            val searchResult = AccessibilityUtil.findOnceByTextRegex(item, regex)
                            if (searchResult != null) {
                                item.refresh()
                                val imageView =
                                    AccessibilityUtil.findOneByClazz(item, Views.ImageView, root = false)
                                AccessibilityUtil.performClick(imageView)
                            }
                        }
                    }
                    if (matchSelect != null) {
                        LogUtils.d("找到搜索结果: $select")
                    } else {
                        LogUtils.e("未搜索到结果")
                    }
                    sleep(Constant.POP_WINDOW_INTERVAL)
                }
                val confirmButton =
                    AccessibilityUtil.findOneByTextRegex(getRoot(), "^确定(\\(.*?\\))?\$")
                if (confirmButton != null) {
                    AccessibilityUtil.performClick(confirmButton)
                    sleep(Constant.POP_WINDOW_INTERVAL)
                    if (!extraText.isNullOrBlank()) {
                        LogUtils.d("extraText: $extraText")
                        AccessibilityUtil.findTextInput(getRoot(), extraText)
                    }
                    val sendButton = AccessibilityUtil.findOneByTextRegex(getRoot(), "^发送(\\(.*?\\))?\$")
                    if (sendButton != null) {
                        AccessibilityUtil.performClick(sendButton)
                        return true
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
        val node = AccessibilityUtil.scrollAndFindByText(WeworkController.weworkService, getRoot(), "客户群", "居民群", "学员群")
        if (node != null) {
            AccessibilityUtil.performClick(node)
            sleep(Constant.POP_WINDOW_INTERVAL)
            LogUtils.d("进入客户群应用")
            val textView =
                AccessibilityUtil.findOneByText(getRoot(), "创建一个客户群", "创建一个居民群", "创建一个学员群")
            return AccessibilityUtil.performClick(textView)
        }
        LogUtils.e("未找到客户群应用")
        log("未找到客户群应用")
        return false
    }

    /**
     * 修改群名称
     */
    private fun groupRename(groupName: String): Boolean {
        if (WeworkRoomUtil.intoGroupManager()) {
            val textView = AccessibilityUtil.findOneByText(getRoot(), "全部群成员", "微信用户创建")
                ?: return false
            if (textView.text.contains("微信用户创建")) {
                val button = AccessibilityUtil.findFrontNode(textView)
                if (button != null) {
                    AccessibilityUtil.performClick(button)
                    AccessibilityUtil.findTextInput(getRoot(), groupName)
                    val confirmButton = AccessibilityUtil.findOneByText(getRoot(), "确定")
                    AccessibilityUtil.performClick(confirmButton)
                    sleep(2000)
                    return true
                } else {
                    LogUtils.e("未找到填写群名按钮")
                    return false
                }
            } else {
                val result = AccessibilityUtil.findTextAndClick(getRoot(), "群聊名称")
                if (result) {
                    AccessibilityUtil.findTextInput(getRoot(), groupName)
                    val confirmButton = AccessibilityUtil.findOneByText(getRoot(), "确定")
                    AccessibilityUtil.performClick(confirmButton)
                    sleep(2000)
                    return true
                } else {
                    LogUtils.e("未找到群聊名称按钮")
                }
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
                val tvEmptySize = AccessibilityUtil.findAllOnceByClazz(gridView, Views.TextView)
                    .filter { it.text == null }.size
                LogUtils.v("tvEmptySize: $tvEmptySize")
                if (tvEmptySize == 0) {
                    return true
                } else if (tvEmptySize == 1) {
                    AccessibilityUtil.performClick(gridView.getChild(gridView.childCount - 1))
                } else if (tvEmptySize == 2) {
                    AccessibilityUtil.performClick(gridView.getChild(gridView.childCount - 2))
                }
            } else {
                LogUtils.e("未找到添加成员按钮")
                return false
            }
            //群详情列表
            val list = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
            if (list != null) {
                val frontNode = AccessibilityUtil.findFrontNode(list, 2)
                val textViewList = AccessibilityUtil.findAllOnceByClazz(frontNode, Views.TextView)
                if (textViewList.size >= 2) {
                    val multiButton = textViewList.lastOrNull()
                    for (select in selectList) {
                        val needTrim = select.contains(Constant.regTrimTitle)
                        val trimTitle = select.replace(Constant.regTrimTitle, "")
                        AccessibilityUtil.performClick(multiButton)
                        AccessibilityUtil.findTextInput(getRoot(), trimTitle)
                        sleep(Constant.POP_WINDOW_INTERVAL)
                        val selectListView = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView, Views.RecyclerView, Views.ViewGroup, minChildCount = 2, firstChildClazz = Views.TextView)
                        val regex = "^$trimTitle(-.*)?(…)?(\\(.*?\\))?" + if (needTrim) "" else "$"
                        val matchSelect = AccessibilityUtil.findOneByTextRegex(
                            selectListView,
                            regex,
                            timeout = 2000,
                            root = false
                        )
                        if (selectListView != null && matchSelect != null) {
                            for (i in 0 until selectListView.childCount) {
                                val item = selectListView.getChild(i)
                                val searchResult = AccessibilityUtil.findOnceByTextRegex(item, regex)
                                if (searchResult != null) {
                                    item.refresh()
                                    val imageView =
                                        AccessibilityUtil.findOneByClazz(item, Views.ImageView, root = false)
                                    AccessibilityUtil.performClick(imageView)
                                }
                            }
                        }
                        val textView = AccessibilityUtil.findOnceByClazz(getRoot(), Views.TextView)
                        if (textView != null && textView.text.isNullOrBlank()) {
                            AccessibilityUtil.performClick(textView)
                            sleep(Constant.POP_WINDOW_INTERVAL)
                        }
                        if (matchSelect != null) {
                            LogUtils.d("找到搜索结果: $select")
                        } else {
                            LogUtils.e("未搜索到结果")
                        }
                    }
                    if (showMessageHistory) {
                        AccessibilityUtil.findTextAndClick(getRoot(), "聊天记录")
                    }
                    val confirmButton =
                        AccessibilityUtil.findOneByTextRegex(getRoot(), "^确定(\\(.*?\\))?\$")
                    if (confirmButton != null) {
                        AccessibilityUtil.performClick(confirmButton)
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
        if (removeList.isNullOrEmpty()) return true
        if (WeworkRoomUtil.intoGroupManager()) {
            val gridView = AccessibilityUtil.findOneByClazz(getRoot(), Views.GridView)
            if (gridView != null && gridView.childCount >= 2) {
                val tvEmptySize = AccessibilityUtil.findAllOnceByClazz(gridView, Views.TextView)
                    .filter { it.text == null }.size
                LogUtils.v("tvEmptySize: $tvEmptySize")
                if (tvEmptySize <= 1) {
                    return true
                } else if (tvEmptySize == 2) {
                    AccessibilityUtil.performClick(gridView.getChild(gridView.childCount - 1))
                }
            } else {
                LogUtils.e("未找到删除成员按钮")
                return false
            }
            //群详情列表
            val list = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
            if (list != null) {
                val frontNode = AccessibilityUtil.findFrontNode(list, 2)
                val textViewList = AccessibilityUtil.findAllOnceByClazz(frontNode, Views.TextView)
                if (textViewList.size >= 2) {
                    val multiButton = textViewList.lastOrNull()
                    for (select in removeList) {
                        val needTrim = select.contains(Constant.regTrimTitle)
                        val trimTitle = select.replace(Constant.regTrimTitle, "")
                        AccessibilityUtil.performClick(multiButton)
                        AccessibilityUtil.findTextInput(getRoot(), trimTitle)
                        sleep(Constant.POP_WINDOW_INTERVAL)
                        val selectListView = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView, Views.RecyclerView, Views.ViewGroup, minChildCount = 2, firstChildClazz = Views.RelativeLayout)
                        val regex = "^$trimTitle(-.*)?(…)?(\\(.*?\\))?" + if (needTrim) "" else "$"
                        val matchSelect = AccessibilityUtil.findOneByTextRegex(
                            selectListView,
                            regex,
                            timeout = 2000,
                            root = false
                        )
                        if (selectListView != null && matchSelect != null) {
                            for (i in 0 until selectListView.childCount) {
                                val item = selectListView.getChild(i)
                                val searchResult = AccessibilityUtil.findOnceByTextRegex(item, regex)
                                if (searchResult != null) {
                                    item.refresh()
                                    val imageView =
                                        AccessibilityUtil.findOneByClazz(item, Views.ImageView, root = false)
                                    AccessibilityUtil.performClick(imageView)
                                }
                            }
                        }
                        val textView = AccessibilityUtil.findOnceByClazz(getRoot(), Views.TextView)
                        if (textView != null && textView.text.isNullOrBlank()) {
                            AccessibilityUtil.performClick(textView)
                            sleep(Constant.POP_WINDOW_INTERVAL)
                        }
                        if (matchSelect != null) {
                            LogUtils.d("找到搜索结果: $select")
                        } else {
                            LogUtils.e("未搜索到结果")
                        }
                    }
                    val confirmButton =
                        AccessibilityUtil.findOneByText(getRoot(), "移出(")
                    if (confirmButton != null) {
                        AccessibilityUtil.performClick(confirmButton)
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
     * 注2：外部群为edittext 内部群为webview(只能追加文本)
     */
    private fun groupChangeAnnouncement(groupAnnouncement: String? = null): Boolean {
        if (groupAnnouncement == null) return true
        if (WeworkRoomUtil.intoGroupManager()) {
            val textView = AccessibilityUtil.findOneByText(getRoot(), "群公告", exact = true)
            if (textView != null) {
                AccessibilityUtil.performClick(textView)
                val editButton = AccessibilityUtil.findOneByText(getRoot(), "编辑", timeout = 2000, exact = true)
                if (editButton != null) {
                    LogUtils.d("群公告编辑中: $groupAnnouncement")
                    var retry = 0
                    while (retry++ < 10) {
                        AccessibilityUtil.performClick(editButton)
                        sleep(Constant.POP_WINDOW_INTERVAL)
                        if (AccessibilityUtil.findOnceByText(getRoot(), "编辑", exact = true) == null)
                            break
                    }
                }
                if (AccessibilityUtil.findTextInput(getRoot(), groupAnnouncement)) {
                    LogUtils.d("群公告发布中: $groupAnnouncement")
                    if (AccessibilityUtil.findTextAndClick(getRoot(), "发布")) {
                        val publishButtonList = AccessibilityUtil.findAllByText(getRoot(), "发布")
                        if (publishButtonList.size >= 2) {
                            AccessibilityUtil.performClick(publishButtonList[1])
                        }
                        sleep(3000)
                    } else {
                        LogUtils.e("无法进行群公告发布: ")
                    }
                } else {
                    LogUtils.e("无法进行群公告发布和编辑: ")
                    return false
                }
            } else {
                LogUtils.e("未找到群公告按钮")
                return false
            }
        } else {
            LogUtils.e("进入群管理页失败")
        }
        return true
    }

    /**
     * 修改群备注
     * 注：首次为发布 后续为编辑
     * 注2：外部群为edittext 内部群为webview(只能追加文本)
     */
    private fun groupChangeRemark(groupRemark: String? = null): Boolean {
        if (groupRemark == null) return true
        if (WeworkRoomUtil.intoGroupManager()) {
            val textView = AccessibilityUtil.findOneByText(getRoot(), "备注", exact = true)
            if (textView != null) {
                AccessibilityUtil.performClick(textView)
                if (AccessibilityUtil.findTextInput(getRoot(), groupRemark)) {
                    LogUtils.d("群备注修改: $groupRemark")
                    if (AccessibilityUtil.findTextAndClick(getRoot(), "确定")) {
                        return true
                    } else {
                        LogUtils.e("无法进行群备注发布: ")
                    }
                } else {
                    LogUtils.e("无法进行群备注修改: ")
                }
            } else {
                LogUtils.e("未找到群公告按钮")
            }
        } else {
            LogUtils.e("进入群管理页失败")
        }
        return false
    }

    /**
     * 发送消息+@at
     */
    private fun sendChatMessage(text: String, at: String? = null, atList: List<String>? = null, reply: Boolean? = false): Boolean {
        val voiceFlag = AccessibilityUtil.findOnceByText(getRoot(), "按住 说话", "按住说话", exact = true)
        if (voiceFlag != null) {
            AccessibilityUtil.performClickWithSon(AccessibilityUtil.findFrontNode(voiceFlag))
        }
        var atFailed = false
        val atList = if (!at.isNullOrEmpty()) listOf(at) else atList
        if (!atList.isNullOrEmpty()) {
            atList.forEachIndexed { index, at ->
                if (index == 0) {
                    AccessibilityUtil.findTextInput(getRoot(), "@")
                } else {
                    AccessibilityUtil.sendTextForEditText(Utils.getApp(),
                        AccessibilityUtil.findOnceByClazz(getRoot(), Views.EditText), "@"
                    )
                }
                val atFlag = AccessibilityUtil.findOneByText(getRoot(), "选择提醒的人", timeout = 2000, exact = true)
                if (atFlag != null) {
                    val rv = AccessibilityUtil.findOneByClazz(getRoot(), Views.RecyclerView)
                    if (rv != null) {
                        AccessibilityUtil.findTextInput(getRoot(), at)
                        val atNode =
                            AccessibilityUtil.findOneByText(rv, at, root = false, timeout = 2000)
                        if (atNode != null) {
                            AccessibilityUtil.performClick(atNode)
                        } else {
                            LogUtils.e("未找到at人: $at")
                            atFailed = true
                            backPress()
                        }
                        sleep(Constant.POP_WINDOW_INTERVAL)
                    } else {
                        val searchFlag = AccessibilityUtil.findOnceByText(getRoot(), "搜索", exact = true)
                        val list = AccessibilityUtil.findBackNode(searchFlag, minChildCount = 2)
                        if (list != null) {
                            AccessibilityUtil.findTextInput(getRoot(), at)
                            val atNode =
                                AccessibilityUtil.findOneByText(list, at, root = false, timeout = 2000)
                            if (atNode != null) {
                                AccessibilityUtil.performClick(atNode)
                            } else {
                                LogUtils.e("未找到at人: $at")
                                atFailed = true
                                backPress()
                            }
                            sleep(Constant.POP_WINDOW_INTERVAL)
                        }
                    }
                }
            }
        }
        val content = if (atFailed) "@${atList?.joinToString()} $text" else text
        val append = (reply == true) || (!atList.isNullOrEmpty() && !atFailed)
        if (AccessibilityUtil.findTextInput(getRoot(), content, append = append)) {
            AccessibilityUtil.findOneByText(getRoot(), "发送", exact = true, timeout = 2000)
            val sendButton = AccessibilityUtil.findAllByClazz(getRoot(), Views.Button)
                .firstOrNull { it.text == "发送" }
            if (sendButton != null) {
                LogUtils.d("发送消息: \n$content")
                log("发送消息: \n$content")
                AccessibilityUtil.performClick(sendButton)
                return true
            } else {
                LogUtils.e("未找到发送按钮")
                error("未找到发送按钮")
            }
        } else {
            LogUtils.e("未找到输入框")
            error("未找到输入框")
        }
        return false
    }

    /**
     * 设置好友标签
     */
    private fun setFriendTags(tagList: List<String>): Boolean {
        val tagList = if (tagList.size > 5) tagList.subList(0, 5) else tagList
        val tvTag = AccessibilityUtil.findAllByText(getRoot(), "个人标签").lastOrNull()
        val oldTagList = arrayListOf<String>()
        val list = AccessibilityUtil.findBackNode(tvTag)
        if (list != null && list.childCount > 0) {
            for (i in 0 until list.childCount) {
                val child = list.getChild(i)
                if (child.className.equals(Views.TextView) && child.text != null) {
                    oldTagList.add(child.text.toString())
                }
            }
            //不存在的标签先添加
            for (tag in tagList) {
                if (!oldTagList.contains(tag)) {
                    AccessibilityUtil.findOneByText(getRoot(), "个人标签")
                    sleep(Constant.POP_WINDOW_INTERVAL)
                    val tempList = AccessibilityUtil.findBackNode(
                        AccessibilityUtil.findAllByText(getRoot(), "个人标签").lastOrNull())
                    if (tempList != null && tempList.childCount > 0) {
                        AccessibilityUtil.performClick(tempList.getChild(0))
                        AccessibilityUtil.findTextInput(getRoot(), tag)
                        AccessibilityUtil.findTextAndClick(getRoot(), "确定")
                    }
                }
            }
            //确认只选择列表里的标签
            val count = list.childCount
            for (i in 0 until count) {
                val child = list.getChild(i)
                if (child != null) {
                    val text = child.text
                    val selected = child.isSelected
                    LogUtils.v("text: $text selected: $selected")
                    if (tagList.count { it == text } > 0) {
                        if (!selected) {
                            AccessibilityUtil.performClick(child)
                        }
                    } else {
                        if (selected) {
                            AccessibilityUtil.performClick(child)
                        }
                    }
                }
                list.refresh()
            }
            if (AccessibilityUtil.findTextAndClick(getRoot(), "确定")) {
                sleep(Constant.POP_WINDOW_INTERVAL)
                //可能有两次确定 另一次为添加新tag
                AccessibilityUtil.findTextAndClick(getRoot(), "确定")
                return true
            }
        }
        LogUtils.e("未找到个人标签")
        return false
    }

    /**
     * 获取群二维码并上传后台
     */
    fun getGroupQrcode(groupName: String, groupRemark: String?): Boolean {
        if (AccessibilityUtil.findOneByText(getRoot(), "全部群成员", "微信用户创建", timeout = Constant.CHANGE_PAGE_INTERVAL) != null ||
            (WeworkRoomUtil.intoRoom(groupName) && WeworkRoomUtil.intoGroupManager())) {
            val tvList = AccessibilityUtil.findAllOnceByClazz(getRoot(), Views.TextView)
            tvList.forEachIndexed { index, tv ->
                if (tv.text != null && tv.text.contains("微信用户创建")) {
                    if (index + 1 < tvList.size) {
                        val tvQr = tvList[index + 1]
                        AccessibilityUtil.performClick(tvQr)
                    }
                }
            }
            AccessibilityUtil.findOneByText(getRoot(), "保存到相册")
            val startTime = System.currentTimeMillis()
            var currentTime = startTime
            while (currentTime - startTime <= Constant.CHANGE_PAGE_INTERVAL * 5) {
                AccessibilityUtil.findOnceByClazz(getRoot(), Views.ProgressBar) ?: break
                sleep(Constant.POP_WINDOW_INTERVAL / 5)
                currentTime = System.currentTimeMillis()
            }
            if (AccessibilityUtil.findTextAndClick(getRoot(), "保存到相册")) {
                sleep(Constant.CHANGE_PAGE_INTERVAL)
                val fileDirPath = "/storage/emulated/0/DCIM/WeixinWork"
                val fileDir = FileUtils.getFileByPath(fileDirPath)
                if (fileDir.isDirectory) {
                    for (file in fileDir.listFiles().filter { it.name.endsWith(".jpg") }) {
                        val fileTime = file.name.replace("mmexport", "")
                            .replace(".jpg", "")
                        LogUtils.v("fileTime: $fileTime")
                        if (fileTime.isNotBlank()) {
                            if (fileTime.toLong() > currentTime) {
                                LogUtils.d("找到最新保存二维码图片: $fileTime")
                                try {
                                    val bitmap = ImageUtils.bytes2Bitmap(file.readBytes())
                                    val mDecoder = QRCodeDecoder.Builder().build()
                                    val qrcode = mDecoder.decode(bitmap)
                                    LogUtils.e("group: $groupName qrcode: $qrcode")
                                    val weworkMessageBean = WeworkMessageBean()
                                    weworkMessageBean.type = WeworkMessageBean.GET_GROUP_QRCODE
                                    weworkMessageBean.groupName = groupName
                                    weworkMessageBean.groupRemark = groupRemark
                                    weworkMessageBean.qrcode = qrcode
                                    WeworkController.weworkService.webSocketManager.send(
                                        weworkMessageBean
                                    )
                                    return true
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    LogUtils.e(e)
                                }
                            }
                        }
                    }
                }
            }
        }
        return false
    }

}