package com.ldf.calendar.behavior;


import android.animation.ValueAnimator;
import android.content.Context;

import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.OverScroller;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.math.MathUtils;
import androidx.core.view.ViewCompat;
import androidx.viewpager.widget.ViewPager;

import com.ldf.calendar.Utils;
import com.ldf.calendar.component.CalendarAttr;
import com.ldf.calendar.component.CalendarViewAdapter;
import com.ldf.calendar.view.CalendarView;
import com.ldf.calendar.view.MonthPager;
import com.ldf.calendar.view.WrapMonthPager;

public class CalenderScrollBehavior extends CoordinatorLayout.Behavior<View> {
    private int monthOffset = -1;
    private int weekOffset = -1;
    private int scheduleMonthOffset = -1;
    private int indicatorHeight = -1;
    private Context context;
    private boolean initiated = false;
    boolean hidingTop = false;
    boolean showingTop = false;
    private CalendarAttr.CalendarType calendarType = CalendarView.getCurrCalendarType();

    private OverScroller mOverScroller;
    private ValueAnimator mSpringBackAnimator;
    private int mMinFlingVelocity;
    public static final int DIRECTION_UP = 1 << 0; // Direction to start
    public static final int DIRECTION_DOWN = 1 << 1; // Direction to end
    private static final int MAX_BOUNCE_BACK_DURATION_MS = 300;
    private static final int MIN_BOUNCE_BACK_DURATION_MS = 150;
    private final Interpolator mSpringBackInterpolator = new DecelerateInterpolator(0.8f);

    public CalenderScrollBehavior(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
        parent.onLayoutChild(child, layoutDirection);
        MonthPager monthPager = getMonthPager(parent);
        initMinOffsetAndInitOffset(parent, child, monthPager);
        return true;
    }

    private void initMinOffsetAndInitOffset(CoordinatorLayout parent,
                                            View child,
                                            MonthPager monthPager) {
        WrapMonthPager wrapMonthPager = (WrapMonthPager)monthPager.getParent();

        if (monthPager.getBottom() > 0 && monthOffset == -1) {

            monthOffset = monthPager.getMonthHeight();
            weekOffset = getMonthPager(parent).getWeekHeight();
            scheduleMonthOffset = monthPager.getViewHeight();
            indicatorHeight = monthPager.getIndicatorHeight();
            if(calendarType == CalendarAttr.CalendarType.MONTH){
                saveTop(monthOffset);
                wrapMonthPager.setIndicatorTranslationY(monthOffset);
            }else if(calendarType == CalendarAttr.CalendarType.WEEK) {
                saveTop(weekOffset);
                wrapMonthPager.setIndicatorTranslationY(weekOffset);
            }else {
                saveTop(scheduleMonthOffset);
                wrapMonthPager.setIndicatorTranslationY(scheduleMonthOffset);
            }

        }
        if (!initiated) {
            monthOffset = monthPager.getMonthHeight();
            weekOffset = getMonthPager(parent).getWeekHeight();
            scheduleMonthOffset = monthPager.getViewHeight();
            indicatorHeight = monthPager.getIndicatorHeight();
            if(calendarType == CalendarAttr.CalendarType.MONTH){
                saveTop(monthOffset);
                wrapMonthPager.setIndicatorTranslationY(monthOffset);
            }else if(calendarType == CalendarAttr.CalendarType.WEEK) {
                saveTop(weekOffset);
                wrapMonthPager.setIndicatorTranslationY(weekOffset);
            }else {
                saveTop(scheduleMonthOffset);
                wrapMonthPager.setIndicatorTranslationY(scheduleMonthOffset);
            }
            initiated = true;
        }
        child.offsetTopAndBottom(Utils.loadTop());
    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child,
                                       @NonNull View directTargetChild, @NonNull View target, int nestedScrollAxes, int type) {
        Log.d("TAG", "scroll: minOffset onStartNestedScroll: ");
        MonthPager monthPager = (MonthPager) getMonthPager(coordinatorLayout);
        monthPager.setScrollable(false);
        boolean isVertical = (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;

        return isVertical;
    }

    @Override
    public void onNestedScrollAccepted(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View directTargetChild, @NonNull View target, int axes, int type) {

        Log.d("TAG", "scroll: minOffset onNestedScrollAccepted: ");
        if (type == ViewCompat.TYPE_TOUCH) {
            stopSpringBack(child);
        }

        if (type == ViewCompat.TYPE_TOUCH) {
            if (mOverScroller != null) {
                mOverScroller.forceFinished(true);
            }
        }

        if ((axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0) {
            mDirectionToEnd = DIRECTION_DOWN;
            mDirectionToStart = DIRECTION_UP;
        }else {
            mDirectionToEnd = -1;
            mDirectionToStart = -1;
        }
    }

    public void stopSpringBack(View child) {
        if (mSpringBackAnimator != null) {
            if (mSpringBackAnimator.isRunning()) {
                mSpringBackAnimator.cancel();
            }
        }
    }

    public void springBack(final View child) {

        int startOffset = getOffset(child);
        if (startOffset == 0) {
            return;
        }


        if (mSpringBackAnimator == null) {
            mSpringBackAnimator = ValueAnimator.ofInt();
            mSpringBackAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    int value = (int) animation.getAnimatedValue();
                    setOffset(child, value);
                }
            });
        }

