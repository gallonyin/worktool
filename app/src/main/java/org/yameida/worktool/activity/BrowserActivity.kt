package org.yameida.worktool.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_browser.*
import org.yameida.worktool.R

/**
 * 浏览器页
 */
class BrowserActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_browser)

        initView()
    }

    private fun initView() {
        qmwv.loadUrl("https://wt.asrtts.cn")
    }
}