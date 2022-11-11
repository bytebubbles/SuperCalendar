package com.ldf.calendar.component;

import static com.ldf.calendar.behavior.MonthPagerBehavior.test;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.ldf.calendar.interf.OnAdapterSelectListener;
import com.ldf.calendar.interf.IDayRenderer;
import com.ldf.calendar.interf.OnSelectDateListener;
import com.ldf.calendar.Utils;
import com.ldf.calendar.view.MonthPager;
import com.ldf.calendar.model.CalendarDate;
import com.ldf.calendar.view.Calendar;

import java.util.ArrayList;
import java.util.HashMap;

public class CalendarViewAdapter extends PagerAdapter {

    private static CalendarDate date = new CalendarDate();
    private ArrayList<Calendar> calendars = new ArrayList<>();
    private int currentPosition = MonthPager.CURRENT_DAY_INDEX;
    private CalendarAttr.CalendarType calendarType = Calendar.getCurrCalendarType();
    //private int rowCount = 0;
    private CalendarDate seedDate;
    private OnCalendarTypeChanged onCalendarTypeChangedListener;
    //周排列方式 1：代表周日显示为本周的第一天
    //0:代表周一显示为本周的第一天
    private CalendarAttr.WeekArrayType weekArrayType = CalendarAttr.WeekArrayType.Monday;

    public CalendarViewAdapter(Context context,
                               OnSelectDateListener onSelectDateListener,
                               CalendarAttr.WeekArrayType weekArrayType,
                               IDayRenderer dayView) {
        super();
        this.weekArrayType = weekArrayType;
        init(context, onSelectDateListener);
        setCustomDayRenderer(dayView);
    }

    public static void saveSelectedDate(CalendarDate calendarDate) {
        date = calendarDate;
    }

    public static CalendarDate loadSelectedDate() {
        return date;
    }

    private void init(Context context, OnSelectDateListener onSelectDateListener) {
        saveSelectedDate(new CalendarDate());
        //初始化的种子日期为今天
        seedDate = new CalendarDate();
        seedDate.setDay(1);
        for (int i = 0; i < 3; i++) {
            CalendarAttr calendarAttr = new CalendarAttr();
            calendarAttr.setCalendarType(calendarType);
            calendarAttr.setWeekArrayType(weekArrayType);
            Calendar calendar = new Calendar(context, onSelectDateListener, calendarAttr);
            calendar.setOnAdapterSelectListener(new OnAdapterSelectListener() {
                @Override
                public void cancelSelectState() {
                    //cancelOtherSelectState();
                }

                @Override
                public void updateSelectState() {
                    //invalidateCurrentCalendar();
                }
            });
            calendars.add(calendar);
        }
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        Log.e("ldf", "setPrimaryItem");
        super.setPrimaryItem(container, position, object);
        this.currentPosition = position;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Log.e("ldf", "instantiateItem");
        if (position < 2) {
            return null;
        }
        Calendar calendar = calendars.get(position % calendars.size());
        calendar.setMonthPager((MonthPager) container);
        if (calendarType == CalendarAttr.CalendarType.MONTH
                || calendarType == CalendarAttr.CalendarType.SCHEDULE_MONTH) {
            CalendarDate current = seedDate.modifyMonth(position - MonthPager.CURRENT_DAY_INDEX);
            current.setDay(1);//每月的种子日期都是1号
            setSelectedCalendarInCalendar(calendar, current);
            calendar.showDate(current);
        } else {
            CalendarDate current = date.modifyWeek(position - currentPosition);
            calendar.setSelectedCalendarDate(current);
            CalendarDate saturday = Utils.getSaturday(current);
            CalendarDate sunday;
            if(position - currentPosition < 0){
                sunday = Utils.getSunday(current.modifyWeek(-1)); //获取上上周的周日，即当前行的开始日期
            }else {
                sunday = Utils.getSunday(date);
            }
            calendar.showDate(current); //默认用选中日期创建当前月数据
            int selectIndex = getSelectIndex(position - currentPosition, saturday, sunday, current);
            calendar.updateWeek(selectIndex);
        }
        if (container.getChildCount() == calendars.size()) {
            container.removeView(calendars.get(position % 3));
        }
        if (container.getChildCount() < calendars.size()) {
            container.addView(calendar, 0);
        } else {
            container.addView(calendar, position % 3);
        }
        return calendar;
    }

