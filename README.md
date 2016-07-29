


## SwipeRefresh 支持单个View 的下拉刷新以及上拉加载
## SwipeNest 垂直布局(1~N)个NestScrollChilder 实现下拉刷新(不支持上拉加载） 
## MultipleAdapter 快速实现 ListView 多种类型展示 

Add Gradle dependency:
```gradle
dependencies {
  compile 'com.github.powyin:scroll:1.3.1'
}
```

### SwipeRefresh UI

|刷新（可自定义）|下拉加载获取新数据(可自定义)|上拉加载数据全部获得(可自定义)|
|---|---|----
|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_pre.gif)|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_load_2.gif)|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_load_1.gif)|


### SwipeNest UI

|刷新（可自定义）|平滑多个NestScrollChilder之间的滚动|自定义刷新范例|
|---|---|----
|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/nest_pre.gif)|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/nest_pre_1.gif)|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/nest_pre_2.gif)|


### how to use  SwipeRefresh

      <com.powyin.scroll.widget.SwipeRefresh
        android:id = "@+id/re"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
         <!--app:fresh_model="ONLY_REFRESH"                                    枚举类型 设置支持刷新模式-->
        app:fresh_model="BOTH"
        >
        <ListView
            android:id="@+id/my_list"
            android:background="#ffffffff"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
        <!--或者是RecyclerView-->
        <!--或者 Any View-->
        <!--<android.support.v7.widget.RecyclerView-->
            <!--android:background="#ffffff"-->
            <!--android:id="@+id/my_recycle"-->
            <!--android:overScrollMode="never"-->
            <!--android:layout_width="match_parent"-->
            <!--android:layout_height="match_parent"/>-->
    </com.powyin.scroll.widget.SwipeRefresh>
    
### how to use  SwipeNest 

      <com.powyin.scroll.widget.SwipeNest
        android:id="@+id/nest_combine"
        android:background="#e5e5e5"
        android:layout_width="match_parent"
        android:layout_height="match_parent" >

        <android.support.v4.widget.NestedScrollView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:overScrollMode="never">
            <FrameLayout
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:background="#b7b7b7"
                android:clickable="true">
                <ImageView
                    android:layout_width="match_parent"
                    android:layout_height="300dp"
                    android:src="@drawable/pic_4"
                    android:scaleType="centerCrop"  />
            </FrameLayout>
        </android.support.v4.widget.NestedScrollView>

        <android.support.v7.widget.RecyclerView
            android:background="#ffffff"
            android:id="@+id/my_recycle"
            android:overScrollMode="never"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
      </com.powyin.scroll.widget.SwipeNest>
    
    
### 自定义刷新UI

```
调用swipeRefresh.setSwipeControl(SwipeControl control) 或者 SwipeNest.setSwipeControl(SwipeControl control);

继承范例
public class SwipeControlStyle_Horizontal implements SwipeControl {
    private Context mContent;

    private View headView;
    private View overHead;
    private ProgressBar statusPre;

    private ImageView statusComplete;
    private TextView textInfo;

    private View footView;
    private TextView loadProgressBar;
    private TextView textLoad;

    public SwipeControlStyle_Horizontal(Context context) {
        this.mContent = context;
        LayoutInflater inflater = LayoutInflater.from(mContent);
        this.headView = inflater.inflate(R.layout.swipe_control_head, null);
        overHead = headView.findViewById(R.id.swipe_over_head);
        statusPre = (ProgressBar) headView.findViewById(R.id.swipe_image_info);

        statusComplete = (ImageView) headView.findViewById(R.id.swipe_ok);
        textInfo = (TextView) headView.findViewById(R.id.swipe_text_info);

        this.footView = inflater.inflate(R.layout.swipe_control_foot,null);
        loadProgressBar = (TextView) footView.findViewById(R.id.text_load_more_progress);
        textLoad = (TextView)footView.findViewById(R.id.text_load_more_over);
    }

    // 刷新头部
    @Override
    public View getSwipeHead() {
        return headView;
    }

    // SwipeRefresh(必须 getSwipeFoot() != null )  SwipeNest(可以 getSwipeFoot() == null 其不实现上拉加载)
    // 刷新尾部
    @Override
    public View getSwipeFoot() {
        return footView;
    }
    
    // 头部过度拉伸距离
    @Override
    public int getOverScrollHei() {
        return overHead.getHeight();
    }
    
    // 根据 SwipeStatus 状态值 做相应的 UI 调整即可
    // status 当前刷新的状态
    // visibleHei 下拉刷新UI 的可见高度；
    // wholeHei 下拉刷新UI 的总高度
    @Override
    public void onSwipeStatue(SwipeStatus status, int visibleHei, int wholeHei) {
        switch (status) {
            case SWIPE_HEAD_OVER:             // 提示 过度拉伸
                statusPre.setVisibility(View.VISIBLE);
                statusComplete.setVisibility(View.INVISIBLE);
                if(!textInfo.getText().toString().equals("松开刷新")){
                    textInfo.setText("松开刷新");
                }

                break;
            case SWIPE_HEAD_TOAST:           // 提示 下拉刷新
                statusPre.setVisibility(View.VISIBLE);
                statusComplete.setVisibility(View.INVISIBLE);

                if(!textInfo.getText().toString().equals("上拉刷新")){
                    textInfo.setText("上拉刷新");
                }
                break;
            case SWIPE_HEAD_LOADING:         // 提示 刷新中
                statusPre.setVisibility(View.VISIBLE);
                statusComplete.setVisibility(View.INVISIBLE);
                if(!textInfo.getText().toString().equals("正在拼命刷新中")){
                    textInfo.setText("正在拼命刷新中");
                }
                break;
            case SWIPE_HEAD_COMPLETE:        // 提示刷新完成
                statusPre.setVisibility(View.INVISIBLE);

                statusComplete.setVisibility(View.VISIBLE);
                if(!textInfo.getText().toString().equals("刷新成功")){
                    textInfo.setText("刷新成功");
                }
                break;

            case SWIPE_FOOT_LOADING:          // 上拉加载 进行中               (如果是给SwipeNest使用  忽略)
                loadProgressBar.setVisibility(View.VISIBLE);
                textLoad.setVisibility(View.GONE);
                break;
            case SWIPE_FOOT_COMPLETE:        // 上拉加载 已经拉取全部数据      (如果是给SwipeNest使用  忽略)
                loadProgressBar.setVisibility(View.GONE);
                textLoad.setVisibility(View.VISIBLE);
                break;
        }
    }

}

```

