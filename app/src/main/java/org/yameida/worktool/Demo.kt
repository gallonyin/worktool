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
                        "甲仑"
                      ],
                      "groupAnnouncement": "(自动拉群+自动群公告) WorkTool欢迎大家~WorkTool管家是机器人，有问题可以在QQ群反馈~@我可以聊天~"
                    }
                ]
            }
        """.trimIndent()
        MyLooper.onMessage(null, json)
    }

}