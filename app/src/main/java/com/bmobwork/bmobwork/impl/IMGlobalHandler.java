package com.bmobwork.bmobwork.Impl;

import org.greenrobot.eventbus.EventBus;

import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;
import cn.bmob.newim.listener.BmobIMMessageHandler;

/*
 * Created by Administrator on 2021/2/16.
 * 该类为全局监听器 (该类暂时不用, 已经由LeanIM取代工作)
 * (SDK的内部已经采用Eventbus来发送, 因此该监听器可不使用, 直接在外部订阅即可, 如下:)
 *
 * @Subscribe
 * public void getMessage(MessageEvent event){
 *   // 处理聊天消息
 * }
 *
 * @Subscribe
 * public void getOffline(OfflineMessageEvent event){
 *   // 处理离线消息
 * }
 */
public class IMGlobalHandler extends BmobIMMessageHandler {
    @Override
    public void onMessageReceive(MessageEvent messageEvent) {
        super.onMessageReceive(messageEvent);
        /*
         * 此处只负责发送, eventbus的注册和注销由外部具体业务进行操作, 本SDK不干涉eventbus的生命周期
         */
        EventBus.getDefault().post(messageEvent);
    }

    @Override
    public void onOfflineReceive(OfflineMessageEvent offlineMessageEvent) {
        super.onOfflineReceive(offlineMessageEvent);
        /*
         * 此处只负责发送, eventbus的注册和注销由外部具体业务进行操作, 本SDK不干涉eventbus的生命周期
         */
        EventBus.getDefault().post(offlineMessageEvent);
    }
}
