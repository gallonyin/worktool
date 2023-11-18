package org.yameida.worktool.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class StringFeatureUtil {

    public static int generateFeatureValue(String input) {
        try {
            // 计算MD5哈希值
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] md5Hash = md.digest(input.getBytes(StandardCharsets.UTF_8));

            // 计算字符串长度并取余
            int length = input.length() % 1024;

            // 将MD5哈希值的前面位数组成int值
            int md5Value = 0;
            for (int i = 0; i < Math.min(4, md5Hash.length); i++) {
                md5Value = (md5Value << 8) | (md5Hash[i] & 0xFF);
            }

            // 将字符串长度合并到特征值的最后10位
            int featureValue = (md5Value << 10) | length;

            return featureValue;
        } catch (Exception e) {
            e.printStackTrace();
            return -1; // 错误情况下返回-1
        }
    }

}
