package org.yameida.worktool.utils.capture

import android.media.projection.MediaProjection
import android.os.Handler
import android.os.Looper

/**
 * Created by Gallon on 2019/8/4.
 */
class MediaProjectionHolder {

    companion object {
        var mMediaProjection: MediaProjection? = null

        @Synchronized
        fun setMediaProjection(mediaProjection: MediaProjection) {
            if (mMediaProjection == null) {
                mMediaProjection = mediaProjection
                mediaProjection.registerCallback(object : MediaProjection.Callback() {
                    override fun onStop() {
                        mMediaProjection = null
                    }
                }, Handler(Looper.getMainLooper()))
            }
        }
    }

}