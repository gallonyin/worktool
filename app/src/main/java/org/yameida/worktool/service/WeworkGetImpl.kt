package org.yameida.worktool.service

import com.blankj.utilcode.util.LogUtils
import org.yameida.worktool.Constant
import org.yameida.worktool.model.WeworkMessageBean
import org.yameida.worktool.utils.AccessibilityUtil
import org.yameida.worktool.utils.Views
import org.yameida.worktool.utils.WeworkRoomUtil

/**
 * 获取数据类型 500 实现类
 */
object WeworkGetImpl {

    /**
     * 获取群信息
     * @param selectList 群名列表 为空时去群管理页查询并返回群聊页
     */
    fun getGroupInfo(selectList: List<String>): Boolean {
        if (selectList.isNullOrEmpty()) {
            WeworkRoomUtil.intoGroupManager()
            val groupInfo = getGroupInfoDetail()
            WeworkController.weworkService.webSocketManager.send(groupInfo)
            backPress()
        } else {
            for (groupName in selectList) {
                if (WeworkRoomUtil.intoRoom(groupName) && WeworkRoomUtil.intoGroupManager()) {
                    val groupInfo = getGroupInfoDetail()
                    WeworkController.weworkService.webSocketManager.send(groupInfo)
                }
            }
        }
        return true
    }

    /**
     * 获取好友信息
     * @param selectList 好友名列表
     */
    fun getFriendInfo(selectList: List<String>): Boolean {
        return true
    }

    /**
     * 获取我的信息
     */
    fun getMyInfo(): Boolean {
        if (!goHomeTab("我")) {
            LogUtils.d("未找到我的信息")
            goHomeTab("消息")
            val firstTv = AccessibilityUtil.findAllByClazz(getRoot(), Views.TextView)
                .firstOrNull { it.text == null }
            AccessibilityUtil.performClick(firstTv)
            sleep(Constant.CHANGE_PAGE_INTERVAL)
            val newFirstTv = AccessibilityUtil.findOneByClazz(getRoot(), Views.TextView)
            val nickname = newFirstTv?.text?.toString()
            AccessibilityUtil.performClick(firstTv)
            if (nickname != null) {
                Constant.myName = nickname
                LogUtils.d("我的昵称: ${Constant.myName}")
                val weworkMessageBean = WeworkMessageBean()
                weworkMessageBean.type = WeworkMessageBean.GET_MY_INFO
                weworkMessageBean.myInfo = WeworkMessageBean.MyInfo().apply { name = nickname }
                WeworkController.weworkService.webSocketManager.send(weworkMessageBean)
                return true
            } else {
                LogUtils.d("未找到我的昵称")
                return false
            }
        }
        AccessibilityUtil.performClick(AccessibilityUtil.findOneByClazz(getRoot(), Views.ImageView))
        sleep(Constant.CHANGE_PAGE_INTERVAL)
        val relativeLayoutList = AccessibilityUtil.findAllByClazz(getRoot(), Views.RelativeLayout, minSize = 50)
        val myInfo = WeworkMessageBean.MyInfo()
        for (relativeLayout in relativeLayoutList.filter { it.childCount >= 2 }) {
            val textViewList = AccessibilityUtil.findAllOnceByClazz(relativeLayout, Views.TextView)
            if (textViewList.size >= 2) {
                val firstText = textViewList[0].text?.toString()
                if (firstText == "姓名" && myInfo.name == null) {
                    myInfo.name = textViewList[1].text?.toString() ?: ""
                    Constant.myName = myInfo.name
                    LogUtils.d("我的昵称: ${Constant.myName}")
                }
                if (firstText == "别名" && myInfo.alias == null) {
                    myInfo.alias = textViewList[1].text?.toString() ?: ""
                }
                if (firstText == "性别" && myInfo.gender == null) {
                    myInfo.gender = textViewList[1].text?.toString() ?: ""
                }
                if (firstText == "对外信息显示" && myInfo.showName == null) {
                    myInfo.showName = textViewList[1].text?.toString() ?: ""
                }
                if (firstText == "工作签名" && myInfo.workSign == null) {
                    myInfo.workSign = textViewList[1].text?.toString() ?: ""
                }
                if (firstText == "所在企业" && myInfo.corporation == null) {
                    myInfo.corporation = textViewList[1].text?.toString() ?: ""
                }
                if (firstText == "手机" && myInfo.phone == null) {
                    myInfo.phone = textViewList[1].text?.toString() ?: ""
                }
                if (firstText == "职务" && myInfo.job == null) {
                    myInfo.job = textViewList[1].text?.toString() ?: ""
                }
            }
        }
        LogUtils.d("我的信息", myInfo)
        val weworkMessageBean = WeworkMessageBean()
        weworkMessageBean.type = WeworkMessageBean.GET_MY_INFO
        weworkMessageBean.myInfo = myInfo
        WeworkController.weworkService.webSocketManager.send(weworkMessageBean)
        return true
    }

