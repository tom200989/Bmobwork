package com.bmobwork.bmobwork.helper;

import android.text.TextUtils;

import com.bmobwork.bmobwork.demo.Printer;

import java.util.Collections;
import java.util.List;

import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMClientEventHandler;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMConversationEventHandler;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageHandler;
import cn.leancloud.im.v2.AVIMMessageManager;
import cn.leancloud.im.v2.callback.AVIMClientCallback;
import cn.leancloud.im.v2.callback.AVIMConversationCallback;
import cn.leancloud.im.v2.callback.AVIMConversationCreatedCallback;
import cn.leancloud.im.v2.messages.AVIMTextMessage;

/*
 * Created by Administrator on 2021/2/19.
 */
public class LeanIM extends BmobBase {

    public static LeanIM im;// 单例
    private AVIMClient localClient;// 发送者
    private String localUser;
    private String targetUser;
    private AVIMConversation conversation;// 会话

    private int RETRY = 5;// 重试次数
    private int client_count = 0;// 上线计数器 (用于失败时尝试)
    private int conversation_count = 0;// 开启会话计数器 (用于失败时尝试)
    private int send_count = 0;// 发送计数器 (用于失败时尝试)
    private int offline_count = 0;// 下线计数器 (用于失败时尝试)

    private LeanIM() {
        registerHandler();// 注册监听器
    }

    public static synchronized LeanIM getInstance() {
        // 创建单例
        if (im == null) {
            synchronized (LeanIM.class) {
                if (im == null) {
                    im = new LeanIM();
                }
            }
        }
        // 注册监听器 - 每次调用都重新注册 (为了在弱网情况下确保长连)
        registerHandler();
        return im;
    }

    /**
     * 注册监听器
     */
    public static void registerHandler() {
        // 会话处理器
        AVIMMessageManager.setConversationEventHandler(new ConversationHandler());
        // 消息处理器
        AVIMMessageManager.registerDefaultMessageHandler(new MessageIMHandler());
        // 离线处理器
        AVIMClient.setClientEventHandler(new ClientHandler());
    }


    /**
     * 上线
     *
     * @param localUser 本地用户
     */
    public void online(String localUser) {
        this.localUser = localUser;
        localClient = AVIMClient.getInstance(localUser);
        localClient.open(new AVIMClientCallback() {
            @Override
            public void done(AVIMClient client, AVIMException e) {
                if (e == null) {
                    Printer.i("打开了连接");
                    client_count = 0;// 复位
                    OnlineSuccessNext();
                } else if (client_count < RETRY) {
                    client_count++;
                    Printer.w("正在重试第 " + client_count + " 次上线");
                    online(localUser);
                } else {
                    client_count = 0;// 复位
                    OnlineFailedNext();
                    BmobError("上线失败", e);
                }
            }
        });

    }

    /**
     * 创建会话
     *
     * @param targetUser 对方用户
     */
    public void createdConversation(String targetUser) {

        // 非空判断
        if (localClient == null) {
            online(localUser);
            return;
        }
        this.targetUser = targetUser;
        // 创建
        localClient.createConversation(Collections.singletonList(targetUser),// 对方用户
                localUser + " and " + targetUser,// 会话标题
                null,// 其它枢性
                false,// 是否为暂态会话
                true,// 是否返回已有会话
                new AVIMConversationCreatedCallback() {
                    @Override
                    public void done(AVIMConversation conversation, AVIMException e) {
                        if (e == null) {
                            // 创建成功
                            Printer.i("创建会话成功");
                            conversation_count = 0;
                            LeanIM.this.conversation = conversation;
                            CreatedConversationSuccessNext();
                        } else {
                            if (conversation_count < RETRY) {
                                conversation_count++;
                                Printer.w("正在重试第 " + conversation_count + " 次创建");
                                createdConversation(targetUser);
                            } else {
                                conversation_count = 0;
                                CreatedConversationFailedNext();
                                BmobError("创建会话失败", e);
                            }
                        }
                    }
                });
    }

