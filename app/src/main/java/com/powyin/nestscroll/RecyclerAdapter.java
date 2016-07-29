package com.powyin.nestscroll;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by powyin on 2016/7/27.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder> {

    private Context mContext;

    public RecyclerAdapter(Context context){
        this.mContext = context;
    }
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder();
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        TextView textView = (TextView) holder.itemView.findViewById(R.id.my_id);
        textView.setText("this is for you 托尔斯泰"+position);
    }

    @Override
    public int getItemCount() {
        return 40;
    }

    class Holder extends RecyclerView.ViewHolder {
        public Holder() {
            super(LayoutInflater.from(mContext).inflate(R.layout.recycler_view_holder_item,null));
        }
    }

}
