package com.powyin.nestscroll.adapter;

import android.app.Activity;
import android.view.ViewGroup;

import com.powyin.scroll.adapter.MultipleListAdapter;
import com.powyin.scroll.adapter.MultipleRecycleAdapter;
import com.powyin.scroll.adapter.PowViewHolder;

import java.util.Date;

/**
 * Created by powyin on 2016/7/31.
 */
public class TypePowViewHolder_Date extends PowViewHolder<Date> {

    public TypePowViewHolder_Date(Activity activity, ViewGroup viewGroup) {
        super(activity, viewGroup);
    }

    @Override
    protected int getItemViewRes() {
        return 0;
    }

    @Override
    public void loadData(MultipleListAdapter<? super Date> multipleListAdapter, MultipleRecycleAdapter<? super Date> multipleRecycleAdapter, Date data) {

    }
}
