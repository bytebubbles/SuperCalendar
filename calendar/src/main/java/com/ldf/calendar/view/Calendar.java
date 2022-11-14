package com.ldf.calendar.view;

import static com.ldf.calendar.component.CalendarAttr.CalendarType.WEEK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.ldf.calendar.Config;
import com.ldf.calendar.Const;
import com.ldf.calendar.component.CalendarViewAdapter;
import com.ldf.calendar.interf.IViewRenderer;
import com.ldf.calendar.interf.OnAdapterSelectListener;
import com.ldf.calendar.component.CalendarAttr;
import com.ldf.calendar.component.CalendarRenderer;
import com.ldf.calendar.interf.OnSelectDateListener;
import com.ldf.calendar.model.CalendarDate;
import com.ldf.calendar.Utils;

@SuppressLint("ViewConstructor")
public class Calendar extends FrameLayout {
    /**
     * 日历列数
     */
    private static CalendarAttr.CalendarType currCalendarType = WEEK;
    private CalendarAttr.CalendarType calendarType;
    private int weekHeight; // 单元格高度
    private int cellWidth; // 单元格宽度

    private OnSelectDateListener onSelectDateListener;    // 单元格点击回调事件
    private Context context;
    private CalendarAttr calendarAttr;
    private CalendarRenderer renderer;

    private int scheduleHeight; //日程的高度
    private int minScheduleHeight; //最小日程的高度
    private int indicatorHeight; //指示器高度

    private OnAdapterSelectListener onAdapterSelectListener;
    private float touchSlop;
    private MonthPager monthPager;
    private int viewHeight;
    private int totalCol = Const.TOTAL_COL;
    private int totalRow = Const.TOTAL_ROW;

    private final static String scheduleTag = "schedule";
    private final static String scheduleCellTag = "schedule_cell";
    private final static String calendarTag = "calendar";
    private int monthHeight;

    public Calendar(Context context,
                    OnSelectDateListener onSelectDateListener,
                    CalendarAttr attr) {
        super(context);
        setCellAndScheduleHeight(Utils.dpi2px(context, Config.weekHeight),
                Utils.dpi2px(context,  Config.scheduleHeight),
                Utils.dpi2px(context,  Config.minScheduleHeight),
                Utils.dpi2px(context, Config.indicatorHeight)
                );

        this.onSelectDateListener = onSelectDateListener;
        calendarAttr = attr;
        init(context);
        initLayout();
    }

    public void setCellAndScheduleHeight(int cellHeight, int scheduleHeight, int minScheduleHeight, int indicatorHeight){
        this.weekHeight = cellHeight;
        this.scheduleHeight = scheduleHeight;
        this.minScheduleHeight = minScheduleHeight;
        this.viewHeight = cellHeight * 6 + scheduleHeight * 6;
        this.monthHeight = cellHeight * 6 + minScheduleHeight * 6;
        this.indicatorHeight = indicatorHeight;
    }

    private void init(Context context) {
        this.context = context;
        touchSlop = Utils.getTouchSlop(context);
        initAttrAndRenderer();

    }

    private void initAttrAndRenderer() {
        renderer = new CalendarRenderer(this, calendarAttr, context);
        renderer.setOnSelectDateListener(onSelectDateListener);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //renderer.draw(canvas);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        super.onSizeChanged(w, h, oldW, oldH);
       // cellHeight = h / Const.TOTAL_ROW;
        cellWidth = w / Const.TOTAL_COL;
        calendarAttr.setCellHeight(weekHeight);
        calendarAttr.setCellWidth(cellWidth);
        renderer.setAttr(calendarAttr);
    }

    private float posX = 0;
    private float posY = 0;

