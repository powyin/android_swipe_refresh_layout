package com.powyin.nestscroll;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;

import com.powyin.nestscroll.refresh.SwipeControllerStyle_Horizontal;
import com.powyin.scroll.widget.ISwipe;
import com.powyin.scroll.widget.SwipeRefresh;

/**
 * Created by powyin on 2017/7/17.
 */

public class SimpleSwipeRefreshNomal extends Activity implements View.OnClickListener{

    SwipeRefresh swipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.swipe_nomal);
        findView();
        init();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.click_me_to_set_swipe_control:
                swipeRefresh.setSwipeController(new SwipeControllerStyle_Horizontal(this));               //设置定义刷新样式
                break;
            case R.id.click_me_to_stop_head:
                swipeRefresh.setFreshStatue(ISwipe.FreshStatus.SUCCESS);                    //下拉刷新完成
                break;
            case R.id.click_me_to_stop_foot_fresh:

                swipeRefresh.setLoadMoreStatus(SwipeRefresh.LoadedStatus.CONTINUE);                 //已经获取更多数据   隐藏上拉加载进度条
                break;
            case R.id.click_me_to_stop_foot_over:
                swipeRefresh.setLoadMoreStatus(SwipeRefresh.LoadedStatus.NO_MORE);                  //已经没有更多数据   全部数据已经获得
                break;
        }
    }


    private void findView(){
        findViewById(R.id.click_me_to_set_swipe_control).setOnClickListener(this);
        findViewById(R.id.click_me_to_stop_head).setOnClickListener(this);
        findViewById(R.id.click_me_to_stop_foot_fresh).setOnClickListener(this);
        findViewById(R.id.click_me_to_stop_foot_over).setOnClickListener(this);

        swipeRefresh = (SwipeRefresh)findViewById(R.id.re);
    }

    private void init(){

        //   initListView();
        swipeRefresh.setOnRefreshListener(new SwipeRefresh.OnRefreshListener() {
            @Override
            public void onRefresh() {
                System.out.println("------------------------------------------------onRefresh----------------------->>>>>>>>");
                //开始上拉刷新
            }

            @Override
            public void onLoading() {
                System.out.println("------------------------------------------------onLoading----------------------->>>>>>>>");
                // 开始加载更多
            }
        });

    }







}
