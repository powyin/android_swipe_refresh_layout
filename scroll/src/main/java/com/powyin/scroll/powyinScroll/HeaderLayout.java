package com.powyin.scroll.powyinScroll;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.VelocityTrackerCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ScrollerCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.powyin.scroll.powyinScroll.edge.EdgeController;
import com.powyin.scroll.powyinScroll.edge.HeaderEdgeController;


/**
 * Created by MT3020 on 2016/3/10.
 */
public class HeaderLayout extends ViewGroup implements NestedScrollingParent,NestedScrollingChild{

    private static final String TAG = HeaderLayout.class.getCanonicalName();

    // 定位 当前获得触摸事件的  子view；
    int[] mChildLocation = new int[2];
    RectF mChildRectF = new RectF();
    private View mChildTouchView;
    // ---------------------------

    private int sumViewHei = 0;




    public static final int STATE_IDLE = 0;
    public static final int STATE_DRAGE = 1;
    public static final int STATE_FLING = 2;
    public static final int STATE_BOUNCE = 3;


    protected EdgeController mHeaderController;
    protected EdgeController mButtonController;


    private VelocityTracker mVelocityTracker;
    private ValueAnimator mBounceAnim;
    private int mPullState = STATE_IDLE;

    private static final int INVALID_POINTER = -1;
    private int mActivePointerId = INVALID_POINTER;
    private boolean mIsBeingDragged = false;
    private int mLastMotionY = 0;

    private int mTouchSlop;
    private int mMinimumVelocity;
    private int mMaximumVelocity;


