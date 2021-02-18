package com.bmobwork.bmobwork.impl;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.listener.MessageListHandler;

/*
 * Created by Administrator on 2021/2/18.
 * 该类为单个页面需求时的消息监听器
 */
public class IMPageHandler implements MessageListHandler {

    @Override
    public void onMessageReceive(List<MessageEvent> messageEvents) {
        /*
         * 此处只负责发送, eventbus的注册和注销由外部具体业务进行操作, 本SDK不干涉eventbus的生命周期
         */
        EventBus.getDefault().post(messageEvents);
    }
}
