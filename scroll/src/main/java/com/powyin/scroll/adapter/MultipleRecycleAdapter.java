package com.powyin.scroll.adapter;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.TextPaint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Space;
import android.widget.TextView;

import com.powyin.scroll.R;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by powyin on 2016/7/30.
 */
public class MultipleRecycleAdapter<T> extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements AdapterDelegate<T> {

    // 0 空白页面；
    // 1 错误页面；
    // 2 加载更多；

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T, N extends T> MultipleRecycleAdapter<N> getByViewHolder(Activity activity, Class<? extends PowViewHolder<? extends T>>... arrClass) {
        return new MultipleRecycleAdapter(activity, arrClass);
    }


    private PowViewHolder[] mHolderInstances;                                                                          // viewHolder 类实现实例
    private Class<? extends PowViewHolder>[] mHolderClasses;                                                           // viewHolder class类
    private Class[] mHolderGenericDataClass;                                                                           // viewHolder 携带泛型
    private Activity mActivity;
    private boolean isMovingEnable = false;                                                                            // 是否支持拖拽
    private List<T> mDataList = new ArrayList<>();
    private boolean mShowError = true;                                                                                 // 是否展示错误信息
    private RecyclerView mRecyclerView;
    private int mSpaceCount;
    private int mLoadMoreCount;


    // 上拉加载实现
    private boolean mLoadEnableShow = false;                                                                              // 是否展示加载更多
    private LoadStatus mLoadStatus = LoadStatus.CONTINUE;
    private LoadMorePowViewHolder loadMorePowViewHolder;
    private String mLoadCompleteInfo = "我是有底线的";
    private OnLoadMoreListener mOnLoadMoreListener;                                                                       // 显示更多监听
    private OnItemClickListener<T> mOnItemClickListener;


    @SuppressWarnings("unchecked")
    @SafeVarargs
    public MultipleRecycleAdapter(Activity activity, Class<? extends PowViewHolder<? extends T>>... viewHolderClass) {
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
            try {
                mHolderInstances[i] = mHolderClasses[i].getConstructor(Activity.class, ViewGroup.class).newInstance(mActivity, null);         //赋值 holder实例
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }
        }

        for (PowViewHolder<T> holder : mHolderInstances) {
            isMovingEnable |= holder.isEnableDragAndDrop();
            if (isMovingEnable) break;
        }