    /**
     * 周模式：计算页面选择的行
     * @param offsetPage
     * @param saturday
     * @param sunday
     * @return
     */
    private int getSelectIndex(int offsetPage, CalendarDate saturday, CalendarDate sunday, CalendarDate nextSelectDate ) {
        //获取当前选中日期的 开始周和结束周
        CalendarDate currFirstDate = Utils.getSunday(date.modifyWeek(-1)) ; //需要偏移上一周在获取周日，周日开头模式
        CalendarDate currLastDate = Utils.getSaturday(date);    //这周的周六

        CalendarDate firstDate = sunday;
        CalendarDate lastDate = saturday;
        CalendarDate seedDate = nextSelectDate;

        int selectIndex = getCurrCalendarView().getSelectedRowIndex();
        if(saturday.equalsMonth(sunday)){
            if(offsetPage > 0){
                if(currLastDate.equalsMonth(firstDate)){
                    if(currFirstDate.equalsMonth(lastDate)){
                        selectIndex +=1;
                    }else {
                        //这种情况就是当前周里有上月的日期和下月的日期并存

                        selectIndex = 1;
                    }
                }else {
                    //刚好满格到下一月
                    selectIndex = 0;
                }
            }else {
                if(currFirstDate.equalsMonth(lastDate) && currLastDate.equalsMonth(firstDate)){
                    selectIndex -=1;
                }else {
                    //倒数第二行
                    selectIndex = Utils.totalRowCount(seedDate.year, seedDate.month, weekArrayType)-1-1;
                }
            }
        }else {
            //并存行。即该行同时存在当月和下页月
            //如果选中的日期月份和当前的页月份一至则 为 最后一行，否则 第二页的第0行
            if(date.equalsMonth(nextSelectDate)){
                if(offsetPage > 0){
                    selectIndex = Utils.totalRowCount(seedDate.year, seedDate.month, weekArrayType)-1;
                }else {
                    selectIndex = 0;
                }

            }else {
                if(offsetPage > 0){
                    selectIndex = 0;
                }else {
                    selectIndex = Utils.totalRowCount(seedDate.year, seedDate.month, weekArrayType) -1;
                }
            }
        }
        return selectIndex;
    }


    @Override
    public int getCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == ((View) object);
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(container);
    }

    public ArrayList<Calendar> getPagers() {
        return calendars;
    }

    public CalendarDate getFirstVisibleDate() {
        return calendars.get(currentPosition % 3).getFirstDate();
    }

    public CalendarDate getLastVisibleDate() {
        return calendars.get(currentPosition % 3).getLastDate();
    }

    public Calendar getCurrCalendarView(){
        return calendars.get(currentPosition % 3);
    }

    public Calendar getCalendarViewByPosition(int pagerPosition){
        return calendars.get(pagerPosition % 3);
    }

    public void cancelOtherSelectState() {
        for (int i = 0; i < calendars.size(); i++) {
            Calendar calendar = calendars.get(i);
            calendar.cancelSelectState();
        }
    }

