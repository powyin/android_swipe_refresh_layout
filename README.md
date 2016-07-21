Add Gradle dependency:
```gradle
dependencies {
  compile 'com.github.powyin:scroll:1.1.1'
}
```






# SwipeRefresh 支持单个View 的下拉刷新以及上拉加载
# SwipeNest 实现多个NestScrollChilder子类实现下拉刷新 
# MuilpAdapter 快速实现多种类型展示

### SwipeRefresh 下拉刷新效果图
![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_pre.gif "github")  

# How to use

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
    
#### Xml 设置支持4种模式 或者 SwipeRefresh.setSwipeModel(SwipeControl.SwipeModel model) 设置支持模式
(BOTH) 同时支持下拉刷新与上拉加载  (ONLY_REFRESH) 只支持下拉刷新 
(ONLY_REFRESH) 只支持上拉加载 （SWIPE_NONE）都不支持
#### note : 只有当包含的子View有足够内容进行独立滑动时 下拉加载才启动有效
### MuilpAdapter 上拉加载更多 效果图








