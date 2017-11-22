package com.powyin.scroll.adapter;

import android.view.View;

import java.util.List;

/**
 * Created by powyin on 2016/8/1.
 * // ListAdapter 与 RecycleView.Adatper 公共数据操作接口；
 */
public interface AdapterDelegate<T> {

    // 加载更多监听
    interface OnLoadMoreListener {
        void onLoadMore();
    }

    // 点击
    interface OnItemClickListener<T>{
        void onClick(PowViewHolder<T> holder, T data, int index, int resId);

    }

    // 长按点击
    interface OnItemLongClickListener<T>{
        boolean onLongClick(PowViewHolder<T> holder, T data, int index, int resId);
    }

    // 加载更多状态枚举
    enum LoadedStatus{
        ERROR,                                                          //上拉加载失败
        NO_MORE,                                                        //数据全部加载完毕
    }

    //------------------------------------------------------ 数据配置--------------------------------------------------------//

    // 获取数据源
    List<T> getDataList();
    // 获取数据数量
    int getDataCount();

    // 载入数据
    void loadData(List<T> dataList);

    // 添加数据
    void addData(int position, T data);
    void addData(int position, List<T> dataList);

    void addDataAtLast(List<T> dataList);

    // 加入尾部数据
    void addDataAtLast(List<T> dataList, LoadedStatus status, int delayTime);

    // 删除数据
    T removeData(int position);
    void removeData(T data);

    // 清空数据
    void clearData();

    //------------------------------------------------------ 数据配置--------------------------------------------------------//

    //------------------------------------------------------ 上拉加载--------------------------------------------------------//
    // 设置是否展示加载更多
    void enableLoadMore(boolean enable);

    // 设置加载状态
    void setLoadMoreStatus(LoadedStatus status);

    // 手动调用加载更多
    void loadMore();

    // 清除上拉加载中状态
    void completeLoadMore();

    // 设置加载更多监听
    void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener);

    //------------------------------------------------------ 上拉加载--------------------------------------------------------//

    // 加入头部
    void setHeadView(View view);
    // 加入尾部;
    void setFootView(View view);

    // 删除头部
    void removeHeadView();
    // 删除尾部;
    void removeFootView();

    // 显示空白页面
    void enableEmptyView(boolean show);
    // 设置空白页面
    void setEmptyView(View view);

    // 设置点击监听;
    void setOnItemClickListener(OnItemClickListener<T> clickListener);
    // 设置长按点击监听;
    void setOnItemLongClickListener(OnItemLongClickListener<T> clickListener);


}





























