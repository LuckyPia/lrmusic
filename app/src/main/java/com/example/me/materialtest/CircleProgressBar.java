package com.example.me.materialtest;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;


public class CircleProgressBar extends View {
    private Paint mBackPaint;
    private Paint mFrontPaint;
    private Paint mTextPaint;
    private float mStrokeWidth =8;
    private float mHalfStrokeWidth = mStrokeWidth / 2;
    private float mRadius = 38;
    private RectF mRect;
    private int mProgress = 0;
    private int mTargetProgress = 0;
    private int mMax = 100;
    private int mWidth;
    private int mHeight;


    public CircleProgressBar(Context context) {
        super(context);
        init();
    }

    public CircleProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public CircleProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }




    // 完成相关参数初始化
    private void init() {
        mBackPaint = new Paint();
        mBackPaint.setColor(getResources().getColor(R.color.div_white));
        mBackPaint.setAntiAlias(true);
        mBackPaint.setStyle(Paint.Style.STROKE);
        mBackPaint.setStrokeWidth(mStrokeWidth);
        mFrontPaint = new Paint();
        mFrontPaint.setColor(getResources().getColor(R.color.colorPrimary));
        mFrontPaint.setAntiAlias(true);
        mFrontPaint.setStyle(Paint.Style.STROKE);
        mFrontPaint.setStrokeWidth(mStrokeWidth);
        mTextPaint = new Paint();
        mTextPaint.setColor(getResources().getColor(R.color.colorPrimary));
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(24);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
    }


    // 重写测量大小的onMeasure方法和绘制View的核心方法onDraw()
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mWidth = getRealSize(widthMeasureSpec);
        mHeight = getRealSize(heightMeasureSpec);
        setMeasuredDimension(mWidth, mHeight);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        initRect();
        float angle = mProgress / (float) mMax * 360;
        canvas.drawCircle(mWidth / 2, mHeight / 2, mRadius, mBackPaint);
        canvas.drawArc(mRect, -90, angle, false, mFrontPaint);

    }

    public int getRealSize(int measureSpec) {
        int result = 1;
        int mode = View.MeasureSpec.getMode(measureSpec);
        int size = MeasureSpec.getSize(measureSpec);
        if (mode == MeasureSpec.AT_MOST || mode == MeasureSpec.UNSPECIFIED) {
            //自己计算
            result = (int) (mRadius * 2 + mStrokeWidth);
        } else {
            result = size;
        }
        return result;
    }

    public void setProgress(int progress){
        this.mProgress = progress;
        postInvalidate();
    }
    public void setMax(int max){
        this.mMax=max;

    }

    private void initRect() {
        if (mRect == null) {
            mRect = new RectF();
            int viewSize = (int) (mRadius * 2);
            int left = (mWidth - viewSize) / 2;
            int top = (mHeight - viewSize) / 2;
            int right = left + viewSize;
            int bottom = top + viewSize;
            mRect.set(left, top, right, bottom);
        }
    }
}
