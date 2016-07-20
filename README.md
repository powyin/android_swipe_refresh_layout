
### compile 'com.github.powyin:scroll:1.0.9'

Hello,大家好，我是果冻虾仁。

# SwipeRefresh 支持单个View 的下拉刷新 
# SwipeNest 实现多个NestScrollChilder子类实现下拉刷新 
# MuilpAdapter 自带上啦加载

### SwipeRefresh 下拉刷新效果图
![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_pre.gif "github")  

      <com.powyin.scroll.widget.SwipeRefresh
        android:id = "@+id/re"
        android:layout_width="match_parent"
        android:layout_height="match_parent">
        <ListView
            android:id="@+id/my_list"
            android:background="#ffffffff"
            android:overScrollMode="never"
            android:dividerHeight="0dp"
            android:divider="#00000000"
            android:layout_width="match_parent"
            android:layout_height="match_parent"/>
        <!--或者是RecyclerView-->
        <!--<android.support.v7.widget.RecyclerView-->
            <!--android:background="#ffffff"-->
            <!--android:id="@+id/my_recycle"-->
            <!--android:overScrollMode="never"-->
            <!--android:layout_width="match_parent"-->
            <!--android:layout_height="match_parent"/>-->
    </com.powyin.scroll.widget.SwipeRefresh>
    
### MuilpAdapter 上拉加载更多 效果图
![github](https://github.com/powyin/nest-scroll/blob/master/app/src/main/res/raw/refresh_pre.gif "github")  








