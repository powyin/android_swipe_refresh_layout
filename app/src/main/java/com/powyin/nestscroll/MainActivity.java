package com.powyin.nestscroll;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.widget.NestedScrollView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

/**
 * Created by MT3020 on 2016/3/10.
 */
public class MainActivity extends Activity implements View.OnClickListener{


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        findView();

        FrameLayout view = null;




    }


    @Override
    public void onClick(View v) {
        Intent intent;
        switch (v.getId()){
            case R.id.click_me_to_nest:
                intent = new Intent(this,SimpleSwipeNest.class);
                startActivity(intent);
                break;
            case R.id.click_me_to_refresh:
                intent = new Intent(this,SimpleSwipeRefresh.class);
                startActivity(intent);
                break;
            case R.id.click_me_to_adapter:
                intent = new Intent(this,SimpleMuiltpleAdapter.class);
                startActivity(intent);
                break;
            case R.id.click_me_to_swipe_view:
                intent = new Intent(this, SimpleSwipeRefreshNomal.class);
                startActivity(intent);
                break;

        }
    }

    private void findView(){
        findViewById(R.id.click_me_to_nest).setOnClickListener(this);
        findViewById(R.id.click_me_to_refresh).setOnClickListener(this);
        findViewById(R.id.click_me_to_adapter).setOnClickListener(this);
        findViewById(R.id.click_me_to_swipe_view).setOnClickListener(this);
    }



}


































































