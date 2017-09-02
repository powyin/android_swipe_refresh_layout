package com.powyin.scroll.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ScrollingView;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.ScrollerCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ListView;
import android.widget.ScrollView;

import com.powyin.scroll.R;


/**
 * Created by powyin on 2016/7/10.       当swipeRefresh 子View没有足够空间滑动时 上拉加载自动关闭
 */
public class SwipeRefresh extends ViewGroup implements NestedScrollingParent, ISwipe {


    private int mTouchSlop;
    private final NestedScrollingParentHelper mParentHelper;
    private boolean mNestedScrollInProgress = false;
    private int mActivePointerId = -1;                                                    //多手指移动中取值ID

    private float mDragBeginY;                                                            //DispatchTouchEvent
    private float mDragBeginDirect;                                                       //InterceptTouchEvent
    private float mDragLastY;                                                             //TouchEvent

    private ScrollerCompat mScroller;
    private VelocityTracker mVelocityTracker;
    private boolean mIsTouchEventMode = false;                                            //DispatchTouchEvent  是否在进行TouchEvent传递
    private boolean mPreScroll;                                                           //DispatchTouchEvent  是否预滚动
    private boolean mDraggedDispatch;                                                     //DispatchTouchEvent  已经打断
    private boolean mDraggedIntercept;                                                    //InterceptTouchEvent 打断

    private SwipeController mSwipeController;                                             //刷新头部控制器
    private EmptyController mEmptyController;                                             //空白控制器
    private ValueAnimator animationScrollY;                                               //滚动 显示正在刷新状态

    private boolean mRefreshStatusContinueRunning = false;                                //下拉刷新 正在刷新
    private FreshStatus mFreshStatus = null;                                              //下拉刷新状态

    private boolean mLoadedStatusContinueRunning = false;                                 //上拉加载 正在加载
    private ISwipe.LoadedStatus mLoadedStatus = null;                                     //下拉刷新状态;

    private SwipeController.SwipeModel mModel = SwipeController.SwipeModel.SWIPE_BOTH;    //刷新模式设置

    private OnRefreshListener mOnRefreshListener;

    private View mViewTop;
    private View mViewBottom;
    private View mEmptyView;

    private View mTargetView;

    private boolean mShowEmptyView;
    private int mContentScroll;

    private int mOverScrollTop;
    private int mOverScrollTopMiddle;
    private int mOverScrollBottomMiddle;
    private int mOverScrollBottom;
    private boolean mLoadMoreOverScroll = true;

    private int mMaxFlingDirection = 0;

    public SwipeRefresh(Context context) {
        this(context, null);
    }

