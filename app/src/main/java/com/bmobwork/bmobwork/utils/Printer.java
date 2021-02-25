package com.bmobwork.bmobwork.utils;

import com.bmobwork.bmobwork.config.Cons;
import com.bmobwork.bmobwork.log.Legg;

/*
 * Created by Administrator on 2021/02/003.
 */
public class Printer {

    public static String TAG = Cons.TAG;

    /**
     * 信息打印
     *
     * @param msg 内容
     */
    public static void i(String msg) {
        Legg.i(TAG, Cons.TAG + " --> " + msg);
    }

    /**
     * 信息打印
     *
     * @param msg 内容
     */
    public static void e(String msg) {
        Legg.e(TAG, Cons.TAG + " --> " + msg);
    }

    /**
     * 信息打印
     *
     * @param msg 内容
     */
    public static void w(String msg) {
        Legg.w(TAG, Cons.TAG + " --> " + msg);
    }

    /**
     * 信息打印
     *
     * @param msg 内容
     */
    public static void v(String msg) {
        Legg.v(TAG, Cons.TAG + " --> " + msg);
    }
}
