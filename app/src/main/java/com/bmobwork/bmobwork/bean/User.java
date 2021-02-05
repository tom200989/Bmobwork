package com.bmobwork.bmobwork.bean;

import cn.bmob.v3.BmobUser;

/*
 * Created by Administrator on 2021/02/003.
 * 不同的项目, 该类需要优先定制, 因为Bmob不支持泛型
 */
public class User extends BmobUser {

    public float salary;
    public int age;

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("Account{");
        sb.append("\n").append("\t").append("salary =").append(salary);
        sb.append("\n").append("\t").append("age =").append(age);
        sb.append("\n}");
        return sb.toString();
    }

    public float getSalary() {
        return salary;
    }

    public User setSalary(float salary) {
        this.salary = salary;
        return this;
    }

    public int getAge() {
        return age;
    }

    public User setAge(int age) {
        this.age = age;
        return this;
    }
}