    /*
     * 触摸事件为了确定点击的位置日期
     */
    //@Override
    public boolean onSubTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                posX = event.getX();
                posY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                float disX = event.getX() - posX;
                float disY = event.getY() - posY;
                if (Math.abs(disX) < touchSlop && Math.abs(disY) < touchSlop) {
                    int col = (int) (posX / cellWidth);
                    int row ;
                    if(Calendar.getCurrCalendarType() == CalendarAttr.CalendarType.MONTH
                            || Calendar.getCurrCalendarType() == WEEK ){
                        row = (int) (posY / (weekHeight + minScheduleHeight));
                    }else{
                        row = (int) (posY / (weekHeight + scheduleHeight));
                    }
                    cancelSelectState();
                    renderer.onClickDate(col, row);
                    onAdapterSelectListener.updateSelectState();
                }
                break;
        }
        return true;
    }

    public CalendarAttr.CalendarType getCalendarType() {
        return calendarAttr.getCalendarType();
    }

    public void switchCalendarType(CalendarAttr.CalendarType calendarType) {
        calendarAttr.setCalendarType(calendarType);
        renderer.setAttr(calendarAttr);
        //if(test) return;
        if(calendarType != WEEK){
            View wrapView = getChildAt(0);
            ViewGroup.LayoutParams layoutParams = wrapView.getLayoutParams();
            if(calendarType == CalendarAttr.CalendarType.MONTH){
                if(layoutParams.height !=  monthPager.getMonthHeight()){
                    layoutParams.height = monthPager.getMonthHeight();
                    wrapView.setLayoutParams(layoutParams);
                }

            }else if(calendarType == CalendarAttr.CalendarType.SCHEDULE_MONTH){
                if(layoutParams.height != monthPager.getViewHeight()){
                    layoutParams.height = monthPager.getViewHeight();
                    wrapView.setLayoutParams(layoutParams);
                }
            }
        }else {

            View wrapView = getChildAt(0);
            ViewGroup.LayoutParams layoutParams = wrapView.getLayoutParams();
            if(layoutParams.height != monthPager.getMonthHeight()){
                layoutParams.height = monthPager.getMonthHeight();
                wrapView.setLayoutParams(layoutParams);
            }
        }
    }

    public int getWeekHeight() {
        return weekHeight;
    }

    public int getMonthHeight() {
        return monthHeight;
    }

    public int getViewHeight() {
        return viewHeight;
    }

    public void resetSelectedRowIndex() {
        renderer.resetSelectedRowIndex();
    }

    public int getSelectedRowIndex() {
        return renderer.getSelectedRowIndex();
    }

    public void setSelectedRowIndex(int selectedRowIndex) {
        renderer.setSelectedRowIndex(selectedRowIndex);
    }

    public void setOnAdapterSelectListener(OnAdapterSelectListener onAdapterSelectListener) {
        this.onAdapterSelectListener = onAdapterSelectListener;
    }

    public void showDate(CalendarDate current) {
        renderer.showDate(current);
    }

    public void updateWeek(int rowCount) {
        renderer.setSelectedRowIndex(rowCount);
        offsetYByRowIndex();
    }

    public void update() {
        renderer.update();
    }

    public void cancelSelectState() {
        renderer.cancelSelectState();
    }

    public CalendarDate getSeedDate() {
        return renderer.getSeedDate();
    }

    public CalendarDate getFirstDate() {
        return renderer.getFirstDate();
    }

    public CalendarDate getLastDate() {
        return renderer.getLastDate();
    }

    public void setDayRenderer(IViewRenderer dayRenderer) {
        renderer.setDayRenderer(dayRenderer);
    }

    public void setScheduleRenderer(IViewRenderer scheduleRenderer){
        renderer.setScheduleRenderer(scheduleRenderer);
    }


    /**
     * 先插入布局
     */
    public void initLayout() {
        final LinearLayout wrapLy = new LinearLayout(context);
        wrapLy.setTag(1);
        wrapLy.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return onSubTouchEvent(event);
            }
        });
        wrapLy.setOrientation(LinearLayout.VERTICAL);
        int height;
        switch (currCalendarType){
            case WEEK:
            case MONTH:
                height = getMonthHeight();
                break;
            default:
                height = getViewHeight();
                break;
        }
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        wrapLy.setLayoutParams(layoutParams);
        addView(wrapLy);
        for (int row = 0; row < 6; row++) {
            addRowView(wrapLy, row);
        }
        //setBackgroundColor(Color.GREEN);
    }

    private void addRowView(ViewGroup wrapLy,int row){
        //插入周，分两层，一层是用画布绘制的日历，一层是LinerLayout存放日程item
        //日历层
        View rowCalendarView = new RowCalendarView(context);
        ViewGroup.LayoutParams calendarLayoutPas = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, weekHeight);
        rowCalendarView.setLayoutParams(calendarLayoutPas);
        //rowCalendarView.setBackgroundColor(Color.BLUE);

        //日程层
        View rowScheduleView = new RowScheduleView(context);
        LinearLayout.LayoutParams scheduleLayoutPas = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,0);
        scheduleLayoutPas.weight = 1;
        rowScheduleView.setLayoutParams(scheduleLayoutPas);
        rowScheduleView.setBackgroundColor(Color.RED);

        rowCalendarView.setTag(calendarTag + row);
        rowScheduleView.setTag(scheduleTag + row);
        wrapLy.addView(rowCalendarView);
        wrapLy.addView(rowScheduleView);

    }

    public void setMonthPager(MonthPager container) {
        this.monthPager = container;
        this.scheduleHeight = container.getScheduleHeight();
        this.minScheduleHeight = container.getMinScheduleHeight();
    }

    public static CalendarAttr.CalendarType getCurrCalendarType() {
        return currCalendarType;
    }

    public static void setCurrCalendarType(CalendarAttr.CalendarType currCalendarType) {
        Calendar.currCalendarType = currCalendarType;
    }

    public void setSelectedCalendarDate(CalendarDate selectedDate){
        renderer.setSelectedDate(selectedDate);
    }
    public CalendarDate getSelectedCalendarDate() {
        return renderer.getSelectedCalendarDate();
    }

    public void saveSelectedDateToNextPager(CalendarDate nextSelectedDate) {
        CalendarViewAdapter adapter = (CalendarViewAdapter) monthPager.getAdapter();
        adapter.saveSelectedDateToNextPager(nextSelectedDate);
    }

    public void saveSelectedDateToLastPager(CalendarDate lastSelectedDate){
        CalendarViewAdapter adapter = (CalendarViewAdapter) monthPager.getAdapter();
        adapter.saveSelectedDateToLastPager(lastSelectedDate);
    }

    public int getTotalCol() {
        return totalCol;
    }

    public void setTotalCol(int totalCol) {
        this.totalCol = totalCol;
    }

    public int getTotalRow() {
        return totalRow;
    }

    public int getIndicatorHeight() {
        return indicatorHeight;
    }

    public void setTotalRow(int totalRow) {

        ViewGroup wrapView = (ViewGroup) getChildAt(0);
        if(totalRow == 5){
            wrapView.getChildAt(wrapView.getChildCount()-2).setVisibility(View.INVISIBLE);
            wrapView.getChildAt(wrapView.getChildCount()-1).setVisibility(View.INVISIBLE);
        }else {
            wrapView.getChildAt(wrapView.getChildCount()-2).setVisibility(View.VISIBLE);
            wrapView.getChildAt(wrapView.getChildCount()-1).setVisibility(View.VISIBLE);
        }
        this.totalRow = totalRow;
    }

    public void offsetYByRowIndex(){

        final View view = getChildAt(0);
        final int offset = getTopMovableDistance();
        int top = view.getTop();
        if(top != -offset){
            view.post(new Runnable() {
                @Override
                public void run() {
                    view.setTop(-offset);
                }
            });
        }

    }

    public void resetOffsetY(){
        final View view = getChildAt(0);
        view.post(new Runnable() {
            @Override
            public void run() {
                view.setTop(0);
            }
        });
    }

    public int getTopMovableDistance() {

        return weekHeight * getSelectedRowIndex() + minScheduleHeight * getSelectedRowIndex();
    }
}