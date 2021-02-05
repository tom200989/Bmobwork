package com.bmobwork.bmobwork.ui;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.bmobwork.bmobwork.R;
import com.bmobwork.bmobwork.bean.User;
import com.bmobwork.bmobwork.demo.Person;
import com.bmobwork.bmobwork.demo.Printer;
import com.bmobwork.bmobwork.demo.Worker;
import com.bmobwork.bmobwork.helper.BmobDB;
import com.bmobwork.bmobwork.helper.BmobFi;
import com.bmobwork.bmobwork.helper.BmobUr;
import com.bmobwork.bmobwork.utils.SDUtils;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 真机申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{//
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,//
                    Manifest.permission.READ_EXTERNAL_STORAGE//
            }, 1001);
        }
    }

    /* -------------------------------------------- 对象操作 -------------------------------------------- */
    // 新增(单个)
    public void save(View view) {
        Person person = new Person();
        person.setAddress("Beijing");
        person.setName("maqianli");

        BmobDB<Person> db = new BmobDB<>();
        db.setOnSaveOneSuccessListener(objectId -> Printer.i("保存成功, ID = " + objectId));
        db.setOnSaveOneFailedListener(() -> Printer.e("保存失败"));
        db.save(person);
    }

    // 新增(批量)
    public void saveBatch(View view) {
        Person person = new Person();
        person.setAddress("Shanghai");
        person.setName("maqianli");

        Worker worker = new Worker();
        worker.setSex("Man");
        worker.setWork("Programer");

        List<BmobObject> bmobBeans = new ArrayList<>();
        bmobBeans.add(person);
        bmobBeans.add(worker);

        BmobDB<Person> db = new BmobDB<>();
        db.setOnSaveBatchSuccessListener(objectIds -> Printer.i("批量保存成功"));
        db.setOnSaveBatchFailedListener(() -> Printer.e("批量保存失败"));
        db.saveBatch(bmobBeans);
    }

    // 更新(单个)
    public void update(View view) {
        String id = "2a69325b11";

        Person person = new Person();
        person.setObjectId(id);
        person.setAddress("Guangdong");
        person.setName("weixin");

        BmobDB<Person> db = new BmobDB<>();
        db.setOnUpdateOneSuccessListener(() -> Printer.i("更新成功"));
        db.setOnUpdateOneFailedListener(() -> Printer.e("更新失败"));
        db.update(person);
    }

    // 更新(批量)
    public void updateBatch(View view) {
        String id1 = "68cd6aceba";
        String id2 = "020ee82136";

        Person person = new Person();
        person.setObjectId(id1);
        person.setAddress("Hangzhou");
        person.setName("huayang");

        Worker worker = new Worker();
        worker.setObjectId(id2);
        worker.setSex("Women");
        worker.setWork("Newer");

        List<BmobObject> bmobBeans = new ArrayList<>();
        bmobBeans.add(person);
        bmobBeans.add(worker);

        BmobDB<Person> db = new BmobDB<>();
        db.setOnUpdateBatchSuccessListener(objectIds -> Printer.i("批量更新成功"));
        db.setOnUpdateBatchNoSetObjectIdListener(bmobObjects -> Printer.e("部分对象没有指定更新值"));
        db.setOnUpdateBatchFailedListener(() -> Printer.e("批量更新失败"));
        db.updateBatch(bmobBeans);
    }

    // 删除(单个)
    public void delete(View view) {
        String id = "020ee82136";
        BmobDB<BmobObject> db = new BmobDB<>();
        db.setOnDeleteOneSuccessListener(() -> Printer.i("删除成功"));
        db.setOnDeleteOneFailedListener(() -> Printer.e("删除失败"));
        db.delete(Worker.class, id);
    }

    // 删除(批量)
    public void deleteBatch(View view) {
        String id1 = "d5f27e2402";
        String id2 = "ca3ad6bc53";

        Person person = new Person();
        person.setObjectId(id1);

        Worker worker = new Worker();
        worker.setObjectId(id2);

        List<BmobObject> bmobBeans = new ArrayList<>();
        bmobBeans.add(person);
        bmobBeans.add(worker);

        BmobDB<BmobObject> db = new BmobDB<>();
        db.deleteBatch(bmobBeans);
    }

    // 批量操作
    public void doBatch(View view) {
        // 新增
        List<BmobObject> saves = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Person person = new Person();
            person.setAddress("Hubei_" + i);
            person.setName("Qiufeng_" + i);
            saves.add(person);
        }

        // 更新
        List<BmobObject> updates = new ArrayList<>();
        String id1 = "bdf9d7e854";
        Worker worker = new Worker();
        worker.setObjectId(id1);
        worker.setWork("挖煤");
        worker.setSex("男");
        updates.add(worker);

        // 删除
        List<BmobObject> deletes = new ArrayList<>();
        String id2 = "d1853b979b";
        Person person = new Person();
        person.setObjectId(id2);
        deletes.add(person);

        BmobDB<BmobObject> db = new BmobDB<>();
        db.setOnDoBatchSuccessListener(ids -> Printer.i("批量操作成功"));
        db.setOnDoBatchNoSetObjectIdListener(bmobBeans -> Printer.e("部分对象没有指定更新值"));
        db.setOnDoBatchFailedListener(() -> Printer.e("批量操作失败"));
        db.doBatch(saves, updates, deletes);

    }

    /* -------------------------------------------- 用户操作 -------------------------------------------- */

    // 注册
    public void signUp(View view) {

        // 信息
        User user = new User();
        user.setUsername("maqianli");
        user.setPassword("123456");
        user.setMobilePhoneNumber("15012889815");
        user.setMobilePhoneNumberVerified(true);
        user.setSalary(500000.8f);
        user.setAge(32);
        // 发起
        BmobUr ur = new BmobUr();
        ur.setOnSignUpSuccessListener(acc -> Printer.i("注册成功: " + acc.toString()));
        ur.setOnSignUpFailedListener(() -> Printer.e("注册失败"));
        ur.signUp(user);

    }

    // 登陆
    public void login(View view) {
        User user = new User();
        user.setUsername("maqianli");
        user.setPassword("123123");
        BmobUr ur = new BmobUr();
        ur.setOnLoginSuccessListener(user1 -> Printer.i("登录成功\n" + user1.toString()));
        ur.setOnLoginFailedListener(() -> Printer.e("登录失败"));
        ur.login(user);
    }

    // 请求校验码
    public void reqSMSCode(View view) {
        BmobUr ur = new BmobUr();
        ur.setOnReqSMSCodeSuccessListener(smsId -> Printer.i("请求验证码成功: smsID = " + smsId));
        ur.setOnReqSMSCodeFailedListener(() -> Printer.e("请求验证码失败"));
        ur.reqSMSCode("15012889815");
    }

    // 验证校验码
    public void checkSMSCode(View view) {
        BmobUr ur = new BmobUr();
        ur.setOnCheckSMSCodeSuccessListener(() -> Printer.i("验证成功"));
        ur.setOnCheckSMSCodeFailedListener(() -> Printer.e("验证失败"));
        ur.checkSMSCode("15012889815", "591443");
    }

    // 登出
    public void logout(View view) {
        BmobUr ur = new BmobUr();
        ur.setOnLogoutSuccessListener(() -> Printer.i("登出成功"));
        ur.setOnLogoutFailedListener(() -> Printer.e("登出失败"));
        ur.logout();
    }

    // 获取当前用户
    public void getcurrent(View view) {
        BmobUr ur = new BmobUr();
        User user = ur.getCurrentUser();
        if (user != null) {
            Printer.i("当前用户: \n" + user.toString());
        } else {
            Printer.e("请先登录");
        }
    }

    // 是否登录
    public void isLogin(View view) {
        BmobUr ur = new BmobUr();
        boolean login = ur.isLogin();
        if (login) {
            Printer.i("在线");
        } else {
            Printer.e("离线");
        }
    }

    // 刷新用户缓存
    public void fetchUser(View view) {
        BmobUr ur = new BmobUr();
        ur.setOnFetchUserSuccessListener(user -> Printer.i("刷新用户缓存成功, 当前: " + user.toString()));
        ur.setOnFetchUserFailedListener(() -> Printer.e("刷新用户缓存失败"));
        ur.fetchUser();
    }

    // 更新用户信息
    public void updateUser(View view) {
        BmobUr ur = new BmobUr();
        ur.setOnUpdateUserInfoListener(user -> {
            user.setSalary(600000);// 修改内容
            return user;// 返回修改后对象
        });
        ur.setOnUpdateSuccessListener(() -> Printer.i("更新成功"));
        ur.setOnUpdateFailedListener(() -> Printer.e("更新失败"));
        ur.updateUser();
    }

    // 修改密码
    public void changePwd(View view) {
        BmobUr ur = new BmobUr();
        ur.setOnChangePwdSuccessListener(() -> Printer.i("修改成功"));
        ur.setOnChangePwdFailedListener(() -> Printer.e("修改失败"));
        ur.changePwd("123124", "123456");
    }

    /* -------------------------------------------- 文件管理 -------------------------------------------- */

    // 上传(单个)
    public void upload(View view) {
        String path = SDUtils.getSdFilePath(this, "Download", "测试压缩.zip");
        BmobFi fi = new BmobFi();
        fi.setOnUploadSuccessListener(url -> Printer.i("上传成功"));
        fi.setOnUploadFailedListener(() -> Printer.e("上传失败"));
        fi.setOnUploadProgressListener(progress -> Printer.i("上传进度: " + progress));
        fi.upload(path);

    }

    // 下载
    public void download(View view) {
        String savePath = SDUtils.getSdFilePath(this, "Download", "测试压缩_test.zip");
        BmobFile bmobFile = new BmobFile("测试压缩.zip", "", "https://bmob-cdn-29288.bmobpay.com/2021/02/05/23fdd1a03a504e39872b53c9e4c9648c.zip");
        BmobFi fi = new BmobFi();
        fi.setOnDownloadStartListener(() -> Printer.i("开始下载"));
        fi.setOnDownloadSuccessListener(path -> Printer.i("下载成功"));
        fi.setOnDownloadFailedListener(() -> Printer.e("下载失败"));
        fi.setOnDownloadProgressListener((progress, netWorkSpeed) -> Printer.i("下载进度: " + progress + "; 网速: " + netWorkSpeed));
        fi.download(bmobFile, savePath);
    }

    // 删除
    public void deleteFile(View view) {
        BmobFi fi = new BmobFi();
        fi.setOnDeleteFailedListener(() -> Printer.e("删除失败"));
        fi.setOnDeleteSuccessListener(() -> Printer.i("删除成功"));
        fi.delete("http://bmob-cdn-29288.bmobpay.com/2021/02/05/23b1e4b5a7934aaabd58382abe8cf3de.zip");
    }

    // 批量上传
    public void uploadBatch(View view) {
        String path1 = SDUtils.getSdFilePath(this, "Download", "测试压缩.zip");
        String path2 = SDUtils.getSdFilePath(this, "Download", "测试压缩_test.zip");
        BmobFi fi = new BmobFi();
        fi.setOnUpload_Batch_SuccessListener((files, urls) -> Printer.i("批上传成功"));
        fi.setOnUpload_Batch_FailedListener(() -> Printer.e("批上传失败"));
        fi.setOnUpload_Batch_ProgressListener((curIndex, curPercent, total, totalPercent) -> Printer.i("正在上传睇: " + curIndex + " 个"));
        fi.uploadBatch(path1, path2);
    }

    // 批量删除
    public void deleteFileBatch(View view) {
        String url1 = "https://bmob-cdn-29288.bmobpay.com/2021/02/05/333b8c4dee154243aa9f4cd3c8251b3d.zip";
        String url2 = "https://bmob-cdn-29288.bmobpay.com/2021/02/05/13b1adfa23274b1a81332c98ab560c25.zip";
        BmobFi fi = new BmobFi();
        fi.setOnDelete_Batch_SuccessListener(() -> Printer.i("批删除成功"));
        fi.setOnDelete_Batch_Part_FailedListener(urls -> Printer.e("批删除部分失败"));
        fi.setOnDelete_Batch_All_FailedListener(() -> Printer.e("批删除全部失败"));
        fi.deleteBatch(url1, url2);
    }
}
