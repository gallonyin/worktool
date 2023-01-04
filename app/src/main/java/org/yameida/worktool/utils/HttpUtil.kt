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
import org.yameida.worktool.model.network.GetMyConfigResult
import update.UpdateAppUtils

object HttpUtil {

    fun checkUpdate() {
        OkGo.get<String>(Constant.getCheckUpdateUrl())
            .execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>) {
                    try {
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
                    } catch (e: Exception) {
                        LogUtils.e(e)
                        onError(response)
                    }
                }

                override fun onError(response: Response<String>) {
                    ToastUtils.showLong(R.string.update_failed)
                    LogUtils.e("检查更新失败")
                }
            })
    }

    fun getMyConfig() {
        if (Constant.robotId.isBlank()) {
            ToastUtils.showLong("请先填写机器人ID")
            return
        }
        OkGo.get<String>(Constant.getMyConfig())
            .execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>) {
                    try {
                        val commonResult =
                            GsonUtils.fromJson(
                                response.body(),
                                GetMyConfigResult::class.java
                            )
                        if (commonResult.code != 200) {
                            return onError(response)
                        }
                        LogUtils.i(commonResult.data)
                        commonResult.data?.apply {
                            Constant.qaUrl = this.callbackUrl ?: ""
                            Constant.openCallback = this.openCallback ?: 0
                            Constant.replyStrategy = (this.replyAll ?: 0) + 1
                            return
                        }
                    } catch (e: Exception) {
                        LogUtils.e(e)
                        onError(response)
                    }
                }

                override fun onError(response: Response<String>) {
                    ToastUtils.showLong("获取配置失败 请检查机器人ID")
                    LogUtils.e("获取配置失败 请检查机器人ID")
                }
            })
    }
}
