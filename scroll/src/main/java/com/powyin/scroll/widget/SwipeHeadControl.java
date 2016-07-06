package com.powyin.scroll.widget;

import android.view.View;

/**
 * Created by powyin on 2016/7/2.
 */
public interface SwipeHeadControl {
    enum SwipeStatus{
        SWIPE_OVER_PRE,                    //提示: 松开刷新
        SWIPE_TOAST,                       //提示: 下拉刷新
        SWIPE_LOADING,                     //提示: 刷新中
        SWIPE_COMPLETE                     //提示: 刷新完成
    }

    View getSwipeView();
    int getOverScrollHei();
    void onSwipeStatue(SwipeStatus status, int showHei );
}
