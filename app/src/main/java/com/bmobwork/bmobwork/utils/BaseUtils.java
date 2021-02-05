package com.bmobwork.bmobwork.utils;

import com.bmobwork.bmobwork.config.Cons;
import com.bmobwork.bmobwork.log.Legg;

/*
 * Created by Administrator on 2021/02/005.
 */
public class BaseUtils {

    public static String TAG = Cons.TAG;// 日志标记

    /**
     * 流程打印
     *
     * @param msg 内容
     */
    public static  void printVerb(String msg) {
        Legg.v(TAG, Cons.TAG + " --> " + msg);
    }

    /**
     * 信息打印
     *
     * @param msg 内容
     */
    public static void printInfo(String msg) {
        Legg.i(TAG, Cons.TAG + " --> " + msg);
    }

    /**
     * 警告打印
     *
     * @param msg 内容
     */
    public static void printWarn(String msg) {
        Legg.w(TAG, Cons.TAG + " --> " + msg);
    }

    /**
     * 警告打印
     *
     * @param e 错误体
     */
    public static void printWarn(Throwable e) {
        Legg.w(TAG, Cons.TAG + " --> " + e.getMessage());
    }

    /**
     * 警告打印
     *
     * @param e 异常体
     */
    public static void printWarn(Exception e) {
        Legg.w(TAG, Cons.TAG + " --> " + e.getMessage());
    }

    /**
     * 信息打印
     *
     * @param msg 内容
     */
    public static void printErr(String msg) {
        Legg.e(TAG, Cons.TAG + " --> " + msg);
    }

    /**
     * 信息打印
     *
     * @param e 错误体
     */
    public static void printErr(Throwable e) {
        Legg.e(TAG, Cons.TAG + " --> " + e.getMessage());
    }

    /**
     * 信息打印
     *
     * @param e 异常体
     */
    public static void printErr(Exception e) {
        Legg.e(TAG, Cons.TAG + " --> " + e.getMessage());
    }
    
}
