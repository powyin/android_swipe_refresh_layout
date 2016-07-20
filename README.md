
# FlyRefresh
The Android implementation of [Replace](https://dribbble.com/shots/2067564-Replace), designed by [Zee Youn](https://dribbble.com/zeeyoung).
I implement this as a **FlyRefresh** layout. The content of the layout can be any `NestedScrollingChild`, such as a RecyclerView, NestedScrollView, VerticalGridView, etc.
This library can also work with `NestedScrollingParent` as parent, such as CoordinatorLayout.

# How it looks
![flyrefresh](./images/flyrefresh.gif)

# Features
* Work with all [NestedScrollingParent](https://developer.android.com/reference/android/support/v4/view/NestedScrollingParent.html) and [NestedScrollingChild](https://developer.android.com/reference/android/support/v4/view/NestedScrollingChild.html)
* Default minimize configuration for [Replace](https://dribbble.com/shots/2067564-Replace) animation
* Expendable/Shrinkable header
* Support custom header view
* Support custom refresh animation

# How to use

Add Gradle dependency:

```gradle
dependencies {
   compile 'com.race604.flyrefresh:library:2.0.0'
}
```

An example of basic usage in `layout.xml`:

```xml
<com.race604.flyrefresh.FlyRefreshLayout
  android:id="@+id/fly_layout"
  android:layout_width="match_parent"
  android:layout_height="match_parent">

    <android.support.v7.widget.RecyclerView
      android:id="@+id/list"
      android:layout_width="match_parent"
      android:layout_height="match_parent"
      android:paddingTop="24dp"
      android:background="#FFFFFF"/>
</com.race604.flyrefresh.FlyRefreshLayout>
```

Or you can use `PullHeaderLayout` for more configurations, you can set custom attributes as shown below:

```xml
<declare-styleable name="PullHeaderLayout">
    <!-- hader size -->
    <attr name="phl_header_height" format="dimension" />
    <attr name="phl_header_expand_height" format="dimension" />
    <attr name="phl_header_shrink_height" format="dimension" />
    <!-- header view id -->
    <attr name="phl_header" format="reference" />
    <!-- content view id -->
    <attr name="phl_content" format="reference" />
    <!-- Float action button icon -->
    <attr name="phl_action" format="reference" />
</declare-styleable>
```
For more, please turn to the source code.

# License
`FlyRefresh` is available under the MIT license.


compile 'com.github.powyin:scroll\:1.0.9'

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