/*    public void invalidateCurrentCalendar() {
        for (int i = 0; i < calendars.size(); i++) {
            Calendar calendar = calendars.get(i);
            calendar.update();
            if (calendar.getCalendarType() == CalendarAttr.CalendarType.WEEK) {
                calendar.updateWeek(rowCount);
            }
        }
    }*/

    public void setMarkData(HashMap<String, String> markData) {
        //Utils.setMarkData(markData);
        //notifyDataChanged();
    }

    public void switchToSchedule(){
        Log.d("switchTo", "switchToSchedule: ");
        if (calendars != null && calendars.size() > 0 && calendarType != CalendarAttr.CalendarType.SCHEDULE_MONTH) {
            if (onCalendarTypeChangedListener != null) {
                onCalendarTypeChangedListener.onCalendarTypeChanged(CalendarAttr.CalendarType.SCHEDULE_MONTH);
            }

            MonthPager.CURRENT_DAY_INDEX = currentPosition;
            Calendar v = calendars.get(currentPosition % 3);//0
            seedDate = v.getSeedDate();

            Calendar v1 = calendars.get(currentPosition % 3);//0

            Calendar v2 = calendars.get((currentPosition - 1) % 3);//2

            Calendar v3 = calendars.get((currentPosition + 1) % 3);//1
            CalendarAttr.CalendarType lastType = calendarType;
            calendarType = CalendarAttr.CalendarType.SCHEDULE_MONTH;
            if(lastType == CalendarAttr.CalendarType.MONTH){
                v1.switchCalendarType(CalendarAttr.CalendarType.SCHEDULE_MONTH);
                v2.switchCalendarType(CalendarAttr.CalendarType.SCHEDULE_MONTH);
                v3.switchCalendarType(CalendarAttr.CalendarType.SCHEDULE_MONTH);
            }else{
                v1.switchCalendarType(CalendarAttr.CalendarType.SCHEDULE_MONTH);
                v1.showDate(seedDate);

                v2.switchCalendarType(CalendarAttr.CalendarType.SCHEDULE_MONTH);
                CalendarDate last = seedDate.modifyMonth(-1);
                last.setDay(1);
                v2.showDate(last);

                v3.switchCalendarType(CalendarAttr.CalendarType.SCHEDULE_MONTH);
                CalendarDate next = seedDate.modifyMonth(1);
                next.setDay(1);
                v3.showDate(next);
            }
            v1.resetOffsetY();
            v2.resetOffsetY();
            v3.resetOffsetY();
        }
    }

    public void switchToMonth() {
        Log.d("switchTo", "switchToMonth: ");
        if (calendars != null && calendars.size() > 0 && calendarType != CalendarAttr.CalendarType.MONTH) {
            if (onCalendarTypeChangedListener != null) {
                onCalendarTypeChangedListener.onCalendarTypeChanged(CalendarAttr.CalendarType.MONTH);
            }
            calendarType = CalendarAttr.CalendarType.MONTH;
            MonthPager.CURRENT_DAY_INDEX = currentPosition;
            Calendar v = calendars.get(currentPosition % 3);//0
            seedDate = v.getSeedDate();

            Calendar v1 = calendars.get(currentPosition % 3);//0
            v1.switchCalendarType(CalendarAttr.CalendarType.MONTH);
            v1.showDate(seedDate);

            Calendar v2 = calendars.get((currentPosition - 1) % 3);//2
            v2.switchCalendarType(CalendarAttr.CalendarType.MONTH);
            CalendarDate last = seedDate.modifyMonth(-1);
            last.setDay(1);
            setSelectedCalendarInCalendar(v2, last);
            v2.showDate(last);
            v2.resetOffsetY();

            Calendar v3 = calendars.get((currentPosition + 1) % 3);//1
            v3.switchCalendarType(CalendarAttr.CalendarType.MONTH);
            CalendarDate next = seedDate.modifyMonth(1);
            next.setDay(1);
            setSelectedCalendarInCalendar(v3, next);
            v3.showDate(next);
            v3.resetOffsetY();
        }
    }

    public void switchToWeek(int rowIndex) {
        Log.d("switchTo", "switchToWeek: ");
        //int rowCount = rowIndex;
        if (calendars != null && calendars.size() > 0 && calendarType != CalendarAttr.CalendarType.WEEK) {
            if (onCalendarTypeChangedListener != null) {
                onCalendarTypeChangedListener.onCalendarTypeChanged(CalendarAttr.CalendarType.WEEK);
            }
            calendarType = CalendarAttr.CalendarType.WEEK;
            Calendar.setCurrCalendarType(calendarType);
            MonthPager.CURRENT_DAY_INDEX = currentPosition;

            Calendar v = calendars.get(currentPosition % 3);
            seedDate = v.getSeedDate();
            //v.getSelectedRowIndex();
            v.setSelectedRowIndex(rowIndex);

            Calendar v1 = calendars.get(currentPosition % 3);
            v1.switchCalendarType(CalendarAttr.CalendarType.WEEK);
            v1.showDate(date);
            v1.updateWeek(rowIndex);

            refreshCalendarOfWeek(-1);
            refreshCalendarOfWeek(1);
        }
    }

