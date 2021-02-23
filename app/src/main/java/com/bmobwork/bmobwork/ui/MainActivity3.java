package com.bmobwork.bmobwork.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bmobwork.bmobwork.R;
import com.bmobwork.bmobwork.demo.MessageAdapter;
import com.bmobwork.bmobwork.demo.Messagebean;
import com.bmobwork.bmobwork.demo.Printer;
import com.bmobwork.bmobwork.helper.LeanIM;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.messages.AVIMLocationMessage;
import cn.leancloud.im.v2.messages.AVIMTextMessage;
import cn.leancloud.types.AVGeoPoint;

public class MainActivity3 extends AppCompatActivity {

    public EditText et_objid;
    public RecyclerView rcv_message;
    public ImageView iv_status;
    public EditText et_send;
    public Button bt_online;
    public Button bt_create_conv;
    public Button bt_offline;
    public Button bt_send;
    public Button bt_get_convs;

    private List<Messagebean> messages = new ArrayList<>();

    private int flag = 0;
    private String localUser = flag == 0 ? "maqianli" : "xinmiao";
    private String targetUser = flag == 0 ? "xinmiao" : "maqianli";

    private LeanIM leanIM;
    private MessageAdapter adapter;
    private LinearLayoutManager lm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        et_objid = findViewById(R.id.et_objid);// user objectid
        rcv_message = findViewById(R.id.rcv_message);// 消息列表
        iv_status = findViewById(R.id.iv_status);// 上线状态
        et_send = findViewById(R.id.et_send);// 内容框
        bt_online = findViewById(R.id.bt_online);// 上线按钮
        bt_create_conv = findViewById(R.id.bt_created_conversation);// 开启会话按钮
        bt_offline = findViewById(R.id.bt_offline);// 下线按钮
        bt_send = findViewById(R.id.bt_send);// 发送按钮
        bt_get_convs = findViewById(R.id.bt_get_convs);// 获取本人会话按钮

        lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        adapter = new MessageAdapter(this, messages);
        rcv_message.setLayoutManager(lm);
        rcv_message.setAdapter(adapter);

        bt_online.setOnClickListener(v -> click_online());
        bt_create_conv.setOnClickListener(v -> click_created_conv());
        bt_offline.setOnClickListener(v -> click_offline());
        bt_send.setOnClickListener(v -> click_send());
        bt_get_convs.setOnClickListener(v -> click_get_convs());

