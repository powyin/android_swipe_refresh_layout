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
    private int overScrollTop;
    private int overScrollBottom;

    private ScrollerCompat mScroller;
    private VelocityTracker mVelocityTracker;
    private SwipeController mSwipeController;                                               //刷新头部控制器
    private EmptyController mEmptyController;                                               //空白控制器
    private ValueAnimator animationReBackToRefreshing;                                      //滚动 显示正在刷新状态
    private ValueAnimator animationReBackToTop;                                             //滚动 回到正常显示
    private NestedScrollingParentHelper mParentHelper;

    private boolean mRefreshStatusContinueRunning = false;                                  //下拉刷新 正在刷新
    private ISwipe.FreshStatus mFreshStatus = ISwipe.FreshStatus.CONTINUE;                  //下拉刷新状态
    private boolean mLoadedStatusContinueRunning = false;                                   //上拉加载 正在加载
    private ISwipe.LoadedStatus mLoadedStatus = ISwipe.LoadedStatus.CONTINUE;               //下拉刷新状态;
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

            if (range < 6 * getHeight()) {
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
        if (!tryBackToRefreshing()) {
            tryBackToFreshFinish();
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

        if (but > getScrollY()) {
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

        if (animationReBackToRefreshing != null && animationReBackToRefreshing.isRunning()) {
            return;
        }
        if (animationReBackToTop != null && animationReBackToTop.isRunning()) {
            return;
        }

        int scrollY = getScrollY();
        if (scrollY < 0 || scrollY > mContentScroll) return;
        int currentScrollY = getScrollY();

        mMaxFlingScrollUp = 0;
        mMaxFlingScrollButton = mContentScroll;
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

            mMaxFlingScrollButton = mMaxFlingScrollButton <= mContentScroll ? mMaxFlingScrollButton : mContentScroll;
            mMaxFlingDirection = 1;
            mScroller.fling(0, scrollY, 0, velocityY, 0, 0, -1000000, 1000000, 0, 0);
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

                if (mMaxFlingScrollDesView != null) {
                    mMaxFlingScrollUp = integer;  // + targetView.getHeight();
                    break;
                }
            }

            mMaxFlingScrollUp = mMaxFlingScrollUp > 0 ? mMaxFlingScrollUp : 0;
            mMaxFlingDirection = -1;
            mScroller.fling(0, scrollY, 0, velocityY, 0, 0, -1000000, 1000000, 0, 0);
            ViewCompat.postInvalidateOnAnimation(this);
        }


    }


    private View findScrollView(View view, int centerX, int centerY, int direction) {
        if (view.getVisibility() == INVISIBLE || view.getVisibility() == GONE) return null;
        int tem[] = new int[2];
        view.getLocationInWindow(tem);
        if (tem[0] < centerX && centerX < tem[0] + view.getWidth() && tem[1] < centerY && centerY < tem[1] + view.getHeight()) {
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
                final LayoutParams lp = child.getLayoutParams();
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

                int childMeasureHei = child.getMeasuredHeight();
                childMeasureHei = childMeasureHei > 0 ? childMeasureHei : 0;
                mTotalLength = mTotalLength + childMeasureHei;

                if (mEmptyViewIndex == normalIndex) {
                    mEmptyReplaceView = child;
                    // 处理空白页面为最后一项 让其充分展开
                    if (mEmptyView != null) {
                        hasMeasureEmptyView = true;
                        if (normalIndex == (childCount - 3)) {
                            childMeasureHei = heightTarget > mTotalLength ? childMeasureHei + (heightTarget - mTotalLength) : childMeasureHei;
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
                        if (itemEmptyHei > (b - t)) itemEmptyHei = b - t;
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
                    if (itemHei > (b - t)) itemHei = b - t;
                    child.layout(0, currentHei, r - l, currentHei + itemHei);
                    currentHei += itemHei;
                    child.setTag(1 << 30, currentHei - (b - t));
                }
            } else {
                int itemHei = child.getMeasuredHeight();
                if (itemHei > (b - t)) itemHei = b - t;
                child.layout(0, currentHei, r - l, currentHei + itemHei);
                currentHei += itemHei;
                child.setTag(1 << 30, currentHei - (b - t));
            }
        }


        boolean canOverScrollBottom = currentHei >= (b - t);
        mContentScroll = canOverScrollBottom ? currentHei - (b - t) : 0;

        overScrollTop = mViewTop.getMeasuredHeight();
        overScrollBottom = mViewBottom.getMeasuredHeight();

        mViewTop.layout(0, -overScrollTop, r - l, 0);
        mViewTop.setTag(1 << 30, 0);
        mViewBottom.layout(0, b - t + mContentScroll, r - l, b - t + mContentScroll + overScrollBottom);
        mViewTop.setTag(1 << 30, mContentScroll + overScrollBottom);

        if (!hasEmptyViewLayout && mEmptyView != null) {
            mEmptyView.layout(r - l, 0, 2 * (r - l), mEmptyView.getMeasuredHeight());
        }

        if (mModel == SwipeController.SwipeModel.SWIPE_NONE || mModel == SwipeController.SwipeModel.SWIPE_ONLY_LOADINN) {
            overScrollTop = 0;
        }

        if (!canOverScrollBottom || mModel == SwipeController.SwipeModel.SWIPE_NONE || mModel == SwipeController.SwipeModel.SWIPE_ONLY_REFRESH) {
            overScrollBottom = 0;
        }

        if (oldContentScroll > mContentScroll && getScrollY() > 0) {
            int currentScrollY = getScrollY();
            currentScrollY = currentScrollY - (oldContentScroll - mContentScroll);
            currentScrollY = currentScrollY > 0 ? currentScrollY : 0;
            scrollTo(0, currentScrollY);
        }

    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
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
                    mTargetView = child;
                    mTargetViewContain = child;
                    mTargetViewContainIndex = i;
                    break;
                }
            }
        }

        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            mIsTouchEventMode = false;
        }

        if (mNestedScrollInProgress) {
            return super.dispatchTouchEvent(ev);
        }

        if (ev.getAction() == MotionEvent.ACTION_DOWN && (getScrollY() < 0 || getScrollY() > mContentScroll)) {
            mPreScroll = true;
        }

        if (mPreScroll) {
            int action = MotionEventCompat.getActionMasked(ev);
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
                        return true;
                    }
                    break;
                case MotionEventCompat.ACTION_POINTER_UP:
                    onSecondaryPointerUp(ev);
                    break;
            }
        }

        if (ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_CANCEL) {
            if (tryBackToRefreshing() || tryBackToFreshFinish() || mDraggedDispatch || mDraggedIntercept || mShouldCancelMotionEvent) {
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
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
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
                    mVelocityTracker.computeCurrentVelocity(1000, 6000);
                    mVelocityTracker.getYVelocity();
                    int initialVelocity = (int) mVelocityTracker.getYVelocity();

                    fling(-initialVelocity);
                }

                if (mVelocityTracker != null) {
                    mVelocityTracker.recycle();
                    mVelocityTracker = null;
                }
                return true;
        }

        int pointerIndex = ev.findPointerIndex(mActivePointerId);
        if (pointerIndex >= 0) {
            mDragLastY = ev.getY(pointerIndex);
        }

        if (mVelocityTracker != null) {
            mVelocityTracker.addMovement(ev);
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
        if (mScroller.computeScrollOffset() && !mScroller.isFinished()) {
            int oldY = getScrollY();
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

            if (oldY != y) {
                scrollTo(0, y);
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
            if (currentScrollY < 0) {
                progress = true;
                break lab;
            }
            if (currentScrollY > mContentScroll) {
                progress = true;
                break lab;
            }


            Integer scroll = mTargetViewContain != null ? (Integer) mTargetViewContain.getTag(1 << 30) : null;
            scroll = scroll == null ? Integer.MAX_VALUE : scroll;

            if (currentScrollY < scroll) {
                progress = true;
            }
            if (currentScrollY == scroll) {
                progress = false;
            }
            if (currentScrollY > scroll) {
                progress = !(deltaOriginY > 0 && canChildScrollUp() || deltaOriginY < 0 && canChildScrollDown());
            }
        }
        if (!progress) {
            return 0;
        }

        int deltaY = deltaOriginY;
        if (deltaOriginY == 0) return 0;

        int middleHei = -overScrollTop != 0 ? -overScrollTop + mSwipeController.getOverScrollHei() : 0;
        if (deltaOriginY < 0 && currentScrollY < middleHei) {                                                                    //过度拉伸 阻尼效果
            deltaY = (int) (deltaY * Math.pow((mSwipeController.getOverScrollHei() - (middleHei - currentScrollY)) * 1f
                    / mSwipeController.getOverScrollHei(), 2));
        }


        int willTo = currentScrollY + deltaY;
        willTo = Math.min(willTo, overScrollBottom + mContentScroll);
        willTo = Math.max(willTo, -overScrollTop);


        Integer maxScroll = mTargetViewContain != null ? (Integer) mTargetViewContain.getTag(1 << 30) : null;                                   //确保scroll值经过敏感过渡区
        maxScroll = maxScroll == null ? Integer.MAX_VALUE : maxScroll;
        if ((currentScrollY > maxScroll && willTo < maxScroll) || (currentScrollY < maxScroll && willTo > maxScroll)) {
            willTo = maxScroll;
        }


        if (mDragBeginDirect > 0 && willTo > mContentScroll) {                                                                    //确保上拉刷新独立
            willTo = mContentScroll;
        }
        if (mDragBeginDirect < 0 && willTo < 0) {                                                                                //确保下拉加载独立
            willTo = 0;
        }

        if (willTo == currentScrollY) {
            return deltaOriginY;
        }

        scrollTo(0, willTo);

        // ----------------------------------------------------------------------------------------------------------------》》下拉
        if (0 > willTo && willTo > middleHei && (mFreshStatus == ISwipe.FreshStatus.ERROR || mFreshStatus == FreshStatus.ERROR_NET
                || mFreshStatus == ISwipe.FreshStatus.SUCCESS) && !mRefreshStatusContinueRunning) {                                                 //重置下拉刷新状态
            mFreshStatus = ISwipe.FreshStatus.CONTINUE;
        }
        if (0 > willTo) {                                                                                                                            //刷新下拉状态
            int swipeViewVisibilityHei = 0 - willTo;
            switch (mFreshStatus) {
                case CONTINUE:
                    if (mRefreshStatusContinueRunning) {
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_LOADING, swipeViewVisibilityHei, mViewTop.getHeight());
                    } else if (willTo < middleHei) {
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_OVER, swipeViewVisibilityHei, mViewTop.getHeight());
                    } else {
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_TOAST, swipeViewVisibilityHei, mViewTop.getHeight());
                    }
                    break;
                case SUCCESS:
                    mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_COMPLETE_OK, swipeViewVisibilityHei, mViewTop.getHeight());
                    break;
                case ERROR:
                    mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_COMPLETE_ERROR, swipeViewVisibilityHei, mViewTop.getHeight());
                    break;
                case ERROR_NET:
                    mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_COMPLETE_ERROR_NET, swipeViewVisibilityHei, mViewTop.getHeight());
                    break;
            }
        }
        // ----------------------------------------------------------------------------------------------------------------《《 下拉


        // ---------------------------------------------------------------------------------------------------------------- 》》上拉
        if (willTo > mContentScroll && !mLoadedStatusContinueRunning) {                                                                                    //重置上拉刷新状态
            mLoadedStatusContinueRunning = true;
            if (mOnRefreshListener != null) {
                mOnRefreshListener.onLoading();
            }
        }
        if (willTo > mContentScroll) {
            if (mLoadedStatus == ISwipe.LoadedStatus.NO_MORE) {
                mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_NO_MORE, willTo - mContentScroll, mViewBottom.getHeight());
            } else if (mLoadedStatus == ISwipe.LoadedStatus.ERROR) {
                mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_ERROR, willTo - mContentScroll, mViewBottom.getHeight());
            } else if (mLoadedStatus == ISwipe.LoadedStatus.CONTINUE) {
                mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_LOADING, willTo - mContentScroll, mViewBottom.getHeight());
            }
        }
        // ---------------------------------------------------------------------------------------------------------------- 《《上拉
        return (deltaOriginY);
    }

    private boolean tryBackToRefreshing() {
        if (mIsTouchEventMode || mFreshStatus == ISwipe.FreshStatus.SUCCESS || mFreshStatus == ISwipe.FreshStatus.ERROR || mFreshStatus == FreshStatus.ERROR_NET)
            return false;
        int scrollY = getScrollY();
        int middleHei = overScrollTop != 0 ? -overScrollTop + mSwipeController.getOverScrollHei() : 0;
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
                            case CONTINUE:
                                mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_LOADING, mViewTop.getHeight(), mViewTop.getHeight());
                                break;
                        }
                        if (!mRefreshStatusContinueRunning) {
                            mRefreshStatusContinueRunning = true;
                            if (mEmptyController != null) {
                                mEmptyController.onSwipeStatue(FreshStatus.CONTINUE);
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
            animationReBackToRefreshing.setDuration(Math.abs(550 * (middleHei - scrollY) / mViewTop.getHeight()));
            animationReBackToRefreshing.start();
        }
        return isOverProgress;
    }

    private boolean tryBackToFreshFinish() {

        if (mIsTouchEventMode) return false;
        int scrollY = getScrollY();
        int middleHei = overScrollTop != 0 ? -overScrollTop + mSwipeController.getOverScrollHei() : 0;

        if (mFreshStatus == ISwipe.FreshStatus.CONTINUE && mRefreshStatusContinueRunning && scrollY == middleHei)
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
            animationReBackToTop.addListener(new Animator.AnimatorListener() {
                boolean isCancel;

                @Override
                public void onAnimationStart(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (!isCancel) {
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_TOAST, 0, mViewTop.getHeight());
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


            if (scrollY == middleHei) {
                animationReBackToTop.setDuration(Math.abs(550 * (0 - scrollY) / mViewTop.getHeight()));
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

        mScroller.abortAnimation();

    }

    // ----------------------------------------------------------------ISwipeIMP-------------------------------------------------------------------//

    // 设置刷新控制监听
    @Override
    public void setOnRefreshListener(ISwipe.OnRefreshListener onRefreshListener) {
        this.mOnRefreshListener = onRefreshListener;
    }


    @Override
    public void setFreshStatue(ISwipe.FreshStatus statue) {
        switch (statue) {
            case CONTINUE:
                mFreshStatus = ISwipe.FreshStatus.CONTINUE;
                if (mEmptyController != null) {
                    mEmptyController.onSwipeStatue(mFreshStatus);
                }
                mRefreshStatusContinueRunning = true;
                mLoadedStatus = ISwipe.LoadedStatus.CONTINUE;
                mLoadedStatusContinueRunning = false;

                if (mOnRefreshListener != null) {
                    mOnRefreshListener.onRefresh();
                }

                mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_LOADING,
                        -getScrollY(), mViewTop.getHeight());
                tryBackToRefreshing();
                break;
            case SUCCESS:                                                                                   //设置刷新成功 自动隐藏
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mFreshStatus = ISwipe.FreshStatus.SUCCESS;
                        if (mEmptyController != null) {
                            mEmptyController.onSwipeStatue(mFreshStatus);
                        }

                        mRefreshStatusContinueRunning = false;
                        mLoadedStatus = ISwipe.LoadedStatus.CONTINUE;
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
                        mLoadedStatus = LoadedStatus.CONTINUE;
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
                        mLoadedStatus = LoadedStatus.CONTINUE;
                        mLoadedStatusContinueRunning = false;
                        mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_HEAD_COMPLETE_ERROR,
                                -getScrollY(), mViewTop.getHeight());
                        tryBackToFreshFinish();
                    }
                }, 1000);
                break;
        }
    }

    @Override
    public void setLoadMoreStatus(ISwipe.LoadedStatus status) {
        switch (status) {
            case CONTINUE:
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
                this.mLoadedStatus = ISwipe.LoadedStatus.CONTINUE;
                mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_LOADING,
                        getHeight() - mViewBottom.getTop(), mViewBottom.getHeight());
                break;
            case ERROR:
                this.mLoadedStatusContinueRunning = true;
                this.mLoadedStatus = ISwipe.LoadedStatus.ERROR;
                mSwipeController.onSwipeStatue(SwipeController.SwipeStatus.SWIPE_LOAD_ERROR,
                        getHeight() - mViewBottom.getTop(), mViewBottom.getHeight());
                break;
            case NO_MORE:
                this.mLoadedStatusContinueRunning = true;
                this.mLoadedStatus = ISwipe.LoadedStatus.NO_MORE;
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

    // 自定义空白页面
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

    // 设置是否显示空白页面
    @Override
    public void enableEmptyView(boolean show) {
        if (mShowEmptyView != show) {
            mShowEmptyView = show;
            requestLayout();
            // TODO release mTargetView  this is no be layout any more;
            if (mEmptyReplaceView == mTargetViewContain && mShowEmptyView) {
                mTargetView = null;
                mTargetViewContain = null;
            }

        }
    }


}
