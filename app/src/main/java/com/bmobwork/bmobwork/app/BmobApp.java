package com.bmobwork.bmobwork.app;

import android.app.Application;

import com.bmobwork.bmobwork.config.Cons;

import cn.bmob.v3.Bmob;

/*
 * Created by Administrator on 2021/02/002.
 */
public class BmobApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // BMOB需要: 初始化
        Bmob.initialize(this, Cons.APP_ID);
    }
}
