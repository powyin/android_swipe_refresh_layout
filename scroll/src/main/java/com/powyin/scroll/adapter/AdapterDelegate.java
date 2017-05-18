package com.powyin.scroll.adapter;

import java.util.List;

/**
 * Created by powyin on 2016/8/1.
 * // ListAdapter 与 RecycleView.Adatper 公共数据操作接口；
 */
public interface AdapterDelegate<T> {

    enum LoadStatus {
        CONTINUE,
        COMPLITE,
    }

    // 载入数据
    void loadData(List<T> dataList);

    void deleteFirst();

    void deleteLast();

    // 加入头部数据
    void addFirst(T data);

    void addFirst(List<T> datas);

    // 加入尾部数据
    void addLast(T data);
    void addLast(T data, LoadStatus status,  int delayTime);


    void addLast(List<T> dataList);
    void addLast(List<T> dataList,  LoadStatus status, int delayTime);



    List<T> getDataList();



    // 删除数据
    void deleteData(T data);

    // 清空数据
    void deleteAllData();

    // 设置是否展示不合法数据；
    void setShowErrorHolder(boolean show);

    //------------------------------------------------------ 上拉加载--------------------------------------------------------//

    // 设置是否展示加载更多
    void setShowLoadMore(boolean show);

    // 设置加载状态
    void setLoadMoreStatus(LoadStatus status);

    // 设置加载更多监听
    void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener);

    // 设置点击监听;

    void setOnItemClickListener(OnItemClickListener<T> clickListener);

    //------------------------------------------------------ 上拉加载--------------------------------------------------------//

    // 加载更多监听
    interface OnLoadMoreListener {
        void onLoadMore();
    }

    // 点击
    interface OnItemClickListener<T>{
        void onClick(PowViewHolder<T> holder , T data, int index , int resId  );

    }



}





























