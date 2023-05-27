package org.yameida.worktool.utils

import com.blankj.utilcode.util.*
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import model.UpdateConfig
import org.json.JSONObject
import org.yameida.worktool.Constant
import org.yameida.worktool.R
import org.yameida.worktool.model.network.CheckUpdateResult
import org.yameida.worktool.model.network.GetMyConfigResult
import org.yameida.worktool.service.log
import org.yameida.worktool.utils.envcheck.CheckRoot
import update.UpdateAppUtils
import java.io.File

object HttpUtil {

    /**
     * 检查更新
     */
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

    /**
     * 获取机器人配置
     */
    fun getMyConfig(toast: Boolean = true) {
        if (Constant.robotId.isBlank()) {
            if (toast) {
                ToastUtils.showLong("请先填写机器人ID")
            }
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
                        LogUtils.i("获取配置", commonResult.data)
                        SPUtils.getInstance().put("risk", false)
                        if (CheckRoot.isDeviceRooted()) {
                            val date = TimeUtils.string2Date(commonResult.data.createTime, "yyyy-MM-dd'T'HH:mm:ss")
                            if (System.currentTimeMillis() - date.time < 7 * 68400 * 1000) {
                                LogUtils.e("新号使用模拟环境！")
                                ToastUtils.showLong("新号请勿使用模拟器/云手机！")
                                SPUtils.getInstance().put("risk", true)
                            }
                        }
                        commonResult.data?.apply {
                            Constant.qaUrl = this.callbackUrl ?: ""
                            Constant.openCallback = this.openCallback ?: 0
                            Constant.replyStrategy = (this.replyAll ?: 0) + 1
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

    /**
     * 推送图片
     */
    fun pushImage(url: String, titleList: List<String>, receivedName: String?, imagePath: String, roomType: Int) {
        return pushImage(url, titleList, receivedName, File(imagePath).readBytes(), roomType)
    }

    /**
     * 推送图片
     */
    fun pushImage(url: String, titleList: List<String>, receivedName: String?, byteArray: ByteArray, roomType: Int) {
        val json = JSONObject()
        if (receivedName != null) {
            json.put("receivedName", receivedName)
            json.put("groupName", titleList.lastOrNull())
            if (titleList.size > 1) {
                json.put("groupRemark", titleList.first())
            } else {
                json.put("groupRemark", null)
            }
        } else {
            json.put("receivedName", titleList.lastOrNull { !it.contains("＠") } ?: "")
            json.put("groupName", null)
            json.put("groupRemark", null)
        }
        json.put("image", EncodeUtils.base64Encode2String(byteArray))
        json.put("robotId", Constant.robotId)
        json.put("roomType", roomType)
        json.put("atMe", false)
        json.put("textType", 2)
        OkGo.post<String>(url)
            .upJson(json)
            .execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>) {
                    LogUtils.d("推送图片成功: ${titleList.joinToString()} $receivedName")
                    log("推送图片成功: ${titleList.joinToString()} $receivedName")
                }

                override fun onError(response: Response<String>) {
                    ToastUtils.showLong("推送图片失败")
                    LogUtils.e("推送图片失败")
                    error("推送图片失败: ${titleList.joinToString()} $receivedName")
                }
            })
    }

    /**
     * 推送本地文件
     */
    fun pushLocalFile(file: File) {
        OkGo.post<String>(Constant.getPushLocalFileUrl())
            .addFileParams("file", listOf(file))
            .execute(object : StringCallback() {
                override fun onSuccess(response: Response<String>?) {
                    LogUtils.d("推送本地文件成功: ${file.absolutePath}")
                }
            })
    }
}
