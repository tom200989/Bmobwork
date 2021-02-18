package com.bmobwork.bmobwork.helper;

import android.text.TextUtils;

import java.io.File;
import java.util.List;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.DeleteBatchListener;
import cn.bmob.v3.listener.DownloadFileListener;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadBatchListener;
import cn.bmob.v3.listener.UploadFileListener;

/*
 * Created by Administrator on 2021/02/005.
 * 该类用于文件操作
 */
public class BmobFi extends BmobBase {

    /**
     * 上传(单个)
     *
     * @param path 本地路径
     */
    public void upload(String path) {

        // 路径判断
        if (TextUtils.isEmpty(path)) {
            printErr("BmobFi:uploadSingle()-> 当前路径不合法");
            return;
        }
        // 请求
        BmobFile bmobFile = new BmobFile(new File(path));
        bmobFile.uploadblock(new UploadFileListener() {

            @Override
            public void done(BmobException e) {
                if (e == null) {
                    String url = bmobFile.getFileUrl();
                    printInfo("BmobFi:uploadSingle()-> 上传文件成功: Url = " + url);
                    UploadSuccessNext(url);
                } else {
                    UploadFailedNext();
                    BmobError("BmobFi:uploadSingle()-> 上传文件失败", e);
                }

            }

            @Override
            public void onProgress(Integer progress) {
                UploadProgressNext(progress);
            }
        });
    }

    /**
     * 下载(单个)
     *
     * @param bmobFile 需要下载的文件
     * @param savePath 保存到哪里
     */
    public void download(BmobFile bmobFile, String savePath) {
        // 需要保存的路径
        File saveFile = new File(savePath);
        bmobFile.download(saveFile, new DownloadFileListener() {

            @Override
            public void onStart() {
                printInfo("BmobFi:download()-> 开始下载");
                DownloadStartNext();
            }

            @Override
            public void done(String savePath, BmobException e) {
                if (e == null) {
                    printInfo("BmobFi:download()-> 下载成功,保存路径:" + savePath);
                    DownloadSuccessNext(savePath);
                } else {
                    DownloadFailedNext();
                    BmobError("BmobFi:download()-> 下载失败", e);
                }
            }

            @Override
            public void onProgress(Integer progress, long netWorkSpeed) {
                DownloadProgressNext(progress, netWorkSpeed);
            }

        });
    }

    /**
     * 删除(单个)
     *
     * @param url 需要删除的图片链接
     * @apiNote 上传文件成功后通过bmobFile.getUrl()方法获取
     */
    public void delete(String url) {
        BmobFile file = new BmobFile();
        file.setUrl(url);// 此url是的。
        file.delete(new UpdateListener() {

            @Override
            public void done(BmobException e) {
                if (e == null) {
                    printInfo("BmobFi:delete()-> 删除成功");
                    DeleteSuccessNext();
                } else {
                    DeleteFailedNext();
                    BmobError("BmobFi:delete()-> 删除失败", e);
                }
            }
        });
    }

    /**
     * 上传(批量)
     *
     * @param filePaths 上传路径集合
     */
    public void uploadBatch(String... filePaths) {

        BmobFile.uploadBatch(filePaths, new UploadBatchListener() {
            @Override
            public void onSuccess(List<BmobFile> files, List<String> urls) {
                // 数量相等 - 文件全部上传完成 (每上传一个, 该方法就回调1次)
                if (urls.size() == filePaths.length) {
                    printInfo("BmobFi:uploadBatch()-> 批上传成功, 已上传 " + files.size() + " 个文件");
                    Upload_Batch_SuccessNext(files, urls);
                }
            }

            @Override
            public void onError(int statuscode, String errormsg) {
                Upload_Batch_FailedNext();
                BmobError("BmobFi:uploadBatch()-> 批上传失败, CODE = " + statuscode + "; MSG = " + errormsg);
            }

            @Override
            public void onProgress(int curIndex, int curPercent, int total, int totalPercent) {
                //1、curIndex--表示当前第几个文件正在上传
                //2、curPercent--表示当前上传文件的进度值（百分比）
                //3、total--表示总的上传文件数
                //4、totalPercent--表示总的上传进度（百分比）
                Upload_Batch_ProgressNext(curIndex, curPercent, total, totalPercent);
            }
        });
    }

    /**
     * 删除(批量)
     *
     * @param urls 需要删除的路径
     */
    public void deleteBatch(String... urls) {
        BmobFile.deleteBatch(urls, new DeleteBatchListener() {

            @Override
            public void done(String[] failUrls, BmobException e) {
                if (e == null) {
                    printInfo("BmobFi:deleteBatch()-> 批删除成功");
                    Delete_Batch_SuccessNext();
                } else if (failUrls != null) {
                    printErr("BmobFi:deleteBatch()-> 批删除部分失败, 个数: " + failUrls.length + " 个");
                    Delete_Batch_Part_FailedNext(failUrls);
                } else {
                    printErr("BmobFi:deleteBatch()-> 批删除全部失败");
                    Delete_Batch_All_FailedNext();
                }
            }
        });
    }

