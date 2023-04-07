package org.yameida.worktool.service

import android.view.accessibility.AccessibilityNodeInfo
import org.yameida.worktool.Constant
import org.yameida.worktool.model.WeworkMessageBean
import com.github.yoojia.qrcode.qrcode.QRCodeDecoder
import com.blankj.utilcode.util.*
import com.lzy.okgo.OkGo
import org.yameida.worktool.model.ExecCallbackBean
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
        message: WeworkMessageBean,
        titleList: List<String>,
        receivedContent: String?,
        at: String? = null,
        atList: List<String>? = null
    ): Boolean {
        val startTime = System.currentTimeMillis()
        if (receivedContent.isNullOrEmpty()) {
            LogUtils.d("未发现发送内容")
            uploadCommandResult(message, ExecCallbackBean.ERROR_ILLEGAL_DATA, "发送内容为空", startTime, listOf(), titleList)
            goHome()
            return false
        }
        val successList = arrayListOf<String>()
        val failList = arrayListOf<String>()
        for (title in titleList) {
            if (WeworkRoomUtil.intoRoom(title)) {
                if (sendChatMessage(receivedContent, at = at, atList = atList)) {
                    successList.add(title)
                    LogUtils.d("$title: 发送成功")
                } else {
                    LogUtils.d("$title: 发送失败")
                    failList.add(title)
                    error("$title: 发送失败 $receivedContent")
                }
            } else {
                failList.add(title)
                LogUtils.d("$title: 发送失败 进入房间失败")
                error("$title: 发送失败 进入房间失败 $receivedContent")
            }
        }
        if (failList.isNotEmpty()) {
            uploadCommandResult(message, ExecCallbackBean.ERROR_SEND_MESSAGE, "发送成功: ${successList.joinToString()} 发送失败: ${failList.joinToString()}", startTime, successList, failList)
            return false
        }
        uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, successList, failList)
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
        message: WeworkMessageBean,
        titleList: List<String>,
        receivedName: String?,
        originalContent: String,
        textType: Int,
        receivedContent: String?
    ): Boolean {
        val startTime = System.currentTimeMillis()
        if (receivedContent.isNullOrEmpty()) {
            LogUtils.d("未发现回复内容")
            uploadCommandResult(message, ExecCallbackBean.ERROR_ILLEGAL_DATA, "回复内容为空", startTime, listOf(), titleList)
            goHome()
            return false
        }
        val successList = arrayListOf<String>()
        val failList = arrayListOf<String>()
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
                    if (sendChatMessage(receivedContent, reply = true)) {
                        LogUtils.d("$title: 回复成功")
                        successList.add(title)
                        uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, successList, failList)
                        return true
                    } else {
                        LogUtils.d("$title: 回复发送失败")
                        failList.add(title)
                        uploadCommandResult(message, ExecCallbackBean.ERROR_SEND_MESSAGE, "回复发送失败", startTime, successList, failList)
                        return false
                    }
                } else {
                    LogUtils.d("$title: 回复失败 直接发送答案")
                    error("$title: 回复失败 直接发送答案 $receivedContent")
                    val text = if (originalContent.isNotEmpty()) "【$originalContent】\n$receivedContent" else receivedContent
                    if (sendChatMessage(text, receivedName)) {
                        LogUtils.d("$title: 直接发送答案成功")
                        successList.add(title)
                        uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, successList, failList)
                        return true
                    } else {
                        LogUtils.d("$title: 直接发送答案失败")
                        failList.add(title)
                        uploadCommandResult(message, ExecCallbackBean.ERROR_SEND_MESSAGE, "直接发送答案失败", startTime, successList, failList)
                        return false
                    }
                }
            } else {
                error("$title: 回复失败 $receivedContent")
                LogUtils.d("进入房间失败 $title")
                failList.add(title)
                uploadCommandResult(message, ExecCallbackBean.ERROR_INTO_ROOM, "进入房间失败 $title", startTime, successList, failList)
                return false
            }
        }
        LogUtils.d("房间名为空")
        uploadCommandResult(message, ExecCallbackBean.ERROR_ILLEGAL_DATA, "房间名为空", startTime, listOf(), titleList)
        return false
    }

    /**
     * 在房间内转发消息
     * @see WeworkMessageBean.RELAY_MESSAGE
     * @param titleList 房间名称
     * @param receivedName 原始消息的发送者姓名
     * @param originalContent 原始消息的内容
     * @param textType 原始消息的消息类型
     * @param nameList 待转发姓名列表
     * @param extraText 附加留言 选填
     * @see WeworkMessageBean.TEXT_TYPE
     */
    fun relayMessage(
        message: WeworkMessageBean,
        titleList: List<String>,
        receivedName: String?,
        originalContent: String,
        textType: Int,
        nameList: List<String>,
        extraText: String? = null
    ): Boolean {
        val startTime = System.currentTimeMillis()
        for (title in titleList) {
            if (WeworkRoomUtil.intoRoom(title)) {
                if (!receivedName.isNullOrEmpty()) {
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
                        if (relaySelectTarget(nameList, extraText)) {
                            LogUtils.d("$title: 转发成功")
                            uploadCommandResult(message, ExecCallbackBean.SUCCESS, "$title: 转发成功", startTime, titleList, listOf())
                            return true
                        } else {
                            LogUtils.d("$title: 转发失败 $originalContent")
                            uploadCommandResult(message, ExecCallbackBean.ERROR_RELAY, "$title: 转发失败 $originalContent", startTime, listOf(), titleList)
                            return false
                        }
                    } else {
                        LogUtils.e("$title: 长按条目失败 $originalContent")
                        uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "$title: 长按条目失败 $originalContent", startTime, listOf(), titleList)
                        return false
                    }
                } else {
                    if (WeworkTextUtil.longClickMyMessageItem(
                            //聊天消息列表 1ListView 0RecycleView xViewGroup
                            AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView),
                            textType,
                            originalContent,
                            "转发"
                        )
                    ) {
                        LogUtils.d("开始转发")
                        if (relaySelectTarget(nameList, extraText)) {
                            LogUtils.d("$title: 转发成功")
                            uploadCommandResult(message, ExecCallbackBean.SUCCESS, "$title: 转发成功", startTime, titleList, listOf())
                            return true
                        } else {
                            LogUtils.d("$title: 转发失败 $originalContent")
                            uploadCommandResult(message, ExecCallbackBean.ERROR_RELAY, "$title: 转发失败 $originalContent", startTime, listOf(), titleList)
                            return false
                        }
                    } else {
                        LogUtils.e("$title: 长按条目失败 $originalContent")
                        uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "$title: 长按条目失败 $originalContent", startTime, listOf(), titleList)
                        return false
                    }
                }
            } else {
                LogUtils.d("$title: 转发失败 未找到房间")
                error("$title: 转发失败 $originalContent")
            }
        }
        LogUtils.d("转发失败 未找到房间名")
        uploadCommandResult(message, ExecCallbackBean.ERROR_ILLEGAL_DATA, "转发失败 未找到房间名", startTime, listOf(), titleList)
        return false
    }

    /**
     * 初始化群设置
     * 群名称、群公告、拉人、踢人、群备注、群模板
     * 注：群配置模板 1.群名称 2.禁群改名(使用) 3.设置管理员 4.入群欢迎语(使用) 5.自动回复 6.防骚扰规则(使用)
     * @see WeworkMessageBean.INIT_GROUP
     * @param groupName 修改群名称
     * @param selectList 添加群成员名称列表 选填
     * @param groupAnnouncement 修改群公告 选填
     * @param groupRemark 修改群备注 选填
     * @param groupTemplate 修改群模板 选填
     */
    fun initGroup(
        message: WeworkMessageBean,
        groupName: String,
        selectList: List<String>?,
        groupAnnouncement: String?,
        groupRemark: String?,
        groupTemplate: String?
    ): Boolean {
        val startTime = System.currentTimeMillis()
        if (!WeworkRoomUtil.isGroupExists(groupName)) {
            if (!beforeCreateGroupCheck()) {
                uploadCommandResult(message, ExecCallbackBean.ERROR_CREATE_GROUP_LIMIT, "建群达到上限", startTime, listOf(), listOf(groupName))
                return false
            }
            if (!createGroup()) {
                uploadCommandResult(message, ExecCallbackBean.ERROR_CREATE_GROUP, "创建群失败", startTime, listOf(), listOf(groupName))
                return false
            }
            if (createGroupLimit()) {
                uploadCommandResult(message, ExecCallbackBean.ERROR_CREATE_GROUP_LIMIT, "建群达到上限", startTime, listOf(), listOf(groupName))
                return false
            } else {
                LogUtils.v("未发现建群达到上限")
            }
        }
        if (!groupRename(groupName)) {
            uploadCommandResult(message, ExecCallbackBean.ERROR_GROUP_RENAME, "创建群成功 群改名失败", startTime, listOf(), listOf(groupName))
            return false
        }
        if (!groupAddMember(selectList)) {
            uploadCommandResult(message, ExecCallbackBean.ERROR_GROUP_ADD_MEMBER, "创建群成功 群改名成功 群拉人失败: ${selectList?.joinToString()}", startTime, listOf(), listOf(groupName))
            return false
        }
        if (!groupChangeAnnouncement(groupAnnouncement)) {
            uploadCommandResult(message, ExecCallbackBean.ERROR_GROUP_CHANGE_ANNOUNCEMENT, "创建群成功 群改名成功 群拉人成功 群公告失败", startTime, listOf(), listOf(groupName))
            return false
        }
        if (!groupChangeRemark(groupRemark)) {
            uploadCommandResult(message, ExecCallbackBean.ERROR_GROUP_CHANGE_REMARK, "创建群成功 群改名成功 群拉人成功 群公告成功 群备注失败", startTime, listOf(), listOf(groupName))
            return false
        }
        if (!groupTemplate(groupTemplate)) {
            uploadCommandResult(message, ExecCallbackBean.ERROR_GROUP_TEMPLATE, "创建群成功 群改名成功 群拉人成功 群公告成功 群备注成功 群模板失败", startTime, listOf(), listOf(groupName))
            return false
        }
        getGroupQrcode(groupName, groupRemark)
        uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, listOf(groupName), listOf())
        return true
    }

    /**
     * 进入群聊并修改群配置
     * 群名称、群公告、拉人、踢人、群备注、群模板
     * @see WeworkMessageBean.INTO_GROUP_AND_CONFIG
     * @param groupName 待修改的群
     * @param newGroupName 修改群名 选填
     * @param newGroupAnnouncement 修改群公告 选填
     * @param groupRemark 修改群备注 选填
     * @param groupTemplate 修改群模板 选填
     * @param selectList 添加群成员名称列表/拉人 选填
     * @param showMessageHistory 拉人是否附带历史记录 选填
     * @param removeList 移除群成员名称列表/踢人 选填
     */
    fun intoGroupAndConfig(
        message: WeworkMessageBean,
        groupName: String,
        newGroupName: String?,
        newGroupAnnouncement: String?,
        groupRemark: String?,
        groupTemplate: String?,
        selectList: List<String>?,
        showMessageHistory: Boolean?,
        removeList: List<String>?
    ): Boolean {
        val startTime = System.currentTimeMillis()
        if (!WeworkRoomUtil.intoRoom(groupName)) {
            uploadCommandResult(message, ExecCallbackBean.ERROR_INTO_ROOM, "进入房间失败 $groupName", startTime, listOf(), listOf(groupName))
            return false
        }
        if (!groupRename(newGroupName)) {
            uploadCommandResult(message, ExecCallbackBean.ERROR_GROUP_RENAME, "进入房间成功 群改名失败", startTime, listOf(), listOf(groupName))
            return false
        }
        if (!groupAddMember(selectList, showMessageHistory == true)) {
            uploadCommandResult(message, ExecCallbackBean.ERROR_GROUP_ADD_MEMBER, "进入房间成功 群改名成功 群拉人失败: ${selectList?.joinToString()}", startTime, listOf(), listOf(groupName))
            return false
        }
        if (!groupRemoveMember(removeList)) {
            uploadCommandResult(message, ExecCallbackBean.ERROR_GROUP_REMOVE_MEMBER, "进入房间成功 群改名成功 群拉人成功 群踢人失败: ${removeList?.joinToString()}", startTime, listOf(), listOf(groupName))
            return false
        }
        if (!groupChangeAnnouncement(newGroupAnnouncement)) {
            uploadCommandResult(message, ExecCallbackBean.ERROR_GROUP_CHANGE_ANNOUNCEMENT, "进入房间成功 群改名成功 群拉人成功 群公告失败", startTime, listOf(), listOf(groupName))
            return false
        }
        if (!groupChangeRemark(groupRemark)) {
            uploadCommandResult(message, ExecCallbackBean.ERROR_GROUP_CHANGE_REMARK, "进入房间成功 群改名成功 群拉人成功 群公告成功 群备注失败", startTime, listOf(), listOf(groupName))
            return false
        }
        if (!groupTemplate(groupTemplate)) {
            uploadCommandResult(message, ExecCallbackBean.ERROR_GROUP_TEMPLATE, "进入房间成功 群改名成功 群拉人成功 群公告成功 群备注成功 群模板失败", startTime, listOf(), listOf(groupName))
            return false
        }
        getGroupQrcode(groupName, groupRemark)
        uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, listOf(groupName), listOf())
        return true
    }

    /**
     * 解散群聊
     * @see WeworkMessageBean.DISMISS_GROUP
     * @param groupName 待解散的群
     */
    fun dismissGroup(
        message: WeworkMessageBean,
        groupName: String
    ): Boolean {
        val startTime = System.currentTimeMillis()
        if (WeworkRoomUtil.intoRoom(groupName) && WeworkRoomUtil.intoGroupManager()) {
            val groupManagerTv =
                AccessibilityUtil.findOneByText(getRoot(), "群管理", exact = true, timeout = 2000)
            if (groupManagerTv != null) {
                AccessibilityUtil.performClick(groupManagerTv)
                val dismissTv =
                    AccessibilityUtil.findOneByText(getRoot(), "解散群聊", exact = true, timeout = 2000)
                AccessibilityUtil.performClick(dismissTv)
                if (dismissTv != null) {
                    val confirmTv = AccessibilityUtil.findOneByText(getRoot(), "解散", "确定", exact = true, timeout = 2000)
                    if (confirmTv != null) {
                        AccessibilityUtil.performClick(confirmTv)
                        uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, listOf(groupName), listOf())
                        return true
                    } else {
                        uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到解散按钮 $groupName", startTime, listOf(), listOf(groupName))
                        return false
                    }
                } else {
                    uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到解散群聊按钮 $groupName", startTime, listOf(), listOf(groupName))
                    return false
                }
            } else {
                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到群管理按钮 $groupName", startTime, listOf(), listOf(groupName))
                return false
            }
        } else {
            uploadCommandResult(message, ExecCallbackBean.ERROR_INTO_ROOM, "进入房间失败 $groupName", startTime, listOf(), listOf(groupName))
            return false
        }
    }

    /**
     * 推送微盘图片(不推荐)
     * @see pushFile
     * @param titleList 待发送姓名列表
     * @param objectName 图片名称
     * @param extraText  附加留言 可选
     */
    fun pushMicroDiskImage(
        message: WeworkMessageBean,
        titleList: List<String>,
        objectName: String,
        extraText: String? = null
    ): Boolean {
        val startTime = System.currentTimeMillis()
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
                    val shareFileButton = AccessibilityUtil.findOneByDesc(getRoot(), "以原文件分享", "用其他应用打开")
                    AccessibilityUtil.performClick(shareFileButton)
                    var shareToWorkButton = AccessibilityUtil.findOneByText(getRoot(true), "发送给同事")
                    sleep(Constant.POP_WINDOW_INTERVAL)
                    AccessibilityUtil.performClick(shareToWorkButton)
                    sleep(Constant.POP_WINDOW_INTERVAL)
                    shareToWorkButton = AccessibilityUtil.findOnceByText(getRoot(true), "发送给同事")
                    LogUtils.v("尝试发送给同事", shareToWorkButton == null, WeworkController.weworkService.currentPackage)
                    val time = System.currentTimeMillis()
                    var currentTime = time
                    while (currentTime - time < 5000) {
                        if (shareToWorkButton != null
                            && WeworkController.weworkService.currentPackage != Constant.PACKAGE_NAMES) {
                            LogUtils.e("尝试手势点击！！！！！")
                            AccessibilityUtil.clickByNode(WeworkController.weworkService, shareToWorkButton)
                            sleep(Constant.CHANGE_PAGE_INTERVAL)
                            shareToWorkButton = AccessibilityUtil.findOnceByText(getRoot(true), "发送给同事")
                        } else {
                            break
                        }
                        currentTime = System.currentTimeMillis()
                    }
                    if (relaySelectTarget(titleList, extraText, timeout = 10000)) {
                        val stayButton = AccessibilityUtil.findOneByText(getRoot(), "留在企业微信")
                        AccessibilityUtil.performClick(stayButton)
                        uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, titleList, listOf())
                        return true
                    } else {
                        LogUtils.e("微盘文件转发失败: $objectName")
                        uploadCommandResult(message, ExecCallbackBean.ERROR_RELAY, "微盘文件转发失败: $objectName", startTime, listOf(), titleList)
                        return false
                    }
                } else {
                    LogUtils.e("微盘未搜索到相关图片: $objectName")
                    uploadCommandResult(message, ExecCallbackBean.ERROR_TARGET, "微盘未搜索到相关图片: $objectName", startTime, listOf(), titleList)
                    return false
                }
            } else {
                LogUtils.e("未找到微盘内搜索")
                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到微盘内搜索", startTime, listOf(), titleList)
                return false
            }
        } else {
            LogUtils.e("未找到微盘")
            uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到微盘", startTime, listOf(), titleList)
            return false
        }
    }

    /**
     * 推送微盘文件(不推荐)
     * @see pushFile
     * @param titleList 待发送姓名列表
     * @param objectName 文件名称
     * @param extraText  附加留言 可选
     */
    fun pushMicroDiskFile(
        message: WeworkMessageBean,
        titleList: List<String>,
        objectName: String,
        extraText: String? = null
    ): Boolean {
        val startTime = System.currentTimeMillis()
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
                        uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, titleList, listOf())
                        return true
                    } else {
                        LogUtils.e("微盘文件转发失败: $objectName")
                        uploadCommandResult(message, ExecCallbackBean.ERROR_RELAY, "微盘文件转发失败: $objectName", startTime, listOf(), titleList)
                        return false
                    }
                } else {
                    LogUtils.e("微盘未搜索到相关文件: $objectName")
                    uploadCommandResult(message, ExecCallbackBean.ERROR_TARGET, "微盘未搜索到相关文件: $objectName", startTime, listOf(), titleList)
                    return false
                }
            } else {
                LogUtils.e("未找到微盘内搜索")
                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到微盘内搜索", startTime, listOf(), titleList)
                return false
            }
        } else {
            LogUtils.e("未找到微盘")
            uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到微盘", startTime, listOf(), titleList)
            return false
        }
    }

    /**
     * 推送任意小程序(不推荐)
     * @param titleList 待发送姓名列表
     * @param objectName 小程序名称
     * @param extraText  附加留言 可选
     */
    fun pushMicroprogram(
        message: WeworkMessageBean,
        titleList: List<String>,
        objectName: String,
        extraText: String? = null
    ): Boolean {
        val startTime = System.currentTimeMillis()
        return false
    }

    /**
     * 推送腾讯文档
     * @param titleList 待发送姓名列表
     * @param objectName 小程序名称
     * @param extraText  附加留言 可选
     */
    fun pushOffice(
        message: WeworkMessageBean,
        titleList: List<String>,
        objectName: String,
        extraText: String? = null
    ): Boolean {
        val startTime = System.currentTimeMillis()
        goHomeTab("文档")
        val allButton = AccessibilityUtil.findOneByText(getRoot(), "全部")
        if (allButton == null) {
            LogUtils.e("未找到全部按钮")
            uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到全部按钮", startTime, listOf(), titleList)
            return false
        }
        AccessibilityUtil.performClick(allButton)
        val myFileButton = AccessibilityUtil.findOneByText(getRoot(), "共享空间")
        if (myFileButton == null) {
            LogUtils.e("未找到共享空间按钮")
            uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到共享空间按钮", startTime, listOf(), titleList)
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
                    uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, titleList, listOf())
                    return true
                } else {
                    LogUtils.e("微盘文件转发失败: $objectName")
                    uploadCommandResult(message, ExecCallbackBean.ERROR_RELAY, "微盘文件转发失败: $objectName", startTime, listOf(), titleList)
                    return false
                }
            } else {
                LogUtils.e("文档未搜索到相关文件: $objectName")
                uploadCommandResult(message, ExecCallbackBean.ERROR_TARGET, "文档未搜索到相关文件: $objectName", startTime, listOf(), titleList)
                return false
            }
        } else {
            LogUtils.e("未找到文档搜索按钮")
            uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到文档搜索按钮", startTime, listOf(), titleList)
            return false
        }
    }

    /**
     * 推送文件(网络图片视频和文件等)
     * @see WeworkMessageBean.PUSH_FILE
     * @param titleList 待发送姓名列表
     * @param objectName 文件名称
     * @param fileUrl 文件网络地址
     * @param fileBase64 文件Base64
     * @param fileType 文件类型
     * @param extraText 附加留言 可选
     */
    fun pushFile(
        message: WeworkMessageBean,
        titleList: List<String>,
        objectName: String,
        fileUrl: String?,
        fileBase64: String?,
        fileType: String,
        extraText: String? = null,
        maxRetryCount: Int? = null
    ): Boolean {
        val retryCount = maxRetryCount ?: 1
        val startTime = System.currentTimeMillis()
        if (!PermissionUtils.isGrantedDrawOverlays()) {
            LogUtils.e("未打开悬浮窗权限")
            uploadCommandResult(message, ExecCallbackBean.ERROR_ILLEGAL_PERMISSION, "未打开悬浮窗权限", startTime, listOf(), titleList)
            return false
        }
        if (fileUrl != null) {
            LogUtils.i("下载开始 $fileUrl")
            val execute = OkGo.get<File>(fileUrl).execute()
            LogUtils.i("下载完成 $fileUrl")
            val body = execute.body()
            if (body != null) {
                val df = SimpleDateFormat("yyyy-MM-dd")
                val filePath = "${
                    Utils.getApp().getExternalFilesDir("share")
                }/${df.format(Date())}/$objectName"
                val newFile = File(filePath)
                val create = FileUtils.createFileByDeleteOldFile(newFile)
                if (create && newFile.canWrite()) {
                    newFile.writeBytes(body.bytes())
                    LogUtils.i("文件存储本地成功 $filePath")
                    ShareUtil.share("${if (fileType.isBlank()) "*" else fileType}/*", newFile)
                    var shareToWorkButton = AccessibilityUtil.findOneByText(getRoot(true), "发送给同事")
                    sleep(Constant.POP_WINDOW_INTERVAL)
                    AccessibilityUtil.performClick(shareToWorkButton)
                    sleep(Constant.POP_WINDOW_INTERVAL)
                    shareToWorkButton = AccessibilityUtil.findOnceByText(getRoot(true), "发送给同事")
                    LogUtils.v("尝试发送给同事", shareToWorkButton == null, WeworkController.weworkService.currentPackage)
                    val time = System.currentTimeMillis()
                    var currentTime = time
                    while (currentTime - time < 5000) {
                        if (shareToWorkButton != null
                            && WeworkController.weworkService.currentPackage != Constant.PACKAGE_NAMES) {
                            LogUtils.e("尝试手势点击！！！！！")
                            AccessibilityUtil.clickByNode(WeworkController.weworkService, shareToWorkButton)
                            sleep(Constant.CHANGE_PAGE_INTERVAL)
                            shareToWorkButton = AccessibilityUtil.findOnceByText(getRoot(true), "发送给同事")
                        } else {
                            break
                        }
                        currentTime = System.currentTimeMillis()
                    }
                    if (relaySelectTarget(titleList, extraText, timeout = 10000)) {
                        val stayButton = AccessibilityUtil.findOneByText(getRoot(), "留在企业微信")
                        AccessibilityUtil.performClick(stayButton)
                        uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, titleList, listOf())
                        return true
                    } else {
                        LogUtils.e("文件转发失败: $objectName")
                        if (retryCount > 0) {
                            return pushFile(message, titleList, objectName, fileUrl, fileBase64, fileType, extraText, retryCount - 1)
                        }
                        uploadCommandResult(message, ExecCallbackBean.ERROR_RELAY, "文件转发失败: $objectName", startTime, listOf(), titleList)
                        return false
                    }
                } else {
                    LogUtils.e("文件存储本地失败 $filePath")
                    if (retryCount > 0) {
                        return pushFile(message, titleList, objectName, fileUrl, fileBase64, fileType, extraText, retryCount - 1)
                    }
                    uploadCommandResult(message, ExecCallbackBean.ERROR_FILE_STORAGE, "文件存储本地失败 $filePath", startTime, listOf(), titleList)
                    return false
                }
            } else {
                LogUtils.e("文件下载失败")
                if (retryCount > 0) {
                    return pushFile(message, titleList, objectName, fileUrl, fileBase64, fileType, extraText, retryCount - 1)
                }
                uploadCommandResult(message, ExecCallbackBean.ERROR_FILE_DOWNLOAD, "文件下载失败 $fileUrl", startTime, listOf(), titleList)
                return false
            }
        } else if (fileBase64 != null) {
            val df = SimpleDateFormat("yyyy-MM-dd")
            val filePath = "${
                Utils.getApp().getExternalFilesDir("share")
            }/${df.format(Date())}/$objectName"
            val newFile = File(filePath)
            val create = FileUtils.createFileByDeleteOldFile(newFile)
            if (create && newFile.canWrite()) {
                newFile.writeBytes(EncodeUtils.base64Decode(fileBase64))
                LogUtils.i("文件存储本地成功 $filePath")
                ShareUtil.share("${if (fileType.isBlank()) "*" else fileType}/*", newFile)
                var shareToWorkButton = AccessibilityUtil.findOneByText(getRoot(true), "发送给同事")
                sleep(Constant.POP_WINDOW_INTERVAL)
                AccessibilityUtil.performClick(shareToWorkButton)
                sleep(Constant.POP_WINDOW_INTERVAL)
                shareToWorkButton = AccessibilityUtil.findOnceByText(getRoot(true), "发送给同事")
                LogUtils.v("尝试发送给同事", shareToWorkButton == null, WeworkController.weworkService.currentPackage)
                val time = System.currentTimeMillis()
                var currentTime = time
                while (currentTime - time < 5000) {
                    if (shareToWorkButton != null
                        && WeworkController.weworkService.currentPackage != Constant.PACKAGE_NAMES) {
                        LogUtils.e("尝试手势点击！！！！！")
                        AccessibilityUtil.clickByNode(WeworkController.weworkService, shareToWorkButton)
                        sleep(Constant.CHANGE_PAGE_INTERVAL)
                        shareToWorkButton = AccessibilityUtil.findOnceByText(getRoot(true), "发送给同事")
                    } else {
                        break
                    }
                    currentTime = System.currentTimeMillis()
                }
                if (relaySelectTarget(titleList, extraText, timeout = 10000)) {
                    val stayButton = AccessibilityUtil.findOneByText(getRoot(), "留在企业微信")
                    AccessibilityUtil.performClick(stayButton)
                    uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, titleList, listOf())
                    return true
                } else {
                    LogUtils.e("文件转发失败: $objectName")
                    if (retryCount > 0) {
                        return pushFile(message, titleList, objectName, fileUrl, fileBase64, fileType, extraText, retryCount - 1)
                    }
                    uploadCommandResult(message, ExecCallbackBean.ERROR_RELAY, "文件转发失败: $objectName", startTime, listOf(), titleList)
                    return false
                }
            } else {
                LogUtils.e("文件存储本地失败 $filePath")
                if (retryCount > 0) {
                    return pushFile(message, titleList, objectName, fileUrl, fileBase64, fileType, extraText, retryCount - 1)
                }
                uploadCommandResult(message, ExecCallbackBean.ERROR_FILE_STORAGE, "文件存储本地失败 $filePath", startTime, listOf(), titleList)
                return false
            }
        } else {
            LogUtils.e("未找到文件资源参数")
            if (retryCount > 0) {
                return pushFile(message, titleList, objectName, fileUrl, fileBase64, fileType, extraText, retryCount - 1)
            }
            uploadCommandResult(message, ExecCallbackBean.ERROR_ILLEGAL_DATA, "未找到文件资源参数", startTime, listOf(), titleList)
            return false
        }
    }

    /**
     * 推送链接
     * @see WeworkMessageBean.PUSH_LINK
     * @param titleList 待发送姓名列表
     * @param objectName 文章标题
     * @param receivedContent 文章副标题
     * @param originalContent 文章链接地址
     * @param fileUrl 图片地址
     * @param extraText 附加留言 可选
     */
    fun pushLink(
        message: WeworkMessageBean,
        titleList: List<String>,
        objectName: String,
        receivedContent: String,
        originalContent: String,
        fileUrl: String,
        extraText: String? = null,
        maxRetryCount: Int? = null
    ): Boolean {
        val startTime = System.currentTimeMillis()
        if (IWWAPIUtil.sendLink(fileUrl, originalContent, objectName, receivedContent)) {
            if (relaySelectTarget(titleList, extraText)) {
                uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, titleList, listOf())
                return true
            } else {
                LogUtils.e("转发失败")
                uploadCommandResult(message, ExecCallbackBean.ERROR_RELAY, "转发失败", startTime, listOf(), titleList)
                return false
            }
        } else {
            LogUtils.e("非法操作")
            uploadCommandResult(message, ExecCallbackBean.ERROR_ILLEGAL_OPERATION, "非法操作", startTime, listOf(), titleList)
            return false
        }
    }

    /**
     * 撤回消息
     * @see WeworkMessageBean.RECALL_MESSAGE
     * @param titleList 房间名称
     * @param originalContent 原始消息的内容
     * @param textType 原始消息的消息类型
     * @see WeworkMessageBean.TEXT_TYPE
     */
    fun recallMessage(
        message: WeworkMessageBean,
        titleList: List<String>,
        originalContent: String,
        textType: Int
    ): Boolean {
        val startTime = System.currentTimeMillis()
        for (title in titleList) {
            if (WeworkRoomUtil.intoRoom(title)) {
                if (WeworkTextUtil.longClickMyMessageItem(
                        //聊天消息列表 1ListView 0RecycleView xViewGroup
                        AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView),
                        textType,
                        originalContent,
                        "撤回"
                    )
                ) {
                    LogUtils.d("撤回成功")
                    uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, titleList, listOf())
                    return true
                } else {
                    LogUtils.e("撤回失败 未找到目标消息")
                    uploadCommandResult(message, ExecCallbackBean.ERROR_TARGET, "撤回失败 未找到目标消息", startTime, listOf(), titleList)
                    return false
                }
            }
        }
        LogUtils.e("撤回失败 未找到房间")
        uploadCommandResult(message, ExecCallbackBean.ERROR_TARGET, "撤回失败 未找到房间", startTime, listOf(), titleList)
        return false
    }

    /**
     * 批量转发
     * @see WeworkMessageBean.RELAY_MULTI_MESSAGE
     * @param titleList 房间名称
     * @param messageList 消息列表
     * @param nameList 待转发姓名列表
     * @param extraText 附加留言 选填
     * @see WeworkMessageBean.TEXT_TYPE
     */
    fun relayMultiMessage(
        message: WeworkMessageBean,
        titleList: List<String>,
        messageList: List<WeworkMessageBean.SubMessageBean>,
        nameList: List<String>,
        extraText: String? = null
    ): Boolean {
        return relayMultiMessage(message, titleList, messageList, nameList, extraText, "逐条转发")
    }

    /**
     * 合并转发
     * @see WeworkMessageBean.RELAY_MERGE_MESSAGE
     * @param titleList 房间名称
     * @param messageList 消息列表
     * @param nameList 待转发姓名列表
     * @param extraText 附加留言 选填
     * @see WeworkMessageBean.TEXT_TYPE
     */
    fun relayMergeMessage(
        message: WeworkMessageBean,
        titleList: List<String>,
        messageList: List<WeworkMessageBean.SubMessageBean>,
        nameList: List<String>,
        extraText: String? = null
    ): Boolean {
        return relayMultiMessage(message, titleList, messageList, nameList, extraText, "合并转发")
    }

    /**
     * 批量发送
     * @see WeworkMessageBean.SEND_MULTI_MESSAGE
     * @param weworkMessageList 消息列表
     * @param nameList 待转发姓名列表
     * @param extraText 附加留言 选填
     * @see WeworkMessageBean.TEXT_TYPE
     */
    fun sendMultiMessage(
        message: WeworkMessageBean,
        weworkMessageList: List<WeworkMessageBean>,
        nameList: List<String>,
        extraText: String? = null
    ): Boolean {
        return sendMultiMessage(message, weworkMessageList, nameList, extraText, "逐条转发")
    }

    /**
     * 合并发送
     * @see WeworkMessageBean.SEND_MERGE_MESSAGE
     * @param weworkMessageList 消息列表
     * @param nameList 待转发姓名列表
     * @param extraText 附加留言 选填
     * @see WeworkMessageBean.TEXT_TYPE
     */
    fun sendMergeMessage(
        message: WeworkMessageBean,
        weworkMessageList: List<WeworkMessageBean>,
        nameList: List<String>,
        extraText: String? = null
    ): Boolean {
        return sendMultiMessage(message, weworkMessageList, nameList, extraText, "合并转发")
    }

    /**
     * 批量发送 合并发送
     */
    private fun sendMultiMessage(
        message: WeworkMessageBean,
        weworkMessageList: List<WeworkMessageBean>,
        nameList: List<String>,
        extraText: String? = null,
        key: String
    ): Boolean {
        val startTime = System.currentTimeMillis()
        val groupName = "消息转发专用群"
        val titleList = arrayListOf(groupName)
        message.titleList = titleList
        if (!WeworkRoomUtil.isGroupExists(groupName)) {
            if (!createGroup()) {
                uploadCommandResult(message, ExecCallbackBean.ERROR_CREATE_GROUP, "创建群失败", startTime, listOf(), listOf(groupName))
                return false
            }
            if (!groupRename(groupName)) {
                uploadCommandResult(message, ExecCallbackBean.ERROR_GROUP_RENAME, "创建群成功 群改名失败", startTime, listOf(), listOf(groupName))
                return false
            }
        }
        LogUtils.d("进入转发专用群")
        sendMessage(message, message.titleList, startTime.toString())
        for (weworkMessage in weworkMessageList) {
            weworkMessage.titleList = message.titleList
            when (weworkMessage.type) {
                WeworkMessageBean.SEND_MESSAGE -> {
                    WeworkController.sendMessage(weworkMessage)
                }
                WeworkMessageBean.PUSH_MICRO_DISK_IMAGE -> {
                    WeworkController.pushMicroDiskImage(weworkMessage)
                }
                WeworkMessageBean.PUSH_MICRO_DISK_FILE -> {
                    WeworkController.pushMicroDiskFile(weworkMessage)
                }
                WeworkMessageBean.PUSH_MICROPROGRAM -> {
                    WeworkController.pushMicroprogram(weworkMessage)
                }
                WeworkMessageBean.PUSH_OFFICE -> {
                    WeworkController.pushOffice(weworkMessage)
                }
                WeworkMessageBean.PUSH_FILE -> {
                    WeworkController.pushFile(weworkMessage)
                }
                WeworkMessageBean.PUSH_LINK -> {
                    WeworkController.pushLink(weworkMessage)
                }
            }
        }
        if (WeworkRoomUtil.intoRoom(groupName)) {
            if (WeworkTextUtil.longClickMyMessageItem(
                    //聊天消息列表 1ListView 0RecycleView xViewGroup
                    AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView),
                    WeworkMessageBean.TEXT_TYPE_PLAIN,
                    startTime.toString(),
                    "多选"
                )
            ) {
                LogUtils.d("多选成功")
            } else {
                LogUtils.e("$groupName: 多选失败")
                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "多选失败 $startTime", startTime, listOf(), titleList)
                return false
            }
            //聊天消息列表 1ListView 0RecycleView xViewGroup
            val list = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
            if (list != null) {
                val childCount = list.childCount
                LogUtils.v("消息条数: $childCount")
                var start = false
                for (i in 0 until list.childCount) {
                    val item = list.getChild(i)
                    if (item != null && item.childCount > 0) {
                        if (!start) {
                            val parseChatMessageItem = WeworkLoopImpl.parseChatMessageItem(
                                item,
                                titleList,
                                WeworkMessageBean.ROOM_TYPE_EXTERNAL_GROUP,
                                false
                            )
                            if (parseChatMessageItem.itemMessageList.find { it.feature == 2 && it.text?.toString() == startTime.toString() } != null) {
                                start = true
                            }
                        }
                        if (start) {
                            AccessibilityUtil.clickByNode(WeworkController.weworkService, item)
                            LogUtils.d("单击成功")
                            sleep(Constant.POP_WINDOW_INTERVAL / 2)
                        }
                    }
                }
            }
            val bottomList = AccessibilityUtil.findOnceByClazz(getRoot(), Views.ViewGroup, minChildCount = 4)
            if (AccessibilityUtil.performClickWithSon(bottomList)) {
                if (AccessibilityUtil.findTextAndClick(getRoot(), key)) {
                    if (relaySelectTarget(nameList, extraText)) {
                        LogUtils.d("$groupName: 转发成功")
                        uploadCommandResult(message, ExecCallbackBean.SUCCESS, "$groupName: 转发成功", startTime, titleList, listOf())
                        return true
                    } else {
                        LogUtils.e("$groupName: 转发失败")
                        uploadCommandResult(message, ExecCallbackBean.ERROR_RELAY, "$groupName: 转发失败", startTime, listOf(), titleList)
                        return false
                    }
                } else {
                    LogUtils.e("未找到逐条转发按钮")
                    uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到逐条转发按钮", startTime, listOf(), titleList)
                    return false
                }
            } else {
                LogUtils.e("未找到转发按钮")
                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到转发按钮", startTime, listOf(), titleList)
                return false
            }
        } else {
            LogUtils.d("$groupName: 转发失败 未找到房间")
            uploadCommandResult(message, ExecCallbackBean.ERROR_INTO_ROOM, "$groupName: 转发失败 未找到房间", startTime, listOf(), titleList)
            return false
        }
    }

    /**
     * 批量转发 合并转发
     */
    private fun relayMultiMessage(
        message: WeworkMessageBean,
        titleList: List<String>,
        messageList: List<WeworkMessageBean.SubMessageBean>,
        nameList: List<String>,
        extraText: String? = null,
        key: String
    ): Boolean {
        val startTime = System.currentTimeMillis()
        if (messageList.isEmpty()) {
            LogUtils.e("转发内容为空")
            uploadCommandResult(message, ExecCallbackBean.ERROR_ILLEGAL_DATA, "转发内容为空", startTime, listOf(), titleList)
            return false
        } else if (messageList.size == 1) {
            val subMessageBean = messageList.first()
            val receivedName = subMessageBean.nameList?.firstOrNull()
            val textType = subMessageBean.textType
            val originalContent = subMessageBean.itemMessageList?.firstOrNull()?.text ?: ""
            LogUtils.d("receivedName $receivedName textType $textType originalContent $originalContent")
            return relayMessage(message, titleList, receivedName, originalContent, textType, nameList, extraText)
        }
        for (title in titleList) {
            if (WeworkRoomUtil.intoRoom(title)) {
                var hasOpenMulti = false
                for (subMessageBean in messageList) {
                    val receivedName = subMessageBean.nameList?.firstOrNull()
                    val textType = subMessageBean.textType
                    val originalContent = subMessageBean.itemMessageList?.firstOrNull()?.text ?: ""
                    LogUtils.d("receivedName $receivedName textType $textType originalContent $originalContent")
                    if (!hasOpenMulti) {
                        if (!receivedName.isNullOrEmpty()) {
                            if (WeworkTextUtil.longClickMessageItem(
                                    //聊天消息列表 1ListView 0RecycleView xViewGroup
                                    AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView),
                                    textType,
                                    receivedName,
                                    originalContent,
                                    "多选"
                                )
                            ) {
                                LogUtils.d("多选成功")
                                hasOpenMulti = true
                            } else {
                                LogUtils.e("$title: 多选失败")
                                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "多选失败 $originalContent", startTime, listOf(), titleList)
                                return false
                            }
                        } else {
                            if (WeworkTextUtil.longClickMyMessageItem(
                                    //聊天消息列表 1ListView 0RecycleView xViewGroup
                                    AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView),
                                    textType,
                                    originalContent,
                                    "多选"
                                )
                            ) {
                                LogUtils.d("多选成功")
                                hasOpenMulti = true
                            } else {
                                LogUtils.e("$title: 多选失败")
                                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "多选失败 $originalContent", startTime, listOf(), titleList)
                                return false
                            }
                        }
                    } else {
                        if (!receivedName.isNullOrEmpty()) {
                            if (WeworkTextUtil.longClickMessageItem(
                                    //聊天消息列表 1ListView 0RecycleView xViewGroup
                                    AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView),
                                    textType,
                                    receivedName,
                                    originalContent,
                                    "单击"
                                )
                            ) {
                                LogUtils.d("单击成功 $originalContent")
                                sleep(Constant.POP_WINDOW_INTERVAL / 2)
                            } else {
                                LogUtils.e("$title: 单击失败")
                                error("$title: 单击失败 $originalContent")
                            }
                        } else {
                            if (WeworkTextUtil.longClickMyMessageItem(
                                    //聊天消息列表 1ListView 0RecycleView xViewGroup
                                    AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView),
                                    textType,
                                    originalContent,
                                    "单击"
                                )
                            ) {
                                LogUtils.d("单击成功 $originalContent")
                                sleep(Constant.POP_WINDOW_INTERVAL / 2)
                            } else {
                                LogUtils.e("$title: 单击失败")
                                error("$title: 单击失败 $originalContent")
                            }
                        }
                    }
                }
                if (hasOpenMulti) {
                    val bottomList = AccessibilityUtil.findOnceByClazz(getRoot(), Views.ViewGroup, minChildCount = 4)
                    if (AccessibilityUtil.performClickWithSon(bottomList)) {
                        if (AccessibilityUtil.findTextAndClick(getRoot(), key)) {
                            if (relaySelectTarget(nameList, extraText)) {
                                LogUtils.d("$title: 转发成功")
                                uploadCommandResult(message, ExecCallbackBean.SUCCESS, "$title: 转发成功", startTime, titleList, listOf())
                                return true
                            } else {
                                LogUtils.e("$title: 转发失败")
                                uploadCommandResult(message, ExecCallbackBean.ERROR_RELAY, "$title: 转发失败", startTime, listOf(), titleList)
                                return false
                            }
                        } else {
                            LogUtils.e("未找到逐条转发按钮")
                            uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到逐条转发按钮", startTime, listOf(), titleList)
                            return false
                        }
                    } else {
                        LogUtils.e("未找到转发按钮")
                        uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到转发按钮", startTime, listOf(), titleList)
                        return false
                    }
                }
            } else {
                LogUtils.d("$title: 转发失败 未找到房间")
                uploadCommandResult(message, ExecCallbackBean.ERROR_INTO_ROOM, "$title: 转发失败 未找到房间", startTime, listOf(), titleList)
                return false
            }
        }
        LogUtils.d("转发失败 未找到房间名")
        uploadCommandResult(message, ExecCallbackBean.ERROR_ILLEGAL_DATA, "转发失败 未找到房间名", startTime, listOf(), titleList)
        return false
    }

    /**
     * 手机号添加好友或修改好友信息
     * @see WeworkMessageBean.ADD_FRIEND_BY_PHONE
     * @param friend 待添加用户
     */
    fun addFriendByPhone(
        message: WeworkMessageBean,
        friend: WeworkMessageBean.Friend
    ): Boolean {
        val startTime = System.currentTimeMillis()
        //如果已经是好友的可以传name修改好友信息
        if (friend.phone == null && friend.name != null) {
            if (getFriendInfo(friend.name)) {
                if (modifyFriendInfo(friend, addFriend = false)) {
                    LogUtils.d("修改好友信息成功: ${friend.name}")
                    uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, listOf(friend.name), listOf())
                    return true
                } else {
                    LogUtils.e("修改用户信息失败: ${friend.name}")
                    uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "修改用户信息失败: ${friend.name}", startTime, listOf(), listOf(friend.name))
                    return false
                }
            } else {
                LogUtils.e("未找到用户: ${friend.name}")
                uploadCommandResult(message, ExecCallbackBean.ERROR_TARGET, "未找到用户: ${friend.name}", startTime, listOf(), listOf(friend.name))
                return false
            }
        }
        //手机号添加好友
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
                    val button = AccessibilityUtil.findOneByText(list, "添加客户", "添加居民", "加微信", "添加学员", exact = true)
                    if (button != null) {
                        AccessibilityUtil.performClick(button)
                        sleep(Constant.POP_WINDOW_INTERVAL)
                        AccessibilityUtil.findTextAndClick(getRoot(), "搜索手机号添加")
                        AccessibilityUtil.findTextInput(getRoot(), friend.phone.trim())
                        if (AccessibilityUtil.findTextAndClick(getRoot(), "网络查找手机")) {
                            sleep(Constant.POP_WINDOW_INTERVAL)
                            val bothUsedTv = AccessibilityUtil.findOneByTextRegex(getRoot(), "(对方同时使用.*?)|(标签)|(电话)|(描述)|(设置备注和描述)", timeout = 2000)
                            val bothUsedText = bothUsedTv?.text
                            if (bothUsedText != null && bothUsedText.contains("对方同时使用")) {
                                AccessibilityUtil.performClick(
                                    AccessibilityUtil.findOnceByClazz(
                                        AccessibilityUtil.findBackNode(bothUsedTv),
                                        Views.ImageView
                                    )
                                )
                            } else if (AccessibilityUtil.findOnceByText(getRoot(), "该用户不存在") != null) {
                                LogUtils.e("该用户不存在: ${friend.phone}")
                                uploadCommandResult(message, ExecCallbackBean.ERROR_TARGET, "该用户不存在: ${friend.phone}", startTime, listOf(), listOf(friend.phone))
                                return false
                            }
                            if (modifyFriendInfo(friend)) {
                                if (AccessibilityUtil.findTextAndClick(getRoot(), "添加为联系人", timeout = 2000)) {
                                    LogUtils.d("准备发送好友邀请: ${friend.phone}")
                                    if (!friend.leavingMsg.isNullOrEmpty()) {
                                        AccessibilityUtil.findTextInput(getRoot(), friend.leavingMsg)
                                    }
                                    if (AccessibilityUtil.findTextAndClick(getRoot(), "发送添加邀请", "发送申请")) {
                                        LogUtils.d("发送添加邀请成功: ${friend.phone}")
                                        uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, listOf(friend.phone), listOf())
                                        return true
                                    } else {
                                        LogUtils.e("未找到发送邀请按钮")
                                        uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到发送邀请按钮", startTime, listOf(), listOf(friend.phone))
                                        return false
                                    }
                                } else {
                                    if (AccessibilityUtil.findOnceByText(getRoot(), "发消息", exact = true) != null) {
                                        LogUtils.e("已经添加联系人，请勿重复添加" + friend.phone)
                                        uploadCommandResult(message, ExecCallbackBean.ERROR_REPEAT, "已经添加联系人，请勿重复添加" + friend.phone, startTime, listOf(), listOf(friend.phone))
                                        return false
                                    } else {
                                        LogUtils.e("未找到添加为联系人")
                                        uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到添加为联系人", startTime, listOf(), listOf(friend.phone))
                                        return false
                                    }
                                }
                            } else {
                                LogUtils.e("修改用户信息失败: ${friend.phone}")
                                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "修改用户信息失败: ${friend.phone}", startTime, listOf(), listOf(friend.phone))
                                return false
                            }
                        } else {
                            LogUtils.e("未找到查找手机选项")
                            uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到查找手机选项", startTime, listOf(), listOf(friend.phone))
                            return false
                        }
                    } else {
                        LogUtils.e("未找到添加客户按钮")
                        uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到添加客户按钮", startTime, listOf(), listOf(friend.phone))
                        return false
                    }
                } else {
                    LogUtils.e("未找到添加客户列表")
                    uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到添加客户列表", startTime, listOf(), listOf(friend.phone))
                    return false
                }
            } else {
                LogUtils.e("未找到搜索按钮")
                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到搜索按钮", startTime, listOf(), listOf(friend.phone))
                return false
            }
        } else {
            LogUtils.e("未找到聊天列表")
            uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到聊天列表", startTime, listOf(), listOf(friend.phone))
            return false
        }
    }

    /**
     * 从外部群添加好友
     * @see WeworkMessageBean.ADD_FRIEND_BY_GROUP
     * @param groupName 外部群
     * @param friend 待添加用户
     */
    fun addFriendByGroup(
        message: WeworkMessageBean,
        groupName: String,
        friend: WeworkMessageBean.Friend
    ): Boolean {
        val startTime = System.currentTimeMillis()
        if (WeworkRoomUtil.intoRoom(groupName) && WeworkRoomUtil.intoGroupManager()) {
            if (AccessibilityUtil.findTextAndClick(getRoot(), "查看全部群成员")) {
                val title = friend.name
                val list = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
                if (list != null) {
                    val frontNode = AccessibilityUtil.findFrontNode(list)
                    val textViewList = AccessibilityUtil.findAllOnceByClazz(frontNode, Views.TextView)
                        .filter { it.text == null }
                    if (textViewList.size >= 2) {
                        val searchButton: AccessibilityNodeInfo = textViewList[textViewList.size - 1]
                        AccessibilityUtil.performClick(searchButton)
                        val needTrim = title.contains(Constant.regTrimTitle)
                        val trimTitle = title.replace(Constant.regTrimTitle, "")
                        AccessibilityUtil.findTextInput(getRoot(), trimTitle)
                        sleep(Constant.CHANGE_PAGE_INTERVAL)
                        //消息页搜索结果列表
                        val selectListView = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
                        val reverseRegexTitle = RegexHelper.reverseRegexTitle(trimTitle)
                        val regex1 = (if (Constant.friendRemarkStrict) "^$reverseRegexTitle" else "^(微信昵称:)?$reverseRegexTitle") +
                                (if (needTrim) ".*?" else "(-.*)?(…)?(\\(.*?\\))?$")
                        val regex2 = ".*?\\($reverseRegexTitle\\)$"
                        val regex = "($regex1)|($regex2)"
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
                                //过滤异常好友
                                if (searchResult?.parent != null && searchResult.parent.childCount < 3) {
                                    item.refresh()
                                    val imageView =
                                        AccessibilityUtil.findOneByClazz(item, Views.ImageView, root = false)
                                    AccessibilityUtil.performClick(imageView)
                                    break
                                }
                            }
                            if (modifyFriendInfo(friend)) {
                                if (AccessibilityUtil.findTextAndClick(getRoot(), "添加为联系人", timeout = 2000)) {
                                    LogUtils.d("准备发送好友邀请: ${friend.name}")
                                    if (!friend.leavingMsg.isNullOrEmpty()) {
                                        AccessibilityUtil.findTextInput(getRoot(), friend.leavingMsg)
                                    }
                                    if (AccessibilityUtil.findTextAndClick(getRoot(), "发送添加邀请", "发送申请")) {
                                        LogUtils.d("发送添加邀请成功: ${friend.name}")
                                        uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, listOf(friend.name), listOf())
                                        return true
                                    } else {
                                        if (AccessibilityUtil.findOnceByText(getRoot(), "发消息", exact = true) != null) {
                                            LogUtils.d("已经添加成功: ${friend.name}")
                                            uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, listOf(friend.name), listOf())
                                            return true
                                        }
                                        LogUtils.e("未找到发送邀请按钮")
                                        uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到发送邀请按钮", startTime, listOf(), listOf(friend.name))
                                        return false
                                    }
                                } else {
                                    if (AccessibilityUtil.findOnceByText(getRoot(), "发消息", exact = true) != null) {
                                        LogUtils.e("已经添加联系人，请勿重复添加: ${friend.name}")
                                        uploadCommandResult(message, ExecCallbackBean.ERROR_REPEAT, "已经添加联系人，请勿重复添加 ${friend.name}", startTime, listOf(), listOf(friend.name))
                                        return false
                                    } else {
                                        LogUtils.e("未找到添加为联系人")
                                        uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到添加为联系人", startTime, listOf(), listOf(friend.name))
                                        return false
                                    }
                                }
                            } else {
                                LogUtils.e("修改用户信息失败: ${friend.name}")
                                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "修改用户信息失败: ${friend.name}", startTime, listOf(), listOf(friend.name))
                                return false
                            }
                        } else {
                            LogUtils.e("未搜索到结果: ${friend.name}")
                            uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未搜索到结果: ${friend.name}", startTime, listOf(), listOf(friend.name))
                            return false
                        }
                    } else {
                        LogUtils.e("未发现搜索按钮")
                        uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未发现搜索按钮", startTime, listOf(), listOf(friend.name))
                        return false
                    }
                } else {
                    LogUtils.e("未发现通讯录列表")
                    uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未发现通讯录列表", startTime, listOf(), listOf(friend.name))
                    return false
                }
            } else {
                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到查看全部群成员按钮 $groupName", startTime, listOf(), listOf(friend.name))
                return false
            }
        } else {
            uploadCommandResult(message, ExecCallbackBean.ERROR_INTO_ROOM, "进入房间失败 $groupName", startTime, listOf(), listOf(friend.name))
            return false
        }
    }

    /**
     * 给群成员添加备注
     * @see WeworkMessageBean.MODIFY_GROUP_MEMBER_INFO
     * @param groupName 外部群
     * @param friend 待添加用户
     */
    fun modifyGroupMemberInfo(
        message: WeworkMessageBean,
        groupName: String,
        friend: WeworkMessageBean.Friend
    ): Boolean {
        val startTime = System.currentTimeMillis()
        if (WeworkRoomUtil.intoRoom(groupName) && WeworkRoomUtil.intoGroupManager()) {
            if (AccessibilityUtil.findTextAndClick(getRoot(), "查看全部群成员")) {
                val title = friend.name
                val list = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
                if (list != null) {
                    val frontNode = AccessibilityUtil.findFrontNode(list)
                    val textViewList = AccessibilityUtil.findAllOnceByClazz(frontNode, Views.TextView)
                        .filter { it.text == null }
                    if (textViewList.size >= 2) {
                        val searchButton: AccessibilityNodeInfo = textViewList[textViewList.size - 1]
                        AccessibilityUtil.performClick(searchButton)
                        val needTrim = title.contains(Constant.regTrimTitle)
                        val trimTitle = title.replace(Constant.regTrimTitle, "")
                        AccessibilityUtil.findTextInput(getRoot(), trimTitle)
                        sleep(Constant.CHANGE_PAGE_INTERVAL)
                        //消息页搜索结果列表
                        val selectListView = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
                        val reverseRegexTitle = RegexHelper.reverseRegexTitle(trimTitle)
                        val regex1 = (if (Constant.friendRemarkStrict) "^$reverseRegexTitle" else "^(微信昵称:)?$reverseRegexTitle") +
                                (if (needTrim) ".*?" else "(-.*)?(…)?(\\(.*?\\))?$")
                        val regex2 = ".*?\\($reverseRegexTitle\\)$"
                        val regex = "($regex1)|($regex2)"
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
                                //过滤异常好友
                                if (searchResult?.parent != null && searchResult.parent.childCount < 3) {
                                    item.refresh()
                                    val imageView =
                                        AccessibilityUtil.findOneByClazz(item, Views.ImageView, root = false)
                                    AccessibilityUtil.performClick(imageView)
                                    break
                                }
                            }
                            modifyFriendInfo(friend)
                            return false
                        } else {
                            LogUtils.e("未搜索到结果: ${friend.name}")
                            uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未搜索到结果: ${friend.name}", startTime, listOf(), listOf(friend.name))
                            return false
                        }
                    } else {
                        LogUtils.e("未发现搜索按钮")
                        uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未发现搜索按钮", startTime, listOf(), listOf(friend.name))
                        return false
                    }
                } else {
                    LogUtils.e("未发现通讯录列表")
                    uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未发现通讯录列表", startTime, listOf(), listOf(friend.name))
                    return false
                }
            } else {
                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到查看全部群成员按钮 $groupName", startTime, listOf(), listOf(friend.name))
                return false
            }
        } else {
            uploadCommandResult(message, ExecCallbackBean.ERROR_INTO_ROOM, "进入房间失败 $groupName", startTime, listOf(), listOf(friend.name))
            return false
        }
    }

    /**
     * 添加待办
     * @see WeworkMessageBean.ADD_NEED_DEAL
     * @param titleList 内部用户昵称列表
     * @param receivedContent 回复内容
     */
    fun addNeedDeal(
        message: WeworkMessageBean,
        titleList: MutableList<String>,
        receivedContent: String
    ): Boolean {
        val startTime = System.currentTimeMillis()
        goHome()
        if (AccessibilityUtil.findOnceByText(getRoot(), "日程", exact = true) == null) {
            AccessibilityUtil.scrollToTop(WeworkController.weworkService, getRoot())
        }
        val tvDiaryFlag = AccessibilityUtil.findOneByText(getRoot(), "日程", exact = true)
        if (tvDiaryFlag != null && (tvDiaryFlag.parent?.childCount == 2 || tvDiaryFlag.parent?.childCount == 3)) {
            AccessibilityUtil.performClick(tvDiaryFlag)
            val tvNeedDealFlag = AccessibilityUtil.findOneByTextRegex(getRoot(), "^待办( · .*?)?$")
            if (tvNeedDealFlag != null) {
                AccessibilityUtil.performClick(tvNeedDealFlag)
                sleep(Constant.POP_WINDOW_INTERVAL)
                val list = AccessibilityUtil.findOneByClazz(getRoot(), Views.RecyclerView, Views.ViewGroup, minChildCount = 2)
                if (list != null) {
                    val frontNode = AccessibilityUtil.findFrontNode(if (list.className == Views.RecyclerView) list.parent else list)
                    val textViewList =
                        AccessibilityUtil.findAllOnceByClazz(frontNode, Views.TextView)
                    if (textViewList.size >= 2) {
                        val addButton: AccessibilityNodeInfo = textViewList[textViewList.size - 1]
                        AccessibilityUtil.performClick(addButton)
                        AccessibilityUtil.findTextInput(getRoot(), receivedContent)
                        AccessibilityUtil.findTextAndClick(getRoot(), "参与人")
                        if (relaySelectTarget(titleList, needSend = false)) {
                            LogUtils.e("添加参与人成功")
                            if (AccessibilityUtil.findTextAndClick(getRoot(), "保存并发送到聊天", "保存并建群发送")) {
                                uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime, titleList, listOf())
                                return true
                            } else {
                                LogUtils.e("未找到保存并发送按钮")
                                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到保存并发送按钮", startTime, listOf(), titleList)
                                return false
                            }
                        } else {
                            LogUtils.e("添加参与人失败")
                            uploadCommandResult(message, ExecCallbackBean.ERROR_RELAY, "添加参与人失败: $titleList", startTime, listOf(), titleList)
                            return false
                        }
                    } else {
                        LogUtils.e("未找到添加按钮")
                        uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到添加按钮", startTime, listOf(), titleList)
                        return false
                    }
                } else {
                    LogUtils.e("未找到待办列表")
                    uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到待办列表", startTime, listOf(), titleList)
                    return false
                }
            } else {
                LogUtils.e("未找到待办按钮")
                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到待办按钮", startTime, listOf(), titleList)
                return false
            }
        } else {
            LogUtils.e("未找到日程按钮")
            uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到日程按钮", startTime, listOf(), titleList)
            return false
        }
    }

    /**
     * 打卡
     * @see WeworkMessageBean.CLOCK_IN
     */
    fun clockIn(message: WeworkMessageBean): Boolean {
        val startTime = System.currentTimeMillis()
        goHomeTab("工作台")
        val node = AccessibilityUtil.scrollAndFindByText(WeworkController.weworkService, getRoot(), "打卡")
        if (node != null) {
            AccessibilityUtil.performClick(node)
            sleep(Constant.POP_WINDOW_INTERVAL)
            val clockInFlag =
                AccessibilityUtil.findOneByText(getRoot(), "你已在打卡范围内", timeout = 10000)
            if (clockInFlag != null) {
                AccessibilityUtil.findTextAndClick(getRoot(), "上班打卡", "下班打卡")
                val doneFlag = AccessibilityUtil.findOneByText(getRoot(), "上班·正常", "之后可打下班卡", "今日打卡已完成")
                if (doneFlag != null) {
                    LogUtils.d("打卡成功")
                    uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime)
                    return true
                } else {
                    LogUtils.e("未发现完成打卡")
                    uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未发现完成打卡", startTime)
                    return false
                }
            } else {
                LogUtils.e("未找到上下班打卡")
                uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到上下班打卡", startTime)
                return false
            }
        } else {
            LogUtils.e("未找到在打卡范围")
            uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到在打卡范围", startTime)
            return false
        }
    }

    /**
     * 切换企业
     * @see WeworkMessageBean.SWITCH_CORP
     * @param objectName 企业名称
     */
    fun switchCorp(message: WeworkMessageBean, objectName: String): Boolean {
        val startTime = System.currentTimeMillis()
        goHomeTab("消息")
        val firstTv = AccessibilityUtil.findAllByClazz(getRoot(), Views.TextView)
            .firstOrNull { it.text == null }
        AccessibilityUtil.performClick(firstTv, retry = false)
        sleep(Constant.CHANGE_PAGE_INTERVAL)
        val listviewList = AccessibilityUtil.findAllOnceByClazz(getRoot(), Views.RecyclerView, Views.ListView, Views.ViewGroup)
            .filter { it.childCount >= 2 }
        val listview = listviewList.firstOrNull()
        if (listview != null) {
            val tvCorp = AccessibilityUtil.findOnceByText(listview, objectName, exact = true)
            if (tvCorp != null) {
                LogUtils.d("找到目标企业: $objectName")
                AccessibilityUtil.performClick(tvCorp)
                uploadCommandResult(message, ExecCallbackBean.SUCCESS, "切换企业成功: $objectName", startTime, listOf(objectName), listOf())
                goHome()
                WeworkGetImpl.getMyInfo(message)
                return true
            } else {
                LogUtils.e("未找到目标企业: $objectName")
                uploadCommandResult(message, ExecCallbackBean.ERROR_TARGET, "未找到目标企业: $objectName", startTime, listOf(), listOf(objectName))
                goHome()
                return false
            }
        } else {
            LogUtils.e("未找到企业列表: $objectName")
            uploadCommandResult(message, ExecCallbackBean.ERROR_BUTTON, "未找到企业列表: $objectName", startTime, listOf(), listOf(objectName))
            return false
        }
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
        message: WeworkMessageBean,
        titleList: MutableList<String>,
        receivedName: String,
        originalContent: String,
        textType: Int
    ): Boolean {
        val startTime = System.currentTimeMillis()
        for (groupName in titleList) {
            if (WeworkRoomUtil.intoRoom(groupName) && WeworkRoomUtil.intoGroupManager()) {
                val groupInfo = WeworkGetImpl.getGroupInfoDetail()
                groupInfo.titleList = arrayListOf(groupName)
                groupInfo.type = WeworkMessageBean.SHOW_GROUP_INFO
                groupInfo.receivedName = receivedName
                groupInfo.originalContent = originalContent
                groupInfo.textType = textType
                WeworkController.weworkService.webSocketManager.send(groupInfo)
                uploadCommandResult(message, ExecCallbackBean.SUCCESS, "", startTime)
                return true
            }
        }
        uploadCommandResult(message, ExecCallbackBean.ERROR_TARGET, "", startTime)
        return false
    }

    /**
     * 转发消息到目标列表
     * 支持场景：长按消息转发、微盘图片转发
     * selectList 昵称或群名列表
     * extraText 转发是否附加一条文本
     */
    private fun relaySelectTarget(selectList: List<String>, extraText: String? = null, needSend: Boolean = true, timeout: Long = 5000): Boolean {
        if (AccessibilityUtil.findOneByText(getRoot(), "选择联系人", "选择参与人", exact = true, timeout = timeout) == null) {
            LogUtils.e("未找到选择联系人/选择参与人")
            return false
        }
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
                    val reverseRegexTitle = RegexHelper.reverseRegexTitle(trimTitle)
                    val regex1 = (if (Constant.friendRemarkStrict) "^$reverseRegexTitle" else "^(微信昵称:)?$reverseRegexTitle") +
                            (if (needTrim) ".*?" else "(-.*)?(…)?(\\(.*?\\))?$")
                    val regex2 = ".*?\\($reverseRegexTitle\\)$"
                    val regex = "($regex1)|($regex2)"
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
                            //过滤已退出的群聊
                            if (searchResult?.parent != null && searchResult.parent.childCount < 3) {
                                item.refresh()
                                val imageView =
                                    AccessibilityUtil.findOneByClazz(item, Views.ImageView, root = false)
                                AccessibilityUtil.performClick(imageView)
                                break
                            }
                        }
                    }
                    if (matchSelect != null) {
                        LogUtils.d("找到搜索结果: $select")
                    } else {
                        LogUtils.e("未搜索到结果: $select")
                    }
                    sleep(Constant.POP_WINDOW_INTERVAL)
                }
                val confirmButton =
                    AccessibilityUtil.findOneByTextRegex(getRoot(), "^确定(\\(.*?\\))?\$")
                if (confirmButton != null) {
                    AccessibilityUtil.performClick(confirmButton)
                    sleep(Constant.POP_WINDOW_INTERVAL)
                    if (!needSend) {
                        return true
                    }
                    if (!extraText.isNullOrBlank()) {
                        LogUtils.d("extraText: $extraText")
                        AccessibilityUtil.findTextInput(getRoot(), extraText)
                    }
                    val sendButton = AccessibilityUtil.findOneByTextRegex(getRoot(), "^发送(\\(.*?\\))?\$")
                    if (sendButton != null) {
                        AccessibilityUtil.performClick(sendButton)
                        return true
                    }
                    LogUtils.e("未发现发送按钮")
                    return false
                } else {
                    LogUtils.e("未发现确认按钮")
                    return false
                }
            } else {
                LogUtils.e("未发现搜索和多选按钮")
                return false
            }
        }
        LogUtils.e("未知错误")
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
     * 建群前检查是否达到当日建群上限
     * @return true允许建群 false不允许建群
     */
    private fun beforeCreateGroupCheck(): Boolean {
        //有建群权限且最近300秒内发现限制建群
        if (SPUtils.getInstance("limit").getBoolean("canCreateGroup", false)) {
            val interval = System.currentTimeMillis() / 1000 - SPUtils.getInstance("limit").getLong("createGroupLimit", 0)
            if (interval < 300) {
                LogUtils.e("发现达到当日建群上限 请等待${300 - interval}秒后再试!")
                return false
            }
        }
        return true
    }

    /**
     * 建群后检查是否达到当日建群上限
     * @return true达到上限 false为达到上限
     */
    private fun createGroupLimit(): Boolean {
        val hasLimit =
            AccessibilityUtil.findOneByText(getRoot(), "新建群聊功能暂时被限制", "未验证企业", timeout = 2000)
        if (hasLimit == null) {
            SPUtils.getInstance("limit").put("canCreateGroup", true)
        } else if (SPUtils.getInstance("limit").getBoolean("canCreateGroup", false)) {
            SPUtils.getInstance("limit").put("createGroupLimit", System.currentTimeMillis() / 1000)
            LogUtils.e("发现达到当日建群上限")
        }
        return hasLimit != null
    }

    /**
     * 修改群名称
     */
    private fun groupRename(groupName: String?): Boolean {
        if (groupName == null) return true
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
                    sleep(Constant.CHANGE_PAGE_INTERVAL * 2)
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
                    sleep(Constant.CHANGE_PAGE_INTERVAL * 2)
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
        selectList: List<String>?,
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
                } else if (tvEmptySize == 1 || tvEmptySize == 2) {
                    AccessibilityUtil.performClick(gridView.getChild(gridView.childCount - tvEmptySize))
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
                    var count = 0
                    for (select in selectList) {
                        val needTrim = select.contains(Constant.regTrimTitle)
                        val trimTitle = select.replace(Constant.regTrimTitle, "")
                        AccessibilityUtil.performClick(multiButton)
                        AccessibilityUtil.findTextInput(getRoot(), trimTitle)
                        sleep(Constant.POP_WINDOW_INTERVAL)
                        val selectListView = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView, Views.RecyclerView, Views.ViewGroup, minChildCount = 2, firstChildClazz = Views.TextView)
                        val reverseRegexTitle = RegexHelper.reverseRegexTitle(trimTitle)
                        val regex1 = (if (Constant.friendRemarkStrict) "^$reverseRegexTitle" else "^(微信昵称:)?$reverseRegexTitle") +
                                (if (needTrim) ".*?" else "(-.*)?(…)?(\\(.*?\\))?$")
                        val regex2 = ".*?\\($reverseRegexTitle\\)$"
                        val regex = "($regex1)|($regex2)"
                        val matchSelect = AccessibilityUtil.findOneByTextRegex(
                            selectListView,
                            regex,
                            timeout = 2000,
                            root = false
                        )
                        if (selectListView != null && matchSelect != null) {
                            var flag = false
                            for (i in 0 until selectListView.childCount) {
                                val item = selectListView.getChild(i)
                                val searchResult = AccessibilityUtil.findOnceByTextRegex(item, regex)
                                if (searchResult != null) {
                                    item.refresh()
                                    val imageView =
                                        AccessibilityUtil.findOneByClazz(item, Views.ImageView, root = false)
                                    if (imageView != null && !imageView.isEnabled) {
                                        flag = true
                                    } else if (AccessibilityUtil.performClick(imageView)) {
                                        flag = true
                                        count += 1
                                    }
                                }
                            }
                            if (!flag) {
                                LogUtils.e("拉人失败 找不到: $select")
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
                            LogUtils.e("未搜索到结果: $select")
                            if (Constant.groupStrict) return false
                        }
                    }
                    if (count == 0) {
                        while (AccessibilityUtil.findOnceByText(getRoot(), "全部群成员", "微信用户创建") == null && !isAtHome()) {
                            backPress()
                        }
                        LogUtils.d("拉入0人")
                        return true
                    }
                    if (showMessageHistory) {
                        AccessibilityUtil.findTextAndClick(getRoot(), "聊天记录")
                    }
                    val confirmButton =
                        AccessibilityUtil.findOneByTextRegex(getRoot(), "^确定(\\(.*?\\))?\$")
                    if (confirmButton != null) {
                        AccessibilityUtil.performClick(confirmButton)
                        if (AccessibilityExtraUtil.loadingPage("CustomDialog", timeout = Constant.POP_WINDOW_INTERVAL)) {
                            AccessibilityUtil.findTextAndClick(getRoot(), "邀请", exact = true)
                            log("群邀请: ${selectList.joinToString()}")
                        }
                        return true
                    } else {
                        LogUtils.e("未发现确认按钮")
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
        } else {
            LogUtils.e("进入群详情失败")
            return false
        }
    }

    /**
     * 移除群成员/踢人
     */
    private fun groupRemoveMember(removeList: List<String>?): Boolean {
        if (removeList.isNullOrEmpty()) return true
        if (WeworkRoomUtil.intoGroupManager()) {
            val gridView = AccessibilityUtil.findOneByClazz(getRoot(), Views.GridView)
            if (gridView != null && gridView.childCount >= 2) {
                val tvEmptySize = AccessibilityUtil.findAllOnceByClazz(gridView, Views.TextView)
                    .filter { it.text == null }.size
                LogUtils.v("tvEmptySize: $tvEmptySize")
                if (tvEmptySize <= 1) {
                    LogUtils.e("未找到踢人按钮")
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
                    var count = 0
                    for (select in removeList) {
                        val needTrim = select.contains(Constant.regTrimTitle)
                        val trimTitle = select.replace(Constant.regTrimTitle, "")
                        AccessibilityUtil.performClick(multiButton)
                        AccessibilityUtil.findTextInput(getRoot(), trimTitle)
                        sleep(Constant.POP_WINDOW_INTERVAL)
                        val selectListView = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView, Views.RecyclerView, Views.ViewGroup, minChildCount = 2, firstChildClazz = Views.RelativeLayout)
                        val reverseRegexTitle = RegexHelper.reverseRegexTitle(trimTitle)
                        val regex1 = (if (Constant.friendRemarkStrict) "^$reverseRegexTitle" else "^(微信昵称:)?$reverseRegexTitle") +
                                (if (needTrim) ".*?" else "(-.*)?(…)?(\\(.*?\\))?$")
                        val regex2 = ".*?\\($reverseRegexTitle\\)$"
                        val regex = "($regex1)|($regex2)"
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
                                    if (AccessibilityUtil.performClick(imageView)) {
                                        count += 1
                                    }
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
                            LogUtils.e("未搜索到结果: $select")
                            //待踢人已经不在群里的不算失败
//                            if (Constant.groupStrict) return false
                        }
                    }
                    if (count == 0) {
                        while (AccessibilityUtil.findOnceByText(getRoot(), "全部群成员", "微信用户创建") == null && !isAtHome()) {
                            backPress()
                        }
                        LogUtils.d("移出0人")
                        return true
                    }
                    val confirmButton =
                        AccessibilityUtil.findOneByText(getRoot(), "移出(")
                    if (confirmButton != null) {
                        AccessibilityUtil.performClick(confirmButton)
                        return true
                    } else {
                        LogUtils.e("未发现移出按钮")
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
        } else {
            LogUtils.e("进入群详情失败")
            return false
        }
    }

    /**
     * 修改群公告
     * 注：首次为发布 后续为编辑
     * 注2：外部群为edittext 内部群为webview(只能追加文本)
     */
    private fun groupChangeAnnouncement(groupAnnouncement: String?): Boolean {
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
                        sleep(Constant.CHANGE_PAGE_INTERVAL * 3)
                    } else {
                        LogUtils.e("无法进行群公告发布")
                    }
                } else {
                    LogUtils.e("无法进行群公告发布和编辑")
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
    private fun groupChangeRemark(groupRemark: String?): Boolean {
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
                        LogUtils.e("无法进行群备注发布")
                    }
                } else {
                    LogUtils.e("无法进行群备注修改")
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
     * 修改群模板
     */
    private fun groupTemplate(groupTemplate: String?): Boolean {
        if (groupTemplate == null) return true
        if (WeworkRoomUtil.intoGroupManager()) {
            val textView = AccessibilityUtil.findOneByText(getRoot(), "使用模板快速配置群", exact = true)
            if (textView != null) {
                AccessibilityUtil.performClick(textView)
                val item = AccessibilityUtil.findOneByDesc(getRoot(), groupTemplate)
                if (item != null) {
                    AccessibilityUtil.performClick(item)
                    sleep(Constant.POP_WINDOW_INTERVAL)
                    val useTemplateTv = AccessibilityUtil.findOneByDesc(getRoot(), "使用该模板")
                    if (useTemplateTv != null) {
                        AccessibilityUtil.performClick(useTemplateTv)
                        var useTv = AccessibilityUtil.findOneByDesc(getRoot(), "使用", exact = true, timeout = 2000)
                        if (useTv == null) {
                            val useTemplateTv = AccessibilityUtil.findOneByDesc(getRoot(), "使用该模板")
                            if (useTemplateTv != null) {
                                AccessibilityUtil.performClick(useTemplateTv)
                            }
                            useTv = AccessibilityUtil.findOneByDesc(getRoot(), "使用", exact = true, timeout = 2000)
                        }
                        if (useTv != null) {
                            AccessibilityUtil.performClick(useTv)
                            return true
                        } else {
                            LogUtils.e("未找到使用按钮: $groupTemplate")
                        }
                    } else {
                        LogUtils.e("未找到使用该模板按钮: $groupTemplate")
                    }
                } else {
                    LogUtils.e("未找到指定配置: $groupTemplate")
                }
            } else {
                LogUtils.e("未找到使用模板按钮")
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
        val roomType = WeworkRoomUtil.getRoomType()
        val voiceFlag = AccessibilityUtil.findOnceByText(getRoot(), "按住 说话", "按住说话", exact = true)
        if (voiceFlag != null) {
            AccessibilityUtil.performClickWithSon(AccessibilityUtil.findFrontNode(voiceFlag))
        }
        var atFailed = false
        val atList = if (!at.isNullOrEmpty()) arrayListOf(at) else atList?.toMutableList()
        if (!atList.isNullOrEmpty() && (roomType == WeworkMessageBean.ROOM_TYPE_INTERNAL_GROUP || roomType == WeworkMessageBean.ROOM_TYPE_EXTERNAL_GROUP)) {
            if (atList.count { it.startsWith("#regex#") } > 0 && WeworkRoomUtil.intoGroupManager()) {
                val groupInfo = WeworkGetImpl.getGroupInfoDetail(saveAddress = false, saveMembers = true)
                val regex = atList.first { it.startsWith("#regex#") }.split("#regex#").last().toRegex()
                if (groupInfo.nameList != null) {
                    for (name in groupInfo.nameList) {
                        if (name != Constant.myName && name.matches(regex)) {
                            atList.add(name)
                        }
                    }
                }
            }
            atList.removeIf { it.startsWith("#regex#") }
            LogUtils.v("atList: ${atList.joinToString()}")
            atList.forEachIndexed { index, at ->
                if (index == 0) {
                    AccessibilityUtil.findTextInput(getRoot(), "@")
                } else {
                    AccessibilityUtil.sendTextForEditText(Utils.getApp(),
                        AccessibilityUtil.findOnceByClazz(getRoot(), Views.EditText), "@"
                    )
                }
                val atFlag = AccessibilityUtil.findOneByText(getRoot(), "选择提醒的人", exact = true)
                if (atFlag != null) {
                    val searchFlag = AccessibilityUtil.findOneByText(getRoot(), "搜索", exact = true)
                    val container = AccessibilityUtil.findBackNode(searchFlag, minChildCount = 2)?.parent
                    if (container != null) {
                        val atNode = AccessibilityUtil.findOnceByTextRegex(container, "${RegexHelper.reverseRegexTitle(at)}(@.*)?")
                        if (atNode != null && !at.matches("^[A-Z#]$".toRegex())) {
                            AccessibilityUtil.performClick(atNode)
                        } else {
                            AccessibilityUtil.findTextInput(getRoot(), at)
                            val atNodeList = AccessibilityUtil.findAllByTextRegex(container, "${RegexHelper.reverseRegexTitle(at)}(@.*)?", root = false, minSize = 2)
                            if (atNodeList.size > 1 && at != "@所有人") {
                                AccessibilityUtil.performClick(atNodeList[1])
                            } else {
                                LogUtils.e("未找到at人: $at")
                                atFailed = true
                                backPress()
                            }
                        }
                        sleep(Constant.POP_WINDOW_INTERVAL)
                    } else {
                        LogUtils.e("未找到搜索按钮和@列表")
                        backPress()
                        sleep(Constant.POP_WINDOW_INTERVAL)
                    }
                } else {
                    LogUtils.e("未找到选择提醒的人按钮")
                }
            }
        }
        LogUtils.v("atFailed: $atFailed")
        val content = if (atFailed) "@${atList?.joinToString()} $text" else text
        val append = (reply == true) || (!atList.isNullOrEmpty() && !atFailed)
        WeworkLoopImpl.getChatMessageList(needInfer = false, imageCheck = false)
        if (AccessibilityUtil.findTextInput(getRoot(), content, append = append)) {
            AccessibilityUtil.findOneByText(getRoot(), "发送", exact = true, timeout = 2000)
            val sendButton = AccessibilityUtil.findAllByClazz(getRoot(), Views.Button)
                .firstOrNull { it.text?.toString() == "发送" }
            if (sendButton != null) {
                LogUtils.d("发送消息: \n$content")
                log("发送消息: \n$content")
                AccessibilityUtil.performClick(sendButton)
                sleep(Constant.POP_WINDOW_INTERVAL)
                WeworkLoopImpl.getChatMessageList(needInfer = false)
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
     * 从通讯录查询好友信息
     */
    private fun getFriendInfo(title: String): Boolean {
        goHomeTab("通讯录")
        val list = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView, Views.RecyclerView)
        if (list != null) {
            val frontNode = AccessibilityUtil.findFrontNode(list, minChildCount = 2)
            val textViewList = AccessibilityUtil.findAllOnceByClazz(frontNode, Views.TextView)
            if (textViewList.size >= 2) {
                val searchButton: AccessibilityNodeInfo = textViewList[textViewList.size - 2]
                val multiButton: AccessibilityNodeInfo = textViewList[textViewList.size - 1]
                AccessibilityUtil.performClick(searchButton)
                val needTrim = title.contains(Constant.regTrimTitle)
                val trimTitle = title.replace(Constant.regTrimTitle, "")
                AccessibilityUtil.findTextInput(getRoot(), trimTitle)
                sleep(Constant.CHANGE_PAGE_INTERVAL)
                //消息页搜索结果列表
                val selectListView = AccessibilityUtil.findOneByClazz(getRoot(), Views.ListView)
                val reverseRegexTitle = RegexHelper.reverseRegexTitle(trimTitle)
                val regex1 = (if (Constant.friendRemarkStrict) "^$reverseRegexTitle" else "^(微信昵称:)?$reverseRegexTitle") +
                        (if (needTrim) ".*?" else "(-.*)?(…)?(\\(.*?\\))?$")
                val regex2 = ".*?\\($reverseRegexTitle\\)$"
                val regex = "($regex1)|($regex2)"
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
                        //过滤异常好友
                        if (searchResult?.parent != null && searchResult.parent.childCount < 3) {
                            item.refresh()
                            val imageView =
                                AccessibilityUtil.findOneByClazz(item, Views.ImageView, root = false)
                            AccessibilityUtil.performClick(imageView)
                            break
                        }
                    }
                }
                if (matchSelect != null) {
                    LogUtils.d("找到搜索结果: $title")
                    return true
                } else {
                    LogUtils.e("未搜索到结果: $title")
                }
            } else {
                LogUtils.e("ActionBar获取失败: $title")
            }
        } else {
            LogUtils.e("通讯录列表获取失败: $title")
        }
        return false
    }

    /**
     * 修改好友信息
     */
    private fun modifyFriendInfo(friend: WeworkMessageBean.Friend, addFriend: Boolean = true): Boolean {
        if (AccessibilityUtil.findOneByText(getRoot(), "标签", "电话", "描述", "设置备注和描述", exact = true) != null) {
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
                    AccessibilityUtil.findAllByClazz(getRoot(), Views.EditText, minSize = 2)
                if (etList.size >= 4) {
                    //微信用户 备注/企业/电话/电话/描述
                    if (friend.markName != null) {
                        AccessibilityUtil.editTextInput(etList[0], friend.markName)
                    }
                    if (friend.markCorp != null) {
                        AccessibilityUtil.editTextInput(etList[1], friend.markCorp)
                    }
                    if (friend.markExtra != null) {
                        AccessibilityUtil.editTextInput(etList[etList.size - 1], friend.markExtra)
                    }
                } else if (etList.size == 2) {
                    //同企业内部用户 备注/描述
                    if (friend.markName != null) {
                        AccessibilityUtil.editTextInput(etList[0], friend.markName)
                    }
                    if (friend.markExtra != null) {
                        AccessibilityUtil.editTextInput(etList[etList.size - 1], friend.markExtra)
                    }
                } else if (etList.size == 3) {
                    //外部企业用户 备注/电话/描述
                    if (friend.markName != null) {
                        AccessibilityUtil.editTextInput(etList[0], friend.markName)
                    }
                    if (friend.markExtra != null) {
                        AccessibilityUtil.editTextInput(etList[etList.size - 1], friend.markExtra)
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
            if (!addFriend) {
                return true
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
                    LogUtils.d("好友昵称或备注名: ${tvNick.text}")
                }
            }
            return true
        } else {
            if (AccessibilityUtil.findOnceByText(getRoot(), "无权查看") != null) {
                LogUtils.e("无权查看该用户")
                return false
            }
            LogUtils.e("未找到标签")
            return false
        }
    }

    /**
     * 设置好友标签
     */
    fun setFriendTags(tagList: List<String>): Boolean {
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
                    if (tagList.count { it == text?.toString() } > 0) {
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
                val textNode = AccessibilityUtil.findOneByText(getRoot(), "确定", "个人信息")
                if (textNode?.text?.toString() == "确定") {
                    AccessibilityUtil.performClick(textNode)
                }
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
        if (!Constant.groupQrCode) return true
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
                            if (fileTime.toLongOrNull() ?: 0 > currentTime) {
                                LogUtils.d("找到最新保存二维码图片: $fileTime")
                                try {
                                    val bitmap = ImageUtils.bytes2Bitmap(file.readBytes())
                                    val mDecoder = QRCodeDecoder.Builder().build()
                                    val qrcode = mDecoder.decode(bitmap)
                                    LogUtils.d("group: $groupName qrcode: $qrcode")
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