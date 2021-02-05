package com.bmobwork.bmobwork.log;

import android.annotation.SuppressLint;

import java.text.SimpleDateFormat;
import java.util.Date;

/*
 * Created by qianli.ma on 2020/3/23 0023.
 */
@SuppressLint("SimpleDateFormat")
public class LogBean {

    private String content;// 日志内容
    private LogEnum logEnum;
    private Class clazz;

    public LogBean(String content, LogEnum logEnum, Class clazz) {
        this.content = content;
        this.logEnum = logEnum;
        this.clazz = clazz;
    }

    /**
     * 格式如下:
     * 2020-11-30 08:33:55 [SimpleForm.java]
     * seaofwoefjaoensjfiosaenfoiasnefsenewfaefasf
     *
     * @return 拼接后的日志内容
     */
    public String getContent() {
        return content;
    }

    public String getTime() {
        String date = new SimpleDateFormat("yyyy-dd-mm HH:mm:ss").format(new Date());
        // 类名
        String clzName = clazz.getSimpleName();
        // 拼接
        StringBuilder builder = new StringBuilder();
        builder.append(date).append("\t").append("[").append(clzName).append(".java]\r\n");
        return builder.toString();
    }

    /**
     * @return 日志类型 -- 用于外部决定颜色
     */
    public LogEnum getLogEnum() {
        return logEnum;
    }
}
