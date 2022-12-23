package com.ldf.calendar.component;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;

import com.ldf.calendar.Const;
import com.ldf.calendar.Utils;
import com.ldf.calendar.interf.IViewRenderer;
import com.ldf.calendar.interf.OnSelectDateListener;
import com.ldf.calendar.model.CalendarDate;
import com.ldf.calendar.view.CalendarView;
import com.ldf.calendar.view.Day;
import com.ldf.calendar.view.RowCalendarView;
import com.ldf.calendar.view.RowScheduleView;
import com.ldf.calendar.view.Week;

/**
 * Created by ldf on 17/6/26.
 */

public class CalendarRenderer {
    private Week weeks[] = new Week[Const.TOTAL_ROW];    // 行数组，每个元素代表一行
    private CalendarView calendar;
    private CalendarAttr attr;
    private IViewRenderer dayRenderer;
    private IViewRenderer scheduleRenderer;
    private Context context;
    private OnSelectDateListener onSelectDateListener;    // 单元格点击回调事件
    private CalendarDate seedDate; //种子日期
    private CalendarDate selectedDate; //被选中的日期
    private int selectedRowIndex = 0;
    private boolean isFullRow = true;

    public CalendarRenderer(CalendarView calendar, CalendarAttr attr, Context context) {
        this.calendar = calendar;
        this.attr = attr;
        this.context = context;
    }

    /**
     * 使用dayRenderer绘制每一天
     *
     * @return void
     */
    private void draw() {
        int totalRow = Const.TOTAL_ROW;
        if(!isFullRow){
            totalRow = calendar.getTotalRow();
        }
        for (int row = 0; row < totalRow; row++) {
            int calendarIndex = row * 2;
            RowCalendarView calendarView = (RowCalendarView) ((ViewGroup)calendar.getChildAt(0)).getChildAt(calendarIndex);

            calendarView.drawDay(weeks[row].days,dayRenderer, calendar.getWeekHeight());

            int scheduleIndex = row * 2 + 1;
            RowScheduleView scheduleView = (RowScheduleView) ((ViewGroup)calendar.getChildAt(0)).getChildAt(scheduleIndex);
            scheduleView.drawDay(weeks[row].days, scheduleRenderer, calendar.getWeekHeight());
            //calendarView.invalidate();
            /*
            if (weeks[row] != null) {
                for (int col = 0; col < Const.TOTAL_COL; col++) {
                    if (weeks[row].days[col] != null) {
                        dayRenderer.drawDay(canvas, weeks[row].days[col]);
                    }
                }
            }*/
        }
    }

    /**
     * 点击某一天时刷新这一天的状态
     *
     * @return void
     */
    public void onClickDate(int col, int row) {
        if (col >= Const.TOTAL_COL || row >= calendar.getTotalCol())
            return;
        if (weeks[row] != null) {
            if (attr.getCalendarType() == CalendarAttr.CalendarType.MONTH
                || attr.getCalendarType() == CalendarAttr.CalendarType.SCHEDULE_MONTH) {
                if (weeks[row].days[col].getState() == State.CURRENT_MONTH) {
                    weeks[row].days[col].setState(State.SELECT);
                    selectedDate = weeks[row].days[col].getDate();
                    CalendarViewAdapter.saveSelectedDate(selectedDate);
                    onSelectDate(selectedDate);
                    //seedDate = selectedDate;
                } else if (weeks[row].days[col].getState() == State.PAST_MONTH) {
                    CalendarDate tempSelectedDate = weeks[row].days[col].getDate();
                    calendar.saveSelectedDateToLastPager(tempSelectedDate);
                    //CalendarViewAdapter.saveSelectedDate(tempSelectedDate);
                    if(CalendarView.getCurrCalendarType() == CalendarAttr.CalendarType.WEEK){
                        CalendarViewAdapter.saveSelectedDate(tempSelectedDate);
                    }else {
                        calendar.getMonthPager().selectOtherMonth(-1);
                        onSelectOtherMonth(-1);
                    }
                    onSelectDate(tempSelectedDate);
                } else if (weeks[row].days[col].getState() == State.NEXT_MONTH) {
                    CalendarDate tempSelectedDate = weeks[row].days[col].getDate();
                    //CalendarViewAdapter.saveSelectedDate(tempSelectedDate);
                    calendar.saveSelectedDateToNextPager(tempSelectedDate);
                    if(CalendarView.getCurrCalendarType() == CalendarAttr.CalendarType.WEEK){
                        CalendarViewAdapter.saveSelectedDate(tempSelectedDate);
                    }else {
                        calendar.getMonthPager().selectOtherMonth(1);
                        onSelectOtherMonth(1);
                    }
                    onSelectDate(tempSelectedDate);
                }
            } else {
                weeks[row].days[col].setState(State.SELECT);
                selectedDate = weeks[row].days[col].getDate();
                CalendarViewAdapter.saveSelectedDate(selectedDate);
                onSelectDate(selectedDate);
                //seedDate = selectedDate;
            }
            selectedRowIndex = row;
            draw();
        }

    }

