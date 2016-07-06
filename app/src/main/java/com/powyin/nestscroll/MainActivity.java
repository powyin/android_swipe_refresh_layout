package com.powyin.nestscroll;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.powyin.scroll.widget.ScrollCombine;


/**
 * Created by MT3020 on 2016/3/10.
 */
public class MainActivity extends Activity {
    ScrollCombine combine;
    RecyclerView mRecyclerView;
    Button buttonDown;
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        findView();
        init();
    }



    private void init(){

        initListView();

        buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                combine.finishRefresh();
            }
        });
    }


    private void initListView(){
        listView.setAdapter(new BaseAdapter() {
            @Override
            public int getCount() {
                return 30;
            }

            @Override
            public Object getItem(int i) {
                return null;
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

            @Override
            public View getView(int i, View view, ViewGroup viewGroup) {
                if(view==null){
                    return getLayoutInflater().inflate(R.layout.header,viewGroup,false);
                }
                return view;
            }
        });
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


    private void findView(){
      //  mRecyclerView = (RecyclerView)findViewById(R.id.my_recycle);
        listView = (ListView)findViewById(R.id.my_list);
        buttonDown = (Button)findViewById(R.id.click_me_to_bottom);
        combine = (ScrollCombine)findViewById(R.id.nest_combine);
    }


    class Holder extends RecyclerView.ViewHolder {
        public Holder() {
            super(getLayoutInflater().inflate(R.layout.view_holder_item,null));
        }
    }




}


































































