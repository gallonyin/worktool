package org.yameida.worktool.utils

import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import model.UpdateConfig
import org.yameida.worktool.Constant
import org.yameida.worktool.R
import org.yameida.worktool.model.network.CheckUpdateResult
import update.UpdateAppUtils

object UpdateUtil {

    fun checkUpdate() {
//        val remoteVersionCode = 10
//        val remoteVersionName = "1.0.1"
//        val forceUpdate = false
//        val updateLog = "修复Bug\n优化用户体验"
//        val downloadUrl = "https://down.qq.com/qqweb/QQ_1/android_apk/Android_8.5.0.5025_537066738.apk"
//        val fileMD5 = "560017dc94e8f9b65f4ca997c7feb326"
        OkGo.get<String>(Constant.URL_CHECK_UPDATE)
            .execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>) {
                    val commonResult =
                        GsonUtils.fromJson(
                            response.body(),
                            CheckUpdateResult::class.java
                        )
                    if (commonResult.code != 200) {
                        return onError(response)
                    }
                    LogUtils.i(commonResult.data)
                    commonResult.data?.apply {
                        if (AppUtils.getAppVersionCode() < this.versionCode) {
                            UpdateAppUtils
                                .getInstance()
                                .apkUrl(this.downloadUrl)
                                .updateTitle(this.title)
                                .updateContent(this.updateLog.replace("\\n", "\n"))
                                .updateConfig(
                                    UpdateConfig(
                                        force = AppUtils.getAppVersionCode() < this.minVersionCode,
                                        serverVersionName = this.versionName,
                                        serverVersionCode = this.versionCode
                                    )
                                )
                                .update()
                        } else {
                            ToastUtils.showShort(R.string.update_no_update)
                        }
                        return
                    }
                    ToastUtils.showLong(R.string.update_failed)
                }

                override fun onError(response: Response<String>) {
                    ToastUtils.showLong(R.string.update_failed)
                    LogUtils.e("检查更新失败")
                }
            })
    }
}
