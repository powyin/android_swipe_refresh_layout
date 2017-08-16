package com.powyin.scroll.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.v4.view.MotionEventCompat;
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
 * Created by powyin on 2016/7/10.
 */
public class SwipeNest extends ViewGroup implements NestedScrollingParent, ISwipe, ScrollingView {

    private int mTouchSlop;
    private int mActivePointerId = -1;

    private boolean mNestedScrollInProgress = false;

    private float mDragBeginY;                                                              //打断对比初始化Y
    private float mDragBeginDirect;                                                         //打断时用于记录方向
    private float mDragLastY;                                                               //上一次打断记录Y

    private boolean mIsTouchEventMode = false;                                              //DispatchTouchEvent  是否在进行TouchEvent传递
    private boolean mPreScroll;                                                             //DispatchTouchEvent  是否预滚动
    private boolean mDraggedDispatch;                                                       //DispatchTouchEvent  已经打断
    private boolean mDraggedIntercept;                                                      //InterceptTouchEvent 打断
    private boolean mShouldCancelMotionEvent = false;


    private int mContentScroll;
    private int mOverScrollTop;
    private int mOverScrollTopMiddle;
    private int mOverScrollBottomMiddle;
    private int mOverScrollBottom;
    private boolean mLoadMoreOverScroll = false;

    private ScrollerCompat mScroller;
    private VelocityTracker mVelocityTracker;
    private SwipeController mSwipeController;                                               //刷新头部控制器
    private EmptyController mEmptyController;                                               //空白控制器
    private ValueAnimator animationScrollY;                                                 //滚动 回到正常显示
    private NestedScrollingParentHelper mParentHelper;

    private boolean mRefreshStatusContinueRunning = false;                                  //下拉刷新 正在刷新
    private ISwipe.FreshStatus mFreshStatus = null;                                         //下拉刷新状态
    private boolean mLoadedStatusContinueRunning = false;                                   //上拉加载 正在加载
    private ISwipe.LoadedStatus mLoadedStatus = null;                                       //下拉刷新状态;
    private SwipeController.SwipeModel mModel = SwipeController.SwipeModel.SWIPE_BOTH;      //刷新模式设置
    private ISwipe.OnRefreshListener mOnRefreshListener;
    private boolean mShowEmptyView;
    private int mEmptyViewIndex = -1;

    private View mViewTop;
    private View mViewBottom;

    private View mEmptyView;
    private View mEmptyReplaceView;

    private View mTargetView;
    private View mTargetViewContain;
    private int mTargetViewContainIndex;

    private int mMaxFlingDirection;
    private int mMaxFlingScrollUp = 0;
    private int mMaxFlingScrollButton = 0;
    private View mMaxFlingScrollDesView;

    private int mPreScrollY = 0;
    private OnScrollListener mOnScrollListener;

    public SwipeNest(Context context) {
        this(context, null);
    }

    public SwipeNest(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SwipeNest(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SwipeNest);
            int modelIndex = a.getInt(R.styleable.SwipeNest_fresh, -1);
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

    // -------------------------------------------------------NestedScrollingParent---------------------------------------------------------//

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        if (!mLoadedStatusContinueRunning && mOnRefreshListener != null && target instanceof ScrollingView) {
            ScrollingView scrollingView = (ScrollingView) target;
            int range = scrollingView.computeVerticalScrollRange() -
                    scrollingView.computeVerticalScrollOffset() -
                    scrollingView.computeVerticalScrollExtent();

            if (range < 5 * getHeight()) {
                mLoadedStatusContinueRunning = true;
                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onLoading();
                }
            }
        }
        mNestedScrollInProgress = true;
        if (target != mEmptyView && (target != mEmptyReplaceView || !mShowEmptyView)) {
            mTargetView = target;
            mTargetViewContain = child;
            for (int i = 0; i < getChildCount(); i++) {
                if (getChildAt(i) == mTargetViewContain) {
                    mTargetViewContainIndex = i;
                }
            }
        }

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
        if (!tryBackToRefreshing() && !tryBackToFreshFinish()) {
            tryBackToLoading();
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

        Integer but = mTargetViewContain != null ? (Integer) mTargetViewContain.getTag(1 << 30) : null;
        but = but == null ? Integer.MAX_VALUE : but;

        int currentScrollY = getScrollY();
        if (currentScrollY < 0 || currentScrollY > mContentScroll + mOverScrollBottomMiddle)
            return true;

        if (but != currentScrollY) {
            fling((int) velocityY);
            return true;
        } else {
            boolean consume = !(velocityY > 0 && canChildScrollUp() || velocityY < 0 && canChildScrollDown());
            if (consume) {
                fling((int) velocityY);
            }
            return consume;
        }
    }


