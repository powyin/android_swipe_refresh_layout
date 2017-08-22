package com.powyin.nestscroll;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ListView;

import com.powyin.nestscroll.adapter.TypePowViewHolder_Pic_1;
import com.powyin.nestscroll.adapter.TypePowViewHolder_Text;
import com.powyin.nestscroll.adapter.TypePowViewHolder_Pic_4;
import com.powyin.nestscroll.net.DataModel;
import com.powyin.nestscroll.refresh.SwipeControllerStyle_Horizontal;
import com.powyin.scroll.adapter.AdapterDelegate;
import com.powyin.scroll.adapter.MultipleListAdapter;
import com.powyin.scroll.adapter.MultipleRecycleAdapter;
import com.powyin.scroll.adapter.PowViewHolder;
import com.powyin.scroll.widget.ISwipe;
import com.powyin.scroll.widget.SwipeRefresh;

/**
 * Created by powyin on 2016/7/27.
 */
public class SimpleSwipeRefresh extends Activity implements View.OnClickListener {
    SwipeRefresh swipeRefresh;
    RecyclerView mRecyclerView;
    ListView listView;
    MultipleListAdapter<DataModel> multipleListAdapter;
    MultipleRecycleAdapter<DataModel> multipleRecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_swipe_refresh);
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
                swipeRefresh.setFreshResult(ISwipe.FreshStatus.SUCCESS);                    //下拉刷新完成
                break;
            case R.id.click_me_to_stop_foot_fresh:
                if(multipleListAdapter!=null){
                    multipleListAdapter.addData(multipleListAdapter.getDataCount(),new DataModel(2));
                    multipleListAdapter.addData(multipleListAdapter.getDataCount(),new DataModel(1));
                    multipleListAdapter.addData(multipleListAdapter.getDataCount(),new DataModel(-1));                                 //特意加入的无法展示的数据类型；  可以通过multipleAdapter.setShowErrorHolder(false) 关闭无法展示数据的显示
                    multipleListAdapter.addData(multipleListAdapter.getDataCount(),new DataModel(3));
                }
                if(multipleRecycleAdapter!=null){
                    multipleRecycleAdapter.addData(multipleRecycleAdapter.getDataCount(),new DataModel(2));
                    multipleRecycleAdapter.addData(multipleRecycleAdapter.getDataCount(),new DataModel(1));
                    multipleRecycleAdapter.addData(multipleRecycleAdapter.getDataCount(),new DataModel(-1));                              //特意加入的无法展示的数据类型；  可以通过multipleAdapter.setShowErrorHolder(false) 关闭无法展示数据的显示
                    multipleRecycleAdapter.addData(multipleRecycleAdapter.getDataCount(),new DataModel(3));
                }
                swipeRefresh.completeLoadMore();                 //已经获取更多数据   隐藏上拉加载进度条
                break;
            case R.id.click_me_to_stop_foot_over:
                swipeRefresh.setLoadMoreResult(SwipeRefresh.LoadedStatus.NO_MORE);                  //已经没有更多数据   全部数据已经获得
                break;
        }
    }


    private void findView(){
        findViewById(R.id.click_me_to_set_swipe_control).setOnClickListener(this);
        findViewById(R.id.click_me_to_stop_head).setOnClickListener(this);
        findViewById(R.id.click_me_to_stop_foot_fresh).setOnClickListener(this);
        findViewById(R.id.click_me_to_stop_foot_over).setOnClickListener(this);
    //    listView = (ListView)findViewById(R.id.my_list);
        mRecyclerView = (RecyclerView)findViewById(R.id.my_recycle);
        swipeRefresh = (SwipeRefresh)findViewById(R.id.re);
    }

    private void init(){
        initRecycleView();
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

//    private void initListView(){
//        multipleListAdapter = new  MultipleListAdapter<>(this, TypePowViewHolder_Text.class, TypePowViewHolder_Pic_1.class , TypePowViewHolder_Pic_4.class);
//
//        listView.setAdapter(multipleListAdapter);
//        for(int i=0;i<3;i++){
//            int rad = (int)(Math.random()*10)%3+1;
//            multipleListAdapter.addData(0,new DataModel(rad));
//        }
//
//        multipleListAdapter.setOnItemClickListener(new AdapterDelegate.OnItemClickListener() {
//            @Override
//            public void onClick(PowViewHolder holder, Object data, int index, int resId) {
//                System.out.println(".....................clkci" + holder + "   " + data + "   " + index + " " + resId);
//            }
//        });
//    }

    private void initRecycleView(){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        multipleRecycleAdapter = new MultipleRecycleAdapter<>(this, TypePowViewHolder_Text.class, TypePowViewHolder_Pic_1.class , TypePowViewHolder_Pic_4.class);
        for(int i=0;i<3;i++){
            int rad = (int)(Math.random()*10)%3+1;
            multipleRecycleAdapter.addData(0,new DataModel(rad));
        }
        mRecyclerView.setAdapter(multipleRecycleAdapter);

        multipleRecycleAdapter.setOnItemClickListener(new AdapterDelegate.OnItemClickListener<DataModel>() {
            @Override
            public void onClick(PowViewHolder<DataModel> holder, DataModel data, int index, int resId) {
                System.out.println(".....................clkci" + holder + "   " + data + "   " + index + " " + resId);
            }
        });
    }






}
