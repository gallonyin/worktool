package org.yameida.worktool.activity

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils.SimpleStringSplitter
import android.view.WindowManager
import android.widget.CompoundButton
import android.widget.Switch
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.*
import com.umeng.analytics.MobclickAgent
import kotlinx.android.synthetic.main.activity_listen.*
import org.yameida.worktool.*
import org.yameida.worktool.service.WeworkService
import org.yameida.worktool.config.WebConfig
import org.yameida.worktool.utils.UpdateUtil

class ListenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        title = "WorkTool"
        setContentView(R.layout.activity_listen)

        initView()
        initAccessibility()
        UpdateUtil.checkUpdate()
        PermissionUtils.permission("android.permission.READ_EXTERNAL_STORAGE").request()
    }

    override fun onStart() {
        super.onStart()
        freshOpenServiceSwitch(
            WeworkService::class.java,
            sw_accessibility
        )
    }

    private fun initView() {
        et_channel.setText(SPUtils.getInstance().getString(WebConfig.LISTEN_CHANNEL_ID))
        bt_save.setOnClickListener {
            val channel = et_channel.text.toString().trim()
            SPUtils.getInstance().put(WebConfig.LISTEN_CHANNEL_ID, channel)
            ToastUtils.showLong("保存成功")
            sendBroadcast(Intent(WebConfig.WEWORK_NOTIFY).apply {
                putExtra("type", "modify_channel")
            })
            MobclickAgent.onProfileSignIn(channel)
        }
        sw_encrypt.isChecked = Constant.encryptType == 1
        sw_encrypt.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            LogUtils.i("sw_encrypt onCheckedChanged: $isChecked")
            Constant.encryptType = if (isChecked) 1 else 0
            SPUtils.getInstance().put("encryptType", Constant.encryptType)
        })
        sw_auto_reply.isChecked = Constant.autoReply == 1
        sw_auto_reply.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            LogUtils.i("sw_auto_reply onCheckedChanged: $isChecked")
            Constant.autoReply = if (isChecked) 1 else 0
            SPUtils.getInstance().put("autoReply", Constant.autoReply)
        })
        tv_host.text = WebConfig.HOST
        val version = "${AppUtils.getAppVersionName()}     Android ${DeviceUtils.getSDKVersionName()} ${DeviceUtils.getManufacturer()} ${DeviceUtils.getModel()}"
        tv_version.text = version
        val workVersionName = AppUtils.getAppInfo(Constant.PACKAGE_NAMES)?.versionName
        when (workVersionName) {
            null -> {
                LogUtils.e("系统检测到您尚未安装企业微信，请先安装企业微信")
                tv_work_version.text = "检测到您尚未安装企业微信，请先安装登录!"
            }
            in Constant.AVAILABLE_VERSION -> {
                LogUtils.i("当前企业微信版本已适配: $workVersionName")
                val tip = "$workVersionName   已适配，可放心使用~"
                tv_work_version.text = tip
            }
            else -> {
                LogUtils.e("当前企业微信版本未兼容: $workVersionName")
                val tip = "$workVersionName   可能存在部分兼容性问题!"
                tv_work_version.text = tip
            }
        }
        SPUtils.getInstance().put("appVersion", version)
        SPUtils.getInstance().put("workVersion", workVersionName)
    }

    private fun initAccessibility() {
        sw_accessibility.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            LogUtils.i("sw_accessibility onCheckedChanged: $isChecked")
            if (isChecked) {
                if (SPUtils.getInstance().getString(WebConfig.LISTEN_CHANNEL_ID).isNullOrBlank()) {
                    sw_accessibility.isChecked = false
                    ToastUtils.showLong("请先填写并保存链接号~")
                } else if (!isAccessibilitySettingOn()) {
                    openAccessibility()
                }
            } else {
                if (isAccessibilitySettingOn()) {
                    sw_accessibility.isChecked = true
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                }
            }
        })
    }

    private fun isAccessibilitySettingOn(): Boolean {
        val context = Utils.getApp()
        var enable = 0
        val serviceName = context.packageName + "/" + WeworkService::class.java.canonicalName
        LogUtils.i("isAccessibilitySettingOn: $serviceName")
        try {
            enable = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.ACCESSIBILITY_ENABLED,
                0
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (enable == 1) {
            val stringSplitter = SimpleStringSplitter(':')
            val settingVal = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            if (settingVal != null) {
                stringSplitter.setString(settingVal)
                while (stringSplitter.hasNext()) {
                    val accessibilityService = stringSplitter.next()
                    if (accessibilityService == serviceName) {
                        LogUtils.i("isAccessibilitySettingOn: true")
                        return true
                    }
                }
            }
        }
        LogUtils.i("isAccessibilitySettingOn: false")
        return false
    }

    /**
     * 打开辅助
     */
    private fun openAccessibility() {
        val clickListener =
            DialogInterface.OnClickListener { dialog, which ->
                freshOpenServiceSwitch(
                    WeworkService::class.java,
                    sw_accessibility
                )
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
        val cancel = DialogInterface.OnCancelListener {
            freshOpenServiceSwitch(
                WeworkService::class.java,
                sw_accessibility
            )
        }
        val cancelListener = DialogInterface.OnClickListener { dialog, which ->
            freshOpenServiceSwitch(
                WeworkService::class.java,
                sw_accessibility
            )
        }
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setMessage(R.string.tips)
            .setOnCancelListener(cancel)
            .setNegativeButton("取消", cancelListener)
            .setPositiveButton("确定", clickListener)
            .create()
        dialog.show()
    }

    private fun freshOpenServiceSwitch(clazz: Class<*>, s: Switch) {
        if (isAccessibilitySettingOn()) {
            s.isChecked = true
            when (s.id) {
                R.id.sw_accessibility -> {
                }
            }
        } else {
            s.isChecked = false
            when (s.id) {
                R.id.sw_accessibility -> {
                }
            }
        }
    }

}