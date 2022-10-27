package org.yameida.worktool.utils

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
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

}