


# SwipeRefresh 支持单个View 的下拉刷新以及上拉加载
# SwipeNest 垂直布局多个NestScrollChilder 实现下拉刷新 
# MuilpAdapter 快速实现 ListView 多种类型展示 

### SwipeRefresh UI

|刷新|下拉加载获取新数据|上拉加载数据全部获得|
|---|---|----
|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_pre.gif)|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_load_2.gif)|![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_load_1.gif)|



### How to use

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




