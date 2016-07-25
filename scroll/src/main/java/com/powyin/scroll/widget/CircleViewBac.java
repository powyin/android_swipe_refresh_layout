package com.powyin.scroll.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

import com.powyin.scroll.R;

/**
 * Created by powyin on 2016/7/21.
 */

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
        arcRectF = new RectF(0, 0, w , h );
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
        int sweepAngle = progress <= 0 ? 0 : progress >= 1 ? 180 : Math.min(180, (int) (180 * progress - 0.61f));
        bitmapDrawablePre.setBounds(0, 0, wei, hei);
        canvas.save();
        canvas.rotate(sweepAngle, wei / 2, hei / 2);
        bitmapDrawablePre.draw(canvas);
        canvas.restore();
    }


}

