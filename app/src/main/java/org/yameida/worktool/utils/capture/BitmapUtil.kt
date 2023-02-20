package org.yameida.worktool.utils.capture

import android.graphics.BitmapFactory
import android.graphics.Bitmap
import android.provider.MediaStore
import android.app.Activity
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.IOException


/**
 * Created by Gallon on 2019/9/1.
 */
object BitmapUtil {

    /**
     * 获取图片的旋转角度
     *
     * @param path 图片绝对路径
     * @return 图片的旋转角度
     */
    fun getBitmapDegree(path: String): Int {
        var degree = 0
        try {
            // 从指定路径下读取图片，并获取其EXIF信息
            val exifInterface = ExifInterface(path)
            // 获取图片的旋转信息
            val orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> degree = 90
                ExifInterface.ORIENTATION_ROTATE_180 -> degree = 180
                ExifInterface.ORIENTATION_ROTATE_270 -> degree = 270
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return degree
    }

    /**
     * 将图片按照指定的角度进行旋转
     *
     * @param bitmap 需要旋转的图片
     * @param degree 指定的旋转角度
     * @return 旋转后的图片
     */
    fun rotateBitmapByDegree(bitmap: Bitmap, degree: Float): Bitmap {
        // 根据旋转角度，生成旋转矩阵
        val matrix = Matrix()
        matrix.postRotate(degree)
        // 将原始图片按照旋转矩阵进行旋转，并得到新的图片
        val newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        if (!bitmap.isRecycled) {
            bitmap.recycle()
        }
        return newBitmap
    }

    /**
     * 获取我们需要的整理过旋转角度的Uri
     * @param activity  上下文环境
     * @param path      路径
     * @return          正常的Uri
     */
    fun getRotatedUri(activity: Activity, path: String): Uri {
        val degree = getBitmapDegree(path)
        if (degree != 0) {
            val bitmap = BitmapFactory.decodeFile(path)
            val newBitmap = rotateBitmapByDegree(bitmap, degree.toFloat())
            return Uri.parse(MediaStore.Images.Media.insertImage(activity.contentResolver, newBitmap, null, null))
        } else {
            return Uri.fromFile(File(path))
        }
    }

    /**
     * 将图片按照指定的角度进行旋转
     *
     * @param path   需要旋转的图片的路径
     * @param degree 指定的旋转角度
     * @return 旋转后的图片
     */
    fun rotateBitmapByDegree(path: String, degree: Float): Bitmap {
        val bitmap = BitmapFactory.decodeFile(path)
        return rotateBitmapByDegree(bitmap, degree)
    }

}