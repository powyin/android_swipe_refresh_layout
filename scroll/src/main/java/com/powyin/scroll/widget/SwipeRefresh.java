package com.powyin.scroll.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
/**
 * Created by powyin on 2016/7/10.
 */
public class SwipeRefresh extends ViewGroup implements NestedScrollingParent {
    private View mTarget;
    private int mTouchSlop;
    private final NestedScrollingParentHelper mParentHelper;
    private boolean mNestedScrollInProgress;
    private int mActivePointerId = -1;                                             //多手指移动中取值ID

    private float mDispatchTouchEvent_InitialDownY;                                //DispatchTouchEvent
    private float mInterceptTouchEvent_InitialDownY;                               //InterceptTouchEvent
    private float mTouchEvent_InitialDownY;                                        //TouchEvent

    private boolean mDispatchScroll;                                               //DispatchTouchEvent  是否需要打断
    private boolean mDispatchDragged;                                              //DispatchTouchEvent  已经打断
    private boolean mInterceptDragged;                                             //InterceptTouchEvent 打断

    private SwipeControl mSwipeControl;                                            //刷新头部控制器
    private ValueAnimator animationReBackToRefreshing;                             //滚动 显示正在刷新状态
    private ValueAnimator animationReBackToTop;                                    //滚动 回到正常显示
    boolean isFreshContinue = false;                                               //正在刷新
    boolean isFreshComplete = false;                                               //刷新完成
    private int scrollY_Up;
    private int scrollY_Down;
    private OnRefreshListener mOnRefreshListener;

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public SwipeRefresh(Context context) {
        this(context, null);
    }

    public SwipeRefresh(Context context, AttributeSet attrs) {
        super(context, attrs);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mParentHelper = new NestedScrollingParentHelper(this);
    }

