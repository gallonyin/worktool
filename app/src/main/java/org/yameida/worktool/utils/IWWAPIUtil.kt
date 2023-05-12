package org.yameida.worktool.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import com.blankj.utilcode.util.*
import com.lzy.okgo.OkGo
import com.tencent.wework.api.IWWAPI
import com.tencent.wework.api.WWAPIFactory
import com.tencent.wework.api.model.WWMediaLink
import com.tencent.wework.api.model.WWMediaMiniProgram
import org.yameida.worktool.Constant
import org.yameida.worktool.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


object IWWAPIUtil {

    private var iwwapi: IWWAPI? = null

    fun init(context: Context) {
        iwwapi = WWAPIFactory.createWWAPI(context)
        val result = iwwapi?.registerApp(Constant.weworkSchema)
        LogUtils.e("iwwapi.registerApp: $result")
    }

    fun sendLink(thumbUrl: String?, webpageUrl: String?, title: String?, description: String?): Boolean {
        val link = WWMediaLink()
        link.thumbUrl = thumbUrl
        link.webpageUrl = webpageUrl
        link.title = title
        link.description = description
        link.appPkg = AppUtils.getAppPackageName()
        link.appName = AppUtils.getAppName()
        link.appId = Constant.weworkCorpId
        link.agentId = Constant.weworkAgentId
        return iwwapi?.sendMessage(link) ?: false
    }

    fun sendMicroProgram(imageUrl: String?, webpageUrl: String?, title: String?, description: String?): Boolean {
        val miniProgram = WWMediaMiniProgram()
        miniProgram.appPkg = AppUtils.getAppPackageName()
        miniProgram.appName = AppUtils.getAppName()
        miniProgram.appId = Constant.weworkCorpId
        miniProgram.agentId = Constant.weworkAgentId
        miniProgram.schema = Constant.weworkSchema
        miniProgram.username = Constant.weworkMP //必须是应用关联的小程序，注意要有@app后缀
        miniProgram.description = description
        miniProgram.path = webpageUrl
        miniProgram.title = title

        val bitmap = (Utils.getApp().getDrawable(R.mipmap.ic_launcher) as BitmapDrawable).bitmap
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream)
        miniProgram.hdImageData = stream.toByteArray()
        if (imageUrl != null) {
            LogUtils.i("下载开始 $imageUrl")
            val execute = OkGo.get<File>(imageUrl).execute()
            LogUtils.i("下载完成 $imageUrl")
            val body = execute.body()
            if (body != null) {
                val df = SimpleDateFormat("yyyy-MM-dd")
                val filePath = "${
                    Utils.getApp().getExternalFilesDir("mp_image_cache")
                }/${df.format(Date())}/${imageUrl.split("/").lastOrNull()}"
                val newFile = File(filePath)
                val create = FileUtils.createFileByDeleteOldFile(newFile)
                if (create && newFile.canWrite()) {
                    newFile.writeBytes(body.bytes())
                    LogUtils.i("文件存储本地成功 $filePath")
                    val bitmap = ImageUtils.bytes2Bitmap(File(filePath).readBytes())
                    val stream = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream)
                    miniProgram.hdImageData = stream.toByteArray()
                } else {
                    LogUtils.e("文件存储本地失败 $filePath")
                }
            } else {
                LogUtils.e("文件下载失败")
            }
        }
        return iwwapi?.sendMessage(miniProgram) ?: false
    }
}