package com.bmobwork.bmobwork.helper;

import com.bmobwork.bmobwork.config.Cons;
import com.bmobwork.bmobwork.log.Legg;

import cn.bmob.v3.exception.BmobException;

/*
 * Created by Administrator on 2021/02/002.
 */
public class BmobBase {

    public String TAG = Cons.TAG;// 日志标记

    /**
     * 流程打印
     *
     * @param msg 内容
     */
    public void printVerb(String msg) {
        Legg.v(TAG, Cons.TAG + " --> " + msg);
    }

    /**
     * 信息打印
     *
     * @param msg 内容
     */
    public void printInfo(String msg) {
        Legg.i(TAG, Cons.TAG + " --> " + msg);
    }

    /**
     * 警告打印
     *
     * @param msg 内容
     */
    public void printWarn(String msg) {
        Legg.w(TAG, Cons.TAG + " --> " + msg);
    }

    /**
     * 警告打印
     *
     * @param e 错误体
     */
    public void printWarn(Throwable e) {
        Legg.w(TAG, Cons.TAG + " --> " + e.getMessage());
    }

    /**
     * 警告打印
     *
     * @param e 异常体
     */
    public void printWarn(Exception e) {
        Legg.w(TAG, Cons.TAG + " --> " + e.getMessage());
    }

    /**
     * 信息打印
     *
     * @param msg 内容
     */
    public void printErr(String msg) {
        Legg.e(TAG, Cons.TAG + " --> " + msg);
    }

    /**
     * 信息打印
     *
     * @param e 错误体
     */
    public void printErr(Throwable e) {
        Legg.e(TAG, Cons.TAG + " --> " + e.getMessage());
    }

    /**
     * 信息打印
     *
     * @param e 异常体
     */
    public void printErr(Exception e) {
        Legg.e(TAG, Cons.TAG + " --> " + e.getMessage());
    }

    /**
     * 错误处理器
     *
     * @param msg 内容
     * @param es  异常体
     */
    public void BmobError(String msg, Exception... es) {
        if (es != null) if (es.length > 0) es[0].printStackTrace();
        String esg = "unknown exception";
        int code = -1;
        if (es != null) if (es.length > 0) esg = es[0].getMessage();
        if (es != null) {
            if (es.length > 0) {
                if (es[0] instanceof BmobException) {
                    BmobException exp = (BmobException) es[0];
                    code = exp.getErrorCode();
                }
            }
        }

        printErr(msg + "--> " + esg + ", error code: " + code);
        BmobErrorNext(msg);
    }

    /* -------------------------------------------- impl -------------------------------------------- */

    // ---------------- 监听器 [BmobError] ----------------
    private OnBmobErrorListener onBmobErrorListener;

    public interface OnBmobErrorListener {
        void BmobError(String msg);
    }

    public void setOnBmobErrorListener(OnBmobErrorListener onBmobErrorListener) {
        this.onBmobErrorListener = onBmobErrorListener;
    }

    private void BmobErrorNext(String msg) {
        if (onBmobErrorListener != null) {
            onBmobErrorListener.BmobError(msg);
        }
    }

}
