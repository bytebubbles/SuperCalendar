package com.ldf.calendar.component;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.viewpager.widget.PagerAdapter;

import com.ldf.calendar.interf.OnAdapterSelectListener;
import com.ldf.calendar.interf.IViewRenderer;
import com.ldf.calendar.interf.OnSelectDateListener;
import com.ldf.calendar.Utils;
import com.ldf.calendar.view.MonthPager;
import com.ldf.calendar.model.CalendarDate;
import com.ldf.calendar.view.CalendarView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CalendarViewAdapter extends PagerAdapter {

    private static CalendarDate date = new CalendarDate();
    private static CalendarDate preDate = null;
    private ArrayList<CalendarView> calendars = new ArrayList<>();
    private int currentPosition = MonthPager.CURRENT_DAY_INDEX;
    private CalendarAttr.CalendarType calendarType = CalendarView.getCurrCalendarType();
    //private int rowCount = 0;
    private CalendarDate seedDate;
    private CalendarDate todayDate = new CalendarDate();
    private OnCalendarTypeChanged onCalendarTypeChangedListener;
    private OnSelectDateListener onSelectDateListener;
    //周排列方式 1：代表周日显示为本周的第一天
    //0:代表周一显示为本周的第一天
    private CalendarAttr.WeekArrayType weekArrayType = CalendarAttr.WeekArrayType.Monday;

    public CalendarViewAdapter(Context context,
                               OnSelectDateListener onSelectDateListener,
                               CalendarAttr.WeekArrayType weekArrayType,
                               IViewRenderer dayView,
                               IViewRenderer ScheduleView
                               ) {
        super();
        this.weekArrayType = weekArrayType;
        init(context, onSelectDateListener);
        setCustomDayRenderer(dayView, ScheduleView);
    }

    public static void saveSelectedDate(CalendarDate calendarDate) {
        preDate = date;
        date = calendarDate;
    }

    public static CalendarDate loadSelectedDate() {
        return date;
    }

    private void init(Context context, OnSelectDateListener onSelectDateListener) {
        this.onSelectDateListener = onSelectDateListener;
        saveSelectedDate(new CalendarDate());
        //初始化的种子日期为今天
        seedDate = new CalendarDate();
        //seedDate.setDay(1);
        for (int i = 0; i < 3; i++) {
            CalendarAttr calendarAttr = new CalendarAttr();
            calendarAttr.setCalendarType(calendarType);
            calendarAttr.setWeekArrayType(weekArrayType);
            CalendarView calendar = new CalendarView(context, onSelectDateListener, calendarAttr);
            calendar.setOnAdapterSelectListener(new OnAdapterSelectListener() {
                @Override
                public void cancelSelectState() {
                    //cancelOtherSelectState();
                }

                @Override
                public void updateSelectState() {
                    //invalidateCurrentCalendar();
                    updateSelectStateOfWeek();
                }
            });
            calendars.add(calendar);
        }
    }

    private void updateSelectStateOfWeek(){
        if(preDate.month != date.month && CalendarView.getCurrCalendarType() == CalendarAttr.CalendarType.WEEK){
            final CalendarView calendar = getCurrCalendarView();
            final CalendarDate current = date.modifyWeek(0);
            calendar.setSelectedCalendarDate(date);
            final CalendarDate saturday = Utils.getSaturday(current);
            final CalendarDate sunday;
            sunday = Utils.getSunday(current.modifyWeek(-1));
            LinearLayout linearLayout = (LinearLayout) calendar.getChildAt(0);
            linearLayout.requestLayout();
            calendar.showDate(current); //默认用选中日期创建当前月数据
            linearLayout.post(new Runnable() {
                @Override
                public void run() {
                    int selectIndex = getSelectIndex(0, saturday, sunday, current);
                    calendar.updateWeekImmediately(selectIndex);
                }
            });


            refreshCalendarOfWeek(-1);
            refreshCalendarOfWeek(1);
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
        CalendarView calendar = calendars.get(position % calendars.size());
        calendar.setMonthPager((MonthPager) container);
        if (calendarType == CalendarAttr.CalendarType.MONTH
                || calendarType == CalendarAttr.CalendarType.SCHEDULE_MONTH) {
            CalendarDate current = date.modifyMonth(position - currentPosition);
            //current.setDay(1);//每月的种子日期都是1号
            setSelectedCalendarInCalendar(calendar, current);
            calendar.showDate(current);
        } else {
            CalendarDate current = date.modifyWeek(position - currentPosition);
            calendar.setSelectedCalendarDate(current);
            CalendarDate saturday = Utils.getSaturday(current);
            CalendarDate sunday;
            if(position - currentPosition == 0){
                sunday = Utils.getSunday(current.modifyWeek(-1));
            }else if(position - currentPosition < 0){
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
            if(offsetPage == 0 ){
                int day = nextSelectDate.day;
                int row = (day / 7);
                int index = day % 7;
                int offsetIndex = Utils.getFirstDayWeekPosition(nextSelectDate.year,nextSelectDate.month ,CalendarAttr.WeekArrayType.Sunday);
                if (index + offsetIndex > 7){
                    row +=1;
                }
                selectIndex = row;
            }else if(offsetPage > 0){
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
                if (currFirstDate.equalsMonth(currLastDate) && !currFirstDate.equalsMonth(firstDate)){
                    //倒数第一行，刚好当页的第一条和上一页的最后一条是两个不同的月份
                    selectIndex = Utils.totalRowCount(seedDate.year, seedDate.month, weekArrayType)-1;
                }else if(currFirstDate.equalsMonth(lastDate) && currLastDate.equalsMonth(firstDate)){
                    selectIndex -=1;
                }else {
                    //倒数第二行
                    selectIndex = Utils.totalRowCount(seedDate.year, seedDate.month, weekArrayType)-1-1;
                }
            }
        }else {
            //并存行。即该行同时存在当月和下页月
            //如果选中的日期月份和当前的页月份一至则 为 最后一行，否则 第二页的第0行
            if(CalendarView.getCurrCalendarType() == CalendarAttr.CalendarType.WEEK && offsetPage == 0){
                if(preDate.month > nextSelectDate.month){
                    //上个月最后一行
                    selectIndex = Utils.totalRowCount(seedDate.year, seedDate.month, weekArrayType)-1;
                }else {
                    //下个月第一行，注意：这里没有 preDate.month == nextSelectDate.month 的情况
                    selectIndex = 0;
                }
            }else {
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

    public ArrayList<CalendarView> getPagers() {
        return calendars;
    }

    public CalendarDate getFirstVisibleDate() {
        return calendars.get(currentPosition % 3).getFirstDate();
    }

    public CalendarDate getLastVisibleDate() {
        return calendars.get(currentPosition % 3).getLastDate();
    }

    public CalendarView getCurrCalendarView(){
        return calendars.get(currentPosition % 3);
    }

    public CalendarView getCalendarViewByPosition(int pagerPosition){
        return calendars.get(pagerPosition % 3);
    }

    public CalendarView getCalendarViewNext(){
        return getCalendarViewByPosition(currentPosition + 1);
    }

    public CalendarView getCalendarViewPre(){
        return getCalendarViewByPosition(currentPosition - 1);
    }

    public void allPageScheduleRowGone(){
        for(CalendarView calendarView : calendars){
            calendarView.scheduleRowAllGone();
        }
    }

    public void allPageScheduleRowShow(){
        for(CalendarView calendarView : calendars){
            calendarView.scheduleRowAllShow();
        }
    }

    public void cancelOtherSelectState() {
        for (int i = 0; i < calendars.size(); i++) {
            CalendarView calendar = calendars.get(i);
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
        Utils.setMarkData(markData);
        //notifyDataChanged();
    }

    public void setScheduleData(HashMap<String, List> scheduleData){
        Utils.setScheduleData(scheduleData);
        notifyDataChanged();
    }

    public HashMap<String, List> loadScheduleData(){
        return Utils.loadScheduleData();
    }

    public void switchToSchedule(){
        Log.d("switchTo", "switchToSchedule: ");
        if (calendars != null && calendars.size() > 0 && calendarType != CalendarAttr.CalendarType.SCHEDULE_MONTH) {
            if (onCalendarTypeChangedListener != null) {
                onCalendarTypeChangedListener.onCalendarTypeChanged(CalendarAttr.CalendarType.SCHEDULE_MONTH);
            }

            MonthPager.CURRENT_DAY_INDEX = currentPosition;
            CalendarView v = calendars.get(currentPosition % 3);//0
            seedDate = v.getSeedDate();

            CalendarView v1 = calendars.get(currentPosition % 3);//0

            CalendarView v2 = calendars.get((currentPosition - 1) % 3);//2

            CalendarView v3 = calendars.get((currentPosition + 1) % 3);//1
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
                //last.setDay(1);
                v2.showDate(last);

                v3.switchCalendarType(CalendarAttr.CalendarType.SCHEDULE_MONTH);
                CalendarDate next = seedDate.modifyMonth(1);
                //next.setDay(1);
                v3.showDate(next);
            }
            v1.resetOffsetY();
            v2.resetOffsetY();
            v3.resetOffsetY();
        }
    }

    public void switchToMonth() {
        allPageScheduleRowGone();
        Log.d("switchTo", "switchToMonth: ");
        if (calendars != null && calendars.size() > 0 && calendarType != CalendarAttr.CalendarType.MONTH) {
            if (onCalendarTypeChangedListener != null) {
                onCalendarTypeChangedListener.onCalendarTypeChanged(CalendarAttr.CalendarType.MONTH);
            }
            calendarType = CalendarAttr.CalendarType.MONTH;
            MonthPager.CURRENT_DAY_INDEX = currentPosition;
            CalendarView v = calendars.get(currentPosition % 3);//0
            seedDate = v.getSeedDate();

            CalendarView v1 = calendars.get(currentPosition % 3);//0
            v1.switchCalendarType(CalendarAttr.CalendarType.MONTH);
            v1.showDate(seedDate);

            CalendarView v2 = calendars.get((currentPosition - 1) % 3);//2
            v2.switchCalendarType(CalendarAttr.CalendarType.MONTH);
            CalendarDate last = seedDate.modifyMonth(-1);
            //last.setDay(1);
            setSelectedCalendarInCalendar(v2, last);
            v2.showDate(last);
            v2.resetOffsetY();

            CalendarView v3 = calendars.get((currentPosition + 1) % 3);//1
            v3.switchCalendarType(CalendarAttr.CalendarType.MONTH);
            CalendarDate next = seedDate.modifyMonth(1);
            //next.setDay(1);
            setSelectedCalendarInCalendar(v3, next);
            v3.showDate(next);
            v3.resetOffsetY();
        }
    }

    public void switchToWeek(int rowIndex) {
        allPageScheduleRowGone();
        Log.d("switchTo", "switchToWeek: ");
        //int rowCount = rowIndex;
        if (calendars != null && calendars.size() > 0 && calendarType != CalendarAttr.CalendarType.WEEK) {
            if (onCalendarTypeChangedListener != null) {
                onCalendarTypeChangedListener.onCalendarTypeChanged(CalendarAttr.CalendarType.WEEK);
            }
            calendarType = CalendarAttr.CalendarType.WEEK;
            CalendarView.setCurrCalendarType(calendarType);
            MonthPager.CURRENT_DAY_INDEX = currentPosition;

            CalendarView v = calendars.get(currentPosition % 3);
            seedDate = v.getSeedDate();
            //v.getSelectedRowIndex();
            v.setSelectedRowIndex(rowIndex);

            CalendarView v1 = calendars.get(currentPosition % 3);
            v1.switchCalendarType(CalendarAttr.CalendarType.WEEK);
            v1.showDate(date);
            v1.updateWeek(rowIndex);

            refreshCalendarOfWeek(-1);
            refreshCalendarOfWeek(1);
        }
    }

    public void switchScheduleToMonth(){
        MonthPager child = getCurrCalendarView().getMonthPager();
        CoordinatorLayout parent = (CoordinatorLayout) getCurrCalendarView().getMonthPager().getParent().getParent();
        View wrapView = getCurrCalendarView().getChildAt(0);
        Utils.scrollTo2(parent,  parent.getChildAt(1), child, wrapView,child.getMonthHeightWithIndicator(), 300);
        switchToMonth();
        CalendarView.setCurrCalendarType(CalendarAttr.CalendarType.MONTH);
    }
/*    public void notifyMonthDataChanged(CalendarDate date) {
        seedDate = date;
        refreshCalendar();
    }*/

    public void locationToday(boolean initiative){
        if(date.toString() == getTodayDate().toString()) return;
        selectDate(getTodayDate(), initiative);
    }

    public void selectDate(CalendarDate date, boolean initiative){
        notifyDataChanged(date);
        if(onSelectDateListener != null){
            onSelectDateListener.onSelectDate(date, initiative);
        }
    }

    public void resetOtherPagerSelectCalendarDate(CalendarDate date){
        if(calendarType != CalendarAttr.CalendarType.WEEK){
            CalendarView v2 = calendars.get((currentPosition - 1) % 3);//2
            CalendarDate last = date.modifyMonth(-1);
            //last.setDay(1);
            setSelectedCalendarInCalendar(v2, last);
            v2.showDate(last);

            CalendarView v3 = calendars.get((currentPosition + 1) % 3);//1
            CalendarDate next = date.modifyMonth(1);
            //next.setDay(1);
            setSelectedCalendarInCalendar(v3, next);
            v3.showDate(next);
        }

    }

    public void notifyDataChanged(CalendarDate date) {
        //seedDate = date;
        saveSelectedDate(date);
        CalendarView v1 = getCurrCalendarView();
        v1.setSelectedCalendarDate(date);
        //v1.setSelectedRowIndex();
        v1.setSeedDate(date);

        refreshCalendar();
    }

    public void notifyDataChanged() {
        refreshCalendar();
    }

    private void refreshCalendarOfWeek(int offsetPage){
        CalendarView v2 = calendars.get((currentPosition + offsetPage) % 3);
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
            CalendarView v1 = calendars.get(currentPosition % 3);
            seedDate = v1.getSeedDate();
            if(seedDate == null) return;
            v1.showDate(seedDate);
            //v1.updateWeek(v1.getSelectedRowIndex());
            v1.updateWeekImmediately(v1.getSelectedRowIndex());
            refreshCalendarOfWeek(-1);

            refreshCalendarOfWeek(1);

        } else {
            MonthPager.CURRENT_DAY_INDEX = currentPosition;

            CalendarView v1 = calendars.get(currentPosition % 3);//0
            seedDate = v1.getSelectedCalendarDate();
            if(seedDate == null) return;
            setSelectedCalendarInCalendar(v1, seedDate);
            v1.showDate(seedDate);

            CalendarView v2 = calendars.get((currentPosition - 1) % 3);//2
            CalendarDate last = seedDate.modifyMonth(-1);
            //last.setDay(1);
            setSelectedCalendarInCalendar(v2, last);
            v2.showDate(last);

            CalendarView v3 = calendars.get((currentPosition + 1) % 3);//1
            CalendarDate next = seedDate.modifyMonth(1);
            //next.setDay(1);
            setSelectedCalendarInCalendar(v3, next);
            v3.showDate(next);
        }
    }

    private void setSelectedCalendarInCalendar(CalendarView calendar, CalendarDate seedDate){
        calendar.setSelectedCalendarDate(seedDate);
        /*if(date.equalsMonth(seedDate)){
            calendar.setSelectedCalendarDate(date);
        }else {
            if(calendar.getSelectedCalendarDate() == null || seedDate.month != calendar.getSelectedCalendarDate().month){
                calendar.setSelectedCalendarDate(seedDate);
            }
        }*/
    }

    public CalendarAttr.CalendarType getCalendarType() {
        return calendarType;
    }

    public CalendarDate getTodayDate() {
        return todayDate;
    }

    /**
     * 为每一个Calendar实例设置renderer对象
     *
     * @return void
     */
    public void setCustomDayRenderer(IViewRenderer dayRenderer,IViewRenderer scheduleView) {
        CalendarView c0 = calendars.get(0);
        c0.setDayRenderer(dayRenderer);
        c0.setScheduleRenderer(scheduleView);

        CalendarView c1 = calendars.get(1);
        c1.setDayRenderer(dayRenderer.copy());
        c1.setScheduleRenderer(scheduleView.copy());

        CalendarView c2 = calendars.get(2);
        c2.setDayRenderer(dayRenderer.copy());
        c2.setScheduleRenderer(scheduleView.copy());
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

    public ArrayList<CalendarView> getCalendars() {
        return calendars;
    }

    public interface OnCalendarTypeChanged {
        void onCalendarTypeChanged(CalendarAttr.CalendarType type);
    }
}