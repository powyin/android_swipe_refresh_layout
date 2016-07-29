package com.powyin.scroll.adapter;

import android.app.Activity;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.TextView;


import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by powyin on 2016/6/14.
 */
public class MultiAdapter<T> implements ListAdapter {

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T, N extends T> MultiAdapter<N> getByClass(Activity activity, Class<? extends ViewHolder<? extends T>>... cla) {
        Class<? extends ViewHolder>[] arrClass = new Class[cla.length + 1];
        System.arraycopy(cla, 0, arrClass, 0, cla.length);
        arrClass[arrClass.length - 1] = ErrorViewHolder.class;
        return new MultiAdapter<>(activity, arrClass);
    }

    private ViewHolder[] holderInstances;                                                                          // viewHolder 类实现实例
    private Class<? extends ViewHolder>[] holderClasses;                                                           // viewHolder class类
    private Class[] holderGenericDataClass;                                                                        // viewHolder 携带泛型

    private Activity mActivity;

    private List<T> mDataList = new ArrayList<>();

    private Map<T, ViewHolder> mDataToViewHolder = new HashMap<>();
    private final DataSetObservable mDataSetObservable = new DataSetObservable();


    public MultiAdapter(Activity activity, Class<? extends ViewHolder>[] cla) {
        this.mActivity = activity;

        holderClasses = cla;
        holderInstances = new ViewHolder[cla.length];
        holderGenericDataClass = new Class[cla.length];

        for (int i = 0; i < cla.length; i++) {
            Type genericType = null;                                                                                      // class类(泛型修饰信息)
            Class typeClass = holderClasses[i];                                                                                     // class类
            do {
                genericType = typeClass.getGenericSuperclass();
                typeClass = typeClass.getSuperclass();
            } while (typeClass != ViewHolder.class && typeClass != Object.class);

            if (typeClass != ViewHolder.class || genericType == ViewHolder.class) {
                throw new RuntimeException("参数类必须继承泛型ViewHolder");
            }
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type genericClass = paramType.getActualTypeArguments()[0];
            holderGenericDataClass[i] = (Class) genericClass;                                                         //赋值 泛型类型(泛型类持有)
            try {
                holderInstances[i] = holderClasses[i].getConstructor(Activity.class).newInstance(mActivity);         //赋值 holder实例
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("参数类必须实现（Activity）单一参数的构造方法");
            }
        }


    }


    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean isEnabled(int position) {
        if (position == mDataList.size()) return true;
        if (position == mDataList.size() + 1) return true;

        int index = getItemViewType(position);
        return holderInstances[index].isEnabled(mDataList.get(position));
    }


    @Override
    public int getCount() {
        return mDataList.size();
    }

    @Override
    public Object getItem(int position) {
        //   if (position == mDataList.size()) return mSwipeControl;
        return mDataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        //   if (position == mDataList.size()) return mSwipeControl.hashCode();
        return mDataList.get(position).hashCode();
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //    if (position == mDataList.size()) return mSwipeControl.getSwipeHead();

        ViewHolder holder;
        if (convertView == null) {
            int index = getItemViewType(position);
            try {
                holder = holderClasses[index].getConstructor(Activity.class).newInstance(mActivity);
                convertView = holder.mainView;
                convertView.setTag(holder);
            } catch (Exception e) {
                throw new RuntimeException("参数类必须实现（Activity）单一参数的构造方法");
            }
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        T itemData = mDataList.get(position);

        holder.mData = itemData;
        holder.loadData(this, itemData);

        if (itemData != null) {
            mDataToViewHolder.remove(itemData);
            mDataToViewHolder.put(itemData, holder);
        }
        return holder.mainView;
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getItemViewType(int position) {
        if (position == mDataList.size()) return holderInstances.length;          // 刷新页面
        for (int i = 0; i < holderInstances.length - 1; i++) {                    //返回能载入次数据的ViewHolderClass下标
            T itemData = mDataList.get(position);
            if (itemData != null && holderGenericDataClass[i].isAssignableFrom(itemData.getClass()) && holderInstances[i].acceptData(itemData)) {
                return i;
            }
        }
        return holderInstances.length - 1;                                        //错误页面数据
    }

    @Override
    public int getViewTypeCount() {
        return holderClasses.length + 1;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }


    //--------------------------------------------------------BaseAdapterImp------------------------------------------------------------//
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }

    //---------------------------------------------------------------数据设置------------------------------------------------------------//

    // 载入数据
    public void loadData(List<T> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        notifyDataSetChanged();
    }

    public void deleteFirst() {

        mDataList.remove(0);
        notifyDataSetChanged();
    }

    public void deleteLast() {

        mDataList.remove(mDataList.size() - 1);
        notifyDataSetChanged();
    }

    // 加入头部数据
    public void addFirst(T data) {
        mDataList.add(0, data);
        notifyDataSetChanged();
    }

    public void addFirst(List<T> datas) {
        mDataList.addAll(0, datas);
        notifyDataSetChanged();
    }

    // 加入尾部数据
    public void addLast(T data) {
        mDataList.add(mDataList.size(), data);
        notifyDataSetChanged();
    }

    public void addLast(List<T> dataList) {
        mDataList.addAll(mDataList.size(), dataList);
        notifyDataSetChanged();
    }

    // 更新data对应View的数据显示
    @SuppressWarnings("unchecked")
    public void notifyDataChange(T data) {
        ViewHolder holder = mDataToViewHolder.get(data);
        if (holder != null && holder.mData == data) {
            holder.loadData(this, data);
        }
    }

    // 删除数据
    public void deleteData(T data) {
        if (mDataList.contains(data)) {
            mDataList.remove(data);
            notifyDataSetChanged();
        }
    }


    // 不合法信息展示类
    private static class ErrorViewHolder extends ViewHolder<Object> {

        TextView errorInfo;

        public ErrorViewHolder(Activity activity) {
            super(activity);
        }

        @Override
        protected int getItemViewRes() {
            return 0;
        }

        @Override
        protected View getItemView() {
            FrameLayout frameLayout = new FrameLayout(mActivity);
            FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
            layoutParams.gravity = Gravity.CENTER;
            layoutParams.topMargin = 10;
            layoutParams.bottomMargin = 10;

            errorInfo = new TextView(mActivity);
            errorInfo.setMaxLines(2);
            RectShape shape = new RectShape();
            GradientDrawable gradientDrawabled = new GradientDrawable();

            gradientDrawabled.setShape(GradientDrawable.RECTANGLE);
            gradientDrawabled.setCornerRadius(13);
            gradientDrawabled.setStroke(1, 0x33000000);
            gradientDrawabled.setColor(0x11000000);
            gradientDrawabled.setBounds(20, 20, 20, 20);

            errorInfo.setBackgroundDrawable(gradientDrawabled);
            frameLayout.addView(errorInfo, layoutParams);
            return frameLayout;
        }

        @Override
        public void loadData(MultiAdapter<? super Object> adapter, Object data) {
            //   errorInfo.setText(data.getClass()+"\n"+data.toString());
        }

    }
}