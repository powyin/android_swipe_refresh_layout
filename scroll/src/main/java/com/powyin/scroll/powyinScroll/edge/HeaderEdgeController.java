package com.powyin.scroll.powyinScroll.edge;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Created by MT3020 on 2016/3/11.
 */
public class HeaderEdgeController extends EdgeController  {

    private Context mContext;

    String preRefresh = "下拉刷新";
    String refresh = "松开刷新";



    public HeaderEdgeController(Context context) {
        this.mContext = context;
        initHeadView();
    }

    private void initHeadView(){
        setSize(0,-200,-500);
        textPaint = new Paint();
        textPaint.setTextSize(55);
        textPaint.setAntiAlias(true);


    }


    Paint textPaint;

    @Override
    public void onPullProgress(Canvas canvas) {

        if(mScroll < mShowHeight){
            int textLen = (int) textPaint.measureText(refresh);
            canvas.drawText(refresh,canvas.getWidth()/2-textLen/2,(int)(-mScroll/1.12),textPaint);
        }else {
            int textLen = (int) textPaint.measureText(preRefresh);
            canvas.drawText(preRefresh,canvas.getWidth()/2 - textLen/2,(int)(-mScroll/1.12),textPaint);
        }


        System.out.println("dispatch:: draw  " +-mScroll/2);



    }



}


















