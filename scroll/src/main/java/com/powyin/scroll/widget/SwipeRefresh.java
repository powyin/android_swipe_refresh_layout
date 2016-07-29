package com.powyin.scroll.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.powyin.scroll.R;


/**
 * Created by powyin on 2016/7/10.
 */
public class SwipeRefresh extends ViewGroup implements NestedScrollingParent {
    private View mTarget;
    private int mTouchSlop;
    private final NestedScrollingParentHelper mParentHelper;
    private boolean mNestedScrollInProgress;
    private int mActivePointerId = -1;                                              //多手指移动中取值ID


    private float mDispatchTouchEvent_InitialDownY;                                 //DispatchTouchEvent
    private float mInterceptTouchEvent_InitialDownY;                                //InterceptTouchEvent
    private float mInterceptTouchEvent_InitialDownY_Direct;                         //InterceptTouchEvent
    private float mTouchEvent_InitialDownY;                                         //TouchEvent

    private boolean mIsTouchEventMode = false;                                      //DispatchTouchEvent  是否在进行TouchEvent传递
    private boolean mDispatchScroll;                                                //DispatchTouchEvent  是否需要打断
    private boolean mDispatchDragged;                                               //DispatchTouchEvent  已经打断
    private boolean mInterceptDragged;                                              //InterceptTouchEvent 打断

    private SwipeControl mSwipeControl;                                             //刷新头部控制器
    private ValueAnimator animationReBackToRefreshing;                              //滚动 显示正在刷新状态
    private ValueAnimator animationReBackToTop;                                     //滚动 回到正常显示

    private boolean mIsFreshContinue = false;                                       //下拉刷新 正在刷新
    private boolean mIsFreshComplete = false;                                       //下拉刷新 刷新完成
    private boolean mIsLoadContinue = false;                                        //上拉加载 正在加载
    private boolean mIsLoadComplete = false;                                        //上拉加载 全部加载完成

    private SwipeControl.SwipeModel mModel = SwipeControl.SwipeModel.SWIPE_BOTH;    //刷新模式设置
    private int scrollY_Up;
    private int scrollY_Down;
    private OnRefreshListener mOnRefreshListener;


    public SwipeRefresh(Context context) {
        this(context, null);
    }

