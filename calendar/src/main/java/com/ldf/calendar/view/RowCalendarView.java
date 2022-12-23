package com.ldf.calendar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.view.View;

import com.ldf.calendar.Const;
import com.ldf.calendar.interf.IViewRenderer;

/**
 * @author pzj
 * @date 2022/11/3
 * @desc
 **/
public class RowCalendarView extends View {


    private IViewRenderer dayRenderer;
    private int height;
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
                    dayRenderer.draw(canvas, days[col], height);
                }
            }
        }
    }

    public void drawDay(Day[] days, IViewRenderer dayRenderer, int height) {
        this.dayRenderer = dayRenderer;
        this.days = days;
        this.height = height;
       invalidate();
    }

    public void setDayRenderer(IViewRenderer dayRenderer) {
        this.dayRenderer = dayRenderer;
    }
}
