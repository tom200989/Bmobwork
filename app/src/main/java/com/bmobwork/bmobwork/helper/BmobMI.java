package com.bmobwork.bmobwork.helper;

import android.text.TextUtils;

import com.bmobwork.bmobwork.config.Cons;
import com.bmobwork.bmobwork.log.Legg;

import java.util.List;

import cn.bmob.newim.BmobIM;
import cn.bmob.newim.bean.BmobIMAudioMessage;
import cn.bmob.newim.bean.BmobIMConversation;
import cn.bmob.newim.bean.BmobIMFileMessage;
import cn.bmob.newim.bean.BmobIMImageMessage;
import cn.bmob.newim.bean.BmobIMLocationMessage;
import cn.bmob.newim.bean.BmobIMMessage;
import cn.bmob.newim.bean.BmobIMTextMessage;
import cn.bmob.newim.bean.BmobIMUserInfo;
import cn.bmob.newim.bean.BmobIMVideoMessage;
import cn.bmob.newim.core.BmobIMClient;
import cn.bmob.newim.core.ConnectionStatus;
import cn.bmob.newim.listener.ConnectListener;
import cn.bmob.newim.listener.ConnectStatusChangeListener;
import cn.bmob.newim.listener.ConversationListener;
import cn.bmob.newim.listener.MessageListener;
import cn.bmob.newim.listener.MessageSendListener;
import cn.bmob.newim.listener.MessagesQueryListener;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;

/*
 * Created by Administrator on 2021/2/16.
 * 该类为即时通讯
 * // TODO: 2021/2/16 该类需要测试
 */
public class BmobMI extends BmobBase {
    

    public static BmobMI bmobMi;
    public BmobIMConversation convManager;// 会话体

    private BmobMI() {

    }

    public synchronized static BmobMI getInstance() {
        // 判断是否已登录
        if (!BmobUser.isLogin()) {
            Legg.e(Cons.TAG, "未登录 - 不允许通讯操作");
            return bmobMi;
        }
        // 已经登录 - 创建单例
        if (bmobMi == null) {
            synchronized (BmobIM.class) {
                if (bmobMi == null) {
                    bmobMi = new BmobMI();
                }
            }

        }
        return bmobMi;
    }

    /* -------------------------------------------- API -------------------------------------------- */

    /**
     * 连接
     *
     * @param user 用户
     */
    public void connect(BmobUser user) {
        // 非空判断
        if (user == null) {
            printErr("BmobMI:connect()-> 未指定user");
            return;
        }

        String objectId = user.getObjectId();
        if (TextUtils.isEmpty(objectId)) {
            printErr("BmobMI:connect()-> 未指定oid");
            return;
        }

        if (!BmobUser.isLogin()) {
            printErr("BmobMI:connect()-> 未登录");
            return;
        }

        // 连接
        BmobIM.connect(objectId, new ConnectListener() {
            @Override
            public void done(String uid, BmobException e) {
                if (e == null) {
                    printInfo("BmobMI:connect()-> 连接成功");
                    ConnectSuccessNext(uid);
                } else {
                    ConnectFailedNext();
                    BmobError("BmobMI:connect()-> 连接失败", e);
                }
            }
        });
    }

    /**
     * 切断连接
     */
    public void disconnect() {
        BmobIM.getInstance().disConnect();
    }

    /**
     * 获取长连接状态
     */
    public void getConnectState() {
        BmobIM.getInstance().setOnConnectStatusChangeListener(new ConnectStatusChangeListener() {
            @Override
            public void onChange(ConnectionStatus status) {
                printVerb("BmobMI:getConnectState()-> 当前连接状态: " + status.getCode() + "; " + status.getMsg());
                GetConnectStatuNext(status);
            }
        });
    }

    /**
     * 更新用户信息
     *
     * @param info 用户信息
     */
    public void updateUser(BmobIMUserInfo info) {
        BmobIM.getInstance().updateUserInfo(info);
    }

    /**
     * 获取本地用户信息
     *
     * @param uid 唯一标记
     */
    public BmobIMUserInfo getUserInfo(String uid) {
        return BmobIM.getInstance().getUserInfo(uid);
    }

