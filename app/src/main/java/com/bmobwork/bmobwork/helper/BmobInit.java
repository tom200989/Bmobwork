package com.bmobwork.bmobwork.helper;

import android.app.Application;

import com.bmobwork.bmobwork.impl.IMGlobalHandler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import cn.bmob.newim.BmobIM;

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
        // Bmob.initialize(app, Cons.APP_ID);
        if (app.getApplicationInfo().packageName.equals(getMyProcessName())) {
            BmobIM.init(app);
            BmobIM.registerDefaultMessageHandler(new IMGlobalHandler());
        }
    }

    /**
     * 获取当前运行的进程名
     */
    public static String getMyProcessName() {
        try {
            File file = new File("/proc/" + android.os.Process.myPid() + "/" + "cmdline");
            BufferedReader mBufferedReader = new BufferedReader(new FileReader(file));
            String processName = mBufferedReader.readLine().trim();
            mBufferedReader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
