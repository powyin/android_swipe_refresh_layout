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
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.ScrollView;

import com.powyin.scroll.R;


/**
 * Created by powyin on 2016/7/10.       当swipeRefresh 子View没有足够空间滑动时 上拉加载自动关闭
 */
public class SwipeRefresh extends ViewGroup implements NestedScrollingParent, ISwipe {


    private View mTarget;
    private int mTouchSlop;
    private final NestedScrollingParentHelper mParentHelper;
    private boolean mNestedScrollInProgress = false;
    private int mActivePointerId = -1;                                             //多手指移动中取值ID


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

    private boolean mRefreshStatusContinueRunning = false;                          //下拉刷新 正在刷新
    private FreshStatus mFreshStatus = FreshStatus.CONTINUE;                        //下拉刷新状态

    private boolean mLoadedStatusContinueRunning = false;                           //上拉加载 正在加载
    private ISwipe.LoadedStatus mLoadedStatus = LoadedStatus.CONTINUE;              //下拉刷新状态;


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

            mTarget.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    if (!mNestedScrollInProgress && !mLoadedStatusContinueRunning &&
                            !mDispatchDragged && !mInterceptDragged  && mOnRefreshListener != null &&
                            !canChildScrollUp() && canChildScrollDown()) {
                        mLoadedStatusContinueRunning = true;
                        mOnRefreshListener.onLoading(false);
                    }
                }
            });

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
            int speWid = getChildMeasureSpec(widthMeasureSpec, 0, LayoutParams.MATCH_PARENT);
            int speHei = getChildMeasureSpec(heightMeasureSpec, 0, LayoutParams.WRAP_CONTENT);
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

        mSwipeControl.getSwipeHead().layout(left, -mSwipeControl.getSwipeHead().getMeasuredHeight(), right, 0);

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
            mActivePointerId = ev.getPointerId(0);
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

        if (mDispatchScroll ) {
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
        if (mNestedScrollInProgress || !isEnabled() || (canChildScrollDown() && canChildScrollUp())) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mInterceptTouchEvent_InitialDownY = getMotionEventY(ev, mActivePointerId);
                break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                int pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    break;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                mInterceptTouchEvent_InitialDownY = getMotionEventY(ev, mActivePointerId);
                break;
            }


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
                mActivePointerId = ev.getPointerId(0);
                break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                if (pointerIndex < 0) {
                    break;
                }
                mActivePointerId = ev.getPointerId(pointerIndex);
                break;
            }

            case MotionEvent.ACTION_MOVE: {

                float y = getMotionEventY(ev, mActivePointerId);
                if (y == Integer.MAX_VALUE) {
                    return false;
                }

                if (mTouchEvent_InitialDownY == Integer.MAX_VALUE) {
                    mTouchEvent_InitialDownY = y;
                    break;
                }
                offSetChildrenLocation((int) (mTouchEvent_InitialDownY - y), false);
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
        return arg1==0 || (arg1 > 0 && arg2 > 0) || (arg1 < 0 && arg2 < 0);
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
        ensureTarget();
        if (mTarget == null ||  mTarget instanceof NestedScrollingChild) {
            super.requestDisallowInterceptTouchEvent(b);
        }

//        if(mNestedScrollInProgress){
//            super.requestDisallowInterceptTouchEvent(b);
//        }

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

        if (deltaOriginY < 0 && currentScrollY < middleHei) {                                                                        //过度拉伸 阻尼效果
            deltaY = (int) (deltaY * Math.pow((mSwipeControl.getOverScrollHei() - (middleHei - currentScrollY)) * 1f
                    / mSwipeControl.getOverScrollHei(), 2));
        }

        if (currentScrollY != 0 || !pre) {
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


            if ( willTo > 0 && !canChildScrollDown()) {                                //确保当mTarget没有足够内容进行独立滑动时 上拉加载不启动
                willTo = 0;
            }

            if (willTo == currentScrollY) {
                return deltaOriginY;
            }

            scrollTo(0, willTo);

            if (willTo < 0 && willTo > middleHei && (mFreshStatus == FreshStatus.ERROR_AUTO_CANCEL || mFreshStatus == FreshStatus.SUCCESS) && !mRefreshStatusContinueRunning) {                                                                 //刷新内部状态
                mFreshStatus = FreshStatus.CONTINUE;
            }

            int swipeViewVisibilityHei = 0 - willTo;
            if (swipeViewVisibilityHei > 0) {                                                                                                     //更新刷新状态

                switch (mFreshStatus) {
                    case CONTINUE:
                        if (mRefreshStatusContinueRunning) {
                            mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_LOADING, swipeViewVisibilityHei, mSwipeControl.getSwipeHead().getHeight());
                        } else if (willTo < middleHei) {
                            mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_OVER, swipeViewVisibilityHei, mSwipeControl.getSwipeHead().getHeight());
                        } else {
                            mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_TOAST, swipeViewVisibilityHei, mSwipeControl.getSwipeHead().getHeight());
                        }
                        break;
                    case SUCCESS:
                        mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_COMPLETE_OK, swipeViewVisibilityHei, mSwipeControl.getSwipeHead().getHeight());
                        break;
                    case ERROR_AUTO_CANCEL:
                        mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_COMPLETE_ERROR_AUTO_CANCEL, swipeViewVisibilityHei, mSwipeControl.getSwipeHead().getHeight());
                        break;
                    case ERROR_FIXED:
                        mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_COMPLETE_ERROR_FIXED, swipeViewVisibilityHei, mSwipeControl.getSwipeHead().getHeight());
                        break;
                }
            }

            if (willTo > 0 && !mLoadedStatusContinueRunning) {                                                                                     //刷新内部状态
                mLoadedStatusContinueRunning = true;
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onLoading(true);
                }
            }
            if (willTo > 0) {
                if (mLoadedStatus == LoadedStatus.NO_MORE) {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_LOAD_NO_MORE, willTo, mSwipeControl.getSwipeFoot().getHeight());
                } else if (mLoadedStatus == LoadedStatus.ERROR) {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_LOAD_ERROR, willTo, mSwipeControl.getSwipeFoot().getHeight());
                } else if (mLoadedStatus == LoadedStatus.CONTINUE) {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_LOAD_LOADING, willTo, mSwipeControl.getSwipeFoot().getHeight());
                }
            }

            return (deltaOriginY);
        }
        return 0;
    }

    private boolean tryBackToRefreshing() {
        if (mIsTouchEventMode || mFreshStatus == FreshStatus.SUCCESS || mFreshStatus == FreshStatus.ERROR_AUTO_CANCEL)
            return false;
        int scrollY = getScrollY();
        int middleHei = scrollY_Up != 0 ? scrollY_Up + mSwipeControl.getOverScrollHei() : 0;
        boolean isOverProgress = scrollY < middleHei;
        if (isOverProgress) {
            stopAllScroll();
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
                        switch (mFreshStatus) {
                            case ERROR_FIXED:
                                mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_COMPLETE_ERROR_FIXED, mSwipeControl.getSwipeHead().getHeight(), mSwipeControl.getSwipeHead().getHeight());
                                break;
                            case CONTINUE:
                                mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_LOADING, mSwipeControl.getSwipeHead().getHeight(), mSwipeControl.getSwipeHead().getHeight());
                                break;
                        }

                        if (!mRefreshStatusContinueRunning && mFreshStatus != FreshStatus.ERROR_FIXED) {
                            mRefreshStatusContinueRunning = true;
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
            animationReBackToRefreshing.setDuration(Math.abs(550 * (middleHei - scrollY) / mSwipeControl.getSwipeHead().getHeight()));
            animationReBackToRefreshing.start();
        }
        return isOverProgress;
    }

    private boolean tryBackToFreshFinish() {
        if (mIsTouchEventMode) return false;

        int scrollY = getScrollY();
        int middleHei = scrollY_Up != 0 ? scrollY_Up + mSwipeControl.getOverScrollHei() : 0;

        if ((mFreshStatus == FreshStatus.CONTINUE || mFreshStatus == FreshStatus.ERROR_FIXED) && scrollY == middleHei)
            return false;

        if (scrollY < 0) {
            stopAllScroll();
            animationReBackToTop = ValueAnimator.ofInt(scrollY, 0);
            animationReBackToTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    scrollTo(0, value);
                }
            });

            if (scrollY == middleHei) {
                animationReBackToTop.setDuration(Math.abs(550 * (0 - scrollY) / mSwipeControl.getSwipeHead().getHeight()));
                animationReBackToTop.setStartDelay(650);
            } else {
                animationReBackToTop.setDuration(320);
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

    // ----------------------------------------------------------------ISwipeIMP-------------------------------------------------------------------//

    // 设置刷新控制监听
    @Override
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.mOnRefreshListener = onRefreshListener;
    }

    @Override
    public void setFreshStatue(FreshStatus statue) {
        switch (statue) {
            case CONTINUE:

                mFreshStatus = FreshStatus.CONTINUE;
                mRefreshStatusContinueRunning = true;
                mLoadedStatus = LoadedStatus.CONTINUE;
                mLoadedStatusContinueRunning = false;

                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onRefresh();
                }

                mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_LOADING,
                        -getScrollY(), mSwipeControl.getSwipeHead().getHeight());
                tryBackToRefreshing();

                break;
            case SUCCESS:                                                                                   //设置刷新成功 自动隐藏
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFreshStatus = FreshStatus.SUCCESS;
                        mRefreshStatusContinueRunning = false;
                        mLoadedStatus = LoadedStatus.CONTINUE;
                        mLoadedStatusContinueRunning = false;
                        mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_COMPLETE_OK,
                                -getScrollY(), mSwipeControl.getSwipeHead().getHeight());
                        tryBackToFreshFinish();
                    }
                }, 500);
                break;
            case ERROR_FIXED:                                                                              //设置刷新失败 自动隐藏
                mFreshStatus = FreshStatus.ERROR_FIXED;
                mRefreshStatusContinueRunning = false;
                mLoadedStatus = LoadedStatus.CONTINUE;
                mLoadedStatusContinueRunning = false;
                mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_COMPLETE_ERROR_FIXED,
                        -getScrollY(), mSwipeControl.getSwipeHead().getHeight());
                //     tryBackToFreshFinish();

                break;
            case ERROR_AUTO_CANCEL:                                                                        //设置刷新失败 自动隐藏
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFreshStatus = FreshStatus.ERROR_AUTO_CANCEL;
                        mRefreshStatusContinueRunning = false;
                        mLoadedStatus = LoadedStatus.CONTINUE;
                        mLoadedStatusContinueRunning = false;
                        mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_COMPLETE_ERROR_AUTO_CANCEL,
                                -getScrollY(), mSwipeControl.getSwipeHead().getHeight());
                        tryBackToFreshFinish();
                    }
                }, 500);
                break;


        }
    }

    @Override
    public void setLoadMoreStatus(ISwipe.LoadedStatus status) {
        switch (status) {
            case CONTINUE:

                this.mLoadedStatusContinueRunning = false;
                this.mLoadedStatus = LoadedStatus.CONTINUE;
                mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_LOAD_LOADING,
                        getHeight() - mSwipeControl.getSwipeFoot().getTop(), mSwipeControl.getSwipeFoot().getHeight());

                ensureTarget();
                int currentScrollY = getScrollY();
                if (currentScrollY <= 0) {
                    return;
                }

                if (mTarget instanceof RecyclerView || mTarget instanceof ListView || mTarget instanceof ScrollView || mTarget instanceof NestedScrollView) {
                    mTarget.scrollBy(0, currentScrollY);
                }

                stopAllScroll();
                scrollTo(0, 0);
                break;
            case ERROR:
                this.mLoadedStatusContinueRunning = true;
                this.mLoadedStatus = LoadedStatus.ERROR;
                mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_LOAD_ERROR,
                        getHeight() - mSwipeControl.getSwipeFoot().getTop(), mSwipeControl.getSwipeFoot().getHeight());
                break;
            case NO_MORE:
                this.mLoadedStatusContinueRunning = true;
                this.mLoadedStatus = LoadedStatus.NO_MORE;
                mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_LOAD_NO_MORE,
                        getHeight() - mSwipeControl.getSwipeFoot().getTop(), mSwipeControl.getSwipeFoot().getHeight());
                break;
        }
    }


    // 设置刷新模式
    @Override
    public void setSwipeModel(SwipeControl.SwipeModel model) {
        if (this.mModel != model && model != null) {
            this.mModel = model;
            requestLayout();
        }
    }

    // 设置自定义刷新视图
    @Override
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

}
































