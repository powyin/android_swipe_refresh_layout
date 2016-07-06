package com.powyin.scroll.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;


/**
 * Created by MT3020 on 2016/3/10.
 */
public class ScrollCombine extends ViewGroup implements NestedScrollingParent,NestedScrollingChild{
    public ScrollCombine(Context context) {
        this(context, null);
    }

    public ScrollCombine(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollCombine(Context context, AttributeSet attrs, int defStyleAttr) {
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


    private static final String TAG = ScrollCombine.class.getCanonicalName();
    private final NestedScrollingParentHelper mParentHelper;
    private final NestedScrollingChildHelper mChildHelper;
    private int isInScrollCircle = 3;                                              //辅助nestScrollParent 计算周期
    private boolean isFly = false;                                                 //辅助nestScrollParent 停止fly
    private SwipeHeadControl mSwipeControl;                                        //刷新头部控制器
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
        mSwipeControl = new DefalutHeadControlerIMP(getContext());
        addView(mSwipeControl.getSwipeView(), 0);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
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
        return   (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
        isFly=false;
        isInScrollCircle = 3;
        stopAllScroll();
    }

    @Override
    public void onStopNestedScroll(View target) {
        mParentHelper.onStopNestedScroll(target);
        if(!isFly){
            stopNestedScroll();
            if(!tryBackToRefreshing() && isInScrollCircle<0){
                tryBackToFreshFinish();
            }
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        isInScrollCircle --;
        int myConsumed = offSetChildrenLasLocation(dyUnconsumed);
        dyUnconsumed = dyUnconsumed - myConsumed;
        dispatchNestedScroll(0, myConsumed, 0, dyUnconsumed, null);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        isInScrollCircle --;
        int delta = offSetChildrenPreLocation(dy);
        consumed[1] = delta;
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        if(getScrollY() < scrollY_Down - getHeight()){
            if(!(tryBackToRefreshing() || tryBackToFreshFinish())){
                fling((int)velocityY);
                isFly = true;
            }
            return true;
        }else {
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

        for(int i= 0;i<getChildCount();i++){
            View child = getChildAt(i);
            int speWid = getChildMeasureSpec(widthMeasureSpec, 0, ViewGroup.LayoutParams.MATCH_PARENT);
            int speHei = getChildMeasureSpec(heightMeasureSpec,0,ViewGroup.LayoutParams.WRAP_CONTENT);
            child.measure(speWid,speHei);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int childLeft= getPaddingLeft();
        int childRight = right - left - getPaddingRight();
        int childTop = 0;
        final int count = getChildCount();

        // 下拉刷新 view
        mSwipeControl.getSwipeView().layout(childLeft, -mSwipeControl.getSwipeView().getMeasuredHeight(), childRight, 0);
        scrollY_Up = -mSwipeControl.getSwipeView().getMeasuredHeight();

        // 中间显示View(上部分);
        for (int i = 1; i < count -1; i++) {
            final View child = getChildAt(i);
            if(child.getVisibility()==GONE) break;
            int childHeight = child.getMeasuredHeight();
            ScrollCombine.LayoutParams lp = (ScrollCombine.LayoutParams) child.getLayoutParams();
            childTop += lp.topMargin;
            child.layout(childLeft, childTop, childRight, childTop + childHeight);
            childTop += childHeight + lp.bottomMargin;

        }

        // 中间显示View(下部分);
        View lastIndexView =  getChildAt(getChildCount()-1);
        if(lastIndexView!=null){
            ScrollCombine.LayoutParams lp = (ScrollCombine.LayoutParams) lastIndexView.getLayoutParams();
            lastIndexView.layout(childLeft, childTop, childRight, childTop+bottom);
            childTop += bottom + lp.bottomMargin;
        }
        scrollY_Down = childTop;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            scrollTo(0,y);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    public void finishRefresh(){
        isFreshContinue = false;
        isFreshComplete = true;
        mSwipeControl.onSwipeStatue(SwipeHeadControl.SwipeStatus.SWIPE_COMPLETE,-getScrollY());
        tryBackToFreshFinish();
    }

    private int offSetChildrenPreLocation(int deltaY) {
        if(deltaY==0) return 0;

        int maxScrollY = scrollY_Down - getHeight() ;
        int minScrollY = scrollY_Up - mSwipeControl.getOverScrollHei();
        int currentScrollY = getScrollY();

        if(deltaY<0 && currentScrollY<scrollY_Up && currentScrollY>minScrollY){                                                  //平滑过度下拉刷新的进度变化
            deltaY = (int) ( deltaY * Math.pow ((currentScrollY - minScrollY)*1f/mSwipeControl.getOverScrollHei(),2.5) );
        }

        if((currentScrollY>=minScrollY && currentScrollY<maxScrollY) ||                                                          //提供对多个View的支持
                (getChildCount()==2 && currentScrollY==0 && !getChildAt(1).canScrollVertically(deltaY))){                        //提供对单个View的支持
            int willTo = currentScrollY+deltaY;
            willTo=Math.min(willTo,maxScrollY);
            willTo = Math.max(willTo,minScrollY);
            scrollTo(0, willTo);

            if(willTo<0 && isFreshComplete && !isFreshContinue){                                                                      //刷新内部状态
                isFreshComplete = false;
            }

            int swipeViewVisibilityHei = 0 -willTo;
            if(swipeViewVisibilityHei>0){                                                                                       //更新刷新状态
                if(isFreshContinue){
                    mSwipeControl.onSwipeStatue(SwipeHeadControl.SwipeStatus.SWIPE_LOADING,swipeViewVisibilityHei);
                }else if(isFreshComplete){
                    mSwipeControl.onSwipeStatue(SwipeHeadControl.SwipeStatus.SWIPE_COMPLETE,swipeViewVisibilityHei);
                }else if(willTo < -mSwipeControl.getSwipeView().getHeight()){
                    mSwipeControl.onSwipeStatue(SwipeHeadControl.SwipeStatus.SWIPE_OVER_PRE, swipeViewVisibilityHei);
                }else {
                    mSwipeControl.onSwipeStatue(SwipeHeadControl.SwipeStatus.SWIPE_TOAST, swipeViewVisibilityHei);
                }
            }

            return (willTo-currentScrollY);
        }

        return 0;
    }

    private int offSetChildrenLasLocation(int deltaY) {
        if(deltaY==0) return 0;

        int maxScrollY = scrollY_Down - getHeight();
        int currentScrollY = getScrollY();

        if(currentScrollY>=0){
            int willTo = currentScrollY+deltaY;
            willTo=Math.min(willTo,maxScrollY);
            willTo = Math.max(willTo,0);
            scrollTo(0, willTo);
            return  (willTo-currentScrollY);
        }

        return 0;
    }

    private boolean tryBackToRefreshing(){
        int scrollY = getScrollY();
        stopAllScroll();
        boolean isOverProgress = scrollY<-mSwipeControl.getSwipeView().getHeight();
        if(isOverProgress){
            int animationTarget = -(mSwipeControl.getSwipeView().getHeight());
            animationReBackToRefreshing = ValueAnimator.ofInt(scrollY,animationTarget);
            animationReBackToRefreshing.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    if (value > getScrollY()) {
                        scrollTo(0, value);
                    }
                }
            });
            animationReBackToRefreshing.addListener(new AnimationStatus(){
                boolean isCancel = false;
                @Override
                public void onAnimationEnd(Animator animation) {
                    if(!isCancel){
                        isFreshContinue = true;
                        mSwipeControl.onSwipeStatue(SwipeHeadControl.SwipeStatus.SWIPE_LOADING,mSwipeControl.getSwipeView().getHeight());
                    }
                }
                @Override
                public void onAnimationCancel(Animator animation) {
                    isCancel = true;
                }
            });
            animationReBackToRefreshing.setDuration(Math.abs(400* (animationTarget-scrollY) / mSwipeControl.getSwipeView().getHeight()));
            animationReBackToRefreshing.start();
        }

        return isOverProgress;
    }

    private boolean tryBackToFreshFinish(){
        stopAllScroll();
        int scrollY = getScrollY();
        if(scrollY<0){
            animationReBackToTop = ValueAnimator.ofInt(scrollY,0);
            animationReBackToTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    scrollTo(0, value);
                }
            });
            animationReBackToTop.setDuration(Math.abs(250*(0-scrollY)/mSwipeControl.getSwipeView().getHeight()));
            if(scrollY<= -mSwipeControl.getSwipeView().getHeight() + 10){
                animationReBackToTop.setStartDelay(300);
            }
            animationReBackToTop.start();

            return true;
        }

        return false;
    }

    private void fling(int velocityY) {
        mScroller.abortAnimation();
        mScroller.fling(0,getScrollY(),0,(int)( velocityY * 1f ),0,0,0,scrollY_Down-getHeight());
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void stopAllScroll(){
        if(animationReBackToRefreshing !=null && animationReBackToRefreshing.isRunning()){
            animationReBackToRefreshing.cancel();
        }
        if(animationReBackToTop!=null && animationReBackToTop.isRunning()){
            animationReBackToTop.cancel();
        }
        if(mScroller!=null&& !mScroller.isFinished()){
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


    class AnimationStatus implements  Animator.AnimatorListener{
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
