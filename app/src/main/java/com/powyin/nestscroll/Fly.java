package com.powyin.nestscroll;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.support.v4.view.animation.PathInterpolatorCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.powyin.nestscroll.powyinScroll.HeaderLayout;


/**
 * Created by MT3020 on 2016/3/10.
 */
public class Fly extends HeaderLayout {

 //   private AnimatorSet mFlyAnimator = null;
    private OnPullRefreshListener mListener;

    public Fly(Context context) {
        super(context);
        init(context,null);
    }

    public Fly(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context,attrs);
    }

    public Fly(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context,attrs);
    }

    private void init(Context context,AttributeSet attrs) {
//        MountanScenceView headerView = new MountanScenceView(getContext());
//        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, mHeaderController.getMaxHeight());
//        setHeaderView(headerView, lp);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }


    public void startRefresh() {

//        if (mFlyAnimator != null) {
//            mFlyAnimator.end();
//        }
//
//        final View iconView = getIconView();
//        UIUtils.clearAnimator(iconView);
//
//        AnimatorSet flyUpAnim = new AnimatorSet();
//        flyUpAnim.setDuration(800);
//
//        ObjectAnimator transX = ObjectAnimator.ofFloat(iconView, "translationX", 0, getWidth());
//        ObjectAnimator transY = ObjectAnimator.ofFloat(iconView, "translationY", 0, -mHeaderController.getHeight());
//        transY.setInterpolator(PathInterpolatorCompat.create(0.7f, 1f));
//        ObjectAnimator rotation = ObjectAnimator.ofFloat(iconView, "rotation", -45, 0);
//        rotation.setInterpolator(new DecelerateInterpolator());
//        ObjectAnimator rotationX = ObjectAnimator.ofFloat(iconView, "rotationX", 0, 60);
//        rotationX.setInterpolator(new DecelerateInterpolator());
//
//        flyUpAnim.playTogether(transX, transY, rotationX,
//                ObjectAnimator.ofFloat(iconView, "scaleX", 1, 0.5f),
//                ObjectAnimator.ofFloat(iconView, "scaleY", 1, 0.5f),
//                rotation
//        );
//
//        mFlyAnimator = flyUpAnim;
//        mFlyAnimator.start();

        if (mListener != null) {
            mListener.onRefresh(Fly.this);
        }
    }

    public void setOnPullRefreshListener(OnPullRefreshListener listener) {
        mListener = listener;
    }



    public interface OnPullRefreshListener {
        void onRefresh(Fly view);
        void onRefreshAnimationEnd(Fly view);
    }
}
