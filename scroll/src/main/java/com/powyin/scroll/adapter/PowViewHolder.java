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
    final RecycleViewHolder<T> mViewHolder;

    //public final RecyclerView.ViewHolder holder;
    public final View mItemView;
    protected final Activity mActivity;

    public T mData;
    public MultipleRecycleAdapter<T> mMultipleAdapter;

    int mRegisterMainItemClickStatus = 0;   //  if ==1 hasRegisterMainItemClick  if ==0 needTestRegisterMainItemClick  if ==-1 freeControl

    public PowViewHolder(Activity activity, ViewGroup viewGroup) {
        this.mActivity = activity;
        if (getItemViewRes() == 0) {
            throw new RuntimeException("must provide View by getItemView() or gitItemViewRes()");
        }
        mItemView = activity.getLayoutInflater().inflate(getItemViewRes(), viewGroup, false);
        mViewHolder = new RecycleViewHolder<T>(mItemView, this);
    }

    final void registerAutoItemClick() {
        if (mRegisterMainItemClickStatus != 0) return;

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
            if (mMultipleAdapter != null) {
                mMultipleAdapter.invokeItemClick(PowViewHolder.this, mData, mViewHolder.getAdapterPosition(), v.getId());
            }
        }
    };


    protected final void registerItemClick(int... viewIds) {
        if (mRegisterMainItemClickStatus == 1 && mItemView.hasOnClickListeners() && getItemViewOnClickListener() == mOnClickListener) {
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


    protected boolean acceptData(T data) {
        return true;
    }


    protected void recycleData(){

    }

    // 是否支持拖动
    protected boolean isEnableDragAndDrop() {
        return false;
    }

    public final int getItemPostion() {
        int position = mViewHolder.getAdapterPosition();
        return mMultipleAdapter.mHasHead ? position-1 : position;
    }


    @SuppressWarnings("unchecked")
    public <K extends View> K findViewById(int resId) {
        return (K) mItemView.findViewById(resId);
    }

    // holder 依附
    protected void onViewAttachedToWindow() {

    }

    // holder 脱离
    protected void onViewDetachedFromWindow() {

    }


}
