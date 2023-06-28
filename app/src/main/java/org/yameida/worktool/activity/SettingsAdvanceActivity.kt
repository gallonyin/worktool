package org.yameida.worktool.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.widget.CompoundButton
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.*
import com.lzy.okgo.OkGo
import com.lzy.okgo.callback.StringCallback
import com.lzy.okgo.model.Response
import com.qmuiteam.qmui.widget.dialog.QMUIDialog
import kotlinx.android.synthetic.main.activity_settings_advance.*
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import org.yameida.worktool.Constant
import org.yameida.worktool.R
import org.yameida.worktool.utils.*
import java.util.*


/**
 * 高级选项页
 */
class SettingsAdvanceActivity : AppCompatActivity() {

    companion object {
        fun enterActivity(context: Context) {
            LogUtils.d("SettingsAdvanceActivity.enterActivity")
            context.startActivity(Intent(context, SettingsAdvanceActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_settings_advance)

        initView()
        initData()
    }

    private fun initView() {
        iv_back_left.setOnClickListener { finish() }
        sw_full_name.isChecked = Constant.fullGroupName
        sw_full_name.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            LogUtils.i("sw_full_name onCheckedChanged: $isChecked")
            Constant.fullGroupName = isChecked
        })
        sw_qr_code.isChecked = Constant.groupQrCode
        sw_qr_code.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            LogUtils.i("sw_qr_code onCheckedChanged: $isChecked")
            Constant.groupQrCode = isChecked
        })
        sw_auto_publish.isChecked = Constant.autoPublishComment
        sw_auto_publish.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            LogUtils.i("sw_auto_publish onCheckedChanged: $isChecked")
            Constant.autoPublishComment = isChecked
        })
        sw_old_device.isChecked = Constant.oldDevice
        sw_old_device.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            LogUtils.i("sw_old_device onCheckedChanged: $isChecked")
            Constant.oldDevice = isChecked
            updateOldDeviceConfig()
        })
        ll_corp_param.visibility = if (Constant.customLink) View.VISIBLE else View.GONE
        rl_username.visibility = if (Constant.customMP) View.VISIBLE else View.GONE
        rl_qa_url.setOnClickListener { showQaUrlDialog() }
        rl_corp.setOnClickListener { showCorpIdDialog() }
        rl_agent.setOnClickListener { showAgentIdDialog() }
        rl_schema.setOnClickListener { showSchemaDialog() }
        rl_username.setOnClickListener { showUserNameDialog() }
        rl_signature.setOnClickListener { showSignatureDialog() }
    }

    private fun initData() {
        HttpUtil.getMyConfig()
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

    private fun showCorpIdDialog() {
        val builder = QMUIDialog.EditTextDialogBuilder(this)
        builder.setTitle("CorpId")
            .setDefaultText(Constant.weworkCorpId)
            .setInputType(InputType.TYPE_CLASS_TEXT)
            .addAction(getString(R.string.cancel)) { dialog, index -> dialog.dismiss() }
            .addAction(getString(R.string.add)) { dialog, index ->
                val text = builder.editText.text
                if (text != null) {
                    dialog.dismiss()
                    Constant.weworkCorpId = text.toString().trim()
                } else {
                    ToastUtils.showLong("请勿为空！")
                }
            }
            .create(R.style.QMUI_Dialog).show()
    }

    private fun showAgentIdDialog() {
        val builder = QMUIDialog.EditTextDialogBuilder(this)
        builder.setTitle("AgentId")
            .setDefaultText(Constant.weworkAgentId)
            .setInputType(InputType.TYPE_CLASS_TEXT)
            .addAction(getString(R.string.cancel)) { dialog, index -> dialog.dismiss() }
            .addAction(getString(R.string.add)) { dialog, index ->
                val text = builder.editText.text
                if (text != null) {
                    dialog.dismiss()
                    Constant.weworkAgentId = text.toString().trim()
                } else {
                    ToastUtils.showLong("请勿为空！")
                }
            }
            .create(R.style.QMUI_Dialog).show()
    }

    private fun showSchemaDialog() {
        val builder = QMUIDialog.EditTextDialogBuilder(this)
        builder.setTitle("Schema")
            .setDefaultText(Constant.weworkSchema)
            .setInputType(InputType.TYPE_CLASS_TEXT)
            .addAction(getString(R.string.cancel)) { dialog, index -> dialog.dismiss() }
            .addAction(getString(R.string.add)) { dialog, index ->
                val text = builder.editText.text
                if (text != null) {
                    dialog.dismiss()
                    Constant.weworkSchema = text.toString().trim()
                    IWWAPIUtil.init(this)
                } else {
                    ToastUtils.showLong("请勿为空！")
                }
            }
            .create(R.style.QMUI_Dialog).show()
    }

    private fun showUserNameDialog() {
        val builder = QMUIDialog.EditTextDialogBuilder(this)
        builder.setTitle("UserName")
            .setDefaultText(Constant.weworkMP)
            .setInputType(InputType.TYPE_CLASS_TEXT)
            .addAction(getString(R.string.cancel)) { dialog, index -> dialog.dismiss() }
            .addAction(getString(R.string.add)) { dialog, index ->
                val text = builder.editText.text
                if (text != null) {
                    dialog.dismiss()
                    val username = text.toString().trim()
                    Constant.weworkMP = if (username.endsWith("@app")) username else "$username@app"
                } else {
                    ToastUtils.showLong("请勿为空！")
                }
            }
            .create(R.style.QMUI_Dialog).show()
    }

    private fun showSignatureDialog() {
        val signature = AppUtils.getAppSignaturesMD5().firstOrNull()?.replace(":", "")?.toLowerCase(Locale.ROOT)
        val builder = QMUIDialog.EditTextDialogBuilder(this)
        builder.setTitle("Signature")
            .setDefaultText(signature)
            .setInputType(InputType.TYPE_NULL)
            .addAction(getString(R.string.copy)) { dialog, index ->
                dialog.dismiss()
                ClipboardUtils.copyText(signature)
                ToastUtils.showLong("复制成功")
            }
            .create(R.style.QMUI_Dialog).show()
    }

    private fun updateOldDeviceConfig() {
        if (Constant.oldDevice) {
            Constant.LONG_INTERVAL = (Constant.BASE_LONG_INTERVAL * 1.5).toLong()
            Constant.CHANGE_PAGE_INTERVAL = (Constant.BASE_CHANGE_PAGE_INTERVAL * 1.5).toLong()
            Constant.POP_WINDOW_INTERVAL = (Constant.BASE_POP_WINDOW_INTERVAL * 1.5).toLong()
            ToastUtils.showLong("防卡顿模式开启")
        } else {
            Constant.LONG_INTERVAL = Constant.BASE_LONG_INTERVAL
            Constant.CHANGE_PAGE_INTERVAL = Constant.BASE_CHANGE_PAGE_INTERVAL
            Constant.POP_WINDOW_INTERVAL = Constant.BASE_POP_WINDOW_INTERVAL
            ToastUtils.showLong("防卡顿模式关闭")
        }
    }

}
