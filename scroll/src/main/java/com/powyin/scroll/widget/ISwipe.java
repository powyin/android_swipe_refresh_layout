package com.powyin.scroll.widget;

/**
 * Created by powyin on 2017/4/17.  用于控制刷新结果
 */

public interface ISwipe {

    enum LoadedStatus {
        CONTINUE,                                                       //上拉加载成功 继续上拉可以获取更多数据
        ERROR,                                                          //上拉加载失败
        NO_MORE                                                         //数据全部加载完毕
    }

    enum FreshStatus {
        CONTINUE,                                                       //重置下拉刷新 可以继续触发 （ERROR_NET 会使下拉刷新不再触发）
        ERROR,                                                          //下拉刷新失败
        ERROR_NET,                                                      //下拉刷新失败 网络异常
        SUCCESS                                                         //下拉刷新成功 普通业务只需要使用这个
    }




    // 设置刷新控制监听
    void setOnRefreshListener(OnRefreshListener onRefreshListener);


    // 设置下拉刷新结果
    void setFreshStatue(FreshStatus statue);

    // 设置上拉加载结果
    void setLoadMoreStatus(LoadedStatus status);


    // 设置刷新模式
    public void setSwipeModel(SwipeController.SwipeModel model);

    // 设置自定义刷新视图
    public void setSwipeController(SwipeController controller);

    // 设置空白页面控制器
    public void setEmptyController(EmptyController controller);

    public void setShowEmptyView(boolean show);


    public interface OnRefreshListener {
        //TODO  刷新开始
        void onRefresh();
        //TODO  isLoadViewShow  上拉进度条是否正在显示  不考虑UI和谐的话 可以直接不管它
        void onLoading();
    }

    @Deprecated
    public interface OnStatusListener {
        @Deprecated
        void onFreshStatue(FreshStatus status);
    }


}
