package com.powyin.scroll.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.widget.ListView;



/**
 * Created by powyin on 2016/7/21.
 */
class LoadProgressBar extends View {              //刷新视图
    public LoadProgressBar(Context context) {
        this(context,null);
    }
    public LoadProgressBar(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    public LoadProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mFixedHei = ViewUtils.dip2px(context,50);
        circlePaint = new Paint();
        circlePaint.setColor(0x99000000);
        circlePaint.setStrokeWidth(4);
    }
    private int mFixedHei;
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec,heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), mFixedHei);
    }

    ValueAnimator animator;
    Paint circlePaint;
    int canvasWei;
    int canvasHei;

    int ballCount = 8;
    float divide;



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(int i =0 ;i <ballCount;i++){
            float wei = 4*(1f*i/ballCount -0.5f) + divide;
            wei =  canvasWei/2 +getSplit(wei)*canvasWei*0.08f ;
            canvas.drawCircle(wei,canvasHei/2,8,circlePaint);
        }
    }

    void ensureAnimation(){
        ensureAnimation(false);
    }

    private void ensureAnimation( boolean forceReStart ){
        if(forceReStart){
            if(animator!=null){
                animator.cancel();
            }
        }else {
            if(animator!=null && animator.isRunning()){
                return;
            }
        }

        animator = ValueAnimator.ofFloat(0,1);
        animator.setDuration(2000);
        animator.setRepeatCount(5);

        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                divide = 8*((System.currentTimeMillis()%3000)-1500)/3000f;
                invalidate();
            }
        });
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
            }
            @Override
            public void onAnimationEnd(Animator animation) {
                ViewParent parent =  getParent() ;
                while (parent !=null && !(parent instanceof SwipeRefresh ) && !( parent instanceof  SwipeNest)) {
                    parent = parent.getParent();
                }
                if(parent==null) return;
                View viewParent = (View) parent;
                if(viewParent.getScrollY()>0 && animation == animator){
                    ensureAnimation(true);
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

    private float getSplit(float value){
        int positive = value>=0 ? 1 : -1;                                 //保存符号 判断正负
        value = Math.abs(value);
        if(value<=1) return value*positive;
        return (float)Math.pow(value,2) * positive;
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        canvasWei = right-left;
        canvasHei = bottom-top;
        ensureAnimation();
    }










}
