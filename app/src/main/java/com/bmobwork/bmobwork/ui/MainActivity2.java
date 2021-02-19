package com.bmobwork.bmobwork.ui;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bmobwork.bmobwork.R;
import com.bmobwork.bmobwork.bean.User;
import com.bmobwork.bmobwork.demo.MessageAdapter;
import com.bmobwork.bmobwork.demo.Messagebean;
import com.bmobwork.bmobwork.demo.Printer;
import com.bmobwork.bmobwork.helper.BmobMI;
import com.bmobwork.bmobwork.helper.BmobUr;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.event.MessageEvent;
import cn.bmob.newim.event.OfflineMessageEvent;

public class MainActivity2 extends AppCompatActivity {

    public EditText et_objid;
    public androidx.recyclerview.widget.RecyclerView rcv_message;
    public ImageView iv_status;
    public EditText et_send;
    public Button bt_online;
    public Button bt_offline;
    public Button bt_send;

    private List<Messagebean> messages = new ArrayList<>();
    private BmobMI im;// 辅助器
    private BmobIMConversation conv_manager;// 会话体

    private boolean flag = false;
    private String username = flag ? "maqianli" : "weixin";// 自己
    private String user_id = flag ? "8632e0a0f4" : "42e58e0c19";// 对方
    private String other_name = flag ? "weixin" : "maqianli";// 对方名字
    private BmobUr ur;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        EventBus.getDefault().register(this);
        et_objid = findViewById(R.id.et_objid);// user objectid
        rcv_message = findViewById(R.id.rcv_message);// 消息列表
        iv_status = findViewById(R.id.iv_status);// 上线状态
        et_send = findViewById(R.id.et_send);// 内容框
        bt_online = findViewById(R.id.bt_online);// 上线按钮
        bt_offline = findViewById(R.id.bt_offline);// 下线按钮
        bt_send = findViewById(R.id.bt_send);// 发送按钮

        rcv_message.setLayoutManager(new LinearLayoutManager(this));
        rcv_message.setAdapter(new MessageAdapter(this, messages));

        bt_online.setOnClickListener(v -> click_online());
        bt_offline.setOnClickListener(v -> click_offline());
        bt_send.setOnClickListener(v -> click_send());
    }

    /* 接收到消息后 */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getMessages(MessageEvent messageEvent) {
        BmobIMUserInfo fromUserInfo = messageEvent.getFromUserInfo();
        String fromWho = fromUserInfo.getName();// 谁发送的 
        BmobIMMessage message = messageEvent.getMessage();
        String msgType = message.getMsgType();// 信息类型
        String content = message.getContent();// 信息内容

        StringBuilder builder = new StringBuilder();
        builder.append("--\n");
        builder.append("from who = ").append(fromWho).append("\n");
        builder.append("message type = ").append(msgType).append("\n");
        builder.append("content: ").append("\n").append(content).append("\n");

        Printer.i(builder.toString());
    }

    /* 接收到离线状态后 */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void getOffline(OfflineMessageEvent offlineMessageEvent) {
        StringBuilder builder = new StringBuilder();

        Map<String, List<MessageEvent>> eventMap = offlineMessageEvent.getEventMap();
        for (Map.Entry<String, List<MessageEvent>> entry : eventMap.entrySet()) {
            String key = entry.getKey();
            List<MessageEvent> messageEvents = entry.getValue();
            builder.append("--\n");
            builder.append("key = ").append(key).append("\n");
            for (MessageEvent messageEvent : messageEvents) {
                builder.append("message type = ").append(messageEvent.getMessage().getMsgType()).append("\n");
                builder.append("content: ").append("\n").append(messageEvent.getMessage().getContent()).append("\n");
            }
        }

        Printer.i(builder.toString());
    }

    // 发送
    private void click_send() {
        if (conv_manager == null) {
            BmobIMUserInfo info = new BmobIMUserInfo();
            info.setUserId(user_id);
            info.setName(other_name);

            if (im != null) {
                im.setOnStartConversationSuccessListener(conv -> {
                    conv_manager = conv;

                    String content = et_send.getText().toString().trim();
                    im.setOnSendTextMessageSuccessListener(message -> toast("发送成功"));
                    im.setOnSendTextMessageFailedListener(() -> toast("发送失败"));
                    im.sendText(content);
                });
                im.setOnStartConversationFailedListener(() -> toast("启动会话失败"));
                im.startConversation(info);
            }
        }

    }

    // 上线
    private void click_online() {
        User user = new User();
        user.setUsername(username);
        user.setPassword("123456");

        ur = new BmobUr();
        ur.setOnLoginSuccessListener(user1 -> {
            toast("登录成功");
            im = BmobMI.getInstance();
            im.setOnConnectSuccessListener(uid -> {
                toast("上线成功: uid = " + uid);
                iv_status.setBackground(new ColorDrawable(Color.GREEN));
            });
            im.setOnConnectFailedListener(() -> toast("上线失败"));
            im.connect(ur.getCurrentUser());
        });
        ur.setOnLoginFailedListener(() -> toast("登录失败"));
        ur.login(user);
    }

    // 下线
    private void click_offline() {
        if (im != null) {
            im.disconnect();
            ur.logout();
            toast("下线成功");
            iv_status.setBackground(new ColorDrawable(Color.RED));
        }
    }


    public void toast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
