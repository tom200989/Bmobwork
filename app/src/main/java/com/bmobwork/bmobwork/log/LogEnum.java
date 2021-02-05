package com.bmobwork.bmobwork.log;

/*
 * Created by qianli.ma on 2020/3/23 0023.
 */
public enum LogEnum {
    VERBOSE(0),// 流程日志
    INFO(1),// 数据日志
    ERROR(2);// 错误日志


    private int logType;

    LogEnum(int logType) {
        this.logType = logType;
    }
}
