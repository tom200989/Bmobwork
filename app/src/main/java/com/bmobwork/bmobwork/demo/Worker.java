package com.bmobwork.bmobwork.demo;

import cn.bmob.v3.BmobObject;

public class Worker extends BmobObject {

    private String work;
    private String sex;

    public String getWork() {
        return work;
    }

    public Worker setWork(String work) {
        this.work = work;
        return this;
    }

    public String getSex() {
        return sex;
    }

    public Worker setSex(String sex) {
        this.sex = sex;
        return this;
    }
}
