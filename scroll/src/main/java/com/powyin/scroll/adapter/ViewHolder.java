package com.powyin.scroll.adapter;

import android.app.Activity;
import android.view.View;

import com.powyin.scroll.adapter.MultiAdapter;

/**
 * Created by powyin on 2016/6/16.
 */
public abstract class ViewHolder<T> {
    protected final View mainView;
    protected final Activity mActivity;
    protected T mData;

    public ViewHolder(Activity activity) {
        this.mActivity = activity;
        View itemView = getItemView();
        if (itemView == null && getItemViewRes() == 0) {
            throw new RuntimeException("must provide View by getItemView() or gitItemViewRes()");
        }
        this.mainView = itemView == null ? activity.getLayoutInflater().inflate(getItemViewRes(), null) : itemView;
    }

    protected abstract int getItemViewRes();

    public abstract void loadData(MultiAdapter<? super T> adapter, T data);

    protected View getItemView() {
        return null;
    }

    protected boolean acceptData(T data) {
        return true;
    }

    protected boolean isEnabled(T data) {
        return true;
    }
}
