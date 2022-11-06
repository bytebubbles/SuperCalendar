package com.ldf.calendar.view;

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
import com.ldf.calendar.interf.IDayRenderer;
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
    private static CalendarAttr.CalendarType currCalendarType = CalendarAttr.CalendarType.SCHEDULE_MONTH;
    private CalendarAttr.CalendarType calendarType;
    private int cellHeight; // 单元格高度
    private int cellWidth; // 单元格宽度

    private OnSelectDateListener onSelectDateListener;    // 单元格点击回调事件
    private Context context;
    private CalendarAttr calendarAttr;
    private CalendarRenderer renderer;

    private int scheduleHeight; //日程的高度
    private int minScheduleHeight; //最小日程的高度

    private OnAdapterSelectListener onAdapterSelectListener;
    private float touchSlop;
    private MonthPager monthPager;
    private int viewHeight;
    private final static String scheduleTag = "schedule";
    private final static String scheduleCellTag = "schedule_cell";
    private final static String calendarTag = "calendar";

    public Calendar(Context context,
                    OnSelectDateListener onSelectDateListener,
                    CalendarAttr attr) {
        super(context);
        setCellAndScheduleHeight(Utils.dpi2px(context, Config.cellHeight), Utils.dpi2px(context,  Config.scheduleHeight), Utils.dpi2px(context,  Config.minScheduleHeight));

        this.onSelectDateListener = onSelectDateListener;
        calendarAttr = attr;
        init(context);
        initLayout();
    }

    public void setCellAndScheduleHeight(int cellHeight, int scheduleHeight, int minScheduleHeight){
        this.cellHeight = cellHeight;
        this.scheduleHeight = scheduleHeight;
        this.minScheduleHeight = minScheduleHeight;
        this.viewHeight = cellHeight * 6 + scheduleHeight * 6;
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
        calendarAttr.setCellHeight(cellHeight);
        calendarAttr.setCellWidth(cellWidth);
        renderer.setAttr(calendarAttr);
    }

    private float posX = 0;
    private float posY = 0;

    /*
     * 触摸事件为了确定点击的位置日期
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
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
                    int row = (int) (posY / cellHeight);
                    onAdapterSelectListener.cancelSelectState();
                    renderer.onClickDate(col, row);
                    onAdapterSelectListener.updateSelectState();
                    invalidate();
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

        if(calendarType != CalendarAttr.CalendarType.WEEK){
            View wrapView = getChildAt(0);
            ViewGroup.LayoutParams layoutParams = wrapView.getLayoutParams();
            ViewGroup.LayoutParams layoutParams1 = monthPager.getLayoutParams();
            if(calendarType == CalendarAttr.CalendarType.MONTH){
                layoutParams.height = monthPager.getMonthHeight();
            }else if(calendarType == CalendarAttr.CalendarType.SCHEDULE_MONTH){
                layoutParams.height = monthPager.getViewHeight();
            }
            layoutParams1.height = layoutParams.height;
            wrapView.setLayoutParams(layoutParams);
            monthPager.setLayoutParams(layoutParams1);
        }
    }

    public int getCellHeight() {
        return cellHeight;
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
        renderer.updateWeek(rowCount);

        //invalidate();
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

    public void setDayRenderer(IDayRenderer dayRenderer) {
        renderer.setDayRenderer(dayRenderer);
    }


    /**
     * 数据准备完毕后，插入布局
     */
    public void initLayout() {
        LinearLayout wrapLy = new LinearLayout(context);
        wrapLy.setOrientation(LinearLayout.VERTICAL);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, viewHeight);
        wrapLy.setLayoutParams(layoutParams);
        addView(wrapLy);

        Week[] weeks = renderer.getWeeks();
        for (int row = 0; row < Const.TOTAL_ROW; row++) {

            //插入周，分两层，一层是用画布绘制的日历，一层是LinerLayout存放日程item
            //日历层
            View rowCalendarView = new RowCalendarView(context);
            ViewGroup.LayoutParams calendarLayoutPas = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,cellHeight);
            rowCalendarView.setLayoutParams(calendarLayoutPas);
            //rowCalendarView.setBackgroundColor(Color.BLUE);

            //日程层
            LinearLayout rowScheduleView = new LinearLayout(context);
            LinearLayout.LayoutParams scheduleLayoutPas = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.MATCH_PARENT,0);
            scheduleLayoutPas.weight = 1;
            rowScheduleView.setLayoutParams(scheduleLayoutPas);
            rowScheduleView.setBackgroundColor(Color.RED);

            rowCalendarView.setTag(calendarTag + row);
            rowScheduleView.setTag(scheduleTag + row);
            wrapLy.addView(rowCalendarView);
            wrapLy.addView(rowScheduleView);

            /*if (weeks[row] != null) {
                for (int col = 0; col < Const.TOTAL_COL; col++) {
                    if (weeks[row].days[col] != null) {
                        //dayRenderer.drawDay(canvas, weeks[row].days[col]);
                    }
                }
            }*/
        }

    }

    public void setMonthPager(MonthPager container) {
        this.monthPager = container;
        this.scheduleHeight = container.getScheduleHeight();
        this.minScheduleHeight = container.getMinScheduleHeight();
    }

    public static CalendarAttr.CalendarType getCurrCalendarType() {
       /* int scheduleToMonthTV = monthPager.getViewHeight() - 150;
        int monthToScheduleTV = monthPager.getMonthHeight() + 150;
        if(Utils.loadTop() > child.getMonthHeight()){
           // if()
        }
*/
        return currCalendarType;
    }

    public static void setCurrCalendarType(CalendarAttr.CalendarType currCalendarType) {
        Calendar.currCalendarType = currCalendarType;
    }
}