package org.yameida.worktool.utils;

import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class ImageDepthSizeUtil {

    public static boolean checkRawImage(String path) {
        try {
            File file = new File(path);
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] b = new byte[1000000];
            fileInputStream.read(b);
            int bitsPerPixel = (b[25] & 0xff) == 2 || (b[25] & 0xff) == 6 ? (b[24] & 0xff) * 3 : b[24] & 0xff;
            LogUtils.v("path: " + path, "bitsPerPixel: " + bitsPerPixel);
            return bitsPerPixel == 24;
        } catch (IOException e) {
            LogUtils.e("ImageDepthSize Check Error", e);
        }
        return false;
    }

}
