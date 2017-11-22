package com.powyin.scroll.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;

/**
 * Created by whoha on 2017/8/8.
 */

public class LoadProgressBar extends View {

    public LoadProgressBar(Context context) {
        this(context, null);
    }

    public LoadProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        circlePaint = new Paint();
        circlePaint.setColor(0x85ffffff);
        circlePaint.setStrokeWidth(4);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        float scale = getContext().getResources().getDisplayMetrics().density;
        int target = (int) (40 * scale + 0.5f);

        setMeasuredDimension(getMeasuredWidth(), target);

    }


    private ValueAnimator animator;
    private Paint circlePaint;
    private boolean mAttach = false;
    private int canvasWei;
    private int canvasHei;

    private final int ballCount = 12;
    private float divide;

    private int mVisibility = 0;

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        for (int i = 0; i < ballCount; i++) {
            float wei = 4 * (1f * i / ballCount - 0.5f) + divide;
            wei = canvasWei / 2 + getSplit(wei) * canvasWei * 0.08f;
            canvas.drawCircle(wei, canvasHei / 2, 8, circlePaint);
        }
    }


    public void ensureAnimation(boolean forceReStart) {
        if (forceReStart) {
            if (animator != null) {
                animator.cancel();
                animator = null;
            }
        } else {
            if (animator != null && animator.isStarted()) {
                return;
            }
        }

        animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(2000);
        animator.setRepeatCount(1);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                divide = 8 * ((System.currentTimeMillis() % 3000) - 1500) / 3000f;
                invalidate();
            }
        });

        animator.addListener(new Animator.AnimatorListener() {
            boolean isDeprecated = false;
            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isDeprecated || !mAttach || animator != animation || mVisibility == INVISIBLE || mVisibility == GONE)
                    return;
                isDeprecated = true;
                ViewParent parent = getParent();
                while (parent != null && !(parent instanceof ISwipe)) {
                    parent = parent.getParent();
                }

                if (parent instanceof SwipeNest) {
                    SwipeNest swipeNest = (SwipeNest) parent;
                    if (swipeNest.getScrollY() > swipeNest.computeVerticalScrollRange() - swipeNest.computeVerticalScrollExtent()) {
                        ensureAnimation(true);
                    }
                    return;
                }

                if (parent instanceof SwipeRefresh) {
                    if (((SwipeRefresh) parent).getScrollY() > 0) {
                        ensureAnimation(true);
                    }
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

        });

        animator.start();
    }




    @Override
    protected void onVisibilityChanged(@NonNull View changedView,  int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        mVisibility = visibility;


        if ((visibility == GONE || visibility == INVISIBLE) && animator != null) {
            animator.cancel();
            animator = null;
        }

        if (visibility == VISIBLE) {
            ensureAnimation(false);
        }
    }


    private float getSplit(float value) {
        int positive = value >= 0 ? 1 : -1;                                 //保存符号 判断正负
        value = Math.abs(value);
        if (value <= 1) return value * positive;
        return (float) Math.pow(value, 2) * positive;
    }



    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttach = true;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttach = false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        canvasWei = right - left;
        canvasHei = bottom - top;
        ensureAnimation(false);
    }


}
