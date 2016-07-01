package com.powyin.nestscroll.powyinScroll.edge;

import android.graphics.Canvas;
import android.support.annotation.NonNull;
import android.view.View;

import com.powyin.nestscroll.powyinScroll.HeaderLayout;

import java.security.PublicKey;

import javax.xml.transform.sax.TemplatesHandler;

/**
 * Created by MT3020 on 2016/3/10.
 */
public abstract class EdgeController {


    protected int mOrgHei;
    protected int mShowHeight;
    protected int mExpandHeight;

    protected int mScroll = 0;


    public EdgeController( ) {

    }

    public void setSize(int orgHei, int showHeight, int expandHeight) {

        this.mOrgHei = orgHei;
        this.mShowHeight = showHeight;
        this.mExpandHeight = expandHeight;

        mScroll = 0;
    }


    public abstract void onPullProgress(Canvas canvas);




    public int getExpandHeight() {
        return mExpandHeight;
    }


    public int getShowHeight() {
        return mShowHeight;
    }

    public int getOrgHeight(){
        return mOrgHei;
    }

    public int getScroll() {
        return (int) mScroll;
    }

    public void setScroll(int scroll){
        this.mScroll = scroll;
    }


    public boolean isOverScroll(){
        if(checkLegalLeft(mScroll)){
            return mScroll<mShowHeight;
        }
        if(checkLegalRight(mScroll)){
            return mScroll>mShowHeight;
        }
        return false;
    }

    public boolean canScroll(){
        return checkLegalLeft(mScroll) || checkLegalRight(mScroll);
    }


    private boolean checkLegalRight (int willTo){
        return ( mOrgHei<= willTo  && willTo <= mExpandHeight );
    }
    private boolean checkLegalLeft (int willTo){
        return ( mExpandHeight<= willTo  && willTo <= mOrgHei );
    }

    public int move(int deltaY) {

        int willTo = deltaY+mScroll;
        int consumed =0;

        if(checkLegalLeft(mScroll)){
            if(willTo<mExpandHeight){
                willTo = mExpandHeight;
            }

            if(willTo>mOrgHei){
                willTo = mOrgHei;
            }

            consumed = willTo - mScroll;
            mScroll = willTo;

            return consumed;
        }else if(checkLegalRight(mScroll)){
            if (willTo > mExpandHeight) {
                willTo = mExpandHeight;
            }

            if(willTo<mOrgHei){
                willTo = mOrgHei;
            }

            consumed = willTo - mScroll;
            mScroll = willTo;

            return consumed;
        }else return consumed;

    }

    public boolean isOverHeight() {
        return mScroll < 0;
    }

}