        if (mSpringBackAnimator.isStarted()) {
            return;
        }


        float bounceBackDuration = (Math.abs(startOffset) * 1f / getMaxOffset(child)) * MAX_BOUNCE_BACK_DURATION_MS;
        mSpringBackAnimator.setDuration(Math.max((int) bounceBackDuration, MIN_BOUNCE_BACK_DURATION_MS));
        mSpringBackAnimator.setInterpolator(mSpringBackInterpolator);
        mSpringBackAnimator.setIntValues(startOffset, 0);
        mSpringBackAnimator.start();
    }


    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dx, int dy, @NonNull int[] consumed, int type) {
        //super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
        Log.d("TAG", "scroll: minOffset onNestedPreScroll: ");
        child.setVerticalScrollBarEnabled(true);

        MonthPager monthPager = (MonthPager) getMonthPager(coordinatorLayout);
        if (monthPager.getPageScrollState() != ViewPager.SCROLL_STATE_IDLE) {
            consumed[1] = dy;
            //Toast.makeText(context, "loading month data", Toast.LENGTH_SHORT).show();
            return;
        }

        // 上滑，正在隐藏顶部的日历
        hidingTop = dy > 0 && child.getTop() <= scheduleMonthOffset + indicatorHeight
                && child.getTop() > getMonthPager(coordinatorLayout).getWeekHeightWithIndicator();
        // 下滑，正在展示顶部的日历
        //showingTop = dy < 0 && !ViewCompat.canScrollVertically(target, -1);

        if (hidingTop ) {
            consumed[1] = Utils.scroll(child, dy,
                    getMonthPager(coordinatorLayout).getWeekHeightWithIndicator(),
                    getMonthPager(coordinatorLayout).getViewHeightWithIndicator());
            saveTop(child.getTop() - indicatorHeight);
            return;
        }
        if (dy != 0) {
            int min, max;
            if (dy < 0) { // 滚动到尾部

                min = getOffset(target);
                max = 0;
            } else {  // 滚动到头部

                min = 0;
                max = getOffset(target);
            }
            if (min != max) {
                consumed[1] = computerOffset(target, getOffset(target) - dy, min, max);
                //Log.d("TAG", "scroll: minOffset onNestedPreScroll: consumed[1] - " + consumed[1]);
                return;
            }
        }

        consumed[1] = 0;
        //Log.d("TAG", "scroll: minOffset onNestedPreScroll: consumed[1] - " + consumed[1]);
    }

    @Override
    public void onStopNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int type) {
        //super.onStopNestedScroll(coordinatorLayout, child, target, type);
        Log.d("TAG", "scroll: minOffset onStopNestedScroll: ");
        MonthPager wrapView = getMonthPager(coordinatorLayout);
        wrapView.setScrollable(true);
        if(!(Utils.loadTop() == wrapView.getWeekHeightWithIndicator()
                || Utils.loadTop() == wrapView.getMonthHeightWithIndicator()
                || Utils.loadTop() == wrapView.getViewHeightWithIndicator()
        )) {
            Utils.touchUp(coordinatorLayout, wrapView, hidingTop);
        }else {
            CalendarViewAdapter calendarViewAdapter = (CalendarViewAdapter) wrapView.getAdapter();
            if(Utils.loadTop() == wrapView.getWeekHeightWithIndicator()){
                calendarViewAdapter.switchToWeek(wrapView.getRowIndex());
            }else if(Utils.loadTop() == wrapView.getMonthHeightWithIndicator()){
                calendarViewAdapter.switchToMonth();
            }else if(Utils.loadTop() == wrapView.getViewHeightWithIndicator()){
                calendarViewAdapter.switchToSchedule();
            }
        }

        if (type == ViewCompat.TYPE_TOUCH) { // touching
            if (getOffset(target) != 0) { // and out of bound
                if( mOverScroller == null || !mOverScroller.computeScrollOffset()) { // no fling
                    springBack(target);
                }
            }
        } else {
            if (getOffset(target) != 0) {
                springBack(target);
            }
        }
    }

    @Override
    public boolean onNestedFling(CoordinatorLayout coordinatorLayout, View child, View target, float velocityX, float velocityY, boolean consumed) {
        //Log.d("TAG", "scroll: minOffset onNestedFling: consumed -------- " +consumed);
        return super.onNestedFling(coordinatorLayout, child, target, velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(CoordinatorLayout coordinatorLayout, View child, View target, float velocityX, float velocityY) {
        // 日历隐藏和展示过程，不允许RecyclerView进行fling
        //Log.d("TAG", "scroll: minOffset onNestedPreFling: " + hidingTop);
        if (hidingTop) {
            return true;
        }
        if (mOverScroller == null) {
            mOverScroller = new OverScroller(coordinatorLayout.getContext());
        }
        //Log.d("TAG", "onNestedPreFlingInner: velocity - " +velocityY);
        mOverScroller.fling(0, 0, 0, (int) velocityY, 0, 0, Integer.MIN_VALUE, Integer.MAX_VALUE);
        return false;
    }

    @Override
    public void onNestedScroll(@NonNull CoordinatorLayout coordinatorLayout, @NonNull View child, @NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int type) {
        //super.onNestedScroll(coordinatorLayout, child, target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, type, consumed);
        onNestedScrollInner(coordinatorLayout, child, target, dyConsumed, dyUnconsumed, type);
    }

    private void onNestedScrollInner(CoordinatorLayout coordinatorLayout, View child, View target, int dyConsumed, int distanceUnconsumed, int type) {

        //Log.d("TAG", "scroll: minOffset onNestedScrollInner: dyConsumed - " + dyConsumed + " distanceUnconsumed - " + distanceUnconsumed + " type - " + type);

        if (distanceUnconsumed != 0) {
            coordinatorLayout.requestDisallowInterceptTouchEvent(true);
        }

        if(distanceUnconsumed < 0){
            //在顶部
            if (type == ViewCompat.TYPE_TOUCH) {
                scroll(target, distanceUnconsumed, 0, getMaxOffset(target));
            }else {
                // fling
                if (mOverScroller == null
                        || !mOverScroller.computeScrollOffset()
                        || Math.abs(mOverScroller.getCurrVelocity()) < Math.abs(getMinFlingVelocity(this, target, mDirectionToEnd))  // too slow
                        || getOffset(target) >= getMaxFlingOffset( target, mDirectionToEnd)) { // reach edge
                    ViewCompat.stopNestedScroll(target, ViewCompat.TYPE_NON_TOUCH);
                } else {
                    scroll(target, distanceUnconsumed,
                            getOffset(target), getMaxFlingOffset(target, mDirectionToEnd));
                }
            }
        }else if (distanceUnconsumed > 0)  {

            if (type == ViewCompat.TYPE_TOUCH) {
                scroll(target, distanceUnconsumed, getMinOffset(target), 0);
            } else { // fling
                if (mOverScroller == null
                        || !mOverScroller.computeScrollOffset()
                        || Math.abs(mOverScroller.getCurrVelocity()) < Math.abs(getMinFlingVelocity(this, target, mDirectionToStart))  // too slow
                        || getOffset(target) <= getMaxFlingOffset(target, mDirectionToStart)) { // reach edge
                    ViewCompat.stopNestedScroll(target, ViewCompat.TYPE_NON_TOUCH);
                } else {
                    scroll(target, distanceUnconsumed,  // slow down
                            getMaxFlingOffset( target, mDirectionToStart), getOffset(target));
                }
            }

        }
    }

    private int scroll(View child, int distance, int minOffset, int maxOffset) {
        //Log.d("TAG", "scroll: minOffset - "+ minOffset + " maxOffset- " + maxOffset + " distance-"+distance);
        return computerOffset(child, getOffset(child) - computerWithDampingFactor(child, distance), minOffset, maxOffset);
    }

    /**
     * @return 消耗掉距离
     */
    private int computerOffset(View child, int newOffset, int minOffset, int maxOffset) {
        //Log.d("TAG", "computerOffset: newOffset - " + newOffset + " minOffset - " + minOffset + " maxOffset - " + maxOffset);
        final int curOffset = getOffset(child);
        int consumed = 0;

        if (curOffset >= minOffset && curOffset <= maxOffset) {
            newOffset = MathUtils.clamp(newOffset, minOffset, maxOffset);

            if (curOffset != newOffset) {
                setOffset(child, newOffset);
                // Update how much dy we have consumed
                consumed = curOffset - newOffset;
            }
        }

        return consumed;
    }
    int mDirectionToEnd, mDirectionToStart;
    private final int computerWithDampingFactor(View child, int distance) {
        //int direction = distance > 0 ? mDirectionToStart : mDirectionToEnd;
        float factor = getDampingFactor(child);
        if (factor == 0) {
            factor = 1;
        }
        int newDistance = (int) (distance / factor + 0.5f);
        return newDistance;
    }


    public void setOffset(View child, int offset) {
        updateOffset(child, offset);
    }

    public void updateOffset(View child, int offset) {
        child.setTranslationY(offset);
    }

    public int getOffset(View child) {
        return (int) child.getTranslationY();
    }

    public int getMaxOffset(View child){
        return Utils.dpi2px(context, 200);
    }

    public int getMinOffset(View child) {
        return - Utils.dpi2px(context, 200);
    }

    private int getMinFlingVelocity(CalenderScrollBehavior calenderScrollBehavior, View child, int mDirectionToEnd) {
        if (mMinFlingVelocity <= 0) {
            mMinFlingVelocity = ViewConfiguration.get(child.getContext()).getScaledMinimumFlingVelocity() * 15;
        }
        return mMinFlingVelocity;
    }

    public int getMaxFlingOffset(View child,  int scrollDirection) {
        if (scrollDirection == DIRECTION_DOWN) {
            return child.getHeight() / 5;
        } else {
            return -child.getHeight() / 5;
        }
    }


    /**
     * 获取阻尼因子，值越大，摩擦越大
     * @param child
     * @return
     */
    public float getDampingFactor(View child) {
        int absOffset = Math.abs(getOffset(child));
        float progress = absOffset * 1f / child.getHeight();
        return 1 + 6 * progress; // factor = {1, 5}
    }



    private MonthPager getMonthPager(CoordinatorLayout coordinatorLayout) {
        return ((WrapMonthPager) coordinatorLayout.getChildAt(0)).getMonthPager();
    }

    private void saveTop(int top) {
        Utils.saveTop(top + indicatorHeight);
        if (Utils.loadTop() == monthOffset) {
            Utils.setScrollToBottom(false);
        } else if (Utils.loadTop() == weekOffset) {
            Utils.setScrollToBottom(true);
        } else if(Utils.loadTop() == scheduleMonthOffset){
        }
    }
}
