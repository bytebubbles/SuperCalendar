package com.ldf.calendar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import com.ldf.calendar.Const;
import com.ldf.calendar.interf.IDayRenderer;

/**
 * @author pzj
 * @date 2022/11/3
 * @desc
 **/
public class RowCalendarView extends View {


    private IDayRenderer dayRenderer;
    private Day[] days;

    public RowCalendarView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (days != null) {
            for (int col = 0; col < Const.TOTAL_COL; col++) {
                if (days[col] != null) {
                    dayRenderer.drawDay(canvas, days[col]);
                }
            }
        }
    }

    public void drawDay(Day[] days, IDayRenderer dayRenderer) {
        this.dayRenderer = dayRenderer;
        this.days = days;
       invalidate();
    }

    public void setDayRenderer(IDayRenderer dayRenderer) {
        this.dayRenderer = dayRenderer;
    }
}
