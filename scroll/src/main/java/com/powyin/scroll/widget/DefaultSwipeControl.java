package com.powyin.scroll.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.powyin.scroll.R;

/**
 * Created by powyin on 2016/7/2.
 */
public class DefaultSwipeControl implements SwipeControl {

    private Context mContent;
    private View rootView;

    private CircleViewBac statusPre;
    private ImageView statusLoad;
    private ImageView statusComplete;
    private TextView textInfo;

    DefaultSwipeControl(Context context) {
        this.mContent = context;
        rootView = LayoutInflater.from(context).inflate(R.layout.powyin_scroll_default_head_swipe, null);
        statusPre = (CircleViewBac) rootView.findViewById(R.id.swipe_image_info);
        statusLoad = (ImageView) rootView.findViewById(R.id.swipe_refresh);
        statusComplete = (ImageView) rootView.findViewById(R.id.swipe_ok);
        textInfo = (TextView) rootView.findViewById(R.id.swipe_text_info);
    }

    @Override
    public View getSwipeView() {
        return rootView;
    }
    @Override
    public int getOverScrollHei() {
        return 300;
    }

    @Override
    public void onSwipeStatue(SwipeStatus status, int visibleHei, int wholeHei) {
        switch (status) {
            case SWIPE_OVER:
                statusPre.setVisibility(View.VISIBLE);
                statusPre.setProgress(1);
                statusLoad.clearAnimation();
                statusLoad.setVisibility(View.INVISIBLE);
                statusComplete.setVisibility(View.INVISIBLE);
                textInfo.setText("松开刷新");
                break;
            case SWIPE_TOAST:
                statusPre.setVisibility(View.VISIBLE);
                statusLoad.clearAnimation();
                statusLoad.setVisibility(View.INVISIBLE);
                statusComplete.setVisibility(View.INVISIBLE);
                float radio = 1f * (visibleHei - textInfo.getHeight()) / statusPre.getHeight();
                statusPre.setProgress(radio);
                textInfo.setText("上拉刷新");
                break;
            case SWIPE_LOADING:
                statusPre.setVisibility(View.INVISIBLE);
                if (statusLoad.getVisibility() != View.VISIBLE) {
                    statusLoad.setVisibility(View.VISIBLE);
                    statusLoad.setAnimation(AnimationUtils.loadAnimation(mContent, R.anim.powyin_scroll_rotale));
                }
                statusComplete.setVisibility(View.INVISIBLE);
                textInfo.setText("正在拼命刷新中");
                break;
            case SWIPE_COMPLETE:
                statusPre.setVisibility(View.INVISIBLE);
                statusLoad.clearAnimation();
                statusLoad.setVisibility(View.INVISIBLE);
                statusComplete.setVisibility(View.VISIBLE);
                textInfo.setText("刷新成功");
                break;
        }
    }

}


class CircleViewBac extends View {
    public CircleViewBac(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CircleViewBac(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        bitmapDrawablePre = (BitmapDrawable) context.getResources().getDrawable(R.drawable.powyin_scroll_progress_pre);
    }
    BitmapDrawable bitmapDrawablePre;
    Paint arcPaint;
    RectF arcRectF;
    int padding = 2;
    int wei;
    int hei;
    float progress;
    public void setProgress(float progress) {
        if(this.progress!=progress){
            this.progress = progress;
            invalidate();
        }
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        wei = w;
        hei = h;
        bitmapDrawablePre.setBounds(0, 0, wei, hei);
        arcRectF = new RectF(padding, padding, w - padding, h - padding);
        arcPaint = new Paint();
        arcPaint.setStrokeWidth(2);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setColor(Color.RED);
        arcPaint.setAntiAlias(true);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawImage(canvas);
    }

    // 画圆
    private void drawCircle(Canvas canvas) {
        int sweepAngle = progress >= 0 && progress <= 1 ? (int) (360 * progress) : 360;
        canvas.drawArc(arcRectF, 90, sweepAngle, false, arcPaint);
    }

    // 画图
    private void drawImage(Canvas canvas) {
        int sweepAngle = progress <= 0.61f ? 0 : progress >= 1 ? 180 : Math.min(180, (int) (180 * (progress - 0.61f) * 2.4f));
        bitmapDrawablePre.setBounds(0, 0, wei, hei);
        canvas.save();
        canvas.rotate(sweepAngle, wei / 2, hei / 2);
        bitmapDrawablePre.draw(canvas);
        canvas.restore();
    }


}

