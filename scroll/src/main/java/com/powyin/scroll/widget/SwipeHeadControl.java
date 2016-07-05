package com.powyin.scroll.widget;

import android.view.View;

/**
 * Created by powyin on 2016/7/2.
 */
public interface SwipeHeadControl {
    enum SwipeStatus{
        SWIPE_PRE,                    //拉升状态
        SWIPE_TOAST,                  //松开刷新
        SWIPE_LOADING,                //刷新中
        SWIPE_COMPLITE                //刷新完成
    }

    View getSwipeView();
    int getOverScrollHei();
    void onSwipeStatue(SwipeStatus status, int showHei );
}
