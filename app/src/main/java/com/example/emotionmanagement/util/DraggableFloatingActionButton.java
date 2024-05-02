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
        if (action == MotionEvent.ACTION_DOWN) {
            downRawX = (int) event.getRawX();
            downRawY = (int) event.getRawY();
            dX = (int) (view.getX() - event.getRawX());
            dY = (int) (view.getY() - event.getRawY());
            return true;
        } else if (action == MotionEvent.ACTION_MOVE) {
            int viewWidth = view.getWidth();
            int viewHeight = view.getHeight();
            View viewParent = (View) view.getParent();
            int parentWidth = viewParent.getWidth();
            int parentHeight = viewParent.getHeight();
            int newX = (int) event.getRawX() + dX;
            int newY = (int) event.getRawY() + dY;
            if (newX < 0) {
                newX = 0;
            } else if (newX > parentWidth - viewWidth) {
                newX = parentWidth - viewWidth;
            }
            if (newY < 0) {
                newY = 0;
            } else if (newY > parentHeight - viewHeight) {
                newY = parentHeight - viewHeight;
            }
            view.animate()
                    .x(newX)
                    .y(newY)
                    .setDuration(0)
                    .start();
            return true;
        }
        return false;
    }
}