    private int mNestedYOffset;

    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];

    private final NestedScrollingParentHelper mParentHelper;
    private final NestedScrollingChildHelper mChildHelper;

    private ScrollerCompat mScroller;

    private OnPullListener mPullListener;

    public HeaderLayout(Context context) {
        this(context, null);
    }

    public HeaderLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mHeaderController = new HeaderEdgeController(context);

        mParentHelper = new NestedScrollingParentHelper(this);
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        init();
    }

    private void init() {
        mScroller = ScrollerCompat.create(getContext());
        final ViewConfiguration configuration = ViewConfiguration.get(getContext());
//        mTouchSlop = configuration.getScaledTouchSlop();
        mTouchSlop = 5;
        mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();



        setHeaderEdgeController(new HeaderEdgeController(getContext()));

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
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
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

        Log.i("NestedScrollingParent", "onStartNestedScroll");

        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        Log.i("NestedScrollingParent","onNestedScrollAccepted");

        mParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
        startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onStopNestedScroll(View target) {
        Log.i("NestedScrollingParent", "onStopNestedScroll");



        tryReBack();

        stopNestedScroll();
    }

    private ValueAnimator preReBackAnimator;
    private ValueAnimator reBackAnimator;

    private void tryReBack(){
        int scrollY = getScrollY();

        if(scrollY!=0){
            if(preReBackAnimator!=null && preReBackAnimator.isRunning()){
                preReBackAnimator.cancel();
            }

            if(reBackAnimator!=null && reBackAnimator.isRunning()){
                reBackAnimator.cancel();
            }

            if( mHeaderController.isOverScroll()){
                startPreReBack();
            }else {
                startReBack();
            }
        }
    }


    private void doReflush(){
        postDelayed(new Runnable() {
            @Override
            public void run() {
                startReBack();
            }
        }, 600);
    }


    private void startPreReBack(){
        if(!mHeaderController.canScroll()) return;


        int scrollY = getScrollY();
        preReBackAnimator = ValueAnimator.ofInt(scrollY,mHeaderController.getShowHeight());
        preReBackAnimator .setDuration((int)Math.abs(300*(1f*
                (scrollY-mHeaderController.getShowHeight())/(mHeaderController.getExpandHeight()-mHeaderController.getShowHeight()))));
        preReBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentY = (int) animation.getAnimatedValue();
                scrollTo(0, currentY);
                mHeaderController.setScroll(currentY);
            }
        });
        preReBackAnimator.addListener(new Animator.AnimatorListener() {
            boolean cancel = false;

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!cancel) {
                    doReflush();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                cancel = true;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });


        preReBackAnimator.start();

    }

    private void startReBack(){
        if(!mHeaderController.canScroll()) return;

        if(reBackAnimator!=null && reBackAnimator.isRunning()){
            reBackAnimator.cancel();
        }
        int scrollY = getScrollY();


        reBackAnimator = ValueAnimator.ofInt(scrollY,0);
        reBackAnimator.setDuration(200);
        reBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentY = (int) animation.getAnimatedValue();
                scrollTo(0, currentY);
                mHeaderController.setScroll(currentY);
            }
        });
        reBackAnimator.start();
    }


    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed,
                               int dyUnconsumed) {
        Log.i("NestedScrollingParent", "onNestedScroll:  dyConsumed" + dyConsumed + "  dyUnConsumed:" + dyUnconsumed);


        // 下拉分发
        if(dyUnconsumed< 0 ){
            int myConsumed = moveBy(dyUnconsumed);
            dyUnconsumed = dyUnconsumed - myConsumed;
            dispatchNestedScroll(0, myConsumed, 0, dyUnconsumed, null);
        }else {
            dispatchNestedScroll(0, 0, 0, dyUnconsumed, null);
        }

    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        Log.i("NestedScrollingParent", "onNestedPreScroll" + consumed[1] + ":" + dy);

        // 上拉分发
        if (dy > 0 ) {
            final int delta = moveBy(dy);
            consumed[0] = 0;
            consumed[1] = delta;
        }


        Log.i("consumedY:", consumed[1] + "");
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        Log.i("NestedScrollingParent","onNestedFling");

//        if (!consumed) {
//            flingWithNestedDispatch((int) velocityY);
//            return true;
//        }

        return false;
    }


    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        int scrollY = getScrollY();

        if(scrollY>0 && scrollY < sumViewHei - getHeight()){
            fling((int)velocityY);
            return true;
        }else {
            return false;
           // return flingWithNestedDispatch((int) velocityY);
        }


       // return flingWithNestedDispatch((int) velocityY);
    }

    private boolean flingWithNestedDispatch(int velocityY) {
        final boolean canFling = (mHeaderController.canScroll() && velocityY > 0) ||
                (mHeaderController.canScroll() && velocityY < 0);
        if (!dispatchNestedPreFling(0, velocityY)) {
            dispatchNestedFling(0, velocityY, canFling);
            if (canFling) {
                fling(velocityY);
            }
        }
        return canFling;
    }





    @Override
    public int getNestedScrollAxes() {
        Log.i("NestedScrollingParent", "getNestedScrollAxes");
        return mParentHelper.getNestedScrollAxes();
    }








    public void setOnPullListener(OnPullListener listener) {
        mPullListener = listener;
    }

    public void setHeaderEdgeController(EdgeController headController) {
        mHeaderController = headController;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {   //下拉刷新 ui 展示
        canvas.save();
        canvas.translate(0, getScrollY());

        if(mHeaderController!=null){
            mHeaderController.onPullProgress(canvas);
        }
        canvas.restore();
        super.dispatchDraw(canvas);



    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int consumeHei = 0;

        for(int i=0;i<getChildCount();i++){
            View child = getChildAt(i);
            measureChildWithMargins(child,widthMeasureSpec,0,heightMeasureSpec,consumeHei);
            consumeHei += child.getMeasuredHeight();
        }
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        layoutVertical(left,top,right,bottom);
    }

    void layoutVertical(int left, int top, int right, int bottom) {


        int childLeft= getPaddingLeft();
        int childRight = right - left - getPaddingRight();
        int childTop = getPaddingTop();


        // Space available for child
        int childSpace = childRight-childLeft;

        final int count = getChildCount();

        for (int i = 0; i < count -1; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE ) {
                final int childWidth = child.getMeasuredWidth();
                final int childHeight = child.getMeasuredHeight();
                final HeaderLayout.LayoutParams lp =
                        (HeaderLayout.LayoutParams) child.getLayoutParams();
                childTop += lp.topMargin;
                child.layout(childLeft, childTop,
                        childRight, childTop + childHeight);
              //  System.out.println("layout::"+childLeft+":"+childTop+":"+childRight+":"+(childTop+childHeight)+":::"+child.getClass());
                childTop += childHeight + lp.bottomMargin;
            }
        }

        View lastIndexView = getChildCount()>0 ? getChildAt(getChildCount()-1) : null;

        if(lastIndexView instanceof  NestedScrollingChild){
            sumViewHei = childTop+bottom;
            lastIndexView.layout(childLeft, childTop, childRight, sumViewHei);
        }else if(lastIndexView!=null){
            sumViewHei = childTop + lastIndexView.getMeasuredHeight();
            lastIndexView.layout(childLeft,childTop,childRight,sumViewHei);
        }


    }



    private void obtainVelocityTracker(MotionEvent event) {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
    }

    private void initOrResetVelocityTracker() {
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        } else {
            mVelocityTracker.clear();
        }
    }

    private void releaseVelocityTracker() {
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >>
                MotionEventCompat.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            // This was our active pointer going up. Choose a new
            // active pointer and adjust accordingly.
            // TODO: Make this decision more intelligent.
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mLastMotionY = (int) MotionEventCompat.getY(ev, newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }

    private void endDrag() {
        mIsBeingDragged = false;
        releaseVelocityTracker();
    }

    // 事件分发三部曲

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

     //   super.dispatchTouchEvent(ev);

        System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiii"+ev.getAction());

        final int action = ev.getAction();
        if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
            return true;
        }

        if(!isEnabled()) {


            return false;
        }

        switch (action & MotionEventCompat.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE: {
                System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiii ACTION_MOVE::"+"1");

                final int activePointerId = mActivePointerId;
                if (activePointerId == INVALID_POINTER) {
                    // If we don't have a valid id, the touch down wasn't on content.
                    break;
                }

                System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiii ACTION_MOVE::"+"2");

                final int pointerIndex = MotionEventCompat.findPointerIndex(ev, activePointerId);
                if (pointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + activePointerId
                            + " in onInterceptTouchEvent");
                    break;
                }

                System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiii ACTION_MOVE::"+"3");

                final int y = (int) MotionEventCompat.getY(ev, pointerIndex);

                final int yDiff = Math.abs(y - mLastMotionY);


                System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiii ACTION_MOVE::::::"+y);
                System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiii ACTION_MOVE::::::    "+(yDiff>mTouchSlop));
                System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiii ACTION_MOVE::::::    "+getNestedScrollAxes());
                System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiii ACTION_MOVE::::::    "+ViewCompat.SCROLL_AXIS_VERTICAL);


                if ((yDiff > mTouchSlop && (getNestedScrollAxes() & ViewCompat.SCROLL_AXIS_VERTICAL) == 0)||
                    (yDiff > mTouchSlop && !(mChildTouchView instanceof NestedScrollingChild))                               //为了党
                   ) {
                    mIsBeingDragged = true;
                    mLastMotionY = y;
                    obtainVelocityTracker(ev);
                    mNestedYOffset = 0;
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }

                System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiii ACTION_MOVE::"+"4");
                break;
            }

            case MotionEvent.ACTION_DOWN: {

                // 为了查找子视图中接受这次点击事件的视图   折中的方法
                findChildTouchView(ev);

                final int y = (int) ev.getY();



                System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiii ACTION_DOWN::"+"1");

                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
                mLastMotionY = y;
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);

                initOrResetVelocityTracker();
                mVelocityTracker.addMovement(ev);
                /*
                * If being flinged and user touches the screen, initiate drag;
                * otherwise don't.  mScroller.isFinished should be false when
                * being flinged.
                */

                System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiii ACTION_DOWN::"+"2");

                mIsBeingDragged = !mScroller.isFinished();

                System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiii ACTION_DOWN::"+mIsBeingDragged);
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            }

            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                /* Release the drag */
                mIsBeingDragged = false;
                mActivePointerId = INVALID_POINTER;
                endDrag();
                stopNestedScroll();
                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        System.out.println("iiiiiiiiiiiiiiiiiiiiiiiiii       -----------------"+mIsBeingDragged);

        return mIsBeingDragged;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        MotionEvent vtev = MotionEvent.obtain(ev);

        final int actionMasked = MotionEventCompat.getActionMasked(ev);

        if (actionMasked == MotionEvent.ACTION_DOWN) {
            mNestedYOffset = 0;
        }
        vtev.offsetLocation(0, mNestedYOffset);

        switch (actionMasked) {
            case MotionEvent.ACTION_DOWN: {
                if ((mIsBeingDragged = !mScroller.isFinished())) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }

                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
                if (!mScroller.isFinished()) {
                    mScroller.abortAnimation();
                }

                // Remember where the motion event started
                mLastMotionY = (int) ev.getY();
                mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                startNestedScroll(ViewCompat.SCROLL_AXIS_VERTICAL);
                break;
            }
            case MotionEvent.ACTION_MOVE:
                final int activePointerIndex = MotionEventCompat.findPointerIndex(ev,
                        mActivePointerId);
                if (activePointerIndex == -1) {
                    Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
                    break;
                }

                final int y = (int) MotionEventCompat.getY(ev, activePointerIndex);
                int deltaY = mLastMotionY - y;
                if (dispatchNestedPreScroll(0, deltaY, mScrollConsumed, mScrollOffset)) {
                    deltaY -= mScrollConsumed[1];
                    vtev.offsetLocation(0, mScrollOffset[1]);
                    mNestedYOffset += mScrollOffset[1];
                }
                if (!mIsBeingDragged && Math.abs(deltaY) > mTouchSlop) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    mIsBeingDragged = true;
                    if (deltaY > 0) {
                        deltaY -= mTouchSlop;
                    } else {
                        deltaY += mTouchSlop;
                    }
                }
                if (mIsBeingDragged) {
                    // Scroll to follow the motion event
                    mLastMotionY = y - mScrollOffset[1];

                    final int scrolledDeltaY = moveBy(deltaY);
                    final int unconsumedY = deltaY - scrolledDeltaY;
                    if (dispatchNestedScroll(0, scrolledDeltaY, 0, unconsumedY, mScrollOffset)) {
                        mLastMotionY -= mScrollOffset[1];
                        vtev.offsetLocation(0, mScrollOffset[1]);
                        mNestedYOffset += mScrollOffset[1];
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mIsBeingDragged) {
                    final VelocityTracker velocityTracker = mVelocityTracker;
                    velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                    int initialVelocity = (int) VelocityTrackerCompat.getYVelocity(velocityTracker,
                            mActivePointerId);

                    if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
                        flingWithNestedDispatch(-initialVelocity);
                    }

                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEvent.ACTION_CANCEL:
                if (mIsBeingDragged && getChildCount() > 0) {
                    mActivePointerId = INVALID_POINTER;
                    endDrag();
                }
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                final int index = MotionEventCompat.getActionIndex(ev);
                mLastMotionY = (int) MotionEventCompat.getY(ev, index);
                mActivePointerId = MotionEventCompat.getPointerId(ev, index);
                break;
            }
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                mLastMotionY = (int) MotionEventCompat.getY(ev,
                        MotionEventCompat.findPointerIndex(ev, mActivePointerId));
                break;
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(vtev);
        }
        vtev.recycle();
        return true;
    }


    private void findChildTouchView(MotionEvent event){
        for(int i=0;i<getChildCount();i++){
            View view  = getChildAt(i);
            view.getLocationInWindow(mChildLocation);

            mChildRectF.left=mChildLocation[0];
            mChildRectF.right = mChildRectF.left+view.getWidth();
            mChildRectF.top = mChildLocation[1];
            mChildRectF.bottom = mChildRectF.top+view.getHeight();

            if(mChildRectF.contains(event.getRawX(),event.getRawY())){
                mChildTouchView = view;
                return;
            }
        }
        mChildTouchView = null;
    }

    //

    public void fling(int velocityY) {

        System.out.println("velocityY"+velocityY);

        mPullState = STATE_FLING;
        mScroller.abortAnimation();
        mScroller.fling(0,getScrollY(),0,(int)( velocityY * 0.55f ),0,0,0,sumViewHei-getHeight());

        ViewCompat.postInvalidateOnAnimation(this);
    }

    private int moveBy(final int deltaY) {

        int consumed = 0;


        if(deltaY==0) return 0;

        int currentScrollY = getScrollY();

        int maxScrollY = sumViewHei - getHeight() ;

        if(currentScrollY>=0 && currentScrollY<=maxScrollY){

            int willTo = currentScrollY+deltaY;
            willTo=Math.min(willTo,maxScrollY);
            scrollTo(0,willTo);

            mHeaderController.setScroll(willTo);

            consumed = (willTo-currentScrollY);
        }

        if(mHeaderController.canScroll()){


            int oldScroll = mHeaderController.getScroll();
            int secondConsume = mHeaderController.move(deltaY-consumed);
            consumed += secondConsume;

//            final int delta = mHeaderController.getScroll() - oldScroll;

            scrollBy(0,secondConsume);

            invalidate();


        }

        System.out.println("cp:::" + consumed + "---" + deltaY);

        return consumed;


    }

    @Override
    public void scrollTo(int x, int y) {
        super.scrollTo(x, y);
        mHeaderController.setScroll(y);
    }



    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            int oldY = mHeaderController.getScroll();
            int y = mScroller.getCurrY();

            if (oldY != y) {
                moveBy(y - oldY);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            tryBounceBack();
        }
    }

    private void tryBounceBack() {

    }




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


    public interface OnPullListener {
        void onPullProgress(View view, int state, float progress);
    }
}