    /**
     * 下线
     */
    public void offline() {
        if (localClient != null) {
            localClient.close(new AVIMClientCallback() {
                @Override
                public void done(AVIMClient client, AVIMException e) {
                    if (e == null) {
                        Printer.i("下线成功");
                        offline_count = 0;
                        OfflineSuccessNext();
                    } else {
                        if (offline_count < 5) {
                            offline_count++;
                            Printer.w("正在重试第 " + offline_count + " 次下线");
                            offline();
                        } else {
                            offline_count = 0;
                            OfflineFailedNext();
                            BmobError("下线失败", e);
                        }
                    }
                }
            });
        }
    }

    /**
     * 发送文本
     *
     * @param content 文本
     */
    public void sendText(String content) {
        // 非空判断
        if (localClient == null) {
            Printer.e("本地尚未上线, 目前正在执行上线操作, 请下一步开启对方会话");
            online(localUser);
            return;
        }
        if (conversation == null) {
            Printer.e("会话尚未开启, 请先调用 createdConversation() 向对方开启会话");
            if (!TextUtils.isEmpty(targetUser)) {// 对方ID必需有业务层传入 - 如果没有传入, 是不知道要和谁进性通讯的
                createdConversation(targetUser);
            } else {
                Printer.e("targetUser 没有被业务指定, 请业务层调用 createdConversation() 传入一个对方ID");
            }
            return;
        }
        AVIMTextMessage msg = new AVIMTextMessage();
        msg.setText(content);
        // 发送消息
        conversation.sendMessage(msg, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
                if (e == null) {
                    Printer.i("发送成功");
                    send_count = 0;
                    SendSuccessNext();
                } else if (send_count < 5) {
                    send_count++;
                    Printer.w("正在重试第 " + send_count + " 次发送");
                    sendText(content);
                } else {
                    send_count = 0;
                    SendFailedNext();
                    BmobError("发送失败", e);
                }
            }
        });
    }

    /**
     * 会话处理器
     */
    public static class ConversationHandler extends AVIMConversationEventHandler {
        @Override
        public void onMemberLeft(AVIMClient client, AVIMConversation conversation, List<String> members, String kickedBy) {
            Printer.v("成员 [" + client.getClientId() + "] 离线了");
        }

        @Override
        public void onMemberJoined(AVIMClient client, AVIMConversation conversation, List<String> members, String invitedBy) {
            StringBuilder builder = new StringBuilder();
            builder.append("--\n");
            for (String member : members) {
                builder.append(member).append("加入了!").append("\n");
            }
            Printer.v(builder.toString());
        }

        @Override
        public void onKicked(AVIMClient client, AVIMConversation conversation, String kickedBy) {

        }

        /*
         * 实现本方法来处理当前用户被邀请到某个聊天对话事件
         *
         * @param client       本地用户
         * @param conversation 被邀请的聊天对话
         * @since 3.0
         */
        @Override
        public void onInvited(AVIMClient client, AVIMConversation conversation, String invitedBy) {
            // 当前 clientId（Jerry）被邀请到对话，执行此处逻辑
            Printer.v("被 [" + invitedBy + "] 邀请");
        }
    }


    /**
     * 消息处理器
     */
    public static class MessageIMHandler extends AVIMMessageHandler {

        /*
         * 重载此方法来处理接收消息
         *
         * @param message      接受到的消息
         * @param conversation 当前会话
         * @param client       本地用户
         */
        @Override
        public void onMessage(AVIMMessage message, AVIMConversation conversation, AVIMClient client) {
            if (message instanceof AVIMTextMessage) {
                // 接收消息 - Jerry，起床了
                Printer.i(((AVIMTextMessage) message).getText());
                // TODO: 2021/2/19  回调
            }
        }
    }

    /**
     * 离线状态
     */
    public static class ClientHandler extends AVIMClientEventHandler {

        @Override
        public void onConnectionPaused(AVIMClient client) {
            Printer.e("网络被切断");
        }

        @Override
        public void onConnectionResume(AVIMClient client) {
            Printer.v("网络重连中....");
        }

        @Override
        public void onClientOffline(AVIMClient client, int code) {
            Printer.e("用户离线");
        }
    }

    /* -------------------------------------------- impl -------------------------------------------- */

    // ---------------- 监听器 [OnlineSuccess] ----------------
    private OnOnlineSuccessListener onOnlineSuccessListener;

    public interface OnOnlineSuccessListener {
        void OnlineSuccess();
    }

    public void setOnOnlineSuccessListener(OnOnlineSuccessListener onOnlineSuccessListener) {
        this.onOnlineSuccessListener = onOnlineSuccessListener;
    }

    private void OnlineSuccessNext() {
        if (onOnlineSuccessListener != null) {
            onOnlineSuccessListener.OnlineSuccess();
        }
    }

    // ---------------- 监听器 [OnlineFailed] ----------------
    private OnOnlineFailedListener onOnlineFailedListener;

    public interface OnOnlineFailedListener {
        void OnlineFailed();
    }

    public void setOnOnlineFailedListener(OnOnlineFailedListener onOnlineFailedListener) {
        this.onOnlineFailedListener = onOnlineFailedListener;
    }

    private void OnlineFailedNext() {
        if (onOnlineFailedListener != null) {
            onOnlineFailedListener.OnlineFailed();
        }
    }

    // ---------------- 监听器 [CreatedConversationSuccess] ----------------
    private OnCreatedConversationSuccessListener onCreatedConversationSuccessListener;

    public interface OnCreatedConversationSuccessListener {
        void CreatedConversationSuccess();
    }

    public void setOnCreatedConversationSuccessListener(OnCreatedConversationSuccessListener onCreatedConversationSuccessListener) {
        this.onCreatedConversationSuccessListener = onCreatedConversationSuccessListener;
    }

    private void CreatedConversationSuccessNext() {
        if (onCreatedConversationSuccessListener != null) {
            onCreatedConversationSuccessListener.CreatedConversationSuccess();
        }
    }

    // ---------------- 监听器 [CreatedConversationFailed] ----------------
    private OnCreatedConversationFailedListener onCreatedConversationFailedListener;

    public interface OnCreatedConversationFailedListener {
        void CreatedConversationFailed();
    }

    public void setOnCreatedConversationFailedListener(OnCreatedConversationFailedListener onCreatedConversationFailedListener) {
        this.onCreatedConversationFailedListener = onCreatedConversationFailedListener;
    }

    private void CreatedConversationFailedNext() {
        if (onCreatedConversationFailedListener != null) {
            onCreatedConversationFailedListener.CreatedConversationFailed();
        }
    }

    // ---------------- 监听器 [SendSuccess] ----------------
    private OnSendSuccessListener onSendSuccessListener;

    public interface OnSendSuccessListener {
        void SendSuccess();
    }

    public void setOnSendSuccessListener(OnSendSuccessListener onSendSuccessListener) {
        this.onSendSuccessListener = onSendSuccessListener;
    }

    private void SendSuccessNext() {
        if (onSendSuccessListener != null) {
            onSendSuccessListener.SendSuccess();
        }
    }

    // ---------------- 监听器 [SendFailed] ----------------
    private OnSendFailedListener onSendFailedListener;

    public interface OnSendFailedListener {
        void SendFailed();
    }

    public void setOnSendFailedListener(OnSendFailedListener onSendFailedListener) {
        this.onSendFailedListener = onSendFailedListener;
    }

    private void SendFailedNext() {
        if (onSendFailedListener != null) {
            onSendFailedListener.SendFailed();
        }
    }

    // ---------------- 监听器 [OfflineSuccess] ----------------
    private OnOfflineSuccessListener onOfflineSuccessListener;

    public interface OnOfflineSuccessListener {
        void OfflineSuccess();
    }

    public void setOnOfflineSuccessListener(OnOfflineSuccessListener onOfflineSuccessListener) {
        this.onOfflineSuccessListener = onOfflineSuccessListener;
    }

    private void OfflineSuccessNext() {
        if (onOfflineSuccessListener != null) {
            onOfflineSuccessListener.OfflineSuccess();
        }
    }

    // ---------------- 监听器 [OfflineFailed] ----------------
    private OnOfflineFailedListener onOfflineFailedListener;

    public interface OnOfflineFailedListener {
        void OfflineFailed();
    }

    public void setOnOfflineFailedListener(OnOfflineFailedListener onOfflineFailedListener) {
        this.onOfflineFailedListener = onOfflineFailedListener;
    }

    private void OfflineFailedNext() {
        if (onOfflineFailedListener != null) {
            onOfflineFailedListener.OfflineFailed();
        }
    }

}
