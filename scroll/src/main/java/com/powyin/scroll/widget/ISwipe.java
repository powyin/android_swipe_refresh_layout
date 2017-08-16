package com.powyin.scroll.widget;

/**
 * Created by powyin on 2017/4/17.  用于控制刷新结果
 */

public interface ISwipe {

    // todo UI开始下拉刷新后  必须调用 setFreshResult 结束下拉刷新
    // todo UI开始上拉加载后  必须调用completeLoadMore  或者  setLoadMoreResult 结束上拉加载

    enum LoadedStatus {
        ERROR,                                                          //上拉加载失败
        NO_MORE                                                         //数据全部加载完毕
    }

    enum FreshStatus {
        ERROR,                                                          //下拉刷新失败
        ERROR_NET,                                                      //下拉刷新失败 网络异常
        SUCCESS                                                         //下拉刷新成功 普通业务只需要使用这个
    }

    interface OnRefreshListener {
        //TODO  下拉刷新开始
        void onRefresh();
        //TODO 上拉加载开始
        void onLoading();
    }

    // 设置刷新控制监听
    void setOnRefreshListener(OnRefreshListener onRefreshListener);

    // 开始刷新
    void refresh();

    // 结束刷新 设置下拉刷新结果
    void setFreshResult(FreshStatus statue);

    // 结束加载 清除上拉加载中状态
    void completeLoadMore();

    // 结束加载 设置上拉加载结果
    void setLoadMoreResult(LoadedStatus status);

    // 设置刷新模式
    void setSwipeModel(SwipeController.SwipeModel model);

    // 设置自定义刷新视图
    void setSwipeController(SwipeController controller);

    // 设置空白页面控制器
    void setEmptyController(EmptyController controller);

    // 启动空白页面显示
    void enableEmptyView(boolean show);

    // 是否支持上拉加载过度拉升
    void enableLoadMoreOverScroll(boolean enable);

}