/*    public void notifyMonthDataChanged(CalendarDate date) {
        seedDate = date;
        refreshCalendar();
    }*/

    public void notifyDataChanged(CalendarDate date) {
        //seedDate = date;
        saveSelectedDate(date);
        refreshCalendar();
    }

    public void notifyDataChanged() {
        refreshCalendar();
    }

    private void refreshCalendarOfWeek(int offsetPage){
        Calendar v2 = calendars.get((currentPosition + offsetPage) % 3);
        v2.switchCalendarType(CalendarAttr.CalendarType.WEEK);
        CalendarDate last = date.modifyWeek(offsetPage);
        v2.setSelectedCalendarDate(last);
        v2.showDate(last);
        //v2.setSelectedCalendarDate(last);

        CalendarDate saturday2 = Utils.getSaturday(last);
        CalendarDate sunday2 = Utils.getSunday(date);

        if(offsetPage < 0){
            sunday2 = Utils.getSunday(last.modifyWeek(-1)); //获取上上周的周日，即当前行的开始日期
        }else {
            saturday2 = Utils.getSunday(date);
        }
        int selectIndex = getSelectIndex(offsetPage, saturday2, sunday2, v2.getSelectedCalendarDate());
        v2.updateWeek(selectIndex);
    }

    private void refreshCalendar() {
        if (calendarType == CalendarAttr.CalendarType.WEEK) {
            MonthPager.CURRENT_DAY_INDEX = currentPosition;
            Calendar v1 = calendars.get(currentPosition % 3);
            seedDate = v1.getSeedDate();
            v1.showDate(seedDate);
            v1.updateWeek(v1.getSelectedRowIndex());

            refreshCalendarOfWeek(-1);

            refreshCalendarOfWeek(1);

        } else {
            MonthPager.CURRENT_DAY_INDEX = currentPosition;

            Calendar v1 = calendars.get(currentPosition % 3);//0
            seedDate = v1.getSeedDate();
            setSelectedCalendarInCalendar(v1, seedDate);
            v1.showDate(seedDate);

            Calendar v2 = calendars.get((currentPosition - 1) % 3);//2
            CalendarDate last = seedDate.modifyMonth(-1);
            last.setDay(1);
            setSelectedCalendarInCalendar(v2, last);
            v2.showDate(last);

            Calendar v3 = calendars.get((currentPosition + 1) % 3);//1
            CalendarDate next = seedDate.modifyMonth(1);
            next.setDay(1);
            setSelectedCalendarInCalendar(v3, next);
            v3.showDate(next);
        }
    }

    private void setSelectedCalendarInCalendar(Calendar calendar, CalendarDate seedDate){
        if(date.equalsMonth(seedDate)){
            calendar.setSelectedCalendarDate(date);
        }else {
            if(calendar.getSelectedCalendarDate() == null || seedDate.month != calendar.getSelectedCalendarDate().month){
                calendar.setSelectedCalendarDate(seedDate);
            }
        }
    }

    public CalendarAttr.CalendarType getCalendarType() {
        return calendarType;
    }

    /**
     * 为每一个Calendar实例设置renderer对象
     *
     * @return void
     */
    public void setCustomDayRenderer(IDayRenderer dayRenderer) {
        Calendar c0 = calendars.get(0);
        c0.setDayRenderer(dayRenderer);

        Calendar c1 = calendars.get(1);
        c1.setDayRenderer(dayRenderer.copy());

        Calendar c2 = calendars.get(2);
        c2.setDayRenderer(dayRenderer.copy());
    }

    public void setCurrentPosition(int currentPosition) {
        this.currentPosition = currentPosition;
    }

    public void setOnCalendarTypeChangedListener(OnCalendarTypeChanged onCalendarTypeChangedListener) {
        this.onCalendarTypeChangedListener = onCalendarTypeChangedListener;
    }

    public CalendarAttr.WeekArrayType getWeekArrayType() {
        return weekArrayType;
    }

    public void saveSelectedDateToLastPager(CalendarDate lastSelectedDate) {
        getCalendarViewByPosition(currentPosition - 1).setSelectedCalendarDate(lastSelectedDate);
    }


    public void saveSelectedDateToNextPager(CalendarDate nextSelectedDate) {
        getCalendarViewByPosition(currentPosition + 1).setSelectedCalendarDate(nextSelectedDate);

    }

    public ArrayList<Calendar> getCalendars() {
        return calendars;
    }

    public interface OnCalendarTypeChanged {
        void onCalendarTypeChanged(CalendarAttr.CalendarType type);
    }
}