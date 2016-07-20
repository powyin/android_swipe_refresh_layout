package com.powyin.nestscroll.adapter;

import android.app.Activity;

import com.powyin.nestscroll.R;
import com.powyin.nestscroll.net.DataModel;
import com.powyin.scroll.adapter.base.MultiAdapter;
import com.powyin.scroll.adapter.base.ViewHolder;

import java.util.Date;

/**
 * Created by powyin on 2016/7/21.
 */
public class TypeViewHolder_Pic_1 extends ViewHolder<DataModel> {
    public TypeViewHolder_Pic_1(Activity activity) {
        super(activity);
    }

    @Override
    protected int getItemViewRes() {
        return R.layout.view_holder_type_pic_1;
    }

    @Override
    protected boolean acceptData(DataModel data) {
        return data.type==2;
    }

    @Override
    public void loadData(MultiAdapter<? super DataModel> adapter, DataModel data) {

    }
}
