package com.powyin.scroll.adapter;

import android.app.Activity;
import android.support.v4.view.PagerAdapter;

import android.view.View;
import android.view.ViewGroup;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by powyin on 2017/8/3.
 */

public class MultipleViewPageAdapter<T> extends PagerAdapter implements AdapterDelegate<T>{


    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> MultipleViewPageAdapter<T> getByViewHolder(Activity activity, Class<? extends PowViewHolder<? extends T>>... arrClass) {
        return new MultipleViewPageAdapter(activity, arrClass);
    }

    private PowViewHolder[] mHolderInstances;                                                                          // viewHolder 类实现实例
    private Class<? extends PowViewHolder>[] mHolderClasses;                                                           // viewHolder class类
    private Class[] mHolderGenericDataClass;                                                                           // viewHolder 携带泛型
    private Activity mActivity;

    private List<T> mDataList = new ArrayList<>();


    private boolean mEnnableChach = true;

    private PowViewHolder[] mCache = new PowViewHolder[100];








    @SuppressWarnings("unchecked")
    @SafeVarargs
    public MultipleViewPageAdapter(Activity activity, Class<? extends PowViewHolder<? extends T>>... viewHolderClass) {
        Class<? extends PowViewHolder>[] arrClass = new Class[viewHolderClass.length];
        System.arraycopy(viewHolderClass, 0, arrClass, 0, viewHolderClass.length);

        this.mActivity = activity;
        this.mHolderClasses = arrClass;
        this.mHolderInstances = new PowViewHolder[arrClass.length];
        this.mHolderGenericDataClass = new Class[arrClass.length];

        for (int i = 0; i < arrClass.length; i++) {
            Type genericType;                                                                                                                // class类(泛型修饰信息)
            Class typeClass = mHolderClasses[i];                                                                                             // class类
            do {
                genericType = typeClass.getGenericSuperclass();
                typeClass = typeClass.getSuperclass();
            } while (typeClass != PowViewHolder.class && typeClass != Object.class);

            if (typeClass != PowViewHolder.class || genericType == PowViewHolder.class) {
                throw new RuntimeException("参数类必须继承泛型ViewHolder");
            }
            ParameterizedType paramType = (ParameterizedType) genericType;
            Type genericClass = paramType.getActualTypeArguments()[0];
            mHolderGenericDataClass[i] = (Class) genericClass;                                                                               //赋值 泛型类型(泛型类持有)
        }


    }

    //----------------------------------------------------adapterImp----------------------------------------------------//

    @SuppressWarnings("unchecked")
    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        PowViewHolder<T> target = null;
        T itemData = mDataList.get(position);

        if(mCache[position] !=null){
            target = mCache[position];
            container.addView(target.mItemView);
            target.mData = itemData;
            target.onViewAttachedToWindow();
            return target;
        }


        for(int i= 0 ; i< mHolderInstances.length ; i++){
            if(itemData == null){
                throw new RuntimeException("data must not be Null");
            }
            if(!mHolderGenericDataClass[i].isAssignableFrom(itemData.getClass())){
                continue;
            }

            if(mHolderInstances[i] == null){
                try {
                    mHolderInstances[i] = mHolderClasses[i].getConstructor(Activity.class, ViewGroup.class).newInstance(mActivity, null);
                    mHolderInstances[i].mViewHolder = null;
                    //赋值 holder实例
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage());
                }
            }

            mHolderInstances[i].mPosition = position;
            if(mHolderInstances[i].acceptData(itemData)){
                target = mHolderInstances[i];
                mHolderInstances[i] = null;
                break;
            }
        }

        if(target == null){
            throw new RuntimeException("can not find holder to load the data");
        }



        if(mEnnableChach){
            mCache[position] = target;
        }
        container.addView(target.mItemView);
        target.mData = itemData;
        target.loadData(null,itemData,position);
        target.onViewAttachedToWindow();
        return target;
    }



    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        PowViewHolder target = (PowViewHolder)object;
        container.removeView(target.mItemView);
        target.onViewDetachedFromWindow();
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
    }




    @Override
    public float getPageWidth(int position) {
        return super.getPageWidth(position);
    }

    @Override
    public int getCount() {
        int size = mDataList.size();

        if(mCache.length < size){
            PowViewHolder[] rep  =  new PowViewHolder[size];
            System.arraycopy(mCache,0,rep,0,mCache.length);
            mCache = rep;
        }

        return size;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((PowViewHolder)object).mItemView;
    }


    //---------------------------------------------------------------AdapterDelegate------------------------------------------------------------//

    //---------------------------------------------------------------数据-----------------------------------------------------------------------//


    @Override
    public List<T> getDataList() {
        ArrayList<T> ret = new ArrayList<>();
        ret.addAll(mDataList);
        return ret;
    }

    @Override
    public int getDataCount() {
        return mDataList.size();
    }

    @Override
    public void loadData(List<T> dataList) {
        mDataList.clear();
        if(dataList!=null && dataList.size()>0){
            mDataList.addAll(dataList);
        }
        notifyDataSetChanged();
    }

    @Override
    public void addData(int position, T data) {

    }

    @Override
    public void addData(int position, List<T> dataList) {

    }

    @Override
    public void addDataAtLast(List<T> dataList) {

    }

    //---------------------------------------------------------------数据-----------------------------------------------------------------------//
    //---------------------------------------------------------------加载-----------------------------------------------------------------------//

    @Override
    public void addDataAtLast(List<T> dataList, LoadedStatus status, int delayTime) {

    }

    @Override
    public T removeData(int position) {
        return null;
    }

    @Override
    public void removeData(T data) {

    }

    @Override
    public void clearData() {

    }

    @Override
    public void enableLoadMore(boolean enable) {

    }

    @Override
    public void setLoadMoreStatus(LoadedStatus status) {

    }

    @Override
    public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {

    }

    @Override
    public void setHeadView(View view) {
        throw new RuntimeException("not Support");
    }

    @Override
    public void setFootView(View view) {
        throw new RuntimeException("not Support");
    }

    @Override
    public void removeHeadView() {
        throw new RuntimeException("not Support");
    }

    @Override
    public void removeFootView() {
        throw new RuntimeException("not Support");
    }

    @Override
    public void enableEmptyView(boolean show) {
        throw new RuntimeException("not Support");
    }

    @Override
    public void setEmptyView(View view) {
        throw new RuntimeException("not Support");
    }

    @Override
    public void setOnItemClickListener(OnItemClickListener<T> clickListener) {
        throw new RuntimeException("not Support");
    }

    @Override
    public void setOnItemLongClickListener(OnItemLongClickListener<T> clickListener) {
        throw new RuntimeException("not Support");
    }




    // --------------------------------------------------------------------unique---------------------------------------------------------//

    public void ennableChach(boolean ennable){
        this.mEnnableChach = ennable;
    }


    @SuppressWarnings("unchecked")
    public PowViewHolder<T> getPage(int index){
        return mCache[index];
    }

}
