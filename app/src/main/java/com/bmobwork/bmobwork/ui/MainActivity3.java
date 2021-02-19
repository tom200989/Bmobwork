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
import com.bmobwork.bmobwork.helper.BmobUr;
import com.bmobwork.bmobwork.helper.LeanIM;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

public class MainActivity3 extends AppCompatActivity {

    public EditText et_objid;
    public androidx.recyclerview.widget.RecyclerView rcv_message;
    public ImageView iv_status;
    public EditText et_send;
    public Button bt_online;
    public Button bt_create_conv;
    public Button bt_offline;
    public Button bt_send;

    private List<Messagebean> messages = new ArrayList<>();

    private int flag = 0;
    private String localUser = flag == 0 ? "maqianli" : "weixin";
    private String targetUser = flag == 0 ? "weixin" : "maqianli";

    private BmobUr ur;
    private LeanIM leanIM;


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

        rcv_message.setLayoutManager(new LinearLayoutManager(this));
        rcv_message.setAdapter(new MessageAdapter(this, messages));

        bt_online.setOnClickListener(v -> click_online());
        bt_create_conv.setOnClickListener(v -> click_created_conv());
        bt_offline.setOnClickListener(v -> click_offline());
        bt_send.setOnClickListener(v -> click_send());

    }


    // 发送
    private void click_send() {
        if (leanIM != null) {
            String content = et_send.getText().toString().trim();
            if (TextUtils.isEmpty(content)) {
                content = targetUser + " 起床啦!";
            }
            leanIM.sendText(content);
        }
    }

    // 上线
    private void click_online() {
        // User user = new User();
        // user.setUsername(username);
        // user.setPassword("123456");
        //
        // ur = new BmobUr();
        // ur.setOnLoginSuccessListener(currentUser -> {
        //     toast("登录成功");
        //     LeanIM leanIM = LeanIM.getInstance();
        //     leanIM.online(localUser, targetUser);
        // });
        // ur.setOnLoginFailedListener(() -> toast("登录失败"));
        // ur.login(user);

        leanIM = LeanIM.getInstance();
        leanIM.setOnOnlineSuccessListener(() -> iv_status.setBackground(new ColorDrawable(Color.GREEN)));
        leanIM.online(localUser);
    }

    // 开启会话
    private void click_created_conv() {
        if (leanIM != null) {
            leanIM.setOnCreatedConversationSuccessListener(() -> iv_status.setBackground(new ColorDrawable(Color.YELLOW)));
            leanIM.createdConversation(targetUser);
        }
    }

    // 下线
    private void click_offline() {
        if (leanIM != null) {
            leanIM.setOnOfflineSuccessListener(() -> iv_status.setBackground(new ColorDrawable(Color.RED)));
            leanIM.offline();
        }
    }


    public void toast(String content) {
        Toast.makeText(this, content, Toast.LENGTH_LONG).show();
    }
}
