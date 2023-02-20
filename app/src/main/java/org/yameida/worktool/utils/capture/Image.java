package org.yameida.worktool.utils.capture;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Base64;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.blankj.utilcode.util.LogUtils;

//import org.opencv.android.Utils;
//import org.opencv.core.Core;
//import org.opencv.core.CvType;
//import org.opencv.core.Mat;
//import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;


public class Image {

    /**
     * 保存图片到图库
     * Image.saveImageToGallery(bt, getExternalFilesDir("").getAbsolutePath() + "/asdf.png");
     *
     * @param bmp
     */
    public static void saveImageToGallery(Bitmap bmp, String bitName) {
        File file = new File(bitName);
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 打开本地图片
     *
     * @param path
     * @return
     */
    public static Bitmap openImg(String path) {

        Bitmap ret = BitmapFactory.decodeFile(path);

        if (ret == null) {
            LogUtils.e("打开" + path + "失败！");
        }

        return ret;
    }


    /**
     * 单点找色
     *
     * @param img
     * @param color
     * @return
     */
    public static LinkedList<Point> findPoint(Bitmap img, Color color) {
        LinkedList<Point> pl = new LinkedList<Point>();
        int width = img.getWidth();
        int height = img.getHeight();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (Color.isSame(getPoint(img, i, j), color)) {
                    pl.add(new Point(i, j));
                }
            }
        }
        return pl;
    }


    /**
     * 多色找点
     * 在屏幕某个范围内
     *
     * @param img
     * @param colorRules
     * @param leftX
     * @param leftY
     * @param rightX
     * @param rightY
     * @return
     */
    public static Point findPointByMulColor(Bitmap img, String colorRules, int leftX, int leftY, int rightX, int rightY) {
        img = cropBitmap(img, leftX, leftY, rightX, rightY);
        Point p = findPointByMulColor(img, colorRules);
        if (p.isEmpty()) {
            return p;
        }
        return new Point(p.getX() + leftX, p.getY() + leftY);
    }

    /**
     * 多色找点函数
     *
     * @param img
     * @param colorRules
     * @return
     */
    public static Point findPointByMulColor(Bitmap img, String colorRules) {
        long now = System.currentTimeMillis();
        int[] colors = new int[img.getWidth() * img.getHeight()];
        String[] res = colorRules.split(",");
        Color firstPointColor = HexColor2DecColor(res[0], true);
        img.getPixels(colors, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
        for (int i = 0; i < colors.length; i++) {
            if (Color.isSame(new Color(colors[i]), firstPointColor)) {
                int y = (int) (i / img.getWidth());
                int x = i % img.getWidth();
                for (int k = 1; k < res.length; k++) {
                    res[k] = res[k].replace("\"", "");
                    String[] info = res[k].split("\\|");
                    int testX = x + Integer.parseInt(info[0]);
                    int testY = y + Integer.parseInt(info[1]);
                    if (testX < 0 || testY < 0 || testX > img.getWidth() || testY > img.getHeight()) {
                        break;
                    }
                    Color nextColor = getPoint(img, testX, testY);
                    if (!Color.isSame(nextColor, HexColor2DecColor(info[2], true))) {
                        break;
                    } else {
                        if (k == (res.length - 1)) {
                            LogUtils.i("找点用时：", String.valueOf(System.currentTimeMillis() - now));
                            return new Point(x, y);
                        }
                    }
                }
            }
        }
        return new Point(-1, -1);
    }

    /**
     * 多色找点,自定义颜色误差
     *
     * @param img
     * @param colorRules
     * @param offset
     * @return
     */
    public static Point findPointByMulColor(Bitmap img, String colorRules, int offset) {
        long now = System.currentTimeMillis();
        // 将图像转换成颜色数组
        int[] colors = new int[img.getWidth() * img.getHeight()];
        String[] res = colorRules.split(",");
        Color firstPointColor = HexColor2DecColor(res[0], true);
        img.getPixels(colors, 0, img.getWidth(), 0, 0, img.getWidth(), img.getHeight());
        //遍历颜色数组
        for (int i = 0; i < colors.length; i++) {
            // 寻找规则中第一个点
            if (Color.isSame(new Color(colors[i]), firstPointColor, offset)) {
                // 第一个点的y坐标
                int y = (int) (i / img.getWidth());
                // 第一个点的x坐标
                int x = i % img.getWidth();
                // 检查规则中后续每个点
                for (int k = 1; k < res.length; k++) {
                    //处理规则中多余的引号
                    res[k] = res[k].replace("\"", "");
                    String[] info = res[k].split("\\|");
                    int testX = x + Integer.parseInt(info[0]);
                    int testY = y + Integer.parseInt(info[1]);
                    //超出图片范围
                    if (testX < 0 || testY < 0 || testX > img.getWidth() || testY > img.getHeight()) {
                        break;
                    }
                    Color nextColor = getPoint(img, testX, testY);
                    if (!Color.isSame(nextColor, HexColor2DecColor(info[2], true), offset)) {
                        break;
                    } else {
                        if (k == (res.length - 1)) {
                            return new Point(x, y);
                        }
                    }
                }
            }
        }
        LogUtils.i("找点用时：", String.valueOf(System.currentTimeMillis() - now));
        return new Point(-1, -1);
    }

    /**
     * 已废弃
     *
     * @param img
     * @param colorRules
     * @return
     * @deprecated
     */
    public static Point findPointByMulColorBack(Bitmap img, String colorRules) {
        long now = System.currentTimeMillis();
        String[] res = colorRules.split(",");
        Color firstPointColor = HexColor2DecColor(res[0], true);
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();
        for (int i = 0; i < imgWidth; i++) {
            for (int j = 0; j < imgHeight; j++) {
                if (Color.isSame(getPoint(img, i, j), firstPointColor)) {
                    for (int k = 1; k < res.length; k++) {
                        res[k] = res[k].replace("\"", "");
                        String[] info = res[k].split("\\|");
                        int testX = i + Integer.parseInt(info[0]);
                        int testY = j + Integer.parseInt(info[1]);
                        if (testX < 0 || testY < 0 || testX > imgWidth || testY > imgHeight) {
                            break;
                        }
                        Color nextColor = getPoint(img, testX, testY);
                        if (!Color.isSame(nextColor, HexColor2DecColor(info[2], true))) {
                            break;
                        } else {
                            if (k == (res.length - 1)) {
                                LogUtils.i("找点用时：", String.valueOf(System.currentTimeMillis() - now));
                                return new Point(i, j);
                            }
                        }
                    }
                }
            }
        }
        LogUtils.i("找点用时：", String.valueOf(System.currentTimeMillis() - now));
        return new Point(-1, -1);
    }


    /**
     * 多色找点，返回屏幕内全部满足规则的点
     *
     * @param img
     * @param colorRules
     * @return
     */
    public static LinkedList<Point> findPointsByMulColor(Bitmap img, String colorRules) {
        LinkedList<Point> ret = new LinkedList<Point>();
        String[] res = colorRules.split(",");
        Color firstPointColor = HexColor2DecColor(res[0], true);
        int imgWidth = img.getWidth();
        int imgHeight = img.getHeight();

        for (int i = 0; i < imgWidth; i++) {
            for (int j = 0; j < imgHeight; j++) {
                if (Color.isSame(getPoint(img, i, j), firstPointColor)) {
                    for (int k = 1; k < res.length; k++) {
                        res[k] = res[k].replace("\"", "");
                        String[] info = res[k].split("\\|");
                        int testX = i + Integer.parseInt(info[0]);
                        int testY = j + Integer.parseInt(info[1]);
                        if (testX < 0 || testY < 0 || testX > imgWidth || testY > imgHeight) {
                            break;
                        }
                        Color nextColor = getPoint(img, testX, testY);
                        if (!Color.isSame(nextColor, HexColor2DecColor(info[2], true))) {
                            break;
                        } else {
                            if (k == (res.length - 1)) {
                                ret.add(new Point(i, j));
                            }
                        }
                    }
                }
            }
        }
        return ret;
    }


    /**
     * @param color
     * @return
     */
    public static Color HexColor2DecColor(String color) {
        color = color.replace("#", "");
        color = color.replace("\"", "");
        try {
            int r = Integer.parseInt(color.substring(0, 2), 16);
            int g = Integer.parseInt(color.substring(2, 4), 16);
            int b = Integer.parseInt(color.substring(4, 6), 16);
            return new Color(r, g, b);

        } catch (Exception e) {
            return new Color();
        }
    }

    /**
     * @param color
     * @param bgr
     * @return
     */
    public static Color HexColor2DecColor(String color, boolean bgr) {
        color = color.replace("#", "");
        color = color.replace("\"", "");
        try {
            int b = Integer.parseInt(color.substring(0, 2), 16);
            int g = Integer.parseInt(color.substring(2, 4), 16);
            int r = Integer.parseInt(color.substring(4, 6), 16);
            return new Color(r, g, b);

        } catch (Exception e) {
            return new Color();
        }
    }


    /**
     * 获取一个点的颜色
     *
     * @param img
     * @param x
     * @param y
     * @return
     */
    public static Color getPoint(Bitmap img, int x, int y) {
        try {
            return new Color(img.getPixel(x, y));
        } catch (IllegalArgumentException e) {
            return new Color(0, 0, 0);
        }


    }


    /**
     * 预览图片
     *
     * @param img
     * @param context
     */
//    public static void show(Bitmap img, Context context) {
//        Dialog dia = new Dialog(context, R.style.edit_AlertDialog_style2);
//        dia.setContentView(R.layout.activity_start_dialog);
//        ImageView imageView = (ImageView) dia.findViewById(R.id.start_img);
//        imageView.setImageBitmap(img);
//        dia.show();
//
//        dia.setCanceledOnTouchOutside(true); // Sets whether this dialog is
//        Window w = dia.getWindow();
//        WindowManager.LayoutParams lp = w.getAttributes();
//        lp.x = 0;
//        lp.y = 40;
//        dia.onWindowAttributesChanged(lp);
//    }


    /**
     * 裁剪
     *
     * @param bitmap
     * @param leftTopX
     * @param leftTopY
     * @param rightBottomX
     * @param rightBottomY
     * @return
     */
    public static Bitmap cropBitmap(Bitmap bitmap, int leftTopX, int leftTopY, int rightBottomX, int rightBottomY) {
        return Bitmap.createBitmap(bitmap, leftTopX, leftTopY, rightBottomX - leftTopX, rightBottomY - leftTopY, null, false);
    }


    /**
     * base64 图片
     *
     * @param bitmap
     * @return
     */
    public static String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        //读取图片到ByteArrayOutputStream
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos); //参数如果为100那么就不压缩
        byte[] bytes = baos.toByteArray();

        return Base64.encodeToString(bytes, Base64.DEFAULT);


    }


    /**
     * 模板匹配
     *
     * @param srcImg      //源图像
     * @param templateImg //模板图像
     * @param threshold   //相识度阈值,阈值调小可以一定程度解决不同手机分辨率的问题
     * @return //如果没有找到则返回(-1,-1)点
     */
