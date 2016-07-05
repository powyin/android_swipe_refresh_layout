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
import android.util.Log;
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

    private ScrollerCompat mScroller;

    private SwipeHeadControl mSwipeControl;


    private int scrollY_Up;
    private int scrollY_Down;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mScroller = ScrollerCompat.create(getContext());
        mSwipeControl = new DefalutHeadControlerIMP(getContext());
        addView(mSwipeControl.getSwipeView(), 0);
    }


    // NestedScrollingChild

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



    // NestedScrollingParent

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {

        boolean tf = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;

        Log.i("NestedScrollingParent", "onStartNestedScroll:::::::" + tf);
        return tf;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        Log.i("NestedScrollingParent", "onNestedScrollAccepted");

        mParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);

        if(animationReBackToRefreshing !=null && animationReBackToRefreshing.isRunning()){
            animationReBackToRefreshing.cancel();
        }
    }

    @Override
    public void onStopNestedScroll(View target) {
        Log.i("NestedScrollingParent", "onStopNestedScroll");

        mParentHelper.onStopNestedScroll(target);

        tryBackToRefreshing();

        stopNestedScroll();
    }


    private ValueAnimator animationReBackToRefreshing;
    private ValueAnimator animationReBackToTop;


    public void reFreshCompleteUp(){
        isFreshing = false;
        isFreshComplete = true;
        mSwipeControl.onSwipeStatue(SwipeHeadControl.SwipeStatus.SWIPE_COMPLITE,-getScrollY());
        tryBackToFreshFinish();
    }


    private boolean tryBackToRefreshing(){
        int scrollY = getScrollY();

        if(animationReBackToRefreshing !=null && animationReBackToRefreshing.isRunning()){
            animationReBackToRefreshing.cancel();
        }
        if(animationReBackToTop !=null && animationReBackToTop.isRunning()){
            animationReBackToTop.cancel();
        }

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
                        isFreshing = true;
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

        int scrollY = getScrollY();

        if(animationReBackToRefreshing !=null && animationReBackToRefreshing.isRunning()){
            animationReBackToRefreshing.cancel();
        }
        if(animationReBackToTop !=null && animationReBackToTop.isRunning()){
            animationReBackToTop.cancel();
        }

        if(scrollY<0){

            animationReBackToTop = ValueAnimator.ofInt(scrollY,0);
            animationReBackToTop.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    scrollTo(0, value);
                }
            });

            animationReBackToTop.addListener(new AnimationStatus(){
                @Override
                public void onAnimationEnd(Animator animation) {
                    isFreshComplete = false;
                }
            });

            animationReBackToTop.setDuration(Math.abs(350*(0-scrollY)/mSwipeControl.getSwipeView().getHeight()));

            if(scrollY<= -mSwipeControl.getSwipeView().getHeight() + 10){
                animationReBackToTop.setStartDelay(300);
            }

            animationReBackToTop.start();

            return true;
        }else {
            isFreshComplete = false;
        }
        return false;
    }




    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        Log.i("NestedScrollingParent", "onNestedScroll:  dyConsumed" + dyConsumed + "  dyUnConsumed:" + dyUnconsumed);
        int myConsumed =0;
        myConsumed = offSetChildrenLasLocation(dyUnconsumed);

        dyUnconsumed = dyUnconsumed - myConsumed;
        dispatchNestedScroll(0, myConsumed, 0, dyUnconsumed, null);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        int delta = offSetChildrenPreLocation(dy);
        consumed[0] = 0;
        consumed[1] = delta;
        Log.i("consumedY:", consumed[1] + "");
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Log.i("NestedScrollingParent", "onNestedFling");
        return false;
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        Log.i("NestedScrollingParent","onNestedPreFling");

        int scrollY = getScrollY();

        if(scrollY>0 && scrollY < scrollY_Down - getHeight()){
            fling((int)velocityY);
            return true;
        }

        return false;

    }

    @Override
    public int getNestedScrollAxes() {
        Log.i("NestedScrollingParent", "getNestedScrollAxes");
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
        layoutVertical(left, top, right, bottom);
    }

    void layoutVertical(int left, int top, int right, int bottom) {
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

    //

    public void fling(int velocityY) {

        System.out.println("velocityY"+velocityY);

        mScroller.abortAnimation();
        mScroller.fling(0,getScrollY(),0,(int)( velocityY * 1f ),0,0,0,scrollY_Down-getHeight());

        ViewCompat.postInvalidateOnAnimation(this);
    }

    boolean isFreshing = false;
    boolean isFreshComplete = false;

    private int offSetChildrenPreLocation(int deltaY) {
        if(deltaY==0) return 0;

        int maxScrollY = scrollY_Down - getHeight() ;
        int minScrollY = scrollY_Up - mSwipeControl.getOverScrollHei();
        int currentScrollY = getScrollY();

        if(deltaY<0 && currentScrollY<scrollY_Up && currentScrollY>minScrollY){                                                  //平滑过度下拉刷新的进度变化
            deltaY = (int) ( deltaY * Math.pow ((currentScrollY - minScrollY)*1f/mSwipeControl.getOverScrollHei(),2.5) );
        }

        if((currentScrollY>=minScrollY && currentScrollY<maxScrollY) ||                                                          //提供对多个View的支持
                (getChildCount()==2 && currentScrollY==0 && !getChildAt(1).canScrollVertically(deltaY))){                        //提供对单个Veiw的支持
            int willTo = currentScrollY+deltaY;
            willTo=Math.min(willTo,maxScrollY);
            willTo = Math.max(willTo,minScrollY);
            scrollTo(0, willTo);

            int swipeViewVisibilityHei = 0 -willTo;
            if(swipeViewVisibilityHei>0){                                                                                       //更新刷新状态
                if(isFreshing){
                    mSwipeControl.onSwipeStatue(SwipeHeadControl.SwipeStatus.SWIPE_LOADING,swipeViewVisibilityHei);
                }else if(isFreshComplete){
                    mSwipeControl.onSwipeStatue(SwipeHeadControl.SwipeStatus.SWIPE_COMPLITE ,swipeViewVisibilityHei);
                }else {
                    mSwipeControl.onSwipeStatue(SwipeHeadControl.SwipeStatus.SWIPE_PRE, swipeViewVisibilityHei);
                }
            }
            return (willTo-currentScrollY);
        }



        return 0;
    }

    private int offSetChildrenLasLocation(int deltaY) {
        if(deltaY==0) return 0;

        int consumed = 0;
        int currentScrollY ;
        int maxScrollY;
        int minScrollY;

        //  中间内容部分
        maxScrollY = scrollY_Down - getHeight();
        minScrollY = 0;
        currentScrollY = getScrollY();

        if(currentScrollY>=minScrollY){
            int willTo = currentScrollY+deltaY;
            willTo=Math.min(willTo,maxScrollY);
            willTo = Math.max(willTo,minScrollY);
            scrollTo(0, willTo);
            consumed += (willTo-currentScrollY);
            deltaY-=consumed;
            System.out.println("ccccccccccc:mmmmmmm:"+consumed+"---"+deltaY);
        }

        return consumed;
    }




    // 滚动相关-----------------------------------------------------------------------------------------------------------------

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
    }



    @Override
    public void computeScroll() {
        super.computeScroll();
        int scrollY = getScrollY();

        if(scrollY<0){
          //  mSwipeControl.onSwipeStatue(SwipeHeadControl.SwipeStatus.SWIPE_LOADING,20);
        }
        if (mScroller.computeScrollOffset()) {
            int y = mScroller.getCurrY();
            scrollTo(0,y);
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }
    //---------------------------------------------------------------------------------------------------------------------------

//
//    private class ViewHolderHeaderProgress {
//        private boolean isRefreshCompleteUp = false;
//        private boolean isRefreshing = false;
//        View rootView ;
//        View contentView;
//        CircleViewBac circleViewBac;
//        ImageView imageView;
//        TextView textInfo;
//        private class CircleViewBac extends View {
//            public CircleViewBac(Context context) {
//                super(context);
//            }
//
//            float progress;
//            boolean isFreshComplete;
//            boolean isFreshing;
//
//            public void setProgress(float progress , boolean isFreshComplete, boolean isFreshing){
//                this.progress = progress;
//                this.isFreshComplete = isFreshComplete;
//                this.isFreshing = isFreshing;
//                invalidate();
//            }
//
//            @Override
//            protected void onDraw(Canvas canvas) {
//                super.onDraw(canvas);
//                drawCircle(canvas);
//                drawTag(canvas);
//            }
//
//            Paint arcPaint;
//            RectF arcRectF;
//            int padding = 2;
//            @Override
//            protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//                super.onSizeChanged(w, h, oldw, oldh);
//                arcRectF = new RectF(padding, padding,w- padding,h- padding);
//                arcPaint = new Paint();
//                arcPaint.setStrokeWidth(2);
//                arcPaint.setStyle(Paint.Style.STROKE);
//                arcPaint.setColor(Color.RED);
//                arcPaint.setAntiAlias(true);
//            }
//
//            private void drawCircle(Canvas canvas){
//                int sweepAngle = progress>=0 && progress<=1? (int)(360*progress) : 360;
//                canvas.drawArc(arcRectF,90,sweepAngle,false,arcPaint);
//
//                imageView.setRotation(sweepAngle);
//
//                View view = null;
//
//
//                System.out.println(progress+"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
//
//            }
//
//            private void drawTag(Canvas canvas){
//                if(progress<0.9f) return;
//            }
//        }
//
//        void updateUI(){
//            if(isRefreshCompleteUp){
//                if(!"刷新成功".equals(textInfo.getText().toString()))
//                textInfo.setText("刷新成功");
//            }else if(isRefreshing){
//                if(!"正在拼命刷新中".equals(textInfo.getText().toString()))
//                textInfo.setText("正在拼命刷新中");
//            }else {
//                if(!"上拉刷新".equals(textInfo.getText().toString()))
//                textInfo.setText("上拉刷新");
//            }
//
//            int scrollY =  Math.abs(getScrollY());
//            scrollY = (int)(1.6f*(scrollY- (int)(contentView.getHeight()*0.68f))) ;
//            scrollY = Math.max(0,scrollY);
//
//            float progress = Math.abs(1f*scrollY/contentView.getHeight());
//
//
//            circleViewBac.setProgress(progress,true,true);
//
//        }
//
//        ViewHolderHeaderProgress(){
//            LinearLayout.LayoutParams params;
//
//            LinearLayout content = new LinearLayout(getContext());
//            content.setOrientation(LinearLayout.VERTICAL);
//            content.setGravity(Gravity.CENTER);
//
//            params = new LinearLayout.LayoutParams(12,12);
//            Space space1 = new Space(getContext());
//            content.addView(space1,params);
//
//            params = new LinearLayout.LayoutParams(80,80);
//            CircleViewBac progressBar = new CircleViewBac(getContext());
//            content.addView(progressBar,params);
//            circleViewBac = progressBar;
//
//            params = new LinearLayout.LayoutParams(80,80);
//            params.setMargins(0,-80,0,0);
//            ImageView imageView = new ImageView(getContext());
//            imageView.setPadding(10,10,10,10);
//            imageView.setImageResource(R.drawable.progress_update);
//            content.addView(imageView,params);
//            this.imageView=imageView;
//
//            params = new LinearLayout.LayoutParams(8,8);
//            Space space2 = new Space(getContext());
//            content.addView(space2,params);
//
//            params = new LinearLayout.LayoutParams(-2,-2);
//            TextView textView = new TextView(getContext());
//            textView.setText("载入中...");
//            textView.setTextSize(11);
//            content.addView(textView,params);
//
//            params = new LinearLayout.LayoutParams(8,8);
//            Space space3 = new Space(getContext());
//            content.addView(space3,params);
//
//            textInfo=textView;
//
//            contentView=content;
//
//            LinearLayout root = new LinearLayout(getContext());
//            root.setOrientation(LinearLayout.VERTICAL);
//            root.setGravity(Gravity.CENTER);
//
//            params = new LinearLayout.LayoutParams(150,150);
//            Space topSpace = new Space(getContext());
//            root.addView(topSpace,params);
//
//            params = new LinearLayout.LayoutParams(-1,-2);
//            root.addView(content,params);
//
//
//            rootView = root;
//
//        }
//    }





    //layoutParams---------------------------------------------------------------------------------------------------------------
    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        System.out.println("layoutParams info "+"checkLayoutParams");
        return p instanceof LayoutParams;
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        System.out.println("layoutParams info "+"generateDefaultLayoutParams");
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        System.out.println("layoutParams info "+"generateLayoutParams");
        return new LayoutParams(p);
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        System.out.println("layoutParams info "+"generateLayoutParams");
        return new LayoutParams(getContext(), attrs);
    }

    //---------------------------------------------------------------------------------------------------------------------------

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

    public interface SwipeListener {
        void onPullProgress(ScrollCombine view, int state, float progress);

        void onRefreshHeader();
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
