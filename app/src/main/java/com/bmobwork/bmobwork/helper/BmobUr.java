package com.bmobwork.bmobwork.helper;

import com.bmobwork.bmobwork.bean.User;
import com.bmobwork.bmobwork.utils.TimerUtils;

import cn.bmob.v3.BmobSMS;
import cn.bmob.v3.BmobUser;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FetchUserInfoListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/*
 * Created by Administrator on 2021/02/003.
 * 该类用于用户操作
 */
public class BmobUr extends BmobBase {

    TimerUtils timer = null;// 登出检测
    int count = 0;// 登出轮询次数

    /**
     * 注册
     *
     * @param user 注册者
     */
    public void signUp(User user) {
        user.signUp(new SaveListener<User>() {
            @Override
            public void done(User user, BmobException e) {
                if (e == null) {
                    printInfo("BmobUr:signUp()-> 注册成功");
                    SignUpSuccessNext(user);
                } else {
                    SignUpFailedNext();
                    BmobError("BmobUr:signUp()-> 注册失败", e);
                }
            }
        });
    }

    /**
     * 登录
     *
     * @param user 登录者
     */
    public void login(User user) {
        user.login(new SaveListener<User>() {
            @Override
            public void done(User userbean, BmobException e) {
                if (e == null) {
                    printInfo("BmobUr:login()-> 登录成功, User = " + userbean.getUsername());
                    LoginSuccessNext(userbean);
                } else {
                    LoginFailedNext();
                    BmobError("BmobUr:login()-> 登录失败", e);
                }
            }
        });
    }

    /**
     * 请求校验码
     *
     * @param phone 接收的手机号
     */
    public void reqSMSCode(String phone) {
        BmobSMS.requestSMSCode(phone, "", new QueryListener<Integer>() {
            @Override
            public void done(Integer smsId, BmobException e) {
                if (e == null) {
                    printInfo("BmobUr:reqSMSCode()-> 发送验证码成功，短信ID：" + smsId);
                    ReqSMSCodeSuccessNext(smsId);
                } else {
                    ReqSMSCodeFailedNext();
                    BmobError("BmobUr:reqSMSCode()-> 发送验证码失败：", e);
                }
            }
        });
    }

