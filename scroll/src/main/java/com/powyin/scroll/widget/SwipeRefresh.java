package com.powyin.scroll.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.AbsListView;

/**
 * Created by powyin on 2016/7/10.
 */
public class SwipeRefresh extends ViewGroup{

    public SwipeRefresh(Context context) {
        this(context, null);
    }

    public SwipeRefresh(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeRefresh(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mParentHelper = new NestedScrollingParentHelper(this);
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
    }

//        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
//        mTouchSlop = configuration.getScaledTouchSlop();
//        mTouchSlop = 5;
//        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
//        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();


    private static final String TAG = SwipeNest.class.getCanonicalName();
    private final NestedScrollingParentHelper mParentHelper;
    private final NestedScrollingChildHelper mChildHelper;
    private int isInScrollCircle = 3;                                              //辅助nestScrollParent 计算周期
    private boolean isFly = false;                                                 //辅助nestScrollParent 停止fly
    private SwipeControl mSwipeControl;                                        //刷新头部控制器
    private ValueAnimator animationReBackToRefreshing;                             //滚动 显示正在刷新状态
    private ValueAnimator animationReBackToTop;                                    //滚动 回到正常显示
    private ScrollerCompat mScroller;                                              //滚动 滑动

    boolean isFreshContinue = false;                                               //正在刷新
    boolean isFreshComplete = false;                                               //刷新完成

    private int scrollY_Up;
    private int scrollY_Down;


    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mScroller = ScrollerCompat.create(getContext());
        mSwipeControl = new DefalutSwipeControl(getContext());
        addView(mSwipeControl.getSwipeView(), 0);
        mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();

        View lastView = getChildAt(getChildCount() - 1);
        if (lastView instanceof AbsListView) {
            AbsListView absListView = (AbsListView) lastView;
            absListView.setOverScrollMode(AbsListView.OVER_SCROLL_NEVER);
        }
    }


//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        final int action = ev.getAction();
//        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
//            return true;
//        }
//
//        if(!isEnabled()) {
//            return false;
//        }
//
//        switch (action & MotionEventCompat.ACTION_MASK) {
//            case MotionEvent.ACTION_MOVE: {
//                final int activePointerId = mActivePointerId;
//                if (activePointerId == INVALID_POINTER) {
//                    // If we don't have a valid id, the touch down wasn't on content.
//                    break;
//                }
//
//                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
//                if (pointerIndex == -1) {
//                    Log.e(TAG, "Invalid pointerId=" + activePointerId
//                            + " in onInterceptTouchEvent");
//                    break;
//                }
//
//                final int y = (int) MotionEventCompat.getY(ev, pointerIndex);
//                final int yDiff = Math.abs(y - mLastMotionY);
//                if (yDiff > mTouchSlop
//                        && (getNestedScrollAxes() & ViewCompat.SCROLL_AXIS_VERTICAL) == 0) {
//                    mIsBeingDragged = true;
//                    mLastMotionY = y;
//                    obtainVelocityTracker(ev);
//                    mNestedYOffset = 0;
//                    final ViewParent parent = getParent();
//                    if (parent != null) {
//                        parent.requestDisallowInterceptTouchEvent(true);
//                    }
//                }
//                break;
//            }
//
//            case MotionEvent.ACTION_DOWN: {
//                final int y = (int) ev.getY();
//
//                /*
//                 * Remember location of down touch.
//                 * ACTION_DOWN always refers to pointer index 0.
//                 */
//                mLastMotionY = y;
//                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
//
//                initOrResetVelocityTracker();
//                mVelocityTracker.addMovement(ev);
//                /*
//                * If being flinged and user touches the screen, initiate drag;
//                * otherwise don't.  mScroller.isFinished should be false when
//                * being flinged.
//                */
//                mIsBeingDragged = !mScroller.isFinished();
//                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
//                break;
//            }
//
//            case MotionEvent.ACTION_CANCEL:
//            case MotionEvent.ACTION_UP:
//                /* Release the drag */
//                mIsBeingDragged = false;
//                mActivePointerId = INVALID_POINTER;
//                endDrag();
//                stopNestedScroll();
//                break;
//            case MotionEventCompat.ACTION_POINTER_UP:
//                onSecondaryPointerUp(ev);
//                break;
//        }
//
//        return mIsBeingDragged;
//    }


//    @Override
//    public boolean onTouchEvent(MotionEvent ev) {
//
//
//
//        MotionEvent vtev = MotionEvent.obtain(ev);
//
//        final int actionMasked = MotionEventCompat.getActionMasked(ev);
//
//        if (actionMasked == MotionEvent.ACTION_DOWN) {
//            mNestedYOffset = 0;
//        }
//        vtev.offsetLocation(0, mNestedYOffset);
//
//        switch (actionMasked) {
//            case MotionEvent.ACTION_DOWN: {
//                if ((mIsBeingDragged = !mScroller.isFinished())) {
//                    final ViewParent parent = getParent();
//                    if (parent != null) {
//                        parent.requestDisallowInterceptTouchEvent(true);
//                    }
//                }
//
//                /*
//                 * If being flinged and user touches, stop the fling. isFinished
//                 * will be false if being flinged.
//                 */
//                if (!mScroller.isFinished()) {
//                    mScroller.abortAnimation();
//                }
//
//                // Remember where the motion event started
//                mLastMotionY = (int) ev.getY();
//                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
//                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
//                break;
//            }
//            case MotionEvent.ACTION_MOVE:
//                final int activePointerIndex = MotionEventCompat.findPointerIndex(ev,
//                        mActivePointerId);
//                if (activePointerIndex == -1) {
//                    Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
//                    break;
//                }
//
//                final int y = (int) MotionEventCompat.getY(ev, activePointerIndex);
//                int deltaY = mLastMotionY - y;
//                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
//                    deltaY -= mScrollConsumed[1];
//                    vtev.offsetLocation(0, mScrollOffset[1]);
//                    mNestedYOffset += mScrollOffset[1];
//                }
//                if (!mIsBeingDragged && Math.abs(deltaY) > mTouchSlop) {
//                    final ViewParent parent = getParent();
//                    if (parent != null) {
//                        parent.requestDisallowInterceptTouchEvent(true);
//                    }
//                    mIsBeingDragged = true;
//                    if (deltaY > 0) {
//                        deltaY -= mTouchSlop;
//                    } else {
//                        deltaY += mTouchSlop;
//                    }
//                }
//                if (mIsBeingDragged) {
//                    // Scroll to follow the motion event
//                    mLastMotionY = y - mScrollOffset[1];
//
//                    final int scrolledDeltaY = moveBy(deltaY);
//                    final int unconsumedY = deltaY - scrolledDeltaY;
//                    if (dispatchNestedScroll(0, scrolledDeltaY, 0, unconsumedY, mScrollOffset)) {
//                        mLastMotionY -= mScrollOffset[1];
//                        vtev.offsetLocation(0, mScrollOffset[1]);
//                        mNestedYOffset += mScrollOffset[1];
//                    }
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                if (mIsBeingDragged) {
//                    final VelocityTracker velocityTracker = mVelocityTracker;
//                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
//                    int initialVelocity = (int) VelocityTrackerCompat.getYVelocity(velocityTracker,
//                            mActivePointerId);
//
//                    if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
//                        flingWithNestedDispatch(-initialVelocity);
//                    }
//
//                    mActivePointerId = INVALID_POINTER;
//                    endDrag();
//                }
//                break;
//            case MotionEvent.ACTION_CANCEL:
//                if (mIsBeingDragged && getChildCount() > 0) {
//                    mActivePointerId = INVALID_POINTER;
//                    endDrag();
//                }
//                break;
//            case MotionEventCompat.ACTION_POINTER_DOWN: {
//                final int index = MotionEventCompat.getActionIndex(ev);
//                mLastMotionY = (int) MotionEventCompat.getY(ev, index);
//                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
//                break;
//            }
//            case MotionEventCompat.ACTION_POINTER_UP:
//                onSecondaryPointerUp(ev);
//                mLastMotionY = (int) MotionEventCompat.getY(ev,
//                        MotionEventCompat.findPointerIndex(ev, mActivePointerId));
//                break;
//        }
//
//        if (mVelocityTracker != null) {
//            mVelocityTracker.addMovement(vtev);
//        }
//        vtev.recycle();
//        return true;
//    }


//    @Override
//    public boolean dispatchTouchEvent(MotionEvent ev) {
//        final int actionMasked = MotionEventCompat.getActionMasked(ev);
//        if (actionMasked == MotionEvent.ACTION_UP || actionMasked == MotionEvent.ACTION_CANCEL) {
//            tryBounceBack();
//        }
//        return super.dispatchTouchEvent(ev);
//    }

    float rawX;
    float rawY;

    View touchTarget;


    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
//        if ((android.os.Build.VERSION.SDK_INT < 21 && mTarget instanceof AbsListView)
//                || (mTarget != null && !ViewCompat.isNestedScrollingEnabled(mTarget))) {
//            // Nope.
//        } else {
//            super.requestDisallowInterceptTouchEvent(b);
//        }
    }

