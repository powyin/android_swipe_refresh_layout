package com.powyin.scroll.widget;

import android.view.View;

/**
 * Created by powyin on 2017/7/29.
 */

public interface EmptyController {
    // 获取空白视图
    View getView();
    int attachToViewIndex();
    // 状态回掉
    void onSwipeStatue(ISwipe.FreshStatus status);
}