    /**
     * 获取暂态会话
     *
     * @param info 对方的信息
     */
    public void startTempConversation(BmobIMUserInfo info) {
        BmobIM.getInstance().startPrivateConversation(info, true, new ConversationListener() {
            @Override
            public void done(BmobIMConversation bmobIMConversation, BmobException e) {
                if (e == null) {
                    printInfo("BmobMI:startTempConversation()-> 创建会话成功");
                    /*
                     * 该 bmobIMConversation 对象有可能不能直接用于消息发送
                     * 而需要调用以下方法才可以 BmobIMConversation.obtain(BmobIMClient.getInstance(), conv);
                     *
                     * @conv: 为首次创建会话时的会话对象
                     */
                    convManager = obtainConvManager(bmobIMConversation);
                    StartConversationSuccessNext(convManager);
                } else {
                    StartConversationFailedNext();
                    BmobError("BmobMI:startTempConversation()-> 创建会话失败", e);
                }
            }
        });
    }

    /**
     * 获取普通会话
     *
     * @param info 对方的信息
     */
    public void startConversation(BmobIMUserInfo info) {
        BmobIM.getInstance().startPrivateConversation(info, new ConversationListener() {
            @Override
            public void done(BmobIMConversation bmobIMConversation, BmobException e) {
                if (e == null) {
                    printInfo("BmobMI:startConversation()-> 创建会话成功");
                    convManager = obtainConvManager(bmobIMConversation);
                    StartConversationSuccessNext(convManager);
                } else {
                    StartConversationFailedNext();
                    BmobError("BmobMI:startConversation()-> 创建会话失败", e);
                }
            }
        });
    }

    /**
     * 获取全部会话
     */
    public List<BmobIMConversation> loadAllConversation() {
        return BmobIM.getInstance().loadAllConversation();
    }

    /**
     * 获取指定会话未读消息数
     */
    public long getUnreadCount(BmobIMConversation conv) {
        return BmobIM.getInstance().getUnReadCount(conv.getConversationId());
    }

    /**
     * 获取全部未读消息数
     */
    public long getAllUnreadCount() {
        return BmobIM.getInstance().getAllUnReadCount();
    }

    /**
     * 删除指定会话
     */
    public void deleteConversation(BmobIMConversation conv) {
        BmobIM.getInstance().deleteConversation(conv);
    }

    /**
     * 删除指定会话
     */
    public void deleteConversation(String conversationId) {
        BmobIM.getInstance().deleteConversation(conversationId);
    }

    /**
     * 清空全部会话
     */
    public void clearAllConversation() {
        BmobIM.getInstance().clearAllConversation();
    }

    /**
     * 获取会话消息体(可能需要通过该方式获取的会话才可发出消息)
     *
     * @param conv 创建会话后返回的会话体
     * @return 有效会话体
     */
    public BmobIMConversation obtainConvManager(BmobIMConversation conv) {
        convManager = BmobIMConversation.obtain(BmobIMClient.getInstance(), conv);
        return convManager;
    }

    /**
     * 查询会话消息
     *
     * @param conv  会话
     * @param msg   从哪条信息开始查询(可用作翻页查询)
     * @param limit 限制多少条
     */
    public void queryMessage(BmobIMConversation conv, BmobIMMessage msg, int limit) {
        conv.queryMessages(msg, limit, new MessagesQueryListener() {
            @Override
            public void done(List<BmobIMMessage> messages, BmobException e) {
                if (e == null) {
                    printInfo("Bmob:queryMessage()-> 查询消息成功");
                    QueryMessageSuccessNext(messages);
                } else {
                    QueryMessageFailedNext();
                    BmobError("Bmob:queryMessage()-> 查询消息失败", e);
                }
            }
        });
    }


    /**
     * 删除一条 或 多条聊天消息
     *
     * @param conv 会话
     * @param msgs 需要删除的消息
     */
    public void deleteBatchMessage(BmobIMConversation conv, List<BmobIMMessage> msgs) {
        conv.deleteBatchMessage(msgs);
    }

