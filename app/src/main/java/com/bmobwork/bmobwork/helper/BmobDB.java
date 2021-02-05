package com.bmobwork.bmobwork.helper;

import android.text.TextUtils;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.QueryListListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

/*
 * Created by Administrator on 2021/02/002.
 * 该类用于普通对象操作
 */
public class BmobDB<DBbean extends BmobObject> extends BmobBase {

    /**
     * 保存(单个)
     *
     * @param DBbean 对象
     */
    public void save(@NotNull DBbean DBbean) {
        DBbean.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    printInfo("BmobDB:save()-> 保存成功, ID = " + objectId);
                    SaveOneSuccessNext(objectId);
                } else {
                    SaveOneFailedNext();
                    BmobError("BmobDB:save()-> 保存失败", e);
                }
            }
        });
    }

    /**
     * 保存(批量)
     *
     * @param bmobBeans 对象
     */
    public void saveBatch(@NotNull List<BmobObject> bmobBeans) {
        new BmobBatch().insertBatch(bmobBeans).doBatch(new QueryListListener<BatchResult>() {
            @Override
            public void done(List<BatchResult> results, BmobException e) {
                if (e == null) {
                    List<String> ids = new ArrayList<>();
                    // 检查
                    for (int i = 0; i < results.size(); i++) {
                        BatchResult result = results.get(i);
                        BmobException ex = result.getError();
                        if (ex == null) {
                            printInfo("第 " + i + " 个数据批量添加成功：ID = " + result.getObjectId());
                            ids.add(result.getObjectId());
                        } else {
                            printErr("第" + i + "个数据批量添加失败：MSG = " + ex.getMessage() + ", CODE = " + ex.getErrorCode());
                        }
                    }
                    // 回调
                    if (ids.size() == results.size()) {
                        printInfo("BmobDB:saveBatch()-> 全部添加成功");
                        SaveBatchSuccessNext(ids);
                    } else {
                        printErr("BmobDB:saveBatch()-> 部分添加失败, 失败个数: " + (results.size() - ids.size()));
                        SaveBatchFailedNext();
                    }

                } else {
                    SaveBatchFailedNext();
                    BmobError("BmobDB:saveBatch()-> 批量添加失败", e);
                }
            }
        });
    }

    /**
     * 更新 (单个)
     *
     * @param DBbean 新的对象
     */
    public void update(@NotNull DBbean DBbean) {
        // 需指定ID
        if (TextUtils.isEmpty(DBbean.getObjectId())) {
            UpdateOneNoSetObjectIdNext(DBbean);
            return;
        }
        // 请求
        DBbean.update(DBbean.getObjectId(), new UpdateListener() {
            @Override
            public void done(BmobException e) {
                if (e == null) {
                    printInfo("BmobDB:update()-> 更新成功");
                    UpdateOneSuccessNext();
                } else {
                    UpdateOneFailedNext();
                    BmobError("BmobDB:update()-> 更新失败", e);
                }
            }
        });
    }

    /**
     * 更新(批量)
     *
     * @param bmobBeans 最新对象
     */
    public void updateBatch(@NotNull List<BmobObject> bmobBeans) {
        // 检查
        List<BmobObject> noSet = new ArrayList<>();
        for (BmobObject bmobObject : bmobBeans) {
            if (TextUtils.isEmpty(bmobObject.getObjectId())) {
                noSet.add(bmobObject);
            }
        }

        // 是否有未设置ID值
        if (noSet.size() > 0) {
            UpdateBatchNoSetObjectIdNext(noSet);
            BmobError("BmobDB:updateBatch()-> 部分需要更新的对象没有指定objectId值, 共 [ " + noSet.size() + " ] 个");
            return;
        }

        new BmobBatch().updateBatch(bmobBeans).doBatch(new QueryListListener<BatchResult>() {

            @Override
            public void done(List<BatchResult> results, BmobException e) {
                if (e == null) {
                    List<String> ids = new ArrayList<>();
                    // 检查
                    for (int i = 0; i < results.size(); i++) {
                        BatchResult result = results.get(i);
                        BmobException ex = result.getError();
                        if (ex == null) {
                            printInfo("第 " + i + " 个数据批量更新成功：ID = " + result.getObjectId());
                            ids.add(result.getObjectId());
                        } else {
                            printErr("第" + i + "个数据批量更新失败：MSG = " + ex.getMessage() + ", CODE = " + ex.getErrorCode());
                        }
                    }

                    // 回调
                    if (ids.size() == results.size()) {
                        printInfo("BmobDB:updateBatch()-> 全部更新成功");
                        UpdateBatchSuccessNext(ids);
                    } else {
                        printErr("BmobDB:updateBatch()-> 部分更新失败, 失败个数: " + (results.size() - ids.size()));
                        UpdateBatchFailedNext();
                    }
                } else {
                    UpdateBatchFailedNext();
                    BmobError("BmobDB:updateBatch()-> 批量更新失败", e);
                }
            }
        });
    }

    /**
     * 删除 (单个)
     *
     * @param objectId 需要删除的对象ID
     * @param clazz    操作的表类
     */
    public void delete(@NotNull Class<? extends BmobObject> clazz, @NotNull String objectId) {
        try {
            // 创建
            BmobObject bmobBean = clazz.newInstance();
            // 发起
            bmobBean.delete(objectId, new UpdateListener() {
                @Override
                public void done(BmobException e) {
                    if (e == null) {
                        printInfo("BmobDB:delete()-> 删除成功");
                        DeleteSuccessNext();
                    } else {
                        DeleteOneFailedNext();
                        BmobError("BmobDB:delete()-> 删除失败", e);
                    }
                }
            });
        } catch (Exception e) {
            DeleteOneFailedNext();
            BmobError("BmobDB:delete()-> 删除失败", e);
        }
    }

    /**
     * 删除(批量)
     *
     * @param bmobBeans 需要删除的对象
     */
    public void deleteBatch(List<BmobObject> bmobBeans) {
        // 检查
        List<BmobObject> noSet = new ArrayList<>();
        for (BmobObject bmobObject : bmobBeans) {
            if (TextUtils.isEmpty(bmobObject.getObjectId())) {
                noSet.add(bmobObject);
            }
        }

        // 是否有未设置ID值
        if (noSet.size() > 0) {
            DeleteBatchNoSetObjectIdNext(noSet);
            BmobError("BmobDB:updateBatch()-> 部分需要删除的对象没有指定objectId值, 共 [ " + noSet.size() + " ] 个");
            return;
        }

        // 发起
        new BmobBatch().deleteBatch(bmobBeans).doBatch(new QueryListListener<BatchResult>() {

            @Override
            public void done(List<BatchResult> results, BmobException e) {
                if (e == null) {
                    List<String> ids = new ArrayList<>();
                    // 检查
                    for (int i = 0; i < results.size(); i++) {
                        BatchResult result = results.get(i);
                        BmobException ex = result.getError();
                        if (ex == null) {
                            printInfo("第 " + i + " 个数据批量删除成功：ID = " + result.getObjectId());
                            ids.add(result.getObjectId());
                        } else {
                            printErr("第" + i + "个数据批量删除失败：MSG = " + ex.getMessage() + ", CODE = " + ex.getErrorCode());
                        }
                    }
                    // 回调
                    if (ids.size() == results.size()) {
                        printInfo("BmobDB:deleteBatch()-> 全部删除成功");
                        DeleteBatchSuccessNext(ids);
                    } else {
                        printErr("BmobDB:deleteBatch()-> 部分删除失败, 失败个数: " + (results.size() - ids.size()));
                        DeleteBatchFailedNext();
                    }
                } else {
                    DeleteBatchFailedNext();
                    BmobError("BmobDB:updateBatch()-> 批量删除失败", e);
                }
            }
        });
    }

    /**
     * 新增、更新、删除(同时操作)
     *
     * @param saveBeans   需新增的集合
     * @param updateBeans 需更新的集合
     * @param deleteBeans 需删除的集合
     */
    public void doBatch(// 批量操作
                        @NotNull List<BmobObject> saveBeans,// 需新增的集合
                        @NotNull List<BmobObject> updateBeans,// 需更新的集合
                        @NotNull List<BmobObject> deleteBeans// 需删除的集合
    ) {
        // 创建
        BmobBatch batch = new BmobBatch();

        // * 对新增集合的操作
        if (saveBeans.size() > 0) {
            batch.insertBatch(saveBeans);
        }

        // * 对更新集合的操作
        if (updateBeans.size() > 0) {
            // 检查
            List<BmobObject> updateNoSet = new ArrayList<>();
            for (BmobObject bmobObject : updateBeans) {
                if (TextUtils.isEmpty(bmobObject.getObjectId())) {
                    updateNoSet.add(bmobObject);
                }
            }

            // 是否有未设置ID值
            if (updateNoSet.size() > 0) {
                DoBatchNoSetObjectIdNext(updateNoSet);
                BmobError("BmobDB:updateBatch()-> 部分需要更新的对象没有指定objectId值, 共 [ " + updateNoSet.size() + " ] 个");
                return;
            }

            // 设置
            batch.updateBatch(updateBeans);
        }

        // * 对删除集合的操作
        if (deleteBeans.size() > 0) {
            // 检查
            List<BmobObject> deleteNoSet = new ArrayList<>();
            for (BmobObject bmobObject : deleteBeans) {
                if (TextUtils.isEmpty(bmobObject.getObjectId())) {
                    deleteNoSet.add(bmobObject);
                }
            }

            // 是否有未设置ID值
            if (deleteNoSet.size() > 0) {
                DoBatchNoSetObjectIdNext(deleteNoSet);
                BmobError("BmobDB:updateBatch()-> 部分需要删除的对象没有指定objectId值, 共 [ " + deleteNoSet.size() + " ] 个");
                return;
            }

            // 设置
            batch.deleteBatch(deleteBeans);
        }

        // 请求
        batch.doBatch(new QueryListListener<BatchResult>() {

            @Override
            public void done(List<BatchResult> results, BmobException e) {
                if (e == null) {
                    List<String> ids = new ArrayList<>();
                    // 返回结果的results和上面提交的顺序是一样的，请一一对应
                    for (int i = 0; i < results.size(); i++) {
                        BatchResult result = results.get(i);
                        BmobException ex = result.getError();
                        // 只有批量添加才返回objectId
                        if (ex == null) {
                            printInfo("第" + i + "个数据批量操作成功：ID = " + result.getObjectId());
                            ids.add(result.getObjectId());
                        } else {
                            printErr("第" + i + "个数据批量操作失败：MSG = " + ex.getMessage() + ", CODE = " + ex.getErrorCode());
                        }
                    }
                    // 回调
                    if (ids.size() == results.size()) {
                        printInfo("BmobDB:doBatch()-> 全部操作成功");
                        DoBatchSuccessNext(ids);
                    } else {
                        printErr("BmobDB:doBatch()-> 部分操作失败, 失败个数: " + (results.size() - ids.size()));
                        DoBatchFailedNext();
                    }
                } else {
                    DoBatchFailedNext();
                    BmobError("BmobDB:doBatch()-> 批量操作失败", e);
                }
            }
        });

    }

    /* -------------------------------------------- impl -------------------------------------------- */

    // ---------------- 监听器 [SaveSuccess] ----------------
    private OnSaveOneSuccessListener onSaveOneSuccessListener;

    public interface OnSaveOneSuccessListener {
        void SaveSuccess(String objectId);
    }

    public void setOnSaveOneSuccessListener(OnSaveOneSuccessListener onSaveOneSuccessListener) {
        this.onSaveOneSuccessListener = onSaveOneSuccessListener;
    }

    private void SaveOneSuccessNext(String objectId) {
        if (onSaveOneSuccessListener != null) {
            onSaveOneSuccessListener.SaveSuccess(objectId);
        }
    }

    // ---------------- 监听器 [SaveFailed] ----------------
    private OnSaveOneFailedListener onSaveOneFailedListener;

    public interface OnSaveOneFailedListener {
        void SaveFailed();
    }

    public void setOnSaveOneFailedListener(OnSaveOneFailedListener onSaveOneFailedListener) {
        this.onSaveOneFailedListener = onSaveOneFailedListener;
    }

    private void SaveOneFailedNext() {
        if (onSaveOneFailedListener != null) {
            onSaveOneFailedListener.SaveFailed();
        }
    }

    // ---------------- 监听器 [SaveBatchSuccess] ----------------
    private OnSaveBatchSuccessListener onSaveBatchSuccessListener;

    public interface OnSaveBatchSuccessListener {
        void SaveBatchSuccess(List<String> objectIds);
    }

    public void setOnSaveBatchSuccessListener(OnSaveBatchSuccessListener onSaveBatchSuccessListener) {
        this.onSaveBatchSuccessListener = onSaveBatchSuccessListener;
    }

    private void SaveBatchSuccessNext(List<String> objectIds) {
        if (onSaveBatchSuccessListener != null) {
            onSaveBatchSuccessListener.SaveBatchSuccess(objectIds);
        }
    }

    // ---------------- 监听器 [SaveBatchFailed] ----------------
    private OnSaveBatchFailedListener onSaveBatchFailedListener;

    public interface OnSaveBatchFailedListener {
        void SaveBatchFailed();
    }

    public void setOnSaveBatchFailedListener(OnSaveBatchFailedListener onSaveBatchFailedListener) {
        this.onSaveBatchFailedListener = onSaveBatchFailedListener;
    }

    private void SaveBatchFailedNext() {
        if (onSaveBatchFailedListener != null) {
            onSaveBatchFailedListener.SaveBatchFailed();
        }
    }

    // ---------------- 监听器 [UpdateSuccess] ----------------
    private OnUpdateOneSuccessListener onUpdateOneSuccessListener;

    public interface OnUpdateOneSuccessListener {
        void UpdateSuccess();
    }

    public void setOnUpdateOneSuccessListener(OnUpdateOneSuccessListener onUpdateOneSuccessListener) {
        this.onUpdateOneSuccessListener = onUpdateOneSuccessListener;
    }

    private void UpdateOneSuccessNext() {
        if (onUpdateOneSuccessListener != null) {
            onUpdateOneSuccessListener.UpdateSuccess();
        }
    }

    // ---------------- 监听器 [UpdateFailed] ----------------
    private OnUpdateOneFailedListener onUpdateOneFailedListener;

    public interface OnUpdateOneFailedListener {
        void UpdateFailed();
    }

    public void setOnUpdateOneFailedListener(OnUpdateOneFailedListener onUpdateOneFailedListener) {
        this.onUpdateOneFailedListener = onUpdateOneFailedListener;
    }

    private void UpdateOneFailedNext() {
        if (onUpdateOneFailedListener != null) {
            onUpdateOneFailedListener.UpdateFailed();
        }
    }

    // ---------------- 监听器 [UpdateNoSetObjectId] ----------------
    private OnUpdateOneNoSetObjectIdListener onUpdateOneNoSetObjectIdListener;

    public interface OnUpdateOneNoSetObjectIdListener {
        void UpdateNoSetObjectId(BmobObject bmobBean);
    }

    public void setOnUpdateOneNoSetObjectIdListener(OnUpdateOneNoSetObjectIdListener onUpdateOneNoSetObjectIdListener) {
        this.onUpdateOneNoSetObjectIdListener = onUpdateOneNoSetObjectIdListener;
    }

    private void UpdateOneNoSetObjectIdNext(BmobObject bmobBean) {
        if (onUpdateOneNoSetObjectIdListener != null) {
            onUpdateOneNoSetObjectIdListener.UpdateNoSetObjectId(bmobBean);
        }
    }

    // ---------------- 监听器 [UpdateBatchSuccess] ----------------
    private OnUpdateBatchSuccessListener onUpdateBatchSuccessListener;

    public interface OnUpdateBatchSuccessListener {
        void UpdateBatchSuccess(List<String> objectIds);
    }

    public void setOnUpdateBatchSuccessListener(OnUpdateBatchSuccessListener onUpdateBatchSuccessListener) {
        this.onUpdateBatchSuccessListener = onUpdateBatchSuccessListener;
    }

    private void UpdateBatchSuccessNext(List<String> objectIds) {
        if (onUpdateBatchSuccessListener != null) {
            onUpdateBatchSuccessListener.UpdateBatchSuccess(objectIds);
        }
    }

    // ---------------- 监听器 [UpdateBatchFailed] ----------------
    private OnUpdateBatchFailedListener onUpdateBatchFailedListener;

    public interface OnUpdateBatchFailedListener {
        void UpdateBatchFailed();
    }

    public void setOnUpdateBatchFailedListener(OnUpdateBatchFailedListener onUpdateBatchFailedListener) {
        this.onUpdateBatchFailedListener = onUpdateBatchFailedListener;
    }

    private void UpdateBatchFailedNext() {
        if (onUpdateBatchFailedListener != null) {
            onUpdateBatchFailedListener.UpdateBatchFailed();
        }
    }

    // ---------------- 监听器 [UpdateNoSetObjectId] ----------------
    private OnUpdateBatchNoSetObjectIdListener onUpdateBatchNoSetObjectIdListener;

    public interface OnUpdateBatchNoSetObjectIdListener {
        void UpdateNoSetObjectId(List<BmobObject> bmobObjects);
    }

    public void setOnUpdateBatchNoSetObjectIdListener(OnUpdateBatchNoSetObjectIdListener onUpdateBatchNoSetObjectIdListener) {
        this.onUpdateBatchNoSetObjectIdListener = onUpdateBatchNoSetObjectIdListener;
    }

    private void UpdateBatchNoSetObjectIdNext(List<BmobObject> bmobObjects) {
        if (onUpdateBatchNoSetObjectIdListener != null) {
            onUpdateBatchNoSetObjectIdListener.UpdateNoSetObjectId(bmobObjects);
        }
    }

    // ---------------- 监听器 [DeleteSuccess] ----------------
    private OnDeleteOneSuccessListener onDeleteOneSuccessListener;

    public interface OnDeleteOneSuccessListener {
        void DeleteSuccess();
    }

    public void setOnDeleteOneSuccessListener(OnDeleteOneSuccessListener onDeleteOneSuccessListener) {
        this.onDeleteOneSuccessListener = onDeleteOneSuccessListener;
    }

    private void DeleteSuccessNext() {
        if (onDeleteOneSuccessListener != null) {
            onDeleteOneSuccessListener.DeleteSuccess();
        }
    }

    // ---------------- 监听器 [DeleteFailed] ----------------
    private OnDeleteOneFailedListener onDeleteOneFailedListener;

    public interface OnDeleteOneFailedListener {
        void DeleteFailed();
    }

    public void setOnDeleteOneFailedListener(OnDeleteOneFailedListener onDeleteOneFailedListener) {
        this.onDeleteOneFailedListener = onDeleteOneFailedListener;
    }

    private void DeleteOneFailedNext() {
        if (onDeleteOneFailedListener != null) {
            onDeleteOneFailedListener.DeleteFailed();
        }
    }

    // ---------------- 监听器 [DeleteBatchSuccess] ----------------
    private OnDeleteBatchSuccessListener onDeleteBatchSuccessListener;

    public interface OnDeleteBatchSuccessListener {
        void DeleteBatchSuccess(List<String> ids);
    }

    public void setOnDeleteBatchSuccessListener(OnDeleteBatchSuccessListener onDeleteBatchSuccessListener) {
        this.onDeleteBatchSuccessListener = onDeleteBatchSuccessListener;
    }

    private void DeleteBatchSuccessNext(List<String> ids) {
        if (onDeleteBatchSuccessListener != null) {
            onDeleteBatchSuccessListener.DeleteBatchSuccess(ids);
        }
    }

    // ---------------- 监听器 [DeleteBatchFailed] ----------------
    private OnDeleteBatchFailedListener onDeleteBatchFailedListener;

    public interface OnDeleteBatchFailedListener {
        void DeleteBatchFailed();
    }

    public void setOnDeleteBatchFailedListener(OnDeleteBatchFailedListener onDeleteBatchFailedListener) {
        this.onDeleteBatchFailedListener = onDeleteBatchFailedListener;
    }

    private void DeleteBatchFailedNext() {
        if (onDeleteBatchFailedListener != null) {
            onDeleteBatchFailedListener.DeleteBatchFailed();
        }
    }

    // ---------------- 监听器 [DeleteBatchNoSetObjectId] ----------------
    private OnDeleteBatchNoSetObjectIdListener onDeleteBatchNoSetObjectIdListener;

    public interface OnDeleteBatchNoSetObjectIdListener {
        void DeleteBatchNoSetObjectId(List<BmobObject> bmobBeans);
    }

    public void setOnDeleteBatchNoSetObjectIdListener(OnDeleteBatchNoSetObjectIdListener onDeleteBatchNoSetObjectIdListener) {
        this.onDeleteBatchNoSetObjectIdListener = onDeleteBatchNoSetObjectIdListener;
    }

    private void DeleteBatchNoSetObjectIdNext(List<BmobObject> bmobBeans) {
        if (onDeleteBatchNoSetObjectIdListener != null) {
            onDeleteBatchNoSetObjectIdListener.DeleteBatchNoSetObjectId(bmobBeans);
        }
    }

    // ---------------- 监听器 [DoBatchSuccess] ----------------
    private OnDoBatchSuccessListener onDoBatchSuccessListener;

    public interface OnDoBatchSuccessListener {
        void DoBatchSuccess(List<String> ids);
    }

    public void setOnDoBatchSuccessListener(OnDoBatchSuccessListener onDoBatchSuccessListener) {
        this.onDoBatchSuccessListener = onDoBatchSuccessListener;
    }

    private void DoBatchSuccessNext(List<String> ids) {
        if (onDoBatchSuccessListener != null) {
            onDoBatchSuccessListener.DoBatchSuccess(ids);
        }
    }

    // ---------------- 监听器 [DoBatchFailed] ----------------
    private OnDoBatchFailedListener onDoBatchFailedListener;

    public interface OnDoBatchFailedListener {
        void DoBatchFailed();
    }

    public void setOnDoBatchFailedListener(OnDoBatchFailedListener onDoBatchFailedListener) {
        this.onDoBatchFailedListener = onDoBatchFailedListener;
    }

    private void DoBatchFailedNext() {
        if (onDoBatchFailedListener != null) {
            onDoBatchFailedListener.DoBatchFailed();
        }
    }

    // ---------------- 监听器 [DoBatchNoSetObjectId] ----------------
    private OnDoBatchNoSetObjectIdListener onDoBatchNoSetObjectIdListener;

    public interface OnDoBatchNoSetObjectIdListener {
        void DoBatchNoSetObjectId(List<BmobObject> bmobBeans);
    }

    public void setOnDoBatchNoSetObjectIdListener(OnDoBatchNoSetObjectIdListener onDoBatchNoSetObjectIdListener) {
        this.onDoBatchNoSetObjectIdListener = onDoBatchNoSetObjectIdListener;
    }

    private void DoBatchNoSetObjectIdNext(List<BmobObject> bmobBeans) {
        if (onDoBatchNoSetObjectIdListener != null) {
            onDoBatchNoSetObjectIdListener.DoBatchNoSetObjectId(bmobBeans);
        }
    }
}
