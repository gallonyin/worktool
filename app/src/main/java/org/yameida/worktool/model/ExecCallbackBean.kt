package org.yameida.worktool.model

/**
 * 任务执行成功失败结果回传
 */
data class ExecCallbackBean(
    //原生消息指令
    var rawMsg: String? = null,

    //0成功 其他是失败错误码
    var errorCode: Int? = null,

    //失败原因
    var errorReason: String? = null,

    //执行时间
    var runTime: Long? = null,

    //执行耗时
    var timeCost: Double? = null,

    //成功名单
    var successList: List<String>? = null,

    //失败名单
    var failList: List<String>? = null,

    //附加信息
    var metaJson: String? = null

) : WeworkMessageBean() {
    companion object {
        /**
         * 错误码
         * 通用格式错误 10xxx
         * 执行任务错误 20xxx
         * 服务端返回错误 50xxx
         */
        const val SUCCESS = 0
        //数据格式错误
        const val ERROR_ILLEGAL_DATA = 101011
        //非法操作
        const val ERROR_ILLEGAL_OPERATION = 101012
        //非法权限
        const val ERROR_ILLEGAL_PERMISSION = 101013

        //创建群失败
        const val ERROR_CREATE_GROUP = 201011
        //群改名失败
        const val ERROR_GROUP_RENAME = 201012
        //群拉人失败
        const val ERROR_GROUP_ADD_MEMBER = 201013
        //群踢人失败
        const val ERROR_GROUP_REMOVE_MEMBER = 201014
        //改群公告失败
        const val ERROR_GROUP_CHANGE_ANNOUNCEMENT = 201015
        //改群备注失败
        const val ERROR_GROUP_CHANGE_REMARK = 201016
        //改群模板失败
        const val ERROR_GROUP_TEMPLATE = 201017
        //创建群达到上限
        const val ERROR_CREATE_GROUP_LIMIT = 201018
        //查找聊天窗失败
        const val ERROR_INTO_ROOM = 201101
        //发送消息失败
        const val ERROR_SEND_MESSAGE = 201102
        //按钮寻找失败
        const val ERROR_BUTTON = 201103
        //目标寻找失败
        const val ERROR_TARGET = 201104
        //转发失败
        const val ERROR_RELAY = 201105
        //重复添加
        const val ERROR_REPEAT = 201106
        //文件下载异常
        const val ERROR_FILE_DOWNLOAD = 201107
        //文件存储异常
        const val ERROR_FILE_STORAGE = 201108

        //机器人id错误
        const val ERROR_INVALID_ROBOT_ID = 501011
        //机器人key错误
        const val ERROR_INVALID_ROBOT_KEY = 501012
        //机器人重复登录
        const val ERROR_ROBOT_MULTI_LOGIN = 501013
        //机器人其他未知错误
        const val ERROR_OTHER = 501000

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        if (!super.equals(other)) return false

        other as ExecCallbackBean

        if (rawMsg != other.rawMsg) return false
        if (errorCode != other.errorCode) return false
        if (errorReason != other.errorReason) return false
        if (runTime != other.runTime) return false
        if (timeCost != other.timeCost) return false
        if (successList != other.successList) return false
        if (failList != other.failList) return false

        return true
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + (rawMsg?.hashCode() ?: 0)
        result = 31 * result + (errorCode ?: 0)
        result = 31 * result + (errorReason?.hashCode() ?: 0)
        result = 31 * result + (runTime?.hashCode() ?: 0)
        result = 31 * result + (timeCost?.hashCode() ?: 0)
        result = 31 * result + (successList?.hashCode() ?: 0)
        result = 31 * result + (failList?.hashCode() ?: 0)
        return result
    }

}