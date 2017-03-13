package com.sen5.ocup.gui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.sen5.ocup.R;

import org.bitlet.weupnp.LogUtils;

/**
 * Created by chenqianghua on 2016/9/12.
 */
public class CircleImageView2 extends ImageView{
    private LogUtils mLogUtils = new LogUtils();

    public CircleImageView2(Context context) {
        super(context);
    }

    private Paint mPaint;
    /**
     * 原型图
     */
    private Bitmap src;
    /**
     * 遮罩层
     */
    private Bitmap mask;
    public CircleImageView2(Context context, AttributeSet attrs) {
        super(context, attrs);
        // 初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        // 拿到原型图
        src = BitmapFactory.decodeResource(getResources(), R.drawable.user_me);
    }
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        // 图片的遮罩，为什么要在这里面初始化遮罩层呢？因为在这个方法里Width()和Height()才被初始化了
//        mLogUtils.e("onSizeChanged执行"+"oldw = "+oldw+"oldh ="+oldh+"w = "+w+"h ="+h);
//        mLogUtils.e(getMeasuredHeight()+"----"+getMeasuredWidth());
//        mLogUtils.e(getHeight()+"-----"+getWidth());
        mask = Bitmap.createBitmap(getMeasuredWidth(), getMeasuredHeight(), Bitmap.Config.ARGB_4444);
    }
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 设置画布的颜色为透明
        canvas.drawColor(Color.TRANSPARENT);
        // 划出你要显示的圆
        Canvas cc = new Canvas(mask);
        cc.drawCircle(getMeasuredWidth() / 2, getMeasuredHeight() / 2, getMeasuredHeight() / 2, mPaint);
        // 这个方法相当于PS新建图层，下面你要做的事就在这个图层上操作
        canvas.saveLayer(0, 0, getMeasuredWidth(), getMeasuredHeight(), null, Canvas.ALL_SAVE_FLAG);
        // 先绘制遮罩层
        canvas.drawBitmap(mask, 0, 0, mPaint);
        // 设置混合模式
        mPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        // 再绘制src源图
        canvas.drawBitmap(src, 0, 0, mPaint);
        // 还原混合模式
        mPaint.setXfermode(null);
        // 还原画布，相当于Ps的合并图层
        canvas.restore();
    }
}
