package com.ldf.calendar.interf;

import android.graphics.Canvas;

import com.ldf.calendar.view.Day;

/**
 * Created by ldf on 17/6/26.
 */

public interface IViewRenderer {

    void refreshContent();

    void draw(Canvas canvas, Day day);

    IViewRenderer copy();

}