    private boolean mIsInterupt;

    private int mTouchSlop;
    int mActivePointerId;
    float mInitialDownY;
    boolean mIsBeingDragged;

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
//        System.out.println("---------------------------------------------");
//        System.out.println("Mask:"+ev.getActionMasked());
//        System.out.println("Orig:"+ev.getAction());
//        System.out.println("Cout:"+ev.getPointerCount());
//        System.out.println("Indx:          "+(ev.getActionIndex()));
//        for(int i=0;i<ev.getPointerCount();i++){
//            System.out.println("Poid:"+ev.getPointerId(i));
//        }


        switch (ev.getActionMasked()) {


            case MotionEvent.ACTION_DOWN:
                mIsInterupt = false;
                touchTarget = getTouchTarget(ev);
                stopAllScroll();
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN:
                mActivePointerId ++;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                mActivePointerId --;
                break;
            case MotionEvent.ACTION_MOVE:
                float y = ev.getY(mActivePointerId);
                int yDiff = (int) (mInitialDownY - y);
                int preY = offSetChildrenPreLocation(yDiff);
                if (preY != 0) {
                    mInitialDownY = ev.getY(mActivePointerId);
                    mIsInterupt = true;
                    return true;
                }


                super.dispatchTouchEvent(ev);


//                System.out.println("------------------------------------::" + (!touchTarget.canScrollVertically(yDiff)));
//                System.out.println("------------------------------------::" + (!touchTarget.canScrollVertically(-yDiff)));
//
//                System.out.println("------------------------------------------::" + (yDiff < 0));
//                System.out.println("------------------------------------------::" + (yDiff > 0));

                int dirction = yDiff == 0 ? 0 : yDiff > 0 ? 1 : -1;

                if (yDiff < 0 && (!touchTarget.canScrollVertically(dirction))) {
                    if (offSetChildrenLasLocation(yDiff) != 0) {
                        mIsInterupt = true;
                    }
                }


                mInitialDownY = ev.getY(mActivePointerId);
                return true;


            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:

                mIsBeingDragged = false;

                if (mIsInterupt) {
                    MotionEvent cancelEvent = MotionEvent.obtain(ev);
                    cancelEvent.setAction(MotionEvent.ACTION_CANCEL);
                    return super.dispatchTouchEvent(cancelEvent);
                }

                break;
        }

        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            mInitialDownY = ev.getY();
        } else if (ev.getActionMasked() != MotionEvent.ACTION_UP && ev.getActionMasked() != MotionEvent.ACTION_CANCEL) {
            mInitialDownY = ev.getY(mActivePointerId);
        }


        return super.dispatchTouchEvent(ev);


    }


