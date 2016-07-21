package com.powyin.scroll.widget;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.powyin.scroll.R;

/**
 * Created by powyin on 2016/7/2.
 */
public class SwipeControlImp implements SwipeControl {
    private Context mContent;

    private View headView;
    private CircleViewBac statusPre;
    private ImageView statusLoad;
    private ImageView statusComplete;
    private TextView textInfo;

    private View footView;
    private LoadProgressBar loadProgressBar;
    private TextView textLoad;

    SwipeControlImp(Context context) {
        this.mContent = context;
        LayoutInflater inflater = LayoutInflater.from(mContent);
        this.headView = inflater.inflate(R.layout.powyin_scroll_default_head_swipe, null);
        statusPre = (CircleViewBac) headView.findViewById(R.id.swipe_image_info);
        statusLoad = (ImageView) headView.findViewById(R.id.swipe_refresh);
        statusComplete = (ImageView) headView.findViewById(R.id.swipe_ok);
        textInfo = (TextView) headView.findViewById(R.id.swipe_text_info);

        this.footView = inflater.inflate(R.layout.powyin_scroll_default_loading_more,null);
        loadProgressBar = (LoadProgressBar) footView.findViewById(R.id.powyin_scroll_load_bar);
        textLoad = (TextView)footView.findViewById(R.id.powyin_scroll_load_more);
    }

    @Override
    public View getSwipeHead() {
        return headView;
    }

    @Override
    public View getSwipeFoot() {
        return footView;
    }

    @Override
    public int getOverScrollHei() {
        return 300;
    }

    @Override
    public void onSwipeStatue(SwipeStatus status, int visibleHei, int wholeHei) {
        switch (status) {
            case SWIPE_HEAD_OVER:
                statusPre.setVisibility(View.VISIBLE);
                statusPre.setProgress(1);
                statusLoad.clearAnimation();
                statusLoad.setVisibility(View.INVISIBLE);
                statusComplete.setVisibility(View.INVISIBLE);
                if(!textInfo.getText().toString().equals("松开刷新")){
                    textInfo.setText("松开刷新");
                }

                break;
            case SWIPE_HEAD_TOAST:
                statusPre.setVisibility(View.VISIBLE);
                statusLoad.clearAnimation();
                statusLoad.setVisibility(View.INVISIBLE);
                statusComplete.setVisibility(View.INVISIBLE);
                float radio = 1f * (visibleHei - textInfo.getHeight()) / statusPre.getHeight();
                statusPre.setProgress(radio);
                if(!textInfo.getText().toString().equals("上拉刷新")){
                    textInfo.setText("上拉刷新");
                }
                break;
            case SWIPE_HEAD_LOADING:
                statusPre.setVisibility(View.INVISIBLE);
                if (statusLoad.getVisibility() != View.VISIBLE) {
                    statusLoad.setVisibility(View.VISIBLE);
                    statusLoad.setAnimation(AnimationUtils.loadAnimation(mContent, R.anim.powyin_scroll_rotale));
                }
                statusComplete.setVisibility(View.INVISIBLE);
                if(!textInfo.getText().toString().equals("正在拼命刷新中")){
                    textInfo.setText("正在拼命刷新中");
                }
                break;
            case SWIPE_HEAD_COMPLETE:
                statusPre.setVisibility(View.INVISIBLE);
                statusLoad.clearAnimation();
                statusLoad.setVisibility(View.INVISIBLE);
                statusComplete.setVisibility(View.VISIBLE);
                if(!textInfo.getText().toString().equals("刷新成功")){
                    textInfo.setText("刷新成功");
                }
                break;

            case SWIPE_FOOT_LOADING:
                loadProgressBar.setVisibility(View.VISIBLE);
                loadProgressBar.ensureAnimation();
                textLoad.setVisibility(View.GONE);
                break;
            case SWIPE_FOOT_COMPLETE:
                loadProgressBar.setVisibility(View.GONE);
                textLoad.setVisibility(View.VISIBLE);
                break;
        }
    }

}