        setHasStableIds(true);

    }

    //----------------------------------------------------adapterImp----------------------------------------------------//

    @SuppressWarnings("unchecked")
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        if (viewType == mHolderClasses.length + 2) {
            if (loadMorePowViewHolder == null) {
                loadMorePowViewHolder = new LoadMorePowViewHolder();
            }
            return loadMorePowViewHolder.getNewInstance();
        } else if (viewType == mHolderClasses.length + 1) {
            return new IncludeTypeEmpty(parent);
        } else if (viewType == mHolderClasses.length) {
            return new IncludeTypeError(parent);
        } else {
            PowViewHolder holder;
            try {
                holder = mHolderClasses[viewType].getConstructor(Activity.class, ViewGroup.class).newInstance(mActivity, parent);
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e.getMessage());
            }

            return holder.mViewHolder;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        T itemData = position < mDataList.size() ? mDataList.get(position) : null;
        PowViewHolder<T> powViewHolder = ((RecycleViewHolder) holder).mPowViewHolder;

        if (position >= mDataList.size()) {
            if (position >= mDataList.size() + mSpaceCount) {
                ((LoadMorePowViewHolder.LoadProgressBar) holder.itemView).mIndex = position - (mDataList.size() + mSpaceCount);
            }

            if (mLoadStatus == LoadStatus.CONTINUE && mOnLoadMoreListener != null) {
                mLoadStatus = null;
                mOnLoadMoreListener.onLoadMore();
            }

        } else {
            if (powViewHolder != null) {
                powViewHolder.mData = itemData;
                powViewHolder.mMultipleAdapter = this;
                powViewHolder.mPosition = position;

                if (mOnItemClickListener != null && powViewHolder.mRegisterMainItemClickStatus == 0) {
                    powViewHolder.registerAutoItemClick();
                }
                powViewHolder.loadData(this, itemData, position);
            }
        }
    }


    private ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            PowViewHolder powViewHolder = ((RecycleViewHolder) viewHolder).mPowViewHolder;
            if (powViewHolder != null && powViewHolder.isEnableDragAndDrop()) {
                return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT, 0);
            }
            return 0;
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            PowViewHolder oriViewHolder = ((RecycleViewHolder) viewHolder).mPowViewHolder;
            PowViewHolder tarViewHolder = ((RecycleViewHolder) target).mPowViewHolder;
            if (oriViewHolder == null || tarViewHolder == null ||
                    !oriViewHolder.isEnableDragAndDrop() || !tarViewHolder.isEnableDragAndDrop())
                return false;
            int ori = viewHolder.getAdapterPosition();
            int tar = target.getAdapterPosition();
            mDataList.set(ori, mDataList.set(tar, mDataList.get(ori)));
            notifyItemMoved(ori, tar);
            return true;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int index = viewHolder.getAdapterPosition();
            mDataList.remove(index);
            notifyItemRemoved(index);
        }
    });


    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
        if (isMovingEnable) {
            itemTouchHelper.attachToRecyclerView(mRecyclerView);
        }
    }

    @Override
    public int getItemCount() {
        return mDataList.size() + mSpaceCount + mLoadMoreCount;
    }

    // holder 依附
    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        ((RecycleViewHolder) holder).onViewAttachedToWindow();
    }

    // holder 脱离
    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        ((RecycleViewHolder) holder).onViewDetachedFromWindow();
    }

    @Override
    public long getItemId(int position) {
        T date = position < mDataList.size() ? mDataList.get(position) : null;
        return date == null ? 0 : date.hashCode();
    }

    private void ensureConfig() {
        if (!mLoadEnableShow || mRecyclerView == null) {
            mSpaceCount = 0;
            mLoadMoreCount = 0;
        } else {
            RecyclerView.LayoutManager layoutManager = mRecyclerView.getLayoutManager();
            if (layoutManager instanceof GridLayoutManager) {
                GridLayoutManager gridLayoutManager = (GridLayoutManager) layoutManager;
                int spanCount = gridLayoutManager.getSpanCount();
                int dataSize = mDataList.size();

                mSpaceCount = dataSize % spanCount == 0 ? 0 : spanCount - dataSize % spanCount;
                mLoadMoreCount = spanCount;
            } else if (layoutManager instanceof LinearLayoutManager) {
                mSpaceCount = 0;
                mLoadMoreCount = 1;
            } else if (layoutManager instanceof StaggeredGridLayoutManager) {
                // 待 完善 ；
                mSpaceCount = 0;
                mLoadMoreCount = 1;
            } else {
                mSpaceCount = 0;
                mLoadMoreCount = 1;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public int getItemViewType(int position) {
        if (position >= mDataList.size()) {


            if (position >= mDataList.size() + mSpaceCount) {
                return mHolderInstances.length + 2;
            } else {
                return mHolderInstances.length + 1;
            }
        }

        for (int i = 0; i < mHolderInstances.length; i++) {                        //返回能载入次数据的ViewHolderClass下标
            T itemData = mDataList.get(position);
            if (itemData != null && mHolderGenericDataClass[i].isAssignableFrom(itemData.getClass()) && mHolderInstances[i].acceptData(itemData)) {
                return i;
            }
        }

        return mHolderInstances.length;                                              //错误页面数据
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        mRecyclerView = null;
    }

    final void invokeItemClick(PowViewHolder<T> powViewHolder, int index, int resId) {
        if (mOnItemClickListener != null && index >= 0 && index < mDataList.size()) {
            mOnItemClickListener.onClick(powViewHolder, mDataList.get(index), index, resId);
        }
    }


    //---------------------------------------------------------------AdapterDelegate------------------------------------------------------------//

    // 获取数据
    @Override
    public List<T> getDataList() {
        return mDataList;
    }

    @Override
    public int getDataCount() {
        return mDataList.size();
    }

    // 载入数据
    @Override
    public void loadData(List<T> dataList) {
        mDataList.clear();
        mDataList.addAll(dataList);
        ensureConfig();
        notifyDataSetChanged();
    }


    // 添加数据
    @Override
    public void addData(int position, T data) {
        if (data == null) return;
        mDataList.add(position, data);
        ensureConfig();
        notifyDataSetChanged();
    }

    // 添加数据
    @Override
    public void addData(int position, List<T> dataList) {
        if(dataList==null) return;
        mDataList.addAll(position,dataList);
        ensureConfig();
        notifyDataSetChanged();
    }

    // 加入尾部数据     delayTime 延迟加入 让上拉加载显示时间加长
    @Override
    public void addDataAtLast(final List<T> dataList, final LoadStatus status, int delayTime) {
        if (delayTime <= 10) {
            mDataList.addAll(mDataList.size(), dataList);
            ensureConfig();
            notifyDataSetChanged();
            setLoadMoreStatus(status);
        } else {
            mActivity.getWindow().getDecorView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDataList.addAll(mDataList.size(), dataList);
                    ensureConfig();
                    notifyDataSetChanged();
                    setLoadMoreStatus(status);
                }
            }, delayTime);
        }
    }

    // 删除数据
    @Override
    public T removeData(int position) {
        T ret = mDataList.remove(position);
        ensureConfig();
        notifyDataSetChanged();
        return ret;
    }


    // 删除数据
    @Override
    public void removeData(T data) {
        mDataList.remove(data);
        ensureConfig();
        notifyDataSetChanged();
    }



    // 清空数据
    @Override
    public void clearData() {
        if (mDataList.size() != 0) {
            mDataList.clear();
            ensureConfig();
            notifyDataSetChanged();
        }
    }

    // 设置是否展示不合法数据；
    @Override
    public void setShowErrorHolder(boolean show) {
        if (mShowError != show) {
            mShowError = show;
            ensureConfig();
            notifyDataSetChanged();
        }
    }

    // 设置是否显示加载更多
    @Override
    public void setShowLoadMore(boolean show) {
        if (this.mLoadEnableShow != show) {
            this.mLoadEnableShow = show;
            ensureConfig();
            notifyDataSetChanged();
        }
    }

    @Override
    public void setLoadMoreStatus(LoadStatus status) {
        if (status == null) return;

        switch (status) {
            case CONTINUE:
                mLoadStatus = LoadStatus.CONTINUE;
                break;
            case COMPLITE:
                mLoadStatus = LoadStatus.COMPLITE;
        }

        if (loadMorePowViewHolder != null) {
            loadMorePowViewHolder.ensureAnimation(false);
            for (RecyclerView.ViewHolder holder : loadMorePowViewHolder.viewHolders) {
                holder.itemView.invalidate();
            }
        }
    }

    // 设置显示更多监听
    @Override
    public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.mOnLoadMoreListener = loadMoreListener;
    }

    // 设置ViewHolder 点击监听


    @Override
    public void setOnItemClickListener(OnItemClickListener<T> clickListener) {
        this.mOnItemClickListener = clickListener;
    }

    // 0 空白页面
    private class IncludeTypeEmpty extends RecycleViewHolder<Object> {
        IncludeTypeEmpty(ViewGroup viewGroup) {
            super(new Space(mActivity), null);
        }
    }

    // 1 不合法信息展示类
    private class IncludeTypeError extends RecycleViewHolder<Object> {
        TextView errorInfo;

        IncludeTypeError(ViewGroup viewGroup) {
            super(mActivity.getLayoutInflater().inflate(R.layout.powyin_scroll_multiple_adapter_err, viewGroup, false), null);
            errorInfo = (TextView) super.itemView.findViewById(R.id.powyin_scroll_err_text);
        }
    }

    // 2 加载更多iew
    private class LoadMorePowViewHolder {

        boolean mAttached = false;

        List<RecyclerView.ViewHolder> viewHolders = new ArrayList<>();

        ValueAnimator animator;
        Paint circlePaint;
        TextPaint textPaint;
        int canvasWei;
        int canvasHei;

        float canvasTextX;
        float canvasTextY;

        int ballCount = 10;
        float divide;

        LoadMorePowViewHolder() {
            circlePaint = new Paint();
            circlePaint.setColor(0x99000000);
            circlePaint.setStrokeWidth(4);
            textPaint = new TextPaint();
            textPaint.setColor(0x99000000);

            final float fontScale = mActivity.getResources().getDisplayMetrics().scaledDensity;
            int target = (int) (13 * fontScale + 0.5f);

            textPaint.setTextSize(target);
            textPaint.setAntiAlias(true);
            textPaint.setStrokeWidth(1);
        }


        RecycleViewHolder getNewInstance() {
            RecycleViewHolder holder = new RecycleViewHolder<Object>(new LoadProgressBar(mActivity), null);
            viewHolders.add(holder);
            return holder;
        }


        private void ensureAnimation(boolean forceReStart) {

            if (!mAttached || mLoadStatus == LoadStatus.COMPLITE) {
                if (animator != null) {
                    animator.cancel();
                    animator = null;
                }
                return;
            }

            if (forceReStart) {
                if (animator != null) {
                    animator.cancel();
                    animator = null;
                }
            } else {
                if (animator != null && animator.isRunning()) {
                    return;
                }
            }

            animator = ValueAnimator.ofFloat(0, 1);
            animator.setDuration(2000);
            animator.setRepeatCount(5);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    divide = 8 * ((System.currentTimeMillis() % 3000) - 1500) / 3000f;
                    for (RecyclerView.ViewHolder holder : viewHolders) {
                        holder.itemView.invalidate();
                    }
                }
            });

            animator.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (animation == animator) {
                        ensureAnimation(true);
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }
            });

            animator.start();
        }

        private float getSplit(float value) {
            int positive = value >= 0 ? 1 : -1;                                 //保存符号 判断正负
            value = Math.abs(value);
            if (value <= 1) return value * positive;
            return (float) Math.pow(value, 2) * positive;
        }

        class LoadProgressBar extends View {              //刷新视图
            int mIndex;

            public LoadProgressBar(Context context) {
                super(context);
            }

            @Override
            protected void onAttachedToWindow() {
                super.onAttachedToWindow();
                mAttached = true;
                ensureAnimation(false);
            }

            @Override
            protected void onDetachedFromWindow() {
                super.onDetachedFromWindow();
                mAttached = false;
                ensureAnimation(false);
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);

                float scale = getContext().getResources().getDisplayMetrics().density;
                int target = (int) (40 * scale + 0.5f);

                setMeasuredDimension(getMeasuredWidth(), target);
            }


            @Override
            protected void onDraw(Canvas canvas) {
                super.onDraw(canvas);
                if (mLoadStatus == LoadStatus.COMPLITE) {
                    int diff = mIndex * getWidth();
                    canvas.drawText(mLoadCompleteInfo, canvasTextX - diff, canvasTextY, textPaint);
                    canvas.drawLine(20 - diff, canvasHei / 2, canvasTextX - 20 - diff, canvasHei / 2, textPaint);
                    canvas.drawLine(canvasWei - canvasTextX + 20 - diff, canvasHei / 2, canvasWei - 20 - diff, canvasHei / 2, textPaint);

                } else {
                    for (int i = 0; i < ballCount; i++) {
                        float wei = 4 * (1f * i / ballCount - 0.5f) + divide;
                        wei = canvasWei / 2 + getSplit(wei) * canvasWei * 0.08f;
                        wei -= mIndex * getWidth();                               // good
                        canvas.drawCircle(wei, canvasHei / 2, 8, circlePaint);
                    }
                }
            }

            @Override
            protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
                super.onLayout(changed, left, top, right, bottom);
                canvasHei = getHeight();
                canvasWei = getWidth() * mLoadMoreCount;

                canvasTextX = canvasWei / 2 - textPaint.measureText(mLoadCompleteInfo) / 2;
                canvasTextY = canvasHei / 2 + textPaint.getTextSize() / 2.55f;

                ensureAnimation(false);
            }
        }

    }


}