    /* -------------------------------------------- impl -------------------------------------------- */

    // ---------------- 监听器 [UploadSingleSuccess] ----------------
    private OnUploadSuccessListener onUploadSuccessListener;

    public interface OnUploadSuccessListener {
        void UploadSingleSuccess(String url);
    }

    public void setOnUploadSuccessListener(OnUploadSuccessListener onUploadSuccessListener) {
        this.onUploadSuccessListener = onUploadSuccessListener;
    }

    private void UploadSuccessNext(String url) {
        if (onUploadSuccessListener != null) {
            onUploadSuccessListener.UploadSingleSuccess(url);
        }
    }

    // ---------------- 监听器 [UploadSingleFailed] ----------------
    private OnUploadFailedListener onUploadFailedListener;

    public interface OnUploadFailedListener {
        void UploadSingleFailed();
    }

    public void setOnUploadFailedListener(OnUploadFailedListener onUploadFailedListener) {
        this.onUploadFailedListener = onUploadFailedListener;
    }

    private void UploadFailedNext() {
        if (onUploadFailedListener != null) {
            onUploadFailedListener.UploadSingleFailed();
        }
    }

    // ---------------- 监听器 [UploadSingleProgress] ----------------
    private OnUploadProgressListener onUploadProgressListener;

    public interface OnUploadProgressListener {
        void UploadSingleProgress(int progress);
    }

    public void setOnUploadProgressListener(OnUploadProgressListener onUploadProgressListener) {
        this.onUploadProgressListener = onUploadProgressListener;
    }

    private void UploadProgressNext(int progress) {
        if (onUploadProgressListener != null) {
            onUploadProgressListener.UploadSingleProgress(progress);
        }
    }

    // ---------------- 监听器 [DownloadStart] ----------------
    private OnDownloadStartListener onDownloadStartListener;

    public interface OnDownloadStartListener {
        void DownloadStart();
    }

    public void setOnDownloadStartListener(OnDownloadStartListener onDownloadStartListener) {
        this.onDownloadStartListener = onDownloadStartListener;
    }

    private void DownloadStartNext() {
        if (onDownloadStartListener != null) {
            onDownloadStartListener.DownloadStart();
        }
    }

    // ---------------- 监听器 [DownloadSuccess] ----------------
    private OnDownloadSuccessListener onDownloadSuccessListener;

    public interface OnDownloadSuccessListener {
        void DownloadSuccess(String savePath);
    }

    public void setOnDownloadSuccessListener(OnDownloadSuccessListener onDownloadSuccessListener) {
        this.onDownloadSuccessListener = onDownloadSuccessListener;
    }

    private void DownloadSuccessNext(String savePath) {
        if (onDownloadSuccessListener != null) {
            onDownloadSuccessListener.DownloadSuccess(savePath);
        }
    }

    // ---------------- 监听器 [DownloadFailed] ----------------
    private OnDownloadFailedListener onDownloadFailedListener;

    public interface OnDownloadFailedListener {
        void DownloadFailed();
    }

    public void setOnDownloadFailedListener(OnDownloadFailedListener onDownloadFailedListener) {
        this.onDownloadFailedListener = onDownloadFailedListener;
    }

    private void DownloadFailedNext() {
        if (onDownloadFailedListener != null) {
            onDownloadFailedListener.DownloadFailed();
        }
    }

    // ---------------- 监听器 [DownloadProgress] ----------------
    private OnDownloadProgressListener onDownloadProgressListener;

    public interface OnDownloadProgressListener {
        void DownloadProgress(int progress, long netWorkSpeed);
    }

    public void setOnDownloadProgressListener(OnDownloadProgressListener onDownloadProgressListener) {
        this.onDownloadProgressListener = onDownloadProgressListener;
    }

    private void DownloadProgressNext(int progress, long netWorkSpeed) {
        if (onDownloadProgressListener != null) {
            onDownloadProgressListener.DownloadProgress(progress, netWorkSpeed);
        }
    }

    // ---------------- 监听器 [DeleteSuccess] ----------------
    private OnDeleteSuccessListener onDeleteSuccessListener;

    public interface OnDeleteSuccessListener {
        void DeleteSuccess();
    }

    public void setOnDeleteSuccessListener(OnDeleteSuccessListener onDeleteSuccessListener) {
        this.onDeleteSuccessListener = onDeleteSuccessListener;
    }

    private void DeleteSuccessNext() {
        if (onDeleteSuccessListener != null) {
            onDeleteSuccessListener.DeleteSuccess();
        }
    }

    // ---------------- 监听器 [DeleteFailed] ----------------
    private OnDeleteFailedListener onDeleteFailedListener;

    public interface OnDeleteFailedListener {
        void DeleteFailed();
    }

