package org.yameida.floatwindow

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.blankj.utilcode.util.*

import org.yameida.floatwindow.listener.FloatWindowListener
import kotlinx.android.synthetic.main.layout_menu_left.view.*
import kotlinx.android.synthetic.main.layout_menu_logo.view.*
import kotlinx.android.synthetic.main.layout_menu_right.view.*
import org.yameida.floatwindow.listener.OnClickListener

/**
 * Created by Gallon on 2019/9/7.
 */

class DefaultFloatService : BaseFloatWindow(), View.OnClickListener {

//    private var currentStatus = RecordStatusManager.PLAY_STATUS_STOP
    private var active = false
    private var context = Utils.getApp()
    private var manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val CHANNEL_ID_STRING = "907"
    var floatWindowListener: FloatWindowListener? = null
    var onClickListener: OnClickListener? = null

    inner class DefaultFloatServiceBinder : Binder() {
        fun getService() = this@DefaultFloatService
    }

    init {
        minY = SizeUtils.dp2px(100F)
        maxY = ScreenUtils.getScreenHeight() - SizeUtils.dp2px(150F)
    }

    override fun onBind(intent: Intent?): IBinder? = DefaultFloatServiceBinder()

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(CHANNEL_ID_STRING, "float", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(mChannel)
            val notification = Notification.Builder(context, CHANNEL_ID_STRING).build()
            startForeground(1, notification)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogUtils.d(TAG, "onStartCommand: ${intent?.data}")
        show()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun show() {
        super.show()
//        FloatWindowManager.isShow = true
        floatWindowListener?.show()
    }

    override fun hide() {
        super.hide()
//        FloatWindowManager.isShow = false
        floatWindowListener?.hide()
    }

    override fun onClick(v: View) {
        LogUtils.v(TAG, "float onClick: ")
        openMenu()
        if (v == leftView.fl_window_background_left || v == rightView.fl_window_background_right) {
            onClickListener?.onClick(v,-1)
        }
        if (v == leftView.iv_logo_left || v == rightView.iv_logo_right || v == leftView.iv_logo_left2 || v == rightView.iv_logo_right2) {
            onClickListener?.onClick(v, 0)
        }
        if (v == leftView.iv_start_left || v == rightView.iv_start_right) {
            onClickListener?.onClick(v,1)
        }
        if (v == leftView.iv_shot_left || v == rightView.iv_shot_right) {
            onClickListener?.onClick(v,2)
        }
        if (v == leftView.iv_back_left || v == rightView.iv_back_right) {
            onClickListener?.onClick(v,3)
        }
        if (v == leftView.iv_settings_left || v == rightView.iv_settings_right) {
            onClickListener?.onClick(v,4)
        }
        if (v == leftView.iv_resume_pause_left || v == rightView.iv_resume_pause_right) {
            onClickListener?.onClick(v,5)
        }
        if (v == leftView.iv_stop_left || v == rightView.iv_stop_right) {
            onClickListener?.onClick(v,6)
        }
    }

    override fun getLeftView(inflater: LayoutInflater): View {
        leftView = inflater.inflate(R.layout.layout_menu_left, null)
        leftView.iv_logo_left.setOnClickListener(this)
        leftView.iv_logo_left2.setOnClickListener(this)
        leftView.iv_start_left.setOnClickListener(this)
        leftView.iv_shot_left.setOnClickListener(this)
        leftView.iv_back_left.setOnClickListener(this)
        leftView.iv_resume_pause_left.setOnClickListener(this)
        leftView.iv_stop_left.setOnClickListener(this)
        leftView.iv_settings_left.setOnClickListener(this)
        leftView.fl_window_background_left.setOnClickListener(this)
        return leftView
    }

    override fun getRightView(inflater: LayoutInflater): View {
        rightView = inflater.inflate(R.layout.layout_menu_right, null)
        rightView.iv_logo_right.setOnClickListener(this)
        rightView.iv_logo_right2.setOnClickListener(this)
        rightView.iv_start_right.setOnClickListener(this)
        rightView.iv_shot_right.setOnClickListener(this)
        rightView.iv_back_right.setOnClickListener(this)
        rightView.iv_resume_pause_right.setOnClickListener(this)
        rightView.iv_stop_right.setOnClickListener(this)
        rightView.iv_settings_right.setOnClickListener(this)
        rightView.fl_window_background_right.setOnClickListener(this)
        return rightView
    }

    override fun getLogoView(inflater: LayoutInflater): View {
        return inflater.inflate(R.layout.layout_menu_logo, null)
    }

    override fun resetLogoViewSize(hintLocation: Int, logoView: View) {
        logoView.clearAnimation()
        logoView.translationX = 0f
        logoView.scaleX = 1f
        logoView.scaleY = 1f
        logoView.alpha = 1f
        logoView.tv_float_time.textSize = 10f
    }

    override fun dragLogoViewOffset(logoView: View, isDrag: Boolean, isResetPosition: Boolean, offset: Float) {
        if (isDrag && offset > 0) {
            logoView.scaleX = 1 + offset
            logoView.scaleY = 1 + offset
        } else {
            logoView.translationX = 0f
            logoView.scaleX = 1f
            logoView.scaleY = 1f
        }

        logoView.rotation = offset * 360
    }

    public override fun shrinkLeftLogoView(smallView: View) {
        smallView.translationX = (-smallView.width / 4).toFloat()
        smallView.alpha = 0.35f
        logoView.tv_float_time.textSize = 7f
    }

    public override fun shrinkRightLogoView(smallView: View) {
        smallView.translationX = (smallView.width / 4).toFloat()
        smallView.alpha = 0.35f
        logoView.tv_float_time.textSize = 7f
    }

    public override fun leftViewOpened(leftView: View) {
        val layoutParams = leftView.fl_window_measure_left.layoutParams as FrameLayout.LayoutParams
        leftView.fl_window_measure_left.measure(0, 0)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            layoutParams.topMargin = params.y - (leftView.fl_window_measure_left.measuredHeight / 2 - leftView.iv_logo_left.measuredHeight / 2)
        } else {
            layoutParams.topMargin = params.y - BarUtils.getStatusBarHeight() - (leftView.fl_window_measure_left.measuredHeight / 2 - leftView.iv_logo_left.measuredHeight / 2)
        }
        leftView.fl_window_measure_left.layoutParams = layoutParams
//        ToastUtils.showShort("左边的菜单被打开了")
    }

    public override fun rightViewOpened(rightView: View) {
        val layoutParams = rightView.fl_window_measure_right.layoutParams as FrameLayout.LayoutParams
        rightView.fl_window_measure_right.measure(0, 0)
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
            layoutParams.topMargin = params.y - (rightView.fl_window_measure_right.measuredHeight / 2 - rightView.iv_logo_right.measuredHeight / 2)
        } else {
            layoutParams.topMargin = params.y - BarUtils.getStatusBarHeight() - (rightView.fl_window_measure_right.measuredHeight / 2 - rightView.iv_logo_right.measuredHeight / 2)
        }
        layoutParams.leftMargin = ScreenUtils.getScreenWidth() - rightView.fl_window_measure_right.measuredWidth
        rightView.fl_window_measure_right.layoutParams = layoutParams
//        ToastUtils.showShort("右边的菜单被打开了")
    }

    public override fun leftOrRightViewClosed(smallView: View) {
//        Toast.makeText(context, "菜单被关闭了", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyed() {}
}
