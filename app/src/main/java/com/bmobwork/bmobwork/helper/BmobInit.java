package com.bmobwork.bmobwork.helper;

import android.app.Application;

import com.bmobwork.bmobwork.config.Cons;

import cn.bmob.v3.Bmob;
import cn.leancloud.AVLogger;
import cn.leancloud.core.AVOSCloud;
import cn.leancloud.session.AVConnectionManager;

/*
 * Created by Administrator on 2021/2/16.
 * 该类被外部开发调用
 */
public class BmobInit extends BmobBase {

    /**
     * 初始化方法
     */
    public static void init(Application app) {
        // BMOB需要: 初始化
        Bmob.initialize(app, Cons.APP_ID);
        // LeanIM Storage 必需
        AVOSCloud.initialize(Cons.LEAN_ID, Cons.LEAN_KEY);
        AVOSCloud.setLogLevel(AVLogger.Level.VERBOSE);
        // LeanIM IM 必需
        AVConnectionManager manager = AVConnectionManager.getInstance();
        manager.startConnection(null);
    }
}
