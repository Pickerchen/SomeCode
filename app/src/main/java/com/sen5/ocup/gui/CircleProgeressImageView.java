package com.sen5.ocup.gui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.sen5.ocup.R;
import com.sen5.ocup.util.LogUtils;

public class CircleProgeressImageView extends View {

    private RectF mRectF;

    private Paint mPaint;

    private int mCircleStoreWidth=11;
    /**
     * 最大进度值
     */
    private int mMaxProcessValue=100;
    /**
     * 进度值
     */
    private int mProcessValue;

    private int width;

    private int height;

    private int bitmap;

    private Bitmap drawBitmap;

    private LogUtils mLogUtils = new LogUtils();

    private Context context;

    public CircleProgeressImageView(Context context) {
        super(context);
        this.context = context;
    }

    public CircleProgeressImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init(context,attrs);
    }

    public CircleProgeressImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }


    private void init(Context context, AttributeSet attrs) {
        this.context=context;
        mCircleStoreWidth = (int) (getResources().getDisplayMetrics().density)*4;
        TypedArray typedArray =context.obtainStyledAttributes(attrs,R.styleable.CircleProgeressImageView2ATTRS);
        bitmap=typedArray.getResourceId(R.styleable.CircleProgeressImageView2ATTRS_imagers,R.drawable.icon_29);
        drawBitmap=BitmapFactory.decodeResource(context.getResources(),bitmap);
        mRectF=new RectF();
        mPaint=new Paint();
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width=this.getWidth();
        height=this.getHeight();
        mRectF.left=mCircleStoreWidth/2;
        mRectF.top=mCircleStoreWidth/2;
        mRectF.right=width-mCircleStoreWidth/2;
        mRectF.bottom=width-mCircleStoreWidth/2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawColor(Color.TRANSPARENT);

        //画圆
        mPaint.setColor(Color.parseColor("#979798"));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mCircleStoreWidth);
        canvas.drawArc(mRectF,-90,360,false,mPaint);
        mPaint.setColor(Color.parseColor("#595757"));
        canvas.drawArc(mRectF,-90,((float) mProcessValue/mMaxProcessValue)*360,false,mPaint);
        float imageLeft=width/2-drawBitmap.getWidth()/2;
        float imageTop=width/2-drawBitmap.getHeight()/2;
        drawBitmap = Bitmap.createScaledBitmap(drawBitmap, 104, 104, true);
        canvas.drawBitmap(drawBitmap,imageLeft,imageTop,mPaint);
    }


    public int getmCircleStoreWidth() {
        return mCircleStoreWidth;
    }

    public void setmCircleStoreWidth(int mCircleStoreWidth) {
        this.mCircleStoreWidth = mCircleStoreWidth;
    }

    public int getmProcessValue() {
        return mProcessValue;
    }

    public void setmProcessValue(int mProcessValue) {
        this.mProcessValue = mProcessValue;
        invalidate();
    }

    private int tem;
    private int tem2;
    public  void setProgress(final int progress){
        if (progress == tem2){
            //防止重复设置
            return;
        }
        tem2 = progress;
        if (isShown()){
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    tem+=3;
                    mProcessValue = tem;
                    if (tem >= progress){
                        return;
                    }
                    invalidate();
                    postDelayed(this,100);
                }
            },100);
        }
    }
}
