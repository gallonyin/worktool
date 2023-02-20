package org.yameida.worktool.service

import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import org.yameida.worktool.R
import org.yameida.worktool.activity.GetScreenShotActivity
import org.yameida.worktool.utils.capture.MediaProjectionHolder


/**
 * Created by Gallon on 2019/8/7.
 */
class PlayNotifyService : Service() {
    private val TAG = PlayNotifyService::class.java.simpleName

    companion object {
        private const val PLAY_NOTIFY_ID = 0x1216
        private const val CHANNEL_ONE_ID = "RECORD_CHANNEL_ONE_ID"
        private const val CHANNEL_ONE_NAME = "RECORD_CHANNEL_ONE_NAME"
        const val PLAY_NOTIFY = "record_notify"
        const val PLAY_NOTIFY_CODE = "record_notify_code"
        private var playNotifyReceiver: PlayNotifyReceiver = PlayNotifyReceiver()
    }
    private var context = Utils.getApp()
    private var manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private var notification: Notification? = null

    inner class PlayNotifyServiceBinder : Binder() {
        fun getService() = this@PlayNotifyService
    }

    override fun onBind(intent: Intent?): IBinder? = PlayNotifyServiceBinder()

    override fun onDestroy() {
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        LogUtils.d(TAG, "onStartCommand: $intent")
        show(null)
        initBroadcastReceivers()
        if (intent?.getBooleanExtra("setMediaProject", false) == true) {
            val resultCode = intent.getIntExtra("code", -1)
            val data = intent.getParcelableExtra<Intent>("data")
            val mediaProjectionManager = Utils.getApp().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            MediaProjectionHolder.setMediaProjection(mediaProjectionManager.getMediaProjection(resultCode, data!!))
        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun show(view: RemoteViews?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ONE_ID, CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            channel.enableLights(true) //是否在桌面icon右上角展示小红点
            channel.lightColor = Color.GREEN //小红点颜色
            channel.setShowBadge(true) //是否在久按桌面图标时显示此渠道的通知
            channel.setSound(null, null) //静音 NOTE:首次安装生效 否则需要APP清除数据生效
            manager.createNotificationChannel(channel)
        }
        val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ONE_ID)
                .setCustomContentView(view)
                .setWhen(System.currentTimeMillis())
                .setAutoCancel(false)
                .setOngoing(true)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
        notification = notificationBuilder.build()
        startForeground(PLAY_NOTIFY_ID, notification)
    }

    private fun initBroadcastReceivers() {
        val filter = IntentFilter(PLAY_NOTIFY)
        context.registerReceiver(playNotifyReceiver, filter)
    }

    private class PlayNotifyReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val code = intent.getIntExtra(PLAY_NOTIFY_CODE, -1)
            if (code == -1) {
                return
            }

            LogUtils.d("notification click: $code")
            if (MediaProjectionHolder.mMediaProjection != null) {
                SystemClock.sleep(300)
                GetScreenShotActivity.startCapture()
            } else {
                fastStartActivity(context, GetScreenShotActivity::class.java)
            }
        }
    }

}