    private void fling(int velocityY) {

        System.out.println("-------->>into fling");

        if (animationScrollY != null && animationScrollY.isStarted()) {
            return;
        }

        int currentScrollY = getScrollY();
        if (mContentScroll == 0 && currentScrollY == 0 && velocityY > 0 && !canChildScrollDown()) {                                          //没有足够容量禁止上拉
            return;
        }

        mMaxFlingScrollUp = 0;
        mMaxFlingScrollButton = mContentScroll + mOverScrollBottomMiddle;
        mMaxFlingDirection = 0;

        if (velocityY > 0) {   // 向上
            View targetView;
            for (int i = (mTargetViewContainIndex > 0 ? mTargetViewContainIndex : 0); i < getChildCount(); i++) {
                Integer integer = (Integer) getChildAt(i).getTag(1 << 30);
                if (integer == null) continue;
                if (integer <= currentScrollY) {
                    continue;
                }

                targetView = getChildAt(i);
                int tem[] = new int[2];
                targetView.getLocationInWindow(tem);
                int centerX = tem[0] + targetView.getWidth() / 2;
                int centerY = tem[1] + targetView.getHeight() / 2;
                mMaxFlingScrollDesView = findScrollView(targetView, centerX, centerY, 1);
                if (mMaxFlingScrollDesView != null) {
                    mMaxFlingScrollButton = integer;
                    break;
                }
            }

            mMaxFlingDirection = 1;
            mScroller.fling(0, currentScrollY, 0, velocityY, 0, 0, -1000000, 1000000, 0, 0);
            ViewCompat.postInvalidateOnAnimation(this);
        }

        if (velocityY < 0) {     // 向下
            View targetView;
            for (int i = (mTargetViewContainIndex > 0 ? mTargetViewContainIndex : getChildCount() - 1); i >= 0; i--) {
                Integer integer = (Integer) getChildAt(i).getTag(1 << 30);
                if (integer == null || integer >= currentScrollY) continue;


                targetView = getChildAt(i);
                int tem[] = new int[2];
                targetView.getLocationInWindow(tem);
                int centerX = tem[0] + targetView.getWidth() / 2;
                int centerY = tem[1] + targetView.getHeight() / 2;
                mMaxFlingScrollDesView = findScrollView(targetView, centerX, centerY, -1);

                System.out.println("-----------------------------------" + mMaxFlingScrollDesView);

                if (mMaxFlingScrollDesView != null) {
                    mMaxFlingScrollUp = integer;  // + targetView.getHeight();
                    break;
                }


            }

            mMaxFlingScrollUp = mMaxFlingScrollUp > 0 ? mMaxFlingScrollUp : 0;
            mMaxFlingDirection = -1;
            mScroller.fling(0, currentScrollY, 0, velocityY, 0, 0, -1000000, 1000000, 0, 0);
            ViewCompat.postInvalidateOnAnimation(this);
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


    // -------------------------------------------------------ScrollingView---------------------------------------------------------//

    @Override
    public int computeHorizontalScrollRange() {
        return getWidth();
    }

    public int computeHorizontalScrollOffset() {
        return 0;
    }

    public int computeHorizontalScrollExtent() {
        return getWidth();
    }

    public int computeVerticalScrollRange() {
        return mContentScroll + getHeight();
    }

    public int computeVerticalScrollOffset() {
        return getScrollY();
    }

    public int computeVerticalScrollExtent() {
        return getHeight();
    }


    // -------------------------------------------------------------ViewFunc------------------------------------------------------------//

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        final int widthTarget = MeasureSpec.getSize(widthMeasureSpec);
        final int heightTarget = MeasureSpec.getSize(heightMeasureSpec);
        final int childWidMeasure = MeasureSpec.makeMeasureSpec(widthTarget, MeasureSpec.EXACTLY);
        final int spHei = MeasureSpec.makeMeasureSpec(heightTarget, MeasureSpec.AT_MOST);

        int mTotalLength = 0;
        int normalIndex = 0;
        int childCount = getChildCount();

        boolean hasMeasureEmptyView = false;
        mEmptyReplaceView = null;
        for (int i = 0; i < childCount; ++i) {
            final View child = getChildAt(i);
            if (child == mViewTop || child == mViewBottom || child == mEmptyView) {
                continue;
            }

            if (child.getVisibility() != GONE) {
                ViewGroup.LayoutParams lp = child.getLayoutParams();
                if (lp instanceof LayoutParams && ((LayoutParams) lp).expend) {
                    child.measure(childWidMeasure, MeasureSpec.makeMeasureSpec(1920 * 8, MeasureSpec.UNSPECIFIED));
                } else {
                    switch (lp.height) {
                        case -2:
                            child.measure(childWidMeasure, MeasureSpec.makeMeasureSpec(heightTarget, MeasureSpec.AT_MOST));
                            break;
                        case -1:
                            child.measure(childWidMeasure, MeasureSpec.makeMeasureSpec(heightTarget, MeasureSpec.EXACTLY));
                            break;
                        default:
                            child.measure(childWidMeasure, MeasureSpec.makeMeasureSpec(lp.height, MeasureSpec.EXACTLY));
                            break;
                    }
                }

                int childMeasureHei = child.getMeasuredHeight();
                childMeasureHei = childMeasureHei > 0 ? childMeasureHei : 0;
                mTotalLength = mTotalLength + childMeasureHei;

                if (mEmptyViewIndex == normalIndex) {
                    mEmptyReplaceView = child;
                    // 处理空白页面为最后一项 让其充分展开
                    if (mEmptyView != null) {
                        hasMeasureEmptyView = true;
                        if (normalIndex == (childCount - 4)) {
                            int preLen = mTotalLength - childMeasureHei;
                            if (heightTarget - preLen > 0) {
                                childMeasureHei = heightTarget - preLen;
                            }
                        }
                        mEmptyView.measure(MeasureSpec.makeMeasureSpec(widthTarget, MeasureSpec.EXACTLY),
                                MeasureSpec.makeMeasureSpec(childMeasureHei, MeasureSpec.EXACTLY));
                    }
                }
            }
            normalIndex++;
        }

        if (mViewTop != null) {
            mViewTop.measure(childWidMeasure, spHei);
        }
        if (mViewBottom != null) {
            mViewBottom.measure(childWidMeasure, spHei);
        }
        if (!hasMeasureEmptyView && mEmptyView != null) {
            mEmptyView.measure(childWidMeasure, spHei);
        }

        int endHei = Math.max(mTotalLength, getSuggestedMinimumHeight());
        int endWid = Math.max(widthTarget, getSuggestedMinimumWidth());

        setMeasuredDimension(resolveSizeAndState(endWid, widthMeasureSpec, 0), resolveSizeAndState(endHei, heightMeasureSpec, 0));
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int oldContentScroll = mContentScroll;
        int currentHei = 0;

        boolean hasEmptyViewLayout = false;


        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if (child == mViewTop || child == mViewBottom || child == mEmptyView || child.getVisibility() == GONE) {
                continue;
            }

            if (child == mEmptyReplaceView) {

                if (mShowEmptyView) {
                    if (mEmptyView != null) {
                        hasEmptyViewLayout = true;
                        int itemEmptyHei = mEmptyView.getMeasuredHeight();
                        mEmptyView.layout(0, currentHei, r - l, currentHei + itemEmptyHei);
                        currentHei += itemEmptyHei;
                        int itemHei = child.getMeasuredHeight();
                        child.layout(r - l, currentHei, 2 * (r - l), currentHei + itemHei);
                        child.setTag(1 << 30, null);
                    } else {
                        int itemHei = child.getMeasuredHeight();
                        currentHei += itemHei;
                        child.layout(r - l, currentHei, 2 * (r - l), currentHei + itemHei);
                        child.setTag(1 << 30, null);
                    }
                } else {
                    if (mEmptyView != null) {
                        hasEmptyViewLayout = true;
                        mEmptyView.layout(r - l, currentHei, 2 * (r - l), currentHei + mEmptyView.getMeasuredHeight());
                    }
                    int itemHei = child.getMeasuredHeight();
                    LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
                    itemHei = itemHei <= (b - t) || (layoutParams != null && layoutParams.expend) ? itemHei : b - t;
                    child.layout(0, currentHei, r - l, currentHei + itemHei);
                    currentHei += itemHei;
                    child.setTag(1 << 30, currentHei - (b - t));
                }
            } else {
                int itemHei = child.getMeasuredHeight();
                LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
                itemHei = itemHei <= (b - t) || (layoutParams != null && layoutParams.expend) ? itemHei : b - t;
                child.layout(0, currentHei, r - l, currentHei + itemHei);
                currentHei += itemHei;
                child.setTag(1 << 30, currentHei - (b - t));
            }
        }


        boolean canOverScrollBottom = currentHei >= (b - t);
        mContentScroll = canOverScrollBottom ? currentHei - (b - t) : 0;

        mOverScrollTop = mViewTop.getMeasuredHeight();
        mOverScrollTopMiddle = mOverScrollTop != 0 ? mOverScrollTop - mSwipeController.getOverScrollHei() : 0;
        mOverScrollTopMiddle = mOverScrollTopMiddle > 0 ? mOverScrollTopMiddle : 0;
        mOverScrollBottomMiddle = mViewBottom.getMeasuredHeight();
        mOverScrollBottom = mLoadMoreOverScroll ? mOverScrollBottomMiddle + (int) ((b - t) * 0.2f) : mOverScrollBottomMiddle;

        mViewTop.layout(0, -mOverScrollTop, r - l, 0);
        mViewTop.setTag(1 << 30, 0);
        mViewBottom.layout(0, b - t + mContentScroll, r - l, b - t + mContentScroll + mOverScrollBottomMiddle);
        mViewTop.setTag(1 << 30, mContentScroll + mOverScrollBottomMiddle);

        if (!hasEmptyViewLayout && mEmptyView != null) {
            mEmptyView.layout(r - l, 0, 2 * (r - l), mEmptyView.getMeasuredHeight());
        }

        if (mModel == SwipeController.SwipeModel.SWIPE_NONE || mModel == SwipeController.SwipeModel.SWIPE_ONLY_LOADINN) {
            mOverScrollTop = 0;
            mOverScrollTopMiddle = 0;
        }

        if (!canOverScrollBottom || mModel == SwipeController.SwipeModel.SWIPE_NONE || mModel == SwipeController.SwipeModel.SWIPE_ONLY_REFRESH) {
            mOverScrollBottomMiddle = 0;
            mOverScrollBottom = 0;
        }

        if (oldContentScroll > mContentScroll && getScrollY() > mContentScroll) {
            int currentScrollY = getScrollY();
            currentScrollY = currentScrollY - (oldContentScroll - mContentScroll);
            currentScrollY = currentScrollY > 0 ? currentScrollY : 0;
            scrollTo(0, currentScrollY);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        int action = MotionEventCompat.getActionMasked(ev);

        if (action == MotionEvent.ACTION_DOWN) {
            mDraggedIntercept = false;
            mPreScroll = false;
            mDraggedDispatch = false;
            mActivePointerId = ev.getPointerId(0);
            mDragBeginY = (int) ev.getY();
            mDragBeginDirect = 0;
            mIsTouchEventMode = true;
            mDragLastY = mDragBeginY;
            mShouldCancelMotionEvent = false;

            mTargetView = null;
            mTargetViewContain = null;
            mTargetViewContainIndex = -1;

            int x = (int) ev.getX();
            int y = (int) ev.getY();

            for (int i = 0; i < getChildCount(); i++) {
                int scrollY = getScrollY();
                View child = getChildAt(i);
                if (child == mViewTop || child == mViewBottom || child == mEmptyView) continue;
                boolean isSelect = !(y < child.getTop() - scrollY || y >= child.getBottom() - scrollY || x < child.getLeft() || x >= child.getRight());
                if (isSelect) {
                    if (child == mEmptyReplaceView) {
                        break;
                    }
                    mTargetView = findScrollView(child, x, y, 0);
                    mTargetViewContain = child;
                    mTargetViewContainIndex = i;
                    break;
                }
            }
        }

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            mIsTouchEventMode = false;
        }

        if (mNestedScrollInProgress) {
            return super.dispatchTouchEvent(ev);
        }

        if (action == MotionEvent.ACTION_DOWN && (getScrollY() < 0 || getScrollY() > mContentScroll)) {
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
                case MotionEventCompat.ACTION_POINTER_DOWN: {
                    int pointerIndex = MotionEventCompat.getActionIndex(ev);
                    mActivePointerId = ev.getPointerId(pointerIndex);
                    mDragBeginY = ev.getY(ev.findPointerIndex(mActivePointerId));
                    mDragLastY = mDragBeginY;
                    break;
                }
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

        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (tryBackToRefreshing() || tryBackToFreshFinish() || tryBackToLoading() || mDraggedDispatch || mDraggedIntercept || mShouldCancelMotionEvent) {
                ev.setAction(MotionEvent.ACTION_CANCEL);

            }
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(ev);
        }

        boolean superRet = super.dispatchTouchEvent(ev);


        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
        }