//    @Override
//    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        switch (ev.getActionMasked()){
//            case MotionEvent.ACTION_DOWN:
//                touchTarget = getTouchTarget(ev);
//                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
//                mInitialDownY = ev.getY(mActivePointerId);
//                stopAllScroll();
//            case MotionEvent.ACTION_MOVE:
//                float y = ev.getY(mActivePointerId);
//                float yDiff = y - mInitialDownY;
//                if (yDiff > mTouchSlop ) {
//                    mIsBeingDragged = true;
//                }
//            case MotionEventCompat.ACTION_POINTER_UP:
//                onSecondaryPointerUp(ev);
//                break;
//            case MotionEvent.ACTION_UP:
//            case MotionEvent.ACTION_CANCEL:
//                mIsBeingDragged = false;
//                mActivePointerId = -1;
//                break;
//        }
//        return mIsBeingDragged;
//    }

    private void onSecondaryPointerUp(MotionEvent ev) {



    }

//    private void onSecondaryPointerUp(MotionEvent ev) {
//        final int pointerIndex = MotionEventCompat.getActionIndex(ev);
//        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
//        if (pointerId == mActivePointerId) {
//            // This was our active pointer going up. Choose a new
//            // active pointer and adjust accordingly.
//            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
//            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
//        }
//    }


    Rect rect = new Rect();

    // 得到能接受此触摸事件的view
    private View getTouchTarget(MotionEvent event) {
        for (int i = 0; i < getChildCount(); i++) {
            View target = getChildAt(i);
            rect.top = target.getTop();
            rect.bottom = target.getBottom();
            rect.left = target.getLeft();
            rect.right = target.getRight();

            //     System.out.println(":::"+target.getClass());
            //     System.out.println(":::::::"+rect);
            //     System.out.println(":::::::::::"+(int)event.getX()+":::"+(int)event.getY());


            if (target.getTop() < event.getY() && event.getY() < target.getBottom() &&
                    target.getLeft() < event.getX() && event.getX() < target.getRight()) {
                return target;
            }


        }
        return null;
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
        isFly = false;
        isInScrollCircle = 3;
        stopAllScroll();
    }

    @Override
    public void onStopNestedScroll(View target) {
        mParentHelper.onStopNestedScroll(target);
        if (!isFly) {
            stopNestedScroll();
            if (!tryBackToRefreshing() && isInScrollCircle < 0) {
                tryBackToFreshFinish();
            }
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        isInScrollCircle--;
        int myConsumed = offSetChildrenLasLocation(dyUnconsumed);
        dyUnconsumed = dyUnconsumed - myConsumed;
        dispatchNestedScroll(0, myConsumed, 0, dyUnconsumed, null);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        isInScrollCircle--;
        int delta = offSetChildrenPreLocation(dy);
        consumed[1] = delta;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if (getScrollY() < scrollY_Down - getHeight()) {
            if (!(tryBackToRefreshing() || tryBackToFreshFinish())) {
                fling((int) velocityY);
                isFly = true;
            }
            return true;
        } else {
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
            int speWid = getChildMeasureSpec(widthMeasureSpec, 0, ViewGroup.LayoutParams.MATCH_PARENT);
            int speHei = getChildMeasureSpec(heightMeasureSpec, 0, ViewGroup.LayoutParams.WRAP_CONTENT);
            child.measure(speWid, speHei);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft = getPaddingLeft();
        int childRight = right - left - getPaddingRight();
        int childTop = 0;
        final int count = getChildCount();

        // 下拉刷新 view
        mSwipeControl.getSwipeView().layout(childLeft, -mSwipeControl.getSwipeView().getMeasuredHeight(), childRight, 0);
        scrollY_Up = -mSwipeControl.getSwipeView().getMeasuredHeight();

        // 中间显示View(上部分);
        for (int i = 1; i < count - 1; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == GONE) break;
            int childHeight = child.getMeasuredHeight();
            SwipeNest.LayoutParams lp = (SwipeNest.LayoutParams) child.getLayoutParams();
            childTop += lp.topMargin;
            child.layout(childLeft, childTop, childRight, childTop + childHeight);
            childTop += childHeight + lp.bottomMargin;

        }

        // 中间显示View(下部分);
        View lastIndexView = getChildAt(getChildCount() - 1);
        if (lastIndexView != null) {
            SwipeNest.LayoutParams lp = (SwipeNest.LayoutParams) lastIndexView.getLayoutParams();
            lastIndexView.layout(childLeft, childTop, childRight, childTop + bottom);
            childTop += bottom + lp.bottomMargin;
        }
        scrollY_Down = childTop;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            scrollTo(0, y);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void finishRefresh() {
        isFreshContinue = false;
        isFreshComplete = true;
        mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_COMPLETE, -getScrollY(),mSwipeControl.getSwipeView().getHeight());
        tryBackToFreshFinish();
    }

    private int offSetChildrenPreLocation(int deltaY) {
        //      System.out.println("oried::::::::::::::::::::::::::::::::;;;"+deltaY);

        if (deltaY == 0) return 0;

        int maxScrollY = scrollY_Down - getHeight();
        int minScrollY = scrollY_Up - mSwipeControl.getOverScrollHei();
        int currentScrollY = getScrollY();

        if (deltaY < 0 && currentScrollY < scrollY_Up && currentScrollY > minScrollY) {                                                  //平滑过度下拉刷新的进度变化
            deltaY = (int) (deltaY * Math.pow((currentScrollY - minScrollY) * 1f / mSwipeControl.getOverScrollHei(), 2.5));

            //       System.out.println("fixed:::::::::::::::::::::;;;"+deltaY);

        }

        //       System.out.println("dataa::::::::::::::::::::::;;;"+currentScrollY);
        //       System.out.println("dataa::::::::::::::::::::::;;;"+minScrollY);
        //       System.out.println("dataa::::::::::::::::::::::;;;"+maxScrollY);
        //      System.out.println("dataa::::::::::::::::::::::;;;"+((currentScrollY>=minScrollY && currentScrollY<maxScrollY)));
        //       System.out.println("dataa::::::::::::::::::::::;;;"+(getChildCount()==2 && currentScrollY==0 && !getChildAt(1).canScrollVertically(deltaY)));

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
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_LOADING, swipeViewVisibilityHei,mSwipeControl.getSwipeView().getHeight());
                } else if (isFreshComplete) {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_COMPLETE, swipeViewVisibilityHei,mSwipeControl.getSwipeView().getHeight());
                } else if (willTo < -mSwipeControl.getSwipeView().getHeight()) {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_OVER, swipeViewVisibilityHei,mSwipeControl.getSwipeView().getHeight());
                } else {
                    mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_TOAST, swipeViewVisibilityHei,mSwipeControl.getSwipeView().getHeight());
                }
            }

            //      System.out.println("inend::::::::::::::::::::::::::::" + (willTo - currentScrollY));

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

            System.out.println("wwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwwww:" + (willTo - currentScrollY));
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
                        isFreshContinue = true;
                        mSwipeControl.onSwipeStatue(SwipeControl.SwipeStatus.SWIPE_LOADING, mSwipeControl.getSwipeView().getHeight(),mSwipeControl.getSwipeView().getHeight());
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
                animationReBackToTop.setStartDelay(300);
            }
            animationReBackToTop.start();

            return true;
        }

        return false;
    }

    private void fling(int velocityY) {
        mScroller.abortAnimation();
        mScroller.fling(0, getScrollY(), 0, (int) (velocityY * 1f), 0, 0, 0, scrollY_Down - getHeight());
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void stopAllScroll() {
        if (animationReBackToRefreshing != null && animationReBackToRefreshing.isRunning()) {
            animationReBackToRefreshing.cancel();
        }
        if (animationReBackToTop != null && animationReBackToTop.isRunning()) {
            animationReBackToTop.cancel();
        }
        if (mScroller != null && !mScroller.isFinished()) {
            mScroller.abortAnimation();
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


    class AnimationStatus implements Animator.AnimatorListener {
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






}
