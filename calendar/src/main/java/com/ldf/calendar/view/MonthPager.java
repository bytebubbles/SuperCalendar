package com.ldf.calendar.view;

import android.content.Context;

import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.ViewPager;

import com.ldf.calendar.Config;
import com.ldf.calendar.Utils;
import com.ldf.calendar.behavior.MonthPagerBehavior;
import com.ldf.calendar.component.CalendarViewAdapter;

@CoordinatorLayout.DefaultBehavior(MonthPagerBehavior.class)
public class MonthPager extends ViewPager {
    public static int CURRENT_DAY_INDEX = 1000;

    private int currentPosition = CURRENT_DAY_INDEX;
    private int weekHeight;     //天的高度
    private int scheduleHeight; //日程的高度
    private int minScheduleHeight; //最小日程的高度
    private int monthHeight;    //月视图高度
    private int viewHeight;
    private int indicatorHeight; //指示器高度
    private int rowIndex = 6;

    private OnPageChangeListener monthPageChangeListener;
    private boolean pageChangeByGesture = false;
    private boolean hasPageChangeListener = false;
    private boolean scrollable = true;
    private int pageScrollState = ViewPager.SCROLL_STATE_IDLE;

    public MonthPager(Context context) {
        this(context, null);
    }

    public MonthPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCellAndScheduleHeight(WrapMonthPager.weekHeight,
                WrapMonthPager.scheduleHeight,
                WrapMonthPager.minScheduleHeight,
                WrapMonthPager.indicatorHeight
        );
        init();
    }

    private void init() {
        ViewPager.OnPageChangeListener viewPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (monthPageChangeListener != null) {
                    monthPageChangeListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            }

            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                CalendarViewAdapter calendarViewAdapter = (CalendarViewAdapter) getAdapter();
                calendarViewAdapter.setCurrentPosition(position);
                if(calendarViewAdapter.getCalendarViewByPosition(currentPosition).getSelectedCalendarDate() != null){

                    CalendarViewAdapter.saveSelectedDate(calendarViewAdapter.getCalendarViewByPosition(currentPosition).getSelectedCalendarDate());
                }
                if (monthPageChangeListener != null) {
                    monthPageChangeListener.onPageSelected(position);
                }
                /*if (pageChangeByGesture) {
                    if (monthPageChangeListener != null) {
                        monthPageChangeListener.onPageSelected(position);
                    }
                    pageChangeByGesture = false;
                }*/
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                pageScrollState = state;
                if (monthPageChangeListener != null) {
                    monthPageChangeListener.onPageScrollStateChanged(state);
                }
                //pageChangeByGesture = true;
            }
        };
        addOnPageChangeListener(viewPageChangeListener);
        hasPageChangeListener = true;
    }

/*    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //int mWithMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

        int mHeightMeasureSpec = MeasureSpec.makeMeasureSpec(viewHeight, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, mHeightMeasureSpec);
    }*/

    @Override
    public void addOnPageChangeListener(ViewPager.OnPageChangeListener listener) {
        if (hasPageChangeListener) {
            Log.e("ldf", "MonthPager Just Can Use Own OnPageChangeListener");
        } else {
            super.addOnPageChangeListener(listener);
        }
    }

    public void addOnPageChangeListener(OnPageChangeListener listener) {
        this.monthPageChangeListener = listener;
        Log.e("ldf", "MonthPager Just Can Use Own OnPageChangeListener");
    }

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent me) {
        if (!scrollable){
            Log.d(MonthPagerBehavior.TAG, "MonthPager- onTouchEvent: ev: " + me.getAction() + " return false");
            return false;
        } else{
            boolean re = super.onTouchEvent(me);
            Log.d(MonthPagerBehavior.TAG, "MonthPager- onTouchEvent: ev: " + me.getAction() + " return "+ re);
            return re;
        }

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent me) {
        if (!scrollable) {
            Log.d(MonthPagerBehavior.TAG, "MonthPager- onInterceptTouchEvent: ev: " + me.getAction() + " return false");
            return false;
        }
        else {
            boolean re = super.onInterceptTouchEvent(me);
            Log.d(MonthPagerBehavior.TAG, "MonthPager- onInterceptTouchEvent: ev: " + me.getAction() + " return "+re);
            return re;
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        boolean re = super.dispatchTouchEvent(ev);
        Log.d(MonthPagerBehavior.TAG, "MonthPager- dispatchTouchEvent: ev: " + ev.getAction() + " return " + re);
        return re;
    }

    public void selectOtherMonth(int offset) {
        setCurrentItem(currentPosition + offset);
        CalendarViewAdapter calendarViewAdapter = (CalendarViewAdapter) getAdapter();
        calendarViewAdapter.notifyDataChanged(CalendarViewAdapter.loadSelectedDate());
    }

    public int getPageScrollState() {
        return pageScrollState;
    }

    public WrapMonthPager getParentView() {
        return (WrapMonthPager) getParent();
    }

    public interface OnPageChangeListener {
        void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        void onPageSelected(int position);

        void onPageScrollStateChanged(int state);
    }

    public int getTopMovableDistance() {
        CalendarViewAdapter calendarViewAdapter = (CalendarViewAdapter) getAdapter();
        if(calendarViewAdapter == null) {
            return weekHeight;
        }
        rowIndex = calendarViewAdapter.getPagers().get(currentPosition % 3).getSelectedRowIndex();
        return weekHeight * rowIndex + minScheduleHeight * rowIndex;
    }

    public int getWeekHeight() {
        return weekHeight;
    }

    public void setViewHeight(int viewHeight) {
        weekHeight = viewHeight / 6;
        this.viewHeight = viewHeight;
    }

    public void setCellAndScheduleHeight(int cellHeight, int scheduleHeight, int minScheduleHeight, int indicatorHeight){
        this.weekHeight = cellHeight;
        this.scheduleHeight = scheduleHeight;
        this.minScheduleHeight = minScheduleHeight;
        this.monthHeight = cellHeight * 6 + minScheduleHeight * 6;
        this.viewHeight = cellHeight * 6 + scheduleHeight * 6;
        this.indicatorHeight = indicatorHeight;
    }


    public int getCurrentPosition() {
        return currentPosition;
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public int getRowIndex() {
        CalendarViewAdapter calendarViewAdapter = (CalendarViewAdapter) getAdapter();
        rowIndex = calendarViewAdapter.getPagers().get(currentPosition % 3).getSelectedRowIndex();
        Log.e("ldf", "getRowIndex = " + rowIndex);
        return rowIndex;
    }

    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getScheduleHeight() {
        return scheduleHeight;
    }

    public int getMinScheduleHeight() {
        return minScheduleHeight;
    }

    public int getMonthHeight() {
        return monthHeight;
    }


    public int getViewHeight() {
        return viewHeight;
    }

    public int getIndicatorHeight() {
        return indicatorHeight;
    }

    public int getViewHeightWithIndicator(){
        return getViewHeight() + getIndicatorHeight();
    }

    public int getMonthHeightWithIndicator(){
        return getMonthHeight() + getIndicatorHeight();
    }

    public int getWeekHeightWithIndicator(){
        return getWeekHeight() + getIndicatorHeight();
    }
}
