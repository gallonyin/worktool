package org.yameida.worktool.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.SPUtils
import com.blankj.utilcode.util.Utils
import kotlinx.android.synthetic.main.activity_float_guide.*
import org.yameida.worktool.R

/**
 * Created by gallon on 2019/7/20.
 * 提示开启悬浮窗权限
 */
class FloatViewGuideActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_float_guide)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        tv_float_allow.setOnClickListener {
            try {
                if (!Settings.canDrawOverlays(Utils.getApp())) {
                    val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                        data = Uri.parse("package:$packageName")
                    }
                    startActivity(intent)
                }
            } catch (t: Throwable) {}
        }
        tv_float_reject.setOnClickListener {
            finish()
        }
        cb_guide_not.isChecked = SPUtils.getInstance().getBoolean("not_show_float_guide", false)
        cb_guide_not.setOnCheckedChangeListener { buttonView, isChecked ->
            SPUtils.getInstance().put("not_show_float_guide", isChecked)
        }

        val alphaAnimation = AlphaAnimation(0.2F, 1F).apply {
            duration = 800
            repeatCount = Animation.INFINITE
            repeatMode = Animation.REVERSE
        }
        iv_over_finger.startAnimation(alphaAnimation)
    }

    override fun onResume() {
        super.onResume()
        val canDrawOverlays = Settings.canDrawOverlays(Utils.getApp())
        LogUtils.d("Settings.canDrawOverlays: $canDrawOverlays")
        if (canDrawOverlays) {
            finish()
        }
    }

}