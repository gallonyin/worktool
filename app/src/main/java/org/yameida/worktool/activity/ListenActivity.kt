package org.yameida.worktool.activity

import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.*
import com.umeng.analytics.MobclickAgent
import kotlinx.android.synthetic.main.activity_listen.*
import org.yameida.worktool.*
import android.content.*
import android.os.IBinder
import android.text.InputType
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import org.yameida.worktool.service.PlayNotifyService
import org.yameida.worktool.service.fastStartActivity
import org.yameida.worktool.utils.*
import org.yameida.worktool.utils.capture.MediaProjectionHolder
import org.yameida.worktool.utils.envcheck.CheckHook
import org.yameida.worktool.utils.envcheck.CheckRoot


class ListenActivity : AppCompatActivity() {

    companion object {
        /**
         * @param type 0=游客登录
         */
        fun enterActivity(context: Context, type: Int) {
            LogUtils.d("ListenActivity.enterActivity type: $type")
            context.startActivity(Intent(context, ListenActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("type", type)
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_listen)

        initView()
        initAccessibility()
        initOverlays()
        initData()
        initNotification()
        PermissionUtils.permission("android.permission.READ_EXTERNAL_STORAGE").request()
        registerReceiver(openWsReceiver, IntentFilter(Constant.WEWORK_NOTIFY))
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(openWsReceiver)
    }

    override fun onResume() {
        super.onResume()
        sw_overlay.isChecked = Settings.canDrawOverlays(Utils.getApp()) && FlowPermissionHelper.canBackgroundStart(Utils.getApp())
        sw_accessibility.isChecked = PermissionHelper.isAccessibilitySettingOn()
        if (needToWork) {
            needToWork = false
            goToWork()
        }
    }

    private fun initView() {
        iv_settings.setOnClickListener {
            SettingsActivity.enterActivity(this)
        }
        et_channel.setText(Constant.robotId)
        bt_save.setOnClickListener {
            val channel = et_channel.text.toString().trim()
            Constant.robotId = channel
            ToastUtils.showLong("保存成功")
            sendBroadcast(Intent(Constant.WEWORK_NOTIFY).apply {
                putExtra("type", "modify_channel")
            })
            HttpUtil.getMyConfig(toast = false)
            MobclickAgent.onProfileSignIn(channel)
        }
        tv_host.text = Constant.host
        tv_host.setOnClickListener {
            showSelectHostDialog()
        }
        tv_host.setOnLongClickListener {
            showInputHostDialog()
            true
        }
        val version = "${AppUtils.getAppVersionName()}     Android ${DeviceUtils.getSDKVersionName()} ${DeviceUtils.getManufacturer()} ${DeviceUtils.getModel()}"
        val deviceRooted = CheckRoot.isDeviceRooted()
        val hook = CheckHook.isHook(applicationContext)
        if (hook) {
            tv_version.text = "当前设备存在侵入代码，请勿在本设备使用本程序！！！"
        } else if (deviceRooted) {
            tv_version.text = "$version\n本设备已Root，存在一定风险！"
        } else {
            tv_version.text = version
        }
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
        SPUtils.getInstance().put("deviceRooted", deviceRooted)
        SPUtils.getInstance().put("hook", hook)
    }

    private fun initAccessibility() {
        sw_accessibility.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            LogUtils.i("sw_accessibility onCheckedChanged: $isChecked")
            if (isChecked) {
                if (Constant.robotId.isBlank()) {
                    sw_accessibility.isChecked = false
                    ToastUtils.showLong("请先填写并保存链接号~")
                } else if (!PermissionHelper.isAccessibilitySettingOn()) {
                    startActivity(Intent(this, AccessibilityGuideActivity::class.java))
                }
            } else {
                if (PermissionHelper.isAccessibilitySettingOn()) {
                    sw_accessibility.isChecked = true
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                    startActivity(intent)
                }
            }
        })
    }

