package com.bmobwork.bmobwork.demo;

import java.io.Serializable;

/*
 * Created by Administrator on 2021/2/19.
 */
public class Messagebean implements Serializable {
    public boolean isReceiver;
    public String content;

    public boolean isReceiver() {
        return isReceiver;
    }

    public Messagebean setReceiver(boolean receiver) {
        isReceiver = receiver;
        return this;
    }

    public String getContent() {
        return content;
    }

    public Messagebean setContent(String content) {
        this.content = content;
        return this;
    }
}
