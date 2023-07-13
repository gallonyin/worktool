package org.yameida.worktool.utils

import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import java.io.File
import kotlin.concurrent.thread

object CacheUtil {

    /**
     * 异步执行
     * 删除7天前的下载文件
     */
    fun autoDelete() {
        thread {
            LogUtils.d("执行自动清除缓存任务")
            val externalFilesDir = Utils.getApp().getExternalFilesDir("share")
            if (externalFilesDir != null && externalFilesDir.isDirectory) {
                val count = deleteOldFiles(System.currentTimeMillis() - 7 * 86400 * 1000, externalFilesDir)
                LogUtils.d("已清除文件数: $count")
            } else {
                LogUtils.d("未发现缓存文件夹")
            }
        }
    }

    private fun deleteOldFiles(deleteTime: Long, directory: File?): Int {
        var count = 0
        if (directory != null && directory.isDirectory) {
            val files = directory.listFiles()
            if (files != null) {
                for (file in files) {
                    if (file.isDirectory) {
                        count += deleteOldFiles(deleteTime, file)
                    } else {
                        if (file.lastModified() < deleteTime) {
                            file.delete()
                            count += 1
                        }
                    }
                }
            }
        }
        return count
    }
}