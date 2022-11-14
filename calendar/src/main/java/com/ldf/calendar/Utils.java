/*
 * Copyright (c) 2016.
 * wb-lijinwei.a@alibaba-inc.com
 */

package com.ldf.calendar;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.ldf.calendar.component.CalendarAttr;
import com.ldf.calendar.component.CalendarViewAdapter;
import com.ldf.calendar.model.CalendarDate;
import com.ldf.calendar.view.MonthPager;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public final class Utils {

    private static HashMap<String, String> markData = new HashMap<>();
    private static HashMap<String, List> scheduleData = new HashMap<>();

    private Utils() {

    }

    /**
     * 得到某一个月的具体天数
     *
     * @param year  参数月所在年
     * @param month 参数月
     * @return int 参数月所包含的天数
     */
    public static int getMonthDays(int year, int month) {
        if (month > 12) {
            month = 1;
            year += 1;
        } else if (month < 1) {
            month = 12;
            year -= 1;
        }
        int[] monthDays = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
        int days = 0;
        // 闰年2月29天
        if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
            monthDays[1] = 29;
        }
        try {
            days = monthDays[month - 1];
        } catch (Exception e) {
            e.getStackTrace();
        }
        return days;
    }

    public static int getYear() {
        return Calendar.getInstance().get(Calendar.YEAR);
    }

    public static int getMonth() {
        return Calendar.getInstance().get(Calendar.MONTH) + 1;
    }

    public static int getDay() {
        return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
    }

    /**
     * 得到当前月第一天在其周的位置
     *
     * @param year  当前年
     * @param month 当前月
     * @param type  周排列方式 0代表周一作为本周的第一天， 2代表周日作为本周的第一天
     * @return int 本月第一天在其周的位置
     */
    public static int getFirstDayWeekPosition(int year, int month, CalendarAttr.WeekArrayType type) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getDateFromString(year, month));
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (type == CalendarAttr.WeekArrayType.Sunday) {
            return week_index;
        } else {
            week_index = cal.get(Calendar.DAY_OF_WEEK) + 5;
            if (week_index >= 7) {
                week_index -= 7;
            }
        }
        return week_index;
    }

    /**
     * 将yyyy-MM-dd类型的字符串转化为对应的Date对象
     *
     * @param year  当前年
     * @param month 当前月
     * @return Date  对应的Date对象
     */
    @SuppressLint("SimpleDateFormat")
    public static Date getDateFromString(int year, int month) {
        String dateString = year + "-" + (month > 9 ? month : ("0" + month)) + "-01";
        Date date = new Date();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        return date;
    }

    /**
     * 计算参数日期月与当前月相差的月份数
     *
     * @param year        参数日期所在年
     * @param month       参数日期所在月
     * @param currentDate 当前月
     * @return int offset 相差月份数
     */
    public static int calculateMonthOffset(int year, int month, CalendarDate currentDate) {
        int currentYear = currentDate.getYear();
        int currentMonth = currentDate.getMonth();
        int offset = (year - currentYear) * 12 + (month - currentMonth);
        return offset;
    }

    /**
     * 删除方法, 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理
     *
     * @param context 上下文
     * @param dpi     dp为单位的尺寸
     * @return int 转化而来的对应像素
     */
    public static int dpi2px(Context context, float dpi) {
        return (int) (context.getResources().getDisplayMetrics().density * dpi + 0.5f);
    }

    /**
     * 得到标记日期数据，可以通过该数据得到标记日期的信息，开发者可自定义格式
     * 目前HashMap<String, String>的组成仅仅是为了DEMO效果
     *
     * @return HashMap<String, String> 标记日期数据
     */
    public static HashMap<String, String> loadMarkData() {
        return markData;
    }

    public static HashMap<String, List> loadScheduleData() { return scheduleData;}

    /**
     * 设置标记日期数据
     *
     * @param data 标记日期数据
     * @return void
     */
    public static void setMarkData(HashMap<String, String> data) {
        markData = data;
    }

    public static void cleanMarkData(){
        markData.clear();
    }

    public static void setScheduleData(HashMap<String, List> scheduleData) {
        Utils.scheduleData = scheduleData;
    }

    public static void cleanScheduleData(){
        scheduleData.clear();
    }

    /**
     * 计算偏移距离
     *
     * @param offset 偏移值
     * @param min    最小偏移值
     * @param max    最大偏移值
     * @return int offset
     */
    private static int calcOffset(int offset, int min, int max) {
        if (offset > max) {
            return max;
        } else if (offset < min) {
            return min;
        } else {
            return offset;
        }
    }

    /**
     * 删除方法, 这里只会删除某个文件夹下的文件，如果传入的directory是个文件，将不做处理
     *
     * @param tempChild     需要移动的View
     * @param dy        实际偏移量
     * @param minOffset 最小偏移量
     * @param maxOffset 最大偏移量
     * @return void
     */
    public static int scroll(View tempChild, int dy, int minOffset, int maxOffset) {
        View child =  tempChild;
        final int initOffset = child.getTop();
        int offset = calcOffset(initOffset - dy, minOffset, maxOffset) - initOffset;
        child.offsetTopAndBottom(offset);
        return -offset;
    }

    /**
     * 得到TouchSlop
     *
     * @param context 上下文
     * @return int touchSlop的具体值
     */
    public static int getTouchSlop(Context context) {
        return ViewConfiguration.get(context).getScaledTouchSlop();
    }

    /**
     * 得到种子日期所在周的周日
     *
     * @param seedDate 种子日期
     * @return CalendarDate 所在周周日 注意:是获取周日结尾的
     */
    public static CalendarDate getSunday(CalendarDate seedDate) {// TODO: 16/12/12 得到一个CustomDate对象
        Calendar c = Calendar.getInstance();
        String dateString = seedDate.toString();
        Date date = new Date();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        c.setTime(date);
        if (c.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            c.add(Calendar.DAY_OF_MONTH, 7 - c.get(Calendar.DAY_OF_WEEK) + 1);
        }
        return new CalendarDate(c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH));
    }



    /**
     * 得到种子日期所在周的周六
     *
     * @param seedDate 种子日期
     * @return CalendarDate 所在周周六
     */
    public static CalendarDate getSaturday(CalendarDate seedDate) {// TODO: 16/12/12 得到一个CustomDate对象
        Calendar c = Calendar.getInstance();
        String dateString = seedDate.toString();
        Date date = null;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-d");
            date = sdf.parse(dateString);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
        c.setTime(date);
        c.add(Calendar.DAY_OF_MONTH, 7 - c.get(Calendar.DAY_OF_WEEK));
        return new CalendarDate(c.get(Calendar.YEAR),
                c.get(Calendar.MONTH) + 1,
                c.get(Calendar.DAY_OF_MONTH));
    }

    private static int top;
    private static boolean customScrollToBottom = false;

    /**
     * 判断上一次滑动改变周月日历是向下滑还是向上滑 向下滑表示切换为月日历模式 向上滑表示切换为周日历模式
     *
     * @return boolean 是否是在向下滑动。(true: 已经收缩; false: 已经打开）
     */
    public static boolean isScrollToBottom() {
        return customScrollToBottom;
    }

    /**
     * 设置上一次滑动改变周月日历是向下滑还是向上滑 向下滑表示切换为月日历模式 向上滑表示切换为周日历模式
     *
     * @return void
     */
    public static void setScrollToBottom(boolean customScrollToBottom) {
        Utils.customScrollToBottom = customScrollToBottom;
    }

    /**
     * 通过scrollTo方法完成协调布局的滑动，其中主要使用了ViewCompat.postOnAnimation
     *
     * @param parent   协调布局parent
     * @param child    协调布局协调滑动的child
     * @param y        滑动目标位置y轴数值
     * @param duration 滑动执行时间
     * @return void
     */
    public static void scrollTo(final CoordinatorLayout parent, final View child, final int y, int duration) {
        final Scroller scroller = new Scroller(parent.getContext());
        scroller.startScroll(0, top, 0, y - top, duration);   //设置scroller的滚动偏移量
        ViewCompat.postOnAnimation(child, new Runnable() {
            @Override
            public void run() {
                //返回值为boolean，true说明滚动尚未完成，false说明滚动已经完成。
                // 这是一个很重要的方法，通常放在View.computeScroll()中，用来判断是否滚动是否结束。
                if (scroller.computeScrollOffset()) {
                    int delta = scroller.getCurrY() - child.getTop();
                    child.offsetTopAndBottom(delta);
                    saveTop(child.getTop());
                    parent.dispatchDependentViewsChanged(child);
                    ViewCompat.postOnAnimation(child, this);
                }
            }
        });
    }

    public static void scrollTo2(final CoordinatorLayout parent, final View childRV, final View childMP, final View childWrapView, final int y, int duration) {
        /*final ValueAnimator anim = ValueAnimator.ofInt(0, y - top);
        anim.setDuration(100);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            int last = 0;
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int currentValue = (int) animation.getAnimatedValue();
                int diff = currentValue - last;
                last = currentValue;
                int c1 = (int) animation.getAnimatedFraction();
                Log.d("TAG", "cuurent value is " + currentValue + " diff: " +diff);
                childRV.offsetTopAndBottom(diff);
                saveTop(childRV.getTop());
                parent.dispatchDependentViewsChanged(childRV);
            }
        });
        anim.start();*/

        final Scroller scroller = new Scroller(parent.getContext());
        scroller.startScroll(0, top, 0, y - top, 400);   //设置scroller的滚动偏移量
        Log.e("123456", "scrollTo2: y - top: " + (y - top) + " y:" + y + " top:" +top);
        ViewCompat.postOnAnimation(childRV, new Runnable() {
            @Override
            public void run() {
                //返回值为boolean，true说明滚动尚未完成，false说明滚动已经完成。
                // 这是一个很重要的方法，通常放在View.computeScroll()中，用来判断是否滚动是否结束。
                if (scroller.computeScrollOffset()) {
                    int delta = scroller.getCurrY() - childRV.getTop();
                    childRV.offsetTopAndBottom(delta);

                    Log.i("TestEvent", "123456 delta:" + delta + " loadTOp: " + Utils.loadTop() + " dependency.getTop(): " + childRV.getTop() +" getCurrY:"+scroller.getCurrY() );

                    saveTop(childRV.getTop());
                    parent.dispatchDependentViewsChanged(childRV);
                    ViewCompat.postOnAnimation(childRV, this);
                }
            }
        });
    }

    public static void touchUp(CoordinatorLayout parent, MonthPager child,boolean directionUpa){

        CalendarViewAdapter calendarViewAdapter = (CalendarViewAdapter) child.getAdapter();
        com.ldf.calendar.view.Calendar calendar = calendarViewAdapter.getCurrCalendarView();
        View wrapView = calendar.getChildAt(0);

        //int scheduleToMonthTV = child.getMonthHeight() + (child.getViewHeight() - child.getMonthHeight())/2;
        int scheduleToMonthTV = child.getViewHeightWithIndicator() - 150;
        int monthToScheduleTV = child.getMonthHeightWithIndicator() + 150;

        int monthToWeekTV = child.getMonthHeightWithIndicator() - 150;
        if(Utils.loadTop() > child.getMonthHeightWithIndicator()){
            if(directionUpa){
                if(Utils.loadTop() < scheduleToMonthTV){
                    //日程状态向月状态过渡
                    //TODO 切换为月状态
                    Log.d("切换为月状态", "touchUp: ");
                    Utils.scrollTo2(parent,  parent.getChildAt(1), child, wrapView,child.getMonthHeightWithIndicator(), 300);
                    calendarViewAdapter.switchToMonth();
                    com.ldf.calendar.view.Calendar.setCurrCalendarType(CalendarAttr.CalendarType.MONTH);
                }else {
                    //日程状态向月状态过渡失败，回弹
                    Utils.scrollTo2(parent, parent.getChildAt(1), child, wrapView,child.getViewHeightWithIndicator(), 300);
                    com.ldf.calendar.view.Calendar.setCurrCalendarType(CalendarAttr.CalendarType.SCHEDULE_MONTH);
                }
            }else {
                if(Utils.loadTop() > monthToScheduleTV){
                    //月状态向日程状态过渡
                    //TODO 切换为日程状态
                    Utils.scrollTo2(parent, parent.getChildAt(1), child, wrapView,child.getViewHeightWithIndicator(), 300);
                    calendarViewAdapter.switchToSchedule();
                    com.ldf.calendar.view.Calendar.setCurrCalendarType(CalendarAttr.CalendarType.SCHEDULE_MONTH);
                }else {
                    //月状态向日程状态过度失败，回弹
                    Utils.scrollTo2(parent,  parent.getChildAt(1), child, wrapView,child.getMonthHeightWithIndicator(), 300);
                    com.ldf.calendar.view.Calendar.setCurrCalendarType(CalendarAttr.CalendarType.MONTH);
                    //calendarViewAdapter.switchToMonth();
                }
            }
        }else if(Utils.loadTop() == child.getMonthHeightWithIndicator()){
            //TODO 切换为月状态
            //com.ldf.calendar.view.Calendar.setCurrCalendarType(CalendarAttr.CalendarType.MONTH);
            calendarViewAdapter.switchToMonth();
            com.ldf.calendar.view.Calendar.setCurrCalendarType(CalendarAttr.CalendarType.MONTH);
        }else{
            //Utils.loadTop() < child.getMonthHeight()
            if(directionUpa){
                if(Utils.loadTop() < monthToWeekTV){
                    //月状态向周状态过度
                    //TODO 切换为周
                    if(Utils.loadTop() > child.getWeekHeightWithIndicator()){
                        Utils.scrollTo(parent,  parent.getChildAt(1), child.getWeekHeightWithIndicator(), 300);
                    }else {
                        saveTop(child.getWeekHeightWithIndicator());
                    }
                    //com.ldf.calendar.view.Calendar.setCurrCalendarType(CalendarAttr.CalendarType.WEEK);
                    calendarViewAdapter.switchToWeek(child.getRowIndex());
                    com.ldf.calendar.view.Calendar.setCurrCalendarType(CalendarAttr.CalendarType.WEEK);
                }else {
                    //月状态向周状态过度失败，回弹
                    Utils.scrollTo(parent, parent.getChildAt(1), child.getMonthHeightWithIndicator(), 300);
                    calendarViewAdapter.switchToMonth();
                    com.ldf.calendar.view.Calendar.setCurrCalendarType(CalendarAttr.CalendarType.MONTH);
                }
            }else{
                if(Utils.loadTop() > Utils.loadTop() - monthToWeekTV){
                    //周状态向月状态过度
                    //TODO 切换为月状态
                    Utils.scrollTo(parent,  parent.getChildAt(1), child.getMonthHeightWithIndicator(), 300);
                    calendarViewAdapter.switchToMonth();
                    com.ldf.calendar.view.Calendar.setCurrCalendarType(CalendarAttr.CalendarType.MONTH);
                }else {
                    //周状态向月状态过度失败，回弹
                    Utils.scrollTo(parent,  parent.getChildAt(1), child.getWeekHeightWithIndicator(), 300);
                    com.ldf.calendar.view.Calendar.setCurrCalendarType(CalendarAttr.CalendarType.WEEK);
                    //calendarViewAdapter.switchToWeek(child.getRowIndex());
                }
            }

        }

    }

    /**
     * 返回最大行数
     * @param targetYear
     * @param targetMont
     * @param type
     * @return
     */
    public static int totalRowCount(int targetYear, int targetMont, CalendarAttr.WeekArrayType type){
        int currentMonthDays = Utils.getMonthDays(targetYear, targetMont);    // 当前月的天数
        int firstDayPosition = Utils.getFirstDayWeekPosition(
                targetYear,
                targetMont,
                type);
        int  fiveRowGrid = 5 * 7;
        int diff = fiveRowGrid - currentMonthDays;
        if(firstDayPosition <= diff){
            //5行
            return 5;
        }else {
            //6行
            return 6;
        }
    }


    public static void saveTop(int y) {
        top = y;
    }

    public static int loadTop() {
        return top;
    }
}
