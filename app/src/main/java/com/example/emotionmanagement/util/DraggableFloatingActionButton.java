package com.example.emotionmanagement.util;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class DraggableFloatingActionButton extends FloatingActionButton implements View.OnTouchListener {

    private int downRawX, downRawY;
    private int dX, dY;

    public DraggableFloatingActionButton(Context context) {
        super(context);
        init();
    }

    public DraggableFloatingActionButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public DraggableFloatingActionButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        setOnTouchListener(this);
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                downRawX = (int) event.getRawX();
                downRawY = (int) event.getRawY();
                dX = (int) (view.getX() - event.getRawX());
                dY = (int) (view.getY() - event.getRawY());
                return true;

            case MotionEvent.ACTION_MOVE:
                int newX = (int) event.getRawX() + dX;
                int newY = (int) event.getRawY() + dY;

                view.animate()
                        .x(newX)
                        .y(newY)
                        .setDuration(0)
                        .start();
                return true;

            case MotionEvent.ACTION_UP:
                int upRawX = (int) event.getRawX();
                int upRawY = (int) event.getRawY();
                int upDX = upRawX - downRawX;
                int upDY = upRawY - downRawY;

                if (Math.abs(upDX) < 10 && Math.abs(upDY) < 10) {  // 判断手指移动的距离是否很小
                    // 手指移动距离小，认为是点击
                    view.performClick();
                }
                return true;
        }
        return false;
    }


}
