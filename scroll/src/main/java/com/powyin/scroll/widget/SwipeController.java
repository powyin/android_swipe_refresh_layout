package com.powyin.scroll.widget;

import android.view.View;

/**
 * Created by powyin on 2016/7/2.  // 用于刷新视图实现控制
 */
public interface SwipeController {
    enum SwipeModel {
        SWIPE_BOTH,
        SWIPE_ONLY_REFRESH,
        SWIPE_ONLY_LOADINN,
        SWIPE_NONE
    }

    enum SwipeStatus {
        // 上拉刷新
        SWIPE_HEAD_OVER,                           //提示: 松开刷新
        SWIPE_HEAD_TOAST,                          //提示: 下拉刷新
        SWIPE_HEAD_LOADING,                        //提示: 刷新中
        SWIPE_HEAD_COMPLETE_OK,                    //提示: 刷新完成
        SWIPE_HEAD_COMPLETE_ERROR,     //提示: 刷新失败  再次下拉自动重置
        SWIPE_HEAD_COMPLETE_ERROR_NET,           //提示: 刷新失败  无法自动重置
        // 下拉加载
        SWIPE_LOAD_LOADING,
        SWIPE_LOAD_NO_MORE,
        SWIPE_LOAD_ERROR
    }

    // 头部刷新View
    View getSwipeHead();

    // 底部加载View
    View getSwipeFoot();

    // 头部刷新View过度拉伸尺度      getSwipeHead得到的View得到高度后  减去此高度后  为松开刷新的实际高度
    int getOverScrollHei();

    // 状态改变回掉
    void onSwipeStatue(SwipeStatus status, int visibleHei, int wholeHei);
}
