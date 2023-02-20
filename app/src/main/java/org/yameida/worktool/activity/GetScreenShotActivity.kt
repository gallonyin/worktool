package org.yameida.worktool.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.*
import org.yameida.worktool.utils.capture.AndroidUtils
import org.yameida.worktool.utils.capture.MediaProjectionHolder

import android.util.DisplayMetrics

import android.view.WindowManager
import org.yameida.worktool.utils.startServiceSafe
import org.yameida.worktool.service.PlayNotifyService
import java.lang.Exception


/**
 * Created by Gallon on 2019/7/30.
 */
class GetScreenShotActivity : AppCompatActivity() {
    private var mediaProjectionManager: MediaProjectionManager? = null
    private var hideFloatWindow = false
    private val handler = Handler(Looper.getMainLooper())

    companion object {
        val HIDE_FLOAT_WINDOW = "hideFloatWindow"

        fun startCapture() {
            val imageReader = ImageReader.newInstance(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(), PixelFormat.RGBA_8888, 1)
            val virtualDisplay = MediaProjectionHolder.mMediaProjection?.createVirtualDisplay("ScreenShout",
                    ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(), ScreenUtils.getScreenDensityDpi(),
                    DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                    imageReader?.surface, null, null)

            val imageName = System.currentTimeMillis().toString() + ".png"
            var image: Image? = null
            var tryCount = 0
            while (tryCount < 10 && image == null) {
                SystemClock.sleep(250)
                image = imageReader?.acquireNextImage()
            }
            if (image == null) {
                LogUtils.i("GetScreenShotActivity", "image is null.")
                return
            }
            val width = image.width
            val height = image.height
            val planes = image.planes
            val buffer = planes[0].buffer
            val pixelStride = planes[0].pixelStride
            val rowStride = planes[0].rowStride
            val rowPadding = rowStride - pixelStride * width
            var bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
            bitmap.copyPixelsFromBuffer(buffer)
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height)
            image.close()
            imageReader.close()
            virtualDisplay?.release()

            if (bitmap != null) {
                AndroidUtils.bitmapToFile(bitmap, imageName)
            }
            ToastUtils.showShort("截图已保存到sdcard/recorder/screenShot~~~")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LogUtils.i("onCreate")
        window.statusBarColor = Color.TRANSPARENT
        mediaProjectionManager = Utils.getApp().getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        hideFloatWindow = intent.getBooleanExtra(HIDE_FLOAT_WINDOW, false)

        PermissionUtils.permission(PermissionConstants.STORAGE)
                .callback(object : PermissionUtils.SimpleCallback {
                    override fun onGranted() {
                        LogUtils.d("start record")
                        mediaProjectionManager?.apply {
                            val intent = this.createScreenCaptureIntent()
                            if (packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY) != null) {
                                startActivityForResult(intent, 0)
                            } else {
                                ToastUtils.showShort("抱歉，你的手机暂不支持录屏")
                            }
                        }
                    }

                    override fun onDenied() {
                        ToastUtils.showShort("请允许申请的权限，否则无法录屏")
                        finish()
                    }
                })
                .request()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        LogUtils.i("onActivityResult")

        if (resultCode == Activity.RESULT_OK && data != null) {
            LogUtils.e("hahaha1")
            LogUtils.e("mediaProjectionManager: $mediaProjectionManager")
            LogUtils.e("resultCode: $resultCode")
            LogUtils.e("data: $data")
//            MediaProjectionHolder.setMediaProjection(mediaProjectionManager!!.getMediaProjection(resultCode, data))
//            LogUtils.e("hahaha2")
//            if (hideFloatWindow) {
//                if (FloatWindowManager.isShow) {
//                    FloatWindowManager.hide()
//                    handler.postDelayed({
//                        SystemClock.sleep(100)
//                        startCapture()
//                        FloatWindowManager.show()
//                    }, 200)
//                } else {
//                    SystemClock.sleep(200)
//                    startCapture()
//                }
//            } else {
//                startCapture()
//            }
//            startCapture()

            try {
                val mWindowManager = getSystemService(WINDOW_SERVICE) as WindowManager
                val metrics = DisplayMetrics()
                mWindowManager.defaultDisplay.getMetrics(metrics)
            } catch (e: Exception) {
                LogUtils.e("MediaProjection error")
            }
            sendBroadcast(Intent())
            val service = Intent(this, PlayNotifyService::class.java)
            service.putExtra("setMediaProject", true)
            service.putExtra("code", resultCode)
            service.putExtra("data", data)
            startServiceSafe(service)

            LogUtils.e("hahaha3")
        }
        finish()
    }


}