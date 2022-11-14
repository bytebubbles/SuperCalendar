package com.ldf.calendar.behavior;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.ldf.calendar.Utils;
import com.ldf.calendar.component.CalendarAttr;
import com.ldf.calendar.view.Calendar;
import com.ldf.calendar.view.MonthPager;
import com.ldf.calendar.view.WrapMonthPager;

public class RecyclerViewBehavior3 extends CoordinatorLayout.Behavior<View> {
    private int monthOffset = -1;
    private int weekOffset = -1;
    private int scheduleMonthOffset = -1;
    private int indicatorHeight = -1;
    private Context context;
    private boolean initiated = false;
    boolean hidingTop = false;
    boolean showingTop = false;
    private CalendarAttr.CalendarType calendarType = Calendar.getCurrCalendarType();

    public RecyclerViewBehavior3(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        parent.onLayoutChild(child, layoutDirection);
        MonthPager monthPager = getMonthPager(parent);
        initMinOffsetAndInitOffset(parent, child, monthPager);
        return true;
    }

    private void initMinOffsetAndInitOffset(CoordinatorLayout parent,
                                            View child,
                                            MonthPager monthPager) {
        WrapMonthPager wrapMonthPager = (WrapMonthPager)monthPager.getParent();

        if (monthPager.getBottom() > 0 && monthOffset == -1) {

            monthOffset = monthPager.getMonthHeight();
            weekOffset = getMonthPager(parent).getWeekHeight();
            scheduleMonthOffset = monthPager.getViewHeight();
            indicatorHeight = monthPager.getIndicatorHeight();
            if(calendarType == CalendarAttr.CalendarType.MONTH){
                saveTop(monthOffset);
                wrapMonthPager.setIndicatorTranslationY(monthOffset);
            }else if(calendarType == CalendarAttr.CalendarType.WEEK) {
                saveTop(weekOffset);
                wrapMonthPager.setIndicatorTranslationY(weekOffset);
            }else {
                saveTop(scheduleMonthOffset);
                wrapMonthPager.setIndicatorTranslationY(scheduleMonthOffset);
            }

        }
        if (!initiated) {
            monthOffset = monthPager.getMonthHeight();
            weekOffset = getMonthPager(parent).getWeekHeight();
            scheduleMonthOffset = monthPager.getViewHeight();
            indicatorHeight = monthPager.getIndicatorHeight();
            if(calendarType == CalendarAttr.CalendarType.MONTH){
                saveTop(monthOffset);
                wrapMonthPager.setIndicatorTranslationY(monthOffset);
            }else if(calendarType == CalendarAttr.CalendarType.WEEK) {
                saveTop(weekOffset);
                wrapMonthPager.setIndicatorTranslationY(weekOffset);
            }else {
                saveTop(scheduleMonthOffset);
                wrapMonthPager.setIndicatorTranslationY(scheduleMonthOffset);
            }
            initiated = true;
        }
        child.offsetTopAndBottom(Utils.loadTop());
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, View child,
                                       View directTargetChild, View target, int nestedScrollAxes) {
        MonthPager monthPager = (MonthPager) getMonthPager(coordinatorLayout);
        monthPager.setScrollable(false);
        boolean isVertical = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;

        return isVertical;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, View child,
                                  View target, int dx, int dy, int[] consumed) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        child.setVerticalScrollBarEnabled(true);

        MonthPager monthPager = (MonthPager) getMonthPager(coordinatorLayout);
        if (monthPager.getPageScrollState() != ViewPager.SCROLL_STATE_IDLE) {
            consumed[1] = dy;
            Toast.makeText(context, "loading month data", Toast.LENGTH_SHORT).show();
            return;
        }

        // 上滑，正在隐藏顶部的日历
        hidingTop = dy > 0 && child.getTop() <= scheduleMonthOffset + indicatorHeight
                && child.getTop() > getMonthPager(coordinatorLayout).getWeekHeightWithIndicator();
        // 下滑，正在展示顶部的日历
        showingTop = dy < 0 && !ViewCompat.canScrollVertically(target, -1);

        if (hidingTop ) {
            consumed[1] = Utils.scroll(child, dy,
                    getMonthPager(coordinatorLayout).getWeekHeightWithIndicator(),
                    getMonthPager(coordinatorLayout).getViewHeightWithIndicator());
            saveTop(child.getTop() - indicatorHeight);
        }
    }

    @Override
    public void onStopNestedScroll(final CoordinatorLayout parent, final View child, View target) {
        super.onStopNestedScroll(parent, child, target);

        MonthPager wrapView = getMonthPager(parent);
        wrapView.setScrollable(true);
        if(Utils.loadTop() == wrapView.getWeekHeightWithIndicator()
                || Utils.loadTop() == wrapView.getMonthHeightWithIndicator()
                || Utils.loadTop() == wrapView.getViewHeightWithIndicator()
        ) return;
        Utils.touchUp(parent, wrapView, hidingTop);
    }

    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, View child, View target, float velocityX, float velocityY, boolean consumed) {
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, View child, View target, float velocityX, float velocityY) {
        // 日历隐藏和展示过程，不允许RecyclerView进行fling
        if (hidingTop || showingTop) {
            return true;
        } else {
            return false;
        }
    }

    private MonthPager getMonthPager(CoordinatorLayout coordinatorLayout) {
        return ((WrapMonthPager) coordinatorLayout.getChildAt(0)).getMonthPager();
    }

    private void saveTop(int top) {
        Utils.saveTop(top + indicatorHeight);
        if (Utils.loadTop() == monthOffset) {
            Utils.setScrollToBottom(false);
        } else if (Utils.loadTop() == weekOffset) {
            Utils.setScrollToBottom(true);
        } else if(Utils.loadTop() == scheduleMonthOffset){
        }
    }
}
