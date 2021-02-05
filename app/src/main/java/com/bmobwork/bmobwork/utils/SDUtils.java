package com.bmobwork.bmobwork.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Objects;

/*
 * Created by Administrator on 2021/02/005.
 */
public class SDUtils extends BaseUtils {

    /**
     * 获取SD卡根路径
     *
     * @param dirName SD下的指定目录
     * @return 根路径
     */
    public static String getSdRootPath(Context context, String dirName) {
        // 规避斜杆错误
        dirName = dirName.startsWith(File.separator) ? dirName : File.separator + dirName;
        // 定义版本层
        int SDK_cur = Build.VERSION.SDK_INT;
        int SDK_Q = Build.VERSION_CODES.Q;
        int SDK_R = Build.VERSION_CODES.R;

        // 当前版本 < Android Q
        if (SDK_cur < SDK_Q) {
            printInfo("当前路径为[SDK_cur < SDK_Q], 路径为[传统模式]");
            return Environment.getExternalStorageDirectory().getAbsolutePath() + dirName;
        }

        // 当前版本 == Android Q
        if (SDK_cur == SDK_Q) {
            // requestLegacyExternalStorage 兼容
            if (isRequestLegacyExternalStorage(context)) {// requestLegacyExternalStorage = true - 传统路径
                printInfo("当前路径为[SDK_cur == SDK_Q], 路径为[传统模式]");
                return Environment.getExternalStorageDirectory().getAbsolutePath() + dirName;
            } else {// requestLegacyExternalStorage = false - 沙盒路径
                printInfo("当前路径为[SDK_cur == SDK_Q], 路径为[沙盒模式]");
                return Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath() + dirName;
            }

        }

        // 当前版本 > Android Q (大于等于 Android R)
        if (SDK_cur >= SDK_R) {
            if (Environment.isExternalStorageManager()) {// 是否有超管权限 - 传统模式
                printInfo("当前路径为[SDK_cur >= SDK_R], 且开通超管权限, 路径为[传统模式]");
                return Environment.getExternalStorageDirectory().getAbsolutePath() + dirName;
            } else {// 沙盒模式
                printInfo("当前路径为[SDK_cur >= SDK_R], 且未开通超管权限, 路径为[沙盒模式]");
                return Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath() + dirName;
            }
        } else {
            return Objects.requireNonNull(context.getExternalFilesDir(null)).getAbsolutePath() + dirName;
        }
    }

    /**
     * 获取SD卡指定目录下文件路径
     *
     * @param context  域
     * @param dirName  目录名
     * @param fileName 文件名
     * @return 全路径
     */
    public static String getSdFilePath(Context context, String dirName, String fileName) {
        // 规避斜杆错误
        fileName = fileName.startsWith(File.separator) ? fileName : File.separator + fileName;
        return getSdRootPath(context, dirName) + fileName;
    }

    /**
     * 获取SD卡指定目录下文件对象
     *
     * @param context  域
     * @param dirName  目录名
     * @param fileName 文件名
     * @return 文件对象
     */
    public static File getSdFile(Context context, String dirName, String fileName) {
        // 规避斜杆错误
        fileName = fileName.startsWith(File.separator) ? fileName : File.separator + fileName;
        String absolutePath = getSdRootPath(context, dirName) + fileName;
        return new File(absolutePath);
    }


    /**
     * 判断AndroidMainfest.xml中 [RequestLegacyExternalStorage] 的值 (仅在Android Q这个版本使用)
     *
     * @param context 域
     * @return T: 设置了true
     */
    protected static boolean isRequestLegacyExternalStorage(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            ApplicationInfo appInfo = packageInfo.applicationInfo;
            Field field = appInfo.getClass().getDeclaredField("privateFlags");
            field.setAccessible(true);
            int value = (int) field.get(appInfo);
            printInfo("读取Legacy标签成功");
            return (value & (1 << 29)) != 0;
        } catch (Exception e) {
            printErr("读取Legacy标签出错");
            e.printStackTrace();
        }
        return false;
    }


}
