package com.powyin.scroll.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.ScrollerCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;


/**
 * Created by powyin on 2016/7/10.
 */
public class SwipeNest extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {
    private View mTarget;
    private final NestedScrollingParentHelper mParentHelper;
    private final NestedScrollingChildHelper mChildHelper;
    private boolean isFly = false;                                                 //辅助nestScrollParent 停止fly
    private SwipeControl mSwipeControl;                                            //刷新头部控制器
    private ValueAnimator animationReBackToRefreshing;                             //滚动 显示正在刷新状态
    private ValueAnimator animationReBackToTop;                                    //滚动 回到正常显示
    private ScrollerCompat mScroller;                                              //滚动 滑动

    private boolean mIsTouchEventMode = false;                                      //DispatchTouchEvent  是否在进行TouchEvent传递
    private boolean mIsFreshContinue = false;                                       //下拉刷新 正在刷新
    private boolean mIsFreshComplete = false;                                       //下拉刷新 刷新完成

    private int scrollY_Up;
    private int scrollY_Down;
    private OnRefreshListener mOnRefreshListener;


    public SwipeNest(Context context) {
        this(context, null);
    }

    public SwipeNest(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeNest(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mParentHelper = new NestedScrollingParentHelper(this);
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        mScroller = ScrollerCompat.create(getContext(), new Interpolator() {
            @Override
            public float getInterpolation(float input) {
                return input;
            }
        });
        initSwipeControl();
    }

    private void ensureTarget() {
        if (mTarget == null) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                View child = getChildAt(i);
                if (child != mSwipeControl.getSwipeHead() && child != mSwipeControl.getSwipeFoot()) {
                    mTarget = child;
                    break;
                }
            }
        }
    }

    private void initSwipeControl() {
        mSwipeControl = new SwipeControlStyleNormal(getContext());
        addView(mSwipeControl.getSwipeHead(), 0);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mIsTouchEventMode = true;
        }

        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            mIsTouchEventMode = false;
        }

        return super.dispatchTouchEvent(ev);
    }


    //--------------------------------------------- NestedScrollingChild-----------------------------------------------------//

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    //--------------------------------------------- NestedScrollingParent-----------------------------------------------------//

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
        stopAllScroll();
    }

    @Override
    public void onStopNestedScroll(View target) {
        mParentHelper.onStopNestedScroll(target);
        stopNestedScroll();
        if (!tryBackToRefreshing()) {
            tryBackToFreshFinish();
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        int myConsumed = offSetChildrenLocation(dyUnconsumed, false);
        dyUnconsumed = dyUnconsumed - myConsumed;
        dispatchNestedScroll(0, myConsumed, 0, dyUnconsumed, null);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        int delta = offSetChildrenLocation(dy, true);
        consumed[1] = delta;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return true;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (getScrollY() < scrollY_Down) {
            if (!(tryBackToRefreshing() || tryBackToFreshFinish())) {
                fling((int) velocityY);
                isFly = true;
                return true;
            } else {
                isFly = false;
                return true;
            }
        } else {
            isFly = false;
            return false;
        }
    }

    @Override
    public int getNestedScrollAxes() {
        return mParentHelper.getNestedScrollAxes();
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
        int childTop = 0;
        final int count = getChildCount();

        View swipeView = mSwipeControl.getSwipeHead();

        for (int i = 0; i <= count - 1; i++) {
            View child = getChildAt(i);

            if (child == swipeView || child == mTarget) continue;

            int childHeight = child.getMeasuredHeight();
            child.layout(0, childTop, right - left, childTop + childHeight);
            childTop += childHeight;
        }

        swipeView.layout(0, -mSwipeControl.getSwipeHead().getMeasuredHeight(), right - left, 0);
        scrollY_Up = -mSwipeControl.getSwipeHead().getMeasuredHeight();

        if (mTarget != null) {
            mTarget.layout(0, childTop, right - left, childTop + bottom - top);
            childTop += bottom - top;
        }

        scrollY_Down = childTop - (bottom - top);
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        ensureTarget();
        if (isFly && mScroller.isOverScrolled() && mScroller.getFinalY() == scrollY_Down) {
            if (mTarget instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) mTarget;
                recyclerView.fling(0, (int) (mScroller.getCurrVelocity() * 0.7f));
            }
            if (mTarget instanceof NestedScrollView) {
                NestedScrollView nestedScrollView = (NestedScrollView) mTarget;
                nestedScrollView.fling((int) (mScroller.getCurrVelocity() * 0.7f));
            }
        }

        if (mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            scrollTo(0, y);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }


    private int offSetChildrenLocation(final int deltaOriginY, boolean pre) {
        ensureTarget();
        int deltaY = deltaOriginY;
        if (deltaOriginY == 0) return 0;
        int middleHei = scrollY_Up != 0 ? scrollY_Up + mSwipeControl.getOverScrollHei() : 0;

        int currentScrollY = getScrollY();

        if (deltaOriginY < 0 && currentScrollY < middleHei) {                                                                      //平滑过度下拉刷新的进度变化
            deltaY = (int) (deltaY * Math.pow((mSwipeControl.getOverScrollHei() - (middleHei - currentScrollY)) * 1f
                    / mSwipeControl.getOverScrollHei(), 2));
        }

        if (pre && currentScrollY != scrollY_Down || !pre) {

            int willTo = currentScrollY + deltaY;
            willTo = Math.min(willTo, scrollY_Down);
            willTo = Math.max(willTo, scrollY_Up);

            if (willTo == currentScrollY) {
                return deltaOriginY;
            }

            scrollTo(0, willTo);

            if (willTo < 0 && willTo > middleHei && mIsFreshComplete && !mIsFreshContinue) {                                          //刷新内部状态
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

            return deltaOriginY;
        }

        return 0;
    }


    private boolean tryBackToRefreshing() {
        if (mIsTouchEventMode || mIsFreshComplete) return false;
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
            animationReBackToRefreshing.setDuration(Math.abs(650 * (middleHei - scrollY) / mSwipeControl.getSwipeHead().getHeight()));
            animationReBackToRefreshing.start();
        }
        return isOverProgress;
    }

    private boolean tryBackToFreshFinish() {
        if (mIsTouchEventMode) return false;

        int scrollY = getScrollY();
        int middleHei = scrollY_Up != 0 ? scrollY_Up + mSwipeControl.getOverScrollHei() : 0;

        if (!mIsFreshComplete && scrollY == middleHei) return false;


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
                animationReBackToTop.setDuration(Math.abs(900 * (0 - scrollY) / mSwipeControl.getSwipeHead().getHeight()));
                animationReBackToTop.setStartDelay(650);
            } else {
                animationReBackToTop.setDuration(460);
            }
            animationReBackToTop.start();
            return true;
        }
        return false;
    }

    private void fling(int velocityY) {
        stopAllScroll();
        mScroller.abortAnimation();
        mScroller.fling(0, getScrollY(), 0, (int) (velocityY * 0.75f), 0, 0, 0, scrollY_Down);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void stopAllScroll() {
        if (animationReBackToRefreshing != null) {
            animationReBackToRefreshing.cancel();
        }
        if (animationReBackToTop != null) {
            animationReBackToTop.cancel();
        }
        if (mScroller != null) {
            mScroller.abortAnimation();
        }

        isFly = false;
    }


    // ----------------------------------------------------------------刷新控制-------------------------------------------------------------------//

    // 设置刷新控制监听
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.mOnRefreshListener = onRefreshListener;
    }

    // 结束上拉刷新
    public void finishRefresh() {
        postDelayed(new Runnable() {
            @Override
            public void run() {
                mIsFreshContinue = false;
                mIsFreshComplete = true;
                mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_HEAD_COMPLETE,
                        -getScrollY(), mSwipeControl.getSwipeHead().getHeight());
                tryBackToFreshFinish();
            }
        },600);
    }

    // 设置自定义刷新视图
    public void setSwipeControl(SwipeControl control) {
        if (control != null && this.mSwipeControl != control) {
            removeView(mSwipeControl.getSwipeHead());
            this.mSwipeControl = control;
            addView(mSwipeControl.getSwipeHead());
            requestLayout();
        }
    }

    public interface OnRefreshListener {
        // 头部刷新开始
        void onRefresh();
    }

}