    private fun initOverlays() {
        sw_overlay.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            LogUtils.i("sw_overlay onCheckedChanged: $isChecked")
            if (isChecked) {
                startActivity(Intent(this, FloatViewGuideActivity::class.java))
            } else {
                if (Settings.canDrawOverlays(Utils.getApp()) && FlowPermissionHelper.canBackgroundStart(Utils.getApp())) {
                    sw_overlay.isChecked = true
                    PermissionPageManagement.goToSetting(this)
                }
            }
        })
        if (Settings.canDrawOverlays(Utils.getApp()) && FlowPermissionHelper.canBackgroundStart(Utils.getApp())) {
            FloatWindowHelper.showWindow()
        }
    }

    private fun initData() {
        HttpUtil.checkUpdate()
        HttpUtil.getMyConfig(toast = false)
    }

    private fun initNotification() {
        if (!Constant.enableMediaProject) {
            return
        }
        application.bindService(Intent(application, PlayNotifyService::class.java), object : ServiceConnection {
            override fun onServiceDisconnected(name: ComponentName) {
                LogUtils.d("onServiceDisconnected")
            }
            override fun onServiceConnected(name: ComponentName, iBinder: IBinder) {
                LogUtils.d("onServiceConnected")
            }
        }, Context.BIND_AUTO_CREATE)
        //开启屏幕录制权限
        if (MediaProjectionHolder.mMediaProjection == null) {
            fastStartActivity(this, GetScreenShotActivity::class.java)
        }
    }

    private fun showSelectHostDialog() {
        val hostList = SPUtils.getInstance().getStringSet("host_list", mutableSetOf(Constant.host))
        if (hostList.isNotEmpty()) {
            val hostArray = hostList.toTypedArray()
            QMUIDialog.CheckableDialogBuilder(this)
                .setTitle(getString(R.string.host_list))
                .addItems(hostArray) { dialog, which ->
                    Constant.host = hostArray[which]
                    tv_host.text = hostArray[which]
                    HostTestHelper.testWs()
                    dialog.dismiss()
                }
                .setCheckedIndex(hostList.indexOf(Constant.host))
                .create(R.style.QMUI_Dialog)
                .show()
        }
    }

    private fun showInputHostDialog() {
        ToastUtils.showLong("请输入专线网络")
        val builder = QMUIDialog.EditTextDialogBuilder(this)
        builder.setTitle(getString(R.string.tip))
            .setPlaceholder(getString(R.string.input_new_host))
            .setDefaultText(tv_host.text)
            .setInputType(InputType.TYPE_CLASS_TEXT)
            .addAction(getString(R.string.delete)) { dialog, index ->
                val hostList = SPUtils.getInstance().getStringSet("host_list", mutableSetOf(Constant.host))
                if (hostList.size > 1) {
                    hostList.remove(Constant.host)
                    Constant.host = hostList.elementAt(0)
                    tv_host.text = Constant.host
                    HostTestHelper.testWs()
                    SPUtils.getInstance().put("host_list", hostList)
                    dialog.dismiss()
                } else {
                    ToastUtils.showLong("至少保留一个host！")
                }
            }
            .addAction(getString(R.string.cancel)) { dialog, index -> dialog.dismiss() }
            .addAction(getString(R.string.add)) { dialog, index ->
                val text = builder.editText.text
                if (text != null && text.isNotEmpty()) {
                    if (text.matches("ws{1,2}://[^/]+.*".toRegex())) {
                        val hostList = SPUtils.getInstance().getStringSet("host_list", mutableSetOf(Constant.host))
                        hostList.add(text.toString())
                        SPUtils.getInstance().put("host_list", hostList)
                        Constant.host = text.toString()
                        tv_host.text = text
                        HostTestHelper.testWs()
                        dialog.dismiss()
                    } else {
                        ToastUtils.showLong("格式异常！")
                    }
                } else {
                    ToastUtils.showLong("请勿为空！")
                }
            }
            .create(R.style.QMUI_Dialog).show()
    }

    private var needToWork = false

    private fun goToWork() {
        val positiveButton =
            MaterialAlertDialogBuilder(this, R.style.Theme_MaterialComponents_DayNight_Dialog)
                .setTitle("设置成功")
                .setMessage("请勿人工操作手机     \n5秒后自动跳转")
                .setNegativeButton("", null)
                .setPositiveButton("", null)
        val show = positiveButton.show()
        bt_save.postDelayed({ show.dismiss() }, 5000)
        bt_save.postDelayed({
            packageManager.getLaunchIntentForPackage(Constant.PACKAGE_NAMES)?.apply {
                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(this)
            }
        }, 5000)
    }

    private val openWsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.getStringExtra("type") == "openWs") {
                needToWork = intent.getBooleanExtra("switch", false)
            }
        }
    }

}