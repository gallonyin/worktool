package org.yameida.worktool.activity

import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import org.yameida.worktool.R
import java.util.*

/**
 * 登录页
 */
class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        setContentView(R.layout.activity_login)

        initView()
    }

    private fun initView() {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        if (hour in 12..17) {
            textView.text = "Afternoon"
        } else if (hour in 18..24 || hour in 0..6) {
            textView.text = "Night"
            imageView.setImageResource(R.drawable.good_night_img)
        }
        tv_visitor_login.setOnClickListener {
            ListenActivity.enterActivity(this, 0)
            finish()
        }
    }
}
