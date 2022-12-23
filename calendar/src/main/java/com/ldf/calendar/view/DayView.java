package com.ldf.calendar.view;

import android.content.Context;
import android.graphics.Canvas;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.ldf.calendar.Utils;
import com.ldf.calendar.interf.IViewRenderer;

/**
 * Created by ldf on 16/10/19.
 */

public abstract class DayView extends RelativeLayout implements IViewRenderer {

    protected Day day;
    protected Context context;
    protected int layoutResource;

    /**
     * 构造器 传入资源文件创建DayView
     *
     * @param layoutResource 资源文件
     * @param context 上下文
     */
    public DayView(Context context, int layoutResource) {
        super(context);
        this.context = context;
        this.layoutResource = layoutResource;
        setupLayoutResource(layoutResource);
    }

    /**
     * 为自定义的DayView设置资源文件
     *
     * @param layoutResource 资源文件
     * @return CalendarDate 修改后的日期
     */
    public void setupLayoutResource(int layoutResource) {
        View inflated = LayoutInflater.from(getContext()).inflate(layoutResource, this);
        inflated.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        inflated.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    public void refreshContent() {
        measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        layout(0, 0, getMeasuredWidth(), getMeasuredHeight());
    }

    @Override
    public void draw(Canvas canvas, Day day, int height) {
        this.day = day;
        refreshContent();
        int saveId = canvas.save();
        //canvas.translate(getTranslateX(canvas, day), day.getPosRow() * getMeasuredHeight());
        float y = (height - getHeight()) / 2;
        canvas.translate(getTranslateX(canvas, day), y);

        draw(canvas);
        canvas.restoreToCount(saveId);
    }

    protected int getTranslateX(Canvas canvas, Day day) {
        int dx;
        int canvasWidth = canvas.getWidth() / 7;
        int viewWidth = getMeasuredWidth();
        int moveX = (canvasWidth - viewWidth) / 2;
        dx = day.getPosCol() * canvasWidth + moveX;
        return dx;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Utils.cleanMarkData();
    }
}