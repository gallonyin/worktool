package org.yameida.worktool.service

import com.blankj.utilcode.util.*
import com.hjq.toast.ToastUtils
import org.yameida.worktool.model.ExecCallbackBean


/**
 * 消息类型 100
 */
object WeworkInteractionImpl {

    /**
     * 交互通知
     * @param errorCode 失败错误码
     * @param errorReason 失败原因
     */
    fun consoleToast(
        message: ExecCallbackBean,
        errorCode: Int?,
        errorReason: String?
    ): Boolean {
        LogUtils.e("错误提示 错误码: $errorCode 错误信息: $errorReason")
        ToastUtils.show("错误提示 错误码: $errorCode 错误信息: $errorReason")
        return true
    }

}