    public SwipeRefresh(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeRefresh(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mParentHelper = new NestedScrollingParentHelper(this);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeRefresh);
            int modelIndex = a.getInt(R.styleable.SwipeRefresh_fresh_model, -1);
            if (modelIndex >= 0 && modelIndex < SwipeControl.SwipeModel.values().length) {
                mModel = SwipeControl.SwipeModel.values()[modelIndex];
            }
            a.recycle();
        }
        initSwipeControl();
    }

    private void ensureTarget() {
        if (mTarget == null) {
            for (int i = 0; i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child != mSwipeControl.getSwipeHead() && child != mSwipeControl.getSwipeFoot()) {
                    mTarget = child;
                }
            }
            if (mTarget instanceof AbsListView) {
                AbsListView absListView = (AbsListView) mTarget;
                absListView.setOverScrollMode(AbsListView.OVER_SCROLL_NEVER);
            }
        }
    }


    private void initSwipeControl() {
        mSwipeControl = new SwipeControlStyleNormal(getContext());
        addView(mSwipeControl.getSwipeHead(), 0);
        addView(mSwipeControl.getSwipeFoot(), getChildCount());
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
        ensureTarget();
        if (getChildCount() > 3) {
            throw new RuntimeException("can not holder only one View");
        }

        scrollY_Up = -mSwipeControl.getSwipeHead().getMeasuredHeight();
        scrollY_Down = mSwipeControl.getSwipeFoot().getMeasuredHeight();

        mSwipeControl.getSwipeHead().layout(0, -mSwipeControl.getSwipeHead().getMeasuredHeight(), right - left, 0);

        mTarget.layout(0, 0, right - left, bottom - top);

        mSwipeControl.getSwipeFoot().layout(0, bottom - top, right - left, bottom - top + scrollY_Down);

        if (mModel == SwipeControl.SwipeModel.SWIPE_NONE || mModel == SwipeControl.SwipeModel.SWIPE_ONLY_LOADINN) {
            scrollY_Up = 0;
        }

        if (mModel == SwipeControl.SwipeModel.SWIPE_NONE || mModel == SwipeControl.SwipeModel.SWIPE_ONLY_REFRESH) {
            scrollY_Down = 0;
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mInterceptDragged = false;
            mDispatchScroll = false;
            mDispatchDragged = false;
            mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
            mInterceptTouchEvent_InitialDownY = Integer.MAX_VALUE;
            mInterceptTouchEvent_InitialDownY_Direct = 0;
            mIsTouchEventMode = true;
        }

        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            mIsTouchEventMode = false;
        }

        if (mNestedScrollInProgress) {
            return super.dispatchTouchEvent(ev);
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
                        offSetChildrenLocation((int) yDiff, true);
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
        if (!isEnabled() || (canChildScrollDown() && canChildScrollUp()) || mNestedScrollInProgress) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mInterceptTouchEvent_InitialDownY = getMotionEventY(ev, mActivePointerId);
                break;
            case MotionEvent.ACTION_MOVE:
                float y = getMotionEventY(ev, mActivePointerId);
                if (y == Integer.MAX_VALUE) {
                    return false;
                }

                if (mInterceptTouchEvent_InitialDownY == Integer.MAX_VALUE) {
                    mInterceptTouchEvent_InitialDownY = y;
                    return false;
                }

                final float yDiff = y - mInterceptTouchEvent_InitialDownY;

                if (yDiff == 0) {
                    return false;
                }

                if (!isSameDirection(mInterceptTouchEvent_InitialDownY_Direct, yDiff)) {
                    mInterceptTouchEvent_InitialDownY_Direct = yDiff;
                    mInterceptTouchEvent_InitialDownY = y;
                    return false;
                }


                if (Math.abs(yDiff) > mTouchSlop && !mInterceptDragged &&
                        ((yDiff < 0 && !canChildScrollUp()) || (yDiff > 0 && !canChildScrollDown()))) {                //头部 与尾巴自动判断
                    mInterceptDragged = true;
                    mTouchEvent_InitialDownY = y;
                }
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
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
                offSetChildrenLocation((int) (mTouchEvent_InitialDownY - y), false);
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

    private boolean isSameDirection(float arg1, float arg2) {
        return (arg1 > 0 && arg2 > 0) || (arg1 < 0 && arg2 < 0);
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }

    private boolean canChildScrollDown() {
        ensureTarget();
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                AbsListView absListView = (AbsListView) mTarget;
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

    private boolean canChildScrollUp() {
        ensureTarget();
        return ViewCompat.canScrollVertically(mTarget, 1) || mTarget.getScrollY() < 0;
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
        int delta = offSetChildrenLocation(dy, true);
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
        if (!tryBackToRefreshing()) {
            tryBackToFreshFinish();
        }

    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        offSetChildrenLocation(dyUnconsumed, false);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        return getScrollY() != 0;
    }

    private int offSetChildrenLocation(int deltaOriginY, boolean pre) {
        ensureTarget();

        int deltaY = deltaOriginY;
        if (deltaOriginY == 0) return 0;
        int middleHei = scrollY_Up != 0 ? scrollY_Up + mSwipeControl.getOverScrollHei() : 0;

        int currentScrollY = getScrollY();

        if (deltaOriginY < 0 && currentScrollY < middleHei) {                                                                        //平滑过度下拉刷新的进度变化
            deltaY = (int) (deltaY * Math.pow((mSwipeControl.getOverScrollHei() - (middleHei - currentScrollY)) * 1f
                    / mSwipeControl.getOverScrollHei(), 2));
        }

        if (pre && currentScrollY != 0 || !pre) {
            int willTo = currentScrollY + deltaY;
            willTo = Math.min(willTo, scrollY_Down);
            willTo = Math.max(willTo, scrollY_Up);

            if ((currentScrollY > 0 && willTo < 0) || (currentScrollY < 0 && willTo > 0)) {                                           //确保scroll值经过0
                willTo = 0;
            }
            if (mInterceptTouchEvent_InitialDownY_Direct > 0 && willTo > 0) {                                                         //确保上拉刷新独立
                willTo = 0;
            }
            if (mInterceptTouchEvent_InitialDownY_Direct < 0 && willTo < 0) {                                                         //确保下拉加载独立
                willTo = 0;
            }
            if (mInterceptTouchEvent_InitialDownY_Direct < 0 && willTo > 0 && !canChildScrollDown()) {                                //确保当mTarget没有足够内容进行独立滑动时 下拉加载不启动
                willTo = 0;
            }

            if (willTo == currentScrollY) {
                return deltaOriginY;
            }

            scrollTo(0, willTo);

            if (willTo < 0 && willTo > middleHei && mIsFreshComplete && !mIsFreshContinue) {                                                                 //刷新内部状态
                mIsFreshComplete = false;
            }
            int swipeViewVisibilityHei = 0 - willTo;
            if (swipeViewVisibilityHei > 0) {                                                                                          //更新刷新状态
                if (mIsFreshContinue) {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_LOADING, swipeViewVisibilityHei, mSwipeControl.getSwipeHead().getHeight());
                } else if (mIsFreshComplete) {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_COMPLETE, swipeViewVisibilityHei, mSwipeControl.getSwipeHead().getHeight());
                } else if (willTo < middleHei) {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_OVER, swipeViewVisibilityHei, mSwipeControl.getSwipeHead().getHeight());
                } else {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_TOAST, swipeViewVisibilityHei, mSwipeControl.getSwipeHead().getHeight());
                }
            }

            if (willTo > 0 && !mIsLoadContinue) {                                                                                     //刷新内部状态
                mIsLoadContinue = true;
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onLoading();
                }
            }
            if (willTo > 0) {
                if (mIsLoadComplete) {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_FOOT_COMPLETE, willTo, mSwipeControl.getSwipeFoot().getHeight());
                } else {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_FOOT_LOADING, willTo, mSwipeControl.getSwipeFoot().getHeight());
                }
            }

            return (deltaOriginY);
        }
        return 0;
    }

    private boolean tryBackToRefreshing() {
        if (mIsTouchEventMode || mIsFreshComplete) return false;
        int scrollY = getScrollY();
        int middleHei = scrollY_Up != 0 ? scrollY_Up + mSwipeControl.getOverScrollHei() : 0;
        stopAllScroll();
        boolean isOverProgress = scrollY < middleHei;
        if (isOverProgress) {
            animationReBackToRefreshing = ValueAnimator.ofInt(scrollY, middleHei);
            animationReBackToRefreshing.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    if (value > getScrollY()) {
                        scrollTo(0, value);
                    }
                }
            });
            animationReBackToRefreshing.addListener(new Animator.AnimatorListener() {
                boolean isCancel = false;

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isCancel) {
                        mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_LOADING, mSwipeControl.getSwipeHead().getHeight(), mSwipeControl.getSwipeHead().getHeight());
                        if (!mIsFreshContinue) {
                            mIsFreshContinue = true;
                            if (mOnRefreshListener != null) {
                                mOnRefreshListener.onRefresh();
                            }
                        }
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isCancel = true;
                }

                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });
            animationReBackToRefreshing.setDuration(Math.abs(450 * (middleHei - scrollY) / mSwipeControl.getSwipeHead().getHeight()));
            animationReBackToRefreshing.start();
        }
        return isOverProgress;
    }

    private boolean tryBackToFreshFinish() {
        if (mIsTouchEventMode) return false;

        int scrollY = getScrollY();
        int middleHei = scrollY_Up != 0 ? scrollY_Up + mSwipeControl.getOverScrollHei() : 0;

        if (!mIsFreshComplete && getScrollY() == middleHei) return false;

        stopAllScroll();
        if (scrollY < 0) {
            animationReBackToTop = ValueAnimator.ofInt(scrollY, 0);
            animationReBackToTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    scrollTo(0, value);
                }
            });

            if (scrollY == middleHei) {
                animationReBackToTop.setDuration(Math.abs(450 * (0 - scrollY) / mSwipeControl.getSwipeHead().getHeight()));
                animationReBackToTop.setStartDelay(400);
            } else {
                animationReBackToTop.setDuration(260);
            }
            animationReBackToTop.start();
            return true;
        }
        return false;
    }

    private void stopAllScroll() {
        if (animationReBackToRefreshing != null) {
            animationReBackToRefreshing.cancel();
        }
        if (animationReBackToTop != null) {
            animationReBackToTop.cancel();
        }
    }

    // ----------------------------------------------------------------刷新控制-------------------------------------------------------------------//

    // 设置刷新控制监听
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.mOnRefreshListener = onRefreshListener;
    }

    // 结束上拉刷新
    public void finishRefresh() {
        this.mIsFreshContinue = false;
        this.mIsFreshComplete = true;
        this.mIsLoadComplete = false;
        this.mIsLoadContinue = false;
        mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_COMPLETE,
                -getScrollY(), mSwipeControl.getSwipeHead().getHeight());
        tryBackToFreshFinish();
    }

    // 设置下拉加载是否全部加载完成
    public void setIsLoadComplete(boolean isLoadComplete) {
        this.mIsLoadComplete = isLoadComplete;
        mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_FOOT_COMPLETE,
                getHeight() - mSwipeControl.getSwipeFoot().getTop(), mSwipeControl.getSwipeFoot().getHeight());
    }

    // 隐藏下拉加载显示
    public void hiddenLoadMore() {
        this.mIsLoadContinue = false;
        ensureTarget();
        int currentScrollY = getScrollY();
        if (currentScrollY <= 0) {
            return;
        }

        if (mTarget instanceof RecyclerView) {                                                                  //平滑recyclerView 下拉加载过程
            RecyclerView recyclerView = (RecyclerView) mTarget;
            if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                layoutManager.scrollVerticallyBy(currentScrollY, null, null);
            }
        }

        if (mTarget instanceof ListView) {                                                                      //ListView 下拉加载过程
            ListView listView = (ListView) mTarget;
            listView.smoothScrollBy(currentScrollY, 0);
        }
        scrollTo(0, 0);
    }

    // 设置刷新模式
    public void setSwipeModel(SwipeControl.SwipeModel model) {
        if (this.mModel != model && model != null) {
            this.mModel = model;
            requestLayout();
        }
    }

    // 设置自定义刷新视图
    public void setSwipeControl(SwipeControl control) {
        if (control != null && this.mSwipeControl != control) {
            removeView(mSwipeControl.getSwipeHead());
            removeView(mSwipeControl.getSwipeFoot());
            this.mSwipeControl = control;
            addView(mSwipeControl.getSwipeHead());
            addView(mSwipeControl.getSwipeFoot());
            requestLayout();
        }
    }

    public interface OnRefreshListener {
        // 头部刷新开始
        void onRefresh();

        // 加载更多开始
        void onLoading();
    }
}
