### 设置刷新监听与刷新结果处理

swipeRefresh

        swipeRefresh.setOnRefreshListener(new SwipeRefresh.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 开始下拉刷新
            }

            @Override
            public void onLoading() {
                // 开始加载更多
            }
        });
        
        swipeRefresh.finishRefresh();            //下拉刷新完成
        swipeRefresh.hiddenLoadMore();           //已经获取更多数据   隐藏上拉加载进度条
        swipeRefresh.setIsLoadComplete(true);    //已经没有更多数据   全部数据已经获得
        
SwipeNest

        swipeNest.setOnRefreshListener(new SwipeNest.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //开始上拉刷新
            }

        });
        
        swipeNest.finishRefresh();              //下拉刷新完成


### SwipeRefresh设置刷新模式

```

SwipeRefresh.setSwipeModel(SwipeControl.SwipeModel model)          

app:fresh_model=“ONLY_REFRESH”                                     

    <declare-styleable name="SwipeRefresh">
        <attr name="fresh_model">
            <enum name="BOTH" value="0" />
            <enum name="ONLY_REFRESH" value="1" />
            <enum name="ONLY_LOADINN" value="2" />
            <enum name="SWIPE_NONE" value="3" />
        </attr>
    </declare-styleable>

(BOTH = SwipeModel.SWIPE_BOTH)                    同时支持下拉刷新与上拉加载  
(ONLY_REFRESH == SwipeModel.SWIPE_ONLY_REFRESH))  只支持下拉刷新 
(ONLY_REFRESH == SwipeModel.SWIPE_ONLY_LOADINN)   只支持上拉加载 
(SWIPE_NONE == SwipeModel.SWIPE_NONE）            都不支持
```
### 注意
```
  一：SwipeRefresh 只有当包含的子View有足够内容进行独立滑动时 下拉加载才启动有效
  二：SwipeRefresh 调用finishRefresh()后；将自动触发setIsLoadComplete(false)；如果不需要， 可设反；
  三：SwipeNest 不支持上拉加载; 请用其他方式实现  比如：
          (1) Adater的getView(getView(int postion) postion 为数据列表的最后一项时可以进行上拉加载操作）
          (2) 设置ViewTreeObserver.addOnGlobalLayoutListener 监听当ViewcanScrollVertically（-1） 与 Adater.getCount() 判断当前是否可以开始上拉加载；
          (3) 直接使用RecyleView();
  四：SwipeNest 目前只支持NestedScrollingChild继承类作为子View；
  五：MultipleAdapter 只是 把各种Adapter的处理逻辑 代理到 ViewHolder 上了；MultipleAdapter会根据 不同的数据类型 分别找到能装载此Data的ViewHolder，再在ViewHolder.loadData() 做刷新操作； 设计思路类似RecycleView.ViewHolder；
  六：题外话 最好熟悉ListAdapter的Api；注意刷新Adapter时 保证 hasStableIds（）{ return true }；getItemId(int position)  获得直接与数据相关的唯一ID；
  
```

### contact me
```
  QQ 1217881964
```





