package com.powyin.nestscroll;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.powyin.nestscroll.adapter.TypeViewHolder_Pic_4;
import com.powyin.nestscroll.adapter.TypeViewHolder_Text;
import com.powyin.nestscroll.adapter.TypeViewHolder_Pic_1;
import com.powyin.nestscroll.net.DataModel;
import com.powyin.scroll.adapter.base.MultiAdapter;
import com.powyin.scroll.widget.SwipeNest;
import com.powyin.scroll.widget.SwipeRefresh;

import java.util.Date;


/**
 * Created by MT3020 on 2016/3/10.
 */
public class MainActivity extends Activity {
    Button buttonDown;

    SwipeNest swipeNest;
    SwipeRefresh swipeRefresh;
    RecyclerView mRecyclerView;

    ListView listView;

    MultiAdapter<DataModel> multiAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        findView();
        init();
    }

    private void findView(){
        buttonDown = (Button)findViewById(R.id.click_me_to_bottom);
        listView = (ListView)findViewById(R.id.my_list);
        swipeRefresh = (SwipeRefresh)findViewById(R.id.re);
    }

    private void init(){
    //    initRecycle();
        initListView();
        buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
             //   swipeNest.finishRefresh();
                buttonDown.setText("刷新结束");
                swipeRefresh.finishRefresh();
            }
        });

        swipeRefresh.setOnRefreshListener(new SwipeRefresh.OnRefreshListener() {
            @Override
            public void onRefresh() {
                System.out.println("------------------------------------------------loading----------------------->>>>>>>>");
                buttonDown.setText("开始刷新---》 点击结束");
            }
        });

    }

    private void initListView(){
        multiAdapter = MultiAdapter.getByClass(this, TypeViewHolder_Text.class, TypeViewHolder_Pic_1.class , TypeViewHolder_Pic_4.class);
        listView.setAdapter(multiAdapter);
        for(int i=0;i<20;i++){
            int rad = (int)(Math.random()*10)%3+1;
            System.out.println(rad+":::::::::::");
            multiAdapter.addLast(new DataModel(rad));
        }
    }

    private void initRecycle(){
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                return new Holder();
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                TextView textView = (TextView) holder.itemView.findViewById(R.id.my_id);
                textView.setText("this is for you 托尔斯泰"+position);
            }

            @Override
            public int getItemCount() {
                return 40;
            }
        });
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }





    class Holder extends RecyclerView.ViewHolder {
        public Holder() {
            super(getLayoutInflater().inflate(R.layout.recycler_view_holder_item,null));
        }
    }




}


































































