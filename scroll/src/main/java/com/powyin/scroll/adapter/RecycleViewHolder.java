package com.powyin.scroll.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by powyin on 2017/6/28.
 */

class RecycleViewHolder<T> extends RecyclerView.ViewHolder {
    PowViewHolder<T> mPowViewHolder;
    RecycleViewHolder(View itemView, PowViewHolder<T> powViewHolder) {
        super(itemView);
        this.mPowViewHolder = powViewHolder;
    }


    // holder 依附
    void onViewAttachedToWindow() {
        if (this.mPowViewHolder != null) {
            this.mPowViewHolder.onViewAttachedToWindow();
        }
    }

    // holder 脱离
    void onViewDetachedFromWindow() {
        if (this.mPowViewHolder != null) {
            this.mPowViewHolder.onViewDetachedFromWindow();
        }
    }

}