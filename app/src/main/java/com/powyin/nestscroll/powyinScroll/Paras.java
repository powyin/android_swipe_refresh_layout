package com.powyin.nestscroll.powyinScroll;

import android.os.Parcelable;
import android.support.design.widget.CoordinatorLayout;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by MT3020 on 2016/4/5.
 */
public class Paras extends CoordinatorLayout.Behavior<ImageView> {

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, ImageView child, MotionEvent ev) {
        return super.onInterceptTouchEvent(parent, child, ev);
    }

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, ImageView child, MotionEvent ev) {
        return super.onTouchEvent(parent, child, ev);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, ImageView child, int layoutDirection) {
        return super.onLayoutChild(parent, child, layoutDirection);
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, ImageView child, View target, float velocityX, float velocityY) {
        return super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, ImageView child, View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
    }

    @Override
    public void onRestoreInstanceState(CoordinatorLayout parent, ImageView child, Parcelable state) {
        super.onRestoreInstanceState(parent, child, state);
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, ImageView child) {
        return super.onSaveInstanceState(parent, child);
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, ImageView child, View directTargetChild, View target, int nestedScrollAxes) {
        return super.onStartNestedScroll(coordinatorLayout, child, directTargetChild, target, nestedScrollAxes);
    }

    @Override
    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, ImageView child, View target) {
        super.onStopNestedScroll(coordinatorLayout, child, target);
    }
}
