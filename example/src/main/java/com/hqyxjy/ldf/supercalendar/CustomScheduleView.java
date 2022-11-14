package com.hqyxjy.ldf.supercalendar;

import static android.widget.LinearLayout.VERTICAL;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ldf.calendar.Utils;
import com.ldf.calendar.component.State;
import com.ldf.calendar.interf.IViewRenderer;
import com.ldf.calendar.model.CalendarDate;
import com.ldf.calendar.view.DayView;

import java.util.List;

/**
 * Created by ldf on 17/6/26.
 */

@SuppressLint("ViewConstructor")
public class CustomScheduleView extends DayView {

    private static String TAG = "CustomScheduleView";

    private final CalendarDate today = new CalendarDate();

    private LinearLayout linearLayout;
    private TextView textView;
    /**
     * 构造器
     *
     * @param context 上下文
     * @param layoutResource 自定义DayView的layout资源
     */
    public CustomScheduleView(Context context, int layoutResource) {
        super(context, layoutResource);
    }

    @Override
    public void setupLayoutResource(int layoutResource) {
        //super.setupLayoutResource(layoutResource);
        if(linearLayout == null){
            linearLayout = new LinearLayout(context);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            linearLayout.setLayoutParams(layoutParams);
            linearLayout.setOrientation(VERTICAL);
            addView(linearLayout);
        }

        /*textView = (TextView) LayoutInflater.from(getContext()).inflate(layoutResource, linearLayout);
        textView.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
                MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        textView.layout(0, 0, getMeasuredWidth(), getMeasuredHeight());*/
    }

    @Override
    public void refreshContent() {
        renderSchedule();
        super.refreshContent();
    }

    private void renderSchedule() {
        if(linearLayout != null){
            linearLayout.removeAllViews();
        }
        List<String> schedules = Utils.loadScheduleData().get(day.getDate().toString());
        //Log.d(TAG, "renderSchedule: mapKey:" + day.getDate());
        if(schedules == null) return;
        for(int i = 0; i < schedules.size(); i++){
            String text = schedules.get(i);
            Log.d(TAG, "renderSchedule: " + text);
            View view =  LayoutInflater.from(getContext()).inflate(layoutResource, linearLayout);
            TextView textView =  view.findViewById(R.id.tvSchedule);
            textView.setText(text);
            //linearLayout.addView(textView);
        }
    }


    @Override
    public IViewRenderer copy() {
        return new CustomScheduleView(context, layoutResource);
    }
}
