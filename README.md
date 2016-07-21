Add Gradle dependency:
```gradle
dependencies {
  compile 'com.github.powyin:scroll:1.1.1'
}
```


# SwipeRefresh 支持单个View 的下拉刷新以及上拉加载
# SwipeNest 实现多个NestScrollChilder子类实现下拉刷新 
# MuilpAdapter 快速实现 ListView 多种类型快速展示 



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
    
**设置支持4种模式**  </tag></tag> Xml app:fresh_model</tag></tag>SwipeRefresh.setSwipeModel(SwipeControl.SwipeModel model)

```
       (BOTH = SwipeModel.SWIPE_BOTH) 同时支持下拉刷新与上拉加载  
       (ONLY_REFRESH == SwipeModel.SWIPE_ONLY_REFRESH)) 只支持下拉刷新 
       (ONLY_REFRESH == SwipeModel.SWIPE_ONLY_LOADINN) 只支持上拉加载 
       (SWIPE_NONE == SwipeModel.SWIPE_NONE）都不支持
```
**note**   </tag></tag>  只有当包含的子View有足够内容进行独立滑动时 下拉加载才启动有效


### MuilpAdapter   ListView 多种类型快速展示 

### SwipeRefresh 下拉刷新效果图
![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_pre.gif "github")  








