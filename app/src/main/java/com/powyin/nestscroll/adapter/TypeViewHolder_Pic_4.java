package com.powyin.nestscroll.adapter;

import android.app.Activity;

import com.powyin.nestscroll.R;
import com.powyin.nestscroll.net.DataModel;
import com.powyin.scroll.adapter.MultiAdapter;
import com.powyin.scroll.adapter.ViewHolder;

public class TypeViewHolder_Pic_4 extends ViewHolder<DataModel> {
    public TypeViewHolder_Pic_4(Activity activity) {
        super(activity);
    }

    @Override
    protected int getItemViewRes() {
        return R.layout.view_holder_type_pic_4;
    }

    @Override
    protected boolean acceptData(DataModel data) {
        return data.type==3;
    }

    @Override
    public void loadData(MultiAdapter<? super DataModel> adapter, DataModel data) {

    }
}
