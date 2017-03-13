package com.sen5.ocup.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by chenqianghua on 2016/9/13.
 */
public class ChatView2 extends View implements View.OnTouchListener {

    private Context mContext;

    public ChatView2(Context context) {
        this(context,null);
    }

    public ChatView2(Context context, AttributeSet attrs) {
        this(context,attrs,0);
    }

    public ChatView2(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initData();
    }

    private void initData() {

    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return false;
    }
}
