package org.yameida.worktool.utils

import android.content.Intent
import org.yameida.worktool.R
import android.content.Context
import android.net.Uri
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import com.qmuiteam.qmui.widget.dialog.QMUIDialog

object DonateUtil {

    fun zfbDonate(context: Context) {
        try {
            QMUIDialog.MessageDialogBuilder(context)
                .setTitle(context.getString(R.string.host_list))
                .setTitle("捐赠")
                .setMessage("如果你觉得${context.getString(R.string.app_name)}很棒，可否愿意花一点点钱请作者喝杯咖啡")
                .addAction("支付宝") {
                    dialog, index -> dialog.dismiss()
                    ToastUtils.showLong(Utils.getApp().getString(R.string.app_name) + " 因为有你的支持而能够不断更新、完善，非常感谢支持！")
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("alipays://platformapi/startapp?saId=10000007&clientVersion=3.7.0.0718&qrcode=https%3A%2F%2Fqr.alipay.com%2Ffkx15436xnv3mzpuufhvn52%3F_s%3Dweb-other")
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(intent)
                }
                .create(R.style.QMUI_Dialog)
                .show()
        } catch (e: Throwable) {
            ToastUtils.showShort("打开支付宝失败，你可能还没有安装支付宝客户端")
        }
    }
}