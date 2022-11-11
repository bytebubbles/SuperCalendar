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
import com.ldf.calendar.view.WrapMonthPager;

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
        return dependency instanceof RecyclerView;
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
                    //Log.d(TAG, "MonthPagerBehavior- onTouchEvent: ev:" + ev.getAction() + " Y: " + ev.getY() + " isVerticalScroll:" + isVerticalScroll);
                    //Log.d(TAG, "MonthPagerBehavior- onTouchEvent: ev:" + Utils.loadTop() + " Y: " + child.getMonthHeight() + " isVerticalScroll:" + isVerticalScroll);
                    Log.d(TAG, "MonthPagerBehavior- onTouchEvent: Utils.loadTop(): " + Utils.loadTop() +" child.getMonthHeightWithIndicator():" + child.getMonthHeightWithIndicator() + " getViewHeightWithIndicator:" + child.getViewHeightWithIndicator());

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
                        //Log.e(TAG, "MonthPagerBehavior- onTouchEvent: offsetTop:" + offsetTop + " child.getViewHeight():" +child.getViewHeight() + " getMonthHeight():" +child.getMonthHeight() + " getMonthHeightWithIndicator: " + child.getMonthHeightWithIndicator());
                        CalendarViewAdapter adapter = (CalendarViewAdapter) child.getAdapter();
                        Calendar calendar = adapter.getCurrCalendarView();
                        View wrapView = calendar.getChildAt(0);

                        ViewGroup.LayoutParams layoutParams = wrapView.getLayoutParams();
                        if(layoutParams.height != height){
                            //Log.e(TAG, "MonthPagerBehavior- onTouchEvent: offsetTop:" + offsetTop + " child.getViewHeight():" +child.getViewHeight() + " getMonthHeight():" +child.getMonthHeight() + " layoutParams.height: " + layoutParams.height);

                            layoutParams.height = height;
                            wrapView.setLayoutParams(layoutParams);
                        }


                           /* ViewGroup.LayoutParams childLayoutParams = child.getLayoutParams();
                            childLayoutParams.height = offsetTop;
                            child.setLayoutParams(childLayoutParams);*/

                           /* ViewGroup.LayoutParams layoutParams1 = wrapMonthPager.getLayoutParams();
                            layoutParams1.height = offsetTop;
                            wrapMonthPager.setLayoutParams(layoutParams1);*/


                    }else if( Utils.loadTop() <= child.getMonthHeightWithIndicator() &&  Utils.loadTop() > child.getWeekHeightWithIndicator()){
                        //月状态
                        float leftTop = Utils.loadTop() + ((ev.getY() - lastY));
                        if(leftTop > child.getWeekHeightWithIndicator()){
                            saveTop((int) leftTop);
                            //Utils.scrollTo(parent, (RecyclerView) parent.getChildAt(1), child.getWeekHeight(), 30);
                            Utils.scroll(parent.getChildAt(1), (int) (lastY - ev.getY()), child.getWeekHeightWithIndicator(), child.getMonthHeightWithIndicator());
                            Log.d("2342342", "onTouchEvent: leftTop > child.getWeekHeight() top:" +Utils.loadTop()  + "  getWeekHeight：" + child.getWeekHeightWithIndicator());
                            Log.d("2342342", "onTouchEvent: 4");
                        }else {
                            saveTop(child.getWeekHeightWithIndicator());
                            Utils.scroll(parent.getChildAt(1), (int) (lastY - ev.getY()), child.getWeekHeightWithIndicator(), child.getMonthHeightWithIndicator());
                            //Log.d("2342342", "onTouchEvent: else top:" +Utils.loadTop()  + "  getWeekHeight：" + child.getWeekHeight());
                            Log.d("2342342", "onTouchEvent: 3");
                        }
                    }else if(Utils.loadTop() <= child.getWeekHeightWithIndicator() ){
                        //不能再拖了啊, 只能下滑
                        if(!directionUpa){
                            Log.d("2342342", "onTouchEvent: 2");
                            float leftTop = Utils.loadTop() + ((ev.getY() - lastY));
                            saveTop((int) leftTop);
                            Utils.scroll(parent.getChildAt(1), (int) (lastY - ev.getY()), child.getWeekHeightWithIndicator(), child.getMonthHeightWithIndicator());
                        }else {

                            Utils.scrollTo(parent, (RecyclerView) parent.getChildAt(1), child.getWeekHeightWithIndicator(), 30);
                            //((RecyclerView) parent.getChildAt(1)).requestDisallowInterceptTouchEvent(false);
                            Log.d("2342342", "onTouchEvent: 1");
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
        //isVerticalScroll = true;
        Log.d(TAG, "MonthPagerBehavior- onInterceptTouchEvent: ev: "+ ev.getAction() + " return: " + isVerticalScroll);
        return isVerticalScroll;
    }

    private int dependentViewTop = -1;
    private int totalOffsetY = 0;

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, WrapMonthPager wrapMonthPager, View dependency) {
        //Log.d("ldf", "onDependentViewChanged: ");
        //if(test) return true;
        MonthPager child = wrapMonthPager.getMonthPager();
        CalendarViewAdapter calendarViewAdapter = (CalendarViewAdapter) child.getAdapter();
        Calendar calendar = calendarViewAdapter.getCurrCalendarView();
        View wrapView = calendar.getChildAt(0);
        //dependency对其依赖的view(本例依赖的view是RecycleView)
        if (dependentViewTop != -1 && Utils.loadTop() <= child.getMonthHeightWithIndicator()) {
            int dy = dependency.getTop() - dependentViewTop;
            int top = wrapView.getTop();
            /*if (dy > touchSlop) {
                calendarViewAdapter.switchToMonth();
            } else if (dy < -touchSlop) {
                calendarViewAdapter.switchToWeek(child.getRowIndex());
            }*/
            //Log.d("ldf", "onDependentViewChanged = " + wrapView.getTop() + " top: " + top + " opMovableDistance: " + child.getTopMovableDistance()+ " Y:" +dy);
            if (dy > -top) {
                dy = -top;
            }

            Log.d(TAG, "onDependentViewChanged: dy:" + dy + " top:" + -top + " getTopMovableDistance: " + child.getTopMovableDistance());
            if (dy < -top - child.getTopMovableDistance()) {
                dy = -top - child.getTopMovableDistance();
                Log.e(TAG, "onDependentViewChanged: dy:" + dy + " top:" + -top + " getTopMovableDistance: " + child.getTopMovableDistance());

                //Log.e("ldf", "onDependentViewChanged = " + wrapView.getTop() + " top: " + top + " opMovableDistance: " + child.getTopMovableDistance() + " Y:" +dy);

                //dy = 0;

            }

            wrapView.offsetTopAndBottom(dy);
            //wrapMonthPager.offsetTopAndBottom(dy);


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
                Log.e(TAG, "123456 " + offsetTop + " loadTOp: " + Utils.loadTop() + " dependency.getTop(): " + dependency.getTop() + " dependentViewTop:" + dependentViewTop + " child.getIndicatorHeight():" +child.getIndicatorHeight() );
                offsetTop = child.getMonthHeight();
            }
            Log.d(TAG, "123456 " + offsetTop + " loadTOp: " + Utils.loadTop() + " dependency.getTop(): " + dependency.getTop() + " dependentViewTop:" + dependentViewTop + " child.getIndicatorHeight():" +child.getIndicatorHeight() );

            if(Math.abs(offsetTop - child.getMonthHeight()) < 3){
                offsetTop = child.getMonthHeight();
            }

            CalendarViewAdapter adapter = (CalendarViewAdapter) child.getAdapter();
                /*Calendar calendar = adapter.getCurrCalendarView();
                View wrapView = calendar.getChildAt(0);*/
            ViewGroup.LayoutParams layoutParams = wrapView.getLayoutParams();
            layoutParams.height = offsetTop;
            wrapView.setLayoutParams(layoutParams);
                /*ViewGroup.LayoutParams childLayoutParams = child.getLayoutParams();
                childLayoutParams.height = offsetTop;
                child.setLayoutParams(childLayoutParams);*/
            //saveTop(offsetTop);
        }else if( Utils.loadTop() <= child.getMonthHeightWithIndicator() &&  Utils.loadTop() > child.getWeekHeightWithIndicator()){
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
        }else if(Utils.loadTop() <= child.getWeekHeightWithIndicator() ){
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
        if(dependentViewTop != -1){
            int dy = dependency.getTop() - dependentViewTop;
            totalOffsetY += dy;
            View indicator = wrapMonthPager.getBottomIndicator();
            indicator.setTranslationY(totalOffsetY);
            //indicator.offsetTopAndBottom(dy);
            Log.d(TAG, "onDependentViewChanged000: dy:"+dy + " top:" +indicator.getTop());
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
