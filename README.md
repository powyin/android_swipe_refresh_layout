


### SwipeRefresh 支持单个View 的下拉刷新以及上拉加载
### SwipeNest 滚动视图（支持各种 普通View recycleView scrollView listView等 混合排列） 处理滚动冲突  附带下拉刷新以及上拉加载
### MultipleListAdapter<T> 简单快速实现 ListView 多种类型展示
### MultipleRecycleAdapter<T>  简单快速实现  RecycleView多种类型展示 

Add Gradle dependency:
```gradle
dependencies {
      compile 'com.github.powyin:scroll:3.2.3'
      compile 'com.android.support:recyclerview-v7:24.0.0'
}
```

### SwipeRefresh UI

|刷新（可自定义）|下拉加载获取新数据(可自定义)|上拉加载数据全部获得(可自定义)|
|---|---|----
|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_pre.gif)|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_load_2.gif)|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_load_1.gif)|


### SwipeNest UI

|刷新（可自定义）|平滑各种View之间的滚动冲突|自定义刷新范例|
|---|---|----
|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/nest_pre.gif)|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/nest_pre_1.gif)|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/nest_pre_2.gif)|

### how to use  SwipeRefresh

```
    <com.powyin.scroll.widget.SwipeRefresh>
        <!--ListView-->
        <android.support.v7.widget.RecyclerView/>
    </com.powyin.scroll.widget.SwipeRefresh>
```
    
### how to use  SwipeNest 

```
    <com.powyin.scroll.widget.SwipeNest>
        <FrameLayout>
            <ImageView />
        </FrameLayout>
        <android.support.v7.widget.RecyclerView/>
        <ImageView />
    </com.powyin.scroll.widget.SwipeNest>
```

### 设置刷新监听与刷新结果处理

ISwipe

        ISwipe.setOnRefreshListener(new SwipeRefresh.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // 开始下拉刷新
            }

            @Override
            public void onLoading(boolen isLoadViewShow) {
                // 开始加载更多
            }
        });
        
        ISwipe.setFreshStatue(ISwip.RreshStatus.SUCCESS);             //下拉刷新 完成
        ISwipe.setFreshStatue(ISwipe.RreshStatus.ERROR);              //下拉刷新 失败
 
        
        
### SwipeRefresh设置刷新模式

```
ISwipe.setSwipeModel(SwipeControl.SwipeModel model)          /
(BOTH = SwipeModel.SWIPE_BOTH)                    同时支持下拉刷新与上拉加载  
(ONLY_REFRESH == SwipeModel.SWIPE_ONLY_REFRESH))  只支持下拉刷新 
(ONLY_REFRESH == SwipeModel.SWIPE_ONLY_LOADINN)   只支持上拉加载 
(SWIPE_NONE == SwipeModel.SWIPE_NONE）            都不支持
```


### MultipleRecycleAdapter&MultipleListAdapter&MultipleViewPageAdapter
        
PowViewHolder<T>    此类抽象出获取ListAdapter.Item 与Recycle.Adapter.Item的必须条件；使用时：必须确定泛型类型
AdapterDelegate<T>  此接口定义了 ListAdapter 与 RecycleView.Adatper 与 PagerAdapter 公共数据操作；
        
        