    private void onSelectDate(CalendarDate selectedDate){
        if(onSelectDateListener != null){
            onSelectDateListener.onSelectDate(selectedDate, true);
        }
    }

    private void onSelectOtherMonth(int offset){
        if (onSelectDateListener != null){
            onSelectDateListener.onSelectOtherMonth(offset);
        }
    }

    /**
     * 刷新指定行的周数据
     *
     * @param rowIndex  参数月所在年
     * @return void
     */
    public void updateWeek(int rowIndex) {

        /*CalendarDate currentWeekLastDay;
        CalendarDate tempDate = selectedDate;
        if(tempDate == null){
            tempDate = seedDate;
        }
        if (attr.getWeekArrayType() == CalendarAttr.WeekArrayType.Sunday) {
            currentWeekLastDay = Utils.getSaturday(tempDate);
        } else {
            currentWeekLastDay = Utils.getSunday(tempDate);
        }
        int day = currentWeekLastDay.day;
        for (int i = Const.TOTAL_COL - 1; i >= 0; i--) {
            CalendarDate date = currentWeekLastDay.modifyDay(day);
            if (weeks[rowIndex] == null) {
                weeks[rowIndex] = new Week(rowIndex);
            }
            if (weeks[rowIndex].days[i] != null) {
                if (date.equals(selectedDate)) {
                    weeks[rowIndex].days[i].setState(State.SELECT);
                    weeks[rowIndex].days[i].setDate(date);
                } else {
                    weeks[rowIndex].days[i].setState(State.CURRENT_MONTH);
                    weeks[rowIndex].days[i].setDate(date);
                }
            } else {
                if (date.equals(selectedDate)) {
                    weeks[rowIndex].days[i] = new Day(State.SELECT, date, rowIndex, i);
                } else {
                    weeks[rowIndex].days[i] = new Day(State.CURRENT_MONTH, date, rowIndex, i);
                }
            }
            day--;
        }*/

    }

    /**
     * 填充月数据
     * 注意: 周模式下 如果选的是 当月的日期，但是 seedDate 为下月的，需要转为当月的
     * @return void
     */
    private void instantiateMonth() {
        int lastMonthDays = Utils.getMonthDays(seedDate.year, seedDate.month - 1);    // 上个月的天数
        int currentMonthDays = Utils.getMonthDays(seedDate.year, seedDate.month);    // 当前月的天数
        int firstDayPosition = Utils.getFirstDayWeekPosition(
                seedDate.year,
                seedDate.month,
                attr.getWeekArrayType());
        Log.e("ldf","firstDayPosition = " + firstDayPosition);
        calendar.setTotalRow(Utils.totalRowCount(seedDate.year, seedDate.month, attr.getWeekArrayType()));
       /* int  fiveRowGrid = 5 * 7;
        int diff = fiveRowGrid - currentMonthDays;
        if(firstDayPosition <= diff){
            //5行
            calendar.setTotalRow(5);
        }else {
            //6行
            calendar.setTotalRow(6);
        }*/
        //weeks = new Week[calendar.getTotalRow()];
        int totalRow = Const.TOTAL_ROW;
        if(!isFullRow){
            totalRow = calendar.getTotalRow();
        }
        int day = 0;
        for (int row = 0; row < totalRow; row++) {
            day = fillWeek(lastMonthDays, currentMonthDays, firstDayPosition, day, row);
        }
    }

    public CalendarDate getFirstDate() {
        Week week = weeks[0];
        Day day = week.days[0];
        return day.getDate();
    }

    public CalendarDate getLastDate() {
        Week week = weeks[weeks.length - 1];
        Day day = week.days[week.days.length - 1];
        return day.getDate();
    }

    /**
     * 填充月中周数据
     *
     * @return void
     */
    private int fillWeek(int lastMonthDays,
                         int currentMonthDays,
                         int firstDayWeek,
                         int day,
                         int row) {
        for (int col = 0; col < Const.TOTAL_COL; col++) {
            int position = col + row * Const.TOTAL_COL;// 单元格位置
            if (position >= firstDayWeek && position < firstDayWeek + currentMonthDays) {
                day++;
                fillCurrentMonthDate(day, row, col);
            } else if (position < firstDayWeek) {
                instantiateLastMonth(lastMonthDays, firstDayWeek, row, col, position);
            } else if (position >= firstDayWeek + currentMonthDays) {
                instantiateNextMonth(currentMonthDays, firstDayWeek, row, col, position);
            }
        }
        return day;
    }

