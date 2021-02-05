package com.bmobwork.bmobwork.demo;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class Person extends BmobObject {
    
    private String name;
    private String address;
    private BmobFile file;

    public BmobFile getFile() {
        return file;
    }

    public Person setFile(BmobFile file) {
        this.file = file;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
