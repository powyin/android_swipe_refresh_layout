package com.powyin.nestscroll.adapter;

import android.app.Activity;
import android.view.ViewGroup;

import com.powyin.nestscroll.R;
import com.powyin.scroll.adapter.AdapterDelegate;
import com.powyin.scroll.adapter.MultipleListAdapter;
import com.powyin.scroll.adapter.MultipleRecycleAdapter;
import com.powyin.scroll.adapter.PowViewHolder;

/**
 * Created by powyin on 2016/7/31.
 */
public class TypePowViewHolder_Str extends PowViewHolder<String> {

    public TypePowViewHolder_Str(Activity activity, ViewGroup viewGroup) {
        super(activity, viewGroup);
    }


    @Override
    public void loadData(AdapterDelegate<? super String> multipleAdapter, String data, int postion) {

    }

    @Override
    protected int getItemViewRes() {
        return R.layout.view_holder_type_pic_1;
    }
}








