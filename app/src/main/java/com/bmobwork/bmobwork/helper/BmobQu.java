package com.bmobwork.bmobwork.helper;

import com.bmobwork.bmobwork.demo.Person;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.CountListener;
import cn.bmob.v3.listener.FindListener;

/*
 * Created by Administrator on 2021/2/8.
 * 该类用于查询操作
 * 
 * 注意: 该类不是直接的操作类, 而是一个模版, 外部开发直接复制该类到业务层使用
 * 使用方式: 全局替换 <Person> 为你需要的表名, 如需要查询Bmob控制台 Order 的表
 * 则全局替换 <Person> 为 <Order> , 另外类名请自己重新命名, 如OrderQuery.java等
 * 
 * // TODO: 2021/2/16  该类需要测试
 */
public class BmobQu extends BmobBase {
    
    private final BmobQuery<Person> query;

    public BmobQu() {
        query = new BmobQuery<Person>();
    }

    /**
     * 发起查询
     */
    public void q_find() {
        query.findObjects(new FindListener<Person>() {
            @Override
            public void done(List<Person> result, BmobException e) {
                if (e == null) {
                    printInfo("BmobDB:q_find()-> 查询成功! 结果个数为: " + result.size());
                    FindSuccessNext(result);
                } else {
                    FindFailedNext();
                    BmobError("BmobDB:q_find()-> 查询失败!", e);
                }
            }
        });
    }

    /**
     * 发起统计
     *
     * @param clazz 需要统计的表
     */
    public void q_count(Class<Person> clazz) {
        query.count(clazz, new CountListener() {
            @Override
            public void done(Integer count, BmobException e) {
                if (e == null) {
                    printInfo("BmobDB:q_count()-> 统计成功! 结果个数为: " + count);
                    CountSuccessNext(count);
                } else {
                    CountFailedNext();
                    BmobError("BmobDB:q_count()-> 统计失败! 错误为: ", e);
                }
            }
        });
    }

    /**
     * 等于
     */
    public BmobQu q_equal(String key, Object value) {
        query.addWhereEqualTo(key, value);
        return this;
    }

    /**
     * 不等于
     */
    public BmobQu q_not_equal(String key, Object value) {
        query.addWhereNotEqualTo(key, value);
        return this;
    }

    /**
     * 小于
     */
    public BmobQu q_less(String key, Object value) {
        query.addWhereLessThan(key, value);
        return this;
    }

    /**
     * 小于等于
     */
    public BmobQu q_less_equal(String key, Object value) {
        query.addWhereLessThanOrEqualTo(key, value);
        return this;
    }

    /**
     * 大于
     */
    public BmobQu q_great(String key, Object value) {
        query.addWhereGreaterThan(key, value);
        return this;
    }

    /**
     * 大于等于
     */
    public BmobQu q_great_equal(String key, Object value) {
        query.addWhereGreaterThanOrEqualTo(key, value);
        return this;
    }

    /**
     * 包含(要查询多个对象)
     *
     * @param key    查询条件
     * @param values 需查询的对象
     */
    public BmobQu q_contain(String key, List<?> values) {
        query.addWhereContainedIn(key, values);
        return this;
    }

    /**
     * 不包含(要查询多个对象)
     *
     * @param key    查询条件
     * @param values 不需查询的对象
     */
    public BmobQu q_not_contain(String key, List<?> values) {
        query.addWhereNotContainedIn(key, values);
        return this;
    }

    /**
     * 包含(字段值是数组)
     *
     * @param key    查询条件
     * @param values 包含的数组值
     */
    public BmobQu q_contains_arr(String key, List<?> values) {
        query.addWhereContainsAll(key, values);
        return this;
    }

    /**
     * 字符串 - 包含 (需充值VIP)
     *
     * @param key   字段
     * @param value 字段值
     */
    public BmobQu q_contains_str(String key, String value) {
        query.addWhereContains(key, value);
        return this;
    }

    /**
     * 字符串 - 开头 (需充值VIP)
     *
     * @param key   字段
     * @param value 字段值
     */
    public BmobQu q_startwith_str(String key, String value) {
        query.addWhereStartsWith(key, value);
        return this;
    }

