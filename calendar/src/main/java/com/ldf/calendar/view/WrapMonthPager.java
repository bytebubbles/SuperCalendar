package com.ldf.calendar.view;

import android.content.Context;
import android.graphics.Color;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

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
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        monthPager.setLayoutParams(layoutParams);
        monthPager.setBackgroundColor(Color.BLUE);
        addView(monthPager);

        bottomIndicator = LayoutInflater.from(context).inflate(R.layout.calendar_bottom_indicator, this, false);
        //LinearLayout.LayoutParams layoutParams1 = (LayoutParams) bottomIndicator.getLayoutParams();
        //layoutParams1.gravity = Gravity.BOTTOM;
       // bottomIndicator.setLayoutParams(layoutParams1);
        //addView(bottomIndicator);
    }

    public MonthPager getMonthPager() {
        return monthPager;
    }
}