        return superRet;
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

                if (mScroller.computeScrollOffset() && !mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    mShouldCancelMotionEvent = true;
                }

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
                    if (mVelocityTracker == null) {
                        mVelocityTracker = VelocityTracker.obtain();
                    } else {
                        mVelocityTracker.clear();
                    }
                    mVelocityTracker.addMovement(ev);

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

        switch (MotionEventCompat.getActionMasked(ev)) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                mDragBeginY = ev.getY(ev.findPointerIndex(mActivePointerId));

                if (mScroller.computeScrollOffset() && !mScroller.isFinished()) {
                    mScroller.abortAnimation();
                    mShouldCancelMotionEvent = true;
                }

                mDragLastY = (int) ev.getY();
                break;
            case MotionEventCompat.ACTION_POINTER_DOWN: {
                int pointerIndex = MotionEventCompat.getActionIndex(ev);
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
                    if (mVelocityTracker == null) {
                        mVelocityTracker = VelocityTracker.obtain();
                    } else {
                        mVelocityTracker.clear();
                    }
                    mVelocityTracker.addMovement(ev);
                }
                if (mDraggedIntercept) {
                    offSetScroll((int) (mDragLastY - y), false);
                }
                break;
            }

            case MotionEventCompat.ACTION_POINTER_UP:
                onSecondaryPointerUp(ev);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (mDraggedIntercept && mVelocityTracker != null) {
                    mVelocityTracker.computeCurrentVelocity(1000, 20000);
                    mVelocityTracker.getYVelocity();
                    int initialVelocity = (int) mVelocityTracker.getYVelocity();

                    System.out.println("--------------------------------------->>>>>>>>>>>>>>>");

                    fling(-initialVelocity);
                }

                return true;
        }

        int pointerIndex = ev.findPointerIndex(mActivePointerId);
        if (pointerIndex >= 0) {
            mDragLastY = ev.getY(pointerIndex);
        }


        return true;
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (disallowIntercept) {
            if (mVelocityTracker != null) {
                mVelocityTracker.recycle();
                mVelocityTracker = null;
            }
        }
        super.requestDisallowInterceptTouchEvent(disallowIntercept);
    }

    @Override
    public void computeScroll() {
        int scrollY = getScrollY();

        if (mScroller.computeScrollOffset() && !mScroller.isFinished()) {

            int y = mScroller.getCurrY();

            if (y > mMaxFlingScrollButton || y < mMaxFlingScrollUp) {
                y = y > mMaxFlingScrollButton ? mMaxFlingScrollButton : y;
                y = y < mMaxFlingScrollUp ? mMaxFlingScrollUp : y;
                int remainVelocity = (int) (mMaxFlingDirection * mScroller.getCurrVelocity());
                lab:
                if (mMaxFlingScrollDesView != null) {
                    if (mMaxFlingScrollDesView instanceof RecyclerView) {
                        RecyclerView recyclerView = (RecyclerView) mMaxFlingScrollDesView;
                        recyclerView.fling(0, remainVelocity);
                        break lab;
                    }
                    if (mMaxFlingScrollDesView instanceof ListView) {
                        if (Build.VERSION.SDK_INT >= 21) {
                            ListView listView = (ListView) mMaxFlingScrollDesView;
                            listView.fling(remainVelocity);
                        }
                        break lab;
                    }
                    if (mMaxFlingScrollDesView instanceof ScrollView) {
                        ScrollView scrollView = (ScrollView) mMaxFlingScrollDesView;
                        scrollView.fling(remainVelocity);
                        break lab;
                    }
                    if (mMaxFlingScrollDesView instanceof NestedScrollView) {
                        NestedScrollView nestedScrollView = (NestedScrollView) mMaxFlingScrollDesView;
                        nestedScrollView.fling(remainVelocity);
                    }
                }
                mScroller.abortAnimation();
            }

            if (scrollY != y) {
                scrollTo(0, y);
                scrollY = y;
            }
        }

        if (mPreScrollY != scrollY) {
            mPreScrollY = scrollY;

            // ----------------------------------------------------------------------------------------------------------------》》下拉
            if (0 > mPreScrollY && mPreScrollY > -mOverScrollTopMiddle && (mFreshStatus == ISwipe.FreshStatus.ERROR || mFreshStatus == FreshStatus.ERROR_NET
                    || mFreshStatus == ISwipe.FreshStatus.SUCCESS) && !mRefreshStatusContinueRunning) {                                                 //重置下拉刷新状态
                mFreshStatus = null;
            }
            if (0 > mPreScrollY) {                                                                                                                            //刷新下拉状态
                int swipeViewVisibilityHei = 0 - mPreScrollY;

                if (mFreshStatus == null) {
                    if (mRefreshStatusContinueRunning) {
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_LOADING, swipeViewVisibilityHei, mOverScrollTop);
                    } else if (mPreScrollY < -mOverScrollTopMiddle) {
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_OVER, swipeViewVisibilityHei, mOverScrollTop);
                    } else {
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_TOAST, swipeViewVisibilityHei, mOverScrollTop);
                    }
                } else {
                    switch (mFreshStatus) {
                        case SUCCESS:
                            mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_COMPLETE_OK, swipeViewVisibilityHei, mOverScrollTop);
                            break;
                        case ERROR:
                            mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_COMPLETE_ERROR, swipeViewVisibilityHei, mOverScrollTop);
                            break;
                        case ERROR_NET:
                            mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_COMPLETE_ERROR_NET, swipeViewVisibilityHei, mOverScrollTop);
                            break;
                    }
                }
            }
            // ----------------------------------------------------------------------------------------------------------------《《 下拉

            // ---------------------------------------------------------------------------------------------------------------- 》》上拉
            if (mPreScrollY > mContentScroll && !mLoadedStatusContinueRunning && mOnRefreshListener != null) {                                                                                    //重置上拉刷新状态
                mLoadedStatusContinueRunning = true;
                mOnRefreshListener.onLoading();
            }

            if (mPreScrollY > mContentScroll) {
                if (mLoadedStatus == ISwipe.LoadedStatus.NO_MORE) {
                    mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_NO_MORE, mPreScrollY - mContentScroll, mOverScrollBottomMiddle);
                } else if (mLoadedStatus == ISwipe.LoadedStatus.ERROR) {
                    mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_ERROR, mPreScrollY - mContentScroll, mOverScrollBottomMiddle);
                } else if (mLoadedStatus == null) {
                    mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_LOADING, mPreScrollY - mContentScroll, mOverScrollBottomMiddle);
                }
            }
            // ---------------------------------------------------------------------------------------------------------------- 《《上拉

            if (mOnScrollListener != null) {
                mOnScrollListener.onScroll(mPreScrollY, mContentScroll);
            }

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
            if (mVelocityTracker != null) {
                mVelocityTracker.clear();
            }
        }
    }


    private boolean canChildScrollDown() {
        return mTargetView != null && ViewCompat.canScrollVertically(mTargetView, -1);
    }

    private boolean canChildScrollUp() {
        return mTargetView != null && (ViewCompat.canScrollVertically(mTargetView, 1));
    }

    private int offSetScroll(int deltaOriginY, boolean pre) {
        int currentScrollY = getScrollY();

        boolean progress = !pre;
        lab:
        if (pre) {
            if (currentScrollY < 0 || currentScrollY > mContentScroll) {
                progress = true;
                break lab;
            }

            Integer scroll = mTargetViewContain != null ? (Integer) mTargetViewContain.getTag(1 << 30) : null;
            scroll = scroll == null ? Integer.MAX_VALUE : scroll;

            if (currentScrollY != scroll) {
                progress = true;
            }
            if (currentScrollY == scroll) {
                progress = false;
            }
//            if (currentScrollY > scroll) {
//                progress = !(deltaOriginY > 0 && canChildScrollUp() || deltaOriginY < 0 && canChildScrollDown());
//            }
        }
        if (!progress) {
            return 0;
        }

        int deltaY = deltaOriginY;
        if (deltaOriginY == 0) return 0;

        if (deltaOriginY < 0 && currentScrollY < -mOverScrollTopMiddle && (mOverScrollTop - mOverScrollTopMiddle != 0)) {                      //下拉刷新过度拉伸 阻尼效果
            deltaY = (int) (deltaY * Math.pow((-mOverScrollTop - currentScrollY) * 1f
                    / (mOverScrollTop - mOverScrollTopMiddle), 3));
            deltaY = deltaY < 0 ? deltaY : -deltaY;
        }

        if (deltaOriginY > 0 && currentScrollY > (mOverScrollBottomMiddle + mContentScroll) &&                                                //上拉加载过度拉伸 阻尼效果
                (mOverScrollBottom - mOverScrollBottomMiddle) != 0) {
            deltaY = (int) (deltaY * Math.pow((mOverScrollBottom + mContentScroll - currentScrollY) * 1f
                    / (mOverScrollBottom - mOverScrollBottomMiddle), 3));
            deltaY = deltaY > 0 ? deltaY : -deltaY;
        }

        int willTo = currentScrollY + deltaY;
        willTo = willTo < mOverScrollBottom + mContentScroll ? willTo : mOverScrollBottom + mContentScroll;
        willTo = willTo > -mOverScrollTop ? willTo : -mOverScrollTop;

        Integer maxScroll = mTargetViewContain != null ? (Integer) mTargetViewContain.getTag(1 << 30) : null;                                  //确保scroll值经过敏感过渡区
        maxScroll = maxScroll == null ? Integer.MAX_VALUE : maxScroll;
        if ((currentScrollY > maxScroll && willTo < maxScroll) || (currentScrollY < maxScroll && willTo > maxScroll)) {
            willTo = maxScroll;
        }

        if (mDragBeginDirect > 0 && willTo > mContentScroll) {                                                                                 //确保上拉加载独立
            willTo = mContentScroll;
        }
        if (mDragBeginDirect < 0 && willTo < 0) {                                                                                              //确保下拉刷新独立
            willTo = 0;
        }

        if (mContentScroll == 0 && currentScrollY == 0 && deltaOriginY > 0 && !canChildScrollDown()) {                                        //没有足够容量禁止上拉
            willTo = 0;
        }

        if (willTo == currentScrollY) {                                                                                                       //无改变
            return deltaOriginY;
        }


        if (willTo > mContentScroll && deltaOriginY > 0) {                                                                                    // 去除子容器 可以上拉的情况
            for (int i = (mTargetViewContainIndex > 0 ? mTargetViewContainIndex : 0); i < getChildCount(); i++) {
                View child = getChildAt(i);
                if (child == mViewTop || child == mViewBottom || child.getVisibility() == GONE || child.getVisibility() == INVISIBLE)
                    continue;
                boolean find = child.canScrollVertically(1);
                if (find) {
                    return deltaOriginY;
                }
            }
        }

        scrollTo(0, willTo);

        return (deltaOriginY);
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
                    scrollTo(0, value);
                }
            });
            animationScrollY.addListener(new Animator.AnimatorListener() {
                boolean isCancel = false;

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isCancel) {
                        if (mFreshStatus == null) {
                            mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_LOADING, mOverScrollTop, mOverScrollTop);
                        }
                        if (!mRefreshStatusContinueRunning) {
                            mRefreshStatusContinueRunning = true;
                            if (mEmptyController != null) {
                                mEmptyController.onSwipeStatue(null);
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
            animationScrollY.setDuration(50 + Math.abs(500 * (-mOverScrollTopMiddle - scrollY) / mOverScrollTop));
            animationScrollY.start();

            System.out.println("---------------->>>>>>>> inva");
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
            animationScrollY.addListener(new Animator.AnimatorListener() {
                boolean isCancel;

                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isCancel) {
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_TOAST, 0, mOverScrollTop);
                    }
                    isCancel = true;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    isCancel = true;
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });


            if (scrollY == -mOverScrollTopMiddle) {
                animationScrollY.setDuration(Math.abs(550 * (0 - scrollY) / mOverScrollTop));
                animationScrollY.setStartDelay(650);
            } else {
                animationScrollY.setDuration(320);
            }
            animationScrollY.start();
            System.out.println("---------------->>>>>>>> inva2");
            return true;
        }
        return false;
    }

    private boolean tryBackToLoading() {

        int scrollY = getScrollY();
        if (mIsTouchEventMode || scrollY <= mContentScroll + mOverScrollBottomMiddle || mOverScrollBottomMiddle == mOverScrollBottom)
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
        System.out.println("---------------->>>>>>>> inva3");
        return true;
    }

    private void stopAllScroll() {
        if (animationScrollY != null) {
            animationScrollY.cancel();
        }
        mScroller.abortAnimation();
    }

    // ----------------------------------------------------------------ISwipeIMP-------------------------------------------------------------------//

    // 设置刷新控制监听
    @Override
    public void setOnRefreshListener(ISwipe.OnRefreshListener onRefreshListener) {
        this.mOnRefreshListener = onRefreshListener;
    }

    // 开始刷新
    @Override
    public void refresh() {
        mFreshStatus = null;
        if (mEmptyController != null) {
            mEmptyController.onSwipeStatue(mFreshStatus);
        }
        mRefreshStatusContinueRunning = true;
        mLoadedStatus = null;
        mLoadedStatusContinueRunning = false;

        if (mOnRefreshListener != null) {
            mOnRefreshListener.onRefresh();
        }

        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_LOADING,
                -getScrollY(), mOverScrollTop);
        tryBackToRefreshing();
    }

    // 结束刷新 设置下拉刷新结果
    @Override
    public void setFreshResult(ISwipe.FreshStatus statue) {
        switch (statue) {
            case SUCCESS:                                                                                   //设置刷新成功 自动隐藏
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFreshStatus = ISwipe.FreshStatus.SUCCESS;
                        if (mEmptyController != null) {
                            mEmptyController.onSwipeStatue(mFreshStatus);
                        }

                        mRefreshStatusContinueRunning = false;
                        mLoadedStatus = null;
                        mLoadedStatusContinueRunning = false;
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_COMPLETE_OK,
                                -getScrollY(), mOverScrollTop);
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
                                -getScrollY(), mOverScrollTop);
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
                                -getScrollY(), mOverScrollTop);
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
                    mTargetView instanceof NestedScrollView)) {
                mTargetView.scrollBy(0, currentScrollY);
            }

            stopAllScroll();
            scrollTo(0, mContentScroll);
        }

        this.mLoadedStatusContinueRunning = false;
        this.mLoadedStatus = null;
        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_LOADING,
                getHeight() - mViewBottom.getTop(), mOverScrollBottomMiddle);
    }

    // 设置上拉加载结果
    @Override
    public void setLoadMoreResult(ISwipe.LoadedStatus status) {
        switch (status) {
            case ERROR:
                this.mLoadedStatusContinueRunning = true;
                this.mLoadedStatus = ISwipe.LoadedStatus.ERROR;
                mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_ERROR,
                        getHeight() - mViewBottom.getTop(), mOverScrollBottomMiddle);
                break;
            case NO_MORE:
                this.mLoadedStatusContinueRunning = true;
                this.mLoadedStatus = ISwipe.LoadedStatus.NO_MORE;
                mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_NO_MORE,
                        getHeight() - mViewBottom.getTop(), mOverScrollBottomMiddle);
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
    public void setSwipeController(SwipeController control) {
        if (control != null && this.mSwipeController != control) {
            removeView(mViewTop);
            removeView(mViewBottom);
            this.mSwipeController = control;
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
            mEmptyViewIndex = controller.attachToViewIndex();
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
            // TODO release mTargetView  this is no be layout any more;
            if (mEmptyReplaceView == mTargetViewContain && mShowEmptyView) {
                mTargetView = null;
                mTargetViewContain = null;
                mTargetViewContainIndex = -1;
            }

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

    // ----------------------------------------------------------------layoutParams-------------------------------------------------------------//

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof LayoutParams;
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) lp);
        }
        return new LayoutParams(lp);

    }

    public static class LayoutParams extends MarginLayoutParams {
        public boolean expend;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            final TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.SwipeNest_Layout);
            expend = a.getBoolean(R.styleable.SwipeNest_Layout_layout_expend, false);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            this.expend = source.expend;
        }
    }


    // ----------------------------------------------------------------layoutParams-------------------------------------------------------------//

    // ----------------------------------------------------------------Unique-------------------------------------------------------------------//


    public void setOnScrollListener(OnScrollListener listener) {
        this.mOnScrollListener = listener;
    }


    public interface OnScrollListener {

        void onScroll(int currentScroll, int contentHei);
    }


}


























