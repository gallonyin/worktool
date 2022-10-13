package org.yameida.worktool.model

import java.util.*

data class ExecCallbackBean(
    //原生消息指令
    var rawMsg: String? = null,

    //1成功 0失败
    var rawSuccess: Int = 0,

    //失败原因
    var errorReason: String = "",

    //执行时间
    var runTime: Date = Date()

) : WeworkMessageBean()