    public void setOnDeleteFailedListener(OnDeleteFailedListener onDeleteFailedListener) {
        this.onDeleteFailedListener = onDeleteFailedListener;
    }

    private void DeleteFailedNext() {
        if (onDeleteFailedListener != null) {
            onDeleteFailedListener.DeleteFailed();
        }
    }

    // ---------------- 监听器 [Upload_Batch_Success] ----------------
    private OnUpload_Batch_SuccessListener onUpload_Batch_SuccessListener;

    public interface OnUpload_Batch_SuccessListener {
        void Upload_Batch_Success(List<BmobFile> files, List<String> urls);
    }

    public void setOnUpload_Batch_SuccessListener(OnUpload_Batch_SuccessListener onUpload_Batch_SuccessListener) {
        this.onUpload_Batch_SuccessListener = onUpload_Batch_SuccessListener;
    }

    private void Upload_Batch_SuccessNext(List<BmobFile> files, List<String> urls) {
        if (onUpload_Batch_SuccessListener != null) {
            onUpload_Batch_SuccessListener.Upload_Batch_Success(files, urls);
        }
    }

    // ---------------- 监听器 [Upload_Batch_Failed] ----------------
    private OnUpload_Batch_FailedListener onUpload_Batch_FailedListener;

    public interface OnUpload_Batch_FailedListener {
        void Upload_Batch_Failed();
    }

    public void setOnUpload_Batch_FailedListener(OnUpload_Batch_FailedListener onUpload_Batch_FailedListener) {
        this.onUpload_Batch_FailedListener = onUpload_Batch_FailedListener;
    }

    private void Upload_Batch_FailedNext() {
        if (onUpload_Batch_FailedListener != null) {
            onUpload_Batch_FailedListener.Upload_Batch_Failed();
        }
    }

    // ---------------- 监听器 [Upload_Batch_Progress] ----------------
    private OnUpload_Batch_ProgressListener onUpload_Batch_ProgressListener;

    public interface OnUpload_Batch_ProgressListener {
        void Upload_Batch_Progress(int curIndex, int curPercent, int total, int totalPercent);
    }

    public void setOnUpload_Batch_ProgressListener(OnUpload_Batch_ProgressListener onUpload_Batch_ProgressListener) {
        this.onUpload_Batch_ProgressListener = onUpload_Batch_ProgressListener;
    }

    private void Upload_Batch_ProgressNext(int curIndex, int curPercent, int total, int totalPercent) {
        if (onUpload_Batch_ProgressListener != null) {
            onUpload_Batch_ProgressListener.Upload_Batch_Progress(curIndex, curPercent, total, totalPercent);
        }
    }

    // ---------------- 监听器 [Delete_Batch_Success] ----------------
    private OnDelete_Batch_SuccessListener onDelete_Batch_SuccessListener;

    public interface OnDelete_Batch_SuccessListener {
        void Delete_Batch_Success();
    }

    public void setOnDelete_Batch_SuccessListener(OnDelete_Batch_SuccessListener onDelete_Batch_SuccessListener) {
        this.onDelete_Batch_SuccessListener = onDelete_Batch_SuccessListener;
    }

    private void Delete_Batch_SuccessNext() {
        if (onDelete_Batch_SuccessListener != null) {
            onDelete_Batch_SuccessListener.Delete_Batch_Success();
        }
    }

    // ---------------- 监听器 [Delete_Batch_Part_Failed] ----------------
    private OnDelete_Batch_Part_FailedListener onDelete_Batch_Part_FailedListener;

    public interface OnDelete_Batch_Part_FailedListener {
        void Delete_Batch_Part_Failed(String... urls);
    }

    public void setOnDelete_Batch_Part_FailedListener(OnDelete_Batch_Part_FailedListener onDelete_Batch_Part_FailedListener) {
        this.onDelete_Batch_Part_FailedListener = onDelete_Batch_Part_FailedListener;
    }

    private void Delete_Batch_Part_FailedNext(String... urls) {
        if (onDelete_Batch_Part_FailedListener != null) {
            onDelete_Batch_Part_FailedListener.Delete_Batch_Part_Failed(urls);
        }
    }

    // ---------------- 监听器 [Delete_Batch_All_Failed] ----------------
    private OnDelete_Batch_All_FailedListener onDelete_Batch_All_FailedListener;

    public interface OnDelete_Batch_All_FailedListener {
        void Delete_Batch_All_Failed();
    }

    public void setOnDelete_Batch_All_FailedListener(OnDelete_Batch_All_FailedListener onDelete_Batch_All_FailedListener) {
        this.onDelete_Batch_All_FailedListener = onDelete_Batch_All_FailedListener;
    }

    private void Delete_Batch_All_FailedNext() {
        if (onDelete_Batch_All_FailedListener != null) {
            onDelete_Batch_All_FailedListener.Delete_Batch_All_Failed();
        }
    }

}
