package com.powyin.scroll.adapter;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;

/**
 * Created by powyin on 2016/6/16.
 * 此类抽象出获取ListAdapter.Item 与Recycle.Adapter.Item的必须条件
 * 使用时：必须确定泛型类型
 */
public abstract class PowViewHolder<T> {
    final RecycleViewHolder mViewHolder;

    protected final View mItemView;
    protected final Activity mActivity;

    protected T mData;
    protected AdapterDelegate<T> mMultipleAdapter;
    protected int mPosition;

    int mRegisterMainItemClickStatus = 0;   //  if ==1 hasRegisterMainItemClick  if ==0 needTestRegisterMainItemClick  if ==-1 freeControl

    public PowViewHolder(Activity activity, ViewGroup viewGroup) {
        this.mActivity = activity;
        View item = getItemView();
        if (item == null && getItemViewRes() == 0) {
            throw new RuntimeException("must provide View by getItemView() or gitItemViewRes()");
        }
        mItemView = item == null ? activity.getLayoutInflater().inflate(getItemViewRes(), viewGroup, false) : item;
        mViewHolder = new RecycleViewHolder<>(mItemView, this);
    }

    final void registerAutoItemClick() {
        if (getItemViewOnClickListener() == null) {
            mItemView.setOnClickListener(mOnClickListener);
            mRegisterMainItemClickStatus = +1;
        } else {
            mRegisterMainItemClickStatus = -1;
        }

    }


    private View.OnClickListener getItemViewOnClickListener() {
        if (mItemView.hasOnClickListeners()) {
            try {
                Field mListenerInfo = View.class.getDeclaredField("mListenerInfo");
                mListenerInfo.setAccessible(true);
                Object infoObject = mListenerInfo.get(mItemView);

                if (infoObject == null) return null;

                Field clickListener = infoObject.getClass().getDeclaredField("mOnClickListener");
                Object onClickObject = clickListener.get(infoObject);

                if (onClickObject == null) return null;

                System.out.println(" ..............            " + (onClickObject instanceof View.OnClickListener));

                if (onClickObject instanceof View.OnClickListener) {
                    return (View.OnClickListener) onClickObject;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @SuppressWarnings("unchecked")
        @Override
        public void onClick(View v) {
            if (mMultipleAdapter != null && mMultipleAdapter instanceof MultipleRecycleAdapter) {
                MultipleRecycleAdapter adapter = (MultipleRecycleAdapter) mMultipleAdapter;
                adapter.invokeItemClick(PowViewHolder.this,mPosition, v.getId());
            }
        }
    };


    protected final void registerItemClick(int... viewIds) {

        if (mRegisterMainItemClickStatus > 0 && mItemView.hasOnClickListeners() && getItemViewOnClickListener() == mOnClickListener) {
            mItemView.setOnClickListener(null);
        }

        mRegisterMainItemClickStatus = -1;

        for (int i = 0; viewIds != null && i < viewIds.length; i++) {
            View item = mItemView.findViewById(viewIds[i]);
            if (item != null) {
                item.setOnClickListener(mOnClickListener);
            }
        }
    }

    protected final void unRegisterItemClick(int... viewIds) {
        for (int i = 0; viewIds != null && i < viewIds.length; i++) {
            View item = mItemView.findViewById(viewIds[i]);
            if (item != null) {
                mItemView.findViewById(viewIds[i]).setOnClickListener(null);
            }
        }
    }

    protected abstract int getItemViewRes();

    public abstract void loadData(AdapterDelegate<? super T> multipleAdapter, T data, int position);

    protected View getItemView() {
        return null;
    }

    protected boolean acceptData(T data) {
        return true;
    }

    // holder 依附
    protected void onViewAttachedToWindow() {

    }

    // holder 脱离
    protected void onViewDetachedFromWindow() {

    }

    static class RecycleViewHolder<T> extends RecyclerView.ViewHolder {
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
}
