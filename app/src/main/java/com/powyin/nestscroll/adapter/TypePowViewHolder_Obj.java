package com.powyin.nestscroll.adapter;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.powyin.nestscroll.R;
import com.powyin.scroll.adapter.MultipleListAdapter;
import com.powyin.scroll.adapter.MultipleRecycleAdapter;
import com.powyin.scroll.adapter.PowViewHolder;

import java.lang.reflect.Field;

/**
 * Created by powyin on 2016/7/27.
 */
public class TypePowViewHolder_Obj extends PowViewHolder<Object> {

    public TypePowViewHolder_Obj(Activity activity, ViewGroup viewGroup) {
        super(activity, viewGroup);
    }

    @Override
    protected int getItemViewRes() {
        return R.layout.recycler_view_holder_item;
    }

    @Override
    public void loadData(MultipleListAdapter<? super Object> multipleListAdapter, MultipleRecycleAdapter<? super Object> multipleRecycleAdapter, Object data) {

    }
}
