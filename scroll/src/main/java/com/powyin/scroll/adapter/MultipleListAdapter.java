package com.powyin.scroll.adapter;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.database.DataSetObservable;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;

import android.support.v7.widget.RecyclerView;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.powyin.scroll.R;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by powyin on 2016/6/14.
 */
public class MultipleListAdapter<T> implements ListAdapter, AdapterDelegate<T> {

    // 空白页面
    private final int ITYPE_Empty = 0x0;
    // 错误页面
    private final int ITYPE_ERROR = 0x1;
    // 加载更多
    private final int ITYPE_LOAD = 0x2;
    // 头部
    private final int ITYPE_HEAD = 0x3;
    // 尾部
    private final int ITYPE_FOOT = 0x4;


    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> MultipleListAdapter<T> getByViewHolder(Activity activity, Class<? extends PowViewHolder<? extends T>>... arrClass) {
        return new MultipleListAdapter(activity, arrClass);
    }


    private PowViewHolder[] mHolderInstances;                                                                          // viewHolder 类实现实例
    private Class<? extends PowViewHolder>[] mHolderClasses;                                                           // viewHolder class类
    private Class[] mHolderGenericDataClass;                                                                           // viewHolder 携带泛型
    private Activity mActivity;

    List<T> mDataList = new ArrayList<>();

    // 上拉加载实现
    private LoadedStatus mLoadStatus;
    private String mLoadCompleteInfo = "我是有底线的";
    private String mLoadErrorInfo = "加载失败";
    private OnLoadMoreListener mOnLoadMoreListener;                                                                    // 显示更多监听

    OnItemClickListener<T> mOnItemClickListener;
    OnItemLongClickListener<T> mOnItemLongClickListener;

    private IncludeTypeLoad mLoad;

    private View mSpaceView;
    private View mHeadView;
    private View mFootView;

    private boolean mSpaceEnable = false;
    boolean mHasHead = false;
    private boolean mHasFoot = false;
    private boolean mHasLoad = false;                     // 是否展示加载更多


