package org.yameida.worktool.utils.capture;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.hardware.display.DisplayManager;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;

import com.blankj.utilcode.util.ScreenUtils;

import java.nio.ByteBuffer;


/**
 * 截图的底层实现类
 * 这个类的方法不要在业务中直接调用
 * 业务中使用ScreenCaptureUtil类
 *
 * @deprecated
 */
public class ScreenCaptureUtilByMediaPro {
    public static MediaProjectionManager mProjectionManager;
    public static Intent data;
    public static int resultCode;
    private static MediaProjection sMediaProjection;
    private static ImageReader mImageReaderHorizontal;
    private static ImageReader mImageReaderVertical;
    private static Handler backgroundHandler;

    private static Bitmap bitmapCacheHorizontal;
    private static Bitmap bitmapCacheVertical;

    public static void init() {
        sMediaProjection = mProjectionManager.getMediaProjection(resultCode, data);
        //start capture reader
        mImageReaderHorizontal = ImageReader.newInstance(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(),
                PixelFormat.RGBA_8888, 2);
        sMediaProjection.createVirtualDisplay(
                "ScreenShot",
                ScreenUtils.getScreenWidth(),
                ScreenUtils.getScreenHeight(),
                ScreenUtils.getScreenDensityDpi(),
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mImageReaderHorizontal.getSurface(),
                null,
                null);


        mImageReaderVertical = ImageReader.newInstance(ScreenUtils.getScreenHeight(), ScreenUtils.getScreenWidth(),
                PixelFormat.RGBA_8888, 2);
        sMediaProjection.createVirtualDisplay(
                "ScreenShot",
                ScreenUtils.getScreenHeight(),
                ScreenUtils.getScreenWidth(),
                ScreenUtils.getScreenDensityDpi(),
                DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
                mImageReaderVertical.getSurface(),
                null,
                null);


    }


    /**
     * 勿直接使用！！
     *
     * @return
     */
    @Deprecated
    public static Bitmap getScreenCapHorizontal() {

        Bitmap bitmap;
        android.media.Image image;
        do {
            image = mImageReaderHorizontal.acquireLatestImage();
            if (image == null && bitmapCacheHorizontal != null) {
                return bitmapCacheHorizontal;
            }
        } while (image == null);
        bitmap = covetBitmap(image);
        if (bitmapCacheHorizontal != null && !bitmapCacheHorizontal.isRecycled()) {
            bitmapCacheHorizontal.recycle();
            bitmapCacheHorizontal = null;
        }
        bitmapCacheHorizontal = bitmap;
        return bitmap;
    }


    /**
     * 勿直接使用！！
     *
     * @return
     */
    @Deprecated
    public static Bitmap getScreenCapVertical() {

        Bitmap bitmap;
        android.media.Image image;
        do {
            image = mImageReaderVertical.acquireLatestImage();
            if (image == null && bitmapCacheVertical != null) {
                return bitmapCacheVertical;
            }
        } while (image == null);
        bitmap = covetBitmap(image);
        if (bitmapCacheVertical != null) {
            bitmapCacheVertical.recycle();
            bitmapCacheVertical = null;
        }
        bitmapCacheVertical = bitmap;
        return bitmap;
    }


    public static Bitmap covetBitmap(android.media.Image image) {
        int width = image.getWidth();
        int height = image.getHeight();
        final android.media.Image.Plane[] planes = image.getPlanes();
        final ByteBuffer buffer = planes[0].getBuffer();
        //每个像素的间距
        int pixelStride = planes[0].getPixelStride();
        //总的间距
        int rowStride = planes[0].getRowStride();
        int rowPadding = rowStride - pixelStride * width;
        Bitmap bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height);
        image.close();
        return bitmap;
    }


    public static void stop() {
        sMediaProjection.stop();
    }
}
