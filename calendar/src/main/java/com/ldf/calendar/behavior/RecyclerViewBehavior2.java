package com.ldf.calendar.behavior;


import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ldf.calendar.Utils;
import com.ldf.calendar.component.CalendarAttr;
import com.ldf.calendar.component.CalendarViewAdapter;
import com.ldf.calendar.view.Calendar;
import com.ldf.calendar.view.MonthPager;

public class RecyclerViewBehavior2 extends CoordinatorLayout.Behavior<RecyclerView> {
    private int monthOffset = -1;
    private int weekOffset = -1;
    private int scheduleMonthOffset = -1;
    private Context context;
    private boolean initiated = false;
    boolean hidingTop = false;
    boolean showingTop = false;
    private CalendarAttr.CalendarType calendarType = Calendar.getCurrCalendarType();

    public RecyclerViewBehavior2(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, RecyclerView child, int layoutDirection) {
        parent.onLayoutChild(child, layoutDirection);
        MonthPager monthPager = getMonthPager(parent);
        initMinOffsetAndInitOffset(parent, child, monthPager);
        return true;
    }

    private void initMinOffsetAndInitOffset(CoordinatorLayout parent,
                                            RecyclerView child,
                                            MonthPager monthPager) {
        if (monthPager.getBottom() > 0 && monthOffset == -1) {

            monthOffset = monthPager.getMonthHeight();
            weekOffset = getMonthPager(parent).getWeekHeight();
            scheduleMonthOffset = monthPager.getViewHeight();
            if(calendarType == CalendarAttr.CalendarType.MONTH){
                saveTop(monthOffset);
            }else if(calendarType == CalendarAttr.CalendarType.WEEK) {
                saveTop(weekOffset);
            }else {
                saveTop(scheduleMonthOffset);
            }

        }
        if (!initiated) {
            monthOffset = monthPager.getMonthHeight();
            weekOffset = getMonthPager(parent).getWeekHeight();
            scheduleMonthOffset = monthPager.getViewHeight();
            if(calendarType == CalendarAttr.CalendarType.MONTH){
                saveTop(monthOffset);
            }else if(calendarType == CalendarAttr.CalendarType.WEEK) {
                saveTop(weekOffset);
            }else {
                saveTop(scheduleMonthOffset);
            }
            initiated = true;
        }
        child.offsetTopAndBottom(Utils.loadTop());
       // minOffset = getMonthPager(parent).getCellHeight();
    }

    @Override
    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, RecyclerView child,
                                       View directTargetChild, View target, int nestedScrollAxes) {
        Log.e("ldf", "onStartNestedScroll");

        MonthPager monthPager = (MonthPager) coordinatorLayout.getChildAt(0);
        monthPager.setScrollable(false);
        boolean isVertical = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;

        return isVertical;
    }

    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull RecyclerView child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
    }

    @Override
    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, RecyclerView child,
                                  View target, int dx, int dy, int[] consumed) {
        Log.e("ldf", "onNestedPreScroll");
        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed);
        child.setVerticalScrollBarEnabled(true);

        MonthPager monthPager = (MonthPager) coordinatorLayout.getChildAt(0);
        if (monthPager.getPageScrollState() != ViewPager.SCROLL_STATE_IDLE) {
            consumed[1] = dy;
            Log.w("ldf", "onNestedPreScroll: MonthPager dragging");
            Toast.makeText(context, "loading month data", Toast.LENGTH_SHORT).show();
            return;
        }

        // 上滑，正在隐藏顶部的日历
        hidingTop = dy > 0 && child.getTop() <= scheduleMonthOffset
                && child.getTop() > getMonthPager(coordinatorLayout).getWeekHeight();
        // 下滑，正在展示顶部的日历
        showingTop = dy < 0 && !ViewCompat.canScrollVertically(target, -1);

        if (hidingTop ) {
            consumed[1] = Utils.scroll(child, dy,
                    getMonthPager(coordinatorLayout).getWeekHeight(),
                    getMonthPager(coordinatorLayout).getViewHeight());
            saveTop(child.getTop());
        }
    }

    @Override
    public void onStopNestedScroll(final CoordinatorLayout parent, final RecyclerView child, View target) {
        Log.e("ldf", "onStopNestedScroll");
        super.onStopNestedScroll(parent, child, target);

        MonthPager wrapView = getMonthPager(parent);
        wrapView.setScrollable(true);
        if(showingTop
                || Utils.loadTop() == wrapView.getWeekHeight()
                || Utils.loadTop() == wrapView.getMonthHeight()
                || Utils.loadTop() == wrapView.getScheduleHeight()
        ) return;
        //int scheduleToMonthTV = child.getMonthHeight() + (child.getViewHeight() - child.getMonthHeight())/2;
        Utils.touchUp(parent, wrapView, hidingTop);

       /* MonthPager monthPager = (MonthPager) parent.getChildAt(0);
        monthPager.setScrollable(true);
        if (!Utils.isScrollToBottom()) {
            if (monthOffset - Utils.loadTop() > Utils.getTouchSlop(context) && hidingTop) {
                Utils.scrollTo(parent, child, getMonthPager(parent).getWeekHeight(), 500);
            } else {
                Utils.scrollTo(parent, child, getMonthPager(parent).getViewHeight(), 150);
            }
        } else {
            if (Utils.loadTop() - weekOffset > Utils.getTouchSlop(context) && showingTop) {
                Utils.scrollTo(parent, child, getMonthPager(parent).getViewHeight(), 500);
            } else {
                Utils.scrollTo(parent, child, getMonthPager(parent).getWeekHeight(), 150);
            }
        }*/
    }

    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, RecyclerView child, View target, float velocityX, float velocityY, boolean consumed) {
        Log.d("ldf", "onNestedFling: velocityY: " + velocityY);
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, RecyclerView child, View target, float velocityX, float velocityY) {
        // 日历隐藏和展示过程，不允许RecyclerView进行fling
        if (hidingTop || showingTop) {
            return true;
        } else {
            return false;
        }
    }

    private MonthPager getMonthPager(CoordinatorLayout coordinatorLayout) {
        return (MonthPager) coordinatorLayout.getChildAt(0);
    }

    private void saveTop(int top) {
        Utils.saveTop(top);
        if (Utils.loadTop() == monthOffset) {
            //Calendar.setCurrCalendarType(CalendarAttr.CalendarType.MONTH);
            Utils.setScrollToBottom(false);
        } else if (Utils.loadTop() == weekOffset) {
            //Calendar.setCurrCalendarType(CalendarAttr.CalendarType.WEEK);
            Utils.setScrollToBottom(true);
        } else if(Utils.loadTop() == scheduleMonthOffset){
           //` Calendar.setCurrCalendarType(CalendarAttr.CalendarType.SCHEDULE_MONTH);
        }
    }
}
