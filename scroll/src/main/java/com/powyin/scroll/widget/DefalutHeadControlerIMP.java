package com.powyin.scroll.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.powyin.scroll.R;

/**
 * Created by powyin on 2016/7/2.
 */
public class DefalutHeadControlerIMP implements SwipeHeadControl {

    private Context mContent;
    private View rootView ;

    private CircleViewBac circleViewBac;
    private ImageView imageView;
    private TextView textInfo;

    DefalutHeadControlerIMP(Context context){
        this.mContent=context;
        rootView = LayoutInflater.from(context).inflate(R.layout.powyin_scroll_default_head_swipe,null);
        circleViewBac = (CircleViewBac)rootView.findViewById(R.id.swipe_image_info);
        textInfo = (TextView)rootView.findViewById(R.id.swipe_text_info);
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
    public void onSwipeStatue(SwipeStatus status, int showHei) {

        switch (status){
            case SWIPE_OVER_PRE:
                float progress = Math.abs(1f*showHei/rootView.getHeight());
                circleViewBac.setProgress(progress,true,true);
                textInfo.setText("松开刷新");
                break;
            case SWIPE_TOAST:
                textInfo.setText("上拉刷新");

                break;
            case SWIPE_LOADING:
                textInfo.setText("正在拼命刷新中");

                break;
            case SWIPE_COMPLETE:

                textInfo.setText("刷新成功");

                break;
        }
    }

}


class CircleViewBac extends View {
    public CircleViewBac(Context context) {
        super(context);
    }

    public CircleViewBac(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CircleViewBac(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    float progress;
    boolean isFreshComplete;
    boolean isFreshing;

    public void setProgress(float progress , boolean isFreshComplete, boolean isFreshing){
        this.progress = progress;
        this.isFreshComplete = isFreshComplete;
        this.isFreshing = isFreshing;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawCircle(canvas);
        drawTag(canvas);
    }

    Paint arcPaint;
    RectF arcRectF;
    int padding = 2;
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        arcRectF = new RectF(padding, padding,w- padding,h- padding);
        arcPaint = new Paint();
        arcPaint.setStrokeWidth(2);
        arcPaint.setStyle(Paint.Style.STROKE);
        arcPaint.setColor(Color.RED);
        arcPaint.setAntiAlias(true);
    }

    private void drawCircle(Canvas canvas){
        int sweepAngle = progress>=0 && progress<=1? (int)(360*progress) : 360;
        canvas.drawArc(arcRectF,90,sweepAngle,false,arcPaint);

        //   imageView.setRotation(sweepAngle);

        //   View view = null;


        System.out.println(progress+"xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");

    }

    private void drawTag(Canvas canvas){
        if(progress<0.9f) return;
    }
}



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
//            boolean isFreshContinue;
//
//            public void setProgress(float progress , boolean isFreshComplete, boolean isFreshContinue){
//                this.progress = progress;
//                this.isFreshComplete = isFreshComplete;
//                this.isFreshContinue = isFreshContinue;
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