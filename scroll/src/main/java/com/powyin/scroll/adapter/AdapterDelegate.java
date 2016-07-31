package com.powyin.scroll.adapter;

import java.util.List;

/**
 * Created by powyin on 2016/8/1.
 * // ListAdapter 与 RecycleView.Adatper 公共数据操作接口；
 */
public interface AdapterDelegate<T> {

    // 载入数据
    void loadData(List<T> dataList);

    void deleteFirst();

    void deleteLast();

    // 加入头部数据
    void addFirst(T data);

    void addFirst(List<T> datas);

    // 加入尾部数据
    void addLast(T data);

    void addLast(List<T> dataList);

    // 更新data对应View的数据显示
    void notifyDataChange(T data);

    // 删除数据
    void deleteData(T data);

    // 设置是否展示不合法数据；
    void setShowErrorHolder(boolean show);
}
