package com.ldf.calendar.behavior;

import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.ldf.calendar.Const;
import com.ldf.calendar.Utils;
import com.ldf.calendar.component.CalendarAttr;
import com.ldf.calendar.component.CalendarViewAdapter;
import com.ldf.calendar.view.Calendar;
import com.ldf.calendar.view.MonthPager;

/**
 * Created by ldf on 17/6/15.
 */

public class MonthPagerBehavior extends CoordinatorLayout.Behavior<MonthPager> {
    private int top = 0;
    private int touchSlop = 1;
    private int offsetY = 0;

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, MonthPager child, View dependency) {
        return dependency instanceof RecyclerView;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, MonthPager child, int layoutDirection) {
        parent.onLayoutChild(child, layoutDirection);
        child.offsetTopAndBottom(top);
        return true;
    }

    private float downX, downY, lastY, lastTop;
    private boolean isVerticalScroll;
    private boolean directionUpa;

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, MonthPager child, MotionEvent ev) {
        if (downY > lastTop) {
            return false;
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;
            case MotionEvent.ACTION_MOVE:
                if (isVerticalScroll) {
                    if (ev.getY() > lastY) {
                        Utils.setScrollToBottom(true);
                        directionUpa = false;   //向下
                    } else {
                        Utils.setScrollToBottom(false);
                        directionUpa = true;    //向上
                    }

                    if( Utils.loadTop() <= child.getViewHeight()
                            &&  Utils.loadTop() > child.getMonthHeight()
                    ){
                        //日程状态，改变日程view的高度
                        if(Utils.loadTop() <= child.getViewHeight()){
                            int offsetTop = (int) (Utils.loadTop() + ((ev.getY() - lastY)));
                            if(offsetTop > child.getViewHeight()){
                                offsetTop = child.getViewHeight();
                            }

                            CalendarViewAdapter adapter = (CalendarViewAdapter) child.getAdapter();
                            Calendar calendar = adapter.getCurrCalendarView();
                            View wrapView = calendar.getChildAt(0);
                            ViewGroup.LayoutParams layoutParams = wrapView.getLayoutParams();
                            layoutParams.height = offsetTop;
                            wrapView.setLayoutParams(layoutParams);
                            ViewGroup.LayoutParams childLayoutParams = child.getLayoutParams();
                            childLayoutParams.height = offsetTop;
                            child.setLayoutParams(childLayoutParams);

                            saveTop(offsetTop);
                        }
                    }else if( Utils.loadTop() <= child.getMonthHeight() &&  Utils.loadTop() > child.getWeekHeight()){
                        //月状态
                        float leftTop = Utils.loadTop() + ((ev.getY() - lastY));
                        if(leftTop > child.getWeekHeight()){
                            saveTop((int) leftTop);
                            Utils.scroll(parent.getChildAt(1), (int) (lastY - ev.getY()), child.getWeekHeight(), child.getMonthHeight());
                            Log.d("2342342", "onTouchEvent: leftTop > child.getWeekHeight() top:" +Utils.loadTop()  + "  getWeekHeight：" + child.getWeekHeight());
                        }else {
                            saveTop(child.getWeekHeight());
                            Utils.scroll(parent.getChildAt(1), (int) (lastY - ev.getY()), child.getWeekHeight(), child.getMonthHeight());
                            Log.d("2342342", "onTouchEvent: else top:" +Utils.loadTop()  + "  getWeekHeight：" + child.getWeekHeight());

                        }
                    }else if(Utils.loadTop() <= child.getWeekHeight() ){
                        //不能再拖了啊, 只能下滑
                        if(!directionUpa){
                            float leftTop = Utils.loadTop() + ((ev.getY() - lastY));
                            saveTop((int) leftTop);
                            Utils.scroll(parent.getChildAt(1), (int) (lastY - ev.getY()), child.getWeekHeight(), child.getMonthHeight());
                        }else {
                            Utils.scrollTo(parent, (RecyclerView) parent.getChildAt(1), child.getWeekHeight(), 30);
                            ((RecyclerView) parent.getChildAt(1)).requestDisallowInterceptTouchEvent(false);
                            Log.d("9999", "onTouchEvent: 11111");
                            return false;
                        }

                    }

                    lastY = ev.getY();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isVerticalScroll) {
                   Utils.touchUp(parent, child, directionUpa);
                    /*child.setScrollable(true);

                    CalendarViewAdapter calendarViewAdapter =
                            (CalendarViewAdapter) child.getAdapter();
                    if (calendarViewAdapter != null) {
                        if (directionUpa) {
                            Utils.setScrollToBottom(true);
                            calendarViewAdapter.switchToWeek(child.getRowIndex());
                            Utils.scrollTo(parent, (RecyclerView) parent.getChildAt(1), child.getWeekHeight(), 300);
                        } else {
                            Utils.setScrollToBottom(false);
                            calendarViewAdapter.switchToMonth();
                            Utils.scrollTo(parent, (RecyclerView) parent.getChildAt(1), child.getViewHeight(), 300);
                        }
                    }*/

                    isVerticalScroll = false;
                    return true;
                }
                break;
        }
        return false;
    }



    private void saveTop(int top) {
        Utils.saveTop(top);
    }

    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, MonthPager child, MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = ev.getX();
                downY = ev.getY();
                lastTop = Utils.loadTop();
                lastY = downY;
                break;
            case MotionEvent.ACTION_MOVE:
                if (downY > lastTop) {
                    return false;
                }
                if (Math.abs(ev.getY() - downY) > 25 && Math.abs(ev.getX() - downX) <= 25
                        && !isVerticalScroll) {
                    if(downY > lastTop){
                        if (ev.getY() > lastY){
                            //向下
                            return false;
                        }else {
                            //向上
                            /*if(Calendar.getCurrCalendarType() != CalendarAttr.CalendarType.WEEK){
                                isVerticalScroll = true;
                                return true;
                            }else {
                                return false;
                            }*/
                            if(Utils.loadTop() != child.getWeekHeight()){
                                isVerticalScroll = true;
                                Log.d("9999", "onTouchEvent: 3333");
                                return true;
                            }else {
                                Log.d("9999", "onTouchEvent: 2222");
                                return false;
                            }
                        }
                    }else {
                        isVerticalScroll = true;
                        return true;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isVerticalScroll) {
                    isVerticalScroll = false;
                    return true;
                }
                break;
        }
        return isVerticalScroll;
    }

    private int dependentViewTop = -1;

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, MonthPager child, View dependency) {
        CalendarViewAdapter calendarViewAdapter = (CalendarViewAdapter) child.getAdapter();
        //dependency对其依赖的view(本例依赖的view是RecycleView)
        if (dependentViewTop != -1 && Utils.loadTop() <= child.getMonthHeight()) {
            int dy = dependency.getTop() - dependentViewTop;
            int top = child.getTop();
            /*if (dy > touchSlop) {
                calendarViewAdapter.switchToMonth();
            } else if (dy < -touchSlop) {
                calendarViewAdapter.switchToWeek(child.getRowIndex());
            }*/

            if (dy > -top) {
                dy = -top;
            }

            if (dy < -top - child.getTopMovableDistance()) {
                dy = -top - child.getTopMovableDistance();
            }

            child.offsetTopAndBottom(dy);
            Log.e("ldf", "onDependentViewChanged = " + dy);

        }

        if( Utils.loadTop() <= child.getViewHeight()
                &&  Utils.loadTop() > child.getMonthHeight()){
            //日程状态，改变日程view的高度
            if(Utils.loadTop() <= child.getViewHeight()){
                int offsetTop = Utils.loadTop() + dependency.getTop() - dependentViewTop;
                if(offsetTop > child.getViewHeight()){
                    offsetTop = child.getViewHeight();
                }
                CalendarViewAdapter adapter = (CalendarViewAdapter) child.getAdapter();
                Calendar calendar = adapter.getCurrCalendarView();
                View wrapView = calendar.getChildAt(0);
                ViewGroup.LayoutParams layoutParams = wrapView.getLayoutParams();
                layoutParams.height = offsetTop;
                wrapView.setLayoutParams(layoutParams);
                ViewGroup.LayoutParams childLayoutParams = child.getLayoutParams();
                childLayoutParams.height = offsetTop;
                child.setLayoutParams(childLayoutParams);
                //saveTop(offsetTop);
            }
        }else if( Utils.loadTop() <= child.getMonthHeight() &&  Utils.loadTop() > child.getWeekHeight()){
            //月状态
          /*  float leftTop = Utils.loadTop() + dependency.getTop() - dependentViewTop;
            if(leftTop > child.getWeekHeight()){
                //saveTop((int) leftTop);
                Utils.scroll(parent.getChildAt(1), (int) (dependentViewTop - dependency.getTop()), child.getWeekHeight(), child.getMonthHeight());
                Log.d("2342342", "onTouchEvent: leftTop > child.getWeekHeight() top:" +Utils.loadTop()  + "  getWeekHeight：" + child.getWeekHeight());
            }else {
                //saveTop(child.getWeekHeight());
                Utils.scroll(parent.getChildAt(1), (int) (dependentViewTop - dependency.getTop()), child.getWeekHeight(), child.getMonthHeight());
                Log.d("2342342", "onTouchEvent: else top:" +Utils.loadTop()  + "  getWeekHeight：" + child.getWeekHeight());

            }*/
        }else if(Utils.loadTop() <= child.getWeekHeight() ){
            //不能再拖了啊, 只能下滑
            /*if(!directionUpa){
                float leftTop = Utils.loadTop() + (dependency.getTop() - dependentViewTop);
                //saveTop((int) leftTop);
                Utils.scroll(parent.getChildAt(1), (int) (dependentViewTop - dependency.getTop()), child.getWeekHeight(), child.getMonthHeight());
            }else {
                Utils.scrollTo(parent, (RecyclerView) parent.getChildAt(1), child.getWeekHeight(), 30);
                ((RecyclerView) parent.getChildAt(1)).requestDisallowInterceptTouchEvent(false);
                Log.d("9999", "onTouchEvent: 11111");
                return false;
            }
*/
        }


        dependentViewTop = dependency.getTop();
        top = child.getTop();

       /* if (offsetY > child.getWeekHeight()) {
            calendarViewAdapter.switchToMonth();
        }
        if (offsetY < -child.getWeekHeight()) {
            calendarViewAdapter.switchToWeek(child.getRowIndex());
        }*/

/*
        if (dependentViewTop > child.getWeekHeight() - 24
                && dependentViewTop < child.getWeekHeight() + 24
                && top > -touchSlop - child.getTopMovableDistance()
                && top < touchSlop - child.getTopMovableDistance()) {
            Utils.setScrollToBottom(true);
            calendarViewAdapter.switchToWeek(child.getRowIndex());
            offsetY = 0;
        }
        if (dependentViewTop > child.getViewHeight() - 24
                && dependentViewTop < child.getViewHeight() + 24
                && top < touchSlop
                && top > -touchSlop) {
            Utils.setScrollToBottom(false);
            calendarViewAdapter.switchToMonth();
            offsetY = 0;
        }
*/

        return true;
        // TODO: 16/12/8 dy为负时表示向上滑动，dy为正时表示向下滑动，dy为零时表示滑动停止
    }
}
