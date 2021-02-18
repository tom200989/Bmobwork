package com.bmobwork.bmobwork.helper;

import android.app.Application;

import com.bmobwork.bmobwork.config.Cons;
import com.bmobwork.bmobwork.impl.IMGlobalHandler;

import cn.bmob.newim.BmobIM;
import cn.bmob.v3.Bmob;

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
        BmobIM.init(app);
        Bmob.initialize(app, Cons.APP_ID);
        BmobIM.registerDefaultMessageHandler(new IMGlobalHandler());
    }

}
