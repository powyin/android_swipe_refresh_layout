package com.powyin.scroll.adapter;

import android.app.Activity;
import android.database.DataSetObservable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.powyin.scroll.R;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by powyin on 2016/7/30.
 */
public class MultipleRecycleAdapter<T>  extends RecyclerView.Adapter<PowViewHolder.RecycleViewHolder> implements AdapterDelegate<T>{


    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T, N extends T> MultipleListAdapter<N> getByViewHolder(Activity activity, Class<? extends PowViewHolder<? extends T>>... arrClass) {
        return new MultipleListAdapter(activity, arrClass);
    }

    private PowViewHolder[] mHolderInstances;                                                                          // viewHolder 类实现实例
    private Class<? extends PowViewHolder>[] mHolderClasses;                                                           // viewHolder class类
    private Class[] mHolderGenericDataClass;                                                                           // viewHolder 携带泛型
    private Activity mActivity;
    private List<T> mDataList = new ArrayList<>();
    private boolean mShowError = true;                                                                                 // 是否展示错误信息
    private Map<T, PowViewHolder> mDataToViewHolder = new HashMap<>();
    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public  MultipleRecycleAdapter(Activity activity, Class<? extends PowViewHolder<? extends T >>... viewHolderClass) {

        Class<? extends PowViewHolder>[] arrClass = new Class[viewHolderClass.length + 1];
        System.arraycopy(viewHolderClass, 0, arrClass, 0, viewHolderClass.length);
        arrClass[arrClass.length - 1] = ErrorPowViewHolder.class;

        this.mActivity = activity;
        this.mHolderClasses = arrClass;
        this.mHolderInstances = new PowViewHolder[arrClass.length];
        this.mHolderGenericDataClass = new Class[arrClass.length];

        for (int i = 0; i < arrClass.length; i++) {
            Type genericType;                                                                                          // class类(泛型修饰信息)
            Class typeClass = mHolderClasses[i];                                                                       // class类
            do {
                genericType = typeClass.getGenericSuperclass();
                typeClass = typeClass.getSuperclass();
            } while (typeClass != PowViewHolder.class && typeClass != Object.class);

            if (typeClass != PowViewHolder.class || genericType == PowViewHolder.class) {
                throw new RuntimeException("参数类必须继承泛型ViewHolder");
            }
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type genericClass = paramType.getActualTypeArguments()[0];
            mHolderGenericDataClass[i] = (Class) genericClass;                                                         //赋值 泛型类型(泛型类持有)
            try {
                mHolderInstances[i] = mHolderClasses[i].getConstructor(Activity.class,ViewGroup.class).newInstance(mActivity,null);         //赋值 holder实例
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("参数类必须实现（Activity）单一参数的构造方法  或者 ImageView 载入图片尺寸过大 或者 " + e.getMessage());
            }
        }
    }

    //----------------------------------------------------adapterImp----------------------------------------------------//

    @SuppressWarnings("unchecked")
    @Override
    public PowViewHolder.RecycleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        PowViewHolder holder;
        try {
            holder = mHolderClasses[viewType].getConstructor(Activity.class,ViewGroup.class).newInstance(mActivity,parent);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("参数类必须实现（Activity）单一参数的构造方法  或者 ImageView 载入图片尺寸过大 或者 " + e.getMessage());
        }
        return holder.mViewHolder;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(PowViewHolder.RecycleViewHolder holder, int position) {
        T itemData = mDataList.get(position);
        PowViewHolder<T> powViewHolder = holder.mPowViewHolder;

        powViewHolder.mData = itemData;
        powViewHolder.loadData(null, this, itemData);

        if (itemData != null) {
            mDataToViewHolder.remove(itemData);
            mDataToViewHolder.put(itemData, powViewHolder);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size();
    }


    @Override
    public long getItemId(int position) {
        T date = mDataList.get(position);
        return date==null? 0 : date.hashCode();
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getItemViewType(int position) {
        if (position == mDataList.size()) return mHolderInstances.length;          // 刷新页面
        for (int i = 0; i < mHolderInstances.length - 1; i++) {                    //返回能载入次数据的ViewHolderClass下标
            T itemData = mDataList.get(position);
            if (itemData != null && mHolderGenericDataClass[i].isAssignableFrom(itemData.getClass()) && mHolderInstances[i].acceptData(itemData)) {
                return i;
            }
        }
        return mHolderInstances.length - 1;                                        //错误页面数据
    }


    //---------------------------------------------------------------AdapterDelegate------------------------------------------------------------//

    // 载入数据
    @Override
    public void loadData(List<T> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    @Override
    public void deleteFirst() {
        mDataList.remove(0);
        notifyDataSetChanged();
    }

    @Override
    public void deleteLast() {
        mDataList.remove(mDataList.size() - 1);
        notifyDataSetChanged();
    }

    // 加入头部数据
    @Override
    public void addFirst(T data) {
        mDataList.add(0, data);
        notifyDataSetChanged();
    }

    @Override
    public void addFirst(List<T> datas) {
        mDataList.addAll(0, datas);
        notifyDataSetChanged();
    }

    // 加入尾部数据
    @Override
    public void addLast(T data) {
        mDataList.add(mDataList.size(), data);
        notifyDataSetChanged();
    }

    @Override
    public void addLast(List<T> dataList) {
        mDataList.addAll(mDataList.size(), dataList);
        notifyDataSetChanged();
    }

    // 更新data对应View的数据显示
    @SuppressWarnings("unchecked")
    @Override
    public void notifyDataChange(T data) {
        PowViewHolder holder = mDataToViewHolder.get(data);
        if (holder != null && holder.mData == data) {
            holder.loadData(null, this, data);
        }
    }

    // 删除数据
    @Override
    public void deleteData(T data) {
        if (mDataList.contains(data)) {
            mDataList.remove(data);
            notifyDataSetChanged();
        }
    }

    // 设置是否展示不合法数据；
    @Override
    public void setShowErrorHolder(boolean show) {
        if (mShowError != show) {
            mShowError = show;
            notifyDataSetChanged();
        }
    }



    // 不合法信息展示类
    private static class ErrorPowViewHolder extends PowViewHolder<Object> {

        TextView errorInfo;

        public ErrorPowViewHolder(Activity activity, ViewGroup viewGroup) {
            super(activity,viewGroup);
            errorInfo = (TextView) mViewHolder.itemView.findViewById(R.id.powyin_scroll_err_text);

        }


        @Override
        public void loadData(MultipleListAdapter<? super Object> multipleListAdapter, MultipleRecycleAdapter<? super Object> multipleRecycleAdapter,  Object data) {
            if (multipleRecycleAdapter.mShowError) {
                errorInfo.setVisibility(View.VISIBLE);
                errorInfo.setText(data == null ? "null" : data.toString());
            } else {
                errorInfo.setVisibility(View.GONE);
            }
        }

        @Override
        protected int getItemViewRes() {
            return R.layout.powyin_scroll_multiple_adapter_err;
        }

        @Override
        protected boolean acceptData(Object data) {
            return true;
        }


    }
}