//    public static Point matchTemplate(Bitmap srcImg, Bitmap templateImg, double threshold) {
//
//        if (threshold <= 0) {
//            threshold = 0.5;
//        }
//
//
//        Mat tpl = new Mat();
//        Mat src = new Mat();
//        Utils.bitmapToMat(srcImg, src);
//        Utils.bitmapToMat(templateImg, tpl);
//
//
//        int height = src.rows() - tpl.rows() + 1;
//        int width = src.cols() - tpl.cols() + 1;
//        Mat result = new Mat(height, width, CvType.CV_32FC1);
//        int method = Imgproc.TM_CCOEFF_NORMED;
//        Imgproc.matchTemplate(src, tpl, result, method);
//        Core.MinMaxLocResult minMaxResult = Core.minMaxLoc(result);
//        org.opencv.core.Point maxloc = minMaxResult.maxLoc;
//        if (minMaxResult.maxVal < threshold) {
//            return new Point(-1, -1);
//        }
//        org.opencv.core.Point minloc = minMaxResult.minLoc;
//        org.opencv.core.Point matchloc = null;
//        matchloc = maxloc;
//        return new Point((int) matchloc.x, (int) matchloc.y);
//
//    }


    /**
     * 根据给定的宽和高进行resize
     *
     * @param origin    原图
     * @param newWidth  新图的宽
     * @param newHeight 新图的高
     * @return new Bitmap
     */
    public static Bitmap resize(Bitmap origin, int newWidth, int newHeight) {
        if (origin == null) {
            return null;
        }
        int height = origin.getHeight();
        int width = origin.getWidth();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);// 使用后乘
        Bitmap newBM = Bitmap.createBitmap(origin, 0, 0, width, height, matrix, false);
        if (!origin.isRecycled()) {
            origin.recycle();
        }
        return newBM;
    }

}