    /**
     * 获取群名、群主、群成员数、群公告
     */
    fun getGroupInfoDetail(): WeworkMessageBean {
        val weworkMessageBean = WeworkMessageBean()
        weworkMessageBean.type = WeworkMessageBean.GET_GROUP_INFO
        val tvManagerFlag = AccessibilityUtil.findOneByText(getRoot(), "微信用户创建", timeout = 2000)
        //不是管理员的群可能没有微信用户创建 todo
//            AccessibilityUtil.findOneByText(getRoot(), "全部群成员", timeout = 2000)
        val button = AccessibilityUtil.findFrontNode(tvManagerFlag)
        val tvGroupName = AccessibilityUtil.findOnceByClazz(button, Views.TextView)
        if (tvGroupName != null && tvGroupName.text != null) {
            LogUtils.d("群名: " + tvGroupName.text)
            weworkMessageBean.groupName = tvGroupName.text.toString()
        } else {
            val groupNameTv = AccessibilityUtil.findOnceByText(getRoot(), "群聊名称")
            if (groupNameTv != null) {
                val tvList = AccessibilityUtil.findAllOnceByClazz(
                    groupNameTv.parent.parent.parent,
                    Views.TextView
                )
                if (tvList.size >= 2) {
                    val groupName = tvList[1]
                    LogUtils.d("群名: " + groupName.text)
                    weworkMessageBean.groupName = groupName.text.toString()
                }
            }
        }
        val gridView = AccessibilityUtil.findOneByClazz(getRoot(), Views.GridView)
        if (gridView != null && gridView.childCount >= 2) {
            val tvOwnerName = AccessibilityUtil.findOnceByClazz(gridView.getChild(0), Views.TextView)
            if (tvOwnerName != null && tvOwnerName.text != null) {
                LogUtils.d("群主: " + tvOwnerName.text)
                weworkMessageBean.groupOwner = tvOwnerName.text.toString()
            }
        }
        val tvCountFlag = AccessibilityUtil.findOnceByText(getRoot(), "查看全部群成员")
        val tvCount = AccessibilityUtil.findBackNode(tvCountFlag)
        if (tvCount != null && tvCount.text != null) {
            LogUtils.d("群成员: " + tvCount.text)
            val count = tvCount.text.toString().replace("人", "")
            weworkMessageBean.groupNumber = count.toIntOrNull()
        }
        val tvAnnouncementFlag = AccessibilityUtil.findOnceByText(getRoot(), "群公告")
        val tvAnnouncement = AccessibilityUtil.findBackNode(tvAnnouncementFlag)
        if (tvAnnouncement != null && tvAnnouncement.text != null) {
            LogUtils.d("群公告: " + tvAnnouncement.text)
            weworkMessageBean.groupAnnouncement = tvAnnouncement.text.toString()
        }
        backPress()
        return weworkMessageBean
    }
}