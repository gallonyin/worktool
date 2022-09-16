package org.yameida.worktool

import android.app.Application
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import com.tendcloud.tenddata.TalkingDataSDK
import com.umeng.commonsdk.UMConfigure
import org.yameida.worktool.config.GlobalException
import update.UpdateAppUtils

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        //初始化工具类
        Utils.init(this)
        //初始化友盟统计
        UMConfigure.preInit(this, "6284a3a3d024421570f97c3c", "main_channel")
        //判断是否同意隐私协议，uminit为1时为已经同意，直接初始化umsdk
        if (SPUtils.getInstance().getString("uminit", "1") == "1") {
            UMConfigure.init(this, "6284a3a3d024421570f97c3c", "main_channel", UMConfigure.DEVICE_TYPE_PHONE, "")
        }
        TalkingDataSDK.init(this, "80E9C84E39904DAFB28562910FF7C86C", "worktool_master", SPUtils.getInstance().getString(Constant.LISTEN_CHANNEL_ID));
        //初始化自动更新
        UpdateAppUtils.init(this)
        //设置全局异常捕获重启
        Thread.setDefaultUncaughtExceptionHandler(GlobalException.getInstance())
    }

}