package com.ldf.calendar.view;

import static android.view.View.MeasureSpec.EXACTLY;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ldf.calendar.Config;
import com.ldf.calendar.Utils;
import com.ldf.calendar.behavior.MonthPagerBehavior;
import com.ldf.mi.calendar.R;

/**
 * author pzj
 * date 2022/11/8
 * Email 1538563097@qq.com
 * remarks：
 */

@CoordinatorLayout.DefaultBehavior(MonthPagerBehavior.class)
public class WrapMonthPager extends FrameLayout {

    MonthPager monthPager;

    View bottomIndicator;

    public WrapMonthPager(Context context) {
        this(context, null);
    }

    public WrapMonthPager(Context context, AttributeSet attrs){
        super(context, attrs);
        init(context);
    }


    private void init(Context context) {

        //setOrientation(VERTICAL);
        monthPager = new MonthPager(context);
        //int height = monthPager.getViewHeight() + monthPager.getIndicatorHeight();
        int height = monthPager.getViewHeight();
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        monthPager.setLayoutParams(layoutParams);
        monthPager.setBackgroundColor(Color.BLUE);
        addView(monthPager);

        bottomIndicator = LayoutInflater.from(context).inflate(R.layout.calendar_bottom_indicator, this, false);
        FrameLayout.LayoutParams layoutParams1 = (LayoutParams) bottomIndicator.getLayoutParams();
        layoutParams1.gravity = Gravity.BOTTOM;
        bottomIndicator.setLayoutParams(layoutParams1);
        addView(bottomIndicator);
        bottomIndicator.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    return true;
                }else {
                    return false;
                }

            }
        });

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = monthPager.getViewHeight() + Utils.dpi2px(getContext(), Config.indicatorHeight);
        int newHeightSpec = MeasureSpec.makeMeasureSpec(height, EXACTLY);
        super.onMeasure(widthMeasureSpec, newHeightSpec);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Log.d(MonthPagerBehavior.TAG, "WrapMonthPager- dispatchTouchEvent: ev: " + ev.getAction());
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        Log.d(MonthPagerBehavior.TAG, "WrapMonthPager- onInterceptTouchEvent: ev: " + ev.getAction());
        return super.onInterceptTouchEvent(ev);
        //return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.d(MonthPagerBehavior.TAG, "WrapMonthPager- onTouchEvent: ev: " + event.getAction());
        return super.onTouchEvent(event);
    }

    public MonthPager getMonthPager() {
        return monthPager;
    }

    public View getBottomIndicator() {
        return bottomIndicator;
    }
}
