package com.ldf.calendar.model;

import android.util.Log;

import com.ldf.calendar.Utils;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class CalendarDate implements Serializable {
    private static final long serialVersionUID = 1L;
    public int year;
    public int month;  //1~12
    public int day;

    public CalendarDate(String dateStr){
        String dateArr[] = dateStr.split("-");

        initParams(Integer.parseInt(dateArr[0]),Integer.parseInt(dateArr[1]), Integer.parseInt(dateArr[2]));
    }

    public CalendarDate(int year, int month, int day) {
        initParams(year, month, day);
    }

    public CalendarDate() {
        this.year = Utils.getYear();
        this.month = Utils.getMonth();
        this.day = Utils.getDay();
    }

    private void initParams(int year, int month, int day){
        if (month > 12) {
            month = 1;
            year++;
        } else if (month < 1) {
            month = 12;
            year--;
        }
        this.year = year;
        this.month = month;
        this.day = day;
    }

    /**
     * 对比获取最大（最后）的日期
     * @param target
     */
    public CalendarDate getMaxDate(CalendarDate target){
        int res = compareTo(target);
        if(res == 0){
            return this;
        }else if(res < 0){
            return target;
        }else {
            return this;
        }
    }

    /**
     * 对比获取最小（最前）的日期
     * @param target
     */
    public CalendarDate getMinDate(CalendarDate target){
        int res = compareTo(target);
        if(res == 0){
            return this;
        }else if(res < 0){
            return this;
        }else {
            return target;
        }
    }

    public int compareTo(CalendarDate target){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day);
        Calendar targetC = Calendar.getInstance();
        targetC.set(target.year, target.month-1, target.day);

        int res = calendar.compareTo(targetC);
        return res;
    }

    public int compareTo(long timestamp){
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day);
        Calendar targetC = Calendar.getInstance();
        targetC.setTimeInMillis(timestamp);

        int res = calendar.compareTo(targetC);
        return res;
    }



    public int diff(CalendarDate target) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day);

        Calendar targetC = Calendar.getInstance();
        targetC.set(target.year, target.month-1, target.day);

        long diffDays = (calendar.getTimeInMillis() - targetC.getTimeInMillis()) / (1000 * 60 * 60 * 24);
        return (int) diffDays;
    }

    public int diff(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1, day);

        Calendar targetC = Calendar.getInstance();
        targetC.setTimeInMillis(timestamp);

        long diffDays = (calendar.getTimeInMillis() - targetC.getTimeInMillis()) / (1000 * 60 * 60 * 24);
        return (int) diffDays;
    }



    /**
     * 获取当前偏移后的 日期
     * @param days
     * @return
     */
    public CalendarDate offsetNDays(int days){
        if(days == 0){
            return new CalendarDate(year, month, day);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month-1 , day);
        //long time = calendar.getTimeInMillis();// 给定时间与1970 年 1 月 1 日的00:00:00.000的差，以毫秒显示
        //calendar.setTimeInMillis(time + days * 1000 * 60 * 60 * 24);// 用给定的 long值设置此Calendar的当前时间值
        calendar.add(Calendar.DATE, days);
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH)+1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        //Log.d("judgeFutureLoadMoreData", "offsetNDays: " + simpleDateFormat.format(calendar.getTimeInMillis()));
        return new CalendarDate(year, month, day);
    }

    /**
     * 通过修改当前Date对象的天数返回一个修改后的Date
     *
     * @return CalendarDate 修改后的日期
     */
    public CalendarDate modifyDay(int day) {
        int lastMonthDays = Utils.getMonthDays(this.year, this.month - 1);
        int currentMonthDays = Utils.getMonthDays(this.year, this.month);

        CalendarDate modifyDate;
        if (day > currentMonthDays) {
            modifyDate = new CalendarDate(this.year, this.month, this.day);
            Log.e("ldf", "移动天数过大");
        } else if (day > 0) {
            modifyDate = new CalendarDate(this.year, this.month, day);
        } else if (day > 0 - lastMonthDays) {
            modifyDate = new CalendarDate(this.year, this.month - 1, lastMonthDays + day);
        } else {
            modifyDate = new CalendarDate(this.year, this.month, this.day);
            Log.e("ldf", "移动天数过大");
        }
        return modifyDate;
    }

    /**
     * 通过修改当前Date对象的所在周返回一个修改后的Date
     *
     * @return CalendarDate 修改后的日期
     */
    public CalendarDate modifyWeek(int offset) {
        CalendarDate result = new CalendarDate();
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, year);
        c.set(Calendar.MONTH, month - 1);
        c.set(Calendar.DAY_OF_MONTH, day);
        c.add(Calendar.DATE, offset * 7);
        result.setYear(c.get(Calendar.YEAR));
        result.setMonth(c.get(Calendar.MONTH) + 1);
        result.setDay(c.get(Calendar.DATE));
        return result;
    }

    /**
     * 通过修改当前Date对象的所在月返回一个修改后的Date
     *  以当前日期偏移offset个月，如果当前天不在偏移后月份的范围内，则取偏移后月份的最后一天
     * @return CalendarDate 修改后的日期
     */
    public CalendarDate modifyMonth(int offset) {
        /*CalendarDate result = new CalendarDate();
        int addToMonth = this.month + offset;
        if (offset > 0) {
            if (addToMonth > 12) {
                result.setYear(this.year + (addToMonth - 1) / 12);
                result.setMonth(addToMonth % 12 == 0 ? 12 : addToMonth % 12);
            } else {
                result.setYear(this.year);
                result.setMonth(addToMonth);
            }
        } else {
            if (addToMonth == 0) {
                result.setYear(this.year - 1);
                result.setMonth(12);
            } else if (addToMonth < 0) {
                result.setYear(this.year + addToMonth / 12 - 1);
                int month = 12 - Math.abs(addToMonth) % 12;
                result.setMonth(month == 0 ? 12 : month);
            } else {
                result.setYear(this.year);
                result.setMonth(addToMonth == 0 ? 12 : addToMonth);
            }
        }*/
        CalendarDate result = new CalendarDate();
        Calendar lastCalendar = Calendar.getInstance();
        lastCalendar.set(Calendar.YEAR, year);
        lastCalendar.set(Calendar.MONTH, month-1 + offset);
        lastCalendar.set(Calendar.DATE,lastCalendar.getActualMaximum(Calendar.DATE));
        int lastDay = lastCalendar.get(Calendar.DAY_OF_MONTH);
        int offsetDay = day;
        if(lastDay < day){
            offsetDay = lastDay;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(lastCalendar.getTimeInMillis());
        calendar.set(Calendar.DAY_OF_MONTH, offsetDay);
        result.setYear(calendar.get(Calendar.YEAR));
        result.setMonth(calendar.get(Calendar.MONTH) + 1);
        result.setDay(calendar.get(Calendar.DAY_OF_MONTH));

        return result;
    }

    @Override
    public String toString() {
        String monthStr = "" + month;
        String datStr = "" + day;
        if(month < 10){
            monthStr = "0"+month;
        }
        if(day < 10){
            datStr = "0"+day;
        }
        return year + "-" + monthStr + "-" + datStr;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public boolean equals(CalendarDate date) {
        if (date == null) {
            return false;
        }
        if (this.getYear() == date.getYear()
                && this.getMonth() == date.getMonth()
                && this.getDay() == date.getDay()) {
            return true;
        }
        return false;
    }

    public boolean equalsMonth(CalendarDate date){
        return year == date.year && month == date.month;
    }

    public CalendarDate cloneSelf() {
        return new CalendarDate(year, month, day);
    }

}