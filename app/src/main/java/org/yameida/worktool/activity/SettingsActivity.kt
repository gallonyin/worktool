package org.yameida.worktool.activity

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.WindowManager
import android.widget.CompoundButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.*
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import kotlinx.android.synthetic.main.activity_settings.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import org.yameida.worktool.Constant
import org.yameida.worktool.R
import org.yameida.worktool.utils.*


/**
 * 登录页
 */
class SettingsActivity : AppCompatActivity() {

    companion object {
        fun enterActivity(context: Context) {
            LogUtils.d("SettingsActivity.enterActivity")
            context.startActivity(Intent(context, SettingsActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_settings)

        initView()
    }

    override fun onResume() {
        super.onResume()
        freshOpenFlow()
        freshOpenMain()
    }

    private fun initView() {
        iv_back_left.setOnClickListener { finish() }
        sw_encrypt.isChecked = Constant.encryptType == 1
        sw_encrypt.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            LogUtils.i("sw_encrypt onCheckedChanged: $isChecked")
            Constant.encryptType = if (isChecked) 1 else 0
            SPUtils.getInstance().put("encryptType", Constant.encryptType)
        })
        sw_receive.isChecked = Constant.autoReply == 1
        sw_receive.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            LogUtils.i("sw_receive onCheckedChanged: $isChecked")
            Constant.autoReply = if (isChecked) 1 else 0
            SPUtils.getInstance().put("autoReply", Constant.autoReply)
        })
        rl_reply_strategy.setOnClickListener { showReplyStrategyDialog() }
        rl_qa_url.setOnClickListener { showQaUrlDialog() }
        rl_donate.setOnClickListener { showDonateDialog() }
        rl_share.setOnClickListener { showShareDialog() }
        freshOpenFlow()
        bt_open_flow.setOnClickListener {
            freshOpenFlow()
            if (Settings.canDrawOverlays(Utils.getApp())) {
                if (!FlowPermissionHelper.canBackgroundStart(Utils.getApp())) {
                    ToastUtils.showLong("请同时打开后台弹出界面权限~")
                    PermissionPageManagement.goToSetting(this)
                }
                FloatWindowHelper.showWindow()
            } else {
                startActivityForResult(Intent(this, FloatViewGuideActivity::class.java), 0)
            }
        }
        freshOpenMain()
        bt_open_main.setOnClickListener {
            freshOpenMain()
            if (PermissionHelper.isAccessibilitySettingOn()) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            } else {
                if (Constant.robotId.isBlank()) {
                    ToastUtils.showLong("请先填写并保存链接号~")
                } else if (!PermissionHelper.isAccessibilitySettingOn()) {
                    openAccessibility()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        freshOpenFlow()
        if (Settings.canDrawOverlays(Utils.getApp())) {
            if (!FlowPermissionHelper.canBackgroundStart(Utils.getApp())) {
                ToastUtils.showLong("请同时打开后台弹出界面权限~")
                PermissionPageManagement.goToSetting(this)
            }
            FloatWindowHelper.showWindow()
        }
    }

    private fun showReplyStrategyDialog() {
        val strategyArray = arrayOf("只读消息不回调", "仅私聊和群聊@机器人回调", "私聊群聊全部回调")
        QMUIDialog.CheckableDialogBuilder(this)
            .setTitle("回复策略")
            .addItems(strategyArray) { dialog, which ->
                dialog.dismiss()
                updateRobotReplyStrategy(which)
            }
            .setCheckedIndex(Constant.replyStrategy)
            .create(R.style.QMUI_Dialog)
            .show()
    }

    private fun showQaUrlDialog() {
        val builder = QMUIDialog.EditTextDialogBuilder(this)
        builder.setTitle("消息回调地址")
            .setPlaceholder("请输入回调接口地址")
            .setDefaultText(Constant.qaUrl)
            .setInputType(InputType.TYPE_CLASS_TEXT)
            .addAction(getString(R.string.delete)) { dialog, index ->
                dialog.dismiss()
                updateRobotQaUrl("")
            }
            .addAction(getString(R.string.cancel)) { dialog, index -> dialog.dismiss() }
            .addAction(getString(R.string.add)) { dialog, index ->
                val text = builder.editText.text
                if (text != null) {
                    if (text.matches("https?://[^/]+.*".toRegex())) {
                        dialog.dismiss()
                        updateRobotQaUrl(text.toString().trim())
                    } else {
                        ToastUtils.showLong("格式异常！")
                    }
                } else {
                    ToastUtils.showLong("请勿为空！")
                }
            }
            .create(R.style.QMUI_Dialog).show()
    }

    private fun showDonateDialog() {
        DonateUtil.zfbDonate(this)
    }

    private fun showShareDialog() {
        startActivity(Intent.createChooser(Intent().apply {
            action = Intent.ACTION_SEND
            type = ShareUtil.TEXT
            putExtra(Intent.EXTRA_TEXT, "我发现一个非常好用的企业微信机器人程序，文档地址: https://worktool.apifox.cn/ APP下载地址是: https://cdn.asrtts.cn/uploads/worktool/apk/worktool-latest.apk")
        }, "分享"))
    }

    private fun freshOpenFlow() {
        if (Settings.canDrawOverlays(Utils.getApp())) {
            if (FlowPermissionHelper.canBackgroundStart(Utils.getApp())) {
                bt_open_flow.setBackgroundResource(R.drawable.comment_gray_btn)
                bt_open_flow.text = "悬浮窗权限已开启"
            } else {
                bt_open_flow.setBackgroundResource(R.drawable.comment_red_btn)
                bt_open_flow.text = "开启后台弹出界面"
            }
        } else {
            bt_open_flow.setBackgroundResource(R.drawable.comment_red_btn)
            bt_open_flow.text = "开启悬浮窗权限"
        }
    }

    private fun freshOpenMain() {
        if (PermissionHelper.isAccessibilitySettingOn()) {
            bt_open_main.setBackgroundResource(R.drawable.comment_gray_btn)
            bt_open_main.text = "主功能已开启"
        } else {
            bt_open_main.setBackgroundResource(R.drawable.comment_red_btn)
            bt_open_main.text = "开启主功能"
        }
    }

    /**
     * 打开辅助
     */
    private fun openAccessibility() {
        val clickListener =
            DialogInterface.OnClickListener { dialog, which ->
                freshOpenMain()
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                startActivity(intent)
            }
        val cancel = DialogInterface.OnCancelListener {
            freshOpenMain()
        }
        val cancelListener = DialogInterface.OnClickListener { dialog, which ->
            freshOpenMain()
        }
        val dialog: AlertDialog = AlertDialog.Builder(this)
            .setMessage(R.string.tips)
            .setOnCancelListener(cancel)
            .setNegativeButton("取消", cancelListener)
            .setPositiveButton("确定", clickListener)
            .create()
        dialog.show()
    }

    private fun updateRobotQaUrl(callbackUrl: String) {
        try {
            val json = hashMapOf<String, Any>()
            json["robotId"] = Constant.robotId
            if (callbackUrl.isEmpty()) {
                json["openCallback"] = 0
            } else {
                json["openCallback"] = 1
                json["callbackUrl"] = callbackUrl
            }
            val requestBody = RequestBody.create(
                MediaType.parse("application/json;charset=UTF-8"),
                GsonUtils.toJson(json)
            )
            val call = object : StringCallback() {
                override fun onSuccess(response: Response<String>?) {
                    if (response != null && JSONObject(response.body()).getInt("code") == 200) {
                        Constant.qaUrl = callbackUrl
                        ToastUtils.showLong(if (callbackUrl.isEmpty()) "关闭成功" else "更新成功")
                    } else {
                        onError(response)
                    }
                }

                override fun onError(response: Response<String>?) {
                    ToastUtils.showLong(if (callbackUrl.isEmpty()) "关闭失败，请稍后再试~" else "更新失败，请稍后再试~")
                }
            }
            OkGo.post<String>(Constant.getRobotUpdateUrl()).upRequestBody(requestBody).execute(call)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun updateRobotReplyStrategy(type: Int) {
        try {
            val json = hashMapOf<String, Any>()
            json["robotId"] = Constant.robotId
            json["replyAll"] = type
            val requestBody = RequestBody.create(
                MediaType.parse("application/json;charset=UTF-8"),
                GsonUtils.toJson(json)
            )
            val call = object : StringCallback() {
                override fun onSuccess(response: Response<String>?) {
                    if (response != null && JSONObject(response.body()).getInt("code") == 200) {
                        Constant.replyStrategy = type
                        ToastUtils.showLong("更新成功")
                    } else {
                        onError(response)
                    }
                }

                override fun onError(response: Response<String>?) {
                    ToastUtils.showLong("更新失败，请稍后再试~")
                }
            }
            OkGo.post<String>(Constant.getRobotUpdateUrl()).upRequestBody(requestBody).execute(call)
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
