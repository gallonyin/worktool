package org.yameida.worktool.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.widget.Toast
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.Utils
import com.tencent.wework.api.IWWAPI
import com.tencent.wework.api.WWAPIFactory
import com.tencent.wework.api.model.WWMediaLink
import com.tencent.wework.api.model.WWMediaMiniProgram
import com.tencent.wework.api.model.WWSimpleRespMessage


object IWWAPIUtil {

    private var iwwapi: IWWAPI? = null

    var appid = "wwe51e5ed82702b49b" //企业唯一标识。创建企业后显示在，我的企业 CorpID字段
    var agentid = "1000002" //应用唯一标识。显示在具体应用下的 AgentId字段
    var schema = "wwauthe51e5ed82702b49b000002"

    fun init(context: Context, schema: String = this.schema) {
        this.schema = schema
        iwwapi = WWAPIFactory.createWWAPI(context)
        iwwapi?.registerApp(schema)
    }

    fun sendLink(thumbUrl: String?, webpageUrl: String?, title: String?, description: String?): Boolean {
        val link = WWMediaLink()
        link.thumbUrl = thumbUrl
        link.webpageUrl = webpageUrl
        link.title = title
        link.description = description
        link.appPkg = AppUtils.getAppPackageName()
        link.appName = AppUtils.getAppName()
        link.appId = appid
        link.agentId = agentid
        return iwwapi?.sendMessage(link) ?: false
    }

    fun sendMicroProgram() {
        val miniProgram = WWMediaMiniProgram()
        miniProgram.appPkg = AppUtils.getAppPackageName()
        miniProgram.appName = AppUtils.getAppName()
        miniProgram.appId = appid
        miniProgram.agentId = agentid
        miniProgram.schema = schema
        miniProgram.username = "gh_dde54cb88ce7@app" //必须是应用关联的小程序，注意要有@app后缀
        miniProgram.description = "dddddd"
        miniProgram.path = "/pages/plugin/index.html?plugid=1cbd3b7c8674e61769436b5e354ddb2f"
//        val bitmap = (getDrawable(R.drawable.test) as BitmapDrawable).bitmap
//        val stream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 0, stream)
//        val byteArray: ByteArray = stream.toByteArray()

//        miniProgram.hdImageData = byteArray
        miniProgram.title = "测试_MaHow"
        iwwapi!!.sendMessage(miniProgram) { resp ->
            if (resp is WWSimpleRespMessage) {
                val rsp = resp as WWSimpleRespMessage
                var t: String? = ""
                Toast.makeText(
                    Utils.getApp(),
                    "发小程序," + rsp.errCode + "," + rsp.errMsg.also {
                        t = it
                    },
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}