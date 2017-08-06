package com.powyin.scroll.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.Field;

/**
 * Created by powyin on 2016/6/16.
 * 此类抽象出获取ListAdapter.Item 与Recycle.Adapter.Item的必须条件
 * 使用时：必须确定泛型类型
 */
public abstract class PowViewHolder<T> {
    RecycleViewHolder<T> mViewHolder;
    int mPosition = -1;


    //public final RecyclerView.ViewHolder holder;
    public final View mItemView;
    protected final Activity mActivity;
    public T mData;

    MultipleRecycleAdapter<T> mMultipleAdapter;
    MultipleListAdapter<T> mMultipleListAdapter;

    // todo if ==1 hasRegisterMainItemClick  if ==0 needTestRegisterMainItemClick  if ==-1 freeControl
    private int mRegisterMainItemClickStatus = 0;
    // todo if ==1 hasRegisterMainItemLongClick  if ==0 needTestRegisterMainItemLongClick  if ==-1 freeControl
    private int mRegisterMainItemLongClickStatus = 0;   //

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

    final void registerAutoItemLongClick() {
        if (mRegisterMainItemLongClickStatus != 0) return;
        if (getItemViewOnLongClickListener() == null) {
            mItemView.setOnLongClickListener(mOnLongClickListener);
            mRegisterMainItemLongClickStatus = +1;
        } else {
            mRegisterMainItemLongClickStatus = -1;
        }
    }

    private View.OnClickListener getItemViewOnClickListener() {
        if (mItemView.isClickable()) {
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

    private View.OnLongClickListener getItemViewOnLongClickListener() {
        if (mItemView.isLongClickable()) {
            try {
                Field mListenerInfo = View.class.getDeclaredField("mListenerInfo");
                mListenerInfo.setAccessible(true);
                Object infoObject = mListenerInfo.get(mItemView);

                if (infoObject == null) return null;

                Field clickListener = infoObject.getClass().getDeclaredField("mOnLongClickListener");
                Object onClickObject = clickListener.get(infoObject);

                if (onClickObject == null) return null;

                if (onClickObject instanceof View.OnLongClickListener) {
                    return (View.OnLongClickListener) onClickObject;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    private final View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mMultipleAdapter != null && mMultipleAdapter.mOnItemClickListener != null) {
                int index = mViewHolder!=null ? mViewHolder.getAdapterPosition() : mPosition;
                if (mMultipleAdapter.mHasHead) index--;
                AdapterDelegate.OnItemClickListener<T> onItemClickListener = mMultipleAdapter.mOnItemClickListener;
                if (index >= 0 && index < mMultipleAdapter.mDataList.size()) {
                    onItemClickListener.onClick(PowViewHolder.this, mData, index, v.getId());
                }
            }
            if (mMultipleListAdapter != null && mMultipleListAdapter.mOnItemClickListener != null) {
                int index = mViewHolder!=null ? mViewHolder.getAdapterPosition() : mPosition;
                if (mMultipleListAdapter.mHasHead) index--;
                AdapterDelegate.OnItemClickListener<T> onItemClickListener = mMultipleListAdapter.mOnItemClickListener;
                if (index >= 0 && index < mMultipleListAdapter.mDataList.size()) {
                    onItemClickListener.onClick(PowViewHolder.this, mData, index, v.getId());
                }
            }
        }
    };

    private final View.OnLongClickListener mOnLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mMultipleAdapter != null && mMultipleAdapter.mOnItemLongClickListener != null) {
                int index = mViewHolder!=null ? mViewHolder.getAdapterPosition() : mPosition;
                if (mMultipleAdapter.mHasHead) index--;
                AdapterDelegate.OnItemLongClickListener<T> onItemLongClickListener = mMultipleAdapter.mOnItemLongClickListener;
                if (index >= 0 && index < mMultipleAdapter.mDataList.size()) {
                    return onItemLongClickListener.onLongClick(PowViewHolder.this, mData, index, v.getId());
                }
            }

            if (mMultipleListAdapter != null && mMultipleListAdapter.mOnItemLongClickListener != null) {
                int index = mViewHolder!=null ? mViewHolder.getAdapterPosition() : mPosition;
                if (mMultipleListAdapter.mHasHead) index--;
                AdapterDelegate.OnItemLongClickListener<T> onItemLongClickListener = mMultipleListAdapter.mOnItemLongClickListener;
                if (index >= 0 && index < mMultipleListAdapter.mDataList.size()) {
                    return onItemLongClickListener.onLongClick(PowViewHolder.this, mData, index, v.getId());
                }
            }

            return false;
        }
    };

    protected final void registerItemClick(int... viewIds) {
        if (mRegisterMainItemClickStatus == 1 && mItemView.isClickable() && getItemViewOnClickListener() == mOnClickListener) {
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

    protected final void registerItemLongClick(int... viewIds) {
        if (mRegisterMainItemLongClickStatus == 1 && mItemView.isLongClickable() && getItemViewOnLongClickListener() == mOnLongClickListener) {
            mItemView.setOnLongClickListener(null);
        }
        mRegisterMainItemLongClickStatus = -1;

        for (int i = 0; viewIds != null && i < viewIds.length; i++) {
            View item = mItemView.findViewById(viewIds[i]);
            if (item != null) {
                item.setOnLongClickListener(mOnLongClickListener);
            }
        }
    }


    protected abstract int getItemViewRes();

    public abstract void loadData(AdapterDelegate<? super T> multipleAdapter, T data, int position);


    protected boolean acceptData(T data) {
        return true;
    }


    // 是否支持拖动
    protected boolean isEnableDragAndDrop() {
        return false;
    }

    public final int getItemPostion() {
        if(mViewHolder!=null){
            int position = mViewHolder.getAdapterPosition();
            return mMultipleAdapter.mHasHead ? position - 1 : position;
        }
        return mPosition;
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
