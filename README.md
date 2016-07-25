


# SwipeRefresh 支持单个View 的下拉刷新以及上拉加载
# SwipeNest 垂直布局多个NestScrollChilder 实现下拉刷新 
# MuilpAdapter 快速实现 ListView 多种类型展示 

### SwipeRefresh UI

|刷新|下拉加载获取新数据|上拉加载数据全部获得|
|---|---|----
|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_pre.gif)|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_load_2.gif)|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_load_1.gif)|



### how to use

      <com.powyin.scroll.widget.SwipeRefresh
        android:id = "@+id/re"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        app:fresh_model="BOTH" >
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
    
    
    
**自定义刷新UI**   

```
调用swipeRefresh.setSwipeControl(SwipeControl control);

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

            case SWIPE_FOOT_LOADING:          // 上拉加载 进行中
                loadProgressBar.setVisibility(View.VISIBLE);
                textLoad.setVisibility(View.GONE);
                break;
            case SWIPE_FOOT_COMPLETE:        // 上拉加载 已经拉取全部数据
                loadProgressBar.setVisibility(View.GONE);
                textLoad.setVisibility(View.VISIBLE);
                break;
        }
    }

}

       

```



    
**设置支持4种刷新模式**   

```
(BOTH = SwipeModel.SWIPE_BOTH) 同时支持下拉刷新与上拉加载  
(ONLY_REFRESH == SwipeModel.SWIPE_ONLY_REFRESH)) 只支持下拉刷新 
(ONLY_REFRESH == SwipeModel.SWIPE_ONLY_LOADINN) 只支持上拉加载 
(SWIPE_NONE == SwipeModel.SWIPE_NONE）都不支持
```
**note**  
```
  只有当包含的子View有足够内容进行独立滑动时 下拉加载才启动有效
  通过Xml app:fresh_model或者SwipeRefresh.setSwipeModel(SwipeControl.SwipeModel model) 定义刷新模式
  
```

Add Gradle dependency:
```gradle
dependencies {
  compile 'com.github.powyin:scroll:1.1.4'
}
```