    /**
     * 验证校验码
     *
     * @param phone 发送手机号
     * @param code  收到的校验码
     */
    public void checkSMSCode(String phone, String code) {
        BmobSMS.verifySmsCode(phone, code, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    printInfo("BmobUr:checkSMSCode()-> 验证成功");
                    CheckSMSCodeSuccessNext();
                } else {
                    CheckSMSCodeFailedNext();
                    BmobError("BmobUr:checkSMSCode()-> 验证失败", e);
                }
            }
        });
    }

    /**
     * 退出
     */
    public void logout() {
        count = 0;
        timer = null;
        BmobUser.logOut();
        // 弱网轮询
        if (timer == null) {
            timer = new TimerUtils() {
                @Override
                public void doSomething() {
                    if (!isLogin()) {
                        timer.stop();
                        timer = null;
                        printInfo("BmobUr:logout()-> 登出成功");
                        LogoutSuccessNext();
                    } else if (count > 10) {
                        printErr("BmobUr:logout()-> 登出失败");
                        LogoutFailedNext();
                    } else {
                        count++;
                        printWarn("BmobUr:logout()-> 当前弱网环境, 正在尝试第 " + count + " 次");
                    }
                }
            };
        }
        timer.start(1000, 1000);
    }

    /**
     * 获取当前用户
     *
     * @return 用户对象
     */
    public User getCurrentUser() {
        if (BmobUser.isLogin()) {
            User user = BmobUser.getCurrentUser(User.class);
            printInfo("BmobUr:getCurrentUser()-> 用户 [" + user.getUsername() + "] 已登陆");
            return user;
        } else {
            printWarn("BmobUr:getCurrentUser()-> 没有登录, 无法获取对象");
        }
        return null;
    }

    /**
     * @return 用户是否登录
     */
    public boolean isLogin() {
        return BmobUser.isLogin();
    }

    /**
     * 更新用户缓存
     */
    public void fetchUser() {
        if (!isLogin()) {
            BmobError("BmobUr:fetchUser()-> 当前用户未登录, 不允许同步");
            return;
        }
        BmobUser.fetchUserInfo(new FetchUserInfoListener<BmobUser>() {
            @Override
            public void done(BmobUser user, BmobException e) {
                if (e == null) {
                    final User userbean = BmobUser.getCurrentUser(User.class);
                    printInfo("BmobUr:fetchUser()-> 刷新用户缓存成功\n" + userbean.getUsername());
                    FetchUserSuccessNext(userbean);
                } else {
                    FetchUserFailedNext();
                    BmobError("BmobUr:fetchUser()-> 刷新用户缓存失败", e);
                }
            }
        });
    }

    /**
     * 更新用户信息
     */
    public void updateUser() {
        if (!isLogin()) {
            BmobError("BmobUr:updateUser()-> 当前用户未登录, 不允许更新");
            return;
        }
        User user_old = BmobUser.getCurrentUser(User.class);
        User user_new = UpdateUserInfoNext(user_old);
        user_new.update(new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    printInfo("BmobUr:updateUser()-> 更新用户信息成功");
                    UpdateSuccessNext();
                } else {
                    UpdateFailedNext();
                    BmobError("BmobUr:updateUser()-> 更新用户信息失败", e);
                }
            }
        });
    }

    /**
     * 修改密码
     */
    public void changePwd(String oldPwd, String newPwd) {
        // 密码本地校验
        if (oldPwd.equalsIgnoreCase(newPwd)) {
            printWarn("BmobUr:changePwd()-> 前后密码一样,无需修改");
            return;
        }
        if (!isLogin()) {
            BmobError("BmobUr:changePwd()-> 当前用户未登录, 不允许修改");
            return;
        }
        // 修改
        BmobUser.updateCurrentUserPassword(oldPwd, newPwd, new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    printInfo("BmobUr:changePwd()-> 更换密码成功");
                    ChangePwdSuccessNext();
                } else {
                    ChangePwdFailedNext();
                    BmobError("BmobUr:changePwd()-> 更换密码失败", e);
                }
            }
        });
    }

    /* -------------------------------------------- impl -------------------------------------------- */
    // ---------------- 监听器 [SignUpSuccess] ----------------
    private OnSignUpSuccessListener<User> onSignUpSuccessListener;

    public interface OnSignUpSuccessListener<Userbean> {
        void SignUpSuccess(Userbean userbean);
    }

    public void setOnSignUpSuccessListener(OnSignUpSuccessListener<User> onSignUpSuccessListener) {
        this.onSignUpSuccessListener = onSignUpSuccessListener;
    }

    private void SignUpSuccessNext(User userbean) {
        if (onSignUpSuccessListener != null) {
            onSignUpSuccessListener.SignUpSuccess(userbean);
        }
    }

    // ---------------- 监听器 [SignUpFailed] ----------------
    private OnSignUpFailedListener onSignUpFailedListener;

    public interface OnSignUpFailedListener {
        void SignUpFailed();
    }

    public void setOnSignUpFailedListener(OnSignUpFailedListener onSignUpFailedListener) {
        this.onSignUpFailedListener = onSignUpFailedListener;
    }

    private void SignUpFailedNext() {
        if (onSignUpFailedListener != null) {
            onSignUpFailedListener.SignUpFailed();
        }
    }

    // ---------------- 监听器 [LoginSuccess] ----------------
    private OnLoginSuccessListener<User> onLoginSuccessListener;

    public interface OnLoginSuccessListener<Userbean> {
        void LoginSuccess(Userbean userbean);
    }

    public void setOnLoginSuccessListener(OnLoginSuccessListener<User> onLoginSuccessListener) {
        this.onLoginSuccessListener = onLoginSuccessListener;
    }

    private void LoginSuccessNext(User userbean) {
        if (onLoginSuccessListener != null) {
            onLoginSuccessListener.LoginSuccess(userbean);
        }
    }

    // ---------------- 监听器 [LoginFailed] ----------------
    private OnLoginFailedListener onLoginFailedListener;

    public interface OnLoginFailedListener {
        void LoginFailed();
    }

    public void setOnLoginFailedListener(OnLoginFailedListener onLoginFailedListener) {
        this.onLoginFailedListener = onLoginFailedListener;
    }

    private void LoginFailedNext() {
        if (onLoginFailedListener != null) {
            onLoginFailedListener.LoginFailed();
        }
    }

    // ---------------- 监听器 [ReqSMSCodeSuccess] ----------------
    private OnReqSMSCodeSuccessListener onReqSMSCodeSuccessListener;

    public interface OnReqSMSCodeSuccessListener {
        void ReqSMSCodeSuccess(int smsId);
    }

    public void setOnReqSMSCodeSuccessListener(OnReqSMSCodeSuccessListener onReqSMSCodeSuccessListener) {
        this.onReqSMSCodeSuccessListener = onReqSMSCodeSuccessListener;
    }

    private void ReqSMSCodeSuccessNext(int smsId) {
        if (onReqSMSCodeSuccessListener != null) {
            onReqSMSCodeSuccessListener.ReqSMSCodeSuccess(smsId);
        }
    }

    // ---------------- 监听器 [ReqSMSCodeFailed] ----------------
    private OnReqSMSCodeFailedListener onReqSMSCodeFailedListener;

    public interface OnReqSMSCodeFailedListener {
        void ReqSMSCodeFailed();
    }

    public void setOnReqSMSCodeFailedListener(OnReqSMSCodeFailedListener onReqSMSCodeFailedListener) {
        this.onReqSMSCodeFailedListener = onReqSMSCodeFailedListener;
    }

    private void ReqSMSCodeFailedNext() {
        if (onReqSMSCodeFailedListener != null) {
            onReqSMSCodeFailedListener.ReqSMSCodeFailed();
        }
    }

    // ---------------- 监听器 [FetchUserSuccess] ----------------
    private OnFetchUserSuccessListener<User> onFetchUserSuccessListener;

    public interface OnFetchUserSuccessListener<Userbean> {
        void FetchUserSuccess(Userbean userbean);
    }

    public void setOnFetchUserSuccessListener(OnFetchUserSuccessListener<User> onFetchUserSuccessListener) {
        this.onFetchUserSuccessListener = onFetchUserSuccessListener;
    }

    private void FetchUserSuccessNext(User userbean) {
        if (onFetchUserSuccessListener != null) {
            onFetchUserSuccessListener.FetchUserSuccess(userbean);
        }
    }

    // ---------------- 监听器 [FetchUserFailed] ----------------
    private OnFetchUserFailedListener onFetchUserFailedListener;

    public interface OnFetchUserFailedListener {
        void FetchUserFailed();
    }

    public void setOnFetchUserFailedListener(OnFetchUserFailedListener onFetchUserFailedListener) {
        this.onFetchUserFailedListener = onFetchUserFailedListener;
    }

    private void FetchUserFailedNext() {
        if (onFetchUserFailedListener != null) {
            onFetchUserFailedListener.FetchUserFailed();
        }
    }

    // ---------------- 监听器 [UpdateUserInfo] ----------------
    private OnUpdateUserInfoListener<User> onUpdateUserInfoListener;

    public interface OnUpdateUserInfoListener<Userbean> {
        Userbean UpdateUserInfo(Userbean userbean);
    }

    public void setOnUpdateUserInfoListener(OnUpdateUserInfoListener<User> onUpdateUserInfoListener) {
        this.onUpdateUserInfoListener = onUpdateUserInfoListener;
    }

    private User UpdateUserInfoNext(User userbean) {
        if (onUpdateUserInfoListener != null) {
            return onUpdateUserInfoListener.UpdateUserInfo(userbean);
        }
        return userbean;
    }

    // ---------------- 监听器 [UpdateSuccess] ----------------
    private OnUpdateSuccessListener onUpdateSuccessListener;

    public interface OnUpdateSuccessListener {
        void UpdateSuccess();
    }

    public void setOnUpdateSuccessListener(OnUpdateSuccessListener onUpdateSuccessListener) {
        this.onUpdateSuccessListener = onUpdateSuccessListener;
    }

    private void UpdateSuccessNext() {
        if (onUpdateSuccessListener != null) {
            onUpdateSuccessListener.UpdateSuccess();
        }
    }

    // ---------------- 监听器 [UpdateFailed] ----------------
    private OnUpdateFailedListener onUpdateFailedListener;

    public interface OnUpdateFailedListener {
        void UpdateFailed();
    }

    public void setOnUpdateFailedListener(OnUpdateFailedListener onUpdateFailedListener) {
        this.onUpdateFailedListener = onUpdateFailedListener;
    }

    private void UpdateFailedNext() {
        if (onUpdateFailedListener != null) {
            onUpdateFailedListener.UpdateFailed();
        }
    }

    // ---------------- 监听器 [ChangePwdSuccess] ----------------
    private OnChangePwdSuccessListener onChangePwdSuccessListener;

    public interface OnChangePwdSuccessListener {
        void ChangePwdSuccess();
    }

    public void setOnChangePwdSuccessListener(OnChangePwdSuccessListener onChangePwdSuccessListener) {
        this.onChangePwdSuccessListener = onChangePwdSuccessListener;
    }

    private void ChangePwdSuccessNext() {
        if (onChangePwdSuccessListener != null) {
            onChangePwdSuccessListener.ChangePwdSuccess();
        }
    }

    // ---------------- 监听器 [ChangePwdFailed] ----------------
    private OnChangePwdFailedListener onChangePwdFailedListener;

    public interface OnChangePwdFailedListener {
        void ChangePwdFailed();
    }

    public void setOnChangePwdFailedListener(OnChangePwdFailedListener onChangePwdFailedListener) {
        this.onChangePwdFailedListener = onChangePwdFailedListener;
    }

    private void ChangePwdFailedNext() {
        if (onChangePwdFailedListener != null) {
            onChangePwdFailedListener.ChangePwdFailed();
        }
    }

    // ---------------- 监听器 [CheckSMSCodeSuccess] ----------------
    private OnCheckSMSCodeSuccessListener onCheckSMSCodeSuccessListener;

    public interface OnCheckSMSCodeSuccessListener {
        void CheckSMSCodeSuccess();
    }

    public void setOnCheckSMSCodeSuccessListener(OnCheckSMSCodeSuccessListener onCheckSMSCodeSuccessListener) {
        this.onCheckSMSCodeSuccessListener = onCheckSMSCodeSuccessListener;
    }

    private void CheckSMSCodeSuccessNext() {
        if (onCheckSMSCodeSuccessListener != null) {
            onCheckSMSCodeSuccessListener.CheckSMSCodeSuccess();
        }
    }

    // ---------------- 监听器 [CheckSMSCodeFailed] ----------------
    private OnCheckSMSCodeFailedListener onCheckSMSCodeFailedListener;

    public interface OnCheckSMSCodeFailedListener {
        void CheckSMSCodeFailed();
    }

    public void setOnCheckSMSCodeFailedListener(OnCheckSMSCodeFailedListener onCheckSMSCodeFailedListener) {
        this.onCheckSMSCodeFailedListener = onCheckSMSCodeFailedListener;
    }

    private void CheckSMSCodeFailedNext() {
        if (onCheckSMSCodeFailedListener != null) {
            onCheckSMSCodeFailedListener.CheckSMSCodeFailed();
        }
    }

    // ---------------- 监听器 [LogoutSuccess] ----------------
    private OnLogoutSuccessListener onLogoutSuccessListener;

    public interface OnLogoutSuccessListener {
        void LogoutSuccess();
    }

    public void setOnLogoutSuccessListener(OnLogoutSuccessListener onLogoutSuccessListener) {
        this.onLogoutSuccessListener = onLogoutSuccessListener;
    }

    private void LogoutSuccessNext() {
        if (onLogoutSuccessListener != null) {
            onLogoutSuccessListener.LogoutSuccess();
        }
    }

    // ---------------- 监听器 [LogoutFailed] ----------------
    private OnLogoutFailedListener onLogoutFailedListener;

    public interface OnLogoutFailedListener {
        void LogoutFailed();
    }

    public void setOnLogoutFailedListener(OnLogoutFailedListener onLogoutFailedListener) {
        this.onLogoutFailedListener = onLogoutFailedListener;
    }

    private void LogoutFailedNext() {
        if (onLogoutFailedListener != null) {
            onLogoutFailedListener.LogoutFailed();
        }
    }


}