    private void fillCurrentMonthDate(int day, int row, int col) {
        CalendarDate date = seedDate.modifyDay(day);
        if (weeks[row] == null) {
            weeks[row] = new Week(row);
        }
        if (weeks[row].days[col] != null) {
            if (date.equals(selectedDate)) {
                weeks[row].days[col].setDate(date);
                weeks[row].days[col].setState(State.SELECT);
            } else {
                weeks[row].days[col].setDate(date);
                weeks[row].days[col].setState(State.CURRENT_MONTH);
            }
        } else {
            if (date.equals(selectedDate)) {
                weeks[row].days[col] = new Day(State.SELECT, date, row, col);
            } else {
                weeks[row].days[col] = new Day(State.CURRENT_MONTH, date, row, col);
            }
        }
        if (date.equals(selectedDate) ) {
            selectedRowIndex = row;
        }
    }

    private void instantiateNextMonth(int currentMonthDays,
                                      int firstDayWeek,
                                      int row,
                                      int col,
                                      int position) {
        CalendarDate date = new CalendarDate(
                seedDate.year,
                seedDate.month + 1,
                position - firstDayWeek - currentMonthDays + 1);
        if (weeks[row] == null) {
            weeks[row] = new Week(row);
        }
        if (weeks[row].days[col] != null) {
            weeks[row].days[col].setDate(date);
            weeks[row].days[col].setState(State.NEXT_MONTH);
        } else {
            weeks[row].days[col] = new Day(State.NEXT_MONTH, date, row, col);
        }
        // TODO: 17/6/27  当下一个月的天数大于七时，说明该月有六周
//        if(position - firstDayWeek - currentMonthDays + 1 >= 7) { //当下一个月的天数大于七时，说明该月有六周
//        }
    }

    private void instantiateLastMonth(int lastMonthDays, int firstDayWeek, int row, int col, int position) {
        CalendarDate date = new CalendarDate(
                seedDate.year,
                seedDate.month - 1,
                lastMonthDays - (firstDayWeek - position - 1));
        if (weeks[row] == null) {
            weeks[row] = new Week(row);
        }
        if (weeks[row].days[col] != null) {
            weeks[row].days[col].setDate(date);
            weeks[row].days[col].setState(State.PAST_MONTH);
        } else {
            weeks[row].days[col] = new Day(State.PAST_MONTH, date, row, col);
        }
    }

    /**
     * 根据种子日期孵化出本日历牌的数据
     *
     * @return void
     */
    public void showDate(CalendarDate seedDate) {
        /*if (seedDate != null) {
            if(Calendar.getCurrCalendarType() == CalendarAttr.CalendarType.WEEK
                && seedDate.month != selectedDate.month){
                this.seedDate = selectedDate;
            }else {
                this.seedDate = seedDate;
            }

        } else {
            this.seedDate = new CalendarDate();
        }*/
       /* if(selectedDate == null){
            selectedDate = seedDate;
        }*/
        this.seedDate = seedDate;
        update();
    }

    public void setSeedDate(CalendarDate seedDate) {
        this.seedDate = seedDate;
    }

    public void update() {
        instantiateMonth();
        draw();
        //calendar.invalidate();
        //calendar.initLayout();
    }


    public CalendarDate getSeedDate() {
        return this.seedDate;
    }

    public void cancelSelectState() {
        int totalRow = Const.TOTAL_ROW;
        if(!isFullRow){
            totalRow = calendar.getTotalRow();
        }
        for (int i = 0; i < totalRow; i++) {
            if (weeks[i] != null) {
                for (int j = 0; j < Const.TOTAL_COL; j++) {
                    if (weeks[i].days[j].getState() == State.SELECT) {
                        weeks[i].days[j].setState(State.CURRENT_MONTH);
                        resetSelectedRowIndex();
                        break;
                    }
                }
            }
        }
    }

    public void resetSelectedRowIndex() {
        selectedRowIndex = 0;
    }

    public int getSelectedRowIndex() {
        return selectedRowIndex;
    }

    public void setSelectedRowIndex(int selectedRowIndex) {
        this.selectedRowIndex = selectedRowIndex;
    }

    public CalendarView getCalendar() {
        return calendar;
    }

    public void setCalendar(CalendarView calendar) {
        this.calendar = calendar;
    }

    public CalendarAttr getAttr() {
        return attr;
    }

    public void setAttr(CalendarAttr attr) {
        this.attr = attr;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public void setOnSelectDateListener(OnSelectDateListener onSelectDateListener) {
        this.onSelectDateListener = onSelectDateListener;
    }

    public void setDayRenderer(IViewRenderer dayRenderer) {
        this.dayRenderer = dayRenderer;
    }

    public void setScheduleRenderer(IViewRenderer scheduleRenderer) {
        this.scheduleRenderer = scheduleRenderer;
    }

    public Week[] getWeeks() {
        return weeks;
    }

    public void setSelectedDate(CalendarDate selectedDate) {
        this.selectedDate = selectedDate;
        //draw();
    }

    public CalendarDate getSelectedCalendarDate() {
        return selectedDate;
    }
}
