package com.bmobwork.bmobwork.app;

import android.app.Application;

import com.bmobwork.bmobwork.helper.BmobInit;

/*
 * Created by Administrator on 2021/02/002.
 */
public class BmobApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        BmobInit.init(this);
    }
}