    /**
     * 字符串 - 结尾 (需充值VIP)
     *
     * @param key   字段
     * @param value 字段值
     */
    public BmobQu q_endwith_str(String key, String value) {
        query.addWhereEndsWith(key, value);
        return this;
    }

    /**
     * 条数 - 每次查询limit个
     *
     * @param limit 个数
     */
    public BmobQu q_limit(int limit) {
        query.setLimit(limit);
        return this;
    }

    /**
     * 跳过 - 每次跳过skip个
     *
     * @param skip 个数
     */
    public BmobQu q_skip(int skip) {
        query.setSkip(skip);
        return this;
    }

    /**
     * 升序
     *
     * @param key 升序字段
     */
    public BmobQu q_order_accent(String key) {
        query.order(key);
        return this;
    }

    /**
     * 降序
     *
     * @param key 降序字段
     */
    public BmobQu q_order_desend(String key) {
        query.order("-" + key);
        return this;
    }

    /**
     * 与
     *
     * @param ands 与集合 (集合中元素间为与关系)
     */
    public BmobQu q_and(List<BmobQuery<Person>> ands) {
        query.and(ands);
        return this;
    }

    /**
     * 或
     *
     * @param ors 或集合 (集合中元素间为或关系)
     */
    public BmobQu q_or(List<BmobQuery<Person>> ors) {
        query.and(ors);
        return this;
    }

    /**
     * 查询指定列
     *
     * @param keys 需要查询的列字段
     */
    public BmobQu q_add_keys(String... keys) {
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            buffer.append(keys[i]);
            if (i != keys.length - 1) {
                buffer.append(",");
            }
        }
        query.addQueryKeys(buffer.toString());
        return this;
    }

    /**
     * 内嵌对象
     *
     * @param include 包含的内嵌对象
     * @apiNote query.include(" author ");
     * @apiNote query.include(" user, post ");
     * @apiNote query.include(" post[likes].author[username | email] ");
     */
    public BmobQu q_include(String include) {
        query.include(include);
        return this;
    }

    /* -------------------------------------------- impl -------------------------------------------- */

    // ---------------- 监听器 [FindSuccess] ----------------
    private OnFindSuccessListener<Person> onFindSuccessListener;

    public interface OnFindSuccessListener<Person> {
        void FindSuccess(List<Person> result);
    }

    public void setOnFindSuccessListener(OnFindSuccessListener<Person> onFindSuccessListener) {
        this.onFindSuccessListener = onFindSuccessListener;
    }

    private void FindSuccessNext(List<Person> result) {
        if (onFindSuccessListener != null) {
            onFindSuccessListener.FindSuccess(result);
        }
    }

    // ---------------- 监听器 [FindFailed] ----------------
    private OnFindFailedListener onFindFailedListener;

    public interface OnFindFailedListener {
        void FindFailed();
    }

    public void setOnFindFailedListener(OnFindFailedListener onFindFailedListener) {
        this.onFindFailedListener = onFindFailedListener;
    }

    private void FindFailedNext() {
        if (onFindFailedListener != null) {
            onFindFailedListener.FindFailed();
        }
    }

    // ---------------- 监听器 [CountSuccess] ----------------
    private OnCountSuccessListener onCountSuccessListener;

    public interface OnCountSuccessListener {
        void CountSuccess(int count);
    }

    public void setOnCountSuccessListener(OnCountSuccessListener onCountSuccessListener) {
        this.onCountSuccessListener = onCountSuccessListener;
    }

    private void CountSuccessNext(int count) {
        if (onCountSuccessListener != null) {
            onCountSuccessListener.CountSuccess(count);
        }
    }

    // ---------------- 监听器 [CountFailed] ----------------
    private OnCountFailedListener onCountFailedListener;

    public interface OnCountFailedListener {
        void CountFailed();
    }

    public void setOnCountFailedListener(OnCountFailedListener onCountFailedListener) {
        this.onCountFailedListener = onCountFailedListener;
    }

    private void CountFailedNext() {
        if (onCountFailedListener != null) {
            onCountFailedListener.CountFailed();
        }
    }

}
