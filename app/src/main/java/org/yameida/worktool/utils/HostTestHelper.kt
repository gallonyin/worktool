package org.yameida.worktool.utils

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.yameida.worktool.Constant

object HostTestHelper {

    fun test() {
        OkGo.get<String>(Constant.getTestUrl())
            .execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>) {
                    LogUtils.i("测试接口: " + response.body())
                    ToastUtils.showLong("服务器连接测试成功!")
                    return
                }

                override fun onError(response: Response<String>) {
                    LogUtils.e("服务器连接测试失败")
                    ToastUtils.showLong("服务器连接测试失败!" + response.exception)
                }
            })
    }

    fun testWs() {
        val s = OkHttpClient().newWebSocket(Request.Builder().url(Constant.getWsUrl()).build(),
            object : WebSocketListener() {
                override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
                    ToastUtils.showLong("链接: ${Constant.getWsUrl()}\nonOpen\n" + response.body())
                }

                override fun onMessage(webSocket: WebSocket, text: String) {
                    ToastUtils.showLong("链接: ${Constant.getWsUrl()}\nonMessage\ntext:$text")
                    webSocket.close(1000, "接口测试成功")
                }

                override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                    ToastUtils.showLong("链接: ${Constant.getWsUrl()}\nonMessage\nbytes:$bytes")
                    webSocket.close(1000, "接口测试成功")
                }

                override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                    ToastUtils.showLong("链接: ${Constant.getWsUrl()}\nonClosing\ncode:$code reason:$reason")
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    ToastUtils.showLong("链接: ${Constant.getWsUrl()}\nonClosed\ncode:$code reason:$reason")
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
                    ToastUtils.showLong("链接: ${Constant.getWsUrl()}\nonClosed\nresponse:${response?.body()} t:${t.message}")
                }
            })
    }

}