    public SwipeRefresh(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeRefresh(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeRefresh);
            int modelIndex = a.getInt(R.styleable.SwipeRefresh_fresh_model, -1);
            if (modelIndex >= 0 && modelIndex < SwipeController.SwipeModel.values().length) {
                mModel = SwipeController.SwipeModel.values()[modelIndex];
            }
            a.recycle();
        }
        mScroller = ScrollerCompat.create(getContext(), null);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        mParentHelper = new NestedScrollingParentHelper(this);
        initSwipeControl();
    }


    private void initSwipeControl() {
        mSwipeController = new SwipeControllerStyleNormal(getContext());
        mViewTop = mSwipeController.getSwipeHead();
        mViewBottom = mSwipeController.getSwipeFoot();
        addView(mViewTop, 0);
        addView(mViewBottom, getChildCount());
    }

    //--------------------------------------------- NestedScrollingParent -----------------------------------------------------//

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        if (!mLoadedStatusContinueRunning && mOnRefreshListener != null && target instanceof ScrollingView) {
            ScrollingView scrollingView = (ScrollingView) target;
            int range = scrollingView.computeVerticalScrollRange() -
                    scrollingView.computeVerticalScrollOffset() -
                    scrollingView.computeVerticalScrollExtent();

            if (range < 6 * getHeight()) {
                mLoadedStatusContinueRunning = true;
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onLoading();
                }
            }

        }

        mTargetView = target;
        mNestedScrollInProgress = true;
        stopAllScroll();

        return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int nestedScrollAxes) {
        mParentHelper.onNestedScrollAccepted(child, target, nestedScrollAxes);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        int delta = offSetScroll(dy, true);

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
        if (tryBackToRefreshing() || tryBackToFreshFinish() || tryBackToLoading()) {
        }
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        offSetScroll(dyUnconsumed, false);
    }


    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY, boolean consumed) {
        if (velocityY < 0 || mLoadedStatusContinueRunning || mOnRefreshListener == null || !(target instanceof ScrollingView))
            return false;

        float radio = velocityY / getHeight();
        radio = radio > 1 ? radio : 1;
        radio = radio < 10 ? radio : 10;

        ScrollingView scrollingView = (ScrollingView) target;
        int range = scrollingView.computeVerticalScrollRange() -
                scrollingView.computeVerticalScrollOffset() -
                scrollingView.computeVerticalScrollExtent();

        if (range < radio * getHeight()) {
            mLoadedStatusContinueRunning = true;
            if (mOnRefreshListener != null) {
                mOnRefreshListener.onLoading();
            }
        }

        return false;
    }


    @Override
    public boolean onNestedPreFling(View target, float velocityX, float velocityY) {
        int scrollY = getScrollY();
        if (scrollY > 0 && scrollY < mOverScrollBottomMiddle) {
            fling((int) velocityY);
        }
        return scrollY != 0;
    }


    private void fling(int velocityY) {
        if (animationScrollY != null && animationScrollY.isStarted() || velocityY == 0) {
            return;
        }

        int currentScrollY = getScrollY();
        if (mContentScroll == 0 && currentScrollY == 0 && velocityY > 0 && !canChildScrollDown()) {                                          //没有足够容量禁止上拉
            return;
        }

        mMaxFlingDirection = velocityY > 0 ? 1 : -1;
        mScroller.fling(0, currentScrollY, 0, velocityY, 0, 0, -1000000, 1000000, 0, 0);
        ViewCompat.postInvalidateOnAnimation(this);
    }


    private int offSetScroll(int deltaOriginY, boolean pre) {
        int deltaY = deltaOriginY;
        if (deltaOriginY == 0) return 0;

        int currentScrollY = getScrollY();

        if (deltaOriginY < 0 && currentScrollY < -mOverScrollTopMiddle && (mOverScrollTop != mOverScrollTopMiddle)) {                      //下拉刷新过度拉伸 阻尼效果
            deltaY = (int) (deltaY * Math.pow((-mOverScrollTop - currentScrollY) * 1f
                    / (mOverScrollTop - mOverScrollTopMiddle), 3));
            deltaY = deltaY < 0 ? deltaY : -deltaY;
        }

        if (deltaOriginY > 0 && currentScrollY > (mOverScrollBottomMiddle + mContentScroll) &&                                                //上拉加载过度拉伸 阻尼效果
                (mOverScrollBottom != mOverScrollBottomMiddle)) {
            deltaY = (int) (deltaY * Math.pow((mOverScrollBottom + mContentScroll - currentScrollY) * 1f
                    / (mOverScrollBottom - mOverScrollBottomMiddle), 3));
            deltaY = deltaY > 0 ? deltaY : -deltaY;
        }

        if (!(currentScrollY == mContentScroll) || !pre) {
            int willTo = currentScrollY + deltaY;
            willTo = Math.min(willTo, mOverScrollBottom);
            willTo = Math.max(willTo, -mOverScrollTop);

            if ((currentScrollY > 0 && willTo < 0) || (currentScrollY < 0 && willTo > 0)) {                                                   //确保scroll值经过0
                willTo = 0;
            }

            if ((currentScrollY > mContentScroll && willTo < mContentScroll) || (currentScrollY < mContentScroll && willTo > mContentScroll)) {   //确保scroll值经过scrollContent
                willTo = mContentScroll;
            }

            if (mDragBeginDirect > 0 && willTo > mContentScroll) {                                                      //确保上拉刷新独立
                willTo = mContentScroll;
            }
            if (mDragBeginDirect < 0 && willTo < 0) {                                                                  //确保下拉加载独立
                willTo = 0;
            }


            if (willTo > 0 && !canChildScrollDown()) {                                                                                          //确保当mTarget没有足够内容进行独立滑动时 上拉加载不启动
                willTo = 0;
            }

            if (willTo == currentScrollY) {
                return deltaOriginY;
            }

            scrollTo(0, willTo);

            return (deltaOriginY);
        }
        return 0;
    }

    private boolean tryBackToRefreshing() {
        if (mIsTouchEventMode || mOverScrollTopMiddle == 0 ||
                mFreshStatus == FreshStatus.SUCCESS || mFreshStatus == FreshStatus.ERROR || mFreshStatus == FreshStatus.ERROR_NET)
            return false;

        int scrollY = getScrollY();
        if (scrollY <= -mOverScrollTopMiddle) {
            stopAllScroll();
            animationScrollY = ValueAnimator.ofInt(scrollY, -mOverScrollTopMiddle);
            animationScrollY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    if (value > getScrollY()) {
                        scrollTo(0, value);
                    }
                }
            });
            animationScrollY.addListener(new Animator.AnimatorListener() {
                boolean isCancel = false;

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isCancel) {
                        if (mFreshStatus == null) {
                            mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_LOADING, mViewTop.getHeight(), mViewTop.getHeight());

                        }
                        if (!mRefreshStatusContinueRunning) {
                            mRefreshStatusContinueRunning = true;
                            if (mEmptyController != null) {
                                mEmptyController.onSwipeStatue(mFreshStatus);
                            }
                            if (mOnRefreshListener != null) {
                                mOnRefreshListener.onRefresh();
                            }
                        }
                    }

                    isCancel = true;
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
            animationScrollY.setDuration(50 + Math.abs(500 * (-mOverScrollTopMiddle - scrollY) / mViewTop.getHeight()));
            animationScrollY.start();
            return true;
        }
        return false;
    }

    private boolean tryBackToFreshFinish() {
        int scrollY = getScrollY();
        if (mIsTouchEventMode || (mFreshStatus == null && mRefreshStatusContinueRunning && scrollY == -mOverScrollTopMiddle))
            return false;

        if (scrollY < 0) {
            stopAllScroll();
            animationScrollY = ValueAnimator.ofInt(scrollY, 0);
            animationScrollY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    scrollTo(0, value);
                }
            });



            if (scrollY == -mOverScrollTopMiddle) {
                animationScrollY.setDuration(Math.abs(550 * (0 - scrollY) / mViewTop.getHeight()));
                animationScrollY.setStartDelay(650);
            } else {
                animationScrollY.setDuration(320);
            }
            animationScrollY.start();
            return true;
        }
        return false;
    }

    private boolean tryBackToLoading() {
        int scrollY = getScrollY();
        if (mIsTouchEventMode || mOverScrollBottomMiddle == 0 || scrollY < mContentScroll + mOverScrollBottomMiddle || mOverScrollBottomMiddle == mOverScrollBottom)
            return false;

        stopAllScroll();
        animationScrollY = ValueAnimator.ofInt(scrollY, mContentScroll + mOverScrollBottomMiddle);
        animationScrollY.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = (int) animation.getAnimatedValue();
                scrollTo(0, value);
            }
        });
        animationScrollY.setDuration(50 + (int) ((scrollY - mContentScroll - mOverScrollBottomMiddle) * 250f / (mOverScrollBottom - mOverScrollBottomMiddle)));
        animationScrollY.start();

        return true;
    }

    private void stopAllScroll() {
        if (animationScrollY != null) {
            animationScrollY.cancel();
        }

        mScroller.abortAnimation();
    }


    // -------------------------------------------------------------ViewFunc------------------------------------------------------------//

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthTarget = MeasureSpec.getSize(widthMeasureSpec);
        final int heightTarget = MeasureSpec.getSize(heightMeasureSpec);

        final int childWidMeasure = MeasureSpec.makeMeasureSpec(widthTarget, MeasureSpec.EXACTLY);
        final int spHei = MeasureSpec.makeMeasureSpec(heightTarget, MeasureSpec.AT_MOST);

        for (int i = 0; i < getChildCount(); ++i) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            if (mViewTop == child || mViewBottom == child) continue;
            child.measure(childWidMeasure, MeasureSpec.makeMeasureSpec(heightTarget, MeasureSpec.EXACTLY));
        }

        if (mViewTop != null) {
            mViewTop.measure(childWidMeasure, spHei);
        }
        if (mViewBottom != null) {
            mViewBottom.measure(childWidMeasure, spHei);
        }

        setMeasuredDimension(widthTarget, heightTarget);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {

        mContentScroll = 0;
        mOverScrollTop = mViewTop.getMeasuredHeight();
        mOverScrollTopMiddle = mOverScrollTop != 0 ? mOverScrollTop - mSwipeController.getOverScrollHei() : 0;
        mOverScrollTopMiddle = mOverScrollTopMiddle > 0 ? mOverScrollTopMiddle : 0;
        mOverScrollBottomMiddle = mViewBottom.getMeasuredHeight();
        mOverScrollBottom = mLoadMoreOverScroll ? mOverScrollBottomMiddle + (int) ((bottom - top) * 0.2f) : mOverScrollBottomMiddle;

        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == mViewTop || child == mViewBottom || child == mEmptyView) continue;
            if (mShowEmptyView) {
                child.layout(right - left, 0, 2 * (right - left), bottom - top);
            } else {
                child.layout(0, 0, right - left, bottom - top);
            }
        }

        mViewTop.layout(left, -mViewTop.getMeasuredHeight(), right, 0);
        mViewBottom.layout(0, bottom - top, right - left, bottom - top + mOverScrollBottomMiddle);

        if (mEmptyView != null) {
            if (mShowEmptyView) {
                mEmptyView.layout(0, 0, right - left, bottom - top);
            } else {
                mEmptyView.layout(right - left, 0, 2 * (right - left), bottom - top);
            }
        }

        if (mModel == SwipeController.SwipeModel.SWIPE_NONE || mModel == SwipeController.SwipeModel.SWIPE_ONLY_LOADINN) {
            mOverScrollTop = 0;
            mOverScrollTopMiddle = 0;
        }

        if (mModel == SwipeController.SwipeModel.SWIPE_NONE || mModel == SwipeController.SwipeModel.SWIPE_ONLY_REFRESH) {
            mOverScrollBottomMiddle = 0;
            mOverScrollBottom = 0;
        }

    }


    private View findScrollView(View view, int centerX, int centerY, int direction) {
        if (view.getVisibility() == INVISIBLE || view.getVisibility() == GONE) return null;
        if (view == mViewTop || view == mViewBottom || view == mEmptyView) return null;
        int tem[] = new int[2];
        view.getLocationInWindow(tem);
        if (tem[0] < centerX && centerX < tem[0] + view.getWidth() && tem[1] < centerY && centerY < tem[1] + view.getHeight()) {

            if (direction == 0) {
                if (view.canScrollVertically(-1) || view.canScrollVertically(1)) {
                    return view;
                } else {
                    if (view instanceof ViewGroup) {
                        ViewGroup group = (ViewGroup) view;
                        for (int i = 0; i < group.getChildCount(); i++) {
                            View child = findScrollView(group.getChildAt(i), centerX, centerY, direction);
                            if (child != null)
                                return child;
                        }
                    }
                    return null;
                }

            } else {
                if (view.canScrollVertically(direction)) {
                    return view;
                } else {
                    if (view instanceof ViewGroup) {
                        ViewGroup group = (ViewGroup) view;
                        for (int i = 0; i < group.getChildCount(); i++) {
                            View child = findScrollView(group.getChildAt(i), centerX, centerY, direction);
                            if (child != null)
                                return child;
                        }
                    }
                    return null;
                }
            }
        }
        return null;
    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        int action = MotionEventCompat.getActionMasked(ev);
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mDraggedIntercept = false;
            mPreScroll = false;
            mDraggedDispatch = false;
            mActivePointerId = ev.getPointerId(0);
            mDragBeginY = (int) ev.getY();
            mDragBeginDirect = 0;
            mIsTouchEventMode = true;
            mDragLastY = mDragBeginY;

            mTargetView = findScrollView(this, (int) ev.getX(), (int) ev.getY(), 0);

            stopAllScroll();
        }

        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            mIsTouchEventMode = false;
        }

        if (mNestedScrollInProgress) {
            return super.dispatchTouchEvent(ev);
        }


        if (action == MotionEvent.ACTION_DOWN && (getScrollY() != 0)) {
            mPreScroll = true;
            if (mVelocityTracker == null) {
                mVelocityTracker = VelocityTracker.obtain();
            } else {
                mVelocityTracker.clear();
            }
            mVelocityTracker.addMovement(ev);
        }

        if (mPreScroll) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    mActivePointerId = MotionEventCompat.getPointerId(ev, 0);
                    mDragBeginY = ev.getY(ev.findPointerIndex(mActivePointerId));
                    mDragLastY = mDragBeginY;
                    break;
                case MotionEventCompat.ACTION_POINTER_DOWN:
                    int pointerIndex = MotionEventCompat.getActionIndex(ev);
                    mActivePointerId = ev.getPointerId(pointerIndex);
                    mDragBeginY = ev.getY(ev.findPointerIndex(mActivePointerId));
                    mDragLastY = mDragBeginY;
                    break;
                case MotionEvent.ACTION_MOVE:
                    float y = ev.getY(ev.findPointerIndex(mActivePointerId));
                    float yDiff = mDragBeginY - y;
                    if (!mDraggedDispatch && Math.abs(yDiff) > mTouchSlop / 2) {
                        mDraggedDispatch = true;
                        mDragLastY = y;
                    }

                    if (mDraggedDispatch) {
                        offSetScroll((int) (mDragLastY - y), true);
                        if (getScrollY() == 0) {
                            mPreScroll = false;
                        }
                        mDragLastY = y;
                        mVelocityTracker.addMovement(ev);
                        return true;
                    }
                    break;
                case MotionEventCompat.ACTION_POINTER_UP:
                    onSecondaryPointerUp(ev);
                    break;
                case MotionEvent.ACTION_CANCEL:
                case MotionEvent.ACTION_UP:
                    ev.setAction(MotionEvent.ACTION_CANCEL);
                    if (mVelocityTracker != null) {
                        mVelocityTracker.addMovement(ev);
                        mVelocityTracker.computeCurrentVelocity(1000, 20000);
                        mVelocityTracker.getYVelocity();
                        int initialVelocity = (int) mVelocityTracker.getYVelocity();
                        fling(-initialVelocity);
                        mVelocityTracker.recycle();
                        mVelocityTracker = null;
                    }
                    break;
            }
        }

        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            if (tryBackToRefreshing() || tryBackToFreshFinish() || tryBackToLoading() || mDraggedDispatch || mDraggedIntercept) {
                ev.setAction(MotionEvent.ACTION_CANCEL);
            }
        }

        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mNestedScrollInProgress || !isEnabled() || (canChildScrollDown() && canChildScrollUp())) {
            return false;
        }

        final int action = MotionEventCompat.getActionMasked(ev);

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mDragBeginY = ev.getY(ev.findPointerIndex(mActivePointerId));
                mDragLastY = mDragBeginY;
                break;

            case MotionEventCompat.ACTION_POINTER_DOWN: {
                int pointerIndex = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = ev.getPointerId(pointerIndex);
                mDragBeginY = ev.getY(ev.findPointerIndex(mActivePointerId));
                mDragLastY = mDragBeginY;
                break;
            }


            case MotionEvent.ACTION_MOVE:

                float y = ev.getY(ev.findPointerIndex(mActivePointerId));


                final float yDiff = mDragBeginY - y;

                if (Math.abs(yDiff) > mTouchSlop && !mDraggedIntercept &&
                        ((yDiff > 0 && !canChildScrollUp()) || (yDiff < 0 && !canChildScrollDown()))) {                //头部 与尾巴自动判断
                    mDraggedIntercept = true;
                    mDragLastY = y;
                    mDragBeginDirect = -yDiff;
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                }


                break;
            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        return mDraggedIntercept;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {

        int pointerIndex;
        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mDragBeginY = ev.getY(ev.findPointerIndex(mActivePointerId));
                mDragLastY = (int) ev.getY();
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                pointerIndex = MotionEventCompat.getActionIndex(ev);
                mActivePointerId = ev.getPointerId(pointerIndex);
                mDragBeginY = ev.getY(ev.findPointerIndex(mActivePointerId));
                mDragLastY = mDragBeginY;
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                float y = ev.getY(ev.findPointerIndex(mActivePointerId));

                int deltaY = (int) mDragBeginY - (int) y;
                if (!mDraggedIntercept && Math.abs(deltaY) > mTouchSlop) {
                    final ViewParent parent = getParent();
                    if (parent != null) {
                        parent.requestDisallowInterceptTouchEvent(true);
                    }
                    mDraggedIntercept = true;
                    mDragBeginDirect = -deltaY;
                }
                if (mDraggedIntercept) {
                    offSetScroll((int) (mDragLastY - y), false);
                }
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
        }

        pointerIndex = MotionEventCompat.findPointerIndex(ev, mActivePointerId);
        if (pointerIndex >= 0) {
            mDragLastY = MotionEventCompat.getY(ev, pointerIndex);
        }

        return true;
    }

    @Override
    public void addView(View child, int index, LayoutParams params) {
        if ((getChildCount() + (mEmptyView != null ? -1 : 0)) > 3) {
            throw new RuntimeException("only one View is support");
        }
        super.addView(child, index, params);
    }


    private int preScroll = Integer.MIN_VALUE;

    @Override
    public void computeScroll() {
        int scrollY = getScrollY();

        if (mScroller.computeScrollOffset() && !mScroller.isFinished()) {
            int y = mScroller.getCurrY();

            if (y < 0 || y > mContentScroll + mOverScrollBottomMiddle) {
                y = y < 0 ? 0 : y;
                y = y > mContentScroll + mOverScrollBottomMiddle ? mContentScroll + mOverScrollBottomMiddle : y;

                if (scrollY != y) {
                    scrollTo(0, y);
                }

                int remainVelocity = (int) (mMaxFlingDirection * mScroller.getCurrVelocity());

                lab:
                if (mTargetView != null) {
                    if (mTargetView instanceof RecyclerView) {
                        RecyclerView recyclerView = (RecyclerView) mTargetView;
                        recyclerView.fling(0, remainVelocity);
                        break lab;
                    }
                    if (mTargetView instanceof ListView) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            ListView listView = (ListView) mTargetView;
                            listView.fling(remainVelocity);
                        }
                        break lab;
                    }
                    if (mTargetView instanceof ScrollView) {
                        ScrollView scrollView = (ScrollView) mTargetView;
                        scrollView.fling(remainVelocity);
                        break lab;
                    }
                    if (mTargetView instanceof NestedScrollView) {
                        NestedScrollView nestedScrollView = (NestedScrollView) mTargetView;
                        nestedScrollView.fling(remainVelocity);
                    }
                }

                mScroller.abortAnimation();
            } else {
                if (y == scrollY) {
                    ViewCompat.postInvalidateOnAnimation(this);
                } else {
                    scrollTo(0, y);
                }
            }
        }

        if (scrollY != preScroll) {
            preScroll = scrollY;
            // ----------------------------------------------------------------------------------------------------------------》》下拉
            if (0 == scrollY && (mFreshStatus == ISwipe.FreshStatus.ERROR || mFreshStatus == FreshStatus.ERROR_NET
                    || mFreshStatus == ISwipe.FreshStatus.SUCCESS) && !mRefreshStatusContinueRunning) {                                              //重置下拉刷新状态
                mFreshStatus = null;

            }
            if (0 > scrollY) {                                                                                                                     //刷新下拉状态
                int swipeViewVisibilityHei = 0 - scrollY;

                if (mFreshStatus == null) {
                    if (mRefreshStatusContinueRunning) {
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_LOADING, swipeViewVisibilityHei, mOverScrollTop);
                    } else if (scrollY < -mOverScrollTopMiddle) {
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_OVER, swipeViewVisibilityHei, mOverScrollTop);
                    } else {
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_TOAST, swipeViewVisibilityHei, mOverScrollTop);
                    }
                }
            }
            // ----------------------------------------------------------------------------------------------------------------《《 下拉

            // ---------------------------------------------------------------------------------------------------------------- 》》上拉
            if (scrollY > mContentScroll && !mLoadedStatusContinueRunning) {                                                                                     //重置上拉刷新状态
                mLoadedStatusContinueRunning = true;
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onLoading();
                }
            }
            if (scrollY > mContentScroll) {
                if (mLoadedStatus == ISwipe.LoadedStatus.NO_MORE) {
                    mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_NO_MORE, scrollY - mContentScroll, mOverScrollBottomMiddle);
                } else if (mLoadedStatus == ISwipe.LoadedStatus.ERROR) {
                    mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_ERROR, scrollY - mContentScroll, mOverScrollBottomMiddle);
                } else if (mLoadedStatus == null) {
                    mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_LOADING, scrollY - mContentScroll, mOverScrollBottomMiddle);
                }
            }
            // ---------------------------------------------------------------------------------------------------------------- 《《上拉

        }


    }

    private void onSecondaryPointerUp(MotionEvent ev) {
        final int pointerIndex = (ev.getAction() & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >>
                MotionEventCompat.ACTION_POINTER_INDEX_SHIFT;
        final int pointerId = MotionEventCompat.getPointerId(ev, pointerIndex);
        if (pointerId == mActivePointerId) {
            final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
            mDragLastY = (int) ev.getY(newPointerIndex);
            mActivePointerId = MotionEventCompat.getPointerId(ev, newPointerIndex);
        }
    }


    private boolean canChildScrollDown() {
        return !mShowEmptyView && mTargetView != null && ViewCompat.canScrollVertically(mTargetView, -1);
    }

    private boolean canChildScrollUp() {
        return !mShowEmptyView && mTargetView != null && ViewCompat.canScrollVertically(mTargetView, 1);
    }


    @Override
    public void requestDisallowInterceptTouchEvent(boolean b) {
        if (mShowEmptyView || mTargetView == null || mTargetView instanceof NestedScrollingChild) {
            super.requestDisallowInterceptTouchEvent(b);
        }
    }


    // ---------------------------------------------------------------- ISwipeIMP -------------------------------------------------------------------//

    // 设置刷新控制监听
    @Override
    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        this.mOnRefreshListener = onRefreshListener;
    }

    // 开始刷新
    @Override
    public void refresh() {
        mFreshStatus = null;
        if (mEmptyController != null) {
            mEmptyController.onSwipeStatue(null);
        }
        mRefreshStatusContinueRunning = true;
        mLoadedStatus = null;
        mLoadedStatusContinueRunning = false;

        if (mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh();
        }

        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_LOADING,
                -getScrollY(), mViewTop.getHeight());
        tryBackToRefreshing();
    }

    // 结束刷新 设置下拉刷新结果
    @Override
    public void setFreshResult(FreshStatus statue) {
        switch (statue) {
            case SUCCESS:                                                                                   //设置刷新成功 自动隐藏
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFreshStatus = FreshStatus.SUCCESS;
                        if (mEmptyController != null) {
                            mEmptyController.onSwipeStatue(mFreshStatus);
                        }
                        mRefreshStatusContinueRunning = false;
                        mLoadedStatus = null;
                        mLoadedStatusContinueRunning = false;
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_COMPLETE_OK,
                                -getScrollY(), mViewTop.getHeight());
                        tryBackToFreshFinish();
                    }
                }, 1000);
                break;
            case ERROR_NET:                                                                              //设置刷新失败 自动隐藏
                if (mEmptyController != null) {
                    mEmptyController.onSwipeStatue(mFreshStatus);
                }
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFreshStatus = FreshStatus.ERROR_NET;
                        if (mEmptyController != null) {
                            mEmptyController.onSwipeStatue(mFreshStatus);
                        }
                        mRefreshStatusContinueRunning = false;
                        mLoadedStatus = null;
                        mLoadedStatusContinueRunning = false;
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_COMPLETE_ERROR_NET,
                                -getScrollY(), mViewTop.getHeight());
                        tryBackToFreshFinish();
                    }
                }, 1000);

                break;
            case ERROR:                                                                        //设置刷新失败 自动隐藏
                if (mEmptyController != null) {
                    mEmptyController.onSwipeStatue(mFreshStatus);
                }
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFreshStatus = FreshStatus.ERROR;
                        if (mEmptyController != null) {
                            mEmptyController.onSwipeStatue(mFreshStatus);
                        }
                        mRefreshStatusContinueRunning = false;
                        mLoadedStatus = null;
                        mLoadedStatusContinueRunning = false;
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_COMPLETE_ERROR,
                                -getScrollY(), mViewTop.getHeight());
                        tryBackToFreshFinish();
                    }
                }, 1000);
                break;
        }
    }

    // 结束加载 清除上拉加载中状态
    @Override
    public void completeLoadMore() {
        lab:
        {
            int currentScrollY = getScrollY() - mContentScroll;
            if (currentScrollY <= 0) {
                break lab;
            }
            if ((mTargetView instanceof RecyclerView ||
                    mTargetView instanceof ListView ||
                    mTargetView instanceof ScrollView ||
                    mTargetView instanceof NestedScrollView) && canChildScrollUp()) {
                mTargetView.scrollBy(0, currentScrollY);
            }

            stopAllScroll();
            scrollTo(0, mContentScroll);
        }

        this.mLoadedStatusContinueRunning = false;
        this.mLoadedStatus = null;
        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_LOADING,
                getHeight() - mViewBottom.getTop(), mViewBottom.getHeight());
    }

    // 设置上拉加载结果
    @Override
    public void setLoadMoreResult(ISwipe.LoadedStatus status) {
        switch (status) {
            case ERROR:
                this.mLoadedStatusContinueRunning = true;
                this.mLoadedStatus = LoadedStatus.ERROR;
                mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_ERROR,
                        getHeight() - mViewBottom.getTop(), mViewBottom.getHeight());
                break;
            case NO_MORE:
                this.mLoadedStatusContinueRunning = true;
                this.mLoadedStatus = LoadedStatus.NO_MORE;
                mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_NO_MORE,
                        getHeight() - mViewBottom.getTop(), mViewBottom.getHeight());
                break;
        }
    }


    // 设置刷新模式
    @Override
    public void setSwipeModel(SwipeController.SwipeModel model) {
        if (this.mModel != model && model != null) {
            this.mModel = model;
            requestLayout();
        }
    }

    // 设置自定义刷新视图
    @Override
    public void setSwipeController(SwipeController controller) {
        if (controller != null && this.mSwipeController != controller) {
            removeView(mViewTop);
            removeView(mViewBottom);
            this.mSwipeController = controller;
            mViewTop = mSwipeController.getSwipeHead();
            mViewBottom = mSwipeController.getSwipeFoot();
            addView(mViewTop);
            addView(mViewBottom);
            requestLayout();
        }
    }

    // 设置空白页面控制器
    @Override
    public void setEmptyController(EmptyController controller) {
        if (controller != null && this.mEmptyController != controller) {
            mEmptyController = controller;
            if (mEmptyView != null) {
                removeView(mEmptyView);
            }
            mEmptyView = controller.getView();
            addView(mEmptyView);
            requestLayout();
        }
    }

    // 启动空白页面显示
    @Override
    public void enableEmptyView(boolean show) {
        if (mShowEmptyView != show) {
            mShowEmptyView = show;
            requestLayout();
        }
    }

    // 是否支持上拉加载过度拉升
    @Override
    public void enableLoadMoreOverScroll(boolean enable) {
        if (this.mLoadMoreOverScroll != enable) {
            mLoadMoreOverScroll = enable;
            requestLayout();
        }
    }
}
































