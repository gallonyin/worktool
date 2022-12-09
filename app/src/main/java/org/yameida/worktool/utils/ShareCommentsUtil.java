package org.yameida.worktool.utils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;

import com.blankj.utilcode.util.ToastUtils;

import org.yameida.worktool.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class ShareCommentsUtil {
    private static boolean checkInstallation(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static void shareToWeChat(Context context) {
        try {
            if (!checkInstallation(context, "com.tencent.mm")) {
                ToastUtils.showShort("未发现微信");
                return;
            }
            Intent intent = new Intent(); //分享精确到微信的页面，朋友圈页面，或者选择好友分享页面
            ComponentName comp = new ComponentName("com.tencent.mm", "com.tencent.mm.ui.tools.ShareToTimeLineUI");
            intent.setComponent(comp);
            intent.setAction(Intent.ACTION_SEND_MULTIPLE);
            intent.setType("image/*"); // intent.setType("text/plain");添加Uri图片地址
            String msg = "我想分享";
            intent.putExtra("Kdescription", msg);
            ArrayList<Uri> imageUris = new ArrayList<Uri>();
            File dir = context.getExternalFilesDir(null);
            if (dir == null || dir.getAbsolutePath().equals("")) {
                dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            }
            File pic = new File(dir, "bigbang.jpg");
            pic.deleteOnExit();
            BitmapDrawable bitmapDrawable;
            bitmapDrawable = (BitmapDrawable) context.getDrawable(R.mipmap.ic_launcher);
            try {
                bitmapDrawable.getBitmap().compress(Bitmap.CompressFormat.JPEG, 75, new FileOutputStream(pic));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Uri uri = Uri.parse(android.provider.MediaStore.Images.Media.insertImage(context.getContentResolver(), pic.getAbsolutePath(), "bigbang.jpg", null));
            imageUris.add(uri);
            intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, imageUris);
            ((Activity) context).startActivityForResult(intent, 1000);
        } catch (Throwable e) {
            ToastUtils.showShort("分享到微信失败");
        }
    }
}