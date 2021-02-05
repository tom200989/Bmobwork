package com.bmobwork.bmobwork.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;

import com.bmobwork.bmobwork.config.Cons;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Legg {
    public static int VERBOSE = 1;
    private static int DEBUG = 2;
    public static int INFO = 3;
    public static int WARN = 4;
    public static int ERROR = 5;
    private static int ASSERT = 6;
    private static int SHOW_ALL = 7;
    private static int STOP_ALL = 0;
    private static int LOG_FLAG;
    public static String tag = Cons.TAG;
    private static Legg logs;
    private static Thread logThread;
    public static boolean isPrintOne;// 只打1次标记位
    public static Context context;// 适配android Q

    public Legg() {
    }

    public static Legg t(String tags) {
        tag = tags;
        if (logs == null) {
            Class var1 = Legg.class;
            synchronized (Legg.class) {
                if (logs == null) {
                    logs = new Legg();
                }
            }
        }

        return logs;
    }

    public Legg openClose(boolean printTag) {
        LOG_FLAG = printTag ? SHOW_ALL : STOP_ALL;
        return this;
    }

    public void vv(String msg) {
        if (VERBOSE < LOG_FLAG) {
            Log.v(tag, msg);
        }

    }

    public static void v(String tag, String msg) {
        if (VERBOSE < LOG_FLAG) {
            Log.v(tag, msg);
        }

    }

    public void dd(String msg) {
        if (DEBUG < LOG_FLAG) {
            Log.d(tag, msg);
        }

    }

    public static void d(String tag, String msg) {
        if (DEBUG < LOG_FLAG) {
            Log.d(tag, msg);
        }

    }

    public void ii(String msg) {
        if (INFO < LOG_FLAG) {
            Log.i(tag, msg);
        }

    }

    public static void i(String tag, String msg) {
        if (INFO < LOG_FLAG) {
            Log.i(tag, msg);
        }

    }

    public void ww(String msg) {
        if (WARN < LOG_FLAG) {
            Log.w(tag, msg);
        }

    }

    public static void w(String tag, String msg) {
        if (WARN < LOG_FLAG) {
            Log.w(tag, msg);
        }

    }

    public void ee(String msg) {
        if (ERROR < LOG_FLAG) {
            Log.e(tag, msg);
        }

    }

    public static void e(String tag, String msg) {
        if (ERROR < LOG_FLAG) {
            Log.e(tag, msg);
        }

    }

    public static void startRecordLog() {
        if (logThread == null) {
            logThread = new LogThread();
            logThread.start();
        }

    }

    public static void stopRecordLog() {
        if (logThread != null) {
            LogThread.killLoop();
            logThread.interrupt();
            logThread = null;
        }

    }

    public static void writeToSD(String content) {
        if (logThread != null) {
            LogThread.addContentToList(content);
        }
    }

    public static void createdLogDir() {
        if (logThread != null) {
            LogThread.createdLogDirOut(context);
        }

    }

    static {
        LOG_FLAG = SHOW_ALL;
    }

    /**
     * 打印并记录日志
     *
     * @param clazz   当前打印log的类
     * @param content 外部输入内容
     * @param type    0:verbose 1:error
     */
    @SuppressLint("SimpleDateFormat")
    public void recordLog(Class clazz, String content, int type) {
        // 类名
        String clazzName = clazz.getSimpleName();
        // 日期
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String date = format.format(new Date());
        // 整合 "--> 2019-3-10 16:30:55 ［fastcore］xxxxxx:xxxxxx:xxxxxx"
        StringBuffer buffer = new StringBuffer();
        buffer.append("--> ").append(date).append("\t[ ").append(clazzName).append(" ]\t").append(content);
        String outs = buffer.toString();
        // 1.打印日志
        if (type == Legg.VERBOSE) {
            vv(outs);
        } else if (type == Legg.INFO) {
            ii(outs);
        } else if (type == Legg.ERROR) {
            ee(outs);
        }
        // 2.记录LOG到SD
        writeToSD(outs + "\n");
    }
}
