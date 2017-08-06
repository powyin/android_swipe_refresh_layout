
package com.powyin.nestscroll;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.powyin.nestscroll.adapter.TypePowViewHolder_Pic_1;
import com.powyin.nestscroll.adapter.TypePowViewHolder_Text;
import com.powyin.nestscroll.adapter.TypePowViewHolder_Pic_4;
import com.powyin.nestscroll.net.DataModel;
import com.powyin.scroll.adapter.AdapterDelegate;
import com.powyin.scroll.adapter.MultipleRecycleAdapter;
import com.powyin.scroll.adapter.PowViewHolder;

/**
 * Created by powyin on 2016/7/27.
 */
public class SimpleMuiltpleAdapter extends Activity implements View.OnClickListener {

    RecyclerView mRecyclerView;

    MultipleRecycleAdapter<DataModel> multipleRecycleAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_muiltiple_adpter);
        findView();
        init();

        head = getLayoutInflater().inflate(R.layout.view_holder_user,null);
        foot = getLayoutInflater().inflate(R.layout.foot,null);
        space = getLayoutInflater().inflate(R.layout.space,null);

    }

    View head;
    View foot;
    View space;

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.click_me_to_switch_head:
                v.setSelected(!v.isSelected());
                if(v.isSelected()){
                   multipleRecycleAdapter.setHeadView(head);
                }else {
                    multipleRecycleAdapter.removeHeadView();
                }
                break;
            case R.id.click_me_to_switch_foot:
                v.setSelected(!v.isSelected());
                if(v.isSelected()){
                    multipleRecycleAdapter.setFootView(foot);
                }else {
                    multipleRecycleAdapter.removeFootView();
                }
                break;
            case R.id.click_me_to_switch_space:
                v.setSelected(!v.isSelected());
                multipleRecycleAdapter.setEmptyView(space);
                if(v.isSelected()){
                    multipleRecycleAdapter.enableEmptyView(true);
                }else {
                    multipleRecycleAdapter.enableEmptyView(false);
                }
                break;
            case R.id.click_me_to_switch_load:
                v.setSelected(!v.isSelected());
                if(v.isSelected()){
                    multipleRecycleAdapter.enableLoadMore(true);
                }else {
                    multipleRecycleAdapter.enableLoadMore(false);
                }

                break;
            case R.id.click_me_to_add_data:

                multipleRecycleAdapter.addData(multipleRecycleAdapter.getDataCount(),new DataModel(2));
                multipleRecycleAdapter.addData(multipleRecycleAdapter.getDataCount(),new DataModel(1));
                multipleRecycleAdapter.addData(multipleRecycleAdapter.getDataCount(),new DataModel(-1));                              //特意加入的无法展示的数据类型；  可以通过multipleAdapter.setShowErrorHolder(false) 关闭无法展示数据的显示
                multipleRecycleAdapter.addData(multipleRecycleAdapter.getDataCount(),new DataModel(3));


                break;
            case R.id.click_me_to_free_data:

                multipleRecycleAdapter.clearData();

                break;



        }
    }


    private void findView() {
        findViewById(R.id.click_me_to_switch_head).setOnClickListener(this);
        findViewById(R.id.click_me_to_switch_foot).setOnClickListener(this);
        findViewById(R.id.click_me_to_switch_space).setOnClickListener(this);
        findViewById(R.id.click_me_to_switch_load).setOnClickListener(this);

        findViewById(R.id.click_me_to_add_data).setOnClickListener(this);
        findViewById(R.id.click_me_to_free_data).setOnClickListener(this);
        findViewById(R.id.click_me_to_switch_space).setOnClickListener(this);
        findViewById(R.id.click_me_to_switch_load).setOnClickListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycle);
    }

    private void init() {
        initRecycleView();
        //   initListView();

        multipleRecycleAdapter.setOnLoadMoreListener(new AdapterDelegate.OnLoadMoreListener() {

            @Override
            public void onLoadBegin() {

            }

            @Override
            public void onLoadEnd() {
                System.out.println("llllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllllll");
            }
        });

    }


    private void initRecycleView() {
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());

        multipleRecycleAdapter = new MultipleRecycleAdapter<>(this, TypePowViewHolder_Text.class, TypePowViewHolder_Pic_1.class, TypePowViewHolder_Pic_4.class);
        for (int i = 0; i < 3; i++) {
            int rad = (int) (Math.random() * 10) % 3 + 1;
            multipleRecycleAdapter.addData(0, new DataModel(rad));
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