    private final DataSetObservable mDataSetObservable = new DataSetObservable();

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public MultipleListAdapter(Activity activity, Class<? extends PowViewHolder<? extends T>>... viewHolderClass) {

        Class<? extends PowViewHolder>[] arrClass = new Class[viewHolderClass.length];
        System.arraycopy(viewHolderClass, 0, arrClass, 0, viewHolderClass.length);

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
                mHolderInstances[i] = mHolderClasses[i].getConstructor(Activity.class, ViewGroup.class).newInstance(mActivity, null);         //赋值 holder实例
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("参数类必须实现（Activity）单一参数的构造方法  或者  " + e.getMessage());
            }
        }

    }

    //----------------------------------------------------adapterImp----------------------------------------------------//

    @Override
    public boolean areAllItemsEnabled() {
        return true;
    }

    @Override
    public boolean isEnabled(int position) {
        return true;
    }


    @Override
    public int getCount() {
        return mSpaceEnable ? 1 : mDataList.size() + (mHasHead ? 1 : 0) + (mHasFoot ? 1 : 0) + (mHasLoad ? 1 : 0);
    }

    @Override
    public Object getItem(int position) {
        if (mSpaceEnable) {
            return ITYPE_Empty;
        }

        if (mHasHead) {
            if (position == 0) {
                return ITYPE_HEAD;
            }
            position--;
        }

        if (position < mDataList.size()) {
            return mDataList.get(position);
        } else {
            position = position - mDataList.size();
            if (mHasFoot) {
                if (position == 0) {
                    return ITYPE_FOOT;
                }
            }
            return ITYPE_LOAD;
        }

    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        int type = getItemViewType(position);
        if (convertView == null) {
            switch (type) {
                case ITYPE_Empty:
                    RecyclerView.ViewHolder empty;
                    FrameLayout contain = getSpaceContain(parent);
                    empty = new IncludeTypeEmpty(contain);
                    empty.itemView.setTag(empty);
                    convertView = empty.itemView;
                    break;
                case ITYPE_ERROR:
                    RecyclerView.ViewHolder error = new IncludeTypeError(parent);
                    error.itemView.setTag(error);
                    convertView = error.itemView;
                    break;
                case ITYPE_LOAD:
                    RecyclerView.ViewHolder load = new IncludeTypeLoad(parent);
                    load.itemView.setTag(load);
                    convertView = load.itemView;
                    break;
                case ITYPE_HEAD:
                    RecyclerView.ViewHolder head = new IncludeTypeHead(parent);
                    head.itemView.setTag(head);
                    convertView = head.itemView;
                    break;
                case ITYPE_FOOT:
                    RecyclerView.ViewHolder foot = new IncludeTypeFoot(parent);
                    foot.itemView.setTag(foot);
                    convertView = foot.itemView;
                    break;
                default:
                    PowViewHolder holder;
                    try {
                        holder = mHolderClasses[type - 5].getConstructor(Activity.class, ViewGroup.class).newInstance(mActivity, parent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new RuntimeException(e.getMessage());
                    }
                    holder.mItemView.setTag(holder);
                    convertView = holder.mItemView;
            }
        }

        switch (type) {
            case ITYPE_Empty:
                IncludeTypeEmpty empty = (IncludeTypeEmpty) convertView.getTag();
                empty.loadView();
                break;
            case ITYPE_ERROR:
                position = mHasHead ? position - 1 : position;
                IncludeTypeError error = (IncludeTypeError) convertView.getTag();
                error.loadData(mDataList.get(position));
                break;
            case ITYPE_LOAD:
                IncludeTypeLoad load = (IncludeTypeLoad) convertView.getTag();
                mLoad = load;
                load.progressBar.ensureAnimation(false);
                load.ensureLoading();
                break;
            case ITYPE_HEAD:
                IncludeTypeHead head = (IncludeTypeHead) convertView.getTag();
                head.loadView();
                break;
            case ITYPE_FOOT:
                IncludeTypeFoot foot = (IncludeTypeFoot) convertView.getTag();
                foot.loadView();
                break;
            default:
                position = mHasHead ? position - 1 : position;
                T itemData = position < mDataList.size() ? mDataList.get(position) : null;
                PowViewHolder<T> powViewHolder = (PowViewHolder<T>) convertView.getTag();

                powViewHolder.mData = itemData;
                powViewHolder.mMultipleListAdapter = this;

                if (mOnItemClickListener != null) {
                    powViewHolder.registerAutoItemClick();
                }
                if (mOnItemLongClickListener != null) {
                    powViewHolder.registerAutoItemLongClick();
                }

                powViewHolder.loadData(this, itemData, position);
        }


        return convertView;
    }

    @Override
    public long getItemId(int position) {
        if (mSpaceEnable) {
            return ITYPE_Empty;
        }

        if (mHasHead) {
            if (position == 0) {
                return ITYPE_HEAD;
            }
            position--;
        }

        if (position < mDataList.size()) {
            T data = mDataList.get(position);
            return data == null ? 0 : data.hashCode();
        } else {
            position = position - mDataList.size();
            if (mHasFoot) {
                if (position == 0) {
                    return ITYPE_FOOT;
                }
            }
            return ITYPE_LOAD;
        }

    }

    @SuppressWarnings("unchecked")
    @Override
    public int getItemViewType(int position) {

        if (mSpaceEnable) {
            return ITYPE_Empty;
        }

        if (mHasHead) {
            if (position == 0) return ITYPE_HEAD;
            position--;
        }

        if (position < mDataList.size()) {
            for (int i = 0; i < mHolderInstances.length; i++) {                        //返回能载入次数据的ViewHolderClass下标
                T itemData = mDataList.get(position);
                if (itemData != null && mHolderGenericDataClass[i].isAssignableFrom(itemData.getClass()) && mHolderInstances[i].acceptData(itemData)) {
                    return i + 5;
                }
            }
            return ITYPE_ERROR;
        }

        position -= mDataList.size();
        if (mHasFoot) {
            if (position == 0) return ITYPE_FOOT;
            position--;
        }

        if (position == 0 && mHasLoad) return ITYPE_LOAD;

        throw new RuntimeException(" what happen ");

    }


    @Override
    public int getViewTypeCount() {
        return mHolderClasses.length + 5;
    }


    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.registerObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
        mDataSetObservable.unregisterObserver(observer);
    }

    public void notifyDataSetChanged() {
        mDataSetObservable.notifyChanged();
    }

    public void notifyDataSetInvalidated() {
        mDataSetObservable.notifyInvalidated();
    }

    //---------------------------------------------------------------AdapterDelegate------------------------------------------------------------//


    //---------------------------------------------------------------数据-----------------------------------------------------------------------//
    // 获取数据
    @Override
    public List<T> getDataList() {
        ArrayList<T> arrayList = new ArrayList<T>();
        arrayList.addAll(mDataList);
        return arrayList;
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
        notifyDataSetChanged();
    }


    // 添加数据
    @Override
    public void addData(int position, T data) {
        if (data == null) return;
        mDataList.add(position, data);
        notifyDataSetChanged();
    }

    // 添加数据
    @Override
    public void addData(int position, List<T> dataList) {
        if (dataList == null) return;
        mDataList.addAll(position, dataList);
        notifyDataSetChanged();
    }

    @Override
    public void addDataAtLast(List<T> dataList) {
        addDataAtLast(dataList, null, 0);
    }

    // 加入尾部数据     delayTime 延迟加入 让上拉加载显示时间加长
    @Override
    public void addDataAtLast(final List<T> dataList, final LoadedStatus status, int delayTime) {
        if (delayTime <= 0) {
            mDataList.addAll(mDataList.size(), dataList);
            notifyDataSetChanged();
            setLoadMoreStatus(status);
        } else {
            mActivity.getWindow().getDecorView().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDataList.addAll(mDataList.size(), dataList);
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
        notifyDataSetChanged();
        return ret;
    }


    // 删除数据
    @Override
    public void removeData(T data) {
        int index = mDataList.indexOf(data);
        if (index >= 0) {
            removeData(index);
        }
    }


    // 清空数据
    @Override
    public void clearData() {
        if (mDataList.size() != 0) {
            mDataList.clear();
            notifyDataSetChanged();
        }
    }
    //---------------------------------------------------------------数据-----------------------------------------------------------------------//


    //---------------------------------------------------------------加载-----------------------------------------------------------------------//
    // 设置是否显示加载更多
    @Override
    public void enableLoadMore(boolean show) {
        if (this.mHasLoad != show) {
            this.mHasLoad = show;
            notifyDataSetChanged();
        }
    }

    @Override
    public void setLoadMoreStatus(LoadedStatus status) {
        if (status == null) return;
        switch (status) {
            case NO_MORE:
                mLoadStatus = LoadedStatus.NO_MORE;
        }

        if (mLoad != null) {
            mLoad.progressBar.invalidate();
        }
    }


    // 手动调用加载更多
    @Override
    public void loadMore() {

    }

    // 清除上拉加载中状态
    @Override
    public void completeLoadMore() {

    }

    // 设置显示更多监听
    @Override
    public void setOnLoadMoreListener(OnLoadMoreListener loadMoreListener) {
        this.mOnLoadMoreListener = loadMoreListener;
    }

    //---------------------------------------------------------------加载-----------------------------------------------------------------------//

    // 设置ViewHolder 点击监听
    @Override
    public void setOnItemClickListener(OnItemClickListener<T> clickListener) {
        this.mOnItemClickListener = clickListener;
    }

    // 设置ViewHolder 长按点击
    @Override
    public void setOnItemLongClickListener(OnItemLongClickListener<T> longClickListener) {
        this.mOnItemLongClickListener = longClickListener;
    }


    //---------------------------------------------------------------AdapterDelegate------------------------------------------------------------//


    @Override
    public void setHeadView(View view) {
        if (view == null) return;
        mHasHead = true;
        mHeadView = view;
        notifyDataSetChanged();
    }

    @Override
    public void removeHeadView() {
        if (!mHasHead) return;
        mHasHead = false;
        mHeadView = null;
        notifyDataSetChanged();
    }

    @Override
    public void setFootView(View view) {
        if (view == null) return;
        mHasFoot = true;
        mFootView = view;
        notifyDataSetChanged();
    }


    @Override
    public void removeFootView() {
        if (!mHasFoot) return;
        mHasFoot = false;
        mFootView = null;
        notifyDataSetChanged();
    }

    @Override
    public void enableEmptyView(boolean show) {
        if (mSpaceEnable != show) {
            mSpaceEnable = show;
            notifyDataSetChanged();
        }
    }

    @Override
    public void setEmptyView(View view) {
        if (view == null) return;
        if (mSpaceView != view) {
            mSpaceView = view;
            notifyDataSetChanged();
        }
    }


    // 0x110 空白页面
    private class IncludeTypeEmpty extends RecycleViewHolder<Object> {
        FrameLayout mainView;

        IncludeTypeEmpty(FrameLayout viewGroup) {
            super(viewGroup, null);
            mainView = viewGroup;
        }

        void loadView() {
            if (mSpaceView != null) {
                ViewParent parent = mSpaceView.getParent();
                if (parent != null) {
                    ((ViewGroup) parent).removeView(mSpaceView);
                }
                mainView.removeAllViews();
                mainView.addView(mSpaceView, new FrameLayout.LayoutParams(-1, -1));
            }
        }
    }

    private FrameLayout getSpaceContain(ViewGroup viewGroup) {
        FrameLayout frameLayout = new FrameLayout(viewGroup.getContext()) {
            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(heightMeasureSpec), MeasureSpec.EXACTLY);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        };
        TextView textView = new TextView(viewGroup.getContext());
        textView.setText("space");
        textView.setTextColor(0xff000000);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(-2, -2);
        layoutParams.gravity = Gravity.CENTER;
        frameLayout.addView(textView, layoutParams);
        ListView.LayoutParams params = new ListView.LayoutParams(-1, -1);
        frameLayout.setLayoutParams(params);
        return frameLayout;
    }

    // 0x111 错误页面
    private class IncludeTypeError extends RecycleViewHolder<Object> {
        TextView errorInfo;

        IncludeTypeError(ViewGroup viewGroup) {
            super(mActivity.getLayoutInflater().inflate(R.layout.powyin_scroll_multiple_adapter_err, viewGroup, false), null);
            errorInfo = (TextView) super.itemView.findViewById(R.id.powyin_scroll_err_text);
        }

        private void loadData(T data) {
            errorInfo.setText(data == null ? " you has one empty data inside " : data.toString());
        }

    }

    // 0x112 加载更多
    @SuppressWarnings("unchecked")
    private class IncludeTypeLoad extends RecycleViewHolder<Object> {
        LoadProgressBar progressBar;

        IncludeTypeLoad(ViewGroup viewGroup) {
            super(new LoadProgressBar(mActivity), null);
            progressBar = (LoadProgressBar) itemView;
        }

        void ensureLoading() {
            if (mLoadStatus == null && mOnLoadMoreListener != null) {
                mLoadStatus = null;
                mOnLoadMoreListener.onLoadMore();
            }
        }
    }

    // 0x113 头部
    private class IncludeTypeHead extends RecycleViewHolder<Object> {
        FrameLayout frameLayout;

        IncludeTypeHead(ViewGroup viewGroup) {
            super(mActivity.getLayoutInflater().inflate(R.layout.powyin_scroll_multiple_adapter_head, viewGroup, false), null);
            frameLayout = (FrameLayout) itemView;
        }

        void loadView() {
            if (mHeadView != null) {
                ViewParent parent = mHeadView.getParent();
                if (parent != null) {
                    ((ViewGroup) parent).removeView(mHeadView);
                }
                frameLayout.removeAllViews();
                frameLayout.addView(mHeadView, new FrameLayout.LayoutParams(-1, -2));
            }
        }

    }

    // 0x114 尾部
    private class IncludeTypeFoot extends RecycleViewHolder<Object> {
        FrameLayout frameLayout;

        IncludeTypeFoot(ViewGroup viewGroup) {
            super(mActivity.getLayoutInflater().inflate(R.layout.powyin_scroll_multiple_adapter_foot, viewGroup, false), null);
            frameLayout = (FrameLayout) itemView;
        }

        void loadView() {
            if (mFootView != null) {
                ViewParent parent = mFootView.getParent();
                if (parent != null) {
                    ((ViewGroup) parent).removeView(mFootView);
                }
                frameLayout.removeAllViews();
                frameLayout.addView(mFootView, new FrameLayout.LayoutParams(-1, -2));
            }
        }

    }


    // 加载中
    class LoadProgressBar extends View {
        boolean mAttached = false;
        ValueAnimator animator;
        Paint circlePaint;
        TextPaint textPaint;
        int canvasWei;
        int canvasHei;
        float canvasTextX;
        float canvasTextY;
        int ballCount = 10;
        float divide;

        private long beginShowTime = System.currentTimeMillis();

        public LoadProgressBar(Context context) {
            super(context);

            circlePaint = new Paint();
            circlePaint.setColor(0x99000000);
            circlePaint.setStrokeWidth(4);
            textPaint = new TextPaint();
            textPaint.setColor(0x99000000);

            final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
            int target = (int) (13 * fontScale + 0.5f);

            textPaint.setTextSize(target);
            textPaint.setAntiAlias(true);
            textPaint.setStrokeWidth(1);
        }

        private void ensureAnimation(boolean forceReStart) {

            if (!mAttached || mLoadStatus == LoadedStatus.NO_MORE) {
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
            animator.setRepeatCount(-1);

            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    divide = 8 * ((System.currentTimeMillis() % 3000) - 1500) / 3000f;
                    invalidate();
                }
            });


            animator.start();
        }


        private void ensureStopAnimation() {
            if (animator != null) {
                animator.cancel();
                animator = null;
            }
        }


        private float getSplit(float value) {
            int positive = value >= 0 ? 1 : -1;                                 //保存符号 判断正负
            value = Math.abs(value);
            if (value <= 1) return value * positive;
            return (float) Math.pow(value, 2) * positive;
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
            ensureStopAnimation();
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

            if (beginShowTime == -1) {
                return;
            }

            int timeBe = (int) (System.currentTimeMillis() - beginShowTime);

            float alpha = timeBe / 2000f;
            alpha = alpha > 1 ? 1 : alpha;
            alpha = alpha < 0 ? 1 : alpha;

            int colorAlpha = (int) (alpha * 200);

            textPaint.setAlpha(colorAlpha);
            circlePaint.setAlpha(colorAlpha);

            if (mLoadStatus == LoadedStatus.NO_MORE) {
                canvas.drawText(mLoadCompleteInfo, canvasTextX, canvasTextY, textPaint);
                canvas.drawLine(20, canvasHei / 2, canvasTextX - 20, canvasHei / 2, textPaint);
                canvas.drawLine(canvasWei - canvasTextX + 20, canvasHei / 2, canvasWei - 20, canvasHei / 2, textPaint);

            }
            if (mLoadStatus == LoadedStatus.ERROR) {
                canvas.drawText(mLoadErrorInfo, canvasTextX, canvasTextY, textPaint);
                canvas.drawLine(20, canvasHei / 2, canvasTextX - 20, canvasHei / 2, textPaint);
                canvas.drawLine(canvasWei - canvasTextX + 20, canvasHei / 2, canvasWei - 20, canvasHei / 2, textPaint);
            } else {
                for (int i = 0; i < ballCount; i++) {
                    float wei = 4 * (1f * i / ballCount - 0.5f) + divide;
                    wei = canvasWei / 2 + getSplit(wei) * canvasWei * 0.08f;
                    canvas.drawCircle(wei, canvasHei / 2 + 6, 8, circlePaint);
                }
            }
        }


        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            super.onLayout(changed, left, top, right, bottom);
            canvasHei = getHeight();
            canvasWei = getWidth();

            canvasTextX = canvasWei / 2 - textPaint.measureText(mLoadCompleteInfo) / 2;
            canvasTextY = canvasHei / 2 + textPaint.getTextSize() / 2.55f;

            if (bottom < ((ViewGroup) getParent()).getHeight()) {
                beginShowTime = -1;
            } else {
                beginShowTime = System.currentTimeMillis();
            }
        }
    }


}