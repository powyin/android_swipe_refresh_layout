package com.powyin.scroll.widget;

import android.support.v7.widget.RecyclerView;
import android.widget.ListView;

/**
 * Created by powyin on 2017/4/17.  用于控制刷新结果
 */

public interface ISwipe {

    enum LoadedStatus{
        CONTINUE,
        ERROR,
        NO_MORE
    }

    enum RefreshStatus{
        CONTINUE,
        ERROR_AUTO_CANCEL,
        ERROR_FIXED,
        SUCCESS
    }


    // 设置刷新控制监听
    public void setOnRefreshListener(OnRefreshListener onRefreshListener);


    // 设置下拉刷新结果
    public void setFreshStatue ( RefreshStatus statue);


    // 设置上拉加载结果
    public void setLoadMoreStatus( LoadedStatus status);


    // 设置刷新模式
    public void setSwipeModel(SwipeControl.SwipeModel model);

    // 设置自定义刷新视图
    public void setSwipeControl(SwipeControl control);

    public interface OnRefreshListener {
        // 头部刷新开始
        void onRefresh();

        // 加载更多开始
        void onLoading();
    }
}