    /**
     * 清空指定会话的全部消息
     *
     * @param conv 会话
     */
    public void clearMessage(BmobIMConversation conv) {
        conv.clearMessage(true, new MessageListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    printInfo("Bmob:clearMessage()-> 清空成功");
                    ClearMessageSuccessNext();
                } else {
                    ClearMessageFailedNext();
                    BmobError("Bmob:clearMessage()-> 清空失败", e);
                }
            }
        });
    }

    /**
     * 设置指定会话全部消息已读
     *
     * @param conv 会话
     */
    public void setHadRead(BmobIMConversation conv) {
        conv.updateLocalCache();
    }

    /**
     * 发送文本
     */
    public void sendText(String text) {
        if (convManager==null) {
            printErr("会话未准备好, 请调用 startPrivateConversation() 以及 obtainConversation() 方法");
            return;
        }
        // 整理
        BmobIMTextMessage msg = new BmobIMTextMessage();
        msg.setContent(text);
        // 设置额外信息 - 如有需求
        // Map<String, Object> map = new HashMap<>();
        // map.put("attach", "what are you");
        // msg.setExtraMap(map);
        convManager.sendMessage(msg, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage message, BmobException e) {
                if (e == null) {
                    printInfo("Bmob:sendText()-> 发送文本消息成功");
                    SendTextMessageSuccessNext(message);
                } else {
                    SendTextMessageFailedNext();
                    BmobError("Bmob:sendText()-> 发送文本消息失败", e);
                }
            }
        });
    }

    /**
     * 发送图片
     */
    public void sendImage(String path) {
        if (convManager==null) {
            printErr("会话未准备好, 请调用 startPrivateConversation() 以及 obtainConversation() 方法");
            return;
        }
        BmobIMImageMessage image = new BmobIMImageMessage(path);
        convManager.sendMessage(image, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage message, BmobException e) {
                if (e == null) {
                    printInfo("Bmob:sendImage()-> 发送图片消息成功");
                    SendImgMessageSuccessNext(message);
                } else {
                    SendImgMessageFailedNext();
                    BmobError("Bmob:sendImage()-> 发送图片消息失败", e);
                }
            }
        });
    }

    /**
     * 发送音频
     */
    public void sendAudio(String path) {
        if (convManager==null) {
            printErr("会话未准备好, 请调用 startPrivateConversation() 以及 obtainConversation() 方法");
            return;
        }
        BmobIMAudioMessage audio = new BmobIMAudioMessage(path);
        convManager.sendMessage(audio, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage message, BmobException e) {
                if (e == null) {
                    printInfo("Bmob:sendAudio()-> 发送音频消息成功");
                    SendAudioMessageSuccessNext(message);
                } else {
                    SendAudioMessageFailedNext();
                    BmobError("Bmob:sendAudio()-> 发送音频消息失败", e);
                }
            }
        });
    }

    /**
     * 发送视频
     */
    public void sendVideo(String path) {
        if (convManager==null) {
            printErr("会话未准备好, 请调用 startPrivateConversation() 以及 obtainConversation() 方法");
            return;
        }
        BmobIMVideoMessage video = new BmobIMVideoMessage(path);
        convManager.sendMessage(video, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage message, BmobException e) {
                if (e == null) {
                    printInfo("Bmob:sendVideo()-> 发送视频消息成功");
                    SendVideaMessageSuccessNext(message);
                } else {
                    SendVideoMessageFailedNext();
                    BmobError("Bmob:sendVideo()-> 发送视频消息失败", e);
                }
            }
        });
    }

    /**
     * 发送文件
     */
    public void sendFile(String path) {
        if (convManager==null) {
            printErr("会话未准备好, 请调用 startPrivateConversation() 以及 obtainConversation() 方法");
            return;
        }
        BmobIMFileMessage file = new BmobIMFileMessage(path);
        convManager.sendMessage(file, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage message, BmobException e) {
                if (e == null) {
                    printInfo("Bmob:sendFile()-> 发送文件消息成功");
                    SendFileMessageSuccessNext(message);
                } else {
                    SendFileMessageFailedNext();
                    BmobError("Bmob:sendFile()-> 发送文件消息失败", e);
                }
            }
        });
    }

    /**
     * 发送坐标
     */
    public void sendLocation(String content, double lat, double lng) {
        if (convManager==null) {
            printErr("会话未准备好, 请调用 startPrivateConversation() 以及 obtainConversation() 方法");
            return;
        }
        BmobIMLocationMessage location = new BmobIMLocationMessage(content,lat,lng);
        convManager.sendMessage(location, new MessageSendListener() {
            @Override
            public void done(BmobIMMessage message, BmobException e) {
                if (e == null) {
                    printInfo("Bmob:sendLocation()-> 发送地理消息成功");
                    SendLocationMessageSuccessNext(message);
                } else {
                    SendLocationMessageFailedNext();
                    BmobError("Bmob:sendLocation()-> 发送地理消息失败", e);
                }
            }
        });
    }

    /* -------------------------------------------- impl -------------------------------------------- */

    // ---------------- 监听器 [SendLocationMessageFailed] ----------------
    private OnSendLocationMessageFailedListener onSendLocationMessageFailedListener;

    public interface OnSendLocationMessageFailedListener {
        void SendLocationMessageFailed();
    }

    public void setOnSendLocationMessageFailedListener(OnSendLocationMessageFailedListener onSendLocationMessageFailedListener) {
        this.onSendLocationMessageFailedListener = onSendLocationMessageFailedListener;
    }

    private void SendLocationMessageFailedNext() {
        if (onSendLocationMessageFailedListener != null) {
            onSendLocationMessageFailedListener.SendLocationMessageFailed();
        }
    }

    // ---------------- 监听器 [SendLocationMessageSuccess] ----------------
    private OnSendLocationMessageSuccessListener onSendLocationMessageSuccessListener;

    public interface OnSendLocationMessageSuccessListener {
        void SendLocationMessageSuccess(BmobIMMessage message);
    }

    public void setOnSendLocationMessageSuccessListener(OnSendLocationMessageSuccessListener onSendLocationMessageSuccessListener) {
        this.onSendLocationMessageSuccessListener = onSendLocationMessageSuccessListener;
    }

    private void SendLocationMessageSuccessNext(BmobIMMessage message) {
        if (onSendLocationMessageSuccessListener != null) {
            onSendLocationMessageSuccessListener.SendLocationMessageSuccess(message);
        }
    }

    // ---------------- 监听器 [SendFileMessageFailed] ----------------
    private OnSendFileMessageFailedListener onSendFileMessageFailedListener;

    public interface OnSendFileMessageFailedListener {
        void SendFileMessageFailed();
    }

    public void setOnSendFileMessageFailedListener(OnSendFileMessageFailedListener onSendFileMessageFailedListener) {
        this.onSendFileMessageFailedListener = onSendFileMessageFailedListener;
    }

    private void SendFileMessageFailedNext() {
        if (onSendFileMessageFailedListener != null) {
            onSendFileMessageFailedListener.SendFileMessageFailed();
        }
    }

    // ---------------- 监听器 [SendFileMessageSuccess] ----------------
    private OnSendFileMessageSuccessListener onSendFileMessageSuccessListener;

    public interface OnSendFileMessageSuccessListener {
        void SendFileMessageSuccess(BmobIMMessage message);
    }

    public void setOnSendFileMessageSuccessListener(OnSendFileMessageSuccessListener onSendFileMessageSuccessListener) {
        this.onSendFileMessageSuccessListener = onSendFileMessageSuccessListener;
    }

    private void SendFileMessageSuccessNext(BmobIMMessage message) {
        if (onSendFileMessageSuccessListener != null) {
            onSendFileMessageSuccessListener.SendFileMessageSuccess(message);
        }
    }

    // ---------------- 监听器 [SendVideoMessageFailed] ----------------
    private OnSendVideoMessageFailedListener onSendVideoMessageFailedListener;

    public interface OnSendVideoMessageFailedListener {
        void SendVideoMessageFailed();
    }

    public void setOnSendVideoMessageFailedListener(OnSendVideoMessageFailedListener onSendVideoMessageFailedListener) {
        this.onSendVideoMessageFailedListener = onSendVideoMessageFailedListener;
    }

    private void SendVideoMessageFailedNext() {
        if (onSendVideoMessageFailedListener != null) {
            onSendVideoMessageFailedListener.SendVideoMessageFailed();
        }
    }

    // ---------------- 监听器 [SendVideaMessageSuccess] ----------------
    private OnSendVideaMessageSuccessListener onSendVideaMessageSuccessListener;

    public interface OnSendVideaMessageSuccessListener {
        void SendVideaMessageSuccess(BmobIMMessage message);
    }

    public void setOnSendVideaMessageSuccessListener(OnSendVideaMessageSuccessListener onSendVideaMessageSuccessListener) {
        this.onSendVideaMessageSuccessListener = onSendVideaMessageSuccessListener;
    }

    private void SendVideaMessageSuccessNext(BmobIMMessage message) {
        if (onSendVideaMessageSuccessListener != null) {
            onSendVideaMessageSuccessListener.SendVideaMessageSuccess(message);
        }
    }

    // ---------------- 监听器 [SendAudioMessageFailed] ----------------
    private OnSendAudioMessageFailedListener onSendAudioMessageFailedListener;

    public interface OnSendAudioMessageFailedListener {
        void SendAudioMessageFailed();
    }

    public void setOnSendAudioMessageFailedListener(OnSendAudioMessageFailedListener onSendAudioMessageFailedListener) {
        this.onSendAudioMessageFailedListener = onSendAudioMessageFailedListener;
    }

    private void SendAudioMessageFailedNext() {
        if (onSendAudioMessageFailedListener != null) {
            onSendAudioMessageFailedListener.SendAudioMessageFailed();
        }
    }

    // ---------------- 监听器 [SendAudioMessageSuccess] ----------------
    private OnSendAudioMessageSuccessListener onSendAudioMessageSuccessListener;

    public interface OnSendAudioMessageSuccessListener {
        void SendAudioMessageSuccess(BmobIMMessage message);
    }

    public void setOnSendAudioMessageSuccessListener(OnSendAudioMessageSuccessListener onSendAudioMessageSuccessListener) {
        this.onSendAudioMessageSuccessListener = onSendAudioMessageSuccessListener;
    }

    private void SendAudioMessageSuccessNext(BmobIMMessage message) {
        if (onSendAudioMessageSuccessListener != null) {
            onSendAudioMessageSuccessListener.SendAudioMessageSuccess(message);
        }
    }

    // ---------------- 监听器 [SendImgMessageFailed] ----------------
    private OnSendImgMessageFailedListener onSendImgMessageFailedListener;

    public interface OnSendImgMessageFailedListener {
        void SendImgMessageFailed();
    }

    public void setOnSendImgMessageFailedListener(OnSendImgMessageFailedListener onSendImgMessageFailedListener) {
        this.onSendImgMessageFailedListener = onSendImgMessageFailedListener;
    }

    private void SendImgMessageFailedNext() {
        if (onSendImgMessageFailedListener != null) {
            onSendImgMessageFailedListener.SendImgMessageFailed();
        }
    }

    // ---------------- 监听器 [SendImgMessageSuccess] ----------------
    private OnSendImgMessageSuccessListener onSendImgMessageSuccessListener;

    public interface OnSendImgMessageSuccessListener {
        void SendImgMessageSuccess(BmobIMMessage message);
    }

    public void setOnSendImgMessageSuccessListener(OnSendImgMessageSuccessListener onSendImgMessageSuccessListener) {
        this.onSendImgMessageSuccessListener = onSendImgMessageSuccessListener;
    }

    private void SendImgMessageSuccessNext(BmobIMMessage message) {
        if (onSendImgMessageSuccessListener != null) {
            onSendImgMessageSuccessListener.SendImgMessageSuccess(message);
        }
    }

    // ---------------- 监听器 [SendTextMessageFailed] ----------------
    private OnSendTextMessageFailedListener onSendTextMessageFailedListener;

    public interface OnSendTextMessageFailedListener {
        void SendTextMessageFailed();
    }

    public void setOnSendTextMessageFailedListener(OnSendTextMessageFailedListener onSendTextMessageFailedListener) {
        this.onSendTextMessageFailedListener = onSendTextMessageFailedListener;
    }

    private void SendTextMessageFailedNext() {
        if (onSendTextMessageFailedListener != null) {
            onSendTextMessageFailedListener.SendTextMessageFailed();
        }
    }

    // ---------------- 监听器 [SendTextMessageSuccess] ----------------
    private OnSendTextMessageSuccessListener onSendTextMessageSuccessListener;

    public interface OnSendTextMessageSuccessListener {
        void SendTextMessageSuccess(BmobIMMessage message);
    }

    public void setOnSendTextMessageSuccessListener(OnSendTextMessageSuccessListener onSendTextMessageSuccessListener) {
        this.onSendTextMessageSuccessListener = onSendTextMessageSuccessListener;
    }

    private void SendTextMessageSuccessNext(BmobIMMessage message) {
        if (onSendTextMessageSuccessListener != null) {
            onSendTextMessageSuccessListener.SendTextMessageSuccess(message);
        }
    }


    // ---------------- 监听器 [ClearMessageSuccess] ----------------
    private OnClearMessageSuccessListener onClearMessageSuccessListener;

    public interface OnClearMessageSuccessListener {
        void ClearMessageSuccess();
    }

    public void setOnClearMessageSuccessListener(OnClearMessageSuccessListener onClearMessageSuccessListener) {
        this.onClearMessageSuccessListener = onClearMessageSuccessListener;
    }

    private void ClearMessageSuccessNext() {
        if (onClearMessageSuccessListener != null) {
            onClearMessageSuccessListener.ClearMessageSuccess();
        }
    }

    // ---------------- 监听器 [ClearMessageFailed] ----------------
    private OnClearMessageFailedListener onClearMessageFailedListener;

    public interface OnClearMessageFailedListener {
        void ClearMessageFailed();
    }

    public void setOnClearMessageFailedListener(OnClearMessageFailedListener onClearMessageFailedListener) {
        this.onClearMessageFailedListener = onClearMessageFailedListener;
    }

    private void ClearMessageFailedNext() {
        if (onClearMessageFailedListener != null) {
            onClearMessageFailedListener.ClearMessageFailed();
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

    // ---------------- 监听器 [QueryMessageSuccess] ----------------
    private OnQueryMessageSuccessListener onQueryMessageSuccessListener;

    public interface OnQueryMessageSuccessListener {
        void QueryMessageSuccess(List<BmobIMMessage> messages);
    }

    public void setOnQueryMessageSuccessListener(OnQueryMessageSuccessListener onQueryMessageSuccessListener) {
        this.onQueryMessageSuccessListener = onQueryMessageSuccessListener;
    }

    private void QueryMessageSuccessNext(List<BmobIMMessage> messages) {
        if (onQueryMessageSuccessListener != null) {
            onQueryMessageSuccessListener.QueryMessageSuccess(messages);
        }
    }

    // ---------------- 监听器 [StartConversationFailed] ----------------
    private OnStartConversationFailedListener onStartConversationFailedListener;

    public interface OnStartConversationFailedListener {
        void StartConversationFailed();
    }

    public void setOnStartConversationFailedListener(OnStartConversationFailedListener onStartConversationFailedListener) {
        this.onStartConversationFailedListener = onStartConversationFailedListener;
    }

    private void StartConversationFailedNext() {
        if (onStartConversationFailedListener != null) {
            onStartConversationFailedListener.StartConversationFailed();
        }
    }

    // ---------------- 监听器 [StartConversationSuccess] ----------------
    private OnStartConversationSuccessListener onStartConversationSuccessListener;

    public interface OnStartConversationSuccessListener {
        void StartConversationSuccess(BmobIMConversation conv);
    }

    public void setOnStartConversationSuccessListener(OnStartConversationSuccessListener onStartConversationSuccessListener) {
        this.onStartConversationSuccessListener = onStartConversationSuccessListener;
    }

    private void StartConversationSuccessNext(BmobIMConversation conv) {
        if (onStartConversationSuccessListener != null) {
            onStartConversationSuccessListener.StartConversationSuccess(conv);
        }
    }

    // ---------------- 监听器 [ConnectSuccess] ----------------
    private OnConnectSuccessListener onConnectSuccessListener;

    public interface OnConnectSuccessListener {
        void ConnectSuccess(String uid);
    }

    public void setOnConnectSuccessListener(OnConnectSuccessListener onConnectSuccessListener) {
        this.onConnectSuccessListener = onConnectSuccessListener;
    }

    private void ConnectSuccessNext(String uid) {
        if (onConnectSuccessListener != null) {
            onConnectSuccessListener.ConnectSuccess(uid);
        }
    }

    // ---------------- 监听器 [ConnectFailed] ----------------
    private OnConnectFailedListener onConnectFailedListener;

    public interface OnConnectFailedListener {
        void ConnectFailed();
    }

    public void setOnConnectFailedListener(OnConnectFailedListener onConnectFailedListener) {
        this.onConnectFailedListener = onConnectFailedListener;
    }

    private void ConnectFailedNext() {
        if (onConnectFailedListener != null) {
            onConnectFailedListener.ConnectFailed();
        }
    }

    // ---------------- 监听器 [GetConnectStatu] ----------------
    private OnGetConnectStatuListener onGetConnectStatuListener;

    public interface OnGetConnectStatuListener {
        void GetConnectStatu(ConnectionStatus status);

    }

    public void setOnGetConnectStatuListener(OnGetConnectStatuListener onGetConnectStatuListener) {
        this.onGetConnectStatuListener = onGetConnectStatuListener;
    }

    private void GetConnectStatuNext(ConnectionStatus status) {
        if (onGetConnectStatuListener != null) {
            onGetConnectStatuListener.GetConnectStatu(status);
        }
    }
}
