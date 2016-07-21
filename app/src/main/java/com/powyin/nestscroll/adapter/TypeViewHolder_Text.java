package com.powyin.nestscroll.adapter;

import android.app.Activity;

import com.powyin.nestscroll.R;
import com.powyin.nestscroll.net.DataModel;
import com.powyin.scroll.adapter.MultiAdapter;
import com.powyin.scroll.adapter.ViewHolder;

/**
 * Created by powyin on 2016/7/21.
 */
public class TypeViewHolder_Text extends ViewHolder<DataModel> {
    public TypeViewHolder_Text(Activity activity) {
        super(activity);
    }
    @Override
    protected int getItemViewRes() {
        return R.layout.view_holder_type_text;
    }

    @Override
    protected boolean acceptData(DataModel data) {
        return data.type==1;
    }

    @Override
    public void loadData(MultiAdapter<? super DataModel> adapter, DataModel data) {

    }
}
