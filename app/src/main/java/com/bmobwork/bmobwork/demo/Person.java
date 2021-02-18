package com.bmobwork.bmobwork.demo;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class Person extends BmobObject {
    
    private String name;
    private String address;
    private int age;
    private BmobFile file;

    public BmobFile getFile() {
        return file;
    }

    public Person setFile(BmobFile file) {
        this.file = file;
        return this;
    }

    public int getAge() {
        return age;
    }

    public Person setAge(int age) {
        this.age = age;
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

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Person{");
        sb.append("\n").append("\t").append("name ='").append(name).append('\'');
        sb.append("\n").append("\t").append("address ='").append(address).append('\'');
        sb.append("\n").append("\t").append("age =").append(age);
        sb.append("\n").append("\t").append("file =").append(file);
        sb.append("\n}");
        return sb.toString();
    }
}
