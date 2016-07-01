package com.powyin.nestscroll;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.powyin.nestscroll.freshCombine.NestScrollCombine;


/**
 * Created by MT3020 on 2016/3/10.
 */
public class MainActivity extends Activity {


    NestScrollCombine combine;


    RecyclerView mRecyclerView;

    Button buttonDown;
    FrameLayout head;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        char c = 78;

        System.out.println(".................................................."+c);

        c = 4588;

        System.out.println(".................................................."+c);

        c = 0xfff1;

        System.out.println(".................................................."+c);


        findView();

        init();

        head.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("cccccccccccccccccccccccccccccccccccc");
            }
        });








    }



    private void init(){

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

        buttonDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("------------------------------");

                combine.reFreshCompleteUp();

            }
        });


    }




    private void findView(){
        mRecyclerView = (RecyclerView)findViewById(R.id.my_recycle);
        head = (FrameLayout)findViewById(R.id.my_head);
        buttonDown = (Button)findViewById(R.id.click_me_to_bottom);
        combine = (NestScrollCombine)findViewById(R.id.nest_combine);
    }


    class Holder extends RecyclerView.ViewHolder {
        public Holder() {
            super(getLayoutInflater().inflate(R.layout.view_holder_item,null));
        }
    }




}


































































