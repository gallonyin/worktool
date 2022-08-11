package org.yameida.worktool

import com.blankj.utilcode.util.TimeUtils
import org.yameida.worktool.model.WeworkMessageBean
import org.yameida.worktool.service.MyLooper
import org.yameida.worktool.service.WeworkController
import org.yameida.worktool.service.WeworkLoopImpl
import org.yameida.worktool.service.getRoot
import org.yameida.worktool.utils.AccessibilityUtil
import java.util.*

/**
 * 示例
 */
object Demo {

    fun test(flag: Boolean) {
        if (!flag) return

        MyLooper.getInstance().removeCallbacksAndMessages(null)

        //打印当前视图树
//        AccessibilityUtil.printNodeClazzTree(getRoot())

        //获取群二维码
//        WeworkOperationImpl.getGroupQrcode("测试群01")

        //手机号添加好友
//        WeworkOperationImpl.addFriendByPhone(WeworkMessageBean.Friend().apply {
//            this.phone = "13010001000"
//            this.markName = "hhh"
//            this.markCorp = "corp"
//            this.markExtra = "ex"
//            this.tagList = arrayListOf("tag1", "tag2")
//        })

        //自动通过好友
//        WeworkLoopImpl.getFriendRequest()

        //自动通过好友(后台可配置开关)
//        WeworkLoopImpl.mainLoop()

        //创建群信息
//        WeworkController.initGroup(WeworkMessageBean().apply {
//            groupName = "新建外部群 " + UUID.randomUUID().toString().substring(0, 5)
//            selectList = arrayListOf("冯燕", "尹甲仑")
//            groupAnnouncement = "本群为雨花台区法院诉前调解官方微信群"
//        })

        //修改群信息
//        WeworkController.intoGroupAndConfig(WeworkMessageBean().apply {
//            groupName = "新建外部群 " + UUID.randomUUID().toString().substring(0, 5)
//            selectList = arrayListOf("冯燕", "尹甲仑")
//            groupAnnouncement = "本群为雨花台区法院诉前调解官方微信群"
//        })

        //获取群信息
//        WeworkController.getGroupInfo(WeworkMessageBean().apply {
//            selectList = arrayListOf("企微RPA机器人自测1")
//        })

        //在房间内发送消息
//        WeworkController.sendMessage(WeworkMessageBean().apply {
//            titleList = arrayListOf("下级群1", "上级群1")
//            receivedContent = "aaa"
//        })

        //获取我的信息
//        WeworkController.getMyInfo()

        //推送任意小程序
//        WeworkController.pushMicroprogram(WeworkMessageBean().apply {
//            titleList = arrayListOf("尹甲仑")
//            objectName = "小法名律"
//            extraText = "123"
//        })

        //推送微盘图片
//        WeworkController.pushMicroDiskImage(WeworkMessageBean().apply {
//            titleList = arrayListOf("尹甲仑")
//            objectName = "雨水.jpg"
//        })

        //推送微盘文件
//        WeworkController.pushMicroDiskFile(WeworkMessageBean().apply {
//            titleList = arrayListOf("尹甲仑")
//            objectName = "雨水.jpg"
//        })

        //推送腾讯文档
//        WeworkController.pushOffice(WeworkMessageBean().apply {
//            titleList = arrayListOf("尹甲仑")
//            objectName = "机器人中台"
//            extraText = "附加留言"
//        })
    }

    fun test2(name: String) {
        val time = TimeUtils.date2String(Date(), "MMddHHmm")
        val groupName = "测试群$time"
        val json = """
            {
                "socketType":2,
                "list":[
                    {
                        "type":203,
                        "titleList":[
                            "$name"
                        ],
                        "receivedContent":"你好~我是机器人，你可以@我和我聊天，你也可以通过API文档来让我发送消息或完成建群等任务。接口文档：https://www.apifox.cn/apidoc/project-1035094/api-23520034"
                    },
                    {
                      "type": 206,
                      "groupName": "$groupName",
                      "selectList": [
                        "$name",
                        "尹甲仑"
                      ],
                      "groupAnnouncement": "(自动填写群公告) WorkTool欢迎大家~WorkTool管家是机器人，有问题可以在QQ群反馈~@我可以聊天~"
                    }
                ]
            }
        """.trimIndent()
        MyLooper.onMessage(null, json)
    }

}