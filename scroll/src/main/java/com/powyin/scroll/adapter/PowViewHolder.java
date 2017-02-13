package com.powyin.scroll.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by powyin on 2016/6/16.
 * 此类抽象出获取ListAdapter.Item 与Recycle.Adapter.Item的必须条件
 * 使用时：必须确定泛型类型
 */
public abstract class PowViewHolder<T>{
    final RecycleViewHolder mViewHolder;

    protected final View mItemView;
    protected final Activity mActivity;
    protected T mData;



    public PowViewHolder(Activity activity , ViewGroup viewGroup) {
        this.mActivity = activity;
        View item = getItemView();
        if (item == null && getItemViewRes() == 0) {
            throw new RuntimeException("must provide View by getItemView() or gitItemViewRes()");
        }
        mItemView = item == null ? activity.getLayoutInflater().inflate(getItemViewRes(),viewGroup,false) : item;
        mViewHolder = new RecycleViewHolder<>(mItemView,this);

    }

    protected abstract int getItemViewRes();

    public abstract void loadData(AdapterDelegate<? super T> multipleAdapter, T data , int postion);

    protected View getItemView() {
        return null;
    }

    protected boolean acceptData(T data) {
        return true;
    }


    static class RecycleViewHolder<T> extends RecyclerView.ViewHolder {
        PowViewHolder<T> mPowViewHolder;
        RecycleViewHolder(View itemView, PowViewHolder<T> powViewHolder) {
            super(itemView);
            this.mPowViewHolder = powViewHolder;
        }

    }
}
