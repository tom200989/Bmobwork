package com.bmobwork.bmobwork.helper;

import android.os.Handler;
import android.text.TextUtils;

import com.bmobwork.bmobwork.demo.Printer;

import java.util.Collections;
import java.util.List;

import cn.leancloud.AVFile;
import cn.leancloud.im.v2.AVIMClient;
import cn.leancloud.im.v2.AVIMClientEventHandler;
import cn.leancloud.im.v2.AVIMConversation;
import cn.leancloud.im.v2.AVIMConversationEventHandler;
import cn.leancloud.im.v2.AVIMConversationsQuery;
import cn.leancloud.im.v2.AVIMException;
import cn.leancloud.im.v2.AVIMMessage;
import cn.leancloud.im.v2.AVIMMessageHandler;
import cn.leancloud.im.v2.AVIMMessageManager;
import cn.leancloud.im.v2.annotation.AVIMMessageType;
import cn.leancloud.im.v2.callback.AVIMClientCallback;
import cn.leancloud.im.v2.callback.AVIMConversationCallback;
import cn.leancloud.im.v2.callback.AVIMConversationCreatedCallback;
import cn.leancloud.im.v2.callback.AVIMConversationQueryCallback;
import cn.leancloud.im.v2.callback.AVIMMessagesQueryCallback;
import cn.leancloud.im.v2.messages.AVIMAudioMessage;
import cn.leancloud.im.v2.messages.AVIMFileMessage;
import cn.leancloud.im.v2.messages.AVIMImageMessage;
import cn.leancloud.im.v2.messages.AVIMLocationMessage;
import cn.leancloud.im.v2.messages.AVIMTextMessage;
import cn.leancloud.types.AVGeoPoint;

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
    private int QUERY_LIMIT = 99;// 一次查询的限制条数
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
                    new Handler().postDelayed(() -> online(localUser), 1000);

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
                        } else if (conversation_count < RETRY) {
                            conversation_count++;
                            Printer.w("正在重试第 " + conversation_count + " 次创建");
                            new Handler().postDelayed(() -> createdConversation(targetUser), 1000);
                        } else {
                            conversation_count = 0;
                            CreatedConversationFailedNext();
                            BmobError("创建会话失败", e);
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
                            new Handler().postDelayed(() -> offline(), 1000);
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
     * 设置全部消息已读
     */
    public void setHadRead() {
        if (conversation != null) {
            conversation.read();
        }
    }

    /**
     * 获取未读消息数
     *
     * @return 如返回-1, 表示网络有问题
     */
    public int getUnreadCount() {
        int unread = -1;
        if (conversation != null) {
            unread = conversation.getUnreadMessagesCount();
        }
        return unread;
    }

    /**
     * 发送文本
     *
     * @param content 文本
     */
    public void sendText(String content) {
        // 通讯条件判断
        if (!checkCondition()) return;
        // 封装
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
                } else if (send_count < RETRY) {
                    send_count++;
                    Printer.w("正在重试第 " + send_count + " 次发送");
                    new Handler().postDelayed(() -> sendText(content), 1000);
                } else {
                    send_count = 0;
                    SendFailedNext();
                    BmobError("发送失败", e);
                }
            }
        });
    }

    /**
     * 发送图像\音频\文件
     *
     * @param messageType 消息类型(图像\音频\文件)
     * @param path        本地路径
     * @param des         消息描述
     */
    public void send_img_audio_file(AVIMMessageType messageType, String path, String des) {
        try {
            // 路径、名字
            String name = path.substring(path.lastIndexOf("/") + 1);
            AVFile file = AVFile.withAbsoluteLocalPath(name, path);
            // 创建多媒体消息
            AVIMMessage message;
            if (messageType.type() == AVIMMessageType.IMAGE_MESSAGE_TYPE) {
                AVIMImageMessage imageMessage = new AVIMImageMessage(file);
                imageMessage.setText(des);
                message = imageMessage;
            } else if (messageType.type() == AVIMMessageType.AUDIO_MESSAGE_TYPE) {
                AVIMAudioMessage audioMessage = new AVIMAudioMessage(file);
                audioMessage.setText(des);
                message = audioMessage;
            } else if (messageType.type() == AVIMMessageType.FILE_MESSAGE_TYPE) {
                AVIMFileMessage fileMessage = new AVIMFileMessage(file);
                fileMessage.setText(des);
                message = fileMessage;
            } else {
                Printer.e("无法识别的消息类型, 请留意");
                AVIMTextMessage textMessage = new AVIMTextMessage();
                textMessage.setText(des);
                message = textMessage;
            }
            // 发送
            conversation.sendMessage(message, new AVIMConversationCallback() {
                @Override
                public void done(AVIMException e) {
                    if (e == null) {
                        Printer.i("发送成功");
                        send_count = 0;
                        SendSuccessNext();
                    } else if (send_count < RETRY) {
                        send_count++;
                        Printer.w("正在重试第 " + send_count + " 次发送");
                        new Handler().postDelayed(() -> send_img_audio_file(messageType, path, des), 1000);
                    } else {
                        send_count = 0;
                        SendFailedNext();
                        BmobError("发送失败", e);
                    }
                }
            });
        } catch (Exception e) {
            SendFailedNext();
            BmobError("发送失败", e);
        }
    }

    /**
     * 发送定位
     *
     * @param lat 经度
     * @param lng 纬度
     * @param des 描述
     */
    public void sendLocation(double lat, double lng, String des) {
        AVIMLocationMessage message = new AVIMLocationMessage();
        message.setLocation(new AVGeoPoint(lat, lng));
        message.setText(des);
        conversation.sendMessage(message, new AVIMConversationCallback() {
            @Override
            public void done(AVIMException e) {
                if (e == null) {
                    Printer.i("发送成功");
                    send_count = 0;
                    SendSuccessNext();
                } else if (send_count < RETRY) {
                    send_count++;
                    Printer.w("正在重试第 " + send_count + " 次发送");
                    new Handler().postDelayed(() -> sendLocation(lat, lng, des), 1000);
                } else {
                    send_count = 0;
                    SendFailedNext();
                    BmobError("发送失败", e);
                }
            }
        });
    }

    /**
     * 检查条件
     *
     * @return T:符合体检
     */
    private boolean checkCondition() {
        // 非空判断
        if (localClient == null) {
            Printer.e("本地尚未上线, 目前正在执行上线操作, 请下一步开启对方会话");
            online(localUser);
            return false;
        }
        if (conversation == null) {
            Printer.e("会话尚未开启, 请先调用 createdConversation() 向对方开启会话");
            if (!TextUtils.isEmpty(targetUser)) {// 对方ID必需有业务层传入 - 如果没有传入, 是不知道要和谁进性通讯的
                createdConversation(targetUser);
            } else {
                Printer.e("targetUser 没有被业务指定, 请业务层调用 createdConversation() 传入一个对方ID");
            }
            return false;
        }
        return true;
    }

    /**
     * 查询指定会话 (根据会话ID)
     *
     * @param objectId 会话ID
     * @apiNote 业务点击某个会话时调用该查询
     */
    public void queryConversation(String objectId) {
        AVIMConversationsQuery query = localClient.getConversationsQuery();
        query.whereEqualTo("objectId", objectId);
        query.findInBackground(new AVIMConversationQueryCallback() {
            @Override
            public void done(List<AVIMConversation> convs, AVIMException e) {
                if (e == null) {
                    if (convs != null && !convs.isEmpty()) {
                        AVIMConversation targetConv = convs.get(0);
                        Printer.i("查询指定会话成功, 会话ID = " + targetConv.getConversationId());
                        QueryConversationSuccessNext(targetConv);
                    } else {
                        Printer.w("查询指定会话完毕, 没有符合条件的会话");
                        NoMatchConversationNext();
                    }
                } else {
                    QueryConversationFailedNext();
                    BmobError("查询失败", e);
                }
            }
        });
    }

    /**
     * 查询消息 (根据指定会话)
     *
     * @param conversation 指定会话
     * @apiNote 进入具体的聊天界面后调用该查询
     */
    public void queryMessage(AVIMConversation conversation) {
        conversation.queryMessages(QUERY_LIMIT, new AVIMMessagesQueryCallback() {
            @Override
            public void done(List<AVIMMessage> messages, AVIMException e) {
                if (e == null) {
                    // 成功获取最新 n 条消息记录
                    Printer.i("查询消息成功, 消息条数 = " + messages.size());
                    QueryMessageSuccessNext(messages);
                } else {
                    QueryMessageFailedNext();
                    BmobError("查询消息失败", e);
                }
            }
        });
    }

    /**
     * 翻页查询
     *
     * @param conversation           会话
     * @param last_message_id        最近一条消息ID
     * @param last_message_timeStamp 最近一条消息时间戳
     */
    public void queryMessage(AVIMConversation conversation, String last_message_id, long last_message_timeStamp) {
        conversation.queryMessages(last_message_id, last_message_timeStamp, QUERY_LIMIT, new AVIMMessagesQueryCallback() {
            @Override
            public void done(List<AVIMMessage> messages, AVIMException e) {
                if (e == null) {
                    Printer.i("翻页查询消息成功, 消息条数 = " + messages.size());
                    QueryMessageSuccessNext(messages);
                } else {
                    QueryMessageFailedNext();
                    BmobError("翻页查询消息失败", e);
                }
            }
        });
    }

    /* -------------------------------------------- handler -------------------------------------------- */

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
            ReceiverMessageNext(message);// 回调
            // TODO: 2021/2/20  需要做一个工具类对消息分类转换
        }
    }

    /**
     * 离线状态
     */
    public static class ClientHandler extends AVIMClientEventHandler {

        @Override
        public void onConnectionPaused(AVIMClient client) {
            Printer.e("网络被切断");
            ConnectPauseNext();
        }

        @Override
        public void onConnectionResume(AVIMClient client) {
            Printer.v("网络连接中");
            ConnectResumeNext();
        }

        @Override
        public void onClientOffline(AVIMClient client, int code) {
            Printer.e("用户离线");
            ClientOfflineNext();
        }
    }

    /* -------------------------------------------- impl -------------------------------------------- */

    // ---------------- 监听器 [ConnectPause] ----------------
    private static OnConnectPauseListener onConnectPauseListeners;

    public static interface OnConnectPauseListener {
        void ConnectPause();
    }

    public static void setOnConnectPauseListener(OnConnectPauseListener onConnectPauseListener) {
        onConnectPauseListeners = onConnectPauseListener;
    }

    private static void ConnectPauseNext() {
        if (onConnectPauseListeners != null) {
            onConnectPauseListeners.ConnectPause();
        }
    }

    // ---------------- 监听器 [ConnectResume] ----------------
    private static OnConnectResumeListener onConnectResumeListeners;

    public static interface OnConnectResumeListener {
        void ConnectResume();
    }

    public static void setOnConnectResumeListener(OnConnectResumeListener onConnectResumeListener) {
        onConnectResumeListeners = onConnectResumeListener;
    }

    private static void ConnectResumeNext() {
        if (onConnectResumeListeners != null) {
            onConnectResumeListeners.ConnectResume();
        }
    }

    // ---------------- 监听器 [ClientOffline] ----------------
    private static OnClientOfflineListener onClientOfflineListeners;

    public static interface OnClientOfflineListener {
        void ClientOffline();
    }

    public static void setOnClientOfflineListener(OnClientOfflineListener onClientOfflineListener) {
        onClientOfflineListeners = onClientOfflineListener;
    }

    private static void ClientOfflineNext() {
        if (onClientOfflineListeners != null) {
            onClientOfflineListeners.ClientOffline();
        }
    }

    // ---------------- 监听器 [ReceiverMessage] ----------------
    private static OnReceiverMessageListener onReceiverMessageListeners;

    public static interface OnReceiverMessageListener {
        void ReceiverMessage(AVIMMessage message);
    }

    public static void setOnReceiverMessageListener(OnReceiverMessageListener onReceiverMessageListener) {
        onReceiverMessageListeners = onReceiverMessageListener;
    }

    private static void ReceiverMessageNext(AVIMMessage message) {
        if (onReceiverMessageListeners != null) {
            onReceiverMessageListeners.ReceiverMessage(message);
        }
    }

    // ---------------- 监听器 [QueryMessageSuccess] ----------------
    private OnQueryMessageSuccessListener onQueryMessageSuccessListener;

    public interface OnQueryMessageSuccessListener {
        void QueryMessageSuccess(List<AVIMMessage> messages);
    }

    public void setOnQueryMessageSuccessListener(OnQueryMessageSuccessListener onQueryMessageSuccessListener) {
        this.onQueryMessageSuccessListener = onQueryMessageSuccessListener;
    }

    private void QueryMessageSuccessNext(List<AVIMMessage> messages) {
        if (onQueryMessageSuccessListener != null) {
            onQueryMessageSuccessListener.QueryMessageSuccess(messages);
        }
    }


    // ---------------- 监听器 [QueryMessageFailed] ----------------
    private OnQueryMessageFailedListener onQueryMessageFailedListener;

    public interface OnQueryMessageFailedListener {
        void QueryMessageFailed();
    }

    public void setOnQueryMessageFailedListener(OnQueryMessageFailedListener onQueryMessageFailedListener) {
        this.onQueryMessageFailedListener = onQueryMessageFailedListener;
    }

    private void QueryMessageFailedNext() {
        if (onQueryMessageFailedListener != null) {
            onQueryMessageFailedListener.QueryMessageFailed();
        }
    }

    // ---------------- 监听器 [QueryConversationSuccess] ----------------
    private OnQueryConversationSuccessListener onQueryConversationSuccessListener;

    public interface OnQueryConversationSuccessListener {
        void QueryConversationSuccess(AVIMConversation conversation);
    }

    public void setOnQueryConversationSuccessListener(OnQueryConversationSuccessListener onQueryConversationSuccessListener) {
        this.onQueryConversationSuccessListener = onQueryConversationSuccessListener;
    }

    private void QueryConversationSuccessNext(AVIMConversation conversation) {
        if (onQueryConversationSuccessListener != null) {
            onQueryConversationSuccessListener.QueryConversationSuccess(conversation);
        }
    }

    // ---------------- 监听器 [QueryConversationFailed] ----------------
    private OnQueryConversationFailedListener onQueryConversationFailedListener;

    public interface OnQueryConversationFailedListener {
        void QueryConversationFailed();
    }

    public void setOnQueryConversationFailedListener(OnQueryConversationFailedListener onQueryConversationFailedListener) {
        this.onQueryConversationFailedListener = onQueryConversationFailedListener;
    }

    private void QueryConversationFailedNext() {
        if (onQueryConversationFailedListener != null) {
            onQueryConversationFailedListener.QueryConversationFailed();
        }
    }

    // ---------------- 监听器 [NoMatchConversation] ----------------
    private OnNoMatchConversationListener onNoMatchConversationListener;

    public interface OnNoMatchConversationListener {
        void NoMatchConversation();
    }

    public void setOnNoMatchConversationListener(OnNoMatchConversationListener onNoMatchConversationListener) {
        this.onNoMatchConversationListener = onNoMatchConversationListener;
    }

    private void NoMatchConversationNext() {
        if (onNoMatchConversationListener != null) {
            onNoMatchConversationListener.NoMatchConversation();
        }
    }

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
