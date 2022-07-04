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
        Constant.encryptType = SPUtils.getInstance().getInt("encryptType", 0)
        sw_encrypt.isChecked = Constant.encryptType == 1
        sw_encrypt.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            LogUtils.i("sw_encrypt onCheckedChanged: $isChecked")
            Constant.encryptType = if (isChecked) 1 else 0
            SPUtils.getInstance().put("encryptType", Constant.encryptType)
        })
        tv_host.text = WebConfig.HOST
        tv_version.text = AppUtils.getAppVersionName()
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