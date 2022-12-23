package com.ldf.calendar.behavior;


import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.ldf.calendar.Utils;
import com.ldf.calendar.component.CalendarViewAdapter;
import com.ldf.calendar.view.CalendarView;
import com.ldf.calendar.view.MonthPager;
import com.ldf.calendar.view.WrapMonthPager;
import com.ldf.mi.calendar.R;

/**
 * Created by ldf on 17/6/15.
 */

public class MonthPagerBehavior extends CoordinatorLayout.Behavior<WrapMonthPager> {

    public static String TAG = "TestEvent";

    private int top = 0;
    private int touchSlop = 1;
    private int offsetY = 0;

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, WrapMonthPager child, View dependency) {
        return dependency.getId() == R.id.scheduleListWrap;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, WrapMonthPager child, int layoutDirection) {
        parent.onLayoutChild(child, layoutDirection);
        //child.offsetTopAndBottom(top);
        return true;
    }

    private float downX, downY, lastY, lastTop;
    private boolean isVerticalScroll;
    private boolean directionUpa;

    @Override
    public boolean onTouchEvent(CoordinatorLayout parent, WrapMonthPager wrapMonthPager, MotionEvent ev) {

        if (downY > lastTop) {
            return false;
        }

        MonthPager child = wrapMonthPager.getMonthPager();
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
                    if(  Utils.loadTop() > child.getMonthHeightWithIndicator()
                            && Utils.loadTop() <= child.getViewHeightWithIndicator()
                    ){
                        //日程状态，改变日程view的高度
                        int offsetTop = (int) (Utils.loadTop() + ((ev.getY() - lastY)));
                        int saveTop = offsetTop;
                        if(offsetTop > child.getViewHeightWithIndicator()){
                            saveTop(child.getViewHeightWithIndicator());
                        } else if(offsetTop < child.getMonthHeightWithIndicator()){
                            saveTop(child.getMonthHeightWithIndicator());
                        }else {
                            saveTop(offsetTop);
                        }

                        int height = offsetTop-child.getIndicatorHeight();
                        if(height > child.getViewHeight()){
                            height = child.getViewHeight();
                        }
                        if(height < child.getMonthHeight()){
                            height = child.getMonthHeight();
                        }
                        CalendarViewAdapter adapter = (CalendarViewAdapter) child.getAdapter();
                        CalendarView calendar = adapter.getCurrCalendarView();
                        View wrapView = calendar.getChildAt(0);

                        ViewGroup.LayoutParams layoutParams = wrapView.getLayoutParams();
                        if(layoutParams.height != height){
                            //calendar.scheduleRowAllGone();
                            adapter.allPageScheduleRowGone();
                            layoutParams.height = height;
                            wrapView.setLayoutParams(layoutParams);
                        }
                    }else if( Utils.loadTop() <= child.getMonthHeightWithIndicator() &&  Utils.loadTop() > child.getWeekHeightWithIndicator()){
                        //月状态
                        float leftTop = Utils.loadTop() + ((ev.getY() - lastY));
                        if(leftTop > child.getWeekHeightWithIndicator()){
                            saveTop((int) leftTop);
                            Utils.scroll(parent.getChildAt(1), (int) (lastY - ev.getY()), child.getWeekHeightWithIndicator(), child.getMonthHeightWithIndicator());
                        }else {
                            saveTop(child.getWeekHeightWithIndicator());
                            Utils.scroll(parent.getChildAt(1), (int) (lastY - ev.getY()), child.getWeekHeightWithIndicator(), child.getMonthHeightWithIndicator());
                            //Log.d("2342342", "onTouchEvent: 3");
                        }
                    }else if(Utils.loadTop() <= child.getWeekHeightWithIndicator() ){
                        //不能再拖了啊, 只能下滑
                        if(!directionUpa){
                            //Log.d("2342342", "onTouchEvent: 2");
                            float leftTop = Utils.loadTop() + ((ev.getY() - lastY));
                            saveTop((int) leftTop);
                            Utils.scroll(parent.getChildAt(1), (int) (lastY - ev.getY()), child.getWeekHeightWithIndicator(), child.getMonthHeightWithIndicator());
                        }else {
                            Utils.scrollTo(parent,  parent.getChildAt(1), child.getWeekHeightWithIndicator(), 30);
                            return false;
                        }

                    }

                    lastY = ev.getY();
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isVerticalScroll) {
                    test = true;
                   Utils.touchUp(parent, child, directionUpa);
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

    public static boolean test = false;



    @Override
    public boolean onInterceptTouchEvent(CoordinatorLayout parent, WrapMonthPager wrapMonthPager, MotionEvent ev) {

        MonthPager child = wrapMonthPager.getMonthPager();
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
                    isVerticalScroll = true;
                    return true;
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
    private int totalOffsetY = 0;

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, WrapMonthPager wrapMonthPager, View dependency) {
        MonthPager child = wrapMonthPager.getMonthPager();
        CalendarViewAdapter calendarViewAdapter = (CalendarViewAdapter) child.getAdapter();
        CalendarView calendar = calendarViewAdapter.getCurrCalendarView();
        View wrapView = calendar.getChildAt(0);
        //dependency对其依赖的view(本例依赖的view是RecycleView)
        if (dependentViewTop != -1 && Utils.loadTop() <= child.getMonthHeightWithIndicator()) {
            int dy = dependency.getTop() - dependentViewTop;
            int top = wrapView.getTop();
            if (dy > -top) {
                dy = -top;
            }

            if (dy < -top - child.getTopMovableDistance()) {
                dy = -top - child.getTopMovableDistance();
            }

            wrapView.offsetTopAndBottom(dy);
        }

        if( Utils.loadTop() > child.getMonthHeightWithIndicator()
                && Utils.loadTop() <= child.getViewHeightWithIndicator()
                && dependentViewTop != -1
        ){
            //日程状态，改变日程view的高度
            int offsetTop = Utils.loadTop() + dependency.getTop() - dependentViewTop - child.getIndicatorHeight();
            if(offsetTop > child.getViewHeight()){
                offsetTop = child.getViewHeight();
            }
            if(offsetTop < child.getMonthHeight()){
                offsetTop = child.getMonthHeight();
            }

            if(Math.abs(offsetTop - child.getMonthHeight()) < 3){
                offsetTop = child.getMonthHeight();
            }

            CalendarViewAdapter adapter = (CalendarViewAdapter) child.getAdapter();
            adapter.allPageScheduleRowGone();
            ViewGroup.LayoutParams layoutParams = wrapView.getLayoutParams();
            layoutParams.height = offsetTop;
            wrapView.setLayoutParams(layoutParams);
        }
        if(dependentViewTop != -1){
            int dy = dependency.getTop() - dependentViewTop;
            totalOffsetY += dy;
            wrapMonthPager.setIndicatorTranslationY(dependency.getTop() - child.getIndicatorHeight());
        }

        dependentViewTop = dependency.getTop();
        top = child.getTop();

        return true;
        // TODO: 16/12/8 dy为负时表示向上滑动，dy为正时表示向下滑动，dy为零时表示滑动停止
    }
}