    private void ensureTarget() {
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != mSwipeControl.getSwipeView()) {
                    mTarget = child;
                }
            }
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mSwipeControl = new DefaultSwipeControl(getContext());
        addView(mSwipeControl.getSwipeView(), 0);
        View lastView = getChildAt(getChildCount() - 1);
        if (lastView instanceof AbsListView) {
            AbsListView absListView = (AbsListView) lastView;
            absListView.setOverScrollMode(AbsListView.OVER_SCROLL_NEVER);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            int speWid = getChildMeasureSpec(widthMeasureSpec, 0, ViewGroup.LayoutParams.MATCH_PARENT);
            int speHei = getChildMeasureSpec(heightMeasureSpec, 0, ViewGroup.LayoutParams.WRAP_CONTENT);
            child.measure(speWid, speHei);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (getChildCount() > 2) {
            throw new RuntimeException("can not holder only one View");
        }
        int childLeft = getPaddingLeft();
        int childRight = right - left - getPaddingRight();
        int childTop = 0;

        mSwipeControl.getSwipeView().layout(childLeft, -mSwipeControl.getSwipeView().getMeasuredHeight(), childRight, 0);
        scrollY_Up = -mSwipeControl.getSwipeView().getMeasuredHeight();

        if (getChildCount() != 2) return;
        ensureTarget();

        SwipeRefresh.LayoutParams lp = (SwipeRefresh.LayoutParams) mTarget.getLayoutParams();
        mTarget.layout(childLeft, childTop, childRight, childTop + bottom);
        childTop += bottom + lp.bottomMargin;

        scrollY_Down = childTop;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mDispatchScroll = false;
            mDispatchDragged = false;
            mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
            mInterceptDragged = false;
        }

        if (ev.getAction() == MotionEvent.ACTION_DOWN && getScrollY() != 0) {
            mDispatchScroll = true;
        }

        if (mDispatchScroll && !mNestedScrollInProgress) {
            int action = MotionEventCompat.getActionMasked(ev);
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    mDispatchTouchEvent_InitialDownY = getMotionEventY(ev, mActivePointerId);
                    break;
                case MotionEvent.ACTION_MOVE:
                    float y = getMotionEventY(ev, mActivePointerId);
                    if (mActivePointerId == -1 || mDispatchTouchEvent_InitialDownY == Integer.MAX_VALUE || y == Integer.MAX_VALUE) {
                        break;
                    }

                    float yDiff = mDispatchTouchEvent_InitialDownY - y;
                    if (Math.abs(yDiff) > mTouchSlop / 2 && !mDispatchDragged) {
                        mDispatchDragged = true;
                        yDiff = yDiff / 2;
                    }

                    if (mDispatchDragged) {
                        offSetChildrenPreLocation((int) yDiff);
                        if (getScrollY() == 0) {
                            mDispatchScroll = false;
                        }
                        mDispatchTouchEvent_InitialDownY = y;
                        return true;
                    }
                    break;
                case MotionEventCompat.ACTION_POINTER_UP:
                    onSecondaryPointerUp(ev);
                    break;
            }
        }

        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            if (tryBackToRefreshing() || tryBackToFreshFinish()) {
                ev.setAction(MotionEvent.ACTION_CANCEL);
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        ensureTarget();
        if (!isEnabled() || canChildScrollUp() || mNestedScrollInProgress) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:

                final float initialDownY = getMotionEventY(ev, mActivePointerId);
                if (initialDownY == Integer.MAX_VALUE) {
                    return false;
                }
                mInterceptTouchEvent_InitialDownY = initialDownY;
                break;

            case MotionEvent.ACTION_MOVE:
                if (mActivePointerId == -1) {
                    return false;
                }

                final float y = getMotionEventY(ev, mActivePointerId);
                if (y == Integer.MAX_VALUE) {
                    return false;
                }
                final float yDiff = y - mInterceptTouchEvent_InitialDownY;
                if (yDiff > mTouchSlop && !mInterceptDragged) {
                    mInterceptDragged = true;
                    mTouchEvent_InitialDownY = y;
                }
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mInterceptDragged = false;
                mActivePointerId = -1;
                break;
        }

        return mInterceptDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int pointerIndex;
        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                break;
            case MotionEvent.ACTION_MOVE: {
                pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
                if (pointerIndex < 0 || !mInterceptDragged) {
                    break;
                }

                float y = MotionEventCompat.getY(ev, pointerIndex);
                if (mTouchEvent_InitialDownY == Integer.MAX_VALUE) {
                    mTouchEvent_InitialDownY = y;
                    break;
                }
                offSetChildrenPreLocation((int) (mTouchEvent_InitialDownY - y));
                break;
            }
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    break;
                }
                mActivePointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                mActivePointerId = -1;
                mTouchEvent_InitialDownY = Integer.MAX_VALUE;
                return true;
        }

        pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
        if (pointerIndex >= 0) {
            mTouchEvent_InitialDownY = MotionEventCompat.getY(ev, pointerIndex);
        }

        return true;
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }


    private float getMotionEventY(MotionEvent ev, int activePointerId) {
        final int index = ev.findPointerIndex(activePointerId);
        if (index < 0) {
            return Integer.MAX_VALUE;
        }
        return ev.getY(index);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        if (mTarget != null && mTarget instanceof NestedScrollingChild) {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }

    //--------------------------------------------- NestedScrollingParent-----------------------------------------------------//

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return isEnabled() && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        mNestedScrollInProgress = true;
        stopAllScroll();
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        int delta = offSetChildrenPreLocation(dy);
        consumed[1] = delta;
    }

    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
    }


    @Override
    public void onStopNestedScroll(View target) {
        mParentHelper.onStopNestedScroll(target);
        mNestedScrollInProgress = false;
        if (!tryBackToRefreshing() && mNestedScrollInProgress) {
            tryBackToFreshFinish();
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        offSetChildrenLasLocation(dyUnconsumed);
    }




    //-----------------------------------------------------------------------------------------------------------------------------------------//

    private int offSetChildrenPreLocation(int deltaY) {

        if (deltaY == 0) return 0;

        int maxScrollY = scrollY_Down - getHeight();
        int minScrollY = scrollY_Up - mSwipeControl.getOverScrollHei();
        int currentScrollY = getScrollY();

        if (deltaY < 0 && currentScrollY < scrollY_Up && currentScrollY > minScrollY) {                                               //平滑过度下拉刷新的进度变化
            deltaY = (int) (deltaY * Math.pow((currentScrollY - minScrollY) * 1f / mSwipeControl.getOverScrollHei(), 2.5));
        }

        if ((currentScrollY >= minScrollY && currentScrollY < maxScrollY) ||                                                          //提供对多个View的支持
                (getChildCount() == 2 && currentScrollY == 0 && !getChildAt(1).canScrollVertically(deltaY))) {                        //提供对单个View的支持
            int willTo = currentScrollY + deltaY;
            willTo = Math.min(willTo, maxScrollY);
            willTo = Math.max(willTo, minScrollY);
            scrollTo(0, willTo);
            if (willTo < 0 && isFreshComplete && !isFreshContinue) {                                                                 //刷新内部状态
                isFreshComplete = false;
            }
            int swipeViewVisibilityHei = 0 - willTo;
            if (swipeViewVisibilityHei > 0) {                                                                                        //更新刷新状态
                if (isFreshContinue) {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_LOADING, swipeViewVisibilityHei, mSwipeControl.getSwipeView().getHeight());
                } else if (isFreshComplete) {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_COMPLETE, swipeViewVisibilityHei, mSwipeControl.getSwipeView().getHeight());
                } else if (willTo < -mSwipeControl.getSwipeView().getHeight()) {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_OVER, swipeViewVisibilityHei, mSwipeControl.getSwipeView().getHeight());
                } else {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_TOAST, swipeViewVisibilityHei, mSwipeControl.getSwipeView().getHeight());
                }
            }
            return (willTo - currentScrollY);
        }
        return 0;
    }
    private int offSetChildrenLasLocation(int deltaY) {
        if (deltaY == 0) return 0;
        int maxScrollY = scrollY_Down - getHeight();
        int currentScrollY = getScrollY();
        if (currentScrollY >= 0) {
            int willTo = currentScrollY + deltaY;
            willTo = Math.min(willTo, maxScrollY);
            willTo = Math.max(willTo, 0);
            scrollTo(0, willTo);
            return (willTo - currentScrollY);
        }
        return 0;
    }
    private boolean tryBackToRefreshing() {
        int scrollY = getScrollY();
        stopAllScroll();
        boolean isOverProgress = scrollY < -mSwipeControl.getSwipeView().getHeight();
        if (isOverProgress) {
            int animationTarget = -(mSwipeControl.getSwipeView().getHeight());
            animationReBackToRefreshing = ValueAnimator.ofInt(scrollY, animationTarget);
            animationReBackToRefreshing.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    if (value > getScrollY()) {
                        scrollTo(0, value);
                    }
                }
            });
            animationReBackToRefreshing.addListener(new AnimationStatus() {
                boolean isCancel = false;
                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isCancel) {
                        mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_LOADING, mSwipeControl.getSwipeView().getHeight(), mSwipeControl.getSwipeView().getHeight());
                        if(!isFreshContinue){
                            isFreshContinue = true;
                            if(mOnRefreshListener!=null){
                                mOnRefreshListener.onRefresh();
                            }
                        }
                    }
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                    isCancel = true;
                }
            });
            animationReBackToRefreshing.setDuration(Math.abs(400 * (animationTarget - scrollY) / mSwipeControl.getSwipeView().getHeight()));
            animationReBackToRefreshing.start();
        }
        return isOverProgress;
    }
    private boolean tryBackToFreshFinish() {
        stopAllScroll();
        int scrollY = getScrollY();
        if (scrollY < 0) {
            animationReBackToTop = ValueAnimator.ofInt(scrollY, 0);
            animationReBackToTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    scrollTo(0, value);
                }
            });
            animationReBackToTop.setDuration(Math.abs(250 * (0 - scrollY) / mSwipeControl.getSwipeView().getHeight()));
            if (scrollY <= -mSwipeControl.getSwipeView().getHeight() + 10) {
                animationReBackToTop.setStartDelay(400);
            }
            animationReBackToTop.start();

            return true;
        }
        return false;
    }
    private void stopAllScroll() {
        if (animationReBackToRefreshing != null && animationReBackToRefreshing.isRunning()) {
            animationReBackToRefreshing.cancel();
        }
        if (animationReBackToTop != null && animationReBackToTop.isRunning()) {
            animationReBackToTop.cancel();
        }
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }
    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }
    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new LayoutParams(p);
    }
    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }
    public static class LayoutParams extends MarginLayoutParams {
        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }
    }
    private class AnimationStatus implements Animator.AnimatorListener {
        @Override
        public void onAnimationStart(Animator animation) {
        }

        @Override
        public void onAnimationEnd(Animator animation) {

        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    }

    // ----------------------------------------------------------------刷新控制-------------------------------------------------------------------//
    public void setOnRefreshListener(OnRefreshListener onRefreshListener){
        this.mOnRefreshListener = onRefreshListener;
    }
    public void finishRefresh() {
        isFreshContinue = false;
        isFreshComplete = true;
        mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_COMPLETE, -getScrollY(), mSwipeControl.getSwipeView().getHeight());
        tryBackToFreshFinish();
    }
    public interface OnRefreshListener {
        void onRefresh();
    }
}
