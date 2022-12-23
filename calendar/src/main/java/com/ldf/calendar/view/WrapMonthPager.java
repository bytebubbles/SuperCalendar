package com.ldf.calendar.view;

import static android.view.View.MeasureSpec.EXACTLY;

import static com.ldf.calendar.component.CalendarAttr.CalendarType.MONTH;
import static com.ldf.calendar.component.CalendarAttr.CalendarType.SCHEDULE_MONTH;
import static com.ldf.calendar.component.CalendarAttr.CalendarType.WEEK;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.ldf.calendar.Utils;
import com.ldf.calendar.behavior.MonthPagerBehavior;
import com.ldf.calendar.component.CalendarAttr;
import com.ldf.mi.calendar.R;

/**
 * author pzj
 * date 2022/11/8
 * Email 1538563097@qq.com
 * remarks：
 */

//@CoordinatorLayout.DefaultBehavior(MonthPagerBehavior.class)
public class WrapMonthPager extends FrameLayout  implements CoordinatorLayout.AttachedBehavior {

    private CalendarAttr.CalendarType calendarType = CalendarView.getCurrCalendarType();

    public static int weekHeight = 45;
    public static int scheduleHeight = 45;
    public static int minScheduleHeight = 0;
    public static int indicatorHeight = 45;

    private int indicatorLayoutId = -1;

    MonthPager monthPager;

    View bottomIndicator;

    public WrapMonthPager(Context context) {
        this(context, null);
    }

    public WrapMonthPager(Context context, AttributeSet attrs){
        this(context, attrs, 0);

    }

    public WrapMonthPager(Context context, AttributeSet attrs, int defStyle){
        super(context, attrs,defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.calendar, defStyle, 0);
        weekHeight = a.getDimensionPixelOffset(R.styleable.calendar_week_height, Utils.dpi2px(context,45));
        scheduleHeight = a.getDimensionPixelOffset(R.styleable.calendar_schedule_height, Utils.dpi2px(context, 45));
        minScheduleHeight = a.getDimensionPixelOffset(R.styleable.calendar_min_scheduleHeight, Utils.dpi2px(context, 0));
        indicatorHeight = a.getDimensionPixelOffset(R.styleable.calendar_indicator_height, Utils.dpi2px(context, 35));
        indicatorLayoutId = a.getResourceId(R.styleable.calendar_indicator_layout, -1);
        int type = a.getInt(R.styleable.calendar_calendar_type, 0);
        if(type == 2){
            calendarType = SCHEDULE_MONTH;
        }else if(type == 1){
            calendarType = MONTH;
        }else {
            calendarType = WEEK;
        }
        CalendarView.setCurrCalendarType(calendarType);
        a.recycle();
        init(context);

    }


    private void init(Context context) {

        monthPager = new MonthPager(context);
        //int height = monthPager.getViewHeight() + monthPager.getIndicatorHeight();
        int height = monthPager.getViewHeight();
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        monthPager.setLayoutParams(layoutParams);
        //monthPager.setBackgroundColor(Color.BLUE);
        addView(monthPager);

        if(indicatorLayoutId != -1){
            addIndicatorToParent(context);
        }


    }

    private void addIndicatorToParent(final Context context){
        post(new Runnable() {
            @Override
            public void run() {
                ViewGroup coordinatorLayout = ((ViewGroup)getParent());
                bottomIndicator = LayoutInflater.from(context).inflate(indicatorLayoutId, coordinatorLayout, false);
                CoordinatorLayout.LayoutParams layoutParams1 = (CoordinatorLayout.LayoutParams) bottomIndicator.getLayoutParams();
                bottomIndicator.setLayoutParams(layoutParams1);
                //addView(bottomIndicator);
                coordinatorLayout.addView(bottomIndicator);
                if(calendarType == CalendarAttr.CalendarType.MONTH){
                    setIndicatorTranslationY(monthPager.getMonthHeight());
                }else if(calendarType == CalendarAttr.CalendarType.WEEK) {
                    setIndicatorTranslationY(monthPager.getWeekHeight());
                }else {
                    setIndicatorTranslationY(monthPager.getViewHeight());
                }
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
        });
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int height = monthPager.getViewHeight() + WrapMonthPager.indicatorHeight;
        int newHeightSpec = MeasureSpec.makeMeasureSpec(height, EXACTLY);
        super.onMeasure(widthMeasureSpec, newHeightSpec);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //Log.d(MonthPagerBehavior.TAG, "WrapMonthPager- dispatchTouchEvent: ev: " + ev.getAction());
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        //Log.d(MonthPagerBehavior.TAG, "WrapMonthPager- onInterceptTouchEvent: ev: " + ev.getAction());
        return super.onInterceptTouchEvent(ev);
        //return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //Log.d(MonthPagerBehavior.TAG, "WrapMonthPager- onTouchEvent: ev: " + event.getAction());
        return super.onTouchEvent(event);
    }

    public MonthPager getMonthPager() {
        return monthPager;
    }

    public void setIndicatorTranslationY(int y){
        if(bottomIndicator != null){
            bottomIndicator.setTranslationY(y);
        }
    }

    public View getBottomIndicator() {
        return bottomIndicator;
    }

    @NonNull
    @Override
    public CoordinatorLayout.Behavior getBehavior() {
        return new MonthPagerBehavior();
    }
}