        // 1.初始化IM
        leanIM = LeanIM.getInstance();
        // 2.接收监听
        LeanIM.setOnReceiverMessageListener(avimMessage -> {
            Messagebean messagebean = new Messagebean();
            AVIMMessage.AVIMMessageIOType messageIOType = avimMessage.getMessageIOType();
            Printer.i("IOType = " + messageIOType);
            messagebean.setReceiver(messageIOType == AVIMMessage.AVIMMessageIOType.AVIMMessageIOTypeIn);
            if (avimMessage instanceof AVIMTextMessage) {
                messagebean.setContent(((AVIMTextMessage) avimMessage).getText());
            } else if (avimMessage instanceof AVIMLocationMessage) {
                AVIMLocationMessage locationMessage = (AVIMLocationMessage) avimMessage;
                AVGeoPoint location = locationMessage.getLocation();
                double lat = location.getLatitude();
                double lng = location.getLongitude();
                String des = locationMessage.getText();
                messagebean.setContent(des + "(" + lat + "," + lng + ")");
            }
            messages.add(messagebean);
            // 主线程
            runOnUiThread(() -> {
                // 刷新UI
                adapter.notifys(messages);
                // 滑动到底部
                lm.scrollToPositionWithOffset(adapter.getItemCount() - 1, Integer.MIN_VALUE);
            });
        });
        // 3.上线
        leanIM.setOnOnlineSuccessListener(() -> {
            iv_status.setBackground(new ColorDrawable(Color.GREEN));
            // 4.查询会话
            leanIM.setOnQueryConversationSuccessListener(conversations -> {
                // 5.查询消息
                leanIM.setOnQueryMessageSuccessListener(avimMessages -> {
                    // 主线程
                    runOnUiThread(() -> {
                        Printer.v("avimMessages = " + avimMessages.size());
                        // 先清空
                        messages.clear();
                        // 转换成业务bean
                        for (AVIMMessage avimMessage : avimMessages) {
                            Messagebean messagebean = new Messagebean();
                            messagebean.setReceiver(avimMessage.getFrom().equals(targetUser));
                            if (avimMessage instanceof AVIMTextMessage) {
                                messagebean.setContent(((AVIMTextMessage) avimMessage).getText());
                            } else if (avimMessage instanceof AVIMLocationMessage) {
                                AVIMLocationMessage locationMessage = (AVIMLocationMessage) avimMessage;
                                AVGeoPoint location = locationMessage.getLocation();
                                double lat = location.getLatitude();
                                double lng = location.getLongitude();
                                String des = locationMessage.getText();
                                messagebean.setContent(des + "(" + lat + "," + lng + ")");
                            }
                            messages.add(messagebean);
                        }

                        // 刷新UI
                        adapter.notifys(messages);
                        // 滑动到底部
                        lm.scrollToPositionWithOffset(adapter.getItemCount() - 1, Integer.MIN_VALUE);
                    });

                });
                leanIM.queryMessage(conversations.get(0));
            });
            leanIM.queryConversation("60336c2195211d5b77c021d2");
        });
        leanIM.online(localUser);
    }


    // 发送
    private void click_send() {
        sendText();// 发送文本
        // sendLocation();// 发送定位
    }

    private void sendText() {
        String content = et_send.getText().toString().trim();
        if (TextUtils.isEmpty(content)) {
            content = targetUser + " 起床啦!";
        }

        String text = content;
        leanIM.setOnSendSuccessListener(() -> {
            Messagebean messagebean = new Messagebean();
            messagebean.setReceiver(false);
            messagebean.setContent(text);
            messages.add(messagebean);
            runOnUiThread(() -> {
                adapter.notifys(messages);
                lm.scrollToPositionWithOffset(adapter.getItemCount() - 1, Integer.MIN_VALUE);
            });
        });

        leanIM.sendText(content);
    }

    private void sendLocation() {
        double lat = 30.00009999;
        double lng = 63.00099999;
        String des = "蛋糕店的位置";
        leanIM.setOnSendSuccessListener(() -> {
            Messagebean messagebean = new Messagebean();
            messagebean.setReceiver(false);
            messagebean.setContent(des + "(" + lat + "," + lng + ")");
            messages.add(messagebean);
            runOnUiThread(() -> {
                adapter.notifys(messages);
                lm.scrollToPositionWithOffset(adapter.getItemCount() - 1, Integer.MIN_VALUE);
            });
        });

        leanIM.sendLocation(lat, lng, des);
    }

    // 上线
    private void click_online() {
        leanIM.setOnOnlineSuccessListener(() -> iv_status.setBackground(new ColorDrawable(Color.GREEN)));
        leanIM.online(localUser);
    }

    // 开启会话
    private void click_created_conv() {
        leanIM.setOnCreatedConversationSuccessListener(() -> iv_status.setBackground(new ColorDrawable(Color.YELLOW)));
        leanIM.createdConversation(targetUser);
    }

    // 下线
    private void click_offline() {
        leanIM.setOnOfflineSuccessListener(() -> iv_status.setBackground(new ColorDrawable(Color.RED)));
        leanIM.offline();
    }

    // 获取本人相关的所有会话
    private void click_get_convs() {
        leanIM.setOnQueryConversationSuccessListener(conversations -> {
            Printer.i("共有会话: " + conversations.size() + " 个");
            for (AVIMConversation conversation : conversations) {
                Printer.i("conversation ID = " + conversation.getConversationId());
            }
        });
        leanIM.queryConversation();
    }


    public void toast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_LONG).show();
    }
}
